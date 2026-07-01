package com.campus.help.server.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.help.server.common.Result;
import com.campus.help.server.entity.Task;
import com.campus.help.server.service.TaskService;
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

import java.util.Map;

/**
 * 任务控制器。
 * 对应 Android 端 TaskRepository 的远端数据源。
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 发布任务。
     * POST /api/tasks
     */
    @PostMapping
    public Result<Long> publish(@RequestBody Task task,
                                @RequestAttribute("currentUserId") Long currentUserId) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            return Result.fail(400, "任务标题不能为空");
        }
        // 发布者强制取登录用户，忽略客户端传入，防越权
        task.setPublisherId(currentUserId);
        Long taskId = taskService.publish(task);
        return Result.ok(taskId);
    }

    /**
     * 分页查询任务列表。
     * GET /api/tasks?type=0&status=0&page=1&size=10
     */
    @GetMapping
    public Result<IPage<Task>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        IPage<Task> result;
        if (type != null) {
            result = taskService.listByType(type, page, size);
        } else if (status != null) {
            result = taskService.listByStatus(status, page, size);
        } else {
            result = taskService.listAll(page, size);
        }
        return Result.ok(result);
    }

    /**
     * 获取任务详情。
     * GET /api/tasks/{id}
     */
    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        Task task = taskService.getById(id);
        if (task == null) {
            return Result.fail(404, "任务不存在");
        }
        return Result.ok(task);
    }

    /**
     * 更新任务状态。
     * PUT /api/tasks/{id}/status
     * Body: { "status": 2 }
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            return Result.fail(400, "状态不能为空");
        }
        taskService.updateStatus(id, status);
        return Result.ok();
    }
}
