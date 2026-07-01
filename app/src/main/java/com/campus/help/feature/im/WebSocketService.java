package com.campus.help.feature.im;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.campus.help.BuildConfig;
import com.campus.help.R;
import com.campus.help.core.bus.MessageBus;
import com.campus.help.core.utils.NotificationHelper;
import com.campus.help.core.utils.PermissionUtils;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.data.model.ChatMessage;
import com.campus.help.data.repo.MessageRepository;
import com.campus.help.ui.MainActivity;
import com.campus.help.ui.chat.ChatActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * IM WebSocket 前台保活 Service：真连接 + 断线指数退避重连 + 消息分发 + 通知。
 * <p>
 * 收消息路径：onMessage → 解析 → {@link MessageRepository#cacheIncoming} +
 * {@link MessageBus#post} + （不在该会话页时）发 {@link NotificationHelper#CHANNEL_IM} 通知。
 * 发消息由 {@link MessageRepository#send} 通过 {@link #sendJson} 走 WS，本类不主动发。
 */
public class WebSocketService extends Service {

    private static final int NOTIFICATION_ID = 1001;
    private static final int MAX_RECONNECT_DELAY = 30; // 秒，指数退避封顶

    private static volatile WebSocketService INSTANCE;

    private final Gson gson = new Gson();
    private final Handler handler = new Handler(Looper.getMainLooper());

    /** 单一重连 Runnable 成员，便于去重 / 取消，避免 onClosed+onFailure 叠加排两个 timer。 */
    private final Runnable reconnectRunnable = this::connect;

    private volatile WebSocket webSocket;
    private volatile boolean shouldReconnect;
    private boolean reconnectScheduled; // 仅在主线程访问，替代 Handler.hasCallbacks（需 API 29）
    private int reconnectDelay = 1;     // 仅在主线程访问

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        startForegroundCompat(NOTIFICATION_ID, buildServiceNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (TokenManager.isLogged(this) && webSocket == null) {
            connect();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        shouldReconnect = false;
        if (webSocket != null) {
            webSocket.close(1000, "bye");
            webSocket = null;
        }
        INSTANCE = null;
        super.onDestroy();
    }

    public static WebSocketService getInstance() {
        return INSTANCE;
    }

    /** 由 MessageRepository.send 调用：通过当前 WS 发送 JSON 负载。返回是否成功送出。 */
    public boolean sendJson(String json) {
        WebSocket ws = webSocket;
        return ws != null && ws.send(json);
    }

    // ---------- 连接 ----------

    private void connect() {
        // 取消任何已排队的重连，避免多 timer 叠加连续建连
        handler.removeCallbacks(reconnectRunnable);
        reconnectScheduled = false;
        String token = TokenManager.getToken(this);
        if (token == null) {
            return;
        }
        shouldReconnect = true;
        String url = BuildConfig.WS_BASE_URL + "?token=" + token;
        Request request = new Request.Builder().url(url).build();
        OkHttpClient client = com.campus.help.core.network.OkHttpProvider.getClient();
        if (webSocket != null) {
            webSocket.close(1000, "reconnect");
        }
        webSocket = client.newWebSocket(request, listener);
    }

    private final WebSocketListener listener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket ws, Response response) {
            reconnectDelay = 1;
        }

        @Override
        public void onMessage(WebSocket ws, String text) {
            handleIncoming(text);
        }

        @Override
        public void onClosing(WebSocket ws, int code, String reason) {
            ws.close(code, reason);
        }

        @Override
        public void onClosed(WebSocket ws, int code, String reason) {
            onDisconnect();
            scheduleReconnect();
        }

        @Override
        public void onFailure(WebSocket ws, Throwable t, @Nullable Response response) {
            onDisconnect();
            scheduleReconnect();
        }
    };

    /** 连接丢失：把未收到 ACK 的发送判失败，再排重连。onClosed/onFailure 都可能触发，幂等。 */
    private void onDisconnect() {
        try {
            MessageRepository.getInstance(this).onWsDisconnected();
        } catch (Exception ignored) {
        }
    }

    private void scheduleReconnect() {
        if (!shouldReconnect) {
            return;
        }
        // onClosed/onFailure 在 OkHttp 后台线程触发；切到主线程做「是否已排队」判断 + 排队，
        // 用 reconnectScheduled 标志去重，避免重复排两个 timer（不依赖 API 29 的 hasCallbacks）
        handler.post(this::doScheduleReconnect);
    }

    private void doScheduleReconnect() {
        if (!shouldReconnect || reconnectScheduled) {
            return;
        }
        reconnectScheduled = true;
        handler.postDelayed(reconnectRunnable, reconnectDelay * 1000L);
        reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY);
    }

    // ---------- 收消息分发 ----------

    private void handleIncoming(String json) {
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            MessageRepository repo = MessageRepository.getInstance(this);
            if (obj.has("ack") && obj.get("ack").getAsBoolean()) {
                // 发送方 ACK：用真实 id 替换乐观临时消息
                int seq = obj.has("seq") ? obj.get("seq").getAsInt() : 0;
                repo.applyAck(seq, parseMessage(obj));
                return;
            }
            ChatMessage msg = parseMessage(obj);
            repo.cacheIncoming(msg);
            MessageBus.get().post(msg);
            if (!ChatActivity.isForegroundFor(msg.conversationId)) {
                notifyIncoming(msg);
            }
            repo.refreshUnread();
        } catch (Exception e) {
            // 解析失败不崩
        }
    }

    /** 把 WS JSON（from/to 而非 senderId/receiverId）映射成 ChatMessage。 */
    private ChatMessage parseMessage(JsonObject obj) {
        ChatMessage m = new ChatMessage();
        m.id = obj.has("id") ? obj.get("id").getAsLong() : 0L;
        m.conversationId = obj.has("conversationId") ? obj.get("conversationId").getAsString() : null;
        m.senderId = obj.has("from") ? obj.get("from").getAsLong() : 0L;
        m.receiverId = obj.has("to") ? obj.get("to").getAsLong() : 0L;
        m.content = obj.has("content") ? obj.get("content").getAsString() : "";
        m.type = obj.has("type") ? obj.get("type").getAsInt() : 0;
        m.timestamp = obj.has("timestamp") ? obj.get("timestamp").getAsLong() : System.currentTimeMillis();
        m.read = false;
        return m;
    }

    // ---------- 通知 ----------

    private void notifyIncoming(ChatMessage msg) {
        if (!PermissionUtils.hasPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
            return;
        }
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, msg.conversationId);
        intent.putExtra(ChatActivity.EXTRA_PEER_ID, msg.senderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, (int) (msg.id % Integer.MAX_VALUE),
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification n = new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_IM)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("用户 " + msg.senderId)
                .setContentText(msg.content == null ? "" : msg.content)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        try {
            NotificationManagerCompat.from(this)
                    .notify((int) (msg.id % Integer.MAX_VALUE), n);
        } catch (SecurityException ignored) {
        }
    }

    // ---------- 前台保活通知 ----------

    private Notification buildServiceNotification() {
        PendingIntent pi = PendingIntent.getActivity(
                this, 0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_SERVICE)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("互助消息服务运行中")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void startForegroundCompat(int id, Notification n) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(id, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(id, n);
        }
    }

    // ---------- 启动入口 ----------

    public static void start(Context ctx) {
        ContextCompat.startForegroundService(ctx, new Intent(ctx, WebSocketService.class));
    }
}
