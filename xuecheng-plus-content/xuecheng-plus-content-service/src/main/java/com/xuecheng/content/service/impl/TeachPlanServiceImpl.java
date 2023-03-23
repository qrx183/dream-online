package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.exception.XcPlusException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Override
    public List<TeachPlanDto> findTeachPlanTree(long courseId) {
        return teachplanMapper.selectTreeNode(courseId);
    }

    private int getTeachplanCount(Long courseId,Long parentId) {
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper = wrapper.eq(Teachplan::getCourseId,courseId).eq(Teachplan::getParentid,parentId);

        int count = teachplanMapper.selectCount(wrapper);
        return count+1;
    }

    @Override
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto) {
        Long teachPlanId = saveTeachPlanDto.getId();

        if (teachPlanId == null) {
            // 新增

            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachPlanDto,teachplan);

            int count = getTeachplanCount(teachPlanId,saveTeachPlanDto.getParentid());
            teachplan.setOrderby(count);

            int i = teachplanMapper.insert(teachplan);
            if (i <= 0) {
                XcPlusException.cast("新增课程计划失败");
            }


        }else{
            // 修改

            Teachplan teachplan = teachplanMapper.selectById(saveTeachPlanDto.getId());
            BeanUtils.copyProperties(saveTeachPlanDto,teachplan);

            int i = teachplanMapper.updateById(teachplan);
            if (i <= 0) {
                XcPlusException.cast("更新课程计划失败");
            }
        }
    }
}
