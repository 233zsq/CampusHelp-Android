package com.campus.help.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campus.help.core.base.BaseFragment;
import com.campus.help.core.bus.MessageBus;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.data.model.ChatMessage;
import com.campus.help.data.repo.MessageRepository;
import com.campus.help.databinding.FragmentMessageBinding;
import com.campus.help.ui.chat.ChatActivity;

import java.util.List;

/**
 * 消息 Tab：会话列表（每个会话最新一条消息），点击进入 {@link ChatActivity}。
 * 下拉刷新调 {@link MessageRepository#refreshConversations}；新消息经 MessageBus 触发刷新。
 */
public class MessageFragment extends BaseFragment<FragmentMessageBinding> {

    private ConversationAdapter adapter;
    private MessageRepository repo;

    @Override
    protected FragmentMessageBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentMessageBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        long me = TokenManager.getUserId(requireContext());
        adapter = new ConversationAdapter(me);
        binding.conversationList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.conversationList.setAdapter(adapter);

        adapter.setOnItemClickListener((item, position) -> {
            long peer = item.senderId == me ? item.receiverId : item.senderId;
            startActivity(ChatActivity.intent(requireContext(), item.conversationId, peer));
        });

        binding.refresh.setOnRefreshListener(this::refresh);
    }

    @Override
    protected void initData() {
        repo = MessageRepository.getInstance(requireContext());
        repo.observeConversations().observe(getViewLifecycleOwner(), this::bindConversations);
        MessageBus.get().getIncoming().observe(getViewLifecycleOwner(), m -> repo.refreshConversations());
        refresh();
    }

    private void refresh() {
        binding.refresh.setRefreshing(true);
        repo.refreshConversations();
        binding.refresh.postDelayed(() -> binding.refresh.setRefreshing(false), 600);
    }

    private void bindConversations(List<ChatMessage> list) {
        adapter.submit(list);
        binding.empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
