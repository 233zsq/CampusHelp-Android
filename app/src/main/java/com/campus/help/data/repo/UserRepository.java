package com.campus.help.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campus.help.core.base.BaseRepository;
import com.campus.help.core.base.Callback;
import com.campus.help.core.db.AppDatabase;
import com.campus.help.core.network.ApiResponse;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.core.network.UserApi;
import com.campus.help.core.utils.AppExecutors;
import com.campus.help.data.dao.UserDao;
import com.campus.help.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 用户数据源。{@link #observeUser} 已迁移到网络（信用分单一真源 = GET /api/users/{id} 的 creditScore）。
 * insert / observeAll 仍为本地 Room（离线/演示兜底），完整迁移见团队分工成员 A 后续任务。
 */
public class UserRepository extends BaseRepository {

    private final UserDao dao;
    private final UserApi api;

    public UserRepository(Context context) {
        dao = AppDatabase.getInstance(context.getApplicationContext()).userDao();
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

    public LiveData<List<User>> observeAll() {
        return dao.observeAll();
    }

    public void insert(User user, Callback<Long> cb) {
        AppExecutors.get().diskIO().execute(() -> {
            long id = dao.insert(user);
            if (cb != null) {
                AppExecutors.get().main(() -> cb.onResult(id));
            }
        });
    }
}
