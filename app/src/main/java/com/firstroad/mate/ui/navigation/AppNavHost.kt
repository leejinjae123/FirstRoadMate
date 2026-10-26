package com.firstroad.mate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.firstroad.mate.ui.common.RequestRuntimePermissions
import com.firstroad.mate.ui.drive.DriveRoute
import com.firstroad.mate.ui.home.HomeRoute
import com.firstroad.mate.ui.park.ParkRoute

/** 홈(경로 빌더) → 주행 → 하차 체크 → 홈. */
@Composable
fun AppNavHost() {
    RequestRuntimePermissions()
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppDestinations.HOME) {
        composable(AppDestinations.HOME) {
            HomeRoute(onTripStarted = { place -> navController.navigate(AppDestinations.drive(place)) })
        }
        composable(AppDestinations.DRIVE, arguments = AppDestinations.tripArguments) { entry ->
            val args = entry.arguments
            val name = args?.getString(AppDestinations.ARG_NAME).orEmpty()
            val point = AppDestinations.parsePoint(args?.getString(AppDestinations.ARG_LAT), args?.getString(AppDestinations.ARG_LNG))
            DriveRoute(
                onFinished = {
                    navController.navigate(AppDestinations.park(name, point)) {
                        popUpTo(AppDestinations.HOME)
                    }
                },
            )
        }
        composable(AppDestinations.PARK, arguments = AppDestinations.tripArguments) {
            ParkRoute(onDone = { navController.popBackStack(AppDestinations.HOME, inclusive = false) })
        }
    }
}
