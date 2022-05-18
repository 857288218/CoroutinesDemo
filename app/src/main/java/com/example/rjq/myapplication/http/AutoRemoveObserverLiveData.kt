package com.example.rjq.myapplication.http

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.rjq.myapplication.viewmodel.BaseViewModel
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

open class AutoRemoveObserverLiveData<T> : LiveData<T>() {

    protected var started = AtomicBoolean(false)
    private var foreverObservers: ArrayList<Observer<in T>>? = null
    private var viewModelTag: BaseViewModel? = null

    private class ClearedCallback<T>(val liveData: WeakReference<AutoRemoveObserverLiveData<T>>) :
        Function0<Unit> {
        override fun invoke() {
            liveData.get()?.run {
                removeForeverObservers()
                viewModelTag = null
            }
        }
    }

    fun setTag(tag: BaseViewModel): LiveData<T> {
        if (this.viewModelTag == null) {
            this.viewModelTag = tag
            this.viewModelTag!!.addOnClearedCallback(ClearedCallback(WeakReference(this)))
        }
        return this
    }

    override fun observeForever(observer: Observer<in T>) {
        super.observeForever(observer)
        if (viewModelTag != null) {
            if (foreverObservers == null) {
                foreverObservers = ArrayList()
            }
            foreverObservers!!.add(observer)
        }
    }

    private fun removeForeverObservers() {
        foreverObservers?.forEach {
            removeObserver(it)
        }
        foreverObservers?.clear()
    }
}