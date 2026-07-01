package com.campus.help.core.network;

import com.campus.help.core.network.dto.LoginRequest;
import com.campus.help.core.network.dto.LoginResponse;
import com.campus.help.core.network.dto.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 用户/认证 API（对应后端 AuthController）。
 * 通过 RetrofitClient.create(UserApi.class) 获取实现。
 */
public interface UserApi {

    /**
     * 登录。成功时 data = { token, userId, name, creditScore }。
     */
    @POST("api/auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    /**
     * 注册。成功时 data = 新用户 id。
     */
    @POST("api/auth/register")
    Call<ApiResponse<Long>> register(@Body RegisterRequest request);
}
