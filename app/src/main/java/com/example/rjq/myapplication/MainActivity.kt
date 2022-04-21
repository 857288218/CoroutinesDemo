package com.example.rjq.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.*
import com.example.rjq.myapplication.progress.LoadingDialog
import com.example.rjq.myapplication.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private var loadingDialog: LoadingDialog? = null
    private lateinit var viewModel: MainViewModel
    private lateinit var sharedFlow: MutableSharedFlow<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        testFlow()
        testSharedFlow()
        testStateFlow()
        testDataStore()
    }

    /**
     * SharedFlow
     * 它和stateFlow都是热流
     */
    private fun testSharedFlow() {
        sharedFlow = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        lifecycleScope.launch {
            sharedFlow.collect { i ->
                Log.d("SharedFlow_test", "pre $i thread:${Thread.currentThread()}")
            }
            // 同一协程作用域下的除第一个以外的其他sharedFlow/stateFlow.collect不会执行及监听sharedFlow/stateFlow，普通Flow不会这样，
            // SharedFlow是热流，collect之后会一直监听SharedFlow，即挂起后不会恢复，第二个collect也就执行不到了
            sharedFlow.collect { i ->
                Log.d("SharedFlow_test2", "pre $i thread:${Thread.currentThread()}")
            }
        }
        lifecycleScope.launch {
            // 当缓存数据量bufferSize>=bufferCapacity时，
            // if缓存策略为BufferOverflow.SUSPEND,emit(value)会放在buffer的queued emitters中,上面的collect能收集该lifecycleScope.launch中发的所有数据,也能收到launch中发的所有数据,launch后emit的数据(SharedFlow3)和launch中交叉接收到
            // if缓存策略为BufferOverflow.DROP_OLDEST,会丢弃buffer中最老的数据将新value加入buffer,上面的collect先收集到lifecycleScope.launch中所有emit数据的后bufferCapacity=replay+extraBufferCapacity个;然后收到launch中发的后bufferCapacity个
            // if缓存策略为BufferOverflow.DROP_LATEST,会丢弃当前CoroutineScope内超过缓存数量的新的emit value,上面collect能收到lifecycleScope.launch中emit数据的前bufferCapacity个；然后收到launch中emit的前bufferCapacity个
            // BufferOverflow.DROP_LATEST/DROP_OLDEST策略下 某个CoroutineScope内执行完所有emit后collect才会接收到数据,下面例子就是lifecycleScope.launch内所有emit完collect会收到数据,launch内所有emit完会收到数据
            sharedFlow.emit("SharedFlow")
            sharedFlow.emit("SharedFlow2")
            launch {
                sharedFlow.emit("SharedFlow4")
                sharedFlow.emit("SharedFlow5")
                sharedFlow.emit("SharedFlow6")
            }
            sharedFlow.emit("SharedFlow3")
        }
        // MutableSharedFlow不设置replay，接收不到collect之前sharedFlow#emit的值(缓存值)，如果设置了replay那么会收到replay个缓存值(bufferCapacity=replay+extraBufferCapacity)
        lifecycleScope.launch {
            sharedFlow.collect { i ->
                Log.d("SharedFlow_test", "back $i thread:${Thread.currentThread()}")
            }
        }
        lifecycleScope.launch {
            // 在ON_START时repeatOnLifecycle内部会启动协程调用block，在ON_STOP时取消该协程(协程里面的collect操作也会取消)后面重新回到ON_START时重新启动一个协程执行block，具体见源码
            // 所以if sharedFlow的replay=0,从后台返回前台后不会收到缓存数据,replay=1每次从后台回到前台都会收到缓存数据,因为每次回到前台都是重新执行sharedFlow.collect
            // 仅当UI可见时才收集flow使用repeatOnLifecycle，其更为安全的收集Android UI数据流,避免了手动在onDestroy/onStop取消协程(也就取消了flow.collect)
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedFlow.collect { i ->
                    Log.d("repeatOnLifecycle", i)
                }
            }
            // 当协程恢复时即当运行到这的时候，说明lifecycle已经是ON_DESTROY,具体见repeatOnLifecycle源码
        }
        // launchWhenXXX废弃了，使用repeatOnLifecycle代替；其会在ON_STOP时pause也就不会再执行sharedFlow.collect(但并没取消协程 协程取消后collect操作也会取消)，当ON_START后再恢复协程继续collect，具体见源码
        lifecycleScope.launchWhenStarted {
            sharedFlow.collect { i ->
                Log.d("launchWhenResumed", i)
            }
        }
    }

    /**
     * StateFlow:类似于SharedFlow的一种特殊实现(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
     * 需要设置初始值，不会发送相同值
     */
    private fun testStateFlow() {
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
            // emit内部调用的value
            stateFlow.emit("Hello")
            stateFlow.emit("StateFlow")
            stateFlow.value = "HelloValue"
            stateFlow.value = "StateFlowValue"
            // 以上发送的四条数据只会接收到StateFlowValue
            delay(500)
            // delayHello和delayHello2都会接收到
            stateFlow.value = "delayHello"
            stateFlow.value = "delayHello2"
        }
        lifecycleScope.launch {
            stateFlow.collect { i ->
                Log.d("StateFlow_test", "back $i thread:${Thread.currentThread()}")
            }
        }
        lifecycleScope.launch {
            // 只会接收到backHello2
            stateFlow.value = "backHello"
            stateFlow.value = "backHello2"
        }
        // lastHello和lastHello2都会接收到
        stateFlow.value = "lastHello"
        stateFlow.value = "lastHello2"
        // stateFlow不会收到重复数据，sharedFlow可以
        stateFlow.value = "lastHello2"
    }

    private fun testFlow() {
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
    }

    private fun testDataStore() {
        lifecycleScope.launch {
            // 写入数据
            dataStore.edit {
                it[DataStoreConstants.KEY_USER_AGE] = 27
                it[DataStoreConstants.KEY_USER_NAME] = "ren jun qing"
            }

            // 读取全部数据,当数据改变时会收到最新数据
            dataStore.data.collect {
                val userAge = it[DataStoreConstants.KEY_USER_AGE]
                val userName = it[DataStoreConstants.KEY_USER_NAME]
            }
        }

        // 使用LiveData
        dataStore.data.asLiveData().observe(this@MainActivity, Observer {

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (loadingDialog != null && loadingDialog!!.isShowing)
            loadingDialog!!.dismiss()
    }
}
