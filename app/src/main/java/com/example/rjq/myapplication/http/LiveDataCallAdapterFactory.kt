package com.example.rjq.myapplication.http

import androidx.lifecycle.LiveData
import com.example.rjq.myapplication.entity.WanResponse
import retrofit2.CallAdapter
import retrofit2.CallAdapter.Factory
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class LiveDataCallAdapterFactory : Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != LiveData::class.java && getRawType(returnType) != AutoRemoveObserverLiveData::class.java) {
            return null
        }

        val observableType = getParameterUpperBound(0, returnType as ParameterizedType)
        val rawObservableType = getRawType(observableType)
        if (rawObservableType != WanResponse::class.java) {
            throw IllegalArgumentException("type must be a resource")
        }
        if (observableType !is ParameterizedType) {
            throw IllegalArgumentException("resource must be parameterized")
        }
        // WanResponse中的data type
        val bodyType = getParameterUpperBound(0, observableType)
        // observableType == WanResponse
        return LiveDataJaResponseCallAdapter<Any>(observableType)
    }
}
