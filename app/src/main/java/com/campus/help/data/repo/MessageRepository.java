package com.campus.help.data.repo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campus.help.core.base.BaseRepository;
import com.campus.help.core.base.Callback;
import com.campus.help.core.db.AppDatabase;
import com.campus.help.core.network.ApiResponse;
import com.campus.help.core.network.MessageApi;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.core.network.dto.UnreadCountResponse;
import com.campus.help.core.utils.AppExecutors;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.data.dao.MessageDao;
import com.campus.help.data.model.ChatMessage;
import com.campus.help.feature.im.WebSocketService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 消息数据源（单例）：历史走 REST，实时走 WebSocket，Room 降为聊天记录缓存。
 * <p>
 * 由 {@link WebSocketService} 收到 WS 消息时调 {@link #cacheIncoming} / {@link #applyAck}，
 * 由 {@code ChatActivity} / {@code MessageFragment} 订阅 LiveData。
 */
public class MessageRepository extends BaseRepository {

    private static volatile MessageRepository instance;

    private static final long ACK_TIMEOUT_MS = 8000L; // ACK 超时：8s 未回则判失败

    private final MessageApi api;
    private final MessageDao dao;
    private final Context app;
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** seq → 待 ACK 的发送任务。ACK 到来 / 超时 / 断线 时移除。 */
    private final Map<Integer, SendPending> pending = new ConcurrentHashMap<>();

    private final MutableLiveData<List<ChatMessage>> conversations =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Long> unreadTotal = new MutableLiveData<>(0L);

    private MessageRepository(Context context) {
        app = context.getApplicationContext();
        api = RetrofitClient.create(MessageApi.class);
        dao = AppDatabase.getInstance(app).messageDao();
    }

    public static MessageRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (MessageRepository.class) {
                if (instance == null) {
                    instance = new MessageRepository(context);
                }
            }
        }
        return instance;
    }

    // ---------- 历史 ----------

    /** 观察某会话的消息列表（Room 缓存），同时异步从 REST 拉取并刷新缓存。 */
    public LiveData<List<ChatMessage>> observeHistory(String conversationId) {
        refreshHistory(conversationId);
        return dao.observeByConversation(conversationId);
    }

    public void refreshHistory(String conversationId) {
        api.history(conversationId).enqueue(new retrofit2.Callback<ApiResponse<List<ChatMessage>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChatMessage>>> c,
                                   Response<ApiResponse<List<ChatMessage>>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    List<ChatMessage> data = resp.body().getData();
                    AppExecutors.get().diskIO().execute(() -> {
                        dao.deleteByConversation(conversationId);
                        if (data != null) {
                            for (ChatMessage m : data) {
                                dao.insert(m);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChatMessage>>> c, Throwable t) {
                // 离线则只显示缓存
            }
        });
    }

    // ---------- 会话列表 ----------

    public LiveData<List<ChatMessage>> observeConversations() {
        return conversations;
    }

    public void refreshConversations() {
        api.conversations().enqueue(new retrofit2.Callback<ApiResponse<List<ChatMessage>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChatMessage>>> c,
                                   Response<ApiResponse<List<ChatMessage>>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    List<ChatMessage> data = resp.body().getData();
                    conversations.postValue(data != null ? data : Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChatMessage>>> c, Throwable t) {
            }
        });
    }

    // ---------- 未读 ----------

    public LiveData<Long> observeUnreadTotal() {
        return unreadTotal;
    }

    public void refreshUnread() {
        api.unreadCount().enqueue(new retrofit2.Callback<ApiResponse<UnreadCountResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UnreadCountResponse>> c,
                                   Response<ApiResponse<UnreadCountResponse>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()
                        && resp.body().getData() != null) {
                    unreadTotal.postValue(resp.body().getData().total);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UnreadCountResponse>> c, Throwable t) {
            }
        });
    }

    // ---------- WS 实时入消息 ----------

    /** 接收方收到 WS 推送的新消息：按 id 去重后写 Room 缓存。 */
    public void cacheIncoming(ChatMessage msg) {
        if (msg == null || msg.id <= 0) {
            return;
        }
        AppExecutors.get().diskIO().execute(() -> {
            if (dao.findById(msg.id) == null) {
                dao.insert(msg);
            }
        });
    }

    /** 发送方收到 ACK：用真实 id 替换乐观插入的临时消息（id = -seq），并回调发送成功。 */
    public void applyAck(int seq, ChatMessage canonical) {
        if (canonical == null || canonical.id <= 0) {
            return;
        }
        SendPending p = pending.remove(seq);
        if (p != null) {
            mainHandler.removeCallbacks(p.timeout);
        }
        AppExecutors.get().diskIO().execute(() -> {
            dao.deleteById(-seq);
            if (dao.findById(canonical.id) == null) {
                dao.insert(canonical);
            }
        });
        if (p != null && p.cb != null) {
            AppExecutors.get().main(() -> p.cb.onResult(true));
        }
    }

    /** 乐观插入临时消息（id 为负，待 ACK 替换）。驱动 Room LiveData 即时显示气泡。 */
    public void insertOptimistic(ChatMessage msg) {
        if (msg == null) {
            return;
        }
        AppExecutors.get().diskIO().execute(() -> dao.insert(msg));
    }

    // ---------- 发送 ----------

    /**
     * 发送消息：优先走 WS（{@code {seq, to, content, type}}），失败回退 REST。
     * 调用方应先乐观插入临时消息（id = -seq）显示气泡，ACK 到来后由 {@link #applyAck} 替换。
     * <p>
     * WS 路径下 <b>不</b>立即回调成功——{@code ws.send} 只是把帧放进 OkHttp 发送队列，
     * 真正发出 / ACK 都是异步的。这里登记 seq 为 pending 并起 {@link #ACK_TIMEOUT_MS}
     * 超时；ACK 到来触发 {@link #applyAck} 回调 true，超时或 {@link #onWsDisconnected}
     * 触发回调 false 并删除临时消息，避免临时消息（负 id）永久挂在 Room 里。
     *
     * @param cb 成功回调：true 表示服务端已落库（ACK 到达或 REST 成功），false 表示发送失败。
     */
    public void send(ChatMessage msg, int seq, Callback<Boolean> cb) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("seq", seq);
        payload.put("to", msg.receiverId);
        payload.put("content", msg.content == null ? "" : msg.content);
        payload.put("type", msg.type);
        String json = gson.toJson(payload);

        WebSocketService svc = WebSocketService.getInstance();
        if (svc != null && svc.sendJson(json)) {
            // 已送入 WS 发送队列：等 ACK，超时/断线判失败
            registerPending(seq, cb);
            return;
        }
        // REST 兜底
        api.send(msg).enqueue(new retrofit2.Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> c, Response<ApiResponse<Long>> resp) {
                Long id = (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess())
                        ? resp.body().getData() : null;
                boolean ok = id != null;
                if (ok) {
                    // 用真实 id 替换临时消息
                    AppExecutors.get().diskIO().execute(() -> {
                        dao.deleteById(-seq);
                        msg.id = id;
                        if (dao.findById(id) == null) {
                            dao.insert(msg);
                        }
                    });
                }
                if (cb != null) {
                    AppExecutors.get().main(() -> cb.onResult(ok));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> c, Throwable t) {
                if (cb != null) {
                    AppExecutors.get().main(() -> cb.onResult(false));
                }
            }
        });
    }

    // ---------- WS 发送态追踪 ----------

    /** 登记一个待 ACK 的 seq，超时未回则判失败。 */
    private void registerPending(int seq, Callback<Boolean> cb) {
        Runnable timeout = () -> failPending(seq);
        SendPending p = new SendPending(seq, cb, timeout);
        pending.put(seq, p);
        mainHandler.postDelayed(timeout, ACK_TIMEOUT_MS);
    }

    /** 单个 seq 判失败：删临时消息 + 回调 false。幂等。 */
    private void failPending(int seq) {
        SendPending p = pending.remove(seq);
        if (p == null) {
            return;
        }
        mainHandler.removeCallbacks(p.timeout);
        AppExecutors.get().diskIO().execute(() -> dao.deleteById(-seq));
        if (p.cb != null) {
            AppExecutors.get().main(() -> p.cb.onResult(false));
        }
    }

    /**
     * WS 断线时由 {@link WebSocketService} 调用：把所有未收到 ACK 的发送判失败，
     * 删除对应临时消息并回调 false。避免连接已断却仍等 ACK、临时消息永久挂着。
     */
    public void onWsDisconnected() {
        for (SendPending p : pending.values()) {
            if (pending.remove(p.seq) == null) {
                continue; // 已被 applyAck / failPending 取走
            }
            mainHandler.removeCallbacks(p.timeout);
            AppExecutors.get().diskIO().execute(() -> dao.deleteById(-p.seq));
            if (p.cb != null) {
                AppExecutors.get().main(() -> p.cb.onResult(false));
            }
        }
    }

    private static final class SendPending {
        final int seq;
        final Callback<Boolean> cb;
        final Runnable timeout;

        SendPending(int seq, Callback<Boolean> cb, Runnable timeout) {
            this.seq = seq;
            this.cb = cb;
            this.timeout = timeout;
        }
    }

    // ---------- 已读 ----------

    public void markRead(String conversationId) {
        long me = TokenManager.getUserId(app);
        AppExecutors.get().diskIO().execute(() -> dao.markRead(conversationId, me));
        api.markRead(conversationId).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> c, Response<ApiResponse<Void>> resp) {
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> c, Throwable t) {
            }
        });
    }

    /** 本地统计某会话未读数（会话列表小红点）。 */
    public int unreadIn(String conversationId, long me) {
        return dao.unreadIn(conversationId, me);
    }
}
