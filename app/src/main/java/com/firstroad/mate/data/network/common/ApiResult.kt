package com.firstroad.mate.data.network.common

/** 네트워크 호출 결과.호출하는 쪽은 try-catch 없이 이 타입으로만 분기한다. */
sealed interface ApiResult<out T> {
    data class Success<T>(val body: T) : ApiResult<T>

    /** 서버가 오류 상태 코드로 응답함. */
    data class ApiError(val code: Int, val message: String?) : ApiResult<Nothing>

    /** 연결 실패·타임아웃 등 응답을 받지 못함. */
    data class NetworkError(val cause: Throwable) : ApiResult<Nothing>
}
