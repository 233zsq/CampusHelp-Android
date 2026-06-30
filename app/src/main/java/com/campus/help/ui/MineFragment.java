package com.campus.help.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.campus.help.core.base.BaseFragment;
import com.campus.help.databinding.FragmentMineBinding;

/**
 * 我的 / 个人中心（成员 D）。地基阶段：展示信用分仪表盘自定义 View。
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
    }
}
