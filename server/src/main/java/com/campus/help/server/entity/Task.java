package com.campus.help.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 任务实体 — 对应 Android 端 com.campus.help.data.model.Task + Room tasks 表。
 * type: 0 跑腿 1 拼单 2 二手；status: 0 待接单 1 已接单 2 已完成 3 已取消。
 */
@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布者 user.id */
    private Long publisherId;

    /** 0 跑腿 1 拼单 2 二手 */
    private Integer type;

    /** 标题 */
    private String title;

    /** 内容描述 */
    private String content;

    /** 报酬 / 单价 */
    private Double reward;

    /** 地点文案 */
    private String location;

    /** 纬度 */
    private Double latitude;

    /** 经度 */
    private Double longitude;

    /** 0 待接单 1 已接单 2 已完成 3 已取消 */
    private Integer status;

    /** 截止时间戳 (ms) */
    private Long deadline;

    /** 创建时间戳 (ms) */
    private Long createdAt;

    @TableLogic
    private Integer deleted;
}
