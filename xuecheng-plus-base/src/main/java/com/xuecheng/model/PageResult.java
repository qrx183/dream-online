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

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getCounts() {
        return counts;
    }

    public void setCounts(long counts) {
        this.counts = counts;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
