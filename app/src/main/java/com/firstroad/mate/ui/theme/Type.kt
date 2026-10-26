package com.firstroad.mate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** 주행 중 한눈에 읽히도록 큰 글자 (본문 최소 24sp, 핵심 문장 44sp). */
internal val FirstRoadTypography = Typography(
    displayLarge = TextStyle(fontSize = 44.sp, lineHeight = 56.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 36.sp, lineHeight = 46.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 30.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 26.sp, lineHeight = 36.sp),
    bodyMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    labelLarge = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
)
