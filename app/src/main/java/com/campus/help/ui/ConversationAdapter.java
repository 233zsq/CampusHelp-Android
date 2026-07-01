package com.campus.help.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.campus.help.core.base.BaseAdapter;
import com.campus.help.core.utils.ConversationId;
import com.campus.help.core.utils.TimeUtils;
import com.campus.help.data.model.ChatMessage;
import com.campus.help.databinding.ItemConversationBinding;

/**
 * 会话列表适配器：每条 item 是某会话的「最新一条消息」。
 * 对方 userId 从 conversationId 反推；未读小红点用最新消息的 read 字段近似。
 */
public class ConversationAdapter extends BaseAdapter<ChatMessage, ItemConversationBinding> {

    private final long me;

    public ConversationAdapter(long me) {
        this.me = me;
    }

    @Override
    protected ItemConversationBinding createBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemConversationBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void bind(@NonNull ItemConversationBinding b, @NonNull ChatMessage item, int position) {
        long peer = ConversationId.peerOf(item.conversationId, me);
        if (peer == 0) {
            peer = item.senderId == me ? item.receiverId : item.senderId;
        }
        b.convName.setText("用户 " + peer);
        b.convLast.setText(item.content == null ? "" : item.content);
        b.convTime.setText(TimeUtils.relative(item.timestamp));

        boolean unread = !item.read && item.receiverId == me;
        b.convUnread.setVisibility(unread ? android.view.View.VISIBLE : android.view.View.GONE);
        if (unread) {
            b.convUnread.setText("•");
        }
    }
}
