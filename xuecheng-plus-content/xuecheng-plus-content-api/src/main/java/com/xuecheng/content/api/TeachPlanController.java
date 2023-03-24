package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.DeleteCourseResponseDto;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
@RestController
public class TeachPlanController {

    @Autowired
    TeachPlanService teachPlanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("/teachplan/{courseId}/tre-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable long courseId) {
        return teachPlanService.findTeachPlanTree(courseId);
    }

    @ApiOperation("新增修改课程计划")
    @GetMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachPlanDto saveTeachPlanDto) {
        teachPlanService.saveTeachPlan(saveTeachPlanDto);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{courseId}")
    public DeleteCourseResponseDto deleteTeachplan(@PathVariable long courseId) {
        return teachPlanService.deleteTeachPlan(courseId);
    }
    
    @ApiOperation("移动课程计划")
    @PostMapping("/teachplan/{moveDirection}/{id}")
    public void moveTeachplan(@PathVariable String moveDirection,@PathVariable long id) {

    }
}
