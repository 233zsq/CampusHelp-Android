package com.campus.help.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.help.server.entity.Task;
import com.campus.help.server.mapper.TaskMapper;
import com.campus.help.server.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;

    @Override
    public Long publish(Task task) {
        task.setStatus(0); // 待接单
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(System.currentTimeMillis());
        }
        taskMapper.insert(task);
        log.info("任务发布成功: id={}, title={}", task.getId(), task.getTitle());
        return task.getId();
    }

    @Override
    public IPage<Task> listAll(int page, int size) {
        return taskMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Task>().orderByDesc(Task::getCreatedAt)
        );
    }

    @Override
    public IPage<Task> listByType(int type, int page, int size) {
        return taskMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getType, type)
                        .orderByDesc(Task::getCreatedAt)
        );
    }

    @Override
    public IPage<Task> listByStatus(int status, int page, int size) {
        return taskMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getStatus, status)
                        .orderByDesc(Task::getCreatedAt)
        );
    }

    @Override
    public Task getById(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    public void updateStatus(Long taskId, int status) {
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(status);
        taskMapper.updateById(task);
    }
}
