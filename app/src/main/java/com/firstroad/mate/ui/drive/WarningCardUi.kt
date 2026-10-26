package com.firstroad.mate.ui.drive

import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.WarningLevel

/**
 * 경고등 통역사 카드에 그릴 내용.
 * @param suggestedStop 들르자고 제안하는 곳 (바로 정차 → 휴게소, 타이어 → 주유소 등). 이미 처리했으면 null.
 */
data class WarningCardUi(
    val level: WarningLevel,
    val message: String,
    val suggestedStop: PlaceCategory?,
)
