package com.firstroad.mate.data.datasource.navi.pleos

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.WaypointIndex
import ai.pleos.playground.navi.data.CautionPoint as SdkCautionPoint
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DrivingInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.data.RouteStateInfo
import ai.pleos.playground.navi.data.RouteWithWaypointsRequest
import ai.pleos.playground.navi.data.TBTInfo
import ai.pleos.playground.navi.data.WaypointInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.util.Log
import com.firstroad.mate.data.datasource.navi.NaviDataSource
import com.firstroad.mate.domain.model.NaviCommandResult
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.RoutePlan
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/** 실차 내비 소스. NaviHelper 이벤트를 모아 NavigationState로 내보내고, 경로·경유지 요청을 전달한다. */
@Singleton
class PleosNaviDataSource @Inject constructor(
    private val client: PleosNaviClient,
) : NaviDataSource {

    /** NaviHelper 경유지 슬롯(최대 2개) 중 사용 중인 수. onWayPointInfo로 보정한다. */
    private val usedWaypointSlots = AtomicInteger(0)

    override fun navigationState(): Flow<NavigationState> = flow {
        val helper = client.helperOrNull() ?: return@flow
        emitAll(
            callbackFlow {
                // NaviHelper 콜백은 바인더 스레드 여러 곳에서 올 수 있어 잠금 안에서 갱신한다.
                val lock = Any()
                var state = NavigationState()
                fun update(transform: (NavigationState) -> NavigationState) {
                    val next = synchronized(lock) { transform(state).also { state = it } }
                    trySend(next)
                }
                val listener = object : NaviHelperEventListener {
                    override fun onRouteStarted(routeStartInfo: RouteStartInfo) =
                        update { it.copy(isRouteActive = true, isArrived = false) }

                    override fun onRouteCancelled() = update { NavigationState() }

                    override fun onRouteStateInfo(routeStateInfo: RouteStateInfo) =
                        update { it.copy(isRouteActive = routeStateInfo.isRouting) }

                    override fun onTBTInfo(tbtInfoList: List<TBTInfo>) {
                        val turns = tbtInfoList.map { it.toTurnGuide() }.sortedBy { it.distanceMeters }
                        update { it.copy(nextTurn = turns.firstOrNull(), upcomingTurns = turns) }
                    }

                    override fun onCautionPointInfo(cautionPointList: List<SdkCautionPoint>) {
                        val nearest = cautionPointList.minByOrNull { it.remainDistance }?.toDomain()
                        update { it.copy(cautionAhead = nearest) }
                    }

                    override fun onCurrentRoadSpeedLimit(speedLimit: Int) =
                        update { it.copy(speedLimitKmh = speedLimit.takeIf { limit -> limit > 0 }) }

                    override fun onDrivingInfo(drivingInfo: DrivingInfo) = update {
                        it.copy(
                            remainingMeters = drivingInfo.destination.distance,
                            remainingSeconds = drivingInfo.destination.duration,
                        )
                    }

                    override fun onWayPointInfo(waypointInfoList: List<WaypointInfo>) {
                        usedWaypointSlots.set(waypointInfoList.size)
                        update { it.copy(waypointCount = waypointInfoList.size) }
                    }

                    override fun onDestinationArrived(destinationArrivedInfo: DestinationArrivedInfo) =
                        update { it.copy(isArrived = true, isRouteActive = false, nextTurn = null) }

                    override fun onError(errorCode: NaviErrorCode) {
                        Log.w(TAG, "내비 오류: $errorCode")
                    }
                }
                helper.addListener(listener)
                runCatching { helper.getRouteStateInfo() }
                awaitClose { helper.removeListener(listener) }
            },
        )
    }

    override suspend fun startRoute(plan: RoutePlan): NaviCommandResult = request { helper ->
        val destination = plan.destination.toRouteInfo(plan.preference)
        if (plan.waypoints.isEmpty()) {
            helper.requestRoute(destination)
        } else {
            val waypoints = plan.waypoints.mapIndexed { slot, suggestion -> suggestion.place.toWaypointInfo(slot) }
            helper.requestRouteWithWaypoints(RouteWithWaypointsRequest(destination = destination, waypoints = waypoints))
        }
        usedWaypointSlots.set(plan.waypoints.size)
    }

    override suspend fun addWaypoint(place: Place): NaviCommandResult {
        // 확인과 예약을 한 번에 해서 동시 요청이 같은 슬롯을 쓰지 않게 한다.
        val slot = usedWaypointSlots.getAndUpdate { used -> if (used < RoutePlan.MAX_WAYPOINTS) used + 1 else used }
        if (slot >= RoutePlan.MAX_WAYPOINTS) return NaviCommandResult.WAYPOINT_FULL
        return request { helper -> helper.addWaypoint(place.toWaypointInfo(slot)) }
            .also { result -> if (result != NaviCommandResult.REQUESTED) usedWaypointSlots.decrementAndGet() }
    }

    override suspend fun removeAllWaypoints(): NaviCommandResult = request { helper ->
        helper.removeWaypoint(WaypointIndex.ALL)
        usedWaypointSlots.set(0)
    }

    override suspend fun cancelRoute(): NaviCommandResult = request { helper ->
        helper.cancelRoute()
        usedWaypointSlots.set(0)
    }

    private suspend fun request(block: (NaviHelper) -> Unit): NaviCommandResult {
        val helper = client.helperOrNull() ?: return NaviCommandResult.NOT_AVAILABLE
        return runCatching { block(helper) }
            .onFailure { Log.w(TAG, "내비 요청 실패", it) }
            .fold(onSuccess = { NaviCommandResult.REQUESTED }, onFailure = { NaviCommandResult.FAILED })
    }

    private companion object {
        const val TAG = "PleosNaviDataSource"
    }
}
