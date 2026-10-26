package com.firstroad.mate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** 주행 중 한눈에 읽히도록 큰 글자 (본문 최소 24sp, 핵심 문장 40sp 이상). */
internal val FirstRoadTypography = Typography(
    displayLarge = TextStyle(fontSize = 44.sp, lineHeight = 56.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 36.sp, lineHeight = 46.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 30.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 26.sp, lineHeight = 36.sp),
    bodyMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    labelLarge = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp),
)

/**
 * 계기판식 숫자: 거리·속도처럼 '숫자 하나가 핵심'인 곳에 얇고 큰 숫자를 쓴다.
 * caption은 위젯 제목("다음 안내", "내 차")처럼 작고 흐린 글자.
 */
class CockpitType internal constructor(
    val numeralHuge: TextStyle,
    val numeral: TextStyle,
    val numeralSmall: TextStyle,
    val unit: TextStyle,
    val caption: TextStyle,
)

internal val CockpitTypography = CockpitType(
    numeralHuge = TextStyle(fontSize = 96.sp, lineHeight = 100.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp),
    numeral = TextStyle(fontSize = 64.sp, lineHeight = 68.sp, fontWeight = FontWeight.Light, letterSpacing = (-1).sp),
    numeralSmall = TextStyle(fontSize = 40.sp, lineHeight = 44.sp, fontWeight = FontWeight.Normal),
    unit = TextStyle(fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    caption = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp),
)
