package com.example.rjq.myapplication.entity

data class WanResponse<out T>(val errorCode: Int, val errorMsg: String?, val data: T)