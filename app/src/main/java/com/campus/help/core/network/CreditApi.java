package com.campus.help.core.network;

import com.campus.help.data.model.CreditRecord;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 信用分 API（对应后端 CreditController）。
 * GET /api/credits?userId=  变动明细；GET /api/credits/sum?userId=  求和。
 */
public interface CreditApi {

    @GET("api/credits")
    Call<ApiResponse<List<CreditRecord>>> list(@Query("userId") long userId);

    @GET("api/credits/sum")
    Call<ApiResponse<Integer>> sum(@Query("userId") long userId);
}
