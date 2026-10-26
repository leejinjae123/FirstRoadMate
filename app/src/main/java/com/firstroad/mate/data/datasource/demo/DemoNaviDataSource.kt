package com.firstroad.mate.data.datasource.demo

import com.firstroad.mate.data.datasource.navi.NaviDataSource
import com.firstroad.mate.domain.model.NaviCommandResult
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.RoutePlan
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoNaviDataSource @Inject constructor(
    private val tripState: DemoTripState,
) : NaviDataSource {

    override fun navigationState(): Flow<NavigationState> = flow {
        while (currentCoroutineContext().isActive) {
            val now = System.currentTimeMillis()
            emit(DemoTripScript.navigationAt(tripState.elapsedSeconds(now), tripState.state.value))
            delay(TICK_MILLIS)
        }
    }

    override suspend fun startRoute(plan: RoutePlan): NaviCommandResult {
        tripState.start(System.currentTimeMillis(), plan.waypoints.map { it.place.name })
        return NaviCommandResult.REQUESTED
    }

    override suspend fun addWaypoint(place: Place): NaviCommandResult {
        val elapsed = tripState.elapsedSeconds(System.currentTimeMillis()) ?: return NaviCommandResult.NOT_AVAILABLE
        val added = tripState.tryAddWaypoint(place.name, RoutePlan.MAX_WAYPOINTS, DemoTripScript.passedWaypoints(elapsed))
        return if (added) NaviCommandResult.REQUESTED else NaviCommandResult.WAYPOINT_FULL
    }

    override suspend fun removeAllWaypoints(): NaviCommandResult {
        if (tripState.state.value.startedAtMillis == null) return NaviCommandResult.NOT_AVAILABLE
        tripState.setWaypoints(emptyList())
        return NaviCommandResult.REQUESTED
    }

    override suspend fun cancelRoute(): NaviCommandResult {
        tripState.stop()
        return NaviCommandResult.REQUESTED
    }

    private companion object {
        const val TICK_MILLIS = 500L
    }
}
