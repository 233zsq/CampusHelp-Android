package com.campus.help.server.controller;

import com.campus.help.server.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器（成员 D）。
 * <p>
 * POST /api/upload 接收 MultipartFile，存本地磁盘，返回可访问 URL。
 * 端点在 /api/** 内，自动经 JWT 拦截器鉴权（仅登录用户可传）；
 * 上传后客户端再调 PUT /api/users/{id} 把返回的 url 回写到 user.avatar。
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload-url-prefix:http://localhost:8080/uploads}")
    private String urlPrefix;

    @PostMapping
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestAttribute("currentUserId") Long currentUserId) {
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "文件为空");
        }

        String original = file.getOriginalFilename();
        String ext = getExtension(original);
        String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());
        } catch (IOException e) {
            log.error("上传失败: userId={}, file={}", currentUserId, original, e);
            return Result.fail(500, "上传失败");
        }

        Map<String, String> data = new HashMap<>();
        data.put("url", urlPrefix + "/" + filename);
        data.put("filename", filename);
        return Result.ok(data);
    }

    private static String getExtension(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1);
    }
}
