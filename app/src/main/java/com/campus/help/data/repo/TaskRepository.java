package com.campus.help.data.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campus.help.core.base.BaseRepository;
import com.campus.help.core.base.Callback;
import com.campus.help.core.network.ApiResponse;
import com.campus.help.core.network.RetrofitClient;
import com.campus.help.core.network.TaskApi;
import com.campus.help.core.network.dto.PageResponse;
import com.campus.help.data.model.Task;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 任务数据源（远端）。通过 Retrofit 调后端 TaskController。
 * 各 observe* 方法返回 LiveData，内部异步请求，成功后 postValue。
 */
public class TaskRepository extends BaseRepository {

    private static final int DEFAULT_SIZE = 20;

    private final TaskApi api;

    public TaskRepository() {
        api = RetrofitClient.create(TaskApi.class);
    }

    public LiveData<List<Task>> observeAll() {
        return observePage(api.list(1, DEFAULT_SIZE));
    }

    public LiveData<List<Task>> observeByType(int type) {
        return observePage(api.listByType(type, 1, DEFAULT_SIZE));
    }

    public LiveData<List<Task>> observeByStatus(int status) {
        return observePage(api.listByStatus(status, 1, DEFAULT_SIZE));
    }

    public void insert(Task task, Callback<Long> cb) {
        api.publish(task).enqueue(new retrofit2.Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> resp) {
                if (cb != null && resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    cb.onResult(resp.body().getData());
                } else if (cb != null) {
                    cb.onResult(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                if (cb != null) {
                    cb.onResult(null);
                }
            }
        });
    }

    public void updateStatus(long taskId, int status) {
        Map<String, Integer> body = new HashMap<>();
        body.put("status", status);
        api.updateStatus(taskId, body).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> resp) {
                // no-op
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // no-op
            }
        });
    }

    /** 统一把分页请求封装成 LiveData，成功 post records，失败 post 空列表。 */
    private LiveData<List<Task>> observePage(Call<ApiResponse<PageResponse<Task>>> call) {
        MutableLiveData<List<Task>> live = new MutableLiveData<>(Collections.emptyList());
        call.enqueue(new retrofit2.Callback<ApiResponse<PageResponse<Task>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Task>>> c,
                                   Response<ApiResponse<PageResponse<Task>>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    PageResponse<Task> page = resp.body().getData();
                    live.postValue(page != null && page.records != null
                            ? page.records : Collections.emptyList());
                } else {
                    live.postValue(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Task>>> c, Throwable t) {
                live.postValue(Collections.emptyList());
            }
        });
        return live;
    }
}
