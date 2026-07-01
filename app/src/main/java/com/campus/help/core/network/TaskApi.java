package com.campus.help.core.network;

import com.campus.help.core.network.dto.PageResponse;
import com.campus.help.data.model.Task;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 任务 API（对应后端 TaskController）。通过 RetrofitClient.create(TaskApi.class) 获取。
 */
public interface TaskApi {

    /** 任务列表（按创建时间倒序，分页） */
    @GET("api/tasks")
    Call<ApiResponse<PageResponse<Task>>> list(@Query("page") int page, @Query("size") int size);

    /** 按类型筛选 */
    @GET("api/tasks")
    Call<ApiResponse<PageResponse<Task>>> listByType(@Query("type") int type,
                                                     @Query("page") int page,
                                                     @Query("size") int size);

    /** 按状态筛选 */
    @GET("api/tasks")
    Call<ApiResponse<PageResponse<Task>>> listByStatus(@Query("status") int status,
                                                       @Query("page") int page,
                                                       @Query("size") int size);

    /** 发布任务（publisherId 由后端强制取登录用户，忽略客户端传入） */
    @POST("api/tasks")
    Call<ApiResponse<Long>> publish(@Body Task task);

    /** 更新任务状态。body = { "status": 0~3 } */
    @PUT("api/tasks/{id}/status")
    Call<ApiResponse<Void>> updateStatus(@Path("id") long id, @Body Map<String, Integer> body);
}
