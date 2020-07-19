package com.example.rjq.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rjq.myapplication.NoticeUtils
import com.example.rjq.myapplication.entity.User
import com.example.rjq.myapplication.http.HttpMethods
import kotlinx.coroutines.*

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private var userLive: MutableLiveData<User>? = null

    fun login(userName: String, pwd: String): LiveData<User>? {
        if (userLive == null || (userLive?.value == null)) {
            userLive = MutableLiveData()
            viewModelScope.launch {
                //开启一个协程
                //如果串行调用多个接口的话，使用协程可以避免回调地狱；以同步的方式实现异步的逻辑
                val data = HttpMethods.INSTANCES.login(userName, pwd)
                userLive?.value = data.data
                if (data.errorMsg?.isNotEmpty() == true) {
                    NoticeUtils.showToast(data.errorMsg)
                }
            }
        }
        return userLive
    }

}