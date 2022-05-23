package com.example.rjq.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

open class BaseViewModel(app: Application) : AndroidViewModel(app) {

    private var onClearedListenerList: ArrayList<() -> Unit>? = null

    fun addOnClearedListener(callback: () -> Unit) {
        if (onClearedListenerList == null) {
            onClearedListenerList = ArrayList()
        }
        onClearedListenerList!!.add(callback)
    }

    override fun onCleared() {
        super.onCleared()
        onClearedListenerList?.forEach {
            it.invoke()
        }
        onClearedListenerList?.clear()
    }

    // use this function instead of LiveData.observeForever in viewModel
    fun <T> LiveData<T>.observe(observer: Observer<T>) {
        this.observeForever(observer)
        addOnClearedListener {
            this.removeObserver(observer)
        }
    }
}