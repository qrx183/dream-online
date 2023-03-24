package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.DeleteCourseResponseDto;
import com.xuecheng.content.model.dto.DeleteTeachPlanDto;
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

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

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

    @Override
    public DeleteCourseResponseDto deleteTeachPlan(long id) {

        List<DeleteTeachPlanDto> deleteTeachPlanDtos = teachplanMapper.selectDeleteTreeNode(id);
        DeleteTeachPlanDto deleteTeachPlanDto = deleteTeachPlanDtos.get(0);
        if (deleteTeachPlanDto.getParentid() == 0) {
            // 大章节
            // 判断有没有子节点,有则不能删除
            if (deleteTeachPlanDto.getDeleteTeachPlanDtoList() == null) {
                int i = teachplanMapper.deleteById(id);
                if (i <= 0) {
                    XcPlusException.cast("删除课程章节失败");
                }
            }else{
                return new DeleteCourseResponseDto("120409","课程计划信息还有子级信息，无法操作");
            }
        }else{
            // 小章节
            int i = teachplanMapper.deleteById(id);
            if (i <= 0) {
                XcPlusException.cast("删除课程章节失败");
            }
            i = teachplanMediaMapper.deleteByTeachplanId(id);
            if (i <= 0) {
                XcPlusException.cast("删除课程章节失败");
            }

        }
        return new DeleteCourseResponseDto("200","");

    }

    @Override
    public void moveTeachPlan(String moveDirection, long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        int orderId = teachplan.getOrderby();
        Teachplan teachplanSwap = new Teachplan();
        if ("movedown".equals(moveDirection)){
           teachplanSwap = teachplanMapper.selectByOrderById(orderId+1);
        }else if("moveup".equals(moveDirection)){
            teachplanSwap = teachplanMapper.selectByOrderById(orderId-1);
        }
        if (teachplanSwap == null) {
            XcPlusException.cast("课程计划移动失败");
        }
        int tempId = teachplanSwap.getOrderby();
        teachplan.setOrderby(tempId);
        teachplanSwap.setOrderby(orderId);
        int i = teachplanMapper.updateById(teachplan);
        int j = teachplanMapper.updateById(teachplanSwap);
        if (i <= 0 || j <= 0) {
            XcPlusException.cast("课程计划移动失败");
        }
    }
}
