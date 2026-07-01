package com.campus.help.data.repo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campus.help.core.base.BaseRepository;
import com.campus.help.core.base.Callback;
import com.campus.help.core.network.ApiResponse;
import com.campus.help.core.network.CreditApi;
import com.campus.help.core.network.OkHttpProvider;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.core.network.UploadApi;
import com.campus.help.core.network.UserApi;
import com.campus.help.core.network.dto.UploadResponse;
import com.campus.help.core.utils.AppExecutors;
import com.campus.help.core.utils.TokenManager;
import com.campus.help.data.model.CreditRecord;
import com.campus.help.data.model.User;
import com.campus.help.feature.im.WebSocketService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 用户身份与信用统一入口（成员 D 自补齐，仿 TaskRepository 纯网络 + LiveData 模式）。
 * <p>
 * 封装 {@link TokenManager} + {@link UserApi} + {@link CreditApi} + {@link UploadApi}，
 * 给个人中心 / 头像上传 / 退出登录提供单一数据源。后续成员 A 可在此基础上对齐。
 */
public class UserManager extends BaseRepository {

    private static volatile UserManager instance;

    private final UserApi userApi;
    private final CreditApi creditApi;
    private final UploadApi uploadApi;

    private UserManager() {
        userApi = RetrofitClient.create(UserApi.class);
        creditApi = RetrofitClient.create(CreditApi.class);
        uploadApi = RetrofitClient.create(UploadApi.class);
    }

    public static UserManager get() {
        if (instance == null) {
            synchronized (UserManager.class) {
                if (instance == null) {
                    instance = new UserManager();
                }
            }
        }
        return instance;
    }

    /** 当前登录用户 id（来自 TokenManager 持久化）。 */
    public long getCurrentUserId(Context ctx) {
        return TokenManager.getUserId(ctx);
    }

    /** 拉取指定用户信息。成功 post User，失败 post null。 */
    public LiveData<User> getUserInfo(long id) {
        MutableLiveData<User> live = new MutableLiveData<>();
        userApi.getUser(id).enqueue(new retrofit2.Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> c, Response<ApiResponse<User>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    live.postValue(resp.body().getData());
                } else {
                    live.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> c, Throwable t) {
                live.postValue(null);
            }
        });
        return live;
    }

    /** 拉取当前登录用户信息。 */
    public LiveData<User> refreshUserInfo(Context ctx) {
        return getUserInfo(getCurrentUserId(ctx));
    }

    /** 信用分变动明细。成功 post 列表，失败 post 空列表。 */
    public LiveData<List<CreditRecord>> listCredits(long userId) {
        MutableLiveData<List<CreditRecord>> live = new MutableLiveData<>(Collections.emptyList());
        creditApi.list(userId).enqueue(new retrofit2.Callback<ApiResponse<List<CreditRecord>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CreditRecord>>> c,
                                   Response<ApiResponse<List<CreditRecord>>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    List<CreditRecord> data = resp.body().getData();
                    live.postValue(data != null ? data : Collections.emptyList());
                } else {
                    live.postValue(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CreditRecord>>> c, Throwable t) {
                live.postValue(Collections.emptyList());
            }
        });
        return live;
    }

    /**
     * 上传头像：读 Uri → POST /api/upload → 拿 url → PUT /api/users/{id} 回写 avatar。
     * 全程异步，成功/失败均通过 cb 回调（主线程），cb.onResult 为刷新后的 User（失败为 null）。
     */
    public void uploadAvatar(Context ctx, Uri uri, Callback<User> cb) {
        long uid = getCurrentUserId(ctx);
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

    /** 头像 URL 回写到 user.avatar，成功后重新拉取用户信息回调。 */
    private void writeBackAvatar(long uid, String url, Callback<User> cb) {
        Map<String, Object> body = new HashMap<>();
        body.put("avatar", url);
        userApi.updateUser(uid, body).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> c, Response<ApiResponse<Void>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    // 回写成功后重新拉取用户信息刷新
                    userApi.getUser(uid).enqueue(new retrofit2.Callback<ApiResponse<User>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<User>> c2,
                                               Response<ApiResponse<User>> r2) {
                            User user = (r2.isSuccessful() && r2.body() != null
                                    && r2.body().isSuccess()) ? r2.body().getData() : null;
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

    /**
     * 退出登录：fire-and-forget 调后端 logout（使 token 失效），无论成败都清本地状态。
     * 清 TokenManager + OkHttpProvider token + 停 WebSocketService，然后回调。
     */
    public void logout(Context ctx, Runnable onDone) {
        userApi.logout().enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> c, Response<ApiResponse<Void>> resp) {
                cleanupAndFinish(ctx, onDone);
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> c, Throwable t) {
                cleanupAndFinish(ctx, onDone);
            }
        });
    }

    private void cleanupAndFinish(Context ctx, Runnable onDone) {
        TokenManager.clear(ctx);
        OkHttpProvider.setToken(null);
        try {
            ctx.stopService(new Intent(ctx, WebSocketService.class));
        } catch (Exception ignored) {
        }
        if (onDone != null) {
            AppExecutors.get().main(onDone);
        }
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
