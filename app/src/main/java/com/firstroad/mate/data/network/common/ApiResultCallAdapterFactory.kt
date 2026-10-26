package com.firstroad.mate.data.network.common

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/** `suspend fun ...(): ApiResult<T>` 형태의 Retrofit 서비스 메서드를 지원한다. */
class ApiResultCallAdapterFactory : CallAdapter.Factory() {

    override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != ApiResult::class.java) return null
        val bodyType = getParameterUpperBound(0, callType as ParameterizedType)
        return ApiResultCallAdapter<Any>(bodyType)
    }

    private class ApiResultCallAdapter<T : Any>(
        private val bodyType: Type,
    ) : CallAdapter<T, Call<ApiResult<T>>> {
        override fun responseType(): Type = bodyType
        override fun adapt(call: Call<T>): Call<ApiResult<T>> = ApiResultCall(call)
    }
}
