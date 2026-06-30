package com.campus.help.core.utils;

import android.content.Context;
import android.widget.Toast;

public final class UiUtils {

    private UiUtils() {
    }

    public static void toast(Context ctx, String msg) {
        if (ctx != null && msg != null) {
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
