package com.example.rjq.myapplication.http

import com.example.rjq.myapplication.entity.User
import com.example.rjq.myapplication.entity.WanResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class HttpMethods private constructor() {

    companion object {
        private const val DEFAULT_TIMEOUT = 8
        private const val BASE_URL = "https://www.wanandroid.com"
        private lateinit var movieService: MovieService

        val INSTANCES: HttpMethods by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            //手动创建一个OkHttpClient并设置超时时间
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)

            movieService = Retrofit.Builder()
                    .client(builder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory.invoke())
                    .baseUrl(BASE_URL)
                    .build()
                    .create(MovieService::class.java)
            HttpMethods()
        }
    }

    suspend fun login(userName: String, pwd: String): WanResponse<User> {
        //如果movieService.login使用最原始的方法返回Call<WanResponse<User>>,那么就需要调用这个call.enqueue解析response,然后再返回LiveData<WanResponse<User>>
        return withContext(Dispatchers.IO) {
            movieService.login(userName, pwd).await()
        }
    }
}