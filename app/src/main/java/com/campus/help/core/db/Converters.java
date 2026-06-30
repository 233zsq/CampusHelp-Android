package com.campus.help.core.db;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Room 类型转换器：Date <-> Long(时间戳)。
 */
public class Converters {

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
}
