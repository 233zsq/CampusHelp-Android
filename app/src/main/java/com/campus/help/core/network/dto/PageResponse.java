package com.campus.help.core.network.dto;

import java.util.List;

/**
 * 后端分页响应（对应 MyBatis-Plus IPage 的 JSON 结构）。
 * GET /api/tasks 返回 { records, total, size, current, pages }。
 */
public class PageResponse<T> {

    public List<T> records;
    public long total;
    public long size;
    public long current;
    public long pages;
}
