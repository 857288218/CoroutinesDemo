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
                val data = withContext(Dispatchers.IO) {
                    HttpMethods.INSTANCES.login(userName, pwd)
                }
                userLive?.value = data.data
            }
        }
        return userLive
    }
}