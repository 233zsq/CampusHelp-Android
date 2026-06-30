package com.campus.help.core.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.campus.help.data.dao.CreditDao;
import com.campus.help.data.dao.MessageDao;
import com.campus.help.data.dao.OrderDao;
import com.campus.help.data.dao.TaskDao;
import com.campus.help.data.dao.UserDao;
import com.campus.help.data.model.ChatMessage;
import com.campus.help.data.model.CreditRecord;
import com.campus.help.data.model.Order;
import com.campus.help.data.model.Task;
import com.campus.help.data.model.User;

/**
 * Room 数据库：5 张表（用户 / 任务 / 订单 / 消息 / 信用记录）。
 * 无后端时可作为本地 mock 数据源跑通演示。
 */
@Database(
        entities = {
                User.class,
                Task.class,
                Order.class,
                ChatMessage.class,
                CreditRecord.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "campushelp.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract UserDao userDao();

    public abstract TaskDao taskDao();

    public abstract OrderDao orderDao();

    public abstract MessageDao messageDao();

    public abstract CreditDao creditDao();
}
