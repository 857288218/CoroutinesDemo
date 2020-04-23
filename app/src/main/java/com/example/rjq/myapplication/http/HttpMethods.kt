package com.example.rjq.myapplication.http

import com.example.rjq.myapplication.BaseApplication
import com.example.rjq.myapplication.R
import com.example.rjq.myapplication.entity.User
import com.example.rjq.myapplication.entity.WanResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
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
                    .baseUrl(BASE_URL)
                    .build()
                    .create(MovieService::class.java)
            HttpMethods()
        }
    }

    private fun handleException(e: Exception): String {
        return when (e) {
            is ConnectException -> BaseApplication.context.getString(R.string.network_connect_error)
            is SocketTimeoutException -> BaseApplication.context.getString(R.string.network_connect_timeout)
            is ResponseCodeException -> BaseApplication.context.getString(R.string.network_response_code_error) + e.responseCode
            is NoRouteToHostException -> BaseApplication.context.getString(R.string.no_route_to_host)
            else -> BaseApplication.context.getString(R.string.unknown_error)
        }
    }

    suspend fun login(userName: String, pwd: String): WanResponse<User?> {
        //如果movieService.login使用最原始的方法返回Call<WanResponse<User>>,那么就需要调用call.enqueue(有两个回调)解析response,然后login返回LiveData<WanResponse<User>>
        return try {
            movieService.loginAsync(userName, pwd)
        } catch (e: Exception) {
            WanResponse<User>(-1, handleException(e), null)
        }
    }
}