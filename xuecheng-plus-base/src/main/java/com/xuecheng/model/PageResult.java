package com.xuecheng.model;

import java.io.Serializable;
import java.util.List;

/**
 * 根据页数及其查询条件得到的查询结果
 * @param <T>
 */
public class PageResult<T> implements Serializable {

    private List<T> items;

    private long counts;

    private long page;
    private long pageSize;

    public PageResult(List<T> items, long counts, long page, long pageSize) {
        this.items = items;
        this.counts = counts;
        this.page = page;
        this.pageSize = pageSize;
    }
}
