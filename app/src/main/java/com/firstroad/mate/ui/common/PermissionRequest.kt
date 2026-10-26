package com.firstroad.mate.ui.common

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/** 앱 시작 시 한 번, 음성·위치·차속 런타임 권한을 요청한다. 거부해도 데모·안내 모드로 동작한다. */
@Composable
fun RequestRuntimePermissions() {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(Unit) { launcher.launch(RUNTIME_PERMISSIONS) }
}

private val RUNTIME_PERMISSIONS = arrayOf(
    android.Manifest.permission.RECORD_AUDIO,
    "android.car.permission.CAR_SPEED",
    "pleos.car.permission.FUSED_LOCATION",
)
