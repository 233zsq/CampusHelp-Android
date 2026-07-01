package com.campus.help.server.controller;

import com.campus.help.server.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查（无需认证）。
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of("status", "UP", "service", "CampusHelp Server"));
    }
}
