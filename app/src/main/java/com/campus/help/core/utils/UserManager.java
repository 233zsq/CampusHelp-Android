package com.campus.help.core.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campus.help.core.base.Callback;
import com.campus.help.core.network.ApiResponse;
import com.campus.help.core.network.OkHttpProvider;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.core.network.UploadApi;
import com.campus.help.core.network.UserApi;
import com.campus.help.core.network.dto.UploadResponse;
import com.campus.help.data.model.User;
import com.campus.help.feature.im.WebSocketService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 身份与用户信息统一入口（成员 A）。封装 {@link TokenManager}（取 currentUserId）+ {@link UserApi}（取用户信息）。
 *
 * <p>B/C/D 统一通过本类获取当前用户 id 与用户信息，不直接触碰 TokenManager / UserApi。
 * 在 {@link com.campus.help.CampusHelpApp#onCreate} 调用 {@link #init} 完成初始化，之后用 {@link #get()} 取实例。
 *
 * <p>成员 D 在此基础上补充 {@link #logout}（后端登出 + 清本地 + 停 WS）与 {@link #uploadAvatar}（头像上传 → 回写 → 刷新）。
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
    private final UploadApi uploadApi;
    private final MutableLiveData<User> userInfo = new MutableLiveData<>();

    private UserManager(Context ctx) {
        this.appContext = ctx.getApplicationContext();
        this.api = RetrofitClient.create(UserApi.class);
        this.uploadApi = RetrofitClient.create(UploadApi.class);
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

    /**
     * 退出登录（成员 D）：fire-and-forget 调后端 logout（使 token 失效），无论成败都清本地状态。
     * 清 TokenManager + OkHttpProvider token + 停 WebSocketService，然后回调（主线程）。
     */
    public void logout(Context ctx, Runnable onDone) {
        api.logout().enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> c, Response<ApiResponse<Void>> resp) {
                cleanupAfterLogout(ctx, onDone);
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> c, Throwable t) {
                cleanupAfterLogout(ctx, onDone);
            }
        });
    }

    private void cleanupAfterLogout(Context ctx, Runnable onDone) {
        TokenManager.clear(ctx);
        OkHttpProvider.setToken(null);
        try {
            ctx.stopService(new Intent(ctx, WebSocketService.class));
        } catch (Exception ignored) {
        }
        if (onDone != null) {
            onDone.run();
        }
    }

    /**
     * 上传头像（成员 D）：读 Uri → POST /api/upload → 拿 url → PUT /api/users/{id} 回写 avatar
     * → 拉最新用户信息 post 到 {@link #getUserInfo()} 并回调。
     * 全程异步，成功 cb.onResult 为刷新后的 User，失败为 null（主线程回调）。
     */
    public void uploadAvatar(Context ctx, Uri uri, Callback<User> cb) {
        long uid = getCurrentUserId();
        AppExecutors.get().diskIO().execute(() -> {
            byte[] bytes = readBytes(ctx, uri);
            if (bytes == null) {
                AppExecutors.get().main(() -> { if (cb != null) cb.onResult(null); });
                return;
            }
            String mime = ctx.getContentResolver().getType(uri);
            if (mime == null) {
                mime = "image/*";
            }
            RequestBody body = RequestBody.create(bytes, MediaType.parse(mime));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", "avatar.jpg", body);
            uploadApi.upload(part).enqueue(new retrofit2.Callback<ApiResponse<UploadResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<UploadResponse>> c,
                                       Response<ApiResponse<UploadResponse>> resp) {
                    if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()
                            && resp.body().getData() != null) {
                        writeBackAvatar(uid, resp.body().getData().url, cb);
                    } else {
                        AppExecutors.get().main(() -> { if (cb != null) cb.onResult(null); });
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UploadResponse>> c, Throwable t) {
                    AppExecutors.get().main(() -> { if (cb != null) cb.onResult(null); });
                }
            });
        });
    }

    /** 头像 URL 回写到 user.avatar，成功后拉最新用户信息并回调。 */
    private void writeBackAvatar(long uid, String url, Callback<User> cb) {
        Map<String, Object> body = new HashMap<>();
        body.put("avatar", url);
        api.updateUser(uid, body).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> c, Response<ApiResponse<Void>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    api.getById(uid).enqueue(new retrofit2.Callback<ApiResponse<User>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<User>> c2,
                                               Response<ApiResponse<User>> r2) {
                            User user = (r2.isSuccessful() && r2.body() != null
                                    && r2.body().isSuccess()) ? r2.body().getData() : null;
                            userInfo.postValue(user);
                            AppExecutors.get().main(() -> { if (cb != null) cb.onResult(user); });
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<User>> c2, Throwable t) {
                            AppExecutors.get().main(() -> { if (cb != null) cb.onResult(null); });
                        }
                    });
                } else {
                    AppExecutors.get().main(() -> { if (cb != null) cb.onResult(null); });
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> c, Throwable t) {
                AppExecutors.get().main(() -> { if (cb != null) cb.onResult(null); });
            }
        });
    }

    private static byte[] readBytes(Context ctx, Uri uri) {
        try (InputStream is = ctx.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (is == null) return null;
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                baos.write(buf, 0, n);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
