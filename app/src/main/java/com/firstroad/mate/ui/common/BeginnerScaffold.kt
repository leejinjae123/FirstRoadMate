package com.firstroad.mate.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.firstroad.mate.ui.theme.FirstRoad
import kotlinx.coroutines.flow.Flow

/**
 * 모든 화면의 공통 틀 (AND-ARC-03): 진행바와 스낵바(토스트)를 여기 한 곳에서 처리한다.
 */
@Composable
fun BeginnerScaffold(
    toastEvents: Flow<Int>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(toastEvents) {
        toastEvents.collect { messageId -> snackbarHostState.showSnackbar(context.getString(messageId)) }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                Box(Modifier.padding(horizontal = FirstRoad.dimens.screenPadding)) { topBar() }
                if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        },
        content = content,
    )
}
