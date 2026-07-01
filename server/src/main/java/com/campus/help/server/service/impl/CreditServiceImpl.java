package com.campus.help.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.help.server.entity.CreditRecord;
import com.campus.help.server.entity.User;
import com.campus.help.server.mapper.CreditRecordMapper;
import com.campus.help.server.mapper.UserMapper;
import com.campus.help.server.service.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditRecordMapper creditRecordMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void addRecord(CreditRecord record) {
        if (record.getTimestamp() == null) {
            record.setTimestamp(System.currentTimeMillis());
        }
        creditRecordMapper.insert(record);

        // 同步更新 user 表的 creditScore
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            int newScore = user.getCreditScore() + record.getDelta();
            // 限制在 0~1000
            newScore = Math.max(0, Math.min(1000, newScore));
            user.setCreditScore(newScore);
            userMapper.updateById(user);
            log.info("信用分更新: userId={}, delta={}, newScore={}",
                    record.getUserId(), record.getDelta(), newScore);
        }
    }

    @Override
    public List<CreditRecord> listByUser(Long userId) {
        return creditRecordMapper.selectList(
                new LambdaQueryWrapper<CreditRecord>()
                        .eq(CreditRecord::getUserId, userId)
                        .orderByDesc(CreditRecord::getTimestamp)
        );
    }

    @Override
    public int sumDelta(Long userId) {
        return creditRecordMapper.sumDeltaByUser(userId);
    }
}
