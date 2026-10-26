package com.firstroad.mate.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 초보 배려 크기 토큰 (기획서 6.1 "크게"). */
data class Dimens(
    val minTouch: Dp = 96.dp,
    val screenPadding: Dp = 32.dp,
    val cardPadding: Dp = 32.dp,
    val gap: Dp = 24.dp,
    val smallGap: Dp = 12.dp,
    val cornerRadius: Dp = 24.dp,
    val badgeHeight: Dp = 48.dp,
)
