package com.campus.help.core.utils;

import android.content.Context;

/**
 * 登录态持久化（SharedPreferences）。
 * token 同时写回 {@link com.campus.help.core.network.OkHttpProvider}，
 * 保证 App 重启后请求仍带 Authorization 头。
 */
public final class TokenManager {

    private static final String PREF = "campushelp_auth";
    private static final String K_TOKEN = "token";
    private static final String K_UID = "uid";
    private static final String K_NAME = "name";

    private TokenManager() {
    }

    public static void save(Context ctx, String token, long userId, String name) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(K_TOKEN, token)
                .putLong(K_UID, userId)
                .putString(K_NAME, name)
                .apply();
    }

    public static String getToken(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(K_TOKEN, null);
    }

    public static long getUserId(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getLong(K_UID, 0);
    }

    public static String getName(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(K_NAME, "");
    }

    public static boolean isLogged(Context ctx) {
        return getToken(ctx) != null;
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
