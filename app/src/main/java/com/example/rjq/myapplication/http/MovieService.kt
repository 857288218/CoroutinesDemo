package com.example.rjq.myapplication.http

import androidx.lifecycle.LiveData
import com.example.rjq.myapplication.entity.User
import com.example.rjq.myapplication.entity.WanResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MovieService {

    @FormUrlEncoded
    @POST("/user/login")
    //可以使用最原始的方法返回Call<WanResponse<User>>
    suspend fun loginAsync(
        @Field("username") username: String,
        @Field("password") password: String
    ): WanResponse<User>

    @FormUrlEncoded
    @POST("/user/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): AutoRemoveObserverLiveData<WanResponse<User>>

    @FormUrlEncoded
    @POST("/user/login")
    fun login2(
        @Field("username") username: String,
        @Field("password") password: String
    ): LiveData<WanResponse<User>>
}
