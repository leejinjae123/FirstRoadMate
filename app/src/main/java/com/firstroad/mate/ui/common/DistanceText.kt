package com.firstroad.mate.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.firstroad.mate.R
import java.util.Locale

/** 음성 안내와 같은 규칙: 950m 미만은 m(10m 단위), 이상은 km(소수 한 자리, 끝의 .0 생략). */
@Composable
fun formatDistance(meters: Int): String = if (meters < 950) {
    stringResource(R.string.unit_meters, meters / 10 * 10)
} else {
    stringResource(
        R.string.unit_kilometers,
        String.format(Locale.KOREA, "%.1f", meters / 1_000f).trimEnd('0').trimEnd('.'),
    )
}
