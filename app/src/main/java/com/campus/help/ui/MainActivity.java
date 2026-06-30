package com.campus.help.ui;

import androidx.fragment.app.Fragment;

import com.campus.help.R;
import com.campus.help.core.base.BaseActivity;
import com.campus.help.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主界面：底部导航壳，承载 4 个 Fragment Tab（首页 / 发布 / 消息 / 我的）。
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {

    @Override
    protected ActivityMainBinding createBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
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
}
