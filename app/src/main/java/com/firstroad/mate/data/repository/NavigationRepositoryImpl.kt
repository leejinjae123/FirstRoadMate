package com.firstroad.mate.data.repository

import com.firstroad.mate.data.datasource.DemoSource
import com.firstroad.mate.data.datasource.PleosSource
import com.firstroad.mate.data.datasource.navi.NaviDataSource
import com.firstroad.mate.domain.model.NaviCommandResult
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.RoutePlan
import com.firstroad.mate.domain.repository.NavigationRepository
import com.firstroad.mate.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationRepositoryImpl @Inject constructor(
    @PleosSource private val pleos: NaviDataSource,
    @DemoSource private val demo: NaviDataSource,
    private val settingsRepository: SettingsRepository,
) : NavigationRepository {

    override fun navigationState(): Flow<NavigationState> =
        settingsRepository.isDemoActive()
            .flatMapLatest { isDemo -> source(isDemo).navigationState() }
            .onStart { emit(NavigationState()) }

    override suspend fun startRoute(plan: RoutePlan): NaviCommandResult = current().startRoute(plan)

    override suspend fun addWaypoint(place: Place): NaviCommandResult = current().addWaypoint(place)

    override suspend fun removeAllWaypoints(): NaviCommandResult = current().removeAllWaypoints()

    override suspend fun cancelRoute(): NaviCommandResult = current().cancelRoute()

    private suspend fun current(): NaviDataSource = source(settingsRepository.isDemoActive().first())

    private fun source(isDemo: Boolean): NaviDataSource = if (isDemo) demo else pleos
}
