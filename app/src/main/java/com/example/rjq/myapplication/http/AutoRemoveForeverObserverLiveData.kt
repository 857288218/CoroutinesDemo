package com.example.rjq.myapplication.http

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.rjq.myapplication.viewmodel.BaseViewModel
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

open class AutoRemoveForeverObserverLiveData<T> : LiveData<T>() {

    protected var started = AtomicBoolean(false)
    private var foreverObservers: ArrayList<Observer<in T>>? = null
    private var viewModelTag: WeakReference<BaseViewModel>? = null
    private var fragmentTag: WeakReference<Fragment>? = null
    private var activityTag: WeakReference<FragmentActivity>? = null

    private class ViewModelClearedCallback<T>(val liveData: WeakReference<AutoRemoveForeverObserverLiveData<T>>) :
        Function0<Unit> {
        override fun invoke() {
            liveData.get()?.onTagDestroy()
        }
    }

    private class OnDestroyLifecycleObserver<T>(val liveData: WeakReference<AutoRemoveForeverObserverLiveData<T>>) :
        LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                liveData.get()?.onTagDestroy()
            }
        }
    }

    open fun onTagDestroy() {
        removeForeverObservers()
    }

    fun setTag(tag: BaseViewModel): LiveData<T> {
        if (viewModelTag?.get() == null) {
            viewModelTag = WeakReference(tag)
            viewModelTag!!.get()
                ?.addOnClearedListener(ViewModelClearedCallback(WeakReference(this)))
        }
        return this
    }

    fun setTag(tag: Fragment): LiveData<T> {
        if (fragmentTag?.get() == null) {
            fragmentTag = WeakReference(tag)
            fragmentTag!!.get()!!.viewLifecycleOwner.lifecycle.addObserver(
                OnDestroyLifecycleObserver(WeakReference(this))
            )
        }
        return this
    }

    fun setTag(tag: FragmentActivity): LiveData<T> {
        if (activityTag?.get() == null) {
            activityTag = WeakReference(tag)
            activityTag!!.get()?.lifecycle?.addObserver(
                OnDestroyLifecycleObserver(
                    WeakReference(
                        this
                    )
                )
            )
        }
        return this
    }

    override fun observeForever(observer: Observer<in T>) {
        super.observeForever(observer)
        if (viewModelTag?.get() != null || fragmentTag?.get() != null || activityTag?.get() != null) {
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