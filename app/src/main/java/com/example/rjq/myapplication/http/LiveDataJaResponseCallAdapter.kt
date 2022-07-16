package com.example.rjq.myapplication.http

import android.util.Log
import com.example.rjq.myapplication.entity.Status
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
    CallAdapter<R, AutoRemoveForeverObserverLiveData<R>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<R>): AutoRemoveForeverObserverLiveData<R> {
        return object : AutoRemoveForeverObserverLiveData<R>() {
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    call.enqueue(object : Callback<R> {

                        override fun onResponse(call: Call<R>, response: Response<R>) {
                            if (response.isSuccessful) {
                                val wanResponse = response.body() as WanResponse<*>
                                if (wanResponse.errorCode == 0) {
                                    wanResponse.status = Status.SUCCESS
                                } else {
                                    wanResponse.status = Status.ERROR
                                }
                                postValue(wanResponse as R)
                            } else {
                                val errorResult =
                                    WanResponse(
                                        Status.ERROR,
                                        -1,
                                        "服务器状态码异常：" + response.code(),
                                        null
                                    )
                                postValue(errorResult as R)
                            }
                        }

                        override fun onFailure(call: Call<R>, throwable: Throwable) {
                            Log.e("dev", "LiveDataJaResponseCallAdapter: $throwable")
                            val failureResult =
                                WanResponse(
                                    Status.FAILURE,
                                    -1,
                                    "当前网络不给力,请确认网络已连接:${throwable.message}",
                                    null
                                )
                            postValue(failureResult as R)
                        }
                    })
                }
            }

            override fun onTagDestroy() {
                super.onTagDestroy()
                call.cancel()
            }
        }
    }
}
