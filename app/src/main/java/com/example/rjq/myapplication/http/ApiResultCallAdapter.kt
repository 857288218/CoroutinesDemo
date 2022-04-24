package com.example.rjq.myapplication.http

import com.example.rjq.myapplication.entity.ApiResult
import okhttp3.Request
import okio.Timeout
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApiResultCallAdapterFactory : CallAdapter.Factory() {
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *> {
        check(getRawType(returnType) == Call::class.java) { "$returnType must be retrofit2.Call." }
        check(returnType is ParameterizedType) { "$returnType must be parameterized. Raw types are not supported" }

        //取出Call<T> 里的T，检查是否是ApiResult<T>
        val apiResultType = getParameterUpperBound(0, returnType)
        check(getRawType(apiResultType) == ApiResult::class.java) { "$apiResultType must be ApiResult." }
        check(apiResultType is ParameterizedType) { "$apiResultType must be parameterized. Raw types are not supported" }

        //取出ApiResult<T>中的T 也就是API返回数据对应的数据类型
        val dataType = getParameterUpperBound(0, apiResultType)
        return ApiResultCallAdapter<Any>(dataType)
    }
}

class ApiResultCallAdapter<T>(private val type: Type) : CallAdapter<T, Call<ApiResult<T>>> {
    override fun responseType(): Type = type

    override fun adapt(call: Call<T>): Call<ApiResult<T>> {
        return ApiResultCall(call)
    }
}

class ApiResultCall<T>(private val delegate: Call<T>) : Call<ApiResult<T>> {
    /**
     * 该方法会被Retrofit处理suspend方法的代码调用，并传进来一个callback,如果你回调了callback.onResponse，那么suspend方法就会成功返回
     * 如果你回调了callback.onFailure那么suspend方法就会抛异常
     *
     * 所以我们这里的实现是永远回调callback.onResponse,
     * 这样外面在调用suspend方法的时候就不会抛异常
     */
    override fun enqueue(callback: Callback<ApiResult<T>>) {
        //delegate 是用来做实际的网络请求的Call<T>对象，网络请求的成功失败会回调不同的方法
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    // Returns true if the code is in [200..300),which means the request was successfully received,
                    // understood, and accepted.
                    callback.onResponse(this@ApiResultCall, Response.success(ApiResult.Success(response.body())))
                } else {
                    //http status错误, means the request not received, understood and accepted.
                    val failureApiResult = ApiResult.Failure(response.code(), "服务器状态码异常：" + response.code())
                    callback.onResponse(this@ApiResultCall, Response.success(failureApiResult))
                }

            }

            /**
             * 在网络请求中发生了异常，会回调该方法
             */
            override fun onFailure(call: Call<T>, t: Throwable) {
                val failureApiResult = ApiResult.Failure(-2, "当前网络不给力,请确认网络已连接:${t.message}")
                callback.onResponse(this@ApiResultCall, Response.success(failureApiResult))
            }
        })
    }

    override fun clone(): Call<ApiResult<T>> = ApiResultCall(delegate.clone())

    override fun execute(): Response<ApiResult<T>> {
        throw UnsupportedOperationException("WanResponse does not support synchronous execution")
    }

    override fun isExecuted(): Boolean {
        return delegate.isExecuted
    }

    override fun cancel() {
        delegate.cancel()
    }

    override fun isCanceled(): Boolean {
        return delegate.isCanceled
    }

    override fun request(): Request {
        return delegate.request()
    }

    override fun timeout(): Timeout {
        return delegate.timeout()
    }
}