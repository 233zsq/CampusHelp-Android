package com.campus.help.core.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 为所有请求附加鉴权头（成员 A 登录成功后调用 setToken 写入）。
 */
public class TokenInterceptor implements Interceptor {

    private volatile String token;

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        String t = token;
        if (t != null && !t.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + t);
        }
        return chain.proceed(builder.build());
    }
}
