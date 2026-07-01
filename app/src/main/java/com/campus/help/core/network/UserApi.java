package com.campus.help.core.network;

import com.campus.help.core.network.dto.LoginRequest;
import com.campus.help.core.network.dto.LoginResponse;
import com.campus.help.core.network.dto.RegisterRequest;
import com.campus.help.data.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * 用户/认证 API（对应后端 AuthController + UserController）。
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

    /**
     * 获取用户信息（含 creditScore）。GET /api/users/{id}
     * 信用分展示的单一真源：后端已把 user.creditScore 与 credit_record 同步并 clamp 0~1000。
     */
    @GET("api/users/{id}")
    Call<ApiResponse<User>> getById(@Path("id") long id);
}
