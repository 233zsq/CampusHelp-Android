package com.campus.help.server.service;

import com.campus.help.server.entity.CreditRecord;

import java.util.List;

/**
 * 信用分 Service。
 */
public interface CreditService {

    /**
     * 添加信用分变动记录（同时更新 user 表的 creditScore）。
     */
    void addRecord(CreditRecord record);

    /**
     * 查询用户的信用分变动记录（按时间倒序）。
     */
    List<CreditRecord> listByUser(Long userId);

    /**
     * 计算用户信用分总变动量。
     */
    int sumDelta(Long userId);
}
