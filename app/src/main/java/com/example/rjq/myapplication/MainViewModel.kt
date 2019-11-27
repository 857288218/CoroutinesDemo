package com.example.rjq.myapplication

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.example.rjq.myapplication.entity.User
import com.example.rjq.myapplication.http.HttpMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private var userLive: MutableLiveData<User>? = null

    fun login(userName: String, pwd: String): LiveData<User>? {
        if (userLive == null || userLive?.value == null) {
            userLive = MutableLiveData()
            GlobalScope.launch(Dispatchers.Main) {
                //如果串行调用多个接口的话，使用协程可以避免回调地狱；以同步的方式实现异步的逻辑
                val data = HttpMethods.INSTANCES.login(userName, pwd)
                userLive?.value = data.data
            }
        }
        return userLive
    }
}