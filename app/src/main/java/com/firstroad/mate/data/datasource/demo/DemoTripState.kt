package com.firstroad.mate.data.datasource.demo

import com.firstroad.mate.domain.model.CabinArea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 데모 리플레이의 진행 상태. 데모 차량·내비·위치 소스가 같은 시계를 공유해야 화면이 맞아떨어지므로
 * DI 싱글턴으로 하나만 둔다 (전역 object 변수 대신).
 */
@Singleton
class DemoTripState @Inject constructor() {

    data class State(
        val startedAtMillis: Long? = null,
        val waypointNames: List<String> = emptyList(),
        val closedWindows: Set<CabinArea> = emptySet(),
        val isCabinAdjusted: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun start(nowMillis: Long, waypointNames: List<String>) {
        _state.value = State(startedAtMillis = nowMillis, waypointNames = waypointNames)
    }

    fun stop() {
        _state.value = State()
    }

    fun setWaypoints(names: List<String>) = _state.update { it.copy(waypointNames = names) }

    /**
     * 빈 슬롯이 있으면 경유지를 추가하고 true. 확인과 추가를 한 번에 해 경합을 막는다.
     * @param passed 이미 지나간 경유지 수 (그만큼 슬롯이 비어 있다).
     */
    fun tryAddWaypoint(name: String, max: Int, passed: Int): Boolean {
        var added = false
        _state.update { current ->
            val remaining = current.waypointNames.drop(passed)
            added = remaining.size < max
            if (added) current.copy(waypointNames = remaining + name) else current
        }
        return added
    }

    fun closeWindow(area: CabinArea) = _state.update { it.copy(closedWindows = it.closedWindows + area) }

    fun setCabinAdjusted(adjusted: Boolean) = _state.update { it.copy(isCabinAdjusted = adjusted) }

    /** 출발 후 경과 시간(초). 출발 전이면 null. */
    fun elapsedSeconds(nowMillis: Long): Float? =
        _state.value.startedAtMillis?.let { (nowMillis - it) / 1000f }
}
