package com.campus.help.core.bus;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campus.help.data.model.ChatMessage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * IM 消息总线（单例 LiveData）。
 * 替代已废弃的 LocalBroadcastManager：WebSocketService 收到消息后 post，UI 订阅更新。
 * - getIncoming()：单条新消息事件（用于通知/刷新/聊天页追加气泡）
 * - getMessages()：累计消息列表（预留）
 *
 * <p>线程与丢值问题：{@code post} 由 WebSocketService 在后台线程调用。原实现用
 * {@link MutableLiveData#postValue} 连续投递，LiveData 的 postValue 有合并语义——
 * 两条消息极短时间到达时观察者可能只收到后者，聊天页会漏气泡。这里改为「入队 +
 * 主线程 drain」：后台线程只把消息追加到同步队列并调度一次 drain；drain 在主线程用
 * {@link MutableLiveData#setValue} 逐条派发（setValue 立即派发、不合并），从而不丢消息。
 */
public class MessageBus {

    private static volatile MessageBus instance;

    private final MutableLiveData<ChatMessage> incoming = new MutableLiveData<>();
    private final MutableLiveData<List<ChatMessage>> messages =
            new MutableLiveData<>(new ArrayList<>());

    private final Object lock = new Object();
    private final Deque<ChatMessage> pending = new ArrayDeque<>();
    private final List<ChatMessage> cumulative = new ArrayList<>();
    private boolean drainScheduled = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private MessageBus() {
    }

    public static MessageBus get() {
        if (instance == null) {
            synchronized (MessageBus.class) {
                if (instance == null) {
                    instance = new MessageBus();
                }
            }
        }
        return instance;
    }

    public LiveData<ChatMessage> getIncoming() {
        return incoming;
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    /** 由 WebSocketService 在收到消息时调用（可在任意线程）。 */
    public void post(ChatMessage message) {
        if (message == null) {
            return;
        }
        synchronized (lock) {
            pending.addLast(message);
            cumulative.add(message);
            if (!drainScheduled) {
                drainScheduled = true;
                mainHandler.post(this::drain);
            }
        }
    }

    /** 主线程执行：逐条 setValue 派发 incoming（不合并、不丢），再一次性刷新累计列表。 */
    private void drain() {
        Deque<ChatMessage> queue;
        List<ChatMessage> snapshot;
        synchronized (lock) {
            queue = new ArrayDeque<>(pending);
            pending.clear();
            snapshot = new ArrayList<>(cumulative);
            drainScheduled = false;
        }
        for (ChatMessage m : queue) {
            incoming.setValue(m);
        }
        messages.setValue(snapshot);
    }

    /** 重置累计列表（仅在主线程调用）。 */
    public void reset(List<ChatMessage> initial) {
        synchronized (lock) {
            pending.clear();
            cumulative.clear();
            if (initial != null) {
                cumulative.addAll(initial);
            }
            drainScheduled = false;
        }
        messages.setValue(initial == null ? new ArrayList<>() : new ArrayList<>(initial));
    }
}
