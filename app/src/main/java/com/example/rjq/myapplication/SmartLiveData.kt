package com.example.rjq.myapplication

import android.util.ArrayMap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

// 提供非粘性observeNoSticky,observeForeverNoSticky;
class SmartLiveData<T> : MutableLiveData<T>() {

    private var mVersion = -1
    private val sparseArray = ArrayMap<Observer<T>, CustomObserver>()

    fun observeNoSticky(owner: LifecycleOwner, observer: Observer<T>) {
        if (!sparseArray.containsKey(observer)) {
            // 如果observer被observe/observeForever添加过则先移除，否则observer.onChange每次触发都会回调两次
            super.removeObserver(observer)
            val customObserver = CustomObserver(owner, observer)
            sparseArray[observer] = customObserver
            super.observe(owner, customObserver)
        } else require(!(sparseArray[observer] != null && !sparseArray[observer]!!.isAttachedTo(owner))) {
            ("Cannot add the same observer with different lifecycles")
        }
    }

    fun observeForeverNoSticky(observer: Observer<T>) {
        if (!sparseArray.containsKey(observer)) {
            super.removeObserver(observer)
            val customObserver = CustomObserver(observer)
            sparseArray[observer] = customObserver
            super.observeForever(customObserver)
        } else require(!(sparseArray[observer] != null && !sparseArray[observer]!!.isAttachedTo(null))) {
            ("Cannot add the same observer with different lifecycles")
        }
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (sparseArray.indexOfKey(observer) >= 0) {
            // 如果observer在observeNoSticky或observeForeverNoSticky中添加则不能再使用observe/observeForever添加该observer
            // 否则observer.onChange每次触发都会回调两次
            throw IllegalArgumentException("this observer already add to no sticky")
        }
        super.observe(owner, observer)
    }

    override fun observeForever(observer: Observer<in T>) {
        if (sparseArray.indexOfKey(observer) >= 0) {
            throw IllegalArgumentException("this observer already add to no sticky")
        }
        super.observeForever(observer)
    }

    override fun removeObserver(observer: Observer<in T>) {
        if (observer is CustomObserver) {
            // 如果由LifecycleBoundObserver#onStateChanged中触发removeObserver,observer可能是CustomObserver
            super.removeObserver(observer)
            sparseArray.removeAt(sparseArray.indexOfKey(observer.mObserver))
        } else {
            // 如果主动调用removeObserver，此时observer不是CustomObserver，但可能在observeNoSticky中被封装成CustomObserver
            val removeIndex = sparseArray.indexOfKey(observer)
            if (removeIndex >= 0) {
                val removeObserver = sparseArray.valueAt(removeIndex)
                sparseArray.removeAt(removeIndex)
                super.removeObserver(removeObserver)
            } else {
                super.removeObserver(observer)
            }
        }
    }

    override fun removeObservers(owner: LifecycleOwner) {
        super.removeObservers(owner)
        for (i in 0 until sparseArray.size) {
            if (sparseArray.valueAt(i).isAttachedTo(owner)) {
                sparseArray.removeAt(i)
            }
        }
    }

    override fun setValue(value: T) {
        mVersion++
        super.setValue(value)
    }

    private inner class CustomObserver : Observer<T> {
        val mObserver: Observer<in T>
        var mOwner: LifecycleOwner? = null
        private val observerVersion = mVersion

        constructor(owner: LifecycleOwner?, observer: Observer<in T>) {
            mOwner = owner
            mObserver = observer
        }

        constructor(observer: Observer<in T>) {
            mObserver = observer
        }

        fun isAttachedTo(owner: LifecycleOwner?) = mOwner === owner

        override fun onChanged(t: T) {
            //此处做拦截操作,防止粘性(数据倒灌)
            if (mVersion > observerVersion) {
                mObserver.onChanged(t)
            }
        }
    }
}