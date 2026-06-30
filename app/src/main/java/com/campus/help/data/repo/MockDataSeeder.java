package com.campus.help.data.repo;

import android.content.Context;

import com.campus.help.core.db.AppDatabase;
import com.campus.help.core.utils.AppExecutors;
import com.campus.help.data.model.CreditRecord;
import com.campus.help.data.model.Task;
import com.campus.help.data.model.User;

/**
 * 演示数据填充（无后端时跑通演示）。已存在数据则跳过，幂等。
 */
public final class MockDataSeeder {

    private MockDataSeeder() {
    }

    public static void seedIfEmpty(Context context) {
        AppExecutors.get().diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
            if (db.userDao().count() > 0) {
                return;
            }
            long now = System.currentTimeMillis();

            long id1 = db.userDao().insert(newUser("20210001", "小明", 880, now));
            long id2 = db.userDao().insert(newUser("20210002", "小红", 760, now));
            long id3 = db.userDao().insert(newUser("20210003", "阿强", 620, now));

            db.taskDao().insert(newTask(id1, 0, "代取菜鸟驿站快递", "3 号楼菜鸟驿站，取一个小件", 3.0, "西区 3 号楼", 30.0, 120.0, now));
            db.taskDao().insert(newTask(id2, 1, "食堂拼单-黄焖鸡", "二食堂拼单满减，差 1 人", 12.0, "第二食堂", 31.0, 121.0, now));
            db.taskDao().insert(newTask(id3, 2, "二手高数教材", "九成新同济高数，带笔记", 15.0, "图书馆门口", 31.2, 121.4, now));
            db.taskDao().insert(newTask(id1, 0, "代拿打印资料", "教学楼 B 打印 30 页", 2.0, "教学楼 B", 30.1, 120.1, now));

            db.creditDao().insert(record(id1, +10, "完成代取快递", now));
            db.creditDao().insert(record(id1, -5, "一次超时", now));
            db.creditDao().insert(record(id2, +10, "拼单成功", now));
        });
    }

    private static User newUser(String sid, String name, int credit, long now) {
        User u = new User();
        u.studentId = sid;
        u.name = name;
        u.creditScore = credit;
        u.createdAt = now;
        return u;
    }

    private static Task newTask(long publisherId, int type, String title, String content,
                                double reward, String location, double lat, double lng, long now) {
        Task t = new Task();
        t.publisherId = publisherId;
        t.type = type;
        t.title = title;
        t.content = content;
        t.reward = reward;
        t.location = location;
        t.latitude = lat;
        t.longitude = lng;
        t.status = 0;
        t.deadline = now + 60 * 60 * 1000L;
        t.createdAt = now;
        return t;
    }

    private static CreditRecord record(long userId, int delta, String reason, long now) {
        CreditRecord r = new CreditRecord();
        r.userId = userId;
        r.delta = delta;
        r.reason = reason;
        r.timestamp = now;
        return r;
    }
}
