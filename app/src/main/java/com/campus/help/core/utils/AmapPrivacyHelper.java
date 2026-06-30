package com.campus.help.core.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * 高德地图隐私合规辅助：反射调用，避免编译期硬依赖。
 * 成员 D 引入高德 SDK (com.amap.api:3dmap / location / search) 后反射自动生效；
 * 未引入时静默跳过，不影响编译与运行。
 *
 * 高德合规要求：必须在任何地图/定位/搜索实例化前调用隐私接口。
 * 正式版应先弹窗征得用户同意，同意后再调用 updatePrivacyAgree(true)。
 */
public final class AmapPrivacyHelper {

    private static final String TAG = "AmapPrivacyHelper";

    private AmapPrivacyHelper() {
    }

    /**
     * 在 Application.onCreate() 最早调用。
     */
    public static void applyCompliance(Context context) {
        invokeStatic(context, "com.amap.api.maps.MapsInitializer");
        invokeStatic(context, "com.amap.api.location.AMapLocationClient");
        invokeStatic(context, "com.amap.api.services.ServiceSettings");
    }

    private static void invokeStatic(Context context, String className) {
        try {
            Class<?> clz = Class.forName(className);
            Method show = clz.getMethod("updatePrivacyShow",
                    Context.class, boolean.class, boolean.class);
            show.invoke(null, context, true, true);
            Method agree = clz.getMethod("updatePrivacyAgree",
                    Context.class, boolean.class);
            agree.invoke(null, context, true);
            Log.i(TAG, className + " privacy compliance applied");
        } catch (Exception e) {
            Log.w(TAG, className + " not on classpath — skip AMap compliance", e);
        }
    }
}
