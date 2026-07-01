package com.campus.help.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.help.server.entity.CreditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 信用记录 Mapper — 替代 Android 端 CreditDao (Room @Dao)。
 */
@Mapper
public interface CreditRecordMapper extends BaseMapper<CreditRecord> {

    /**
     * 求用户信用分总变动量（SQL 聚合，避免全表加载到内存）。
     */
    @Select("SELECT IFNULL(SUM(delta), 0) FROM credit_record WHERE user_id = #{userId} AND deleted = 0")
    int sumDeltaByUser(@Param("userId") Long userId);
}
