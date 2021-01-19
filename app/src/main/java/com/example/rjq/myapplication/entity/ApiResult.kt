package com.example.rjq.myapplication.entity

sealed class ApiResult<out T> {
    //服务器正常返回数据response.isSuccessful，但是可能有业务错误,业务错误信息应该是服务端返回，在具体的数据类型bean中
    data class Success<out T>(val data: T?) : ApiResult<T>()

    //http status错误；网络请求中发生了异常(onFailure回调)
    data class Failure(val errorCode: Int, val errorMsg: String) : ApiResult<Nothing>()
}