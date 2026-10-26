package com.firstroad.mate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val FirstRoadColors = darkColorScheme(
    primary = Blue400,
    onPrimary = Navy950,
    primaryContainer = Navy700,
    onPrimaryContainer = Sky100,
    secondary = Blue300,
    onSecondary = Navy950,
    background = Navy950,
    onBackground = White,
    surface = Navy900,
    onSurface = White,
    surfaceVariant = Navy800,
    onSurfaceVariant = Gray300,
    outline = Navy700,
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

/** 테마 토큰 접근점: FirstRoad.dimens.minTouch, FirstRoad.tones.ok */
object FirstRoad {
    val dimens: Dimens
        @Composable get() = LocalDimens.current
    val tones: ToneColors
        @Composable get() = LocalToneColors.current
}
