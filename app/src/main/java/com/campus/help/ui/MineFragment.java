package com.campus.help.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.campus.help.core.base.BaseFragment;
import com.campus.help.core.network.OkHttpProvider;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.databinding.FragmentMineBinding;
import com.campus.help.ui.login.LoginActivity;

/**
 * 我的 / 个人中心（成员 D）。展示信用分仪表盘 + 退出登录。
 */
public class MineFragment extends BaseFragment<FragmentMineBinding> {

    @Override
    protected FragmentMineBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentMineBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        // 演示：成员 A 接入真实信用分后替换
        binding.creditGauge.setScore(820);

        binding.btnLogout.setOnClickListener(v -> {
            TokenManager.clear(requireContext());
            OkHttpProvider.setToken(null);
            startActivity(new Intent(getContext(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
}
