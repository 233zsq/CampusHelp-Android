package com.campus.help.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.help.server.entity.Order;
import com.campus.help.server.entity.Task;
import com.campus.help.server.mapper.OrderMapper;
import com.campus.help.server.mapper.TaskMapper;
import com.campus.help.server.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public Long accept(Order order) {
        // 检查任务是否存在且状态为待接单
        Task task = taskMapper.selectById(order.getTaskId());
        if (task == null || task.getStatus() != 0) {
            throw new RuntimeException("任务不存在或已被接单");
        }

        // 更新任务状态为已接单
        task.setStatus(1);
        taskMapper.updateById(task);

        // 创建接单记录
        order.setStatus(0); // 进行中
        if (order.getAcceptedAt() == null) {
            order.setAcceptedAt(System.currentTimeMillis());
        }
        orderMapper.insert(order);

        log.info("接单成功: orderId={}, taskId={}, takerId={}",
                order.getId(), order.getTaskId(), order.getTakerId());
        return order.getId();
    }

    @Override
    public List<Order> listByTaker(Long takerId) {
        return orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getTakerId, takerId)
                        .orderByDesc(Order::getAcceptedAt)
        );
    }

    @Override
    public Order getByTaskId(Long taskId) {
        return orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getTaskId, taskId)
        );
    }

    @Override
    @Transactional
    public void complete(Long orderId, int status, long completedAt) {
        Order existing = orderMapper.selectById(orderId);
        if (existing == null) {
            throw new RuntimeException("接单记录不存在");
        }

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(status);
        order.setCompletedAt(completedAt);
        orderMapper.updateById(order);

        // 同步更新任务状态为已完成
        Task task = new Task();
        task.setId(existing.getTaskId());
        task.setStatus(2); // 已完成
        taskMapper.updateById(task);
    }
}
