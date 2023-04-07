package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.Q
 * @version 1.0
 * @description TODO
 * @date 2023/2/11 15:44
 */
@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程分页查询接口")
    @PostMapping("/course/list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required=false) QueryCourseParamsDto queryCourseParamsDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        long companyId = Long.parseLong(user.getCompanyId());
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(companyId,pageParams, queryCourseParamsDto);

        return courseBasePageResult;

    }
    @ApiOperation("新增课程")
    @PostMapping("/course")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_add')")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Inster.class) AddCourseDto addCourseDto){

        //获取到用户所属机构的id
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        long companyId = Long.parseLong(user.getCompanyId());

        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }

    @ApiOperation("根据课程id查询接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }


    @ApiOperation("修改课程")
    @PutMapping("/course")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_base')")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto){
        //获取到用户所属机构的id
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        long companyId = Long.parseLong(user.getCompanyId());
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
        return courseBaseInfoDto;
    }
    @ApiOperation(value = "删除课程")
    @DeleteMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_del')")
    public DeleteCourseResponseDto deleteCourse(@PathVariable long courseId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        long companyId = Long.parseLong(user.getCompanyId());
        return courseBaseInfoService.deleteCourse(courseId,companyId);
    }


}
