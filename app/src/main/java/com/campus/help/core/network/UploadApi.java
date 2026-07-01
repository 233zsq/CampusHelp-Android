package com.campus.help.core.network;

import com.campus.help.core.network.dto.UploadResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * 文件上传 API（对应后端 UploadController）。
 * POST /api/upload  multipart/form-data，字段名 file。
 */
public interface UploadApi {

    @Multipart
    @POST("api/upload")
    Call<ApiResponse<UploadResponse>> upload(@Part MultipartBody.Part file);
}
