package com.campus.help.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 信用分变动记录 — 对应 Android 端 com.campus.help.data.model.CreditRecord + Room credit_records 表。
 */
@Data
@TableName("credit_record")
public class CreditRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 user.id */
    private Long userId;

    /** 信用分变动 (+/-) */
    private Integer delta;

    /** 变动原因 */
    private String reason;

    /** 变动时间戳 (ms) */
    private Long timestamp;

    @TableLogic
    private Integer deleted;
}
