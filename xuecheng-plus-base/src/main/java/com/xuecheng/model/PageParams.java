package com.xuecheng.model;


/**
 *  页面信息请求
 */
public class PageParams {

    private long pageNo = 1L;
    private long pageSize = 30L;

    public PageParams() {

    }
    public PageParams(long pageNo, long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
}
