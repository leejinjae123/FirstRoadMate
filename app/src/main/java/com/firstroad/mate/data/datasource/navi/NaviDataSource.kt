package com.firstroad.mate.data.datasource.navi

import com.firstroad.mate.domain.model.NaviCommandResult
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.RoutePlan
import kotlinx.coroutines.flow.Flow

interface NaviDataSource {
    fun navigationState(): Flow<NavigationState>

    suspend fun startRoute(plan: RoutePlan): NaviCommandResult

    suspend fun addWaypoint(place: Place): NaviCommandResult

    suspend fun removeAllWaypoints(): NaviCommandResult

    suspend fun cancelRoute(): NaviCommandResult
}
