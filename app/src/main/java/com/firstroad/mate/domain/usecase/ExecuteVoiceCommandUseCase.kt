package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.ControlResult
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.NaviCommandResult
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.VoiceCommand
import com.firstroad.mate.domain.model.VoiceContext
import com.firstroad.mate.domain.repository.DriveRecordRepository
import com.firstroad.mate.domain.repository.NavigationRepository
import com.firstroad.mate.domain.repository.PlaceRepository
import com.firstroad.mate.domain.repository.VehicleRepository
import com.firstroad.mate.utils.GeoMath
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 음성 명령을 실행하고 운전자에게 돌려줄 대답을 만든다 (기획서 4.2).
 * 확인 질문에 대한 "응/아니"는 질문을 들고 있는 화면(ViewModel)이 먼저 처리한다.
 */
class ExecuteVoiceCommandUseCase @Inject constructor(
    private val navigationRepository: NavigationRepository,
    private val placeRepository: PlaceRepository,
    private val driveRecordRepository: DriveRecordRepository,
    private val vehicleRepository: VehicleRepository,
    private val classifyWarning: ClassifyWarningUseCase,
) {
    suspend operator fun invoke(command: VoiceCommand, context: VoiceContext): Guidance = when (command) {
        is VoiceCommand.AddWaypoint -> addWaypoint(command.category, context)
        VoiceCommand.RemoveWaypoint -> removeWaypoints()
        VoiceCommand.NextTurn -> context.navigation.nextTurn
            ?.let { Guidance.NextTurnAnswer(it.direction, it.distanceMeters) }
            ?: Guidance.NoActiveRoute
        VoiceCommand.Repeat -> context.lastGuidance ?: Guidance.NothingToRepeat
        VoiceCommand.ExplainWarning ->
            Guidance.ContinueAnswer(classifyWarning.mostSevere(context.vehicle.warnings)?.level)
        VoiceCommand.CalmDown -> calmDown()
        VoiceCommand.MarkDifficultSpot -> markSpot(context)
        VoiceCommand.WhereIsMyCar -> whereIsMyCar(context.nowMillis)
        is VoiceCommand.ParkingMemo -> {
            driveRecordRepository.updateParkingMemo(command.memo)
            Guidance.ParkingSaved(command.memo)
        }
        VoiceCommand.Help -> Guidance.Help(context.screen)
        // 창문 닫기는 하차 화면이 직접 처리한다. 그 밖의 화면(주행 중)에서는 세운 뒤에 하도록 안내한다.
        VoiceCommand.CloseWindow -> Guidance.ParkFirst
        // 출발·끝내기·나중에는 그 버튼이 있는 화면이 직접 처리한다. 여기까지 오면 지금 화면에 없는 동작이다.
        VoiceCommand.Start,
        VoiceCommand.Finish,
        VoiceCommand.Later,
        -> Guidance.Help(context.screen)
        VoiceCommand.Yes -> Guidance.NotUnderstood
        VoiceCommand.No -> Guidance.Dismissed
        is VoiceCommand.Unknown -> Guidance.NotUnderstood
    }

    private suspend fun addWaypoint(category: PlaceCategory, context: VoiceContext): Guidance {
        val place = findPlace(category, context) ?: return Guidance.NoPlaceFound(category)
        return when (navigationRepository.addWaypoint(place)) {
            NaviCommandResult.REQUESTED -> Guidance.WaypointAdded(place.name)
            NaviCommandResult.WAYPOINT_FULL -> Guidance.WaypointFull
            NaviCommandResult.NOT_AVAILABLE,
            NaviCommandResult.FAILED -> Guidance.NoPlaceFound(category)
        }
    }

    /**
     * - 주차장: 목적지 근처 (목적지를 모르면 현재 위치)
     * - 휴게소·정비소: 지나온 곳이 아니라 앞쪽에 있는 곳 (목적지까지 거리가 지금보다 가까운 곳)
     * - 그 밖: 현재 위치에서 가까운 곳
     */
    private suspend fun findPlace(category: PlaceCategory, context: VoiceContext): Place? {
        val here = context.location
        val destination = context.destination
        return when (category) {
            PlaceCategory.PARKING -> {
                val near = destination ?: here ?: return null
                placeRepository.searchNearby(category, near, PARKING_RADIUS_M).firstOrNull()
            }
            PlaceCategory.REST_AREA, PlaceCategory.REPAIR_SHOP -> {
                val start = here ?: return null
                if (destination == null) {
                    return placeRepository.searchNearby(category, start, REST_RADIUS_M).firstOrNull()
                }
                val remaining = GeoMath.distanceMeters(start, destination)
                placeRepository.searchNearby(category, GeoMath.midpoint(start, destination), REST_RADIUS_M)
                    .filter { GeoMath.distanceMeters(it.location, destination) < remaining }
                    .minByOrNull { GeoMath.distanceMeters(start, it.location) }
            }
            else -> {
                val start = here ?: return null
                placeRepository.searchNearby(category, start, NEARBY_RADIUS_M).firstOrNull()
            }
        }
    }

    private suspend fun removeWaypoints(): Guidance =
        if (navigationRepository.removeAllWaypoints() == NaviCommandResult.REQUESTED) {
            Guidance.WaypointRemoved
        } else {
            Guidance.NoActiveRoute
        }

    private suspend fun calmDown(): Guidance {
        val result = vehicleRepository.applyCabinAdjustment(TensionScoreUseCase.CALM_ADJUSTMENT)
        return Guidance.CalmModeOn(cabinAdjusted = result == ControlResult.DONE)
    }

    private suspend fun markSpot(context: VoiceContext): Guidance {
        val location = context.location ?: return Guidance.NotUnderstood
        driveRecordRepository.addDifficultSpot(location, context.nowMillis)
        return Guidance.SpotMarked
    }

    private suspend fun whereIsMyCar(nowMillis: Long): Guidance {
        val parking = driveRecordRepository.latestParking().first() ?: return Guidance.NoParkingRecord
        val minutesAgo = ((nowMillis - parking.savedAtMillis) / MILLIS_PER_MINUTE).coerceAtLeast(0)
        return Guidance.CarLocation(parking.memo, minutesAgo)
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
        const val PARKING_RADIUS_M = 1_000
        const val REST_RADIUS_M = 30_000
        const val NEARBY_RADIUS_M = 5_000
    }
}
