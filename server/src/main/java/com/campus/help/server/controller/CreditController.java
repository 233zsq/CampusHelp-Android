package com.campus.help.server.controller;

import com.campus.help.server.common.Result;
import com.campus.help.server.entity.CreditRecord;
import com.campus.help.server.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 信用分控制器。
 * 对应 Android 端 CreditRepository 的远端数据源。
 */
@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    /**
     * 添加信用分变动记录。
     * POST /api/credits
     * Body: { "userId": 1, "delta": 10, "reason": "完成代取快递" }
     */
    @PostMapping
    public Result<Void> addRecord(@RequestBody CreditRecord record,
                                  @RequestAttribute("currentUserId") Long currentUserId) {
        // userId 强制取登录用户，防止客户端给他人刷分。
        // 注：信用分自动结算（如订单完成 +10）应在 Service 层内部触发，不走此接口。
        record.setUserId(currentUserId);
        creditService.addRecord(record);
        return Result.ok();
    }

    /**
     * 查询用户的信用分变动记录。
     * GET /api/credits?userId=1
     */
    @GetMapping
    public Result<List<CreditRecord>> listByUser(@RequestParam Long userId) {
        return Result.ok(creditService.listByUser(userId));
    }

    /**
     * 查询用户信用分总变动量。
     * GET /api/credits/sum?userId=1
     */
    @GetMapping("/sum")
    public Result<Map<String, Integer>> sumDelta(@RequestParam Long userId) {
        int sum = creditService.sumDelta(userId);
        return Result.ok(Map.of("sum", sum));
    }
}
