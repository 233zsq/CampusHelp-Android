package com.campus.help.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户实体 — 对应 Android 端 com.campus.help.data.model.User + Room users 表。
 * MySQL 表名：user
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学号 */
    private String studentId;

    /** 密码 (BCrypt) */
    private String password;

    /** 昵称 */
    private String name;

    /** 头像 URL */
    private String avatar;

    /** 信用分 0~1000 */
    private Integer creditScore;

    /** 手机号 */
    private String phone;

    /** 创建时间戳 (ms) */
    private Long createdAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
