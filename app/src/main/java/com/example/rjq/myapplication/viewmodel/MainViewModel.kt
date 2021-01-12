package com.example.rjq.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
            viewModelScope.launch {
                //开启一个协程
                //如果串行调用多个接口的话，使用协程可以避免回调地狱；以同步的方式实现异步的逻辑
                val currentTime = System.currentTimeMillis()
                Log.d("renjunqingTime", Thread.currentThread().toString())
                //开一个协程(launch/async)相当于向主线程(或指定线程)发消息等待执行，
                //下面例子，先打印System.currentTimeMillis() - currentTime，主线程睡3秒，然后第一个launch执行:主线程睡3秒，login(挂起函数)切到子线程中去执行，login后面的代码需要等到login执行完成后再执行
                //相当于一同和login切到了子线程中，等login执行完后，再切回(handle.post)launch指定得线程
                //当执行到第一个launch/async login时，login切线程去执行，第二个launch/async会在主线程中执行打印Thread.currentThread().toString()，然后login切线程执行，
                //由于两个launch/async中的login都是在子线程执行的，所以两个login后面打印的日志先后顺序是不确定的，取决于哪个login先执行完切回主线程即launch/async所在线程
                val deffer1 = async {
                    Log.d("renjunqingTime", Thread.currentThread().toString() + "1")
                    Thread.sleep(3000)
                    //切到子线程去执行
                    val data = HttpMethods.INSTANCES.login(userName, pwd)
                    //等待login在子线程执行完，再切回launch/async所在线程(handler.post切)
                    Log.d("renjunqingTime", "我是第一个launch login后的代码")
                }
                val deffer2 = async {
                    Log.d("renjunqingTime", Thread.currentThread().toString() + "2")
                    HttpMethods.INSTANCES.login(userName, pwd)
                    Log.d("renjunqingTime", "我是第二个launch login后的代码")
                }
//                deffer1.await()
//                deffer2.await()
                Log.d("renjunqingTime", "${System.currentTimeMillis() - currentTime}")
                Thread.sleep(3000)
//                userLive?.value = data.data
//                if (data.errorMsg?.isNotEmpty() == true) {
//                    NoticeUtils.showToast(data.errorMsg)
//                }
            }

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