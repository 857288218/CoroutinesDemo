package com.example.rjq.myapplication

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.rjq.myapplication.progress.LoadingDialog
import com.example.rjq.myapplication.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var loadingDialog: LoadingDialog? = null
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        //生产者
        val list1: ArrayList<out TextView> = ArrayList<Button>()
        val textView = list1[0]
//        list1.add(Button(this))   //报错 禁止使用add方法

        //消费者
        val list2: ArrayList<in TextView> = ArrayList<View>() //只能add TextView：Button EditText都是TextView(多态)
        list2.add(Button(this))
        list2.add(EditText(this))
        list2.add(TextView(this))
//        list2.add(View(this))   //报错
        val button = list2[0]                   //只能得到Any类型的
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
