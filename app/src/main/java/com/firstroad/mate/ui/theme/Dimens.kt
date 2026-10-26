package com.firstroad.mate.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 초보 배려 크기 토큰 (기획서 6.1 "크게"). 터치 대상은 최소 96dp. */
data class Dimens(
    val minTouch: Dp = 96.dp,
    val screenPadding: Dp = 32.dp,
    val cardPadding: Dp = 28.dp,
    val gap: Dp = 20.dp,
    val smallGap: Dp = 12.dp,
    val cornerRadius: Dp = 28.dp,
    val badgeHeight: Dp = 44.dp,
    val hairline: Dp = 1.dp,
    val micSize: Dp = 96.dp,
    /** 이 폭 이상이면 왼쪽 주 화면 + 오른쪽 위젯 열로 나눈다 (차량 화면의 홈 카드 배치). */
    val splitMinWidth: Dp = 1200.dp,
    /** 이 높이보다 낮으면(예: 1024×768) 카드를 줄여 한 화면에 담는다. */
    val shortScreenHeight: Dp = 760.dp,
)
