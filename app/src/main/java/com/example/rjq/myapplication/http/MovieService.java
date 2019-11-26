package com.example.rjq.myapplication.http;

import com.example.rjq.myapplication.entity.HttpResult;
import com.example.rjq.myapplication.entity.Subject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by liukun on 16/3/9.
 */
public interface MovieService {

    @GET("top250")
    Call<HttpResult<List<Subject>>> getTopMovie(@Query("start") int start, @Query("count") int count);
}
