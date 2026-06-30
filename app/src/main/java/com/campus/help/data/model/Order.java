package com.campus.help.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 接单实体（成员 B）。接单倒计时 = deadline - 当前时间。status: 0 进行中 1 已完成 2 超时。
 */
@Entity(tableName = "orders")
public class Order {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long taskId;

    public long takerId;       // 接单人 userId

    public long acceptedAt;    // 接单时间

    public long deadline;      // 接单倒计时截止

    public int status;         // 0 进行中 1 已完成 2 超时

    public long completedAt;
}
