package com.example.rjq.myapplication

import android.util.ArrayMap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class SmartLiveData<T> : MutableLiveData<T>() {

    private var mVersion = -1
    private val sparseArray = ArrayMap<Observer<T>, CustomObserver>()

    fun observeNoSticky(owner: LifecycleOwner, observer: Observer<T>) {
        if (!sparseArray.containsKey(observer)) {
            val customObserver = CustomObserver(owner, observer)
            sparseArray[observer] = customObserver
            super.observe(owner, customObserver)
        } else require(!(sparseArray[observer] != null && !sparseArray[observer]!!.isAttachedTo(owner))) {
            ("Cannot add the same observer with different lifecycles")
        }
    }

    fun observeForeverNoSticky(observer: Observer<T>) {
        if (!sparseArray.containsKey(observer)) {
            val customObserver = CustomObserver(observer)
            sparseArray[observer] = customObserver
            super.observeForever(customObserver)
        } else {
            require(!(sparseArray[observer] != null && !sparseArray[observer]!!.isAttachedTo(null))) {
                ("Cannot add the same observer with different lifecycles")
            }
        }
    }

    override fun removeObserver(observer: Observer<in T>) {
        val removeObserver = sparseArray.valueAt(sparseArray.indexOfKey(observer))
        if (removeObserver != null) {
            super.removeObserver(removeObserver)
        } else {
            super.removeObserver(observer)
        }
    }

    override fun removeObservers(owner: LifecycleOwner) {
        super.removeObservers(owner)
        for (i in 0 until sparseArray.size) {
            if (owner === sparseArray.valueAt(i)?.mOwner) {
                sparseArray.removeAt(i)
            }
        }
    }

    override fun setValue(value: T) {
        mVersion++
        super.setValue(value)
    }

    private inner class CustomObserver : Observer<T> {
        private val mObserver: Observer<in T>
        var mOwner: LifecycleOwner? = null
        private val observerVersion = mVersion

        constructor(owner: LifecycleOwner?, observer: Observer<in T>) {
            mOwner = owner
            mObserver = observer
        }

        constructor(observer: Observer<in T>) {
            mObserver = observer
        }

        fun isAttachedTo(owner: LifecycleOwner?): Boolean {
            return mOwner === owner
        }

        override fun onChanged(t: T) {
            //此处做拦截操作,防止粘性
            if (mVersion > observerVersion) {
                mObserver.onChanged(t)
            }
        }
    }
}