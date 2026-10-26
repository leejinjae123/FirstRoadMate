package com.firstroad.mate.domain.model

/**
 * 하차 점검 항목 하나.
 *
 * @param isOk 문제가 없으면 true.
 * @param canFixRemotely 앱이 직접 고칠 수 있는 항목(창문 닫기, 문 잠금)이면 true. 실제 실행 여부는 권한에 달려 있다.
 */
data class ParkingCheckItem(
    val type: CheckType,
    val area: CabinArea?,
    val isOk: Boolean,
    val canFixRemotely: Boolean,
)
