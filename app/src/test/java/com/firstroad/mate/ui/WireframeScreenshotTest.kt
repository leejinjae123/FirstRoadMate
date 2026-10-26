package com.firstroad.mate.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.CheckType
import com.firstroad.mate.domain.model.DriveStats
import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.ParkingCheckItem
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.RoutePlan
import com.firstroad.mate.domain.model.RoutePreference
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.domain.model.TurnGuide
import com.firstroad.mate.domain.model.WarningLevel
import com.firstroad.mate.domain.model.WaypointReason
import com.firstroad.mate.domain.model.WaypointSuggestion
import com.firstroad.mate.ui.common.Tone
import com.firstroad.mate.ui.drive.DriveActions
import com.firstroad.mate.ui.drive.DriveScreen
import com.firstroad.mate.ui.drive.DriveUiState
import com.firstroad.mate.ui.drive.WarningCardUi
import com.firstroad.mate.ui.home.HomeScreen
import com.firstroad.mate.ui.home.HomeUiState
import com.firstroad.mate.ui.park.ParkActions
import com.firstroad.mate.ui.park.ParkScreen
import com.firstroad.mate.ui.park.ParkUiState
import com.firstroad.mate.ui.theme.FirstRoadTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * 교차 검증: 기획서 6.3 와이어프레임 A~D를 실제 Compose 화면으로 렌더링해 이미지로 남긴다.
 * 출력: app/build/outputs/roborazzi/screen_*.png (Pleos 앱 영역 크기의 가로 화면)
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w1600dp-h1000dp-land-mdpi", application = android.app.Application::class)
class WireframeScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val office = Place("fav-office", "판교 회사", "", GeoPoint(37.4019, 127.1086), PlaceCategory.DESTINATION)
    private val gas = Place("gas-1", "영통 셀프주유소", "", GeoPoint(37.264, 127.05), PlaceCategory.GAS_STATION)
    private val parking = Place("park-1", "판교 공영주차장", "", GeoPoint(37.4005, 127.1101), PlaceCategory.PARKING)

    /** 앱의 공통 Scaffold와 같은 배경 위에 화면을 그린다. */
    private fun capture(name: String, content: @Composable () -> Unit) {
        composeRule.setContent {
            FirstRoadTheme {
                Surface(color = MaterialTheme.colorScheme.background) { content() }
            }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/screen_$name.png")
    }

    @Test
    fun a_home_route_builder() = capture("A_home") {
        HomeScreen(
            state = HomeUiState(
                level = 1,
                favorites = listOf(office, office.copy(id = "p", name = "부모님 댁"), office.copy(id = "m", name = "자주 가는 마트")),
                selected = office,
                plan = RoutePlan(
                    destination = office,
                    waypoints = listOf(
                        WaypointSuggestion(gas, WaypointReason.LOW_FUEL),
                        WaypointSuggestion(parking, WaypointReason.EASY_PARKING),
                    ),
                    preference = RoutePreference.COMFORTABLE,
                    estimatedMinutes = 37,
                ),
                fuelPercent = 18,
                isDemo = false,
                levelProgress = 0f,
                drivesToNextLevel = 3,
                heardText = "회사 가자",
                answer = "판교 회사까지 37분, 주유소, 주차장 들러요. 출발할까요?",
            ),
            onSelectDestination = {},
            onMic = {},
            onStart = {},
            onRemoveWaypoints = {},
        )
    }

    @Test
    fun b_drive_next_action() = capture("B_drive") {
        DriveScreen(
            state = DriveUiState(
                destinationName = "판교 회사",
                isStarted = true,
                tone = Tone.PREPARE,
                headline = "500m 앞 갈림길, 오른쪽 차선으로 가요",
                nextTurnText = "500m 앞 오른쪽이에요",
                nextTurnDirection = TurnDirection.RIGHT,
                nextTurnMeters = 500,
                speedLimitKmh = 80,
                upcomingTurns = listOf(
                    TurnGuide("exit", TurnDirection.RIGHT, 5_300, "판교IC", isHighwayJunction = true),
                    TurnGuide("city", TurnDirection.LEFT, 5_700, "판교역로", isHighwayJunction = false),
                ),
                remainingMinutes = 33,
                remainingMeters = 13_700,
                speedKmh = 60,
                tension = 65,
                isCalmActive = true,
                isRestricted = true,
                isDemo = false,
            ),
            actions = NO_DRIVE_ACTIONS,
        )
    }

    @Test
    fun c_drive_warning_and_question() = capture("C_warning") {
        DriveScreen(
            state = DriveUiState(
                destinationName = "판교 회사",
                isStarted = true,
                tone = Tone.PREPARE,
                headline = "1km 앞 오른쪽이에요",
                nextTurnDirection = TurnDirection.RIGHT,
                nextTurnMeters = 1_000,
                speedKmh = 90,
                speedLimitKmh = 100,
                remainingMeters = 9_400,
                tension = 40,
                warning = WarningCardUi(
                    WarningLevel.CHECK_TODAY,
                    "타이어 공기압이 낮아요. 오늘 안에 점검해요",
                    suggestedStop = PlaceCategory.REPAIR_SHOP,
                ),
                question = "정비소를 경유지로 넣을까요?",
                heardText = "이거 계속 가도 돼?",
                isDemo = false,
            ),
            actions = NO_DRIVE_ACTIONS,
        )
    }

    @Test
    fun d_park_check_and_report() = capture("D_park") {
        ParkScreen(
            state = ParkUiState(
                destinationName = "판교 회사",
                checks = listOf(
                    ParkingCheckItem(CheckType.WINDOW, CabinArea.REAR_LEFT, isOk = false, canFixRemotely = true),
                    ParkingCheckItem(CheckType.HEADLIGHT, null, isOk = true, canFixRemotely = false),
                    ParkingCheckItem(CheckType.PARKING_BRAKE, null, isOk = true, canFixRemotely = false),
                ),
                isChecking = false,
                isParkingSaved = true,
                parkingMemo = "B2 C14",
                stats = DriveStats(signalUsed = 2, signalMissed = 1, hardBrakes = 1, difficultPassed = 1),
                level = 1,
                isLevelUp = false,
                drivesToNextLevel = 2,
                question = "뒷좌석 왼쪽 창문이 열려 있어요. 닫을까요?",
                isDemo = false,
            ),
            actions = ParkActions(onFix = {}, onAnswer = {}, onMic = {}, onSimulate = {}, onDone = {}),
        )
    }

    private companion object {
        val NO_DRIVE_ACTIONS = DriveActions(
            onMic = {}, onRepeat = {}, onAnswer = {}, onAddSuggestedStop = {},
            onDismissWarning = {}, onSimulate = {}, onEndDrive = {},
        )
    }
}
