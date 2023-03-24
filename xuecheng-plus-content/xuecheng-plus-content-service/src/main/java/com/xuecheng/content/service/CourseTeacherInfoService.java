package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.dto.DeleteCourseResponseDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherInfoService {

    public List<CourseTeacher> getCourseTeachList(long id);

    public CourseTeacher addCourseTeacher(long companyId, AddCourseTeacherDto addCourseTeacherDto);

    public CourseTeacher modifyCourseTeacher(long companyId, CourseTeacher courseTeacher);

    public DeleteCourseResponseDto deleteCourseTeacher(long companyId, long courseId,long id);
}
