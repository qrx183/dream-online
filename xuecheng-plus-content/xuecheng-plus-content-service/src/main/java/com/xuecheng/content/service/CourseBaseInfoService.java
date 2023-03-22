package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;

public interface CourseBaseInfoService {

    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    public CourseBaseInfoDto createCourseBase(long companyId, AddCourseDto addCourseDto);
}
