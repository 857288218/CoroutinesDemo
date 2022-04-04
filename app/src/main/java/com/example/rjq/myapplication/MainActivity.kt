package com.example.rjq.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.example.rjq.myapplication.progress.LoadingDialog
import com.example.rjq.myapplication.viewmodel.MainViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class MainActivity : BaseActivity() {

    private var loadingDialog: LoadingDialog? = null
    private lateinit var viewModel: MainViewModel
    private val a = "ddd"
    private val b
        get() = "sdsd"
    private var e = "bbb"
        get() = "sss"
    private var f = "lll"
    private var g = "ddd"
        set(value) {
            field = value
        }
        get() = field

    object T {
        val c = "ccc"
        val d
            get() = "ddd"
        var ff = "fff"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val h = a
        val j = b
        val k = e
        val l = f
        val ll = g
        e = "mmmm"
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        //测试类的初始化：读取或设置一个类的静态字段（被final修饰，已在编译器把结果放入常量池的静态字段除外），如果类没有进行初始化，则先进行初始化
        TestLoader.instance2

        lifecycleScope.launch {
            val currentTime = System.currentTimeMillis()
            val a = async(Dispatchers.IO) {
                Thread.sleep(1000)
//                delay(1000)
                1 + 2
            }

            val b = async(Dispatchers.IO) {
                Thread.sleep(2000)
//                delay(2000)
                1 + 3
            }

            val c = a.await() + b.await()
            //将async中Dispatchers.IO去掉，那么时间是3秒
            Log.d("rjqasync", "${System.currentTimeMillis() - currentTime}, c: $c")
        }
        viewModel.userLive.observe(this, Observer {
            findViewById<TextView>(R.id.result_TV).text = it?.toString()
        })
        findViewById<View>(R.id.click_me_BN).setOnClickListener {
//            viewModel.loginTest("15620419359", "rjq015")
            viewModel.login("15620419359", "rjq015")

//            CoroutineScope(Dispatchers.Main).launch {
//                Log.d("testGlobalScope", "1")
//            }
//            CoroutineScope(Dispatchers.Main.immediate).launch {
//                Log.d("testGlobalScope", "2")
//            }
            Log.d("testGlobalScope", "0")
        }

//        val user = User::class.java.newInstance()
//        Log.d("rjqtest", user.toString())

//        viewModel.login("rjq", "rjqqqq")

        /**
         * Flow
         */
        val sequence = sequenceOf(1, 2, 3, 4)
        val result: Sequence<Int> = sequence.map { i ->
            Log.d("sequence_test", "Sequence Map $i")
            i * 2
        }.filter { i ->
            Log.d("sequence_test", "Sequence Filter $i")
            i % 3 == 0
        }
        Log.d("sequence_test", result.count().toString())
        Log.d("sequence_test", result.first().toString())

        val list = listOf(1, 2, 3, 4)
        val resultList: List<Int> = list.map { i ->
            Log.d("sequence_test", "List Map $i")
            i * 2
        }.filter { i ->
            Log.d("sequence_test", "List Filter $i")
            i % 3 == 0
        }
//        Log.d("sequence_test", resultList.first().toString())
//        Log.d("sequence_test", resultList.first().toString())

        //一个 Flow 创建出来之后，不消费则不生产，多次消费则多次生产;所谓冷数据流，就是只有消费时才会生产的数据流
        lifecycleScope.launch(Dispatchers.Main) {
            val flow = flow {
                // With the flow builder, the producer cannot emit values from a different CoroutineContext
                // Therefore, don't call emit in a different CoroutineContext by creating new coroutines or by using withContext blocks of code
                emit(1)
                emit(2)
//                throw IllegalStateException()
                emit(3)
                emit(4)
            }.map { i ->
                delay(1000)
                Log.d("flow_test", "Flow Map $i thread:${Thread.currentThread()}")
                i * 2
            }.flowOn(Dispatchers.Main) //默认情况下Flow数据会运行在调用者的上下文(线程)中,可以用flowOn()来改变上游的上下文，这里的上游是指调用flowOn之前的所有操作符
                    .filter { i ->
                        Log.d("flow_test", "Flow Filter $i thread:${Thread.currentThread()}")
                        i % 3 == 0
                    }
                    .flowOn(Dispatchers.Default)
                    .onCompletion {
                        //无论前面是否存在异常，它都会被调用
                        Log.d("flow_test", "Flow onCompletion thread:${Thread.currentThread()}")
                        throw IllegalStateException()
                    }
                    .catch {
                        //Flow从不捕获或处理下游流中发生的异常，它们仅使用catch运算符捕获上游发生的异常
                        Log.d("flow_test", "Flow catch $this thread:${Thread.currentThread()}")
                    }
            flow.collect { i ->
                Log.d("flow_test", "Flow collect $i thread:${Thread.currentThread()}")
            }
//            flow.collect { i ->
//                Log.d("flow_test2", "Flow collect $i thread:${Thread.currentThread()}")
//            }

        }

        /**
         * SharedFlow
          */
        val sharedFlow = MutableSharedFlow<String>()
        lifecycleScope.launch {
            sharedFlow.collect { i ->
                Log.d("SharedFlow_test", "pre $i thread:${Thread.currentThread()}")
            }
            // 同一协程作用域下的除第一个以外的其他sharedFlow.collect不会执行以及监听sharedFlow，针对普通Flow不会这样，
            // SharedFlow因为是热流，collect之后会一直监听SharedFlow，即挂起后不会恢复，第二个collect也就执行不到了
            sharedFlow.collect { i ->
                Log.d("SharedFlow_test2", "pre $i thread:${Thread.currentThread()}")
            }
        }
        lifecycleScope.launch {
            sharedFlow.emit("Hello")
            sharedFlow.emit("SharedFlow")
            sharedFlow.emit("SharedFlow2")
        }
        // 如果MutableSharedFlow不设置replay参数，在sharedFlow.emit之后去订阅那么接收不到订阅之前emit值
        lifecycleScope.launch {
            sharedFlow.collect { i ->
                Log.d("SharedFlow_test", "back $i thread:${Thread.currentThread()}")
            }
        }

        /**
         * StateFlow:是SharedFlow 的一种特殊实现(replay = 1),类似liveDta，也具有粘性效果
         */
        val stateFlow = MutableStateFlow("say")
        lifecycleScope.launch {
            launch {
                Log.d("lifecyclerLaunch", "1")
            }
            launch {
                Log.d("lifecyclerLaunch", "2")
            }
            launch {
                Log.d("lifecyclerLaunch", "3")
            }
            Log.d("lifecyclerLaunch", "0")
            stateFlow.collect { i ->
                Log.d("StateFlow_test", "pre $i thread:${Thread.currentThread()}")
            }
        }
        lifecycleScope.launch {
            stateFlow.emit("Hello")
            stateFlow.emit("StateFlow")
            stateFlow.value = "HelloValue"
            stateFlow.value = "StateFlowValue"
            delay(500)
//            Thread.sleep(5000)
            stateFlow.value = "delayHello"
        }
        lifecycleScope.launch {
            stateFlow.collect { i ->
                Log.d("StateFlow_test", "back $i thread:${Thread.currentThread()}")
            }
        }
        lifecycleScope.launch {
            stateFlow.value = "backHello1"
        }
        stateFlow.value = "backHello2"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (loadingDialog != null && loadingDialog!!.isShowing)
            loadingDialog!!.dismiss()
    }
}
