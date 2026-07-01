package com.campus.help.server.service;

import com.campus.help.server.entity.Order;

import java.util.List;

/**
 * 接单 Service。
 */
public interface OrderService {

    /**
     * 接单。
     */
    Long accept(Order order);

    /**
     * 根据接单人查询接单列表。
     */
    List<Order> listByTaker(Long takerId);

    /**
     * 根据任务 ID 查询接单。
     */
    Order getByTaskId(Long taskId);

    /**
     * 完成接单。
     */
    void complete(Long orderId, int status, long completedAt);
}
