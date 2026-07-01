package com.campus.help.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.help.server.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务 Mapper — 替代 Android 端 TaskDao (Room @Dao)。
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
