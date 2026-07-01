package com.campus.help.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campus.help.R;
import com.campus.help.core.bus.MessageBus;
import com.campus.help.core.utils.ConversationId;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.data.model.ChatMessage;
import com.campus.help.data.repo.MessageRepository;
import com.campus.help.databinding.ActivityChatBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 聊天页：气泡 RecyclerView，发送走 WS（REST 兜底），进页面拉历史 + 标记已读。
 * 通过静态 {@link #FOREGROUND} 集合告知 {@link com.campus.help.feature.im.WebSocketService}
 * 当前正在查看的会话——在该会话页内收到的消息不弹通知。
 */
public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_PEER_ID = "extra_peer_id";

    /** 当前处于前台的会话 ID集合（供 WebSocketService 判定是否发通知）。 */
    private static final Set<String> FOREGROUND = new HashSet<>();

    public static boolean isForegroundFor(String conversationId) {
        synchronized (FOREGROUND) {
            return conversationId != null && FOREGROUND.contains(conversationId);
        }
    }

    public static Intent intent(@NonNull Context ctx, String conversationId, long peerId) {
        return new Intent(ctx, ChatActivity.class)
                .putExtra(EXTRA_CONVERSATION_ID, conversationId)
                .putExtra(EXTRA_PEER_ID, peerId);
    }

    private ActivityChatBinding binding;
    private ChatAdapter adapter;
    private MessageRepository repo;
    private String conversationId;
    private long peerId;
    private long me;
    private int seqCounter = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        me = TokenManager.getUserId(this);
        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        long extraPeer = getIntent().getLongExtra(EXTRA_PEER_ID, 0L);
        if (conversationId == null) {
            if (extraPeer == 0L) {
                finish();
                return;
            }
            conversationId = ConversationId.of(me, extraPeer);
        }
        peerId = extraPeer != 0L ? extraPeer : ConversationId.peerOf(conversationId, me);
        repo = MessageRepository.getInstance(this);

        binding.toolbar.setTitle("用户 " + peerId);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new ChatAdapter(me);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        binding.messageList.setLayoutManager(lm);
        binding.messageList.setAdapter(adapter);

        // 历史（Room 缓存 + REST 刷新）
        repo.observeHistory(conversationId).observe(this, this::bindMessages);
        repo.markRead(conversationId);

        // 实时入消息
        MessageBus.get().getIncoming().observe(this, msg -> {
            if (msg == null || !conversationId.equals(msg.conversationId)) {
                return;
            }
            if (adapter.addIfAbsent(msg)) {
                binding.messageList.scrollToPosition(adapter.getItemCount() - 1);
            }
        });

        binding.btnSend.setOnClickListener(v -> send());
    }

    private void bindMessages(List<ChatMessage> list) {
        adapter.submit(list);
        if (adapter.getItemCount() > 0) {
            binding.messageList.scrollToPosition(adapter.getItemCount() - 1);
        }
        binding.empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void send() {
        String text = binding.editInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        int seq = ++seqCounter;
        ChatMessage msg = new ChatMessage();
        msg.id = -seq; // 临时 id，ACK 到来后由 applyAck 替换
        msg.conversationId = conversationId;
        msg.senderId = me;
        msg.receiverId = peerId;
        msg.content = text;
        msg.type = 0;
        msg.timestamp = System.currentTimeMillis();
        msg.read = true;

        repo.insertOptimistic(msg);
        repo.send(msg, seq, ok -> {
            if (!ok) {
                Snackbar.make(binding.getRoot(), R.string.chat_send_failed, Snackbar.LENGTH_SHORT).show();
            }
        });
        binding.editInput.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        synchronized (FOREGROUND) {
            FOREGROUND.add(conversationId);
        }
        repo.markRead(conversationId);
        repo.refreshUnread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        synchronized (FOREGROUND) {
            FOREGROUND.remove(conversationId);
        }
    }
}
