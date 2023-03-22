package com.xuecheng.content.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.exception.XcPlusException;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.omg.SendingContext.RunTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        // TODO 这一块是MyBatisPlus的部分函数的用法,需要get一下
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());

        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());

        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        Page<CourseBase> result = courseBaseMapper.selectPage(page,queryWrapper);
        List<CourseBase> items = result.getRecords();
        long total = page.getTotal();

        PageResult<CourseBase> courseBasePageResult = new PageResult<>(
                items,total,pageParams.getPageNo(),pageParams.getPageSize()
        );
        return courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(long companyId, AddCourseDto dto) {

        // 参数的合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            XcPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {

            XcPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            XcPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            XcPlusException.cast("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {

            XcPlusException.cast("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            XcPlusException.cast("适应人群为空");

        }

        if (StringUtils.isBlank(dto.getCharge())) {
            XcPlusException.cast("收费规则为空");
        }

        // 向课程基本表course_base写数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        int row = courseBaseMapper.insert(courseBase);
        if (row <= 0) {
            throw new RuntimeException("course_base插入失败");
        }
        // 向课程市场表course_market写数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        courseMarket.setId(courseBase.getId());

        saveCourseMarket(courseMarket);

        return getCourseBaseInfo(courseBase.getId());
    }

    /**
     * 返回课程的信息
     * @param courseId
     * @return
     */
    private CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        // 获取课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        // 获取课程市场信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        // 组装课程基本信息和课程市场信息
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);

        // 对分类进行id-name的替换
        String mtName = courseCategoryMapper.selectById(courseBase.getMt()).getName();
        String stName = courseCategoryMapper.selectById(courseBase.getSt()).getName();

        courseBaseInfoDto.setMtName(mtName);
        courseBaseInfoDto.setStName(stName);

        return courseBaseInfoDto;
    }

    /**
     * 对市场信息进行单独处理:根据courseId是否在表中存在来判断是更新还是删除
     * @param courseMarket
     * @return
     */
    private int saveCourseMarket(CourseMarket courseMarket) {

        // 参数合法性校验
        String charge = courseMarket.getCharge();

        if (StringUtils.isEmpty(charge)){

            XcPlusException.cast("收费规则为空");
        }

        if ("201001".equals(charge)){
            if(courseMarket.getPrice() == null || courseMarket.getPrice() <= 0) {

                XcPlusException.cast("课程价格不能为空且必须大于0");
            }
        }
        Long id = courseMarket.getId();
        CourseMarket courseMarketOld = courseMarketMapper.selectById(id);
        if (courseMarketOld == null) {
            return courseMarketMapper.insert(courseMarket);
        }else{
            BeanUtils.copyProperties(courseMarket,courseMarketOld);
            courseMarketOld.setId(id);
            return courseMarketMapper.updateById(courseMarketOld);
        }
    }
}
