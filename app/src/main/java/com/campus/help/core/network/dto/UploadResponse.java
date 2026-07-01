package com.campus.help.core.network.dto;

/**
 * 文件上传响应 data（对应后端 UploadController.upload 返回）。
 */
public class UploadResponse {

    public String url;       // 可访问的文件 URL
    public String filename;  // 服务端存储的文件名
}
