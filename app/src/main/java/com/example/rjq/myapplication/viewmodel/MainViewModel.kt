package com.example.rjq.myapplication.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rjq.myapplication.BaseApplication
import com.example.rjq.myapplication.entity.ApiResult
import com.example.rjq.myapplication.entity.User
import com.example.rjq.myapplication.entity.WanResponse
import com.example.rjq.myapplication.http.HttpMethods
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

class MainViewModel(app: Application) : AndroidViewModel(app) {

    var userLive: MutableLiveData<WanResponse<User>> = MutableLiveData()

    fun loginTest(userName: String, pwd: String): LiveData<WanResponse<User>>? {
        viewModelScope.launch {
            when (val result = HttpMethods.INSTANCES.login(userName, pwd)) {
                is ApiResult.Success -> {
                    if (result.data?.errorCode == 0) {
                        userLive.value = result.data
                    } else {
                        //业务错误，弹toast，也可以把业务错误封装到ApiResult.Failure中
                        Toast.makeText(BaseApplication.context, result.data?.errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
                is ApiResult.Failure -> {
                    //http status错误或网络请求中发生异常，弹toast
                    Toast.makeText(BaseApplication.context, result.errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
        return userLive
    }

    fun login(userName: String, pwd: String): LiveData<WanResponse<User>>? {
        if (userLive == null || (userLive.value == null)) {
            userLive = MutableLiveData()
            viewModelScope.launch {
                val currentTime = System.currentTimeMillis()
                Log.d("renjunqingTime", Thread.currentThread().toString())
                //在viewModelScope/lifecycleScope.launch协程代码块里用launch/async再开协程相当于向该"协程post消息"等待执行,只有当viewModelScope/lifecycleScope协程里的代码执行完再回来按顺序执行launch/async协程里代码
                //viewModelScope/lifecycleScope.launch/async会立即执行代码块里的内容,只有当该协程里的代码执行完(包括里面launch/async代码块)或挂起才会执行协程后的代码
                //CoroutineScope().launch/async需要等其后面的代码执行完再执行该协程里的代码相当于向线程postMessage,可以使用Dispatchers.Main.immediate做到立即执行协程里的代码,viewModelScope/lifecycleScope就使用的Dispatchers.Main.immediate

                //下面例子先打印System.currentTimeMillis() - currentTime,主线程睡3秒(deffer1.await(),deffer2.await()执行的情况下最后才打印System.currentTimeMillis()-currentTime,主线程睡3秒)，
                //然后第一个launch/async执行:主线程睡3秒,login(挂起函数)切到子线程中执行,该协程中login后面的代码需要等login执行完成后再执行；
                //相当于同login切到子线程中,等login执行完,再切回(handle.post)launch/async指定的线程继续执行;如果login不是挂起函数,那么会立即打印"我是第一个login后的代码"
                //当执行到第一个launch/async login时,login切线程去执行,第二个launch/async会在主线程中执行打印Thread.currentThread().toString(),然后login切线程执行，
                //由于两个launch/async中的login都是在子线程执行的，所以两个login后面打印的日志先后顺序不确定,取决于哪个login先执行完切回主线程即launch/async所在线程
                val deffer1 = async {
                    Log.d("renjunqingTime", Thread.currentThread().toString() + "1")
                    // 注意：Thread.sleep和delay不同，Thread是阻塞当前线程，delay是挂起当前协程(切线程去阻塞这个协程作用域中delay后面的代码，不阻塞当前线程,当时间到了又切回当前线程继续执行)
//                    Thread.sleep(3000)
//                    delay(3000)
                    //切到子线程去执行
                    val data = HttpMethods.INSTANCES.login(userName, pwd)
                    //等待login在子线程执行完，再切回launch/async所在线程(handler.post切)
                    Log.d("renjunqingTime", "我是第一个login后的代码")
                }
                val deffer2 = async {
                    Log.d("renjunqingTime", Thread.currentThread().toString() + "2")
//                    delay(3000)
                    HttpMethods.INSTANCES.login(userName, pwd)
                    Log.d("renjunqingTime", "我是第二个login后的代码")
                }
//                deffer1.await()
//                deffer2.await()
                Log.d("renjunqingTime", "${System.currentTimeMillis() - currentTime}")
//                Thread.sleep(3000)
//                userLive?.value = data.data
//                if (data.errorMsg?.isNotEmpty() == true) {
//                    NoticeUtils.showToast(data.errorMsg)
//                }
            }

            viewModelScope.launch {
//                delay(500)
                Log.d("viewModelScopetest", "1")
            }

            viewModelScope.launch {
                Log.d("viewModelScopetest", "2")
            }

            val handler = CoroutineExceptionHandler { context, exception ->
                println("Caught $exception")
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
        }
        return userLive
    }

}