package com.campus.help.core.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.campus.help.R;

/**
 * 通知渠道：IM 消息（高重要级）+ 前台服务保活（低重要级）。
 */
public final class NotificationHelper {

    public static final String CHANNEL_IM = "channel_im";
    public static final String CHANNEL_SERVICE = "channel_service";

    private NotificationHelper() {
    }

    public static void createChannels(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }

        NotificationChannel im = new NotificationChannel(
                CHANNEL_IM,
                ctx.getString(R.string.channel_im_name),
                NotificationManager.IMPORTANCE_HIGH);
        im.setDescription(ctx.getString(R.string.channel_im_desc));
        nm.createNotificationChannel(im);

        NotificationChannel service = new NotificationChannel(
                CHANNEL_SERVICE,
                ctx.getString(R.string.channel_service_name),
                NotificationManager.IMPORTANCE_LOW);
        service.setDescription(ctx.getString(R.string.channel_service_desc));
        nm.createNotificationChannel(service);
    }
}
