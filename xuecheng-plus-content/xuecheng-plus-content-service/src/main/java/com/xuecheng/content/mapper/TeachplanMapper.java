package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.DeleteTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    public List<TeachPlanDto> selectTreeNode(long id);

    public List<DeleteTeachPlanDto> selectDeleteTreeNode(long id);
    
    public Teachplan selectByOrderById(int id);

    public int deleteByCourseId(long courseId);
}
