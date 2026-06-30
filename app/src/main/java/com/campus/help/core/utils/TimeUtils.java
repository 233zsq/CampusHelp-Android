package com.campus.help.core.utils;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间格式化工具：通用时间 / 接单倒计时 / 相对时间。
 */
public final class TimeUtils {

    private TimeUtils() {
    }

    private static final SimpleDateFormat DATETIME =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public static String formatTime(long millis) {
        return DATETIME.format(new Date(millis));
    }

    /** 接单倒计时格式：< 1 小时显示 mm:ss，否则 HH:mm:ss。 */
    public static String formatCountdown(long millis) {
        if (millis < 0) millis = 0;
        long s = millis / 1000;
        long hh = s / 3600;
        long mm = (s % 3600) / 60;
        long ss = s % 60;
        if (hh > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hh, mm, ss);
        }
        return String.format(Locale.getDefault(), "%02d:%02d", mm, ss);
    }

    public static String relative(long millis) {
        return DateUtils.getRelativeTimeSpanString(
                millis, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
    }
}
