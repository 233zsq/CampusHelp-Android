package com.campus.help.ui.chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.campus.help.core.utils.TimeUtils;
import com.campus.help.data.model.ChatMessage;
import com.campus.help.databinding.ItemMessageReceivedBinding;
import com.campus.help.databinding.ItemMessageSentBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天消息适配器：双视图类型（自己/对方气泡），按 id 去重追加。
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 0;

    private final long currentUserId;
    private final List<ChatMessage> items = new ArrayList<>();

    public ChatAdapter(long currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).senderId == currentUserId ? TYPE_SENT : TYPE_RECEIVED;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SENT) {
            return new SentHolder(ItemMessageSentBinding.inflate(inflater, parent, false));
        }
        return new ReceivedHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = items.get(position);
        String time = TimeUtils.formatTime(m.timestamp);
        if (holder instanceof SentHolder) {
            SentHolder h = (SentHolder) holder;
            h.binding.messageContent.setText(m.content);
            h.binding.messageTime.setText(time);
        } else if (holder instanceof ReceivedHolder) {
            ReceivedHolder h = (ReceivedHolder) holder;
            h.binding.messageContent.setText(m.content);
            h.binding.messageTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** 整体替换（历史拉取后）。 */
    public void submit(List<ChatMessage> list) {
        List<ChatMessage> next = list == null ? new ArrayList<>() : new ArrayList<>(list);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return next.size();
            }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                return items.get(o).id == next.get(n).id;
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                ChatMessage a = items.get(o);
                ChatMessage b = next.get(n);
                return a.id == b.id
                        && (a.content == null ? b.content == null : a.content.equals(b.content))
                        && a.timestamp == b.timestamp;
            }
        });
        items.clear();
        items.addAll(next);
        diff.dispatchUpdatesTo(this);
    }

    /** 追加单条（按 id 去重），返回是否新增。 */
    public boolean addIfAbsent(ChatMessage m) {
        for (ChatMessage existing : items) {
            if (existing.id == m.id) {
                return false;
            }
        }
        items.add(m);
        notifyItemInserted(items.size() - 1);
        return true;
    }

    static class SentHolder extends RecyclerView.ViewHolder {
        final ItemMessageSentBinding binding;

        SentHolder(ItemMessageSentBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    static class ReceivedHolder extends RecyclerView.ViewHolder {
        final ItemMessageReceivedBinding binding;

        ReceivedHolder(ItemMessageReceivedBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
