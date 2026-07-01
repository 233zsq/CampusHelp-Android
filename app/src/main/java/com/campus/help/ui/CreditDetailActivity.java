package com.campus.help.ui;

import android.view.LayoutInflater;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.campus.help.core.base.BaseActivity;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.data.repo.UserManager;
import com.campus.help.databinding.ActivityCreditDetailBinding;

/**
 * 信用分明细页（成员 D）。展示当前用户的信用分变动记录。
 * 数据来自 GET /api/credits?userId=。空列表显示 EmptyView。
 */
public class CreditDetailActivity extends BaseActivity<ActivityCreditDetailBinding> {

    private CreditRecordAdapter adapter;

    @Override
    protected ActivityCreditDetailBinding createBinding() {
        return ActivityCreditDetailBinding.inflate(LayoutInflater.from(this));
    }

    @Override
    protected void initView() {
        adapter = new CreditRecordAdapter();
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        long uid = TokenManager.getUserId(this);
        if (uid <= 0) {
            return;
        }
        UserManager.get().listCredits(uid).observe(this, records -> {
            adapter.submit(records);
            boolean empty = records == null || records.isEmpty();
            binding.emptyView.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.recycler.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
            if (empty) {
                binding.emptyView.setText("暂无信用变动记录");
            }
        });
    }
}
