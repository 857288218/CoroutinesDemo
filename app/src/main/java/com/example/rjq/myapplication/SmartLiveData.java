package com.example.rjq.myapplication;

import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class SmartLiveData<T> extends MutableLiveData<T> {

    private int mVersion = -1;

    private ArrayMap<Observer<T>, CustomObserver<T>> sparseArray = new ArrayMap<>();

    public void observe(@NonNull LifecycleOwner owner, boolean isSticky, @NonNull Observer<T> observer) {
        if (isSticky) {
            super.observe(owner, observer);
        } else {
            if (!sparseArray.containsKey(observer)) {
                CustomObserver<T> customObserver = new CustomObserver<>(observer);
                sparseArray.put(observer, customObserver);
                super.observe(owner, customObserver);
            }
        }
    }

    public Observer<T> observeForever(@NonNull Observer<T> observer, boolean isSticky) {
        if (isSticky) {
            super.observeForever(observer);
            return observer;
        } else {
            if (!sparseArray.containsKey(observer)) {
                CustomObserver<T> customObserver = new CustomObserver<>(observer);
                sparseArray.put(observer, customObserver);
                super.observeForever(customObserver);
                return customObserver;
            } else {
                return sparseArray.get(observer);
            }
        }
    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        if (observer instanceof CustomObserver) {
            super.removeObserver(observer);
            for (int i = 0; i < sparseArray.size(); i++) {
                if (observer == sparseArray.valueAt(i)) {
                    sparseArray.remove(sparseArray.keyAt(i));
                    break;
                }
            }
        } else {
            CustomObserver<T> removeObserver = sparseArray.remove(observer);
            if (removeObserver != null) {
                super.removeObserver(removeObserver);
            } else {
                super.removeObserver(observer);
            }
        }
    }

    @Override
    public void removeObservers(@NonNull LifecycleOwner owner) {
        super.removeObservers(owner);
        sparseArray.clear();
    }

    @Override
    public void setValue(T value) {
        mVersion++;
        super.setValue(value);
    }

    class CustomObserver<T> implements Observer<T> {
        private final Observer<? super T> mObserver;
        private final int observerVersion = mVersion;

        public CustomObserver(Observer<? super T> observer) {
            mObserver = observer;
        }

        @Override
        public void onChanged(T t) {
            //此处做拦截操作
            if (mVersion > observerVersion) {
                mObserver.onChanged(t);
            }
        }
    }
}