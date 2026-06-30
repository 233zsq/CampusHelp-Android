package com.campus.help.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.campus.help.core.base.BaseFragment;
import com.campus.help.core.bus.MessageBus;
import com.campus.help.databinding.FragmentMessageBinding;

/**
 * 消息（成员 C）。地基阶段：订阅 MessageBus，验证 IM 总线就绪。
 */
public class MessageFragment extends BaseFragment<FragmentMessageBinding> {

    @Override
    protected FragmentMessageBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentMessageBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initData() {
        MessageBus.get().getMessages().observe(getViewLifecycleOwner(), list -> {
            int count = list == null ? 0 : list.size();
            binding.messageHint.setText("消息总线就绪，当前 " + count + " 条\n（成员 C 接入 WebSocket 后实时更新）");
        });
    }
}
