package com.campus.help.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 接单实体 — 对应 Android 端 com.campus.help.data.model.Order + Room orders 表。
 * status: 0 进行中 1 已完成 2 超时。
 */
@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 task.id */
    private Long taskId;

    /** 接单人 user.id */
    private Long takerId;

    /** 接单时间戳 (ms) */
    private Long acceptedAt;

    /** 接单截止时间戳 (ms) */
    private Long deadline;

    /** 0 进行中 1 已完成 2 超时 */
    private Integer status;

    /** 完成时间戳 (ms) */
    private Long completedAt;

    @TableLogic
    private Integer deleted;
}
