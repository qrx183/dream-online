package com.xuecheng.learning.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MyCourseTableServiceImpl implements MyCourseTableService {
    @Autowired
    private XcCourseTablesMapper xcCourseTablesMapper;
    @Autowired
    private XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, long courseId) {

        // 获取待选课的课程
        CoursePublish coursePublish = contentServiceClient.getCoursepublish(courseId);
        if (coursePublish == null) {
            XueChengPlusException.cast("课程不存在");
        }
        String charge = coursePublish.getCharge();

        // 按照课程价格进行分类添加
        XcChooseCourse chooseCourse = new XcChooseCourse();
        if ("201000".equals(charge)){
            // 添加免费课程
            // 添加课程到选课表
            chooseCourse = addFreeCourse(userId,coursePublish);

            XcCourseTables xcCourseTables = addCourseTables(chooseCourse);
            // 添加课程到我的课程表
        }else{
            // 添加收费课程
            // 添加课程到选课表
            chooseCourse = addChargeCourse(userId,coursePublish);
            // 收费课程应该在支付完成后才能保存到我的课表,在支付完成后通过mq获取支付成功的通知后再将收费课程存到我的课表
        }
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        XcCourseTablesDto courseTablesDto = getLearningStatus(userId,courseId);
        BeanUtils.copyProperties(chooseCourse,xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(courseTablesDto.learnStatus);
        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, long courseId) {
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getCourseId,courseId)
                .eq(XcCourseTables::getUserId,userId));
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        if (xcCourseTables == null) {
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        boolean before = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if (before) {
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
        xcCourseTablesDto.setLearnStatus("702001");
        BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
        return xcCourseTablesDto;
    }

    @Override
    public boolean saveChooseCourseSuccess(String chooseCourseId) {
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
        if (xcChooseCourse == null) {
            log.debug("接收购买课程的消息,根据选课id从数据库找不到选课记录,选课id:{}",chooseCourseId);
            return false;
        }
        if (xcChooseCourse.getStatus().equals("701002")) {
            // 更新选课状态为选课成功
            xcChooseCourse.setStatus("701001");
            int i = xcChooseCourseMapper.updateById(xcChooseCourse);
            if (i <= 0) {
                log.debug("添加选课失败,{}",xcChooseCourse);
                XueChengPlusException.cast("更新选课状态失败");
            }
            XcCourseTables xcCourseTables = addCourseTables(xcChooseCourse);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<XcCourseTables> myCourseTables(MyCourseTableParams tableParams) {
        int page = tableParams.getPage();
        int size = tableParams.getSize();
        Page<XcCourseTables> courseTablesPage = new Page<>(page,size);
        LambdaQueryWrapper<XcCourseTables> xcCourseTablesLambdaQueryWrapper = new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId,tableParams.getUserId());
        Page<XcCourseTables> result = xcCourseTablesMapper.selectPage(courseTablesPage,xcCourseTablesLambdaQueryWrapper);
        List<XcCourseTables> records = result.getRecords();
        long count = result.getTotal();
        return new PageResult<>(records,count,page,size);
    }

    private XcChooseCourse addFreeCourse(String userId,CoursePublish coursePublish) {
        // 判断选课表中会否已经存在该课程
        long courseId = coursePublish.getId();
        List<XcChooseCourse> xcChooseCourse = xcChooseCourseMapper.selectList(new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getCourseId,courseId)
                .eq(XcChooseCourse::getOrderType,"700001") // 免费课程
                .eq(XcChooseCourse::getStatus,"701001") // 选课成功
        );
        if(xcChooseCourse != null && xcChooseCourse.size() != 0) {
            return xcChooseCourse.get(0);
        }
        XcChooseCourse chooseCourse = new XcChooseCourse();
        chooseCourse.setCourseId(courseId);
        chooseCourse.setUserId(userId);
        chooseCourse.setCourseName(coursePublish.getName());
        chooseCourse.setCompanyId(coursePublish.getCompanyId());
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setOrderType("700001");
        chooseCourse.setStatus("701001");
        chooseCourse.setValidDays(365);
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int i = xcChooseCourseMapper.insert(chooseCourse);
        if (i <= 0) {
            XueChengPlusException.cast("添加选课记录失败");
        }
        return chooseCourse;

    }

    private XcChooseCourse addChargeCourse(String userId,CoursePublish coursePublish) {
        // 判断选课表中会否已经存在该课程
        long courseId = coursePublish.getId();
        List<XcChooseCourse> xcChooseCourse = xcChooseCourseMapper.selectList(new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getCourseId,courseId)
                .eq(XcChooseCourse::getOrderType,"700002") // 收费课程
                .eq(XcChooseCourse::getStatus,"701002") // 选课状态为待支付
        );
        if(xcChooseCourse != null && xcChooseCourse.size() != 0) {
            return xcChooseCourse.get(0);
        }
        XcChooseCourse chooseCourse = new XcChooseCourse();
        chooseCourse.setCourseId(courseId);
        chooseCourse.setUserId(userId);
        chooseCourse.setCourseName(coursePublish.getName());
        chooseCourse.setCompanyId(coursePublish.getCompanyId());
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setOrderType("700002");
        chooseCourse.setStatus("701002");
        chooseCourse.setValidDays(365);
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int i = xcChooseCourseMapper.insert(chooseCourse);
        if (i <= 0) {
            XueChengPlusException.cast("添加选课记录失败");
        }
        return chooseCourse;
    }

    private XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse) {
        if (!"701001".equals(xcChooseCourse.getStatus())){
            XueChengPlusException.cast("选课失败无法添加到我的课程表");
        }
        
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getCourseId,xcChooseCourse.getCourseId())
                .eq(XcCourseTables::getUserId,xcChooseCourse.getUserId()));
        if (xcCourseTables != null) {
            XueChengPlusException.cast("我的课程表中已经存在这门课程,无需重复添加");
        }
        XcCourseTables courseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse,courseTables);

        courseTables.setChooseCourseId(xcChooseCourse.getId());
        courseTables.setCourseType(xcChooseCourse.getOrderType());
        courseTables.setUpdateDate(LocalDateTime.now());
        int i = xcCourseTablesMapper.insert(courseTables);
        if (i <= 0) {
            XueChengPlusException.cast("课程添加到我的课程表失败");
        }
        return courseTables;
    }
}
