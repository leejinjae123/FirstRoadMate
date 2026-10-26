package com.firstroad.mate.domain.model

/**
 * 앱이 운전자에게 전하는 메시지. 문구 자체는 data 계층(PhraseRepository)이 strings.xml에서 만든다.
 */
sealed interface Guidance {
    val priority: GuidancePriority

    // ② 첫길 코치 · ③ 리허설
    data class PrepareLane(val direction: TurnDirection, val distanceMeters: Int, val isHighway: Boolean) : Guidance {
        override val priority = GuidancePriority.TURN
    }

    data class SignalReminder(val direction: TurnDirection) : Guidance {
        override val priority = GuidancePriority.TURN
    }

    data object SignalPraise : Guidance {
        override val priority = GuidancePriority.PRAISE
    }

    data object HardBrakeTip : Guidance {
        override val priority = GuidancePriority.COACH
    }

    data class SpeedOver(val limitKmh: Int) : Guidance {
        override val priority = GuidancePriority.COACH
    }

    /**
     * 출발 전 브리핑. firstJunction이 있으면 "첫 번째는 2.1km 앞 오른쪽"처럼 순서까지 예습시킨다 (③ 리허설).
     */
    data class RouteBriefing(
        val difficultCount: Int,
        val bookmarkedCount: Int,
        val highwayJunctions: Int,
        val firstJunction: TurnGuide? = null,
    ) : Guidance {
        override val priority = GuidancePriority.QUESTION
    }

    data class NextTurnAnswer(val direction: TurnDirection, val distanceMeters: Int) : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data object NoActiveRoute : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    // ④ 경고등 통역사
    data class WarningNotice(val type: WarningType, val level: WarningLevel) : Guidance {
        override val priority = GuidancePriority.WARNING
    }

    data class ContinueAnswer(val level: WarningLevel?) : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    // ⑤ 카밍 캐빈
    data class CalmModeOn(val cabinAdjusted: Boolean) : Guidance {
        override val priority = GuidancePriority.COACH
    }

    data object PassedWell : Guidance {
        override val priority = GuidancePriority.PRAISE
    }

    // ① 경로 빌더 (경유지)
    data class AskAddWaypoint(val category: PlaceCategory) : Guidance {
        override val priority = GuidancePriority.QUESTION
    }

    data class WaypointAdded(val placeName: String) : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data object WaypointRemoved : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data object WaypointFull : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data class NoPlaceFound(val category: PlaceCategory) : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    // ⑥ 하차 안심 체크
    data class CheckIssue(val type: CheckType, val area: CabinArea?) : Guidance {
        override val priority = GuidancePriority.QUESTION
    }

    data object AllClear : Guidance {
        override val priority = GuidancePriority.PRAISE
    }

    data class WindowClosed(val area: CabinArea) : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    /** 제어 권한이 없거나 차종이 지원하지 않아 직접 해 달라고 안내한다 (안내 모드). */
    data object ControlNotPermitted : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data class ParkingSaved(val memo: String?) : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data class CarLocation(val memo: String?, val minutesAgo: Long) : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data object NoParkingRecord : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data class LevelUp(val level: Int) : Guidance {
        override val priority = GuidancePriority.PRAISE
    }

    // 음성 공통
    data object SpotMarked : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data object NothingToRepeat : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data object NotUnderstood : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data object Dismissed : Guidance {
        override val priority = GuidancePriority.ANSWER
    }

    data object Arrived : Guidance {
        override val priority = GuidancePriority.TURN
    }
}
