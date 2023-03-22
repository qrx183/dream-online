package com.xuecheng.content.api;

import com.spring4all.swagger.EnableSwagger2Doc;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */

@Api(value="课程信息管理接口",tags = "课程信息管理接口")
@RestController // Controller 和 ResponseBody 返回json数据
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;


    @ApiOperation(value = "课程信息查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams,queryCourseParamsDto);
        return courseBasePageResult;
    }

    @ApiOperation(value = "新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto creaseCourseBase(@RequestBody AddCourseDto addCourseDto) {

        long companyId = 1232141425L;

        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }
}
