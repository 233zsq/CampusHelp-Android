package com.campus.help.core.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

/**
 * Activity 基类：统一 ViewBinding 绑定与 init 生命周期。
 * 子类实现 {@link #createBinding()}、按需重写 {@link #initView()} / {@link #initData()}。
 */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    protected VB binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = createBinding();
        setContentView(binding.getRoot());
        initView();
        initData();
    }

    protected abstract VB createBinding();

    protected void initView() {
    }

    protected void initData() {
    }
}
