package com.campus.help.ui;

import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.campus.help.core.base.BaseActivity;
import com.campus.help.core.network.ApiResponse;
import com.campus.help.core.network.CreditApi;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.data.model.CreditRecord;
import com.campus.help.databinding.ActivityCreditDetailBinding;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 信用分明细页（成员 D）。展示当前用户的信用分变动记录。
 * 数据来自 GET /api/credits?userId=（{@link CreditApi#listByUser}）。空列表显示 EmptyView。
 */
public class CreditDetailActivity extends BaseActivity<ActivityCreditDetailBinding> {

    private CreditRecordAdapter adapter;
    private CreditApi creditApi;

    @Override
    protected ActivityCreditDetailBinding createBinding() {
        return ActivityCreditDetailBinding.inflate(LayoutInflater.from(this));
    }

    @Override
    protected void initView() {
        creditApi = RetrofitClient.create(CreditApi.class);
        adapter = new CreditRecordAdapter();
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        long uid = TokenManager.getUserId(this);
        if (uid <= 0) {
            showEmpty();
            return;
        }
        creditApi.listByUser(uid).enqueue(new retrofit2.Callback<ApiResponse<List<CreditRecord>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CreditRecord>>> c,
                                   Response<ApiResponse<List<CreditRecord>>> resp) {
                List<CreditRecord> records = (resp.isSuccessful() && resp.body() != null
                        && resp.body().isSuccess() && resp.body().getData() != null)
                        ? resp.body().getData() : Collections.emptyList();
                adapter.submit(records);
                toggleEmpty(records.isEmpty());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CreditRecord>>> c, Throwable t) {
                adapter.submit(Collections.emptyList());
                showEmpty();
            }
        });
    }

    private void toggleEmpty(boolean empty) {
        binding.emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            binding.emptyView.setText("暂无信用变动记录");
        }
    }

    private void showEmpty() {
        toggleEmpty(true);
    }
}
