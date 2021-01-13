package com.example.rjq.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.rjq.myapplication.progress.LoadingDialog
import com.example.rjq.myapplication.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
            flow {
                for (i in 1..10) {
                    Log.d("rjqflow", "emit $i")
                    emit(i)
                }
            }.catch {

            }.filter {
                Log.d("rjqflow", "filter $it")
                true
            }.map {
                Log.d("rjqflow", "map $it")
                it
            }.flowOn(Dispatchers.IO).onCompletion {
                Log.d("rjqflow", "完成")
            }.collect {
                Log.d("rjqflow", "collect $it")
            }
        }

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

        findViewById<View>(R.id.click_me_BN).setOnClickListener {
            viewModel.login("15620419359", "rjq015")?.observe(this, Observer {
                result_TV?.text = it?.toString()
            })
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (loadingDialog != null && loadingDialog!!.isShowing)
            loadingDialog!!.dismiss()
    }
}
