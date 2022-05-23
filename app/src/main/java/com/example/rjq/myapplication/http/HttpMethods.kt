package com.example.rjq.myapplication.http

import androidx.lifecycle.LiveData
import com.example.rjq.myapplication.entity.User
import com.example.rjq.myapplication.entity.WanResponse
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
                .addCallAdapterFactory(ApiResultCallAdapterFactory())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .baseUrl(BASE_URL)
                .build()
                .create(MovieService::class.java)
            HttpMethods()
        }
    }

    // 以下网络请求还有数据库请求应该放到XXXRepository中，ViewModel持有XXXRepository，看JiduWork feture-guide模块
    suspend fun login(userName: String, pwd: String): WanResponse<User> {
        //如果movieService.login使用最原始的方法返回Call<WanResponse<User>>,那么就需要调用call.enqueue(有两个回调)解析response,然后login返回LiveData<WanResponse<User>>
        return movieService.loginAsync(userName, pwd)
    }

    fun loginLive(userName: String, pwd: String): AutoRemoveForeverObserverLiveData<WanResponse<User>> {
        //如果movieService.login使用最原始的方法返回Call<WanResponse<User>>,那么就需要调用call.enqueue(有两个回调)解析response,然后login返回LiveData<WanResponse<User>>
        return movieService.login(userName, pwd)
    }

    fun loginLive2(userName: String, pwd: String): LiveData<WanResponse<User>> {
        //如果movieService.login使用最原始的方法返回Call<WanResponse<User>>,那么就需要调用call.enqueue(有两个回调)解析response,然后login返回LiveData<WanResponse<User>>
        return movieService.login2(userName, pwd)
    }
}