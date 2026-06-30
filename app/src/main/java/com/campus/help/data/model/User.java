package com.campus.help.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 用户实体（成员 A）。信用分 creditScore 0~1000。
 */
@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String studentId;   // 学号

    public String name;

    public String avatar;      // 头像 URL

    public int creditScore;    // 信用分 0~1000

    public String phone;

    public long createdAt;
}
