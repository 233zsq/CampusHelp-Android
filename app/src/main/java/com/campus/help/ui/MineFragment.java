package com.campus.help.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.campus.help.core.base.BaseFragment;
import com.campus.help.core.network.OkHttpProvider;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.data.model.User;
import com.campus.help.data.repo.UserRepository;
import com.campus.help.databinding.FragmentMineBinding;
import com.campus.help.ui.login.LoginActivity;

/**
 * 我的 / 个人中心（成员 D）。展示信用分仪表盘 + 退出登录。
 * 信用分单一真源：GET /api/users/{id} 的 user.creditScore（成员 A）。
 */
public class MineFragment extends BaseFragment<FragmentMineBinding> {

    private UserRepository userRepository;

    @Override
    protected FragmentMineBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentMineBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        // 信用分加载完成前先隐藏仪表盘，避免闪现 0 分
        binding.creditGauge.setVisibility(View.INVISIBLE);

        binding.btnLogout.setOnClickListener(v -> {
            TokenManager.clear(requireContext());
            OkHttpProvider.setToken(null);
            startActivity(new Intent(getContext(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    @Override
    protected void initData() {
        userRepository = new UserRepository(requireContext());
        long uid = TokenManager.getUserId(requireContext());
        LiveData<User> user = userRepository.observeUser(uid);
        user.observe(getViewLifecycleOwner(), u -> {
            if (binding == null || u == null) {
                return;
            }
            binding.creditGauge.setScore(u.creditScore);
            binding.creditGauge.setVisibility(View.VISIBLE);
        });
    }
}
