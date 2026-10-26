package com.firstroad.mate.data.repository

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.CheckType
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.GuidancePriority
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.domain.model.TurnGuide
import com.firstroad.mate.domain.model.VoiceScreen
import com.firstroad.mate.domain.model.WarningLevel
import com.firstroad.mate.domain.model.WarningType
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * 안내는 짧아야 한다: 주행 중 한숨에 듣는 안내(회전·코칭·칭찬)는 25자, 그 밖(경고·질문·대답)은 40자 이내.
 * 문구를 고칠 때 길어지는 것을 막는 회귀 테스트.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], application = Application::class)
class PhraseLengthTest {

    private val phrases = PhraseRepositoryImpl(ApplicationProvider.getApplicationContext())

    private val samples: List<Guidance> = buildList {
        TurnDirection.entries.forEach {
            add(Guidance.PrepareLane(it, 350, isHighway = false))
            add(Guidance.PrepareLane(it, 1_600, isHighway = true))
            add(Guidance.SignalReminder(it))
            add(Guidance.NextTurnAnswer(it, 1_600))
        }
        add(Guidance.SignalPraise)
        add(Guidance.HardBrakeTip)
        add(Guidance.SpeedOver(50))
        add(Guidance.PassedWell)
        add(Guidance.CalmModeOn(cabinAdjusted = true))
        add(Guidance.CalmModeOn(cabinAdjusted = false))
        add(Guidance.Arrived)
        add(Guidance.RouteBriefing(3, 1, 2, TurnGuide("t", TurnDirection.RIGHT, 1_600, "경부고속도로", isHighwayJunction = true)))
        add(Guidance.RouteReady("판교 회사", 37, listOf(PlaceCategory.GAS_STATION, PlaceCategory.PARKING)))
        WarningType.entries.forEach { type -> WarningLevel.entries.forEach { add(Guidance.WarningNotice(type, it)) } }
        PlaceCategory.entries.forEach {
            add(Guidance.AskAddWaypoint(it))
            add(Guidance.NoPlaceFound(it))
        }
        add(Guidance.WaypointAdded("죽전 휴게소"))
        CabinArea.entries.forEach { add(Guidance.CheckIssue(CheckType.WINDOW, it)) }
        add(Guidance.AllClear)
        add(Guidance.CarLocation("B2 C14", 540))
        VoiceScreen.entries.forEach { add(Guidance.Help(it)) }
    }

    @Test
    fun `안내 문장은 짧다`() {
        samples.forEach { guidance ->
            val text = phrases.phrase(guidance)
            val limit = if (guidance.priority.isWhileDriving()) DRIVING_LIMIT else OTHER_LIMIT
            assertTrue("${text.length}자 > $limit: $text", text.length <= limit)
        }
    }

    private fun GuidancePriority.isWhileDriving() =
        this == GuidancePriority.TURN || this == GuidancePriority.COACH || this == GuidancePriority.PRAISE

    private companion object {
        const val DRIVING_LIMIT = 25
        const val OTHER_LIMIT = 40
    }
}
