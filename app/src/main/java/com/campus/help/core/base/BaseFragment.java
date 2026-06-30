package com.campus.help.core.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

/**
 * Fragment 基类：统一 ViewBinding 绑定（在 onCreateView 创建，onViewDestroyed 置空防泄漏）。
 */
public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {

    protected VB binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = createBinding(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected abstract VB createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    protected void initView() {
    }

    protected void initData() {
    }
}
