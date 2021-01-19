package com.example.rjq.myapplication.http

import com.example.rjq.myapplication.entity.ApiResult
import com.example.rjq.myapplication.entity.User
import com.example.rjq.myapplication.entity.WanResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MovieService {

    @FormUrlEncoded
    @POST("/user/login")
    //可以使用最原始的方法返回Call<WanResponse<User>>
    suspend fun loginAsync(@Field("username") username: String, @Field("password") password: String): ApiResult<WanResponse<User>>
}
