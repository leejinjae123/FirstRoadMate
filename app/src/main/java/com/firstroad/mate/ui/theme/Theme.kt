package com.firstroad.mate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val FirstRoadColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Ink,
    primaryContainer = CyanDeep,
    onPrimaryContainer = CyanSoft,
    secondary = CyanSoft,
    onSecondary = Ink,
    background = Ink,
    onBackground = TextHigh,
    surface = Panel,
    onSurface = TextHigh,
    surfaceVariant = PanelHigh,
    onSurfaceVariant = TextMid,
    surfaceContainerLowest = InkRaised,
    outline = Hairline,
    outlineVariant = TextLow,
    error = ToneAction,
)

private val LocalDimens = staticCompositionLocalOf { Dimens() }
private val LocalToneColors = staticCompositionLocalOf { ToneColors() }

/** 차량 화면은 주야간 모두 어두운 바탕이 눈부심이 적어 다크 테마 하나만 쓴다. */
@Composable
fun FirstRoadTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalDimens provides Dimens(),
        LocalToneColors provides ToneColors(),
    ) {
        MaterialTheme(
            colorScheme = FirstRoadColors,
            typography = FirstRoadTypography,
            content = content,
        )
    }
}

/** 테마 토큰 접근점: FirstRoad.dimens.minTouch, FirstRoad.tones.ok, FirstRoad.type.numeral */
object FirstRoad {
    val dimens: Dimens
        @Composable get() = LocalDimens.current
    val tones: ToneColors
        @Composable get() = LocalToneColors.current
    val type: CockpitType
        get() = CockpitTypography
}
