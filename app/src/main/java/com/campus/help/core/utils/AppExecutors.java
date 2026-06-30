package com.campus.help.core.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 线程池封装：磁盘 IO / 网络 / 主线程切换。
 * Room 写操作、Retrofit 同步调用建议走 diskIO/networkIO，回调切回主线程。
 */
public class AppExecutors {

    private static volatile AppExecutors instance;
    private final Executor diskIO = Executors.newSingleThreadExecutor();
    private final Executor networkIO = Executors.newFixedThreadPool(3);
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    private AppExecutors() {
    }

    public static AppExecutors get() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors();
                }
            }
        }
        return instance;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public void main(Runnable r) {
        mainThread.post(r);
    }
}
