package com.firstroad.mate.data.network.common

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/** Retrofit Call을 감싸 모든 결과를 ApiResult로 바꾼다. */
internal class ApiResultCall<T : Any>(
    private val delegate: Call<T>,
) : Call<ApiResult<T>> {

    override fun enqueue(callback: Callback<ApiResult<T>>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onResponse(this@ApiResultCall, Response.success(response.toApiResult()))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onResponse(this@ApiResultCall, Response.success(ApiResult.NetworkError(t)))
            }
        })
    }

    override fun execute(): Response<ApiResult<T>> = try {
        Response.success(delegate.execute().toApiResult())
    } catch (error: IOException) {
        Response.success(ApiResult.NetworkError(error))
    }

    private fun Response<T>.toApiResult(): ApiResult<T> {
        val body = body()
        return if (isSuccessful && body != null) {
            ApiResult.Success(body)
        } else {
            ApiResult.ApiError(code(), errorBody()?.string())
        }
    }

    override fun clone(): Call<ApiResult<T>> = ApiResultCall(delegate.clone())
    override fun isExecuted(): Boolean = delegate.isExecuted
    override fun cancel() = delegate.cancel()
    override fun isCanceled(): Boolean = delegate.isCanceled
    override fun request(): Request = delegate.request()
    override fun timeout(): Timeout = delegate.timeout()
}
