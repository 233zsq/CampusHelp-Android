package com.campus.help.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.campus.help.core.base.BaseFragment;
import com.campus.help.databinding.FragmentPublishBinding;

/**
 * 发布任务（成员 B）。地基阶段占位。
 */
public class PublishFragment extends BaseFragment<FragmentPublishBinding> {

    @Override
    protected FragmentPublishBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentPublishBinding.inflate(inflater, container, false);
    }
}
