package com.campus.help.feature.im;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.campus.help.R;
import com.campus.help.core.utils.NotificationHelper;
import com.campus.help.ui.MainActivity;

/**
 * IM WebSocket 前台保活 Service（成员 C 填充 WebSocket 连接 / 断线重连 / 消息收发逻辑）。
 * 地基阶段：仅启动前台通知保持进程存活，不干蠢事崩系统。
 */
public class WebSocketService extends Service {

    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 成员 C：在此连接 WebSocket & 监听 MessageBus
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 不提供绑定
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private Notification buildNotification() {
        String channelId = NotificationHelper.CHANNEL_SERVICE;
        PendingIntent pi = PendingIntent.getActivity(
                this, 0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("互助消息服务运行中")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
