package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

public interface MyCourseTableService {
    XcChooseCourseDto addChooseCourse(String userId,long courseId);

    XcCourseTablesDto getLearningStatus(String userId,long courseId);

    boolean saveChooseCourseSuccess(String chooseCourseId);

    PageResult<XcCourseTables> myCourseTables(MyCourseTableParams tableParams);
}
