package com.example.rjq.myapplication.entity

//errorCode，errorMsg代表业务错误：指服务器正常返回数据(response.isSuccessful)，但是某种业务错误，比如：密码错误
data class WanResponse<out T>(val errorCode: Int, val errorMsg: String?, val data: T?)