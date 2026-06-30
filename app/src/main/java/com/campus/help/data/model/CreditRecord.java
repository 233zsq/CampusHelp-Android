package com.campus.help.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 信用分变动记录（成员 A）。delta 正负；reason 变动原因。
 */
@Entity(tableName = "credit_records")
public class CreditRecord {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;

    public int delta;          // 信用分变动 +/-

    public String reason;      // 变动原因

    public long timestamp;
}
