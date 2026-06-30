package com.campus.help.core.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * OkHttpClient 单例（同时承担 WebSocket 客户端职责）。
 */
public final class OkHttpProvider {

    private static volatile OkHttpClient client;
    private static final TokenInterceptor TOKEN_INTERCEPTOR = new TokenInterceptor();

    private OkHttpProvider() {
    }

    public static OkHttpClient getClient() {
        if (client == null) {
            synchronized (OkHttpProvider.class) {
                if (client == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    client = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .pingInterval(30, TimeUnit.SECONDS) // WebSocket 心跳
                            .addInterceptor(TOKEN_INTERCEPTOR)
                            .addInterceptor(logging)
                            .build();
                }
            }
        }
        return client;
    }

    public static void setToken(String token) {
        TOKEN_INTERCEPTOR.setToken(token);
    }
}
