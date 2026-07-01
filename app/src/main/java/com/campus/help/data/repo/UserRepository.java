package com.campus.help.data.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campus.help.core.base.BaseRepository;
import com.campus.help.core.base.Callback;
import com.campus.help.core.network.ApiResponse;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.core.network.UserApi;
import com.campus.help.data.model.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 用户数据源（远端）。通过 Retrofit 调后端 UserController，纯网络写法（仿 {@link TaskRepository}）。
 *
 * <p>信用分展示的单一真源是 {@link #observeUser} 取回的 user.creditScore
 * （后端已与 credit_record 同步并 clamp 0~1000）。
 * 各 observe* 方法返回 LiveData，内部异步请求，成功后 postValue。
 *
 * <p>身份 / 缓存的统一入口见 {@link com.campus.help.core.utils.UserManager}（B/C/D 优先用 UserManager）。
 */
public class UserRepository extends BaseRepository {

    private final UserApi api;

    public UserRepository() {
        api = RetrofitClient.create(UserApi.class);
    }

    /**
     * 获取用户信息（网络，单一真源）。信用分展示统一走这里取 user.creditScore。
     * 成功 post User，失败 post null。
     */
    public LiveData<User> observeUser(long id) {
        MutableLiveData<User> live = new MutableLiveData<>();
        api.getById(id).enqueue(new retrofit2.Callback<ApiResponse<User>>() {
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

    /**
     * 更新用户资料（昵称 / 头像 / 手机号）。PUT /api/users/{id}，
     * body 只含需更新字段，creditScore 不在此更新。
     *
     * @param cb 成功 onResult(true)，失败 onResult(false)。
     */
    public void updateUser(long id, Map<String, Object> body, Callback<Boolean> cb) {
        api.updateUser(id, body).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> resp) {
                if (cb != null) {
                    cb.onResult(resp.isSuccessful() && resp.body() != null && resp.body().isSuccess());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                if (cb != null) {
                    cb.onResult(false);
                }
            }
        });
    }
}
