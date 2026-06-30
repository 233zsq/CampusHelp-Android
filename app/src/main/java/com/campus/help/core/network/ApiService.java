package com.campus.help.core.network;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * 占位接口。各 feature 建议在自己包内定义 XxxApi（如 UserApi、TaskApi），
 * 通过 RetrofitClient.create(XxxApi.class) 获取实现。
 */
public interface ApiService {

    @GET("health")
    Call<ApiResponse<Object>> health();
}
