package com.campus.help.server.controller;

import com.campus.help.server.common.Result;
import com.campus.help.server.entity.Order;
import com.campus.help.server.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 接单控制器。
 * 对应 Android 端 OrderDao 的远端数据源。
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 接单。
     * POST /api/orders
     * Body: { "taskId": 1, "takerId": 2, "deadline": 1700000000000 }
     */
    @PostMapping
    public Result<Long> accept(@RequestBody Order order,
                               @RequestAttribute("currentUserId") Long currentUserId) {
        if (order.getTaskId() == null) {
            return Result.fail(400, "taskId 不能为空");
        }
        // 接单人强制取登录用户，忽略客户端传入，防越权
        order.setTakerId(currentUserId);
        try {
            Long orderId = orderService.accept(order);
            return Result.ok(orderId);
        } catch (RuntimeException e) {
            return Result.fail(409, e.getMessage());
        }
    }

    /**
     * 查询我的接单列表。
     * GET /api/orders?takerId=2
     */
    @GetMapping
    public Result<List<Order>> listByTaker(@RequestParam Long takerId) {
        return Result.ok(orderService.listByTaker(takerId));
    }

    /**
     * 完成接单。
     * PUT /api/orders/{id}/complete
     * Body: { "status": 1 }
     */
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            return Result.fail(400, "状态不能为空");
        }
        orderService.complete(id, status, System.currentTimeMillis());
        return Result.ok();
    }
}
