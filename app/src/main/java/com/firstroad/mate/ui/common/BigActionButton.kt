package com.firstroad.mate.ui.common

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.firstroad.mate.ui.theme.FirstRoad

/** 초보 배려 버튼: 최소 96dp 높이, 큰 글자, 한 번 터치. */
@Composable
fun BigActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(FirstRoad.dimens.cornerRadius)
    val sizeModifier = modifier.defaultMinSize(minHeight = FirstRoad.dimens.minTouch, minWidth = FirstRoad.dimens.minTouch)
    val label: @Composable () -> Unit = {
        Text(text = text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = sizeModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) { label() }
    } else {
        OutlinedButton(onClick = onClick, modifier = sizeModifier, enabled = enabled, shape = shape) { label() }
    }
}
