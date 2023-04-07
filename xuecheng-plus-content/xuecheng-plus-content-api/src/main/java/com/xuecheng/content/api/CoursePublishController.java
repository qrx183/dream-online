package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author Mr.Q
 * @version 1.0
 * @description 课程发布
 * @date 2023/2/21 9:28
 */
@Controller
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;


    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId) {
//        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        CoursePublish coursePublish = coursePublishService.getCoursePublishByCache(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        if (coursePublish == null) {
            return coursePreviewDto;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBaseInfoDto);
        List<TeachplanDto> teachplanDtos = JSON.parseArray(coursePublish.getTeachplan(), TeachplanDto.class);
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachplanDtos);
        return coursePreviewDto;
    }

    @ApiOperation("课程详情页面静态初始化")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {

        ModelAndView modelAndView = new ModelAndView();
        //查询课程的信息作为模型数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        //指定模型
        modelAndView.addObject("model", coursePreviewInfo);
        //指定模板
        modelAndView.setViewName("course_template");//根据视图名称加.ftl找到模板
        return modelAndView;
    }

    @ApiOperation("课程审核")
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        long companyId = Long.parseLong(user.getCompanyId());
        coursePublishService.commitAudit(companyId, courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        long companyId = Long.parseLong(user.getCompanyId());
        coursePublishService.publish(companyId, courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursePublish(@PathVariable("courseId") long courseId) {
        return coursePublishService.getCoursePublish(courseId);
    }


}
