package com.campus.help.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.campus.help.R;
import com.campus.help.core.base.BaseActivity;
import com.campus.help.core.bus.MessageBus;
import com.campus.help.core.utils.PermissionUtils;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.data.repo.MessageRepository;
import com.campus.help.databinding.ActivityMainBinding;
import com.campus.help.ui.login.LoginActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主界面：底部导航壳，承载 4 个 Fragment Tab（首页 / 发布 / 消息 / 我的）。
 * 未登录时跳转 {@link LoginActivity}。消息 Tab 显示未读角标。
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {

    private MessageRepository repo;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            });

    @Override
    protected ActivityMainBinding createBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        if (!TokenManager.isLogged(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        // 确保 IM 长连接在跑（从通知或外部启动回到主界面时幂等启动）
        com.campus.help.feature.im.WebSocketService.start(this);
        requestNotificationPermissionIfNeeded();

        repo = MessageRepository.getInstance(this);

        BottomNavigationView nav = binding.bottomNav;
        nav.setOnItemSelectedListener(item -> {
            Fragment target = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                target = new HomeFragment();
            } else if (id == R.id.nav_publish) {
                target = new PublishFragment();
            } else if (id == R.id.nav_message) {
                target = new MessageFragment();
            } else if (id == R.id.nav_mine) {
                target = new MineFragment();
            }
            if (target != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_container, target)
                        .commit();
                return true;
            }
            return false;
        });
        // 默认选中首页
        nav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void initData() {
        if (repo == null) {
            return; // 未登录分支：initView 已 finish()，跳过
        }
        // 未读总数角标
        repo.observeUnreadTotal().observe(this, total -> updateBadge(total == null ? 0L : total));
        // 实时收到新消息时刷新未读
        MessageBus.get().getIncoming().observe(this, m -> repo.refreshUnread());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repo != null) {
            repo.refreshUnread();
        }
    }

    private void updateBadge(long total) {
        BadgeDrawable badge = binding.bottomNav.getBadge(R.id.nav_message);
        if (total <= 0) {
            if (badge != null) {
                badge.setVisible(false);
                badge.clearNumber();
            }
            return;
        }
        if (badge == null) {
            badge = binding.bottomNav.getOrCreateBadge(R.id.nav_message);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.credit_low));
            badge.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));
        }
        badge.setNumber((int) Math.min(total, 99));
        badge.setVisible(true);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (!PermissionUtils.hasPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}
