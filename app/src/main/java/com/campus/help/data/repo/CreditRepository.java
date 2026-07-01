package com.campus.help.data.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campus.help.core.base.BaseRepository;
import com.campus.help.core.base.Callback;
import com.campus.help.core.network.ApiResponse;
import com.campus.help.core.network.CreditApi;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.data.model.CreditRecord;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 信用分数据源（远端）。通过 Retrofit 调后端 CreditController。
 *
 * <p>信用分展示请用 {@link UserRepository#observeUser} 取 user.creditScore（后端已与变动同步并 clamp 0~1000）；
 * 本仓库只负责变动明细（{@link #observeByUser}）与加减（{@link #addRecord}）。
 * 各 observe* 方法返回 LiveData，内部异步请求，成功后 postValue。
 */
public class CreditRepository extends BaseRepository {

    private final CreditApi api;

    public CreditRepository() {
        api = RetrofitClient.create(CreditApi.class);
    }

    /** 信用分变动明细，成功 post 列表，失败 post 空列表。 */
    public LiveData<List<CreditRecord>> observeByUser(long userId) {
        MutableLiveData<List<CreditRecord>> live = new MutableLiveData<>(Collections.emptyList());
        api.listByUser(userId).enqueue(new retrofit2.Callback<ApiResponse<List<CreditRecord>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CreditRecord>>> c,
                                   Response<ApiResponse<List<CreditRecord>>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    List<CreditRecord> list = resp.body().getData();
                    live.postValue(list != null ? list : Collections.emptyList());
                } else {
                    live.postValue(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CreditRecord>>> c, Throwable t) {
                live.postValue(Collections.emptyList());
            }
        });
        return live;
    }

    /**
     * 添加信用分变动（delta/reason）。timestamp 为 0 时由本地填充，userId 由后端强制覆盖为登录用户。
     *
     * @param cb 成功 onResult(true)，失败 onResult(false)。
     */
    public void addRecord(CreditRecord record, Callback<Boolean> cb) {
        if (record.timestamp == 0) {
            record.timestamp = System.currentTimeMillis();
        }
        api.addRecord(record).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> resp) {
                if (cb != null) {
                    cb.onResult(resp.isSuccessful() && resp.body() != null && resp.body().isSuccess());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                if (cb != null) {
                    cb.onResult(false);
                }
            }
        });
    }
}
