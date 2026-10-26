package com.firstroad.mate.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.LevelTier

/** 머리 오른쪽의 작은 레벨 표시: (고리) 초보 레벨 2 */
@Composable
fun LevelBadge(level: Int, modifier: Modifier = Modifier) {
    val total = LevelTier.entries.size
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RingProgress(progress = level / total.toFloat(), modifier = Modifier.size(44.dp), strokeWidth = 6.dp) {
            Text("$level", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Text(stringResource(R.string.home_level, level), style = MaterialTheme.typography.titleMedium)
    }
}
