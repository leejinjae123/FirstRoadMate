package com.firstroad.mate.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R

/** 데모 리플레이로 동작 중임을 항상 표시한다 (실제 차량 데이터로 오해하지 않도록). */
@Composable
fun DemoBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.demo_badge),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
    )
}
