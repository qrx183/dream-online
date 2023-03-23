package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

public interface TeachPlanService {

    public List<TeachPlanDto> findTeachPlanTree(long courseId);

    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto);
}
