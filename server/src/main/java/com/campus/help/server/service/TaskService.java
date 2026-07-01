package com.campus.help.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.help.server.entity.Task;

/**
 * 任务 Service。
 */
public interface TaskService {

    /**
     * 发布任务。
     */
    Long publish(Task task);

    /**
     * 分页查询所有任务（按创建时间倒序）。
     */
    IPage<Task> listAll(int page, int size);

    /**
     * 按类型分页查询。
     */
    IPage<Task> listByType(int type, int page, int size);

    /**
     * 按状态分页查询。
     */
    IPage<Task> listByStatus(int status, int page, int size);

    /**
     * 根据 ID 查询。
     */
    Task getById(Long id);

    /**
     * 更新任务状态。
     */
    void updateStatus(Long taskId, int status);
}
