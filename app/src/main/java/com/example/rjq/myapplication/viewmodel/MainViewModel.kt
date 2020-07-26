package com.example.rjq.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rjq.myapplication.NoticeUtils
import com.example.rjq.myapplication.entity.User
import com.example.rjq.myapplication.http.HttpMethods
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private var userLive: MutableLiveData<User>? = null

    fun login(userName: String, pwd: String): LiveData<User>? {
        if (userLive == null || (userLive?.value == null)) {
            userLive = MutableLiveData()
//            viewModelScope.launch {
//                //开启一个协程
//                //如果串行调用多个接口的话，使用协程可以避免回调地狱；以同步的方式实现异步的逻辑
//                val data = HttpMethods.INSTANCES.login(userName, pwd)
//                userLive?.value = data.data
//                if (data.errorMsg?.isNotEmpty() == true) {
//                    NoticeUtils.showToast(data.errorMsg)
//                }
//            }

            val handler = CoroutineExceptionHandler {
                context, exception -> println("Caught $exception")
            }
            val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//            scope.launch {
//                launch {
//                    delay(1000)
//                    val data = HttpMethods.INSTANCES.login(userName, pwd)
//                    userLive?.value = data.data
//                    if (data.errorMsg?.isNotEmpty() == true) {
//                        NoticeUtils.showToast(data.errorMsg)
//                    }
//                }
//                //不会崩溃，如果换成coroutineScope(或者去掉supervisorScope)会崩溃；在throw Exception外try不会崩溃；去掉deferred.await()外的try会崩溃
//                supervisorScope {
//                    val deferred = async {
////                        try {
//                            throw Exception("async failed")
////                        } catch (e: Exception) {
//
////                        }
//                    }
//                    try {
//                        deferred.await()
//                    } catch (e: Exception) {
//
//                    }
//                }
//            }
            //不会崩溃，如果try/catch包裹scope.launch会崩溃
            scope.launch(Dispatchers.IO) {
                try {
                    throw Exception("Failed coroutine")
                } catch (e: Exception) {

                }
            }
            val flowA = flow {
                emit(1)
                throw ArithmeticException("Div 0")
            }.catch { t: Throwable ->
                println("caught error: $t")
            }.onCompletion { t: Throwable? ->
                println("finally.")
            }
        }
        return userLive
    }

}