package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.dto.DeleteCourseResponseDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "课程教师信息管理")
@RestController
public class CourseTeacherInfoController {

    @Autowired
    CourseTeacherInfoService courseTeacherInfoService;

    @ApiOperation("课程教师列表查询")
    @GetMapping("/courseTeacher/list/#{courseId}")
    public List<CourseTeacher> list(@PathVariable long courseId) {
        return courseTeacherInfoService.getCourseTeachList(courseId);
    }

    @ApiOperation("课程教师信息添加")
    @PostMapping("/courseTeacher")
    public CourseTeacher addCourseTeacher(AddCourseTeacherDto addCourseTeacherDto) {
        long companyId = 1232141425L;
        return courseTeacherInfoService.addCourseTeacher(companyId, addCourseTeacherDto);
    }

    @ApiOperation("课程教师信息修改")
    @PutMapping("/courseTeacher")
    public CourseTeacher modifyCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        long companyId = 1232141425L;
        return courseTeacherInfoService.modifyCourseTeacher(companyId,courseTeacher);
    }
    
    @ApiOperation("课程教师信息删除")
    @DeleteMapping("/courseTeacher/course/{courseId}/{id}")
    public DeleteCourseResponseDto deleteCourseTeacher(@PathVariable long courseId,@PathVariable long id) {
        long companyId = 1232141425L;
        return courseTeacherInfoService.deleteCourseTeacher(companyId,courseId,id);
    }
}
