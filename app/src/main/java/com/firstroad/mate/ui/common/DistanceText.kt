package com.firstroad.mate.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.firstroad.mate.R
import java.util.Locale

/** 음성 안내와 같은 규칙: 950m 미만은 m(10m 단위), 이상은 km(소수 한 자리, 끝의 .0 생략). */
@Composable
fun formatDistance(meters: Int): String = if (meters < KM_THRESHOLD_METERS) {
    stringResource(R.string.unit_meters, meters / 10 * 10)
} else {
    stringResource(R.string.unit_kilometers, kilometers(meters))
}

/** 계기판식 표시용으로 숫자와 단위를 나눈다: 350 → ("350", "m"), 1,400 → ("1.4", "km"). */
fun splitDistance(meters: Int): Pair<String, String> = if (meters < KM_THRESHOLD_METERS) {
    (meters / 10 * 10).toString() to "m"
} else {
    kilometers(meters) to "km"
}

private fun kilometers(meters: Int): String =
    String.format(Locale.KOREA, "%.1f", meters / 1_000f).trimEnd('0').trimEnd('.')

private const val KM_THRESHOLD_METERS = 950
