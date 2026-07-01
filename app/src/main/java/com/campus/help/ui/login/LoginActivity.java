package com.campus.help.ui.login;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.campus.help.core.base.BaseActivity;
import com.campus.help.core.network.ApiResponse;
import com.campus.help.core.network.OkHttpProvider;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.core.network.UserApi;
import com.campus.help.core.network.dto.LoginRequest;
import com.campus.help.core.network.dto.LoginResponse;
import com.campus.help.core.network.dto.RegisterRequest;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.databinding.ActivityLoginBinding;
import com.campus.help.ui.MainActivity;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 登录 / 注册。
 * 登录成功后 token 持久化到 {@link TokenManager}，并注入 {@link OkHttpProvider}，
 * 随后跳转 MainActivity。
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private final UserApi api = RetrofitClient.create(UserApi.class);

    @Override
    protected ActivityLoginBinding createBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        binding.btnLogin.setOnClickListener(v -> doLogin());
        binding.btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doLogin() {
        String sid = binding.etStudentId.getText().toString().trim();
        String pwd = binding.etPassword.getText().toString().trim();
        if (sid.isEmpty() || pwd.isEmpty()) {
            toast("请输入学号和密码");
            return;
        }
        setLoading(true);
        api.login(new LoginRequest(sid, pwd)).enqueue(new retrofit2.Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    LoginResponse data = resp.body().getData();
                    TokenManager.save(LoginActivity.this, data.token, data.userId, data.name);
                    OkHttpProvider.setToken(data.token);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    toast("学号或密码错误");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                toast("网络错误：" + t.getMessage());
            }
        });
    }

    private void doRegister() {
        String sid = binding.etStudentId.getText().toString().trim();
        String pwd = binding.etPassword.getText().toString().trim();
        if (sid.isEmpty() || pwd.isEmpty()) {
            toast("请输入学号和密码");
            return;
        }
        setLoading(true);
        // 注册时昵称默认用学号
        api.register(new RegisterRequest(sid, pwd, sid)).enqueue(new retrofit2.Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    toast("注册成功，请登录");
                } else {
                    toast("注册失败（学号可能已存在）");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                setLoading(false);
                toast("网络错误：" + t.getMessage());
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.btnRegister.setEnabled(!loading);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
