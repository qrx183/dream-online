package com.xuecheng.content.model.dto;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel(value="AddCourseTeacherDto", description="新增课程教师信息")
public class AddCourseTeacherDto {
    @NotEmpty(message = "新增课程Id不能为空")
    private long courseId;
    @NotEmpty(message = "新增课程教师姓名不能为空")
    private String teacherName;
    @NotEmpty(message = "新增课程教师职位不能为空")
    private String position;
    @NotEmpty(message = "新增课程教师简介不能为空")
    private String introduction;
}
