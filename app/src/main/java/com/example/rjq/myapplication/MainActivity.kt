package com.example.rjq.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.example.rjq.myapplication.progress.LoadingDialog
import com.example.rjq.myapplication.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
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

        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.login("15620419359", "rjq015")?.observe(this, Observer {
            result_TV?.text = it?.toString()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (loadingDialog != null && loadingDialog!!.isShowing)
            loadingDialog!!.dismiss()
    }
}
