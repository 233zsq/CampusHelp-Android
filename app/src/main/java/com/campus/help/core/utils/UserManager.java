package com.campus.help.core.utils;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campus.help.core.network.ApiResponse;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.core.network.UserApi;
import com.campus.help.data.model.User;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 身份与用户信息统一入口（成员 A）。封装 {@link TokenManager}（取 currentUserId）+ {@link UserApi}（取用户信息）。
 *
 * <p>B/C/D 统一通过本类获取当前用户 id 与用户信息，不直接触碰 TokenManager / UserApi。
 * 在 {@link com.campus.help.CampusHelpApp#onCreate} 调用 {@link #init} 完成初始化，之后用 {@link #get()} 取实例。
 *
 * <p>用法：
 * <pre>
 *   long uid = UserManager.get().getCurrentUserId();
 *   UserManager.get().getUserInfo().observe(owner, user -> { ... });
 *   UserManager.get().refreshUserInfo();   // 拉最新
 * </pre>
 */
public class UserManager {

    private static volatile UserManager instance;

    private final Context appContext;
    private final UserApi api;
    private final MutableLiveData<User> userInfo = new MutableLiveData<>();

    private UserManager(Context ctx) {
        this.appContext = ctx.getApplicationContext();
        this.api = RetrofitClient.create(UserApi.class);
    }

    /** 在 Application.onCreate 调用一次，完成单例初始化。 */
    public static void init(Context ctx) {
        if (instance == null) {
            synchronized (UserManager.class) {
                if (instance == null) {
                    instance = new UserManager(ctx);
                }
            }
        }
    }

    /** 取单例；未 init 会抛 IllegalStateException。 */
    public static UserManager get() {
        if (instance == null) {
            throw new IllegalStateException(
                    "UserManager not initialized; call init() in CampusHelpApp.onCreate");
        }
        return instance;
    }

    /** 当前登录用户 id（未登录返回 0）。 */
    public long getCurrentUserId() {
        return TokenManager.getUserId(appContext);
    }

    /**
     * 当前用户信息（缓存 LiveData）。首次可能为空 / 旧值，调 {@link #refreshUserInfo()} 拉最新。
     */
    public LiveData<User> getUserInfo() {
        return userInfo;
    }

    /**
     * 拉取当前用户最新信息（GET /api/users/{id}）并 post 到 {@link #getUserInfo()}。
     * 未登录（id &lt;= 0）时不发请求，直接 post null。
     */
    public void refreshUserInfo() {
        long uid = getCurrentUserId();
        if (uid <= 0) {
            userInfo.postValue(null);
            return;
        }
        api.getById(uid).enqueue(new retrofit2.Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> c, Response<ApiResponse<User>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    userInfo.postValue(resp.body().getData());
                } else {
                    userInfo.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> c, Throwable t) {
                userInfo.postValue(null);
            }
        });
    }
}
