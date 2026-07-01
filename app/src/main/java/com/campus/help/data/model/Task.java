package com.campus.help.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * 任务实体（成员 B）。type: 0 跑腿 1 拼单 2 二手；status: 0 待接单 1 已接单 2 已完成 3 已取消。
 */
@Entity(tableName = "tasks")
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long publisherId;   // 发布者 userId

    public int type;           // 0 跑腿 1 拼单 2 二手

    public String title;

    public String content;

    public double reward;      // 报酬 / 单价

    public String location;    // 文案地址

    public double latitude;

    public double longitude;

    public int status;         // 0 待接单 1 已接单 2 已完成 3 已取消

    public long deadline;      // 截止时间

    public long createdAt;
}
