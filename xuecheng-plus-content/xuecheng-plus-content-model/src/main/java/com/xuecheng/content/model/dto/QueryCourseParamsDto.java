package com.xuecheng.content.model.dto;


/**
 * 课程查询条件参数设置
 */
public class QueryCourseParamsDto {


    private String auditStatus; // 审核状态
    private String courseName;  // 课程姓名
    private String publishStatus;   // 发布状态

    public QueryCourseParamsDto() {

    }

    public QueryCourseParamsDto(String auditStatus, String courseName, String publishStatus) {
        this.auditStatus = auditStatus;
        this.courseName = courseName;
        this.publishStatus = publishStatus;
    }
}
