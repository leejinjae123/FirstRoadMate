package com.firstroad.mate.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.LevelTier

/** "초보 레벨 2 ●●○○○" */
@Composable
fun LevelBadge(level: Int, modifier: Modifier = Modifier) {
    val total = LevelTier.entries.size
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.home_level, level), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(16.dp))
        Text(
            text = "●".repeat(level) + "○".repeat((total - level).coerceAtLeast(0)),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
