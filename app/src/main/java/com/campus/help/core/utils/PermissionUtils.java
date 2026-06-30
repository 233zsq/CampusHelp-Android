package com.campus.help.core.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 运行时权限工具：定位 / 通知 / 媒体读取。
 */
public final class PermissionUtils {

    private PermissionUtils() {
    }

    /** 启动时按需申请的核心权限（定位 + 通知 + 媒体）。 */
    public static String[] getStartupPermissions() {
        List<String> ps = new ArrayList<>();
        ps.add(Manifest.permission.ACCESS_FINE_LOCATION);
        ps.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ps.add(Manifest.permission.POST_NOTIFICATIONS);
            ps.add(Manifest.permission.READ_MEDIA_IMAGES);
        }
        return ps.toArray(new String[0]);
    }

    public static boolean hasPermission(Context ctx, String permission) {
        return ContextCompat.checkSelfPermission(ctx, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasLocation(Context ctx) {
        return hasPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                || hasPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION);
    }
}
