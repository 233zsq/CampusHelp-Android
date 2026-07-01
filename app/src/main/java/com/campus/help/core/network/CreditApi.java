package com.campus.help.core.network;

import com.campus.help.data.model.CreditRecord;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 信用分 API（对应后端 CreditController）。通过 RetrofitClient.create(CreditApi.class) 获取。
 *
 * <p>单一真源约定：信用分<strong>展示</strong>用 {@link UserApi#getById} 的 user.creditScore
 * （后端已与 credit_record 同步并 clamp 0~1000）；本接口只负责变动明细（listByUser）与加减（addRecord）。
 * 注意：addRecord 的 userId 会被后端强制覆盖为登录用户，前端无法替他人刷分。
 */
public interface CreditApi {

    /** 信用分变动明细。GET /api/credits?userId= ，按 timestamp 倒序。 */
    @GET("api/credits")
    Call<ApiResponse<List<CreditRecord>>> listByUser(@Query("userId") long userId);

    /** 添加信用分变动。POST /api/credits，body={delta,reason,...}，userId 由后端强制取登录用户。 */
    @POST("api/credits")
    Call<ApiResponse<Void>> addRecord(@Body CreditRecord record);
}