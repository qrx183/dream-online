package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.dto.DeleteCourseResponseDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherInfoService;
import com.xuecheng.exception.XcPlusException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CourseTeacherInfoServiceImpl implements CourseTeacherInfoService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Override
    public List<CourseTeacher> getCourseTeachList(long id) {
        return courseTeacherMapper.selectByCourseId(id);
    }

    @Override
    public CourseTeacher addCourseTeacher(long companyId, AddCourseTeacherDto addCourseTeacherDto) {

        CourseBase courseBase = courseBaseMapper.selectById(addCourseTeacherDto.getCourseId());
        if (companyId != courseBase.getCompanyId()) {
            XcPlusException.cast("无权限更新课程教师信息");
        }
        CourseTeacher courseTeacher = new CourseTeacher();
        BeanUtils.copyProperties(addCourseTeacherDto,courseTeacher);
        int i = courseTeacherMapper.insert(courseTeacher);
        if (i <= 0) {
            XcPlusException.cast("新增课程教师失败");
        }
        return courseTeacher;

    }

    @Override
    public CourseTeacher modifyCourseTeacher(long companyId, CourseTeacher courseTeacher) {
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacher.getCourseId());
        if (companyId != courseBase.getCompanyId()) {
            XcPlusException.cast("无权限更新课程教师信息");
        }
        CourseTeacher courseTeacherOld = courseTeacherMapper.selectById(courseTeacher.getId());
        if (courseTeacherOld == null) {
            XcPlusException.cast("课程教师信息不存在,无法更新");
        }
        BeanUtils.copyProperties(courseTeacher,courseTeacherOld);

        int i = courseTeacherMapper.updateById(courseTeacherOld);
        if (i <= 0) {
            XcPlusException.cast("课程教师信息更新失败");
        }
        return courseTeacher;
    }

    @Override
    public DeleteCourseResponseDto deleteCourseTeacher(long companyId, long courseId, long id) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (companyId != courseBase.getCompanyId()) {
            XcPlusException.cast("无权限更新课程教师信息");
        }
        int i = courseTeacherMapper.deleteByCourseIdAndId(courseId,id);
        if (i <= 0) {
            XcPlusException.cast("课程教师信息删除失败");
        }
        return new DeleteCourseResponseDto("200","");
    }
}
