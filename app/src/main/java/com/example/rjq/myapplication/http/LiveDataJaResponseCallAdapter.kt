package com.example.rjq.myapplication.http

import android.util.Log
import com.example.rjq.myapplication.entity.WanResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

/**
 * A Retrofit adapter that converts the Call into a LiveData of ApiResponse.
 */
class LiveDataJaResponseCallAdapter<R>(private val responseType: Type) :
    CallAdapter<R, AutoRemoveObserverLiveData<R>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<R>): AutoRemoveObserverLiveData<R> {
        return object : AutoRemoveObserverLiveData<R>() {
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    call.enqueue(object : Callback<R> {

                        override fun onResponse(call: Call<R>, response: Response<R>) {
                            if (response.isSuccessful) {
                                postValue(response.body())
                            } else {
                                val errorResult =
                                    WanResponse(-1, "服务器状态码异常：" + response.code(), null)
                                postValue(errorResult as R)
                            }
                        }

                        override fun onFailure(call: Call<R>, throwable: Throwable) {
                            Log.e("dev", "LiveDataJaResponseCallAdapter: $throwable")
                            val failureResult =
                                WanResponse(-1, "当前网络不给力,请确认网络已连接:${throwable.message}", null)
                            postValue(failureResult as R)
                        }
                    })
                }
            }

            override fun onInactive() {
                super.onInactive()
                call.cancel()
            }
        }
    }
}
