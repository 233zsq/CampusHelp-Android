package com.campus.help.core.bus;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campus.help.data.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * IM 消息总线（单例 LiveData）。
 * 替代已废弃的 LocalBroadcastManager：WebSocketService 收到消息后 post，UI 订阅更新。
 * - getIncoming()：单条新消息（用于通知/刷新）
 * - getMessages()：累计消息列表（用于会话页）
 */
public class MessageBus {

    private static volatile MessageBus instance;

    private final MutableLiveData<List<ChatMessage>> messages =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ChatMessage> incoming = new MutableLiveData<>();

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

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public LiveData<ChatMessage> getIncoming() {
        return incoming;
    }

    /** 由 WebSocketService 在收到消息时调用（可在任意线程，用 postValue）。 */
    public void post(ChatMessage message) {
        incoming.postValue(message);
        List<ChatMessage> list = messages.getValue();
        if (list == null) {
            list = new ArrayList<>();
        }
        List<ChatMessage> next = new ArrayList<>(list);
        next.add(message);
        messages.postValue(next);
    }

    public void reset(List<ChatMessage> initial) {
        messages.setValue(initial == null ? new ArrayList<>() : new ArrayList<>(initial));
    }
}
