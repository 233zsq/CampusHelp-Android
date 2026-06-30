package com.campus.help.core.network;

import com.campus.help.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 单例。baseUrl 来自 BuildConfig.API_BASE_URL。
 */
public final class RetrofitClient {

    private static volatile Retrofit retrofit;

    private RetrofitClient() {
    }

    public static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.API_BASE_URL)
                            .client(OkHttpProvider.getClient())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static <T> T create(Class<T> service) {
        return getInstance().create(service);
    }
}
