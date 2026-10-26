package com.firstroad.mate.data.datasource.vehicle.pleos

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.WindowArea
import ai.pleos.playground.vehicle.api.model.telltale.Telltales
import ai.pleos.playground.vehicle.constant.area.TurnSignalValue
import ai.pleos.playground.vehicle.constant.area.WindowValue
import ai.pleos.playground.vehicle.constant.control.LightState
import ai.pleos.playground.vehicle.constant.control.WiperState
import ai.pleos.playground.vehicle.listener.BrakePedalStatusListener
import ai.pleos.playground.vehicle.listener.EvBatteryStateOfChargeListener
import ai.pleos.playground.vehicle.listener.FuelPercentListener
import ai.pleos.playground.vehicle.listener.LightStateListener
import ai.pleos.playground.vehicle.listener.TurnSignalStateListener
import ai.pleos.playground.vehicle.listener.WiperStateListener
import android.util.Log
import com.firstroad.mate.data.datasource.awaitSdkCallback
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.TurnSignal
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pleos Vehicle SDK 신호를 Flow로 바꾼다. 구독 API가 있으면 리스너를, 없으면 코루틴 주기 조회를 쓴다.
 * 연결할 수 없거나 실패한 신호는 null(모름)을 내보낸다.
 */
@Singleton
class PleosVehicleSignals @Inject constructor(
    private val client: PleosVehicleClient,
) {
    fun turnSignal(): Flow<TurnSignal> = withVehicle(TurnSignal.OFF) { vehicle ->
        callbackFlow {
            val api = vehicle.getTurnSignal()
            val listener = object : TurnSignalStateListener {
                override fun onTurnSignalStateUpdated(state: TurnSignalValue?) {
                    trySend(state.toDomain())
                }

                override fun onFailed(e: Exception) {
                    trySend(TurnSignal.OFF)
                }
            }
            api.registerTurnSignalState(listener)
            awaitClose { api.unregisterTurnSignalState(listener) }
        }
    }

    fun brakePressed(): Flow<Boolean?> = withVehicle(null) { vehicle ->
        callbackFlow {
            val api = vehicle.getBrake()
            val listener = object : BrakePedalStatusListener {
                override fun onBrakePedalStatusUpdated(status: Boolean?) {
                    trySend(status)
                }

                override fun onFailed(e: Exception) {
                    trySend(null)
                }
            }
            api.registerBrakePedalStatus(listener)
            awaitClose { api.unregisterBrakePedalStatus(listener) }
        }
    }

    fun fuelPercent(): Flow<Int?> = withVehicle(null) { vehicle ->
        callbackFlow {
            val api = vehicle.getFuel()
            val listener = object : FuelPercentListener {
                override fun onFuelPercentUpdated(value: Int?) {
                    trySend(value)
                }

                override fun onFailed(e: Exception) {
                    trySend(null)
                }
            }
            api.registerFuelPercent(listener)
            awaitClose { api.unregisterFuelPercent(listener) }
        }
    }

    fun evBatteryPercent(): Flow<Int?> = withVehicle(null) { vehicle ->
        callbackFlow {
            val api = vehicle.getEvBattery()
            val listener = object : EvBatteryStateOfChargeListener {
                override fun onEvBatteryStateOfChargeUpdated(value: Int?) {
                    trySend(value)
                }

                override fun onFailed(e: Exception) {
                    trySend(null)
                }
            }
            api.registerEvBatteryStateOfCharge(listener)
            awaitClose { api.unregisterEvBatteryStateOfCharge(listener) }
        }
    }

    fun wiperOn(): Flow<Boolean?> = withVehicle(null) { vehicle ->
        callbackFlow {
            val api = vehicle.getWiper()
            val area = WindowArea(WindowValue.FRONT_WINDSHIELD)
            val listener = object : WiperStateListener {
                override fun onWiperStateUpdated(state: WiperState?) {
                    trySend(state?.let { it == WiperState.ON })
                }

                override fun onFailed(e: Exception) {
                    trySend(null)
                }
            }
            api.registerWiperState(area, listener)
            awaitClose { api.unregisterWiperState(area, listener) }
        }
    }

    fun headlightOn(): Flow<Boolean?> = withVehicle(null) { vehicle ->
        callbackFlow {
            val api = vehicle.getLight()
            val listener = object : LightStateListener {
                override fun onLightStateUpdated(state: LightState?) {
                    trySend(state?.let { it == LightState.ON })
                }

                override fun onFailed(e: Exception) {
                    trySend(null)
                }
            }
            api.registerLowBeamLightState(listener)
            awaitClose { api.unregisterLowBeamLightState(listener) }
        }
    }

    /** 경고등. 구독 API는 비공개 권한이 필요해 1초 주기로 조회한다. */
    fun telltales(): Flow<Telltales?> = withVehicle(null) { vehicle ->
        poll(TELLTALE_PERIOD_MILLIS) {
            awaitSdkCallback<Telltales> { resume ->
                vehicle.getSafety().getTelltalesInfo(onComplete = { resume(it) }, onFailed = { resume(null) })
            }
        }
    }

    /** 열린 창문. 위치가 최솟값보다 크면 열림으로 본다. */
    fun openWindows(): Flow<Set<CabinArea>?> = withVehicle(null) { vehicle ->
        poll(WINDOW_PERIOD_MILLIS) {
            val open = mutableSetOf<CabinArea>()
            for (area in CabinArea.entries) {
                val position = readWindowPosition(vehicle, area) ?: return@poll null
                if (position > 0) open += area
            }
            open
        }
    }

    private suspend fun readWindowPosition(vehicle: Vehicle, area: CabinArea): Int? =
        awaitSdkCallback { resume ->
            vehicle.getWindow().getCurrentWindowPosition(area.toWindowArea(), { resume(it) }, { resume(null) })
        }

    /** 구독·조회가 실패하면(권한 거부 등) 앱을 멈추지 않고 fallback(모름)을 내보낸다. */
    private fun <T> withVehicle(fallback: T, block: (Vehicle) -> Flow<T>): Flow<T> {
        val vehicle = client.vehicleOrNull() ?: return flowOf(fallback)
        return block(vehicle)
            .catch {
                Log.w(TAG, "차량 신호를 읽지 못했습니다.", it)
                emit(fallback)
            }
            .distinctUntilChanged()
    }

    private fun <T> poll(periodMillis: Long, read: suspend () -> T): Flow<T> = flow {
        while (currentCoroutineContext().isActive) {
            emit(read())
            delay(periodMillis)
        }
    }

    private companion object {
        const val TAG = "PleosVehicleSignals"
        const val TELLTALE_PERIOD_MILLIS = 1_000L
        const val WINDOW_PERIOD_MILLIS = 2_000L
    }
}
