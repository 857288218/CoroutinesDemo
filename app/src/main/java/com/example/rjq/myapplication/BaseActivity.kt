package com.example.rjq.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider

open class BaseActivity : AppCompatActivity(), HasDefaultViewModelProviderFactory {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
}