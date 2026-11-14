package com.firstroad.mate.data.repository

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.CheckType
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.GuidancePriority
import com.firstroad.mate.domain.model.IncomingMessage
import com.firstroad.mate.domain.model.MessengerApp
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
        add(Guidance.WaypointAdded("판교 타이어 정비소"))
        CabinArea.entries.forEach { add(Guidance.CheckIssue(CheckType.WINDOW, it)) }
        add(Guidance.AllClear)
        add(Guidance.AskCabinConsent)
        add(Guidance.CabinConsentSaved(allowed = true))
        add(Guidance.CabinConsentSaved(allowed = false))
        add(Guidance.LevelUp(2))
        add(Guidance.LevelUp(5))
        add(Guidance.CarLocation("B2 C14", 540))
        VoiceScreen.entries.forEach { add(Guidance.Help(it)) }
        add(Guidance.AskReadMessages(1, "엄마", 0))
        add(Guidance.AskReadMessages(3, "엄마", 0))
        add(Guidance.AskReadMessages(2, "김 대리", 1))
        add(Guidance.MessagesHeld)
        add(Guidance.NoMessages)
        add(Guidance.MessageAccessOff)
        // MessagesReadout은 메시지 내용 자체라 길이 제한 대신 아래 테스트로 앞부분만 읽는지 본다.
    }

    @Test
    fun `긴 메시지는 앞부분만 읽고, 남은 건수를 알린다`() {
        val long = IncomingMessage("1", "엄마", "가".repeat(200), MessengerApp.KAKAOTALK, 0L)
        val short = IncomingMessage("2", "김 대리", "내일 회의 10시로 바뀌었어요", MessengerApp.SMS, 0L)
        val text = phrases.phrase(Guidance.MessagesReadout(listOf(long, short), remaining = 2))
        assertTrue(text, text.length < 120)
        assertTrue(text, text.contains("김 대리, 내일 회의 10시로 바뀌었어요."))
        assertTrue(text, text.endsWith("나머지 2건은 세운 뒤에 확인해요."))
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
