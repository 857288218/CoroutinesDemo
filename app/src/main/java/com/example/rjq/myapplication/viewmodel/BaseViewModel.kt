package com.example.rjq.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel

open class BaseViewModel(app: Application) : AndroidViewModel(app) {
    private var onClearedCallbackList: ArrayList<() -> Unit>? = null

    fun addOnClearedCallback(callback: () -> Unit) {
        if (onClearedCallbackList == null) {
            onClearedCallbackList = ArrayList()
        }
        onClearedCallbackList!!.add(callback)
    }

    override fun onCleared() {
        super.onCleared()
        onClearedCallbackList?.forEach {
            it.invoke()
        }
        onClearedCallbackList?.clear()
    }

}