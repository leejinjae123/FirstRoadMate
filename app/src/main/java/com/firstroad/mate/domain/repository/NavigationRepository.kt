package com.firstroad.mate.domain.repository

import com.firstroad.mate.domain.model.NaviCommandResult
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.RoutePlan
import kotlinx.coroutines.flow.Flow

interface NavigationRepository {
    fun navigationState(): Flow<NavigationState>

    /** 목적지와 경유지(최대 2개)를 한 번에 경로로 요청한다. */
    suspend fun startRoute(plan: RoutePlan): NaviCommandResult

    /** 안내 중 경유지를 비어 있는 슬롯에 추가한다. 슬롯이 없으면 WAYPOINT_FULL. */
    suspend fun addWaypoint(place: Place): NaviCommandResult

    suspend fun removeAllWaypoints(): NaviCommandResult

    suspend fun cancelRoute(): NaviCommandResult
}
