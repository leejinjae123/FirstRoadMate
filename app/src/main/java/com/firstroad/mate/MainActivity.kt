package com.firstroad.mate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.firstroad.mate.ui.navigation.AppNavHost
import com.firstroad.mate.ui.theme.FirstRoadTheme
import dagger.hilt.android.AndroidEntryPoint

/** 유일한 Activity: Compose 진입점과 내비게이션 호스팅만 한다. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirstRoadTheme { AppNavHost() }
        }
    }
}
