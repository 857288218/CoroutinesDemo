package com.example.rjq.myapplication.entity

enum class Status {
    /**
     * When the HTTP response was successfully returned by the remote server.
     * The ResponseCode == HTTP_CODE_OK and the ResponseBodyCode == CODE_OK
     */
    SUCCESS,

    /**
     * When the HTTP response was successfully returned by the remote server.
     * The ResponseCode == HTTP_CODE_OK or the ResponseBodyCode != CODE_OK
     */
    ERROR,

    /**
     * When the request could not be executed due to cancellation, a connectivity problem,
     * read/write timeout or invalid responseBody.
     */
    FAILURE,
}

//errorCode，errorMsg代表业务错误和网络错误，具体见ApiResultCallAdapter、LiveDataJaResponseCallAdapter
data class WanResponse<out T>(var status: Status, val errorCode: Int, val errorMsg: String?, val data: T?)