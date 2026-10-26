package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.ControlResult
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.PendingAction
import com.firstroad.mate.domain.model.VoiceCommand
import com.firstroad.mate.domain.model.VoiceContext
import com.firstroad.mate.domain.repository.SettingsRepository
import com.firstroad.mate.domain.repository.VehicleRepository
import javax.inject.Inject

/** 확인 질문에 대한 대답을 처리한다. "아니"면 아무것도 하지 않고, "응"이면 질문한 일을 실행한다. 동의 질문은 대답을 그대로 저장한다. */
class ResolvePendingActionUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val settingsRepository: SettingsRepository,
    private val executeVoiceCommand: ExecuteVoiceCommandUseCase,
) {
    suspend operator fun invoke(action: PendingAction, accepted: Boolean, context: VoiceContext): Guidance {
        if (action == PendingAction.AllowCabinAdjust) {
            settingsRepository.setCabinAdjustConsent(accepted)
            return Guidance.CabinConsentSaved(accepted)
        }
        if (!accepted) return Guidance.Dismissed
        return when (action) {
            is PendingAction.AddWaypoint -> executeVoiceCommand(VoiceCommand.AddWaypoint(action.category), context)
            is PendingAction.CloseWindow -> {
                if (!context.vehicle.isParked) return Guidance.ControlNotPermitted
                val result = vehicleRepository.closeWindow(action.area)
                if (result == ControlResult.DONE) Guidance.WindowClosed(action.area) else Guidance.ControlNotPermitted
            }
            PendingAction.AllowCabinAdjust -> Guidance.CabinConsentSaved(accepted)
        }
    }
}
