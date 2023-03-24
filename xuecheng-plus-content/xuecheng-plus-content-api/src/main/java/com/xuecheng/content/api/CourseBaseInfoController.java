package com.xuecheng.content.api;

import com.spring4all.swagger.EnableSwagger2Doc;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.exception.ValidationGroups;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */

@Api(value="课程信息管理接口",tags = "课程信息管理接口")
@RestController // Controller 和 ResponseBody 返回json数据
@EnableTransactionManagement
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;


    @ApiOperation(value = "课程分页查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams,queryCourseParamsDto);
        return courseBasePageResult;
    }

    @ApiOperation(value = "新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto creaseCourseBase(@RequestBody @Validated(value = ValidationGroups.Insert.class) AddCourseDto addCourseDto) {

        long companyId = 1232141425L;

        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }

    @ApiOperation(value = "根据课程id查询接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable @ApiParam(value = "课程id") long courseId) {
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation(value = "根据课程id更新")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(value = ValidationGroups.Modify.class) EditCourseDto editCourseDto) {
        long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId,editCourseDto);
    }

    @ApiOperation(value = "删除课程")
    @DeleteMapping("/course/{courseId}")
    public DeleteCourseResponseDto deleteCourse(@PathVariable long courseId) {
        return courseBaseInfoService.deleteCourse(courseId);
    }

}
