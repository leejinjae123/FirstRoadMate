package com.firstroad.mate.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.firstroad.mate.ui.theme.FirstRoad

/**
 * 초보 배려 버튼: 최소 96dp 높이, 큰 글자, 한 번 터치.
 * 주 버튼은 시안으로 채우고(화면에 하나), 보조 버튼은 얇은 테두리만 그린다.
 */
@Composable
fun BigActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(FirstRoad.dimens.cornerRadius)
    val sizeModifier = modifier.defaultMinSize(minHeight = FirstRoad.dimens.minTouch, minWidth = FirstRoad.dimens.minTouch)
    val content: @Composable () -> Unit = {
        icon?.let {
            Icon(it, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = sizeModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) { content() }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = sizeModifier,
            enabled = enabled,
            shape = shape,
            border = BorderStroke(FirstRoad.dimens.hairline, MaterialTheme.colorScheme.outlineVariant),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        ) { content() }
    }
}
