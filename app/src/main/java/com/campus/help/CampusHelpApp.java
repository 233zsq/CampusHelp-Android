package com.campus.help;

import android.app.Application;

import com.campus.help.core.db.AppDatabase;
import com.campus.help.core.network.OkHttpProvider;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.core.utils.AmapPrivacyHelper;
import com.campus.help.core.utils.NotificationHelper;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.data.repo.MockDataSeeder;

/**
 * 应用入口。
 * <p>职责：高德隐私合规初始化（反射，不强依赖 SDK）、通知渠道、
 * 本地数据库与网络客户端预热、登录态恢复。</p>
 */
public class CampusHelpApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 高德隐私合规（反射调用，未引入 SDK 时静默跳过）
        AmapPrivacyHelper.applyCompliance(this);
        initInfrastructure();
    }

    private void initInfrastructure() {
        NotificationHelper.createChannels(this);
        AppDatabase.getInstance(this);  // 预热本地数据库
        RetrofitClient.getInstance();   // 预热网络客户端
        // 恢复登录态：App 重启后 OkHttp 仍能带上 Authorization 头
        String token = TokenManager.getToken(this);
        if (token != null) {
            OkHttpProvider.setToken(token);
            // 恢复登录态后重启 IM 长连接
            com.campus.help.feature.im.WebSocketService.start(this);
        }
        MockDataSeeder.seedIfEmpty(this); // 无后端时填充演示数据
    }
}
