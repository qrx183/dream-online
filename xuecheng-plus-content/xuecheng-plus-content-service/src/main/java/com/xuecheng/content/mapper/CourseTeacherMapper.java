package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseTeacherMapper extends BaseMapper<CourseTeacher> {
    public List<CourseTeacher> selectByCourseId(long id);

    public int deleteByCourseIdAndId(long courseId,long id);

    public int deleteByCourseId(long courseId);
}
