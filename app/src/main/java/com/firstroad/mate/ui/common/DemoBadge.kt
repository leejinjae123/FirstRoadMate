package com.firstroad.mate.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.ui.theme.FirstRoad

/** 데모 리플레이로 동작 중임을 항상 표시한다 (실제 차량 데이터로 오해하지 않도록). */
@Composable
fun DemoBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.demo_badge),
        style = FirstRoad.type.caption,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}
