package com.xuecheng.learning.service.Impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.StringUtil;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    MyCourseTableService myCourseTableService;
    
    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(Long courseId, Long teachplanId, String userId, String mediaId) {
        // 首先获取学习资格
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish == null) {
            return RestResponse.validfail("课程不存在");
        }

        // 获取课程计划查询该课程是否支持试学
        Teachplan teachplan = JSON.parseObject(coursepublish.getTeachplan(),Teachplan.class);
        if(teachplan.getIsPreview().equals("1")) {
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        if (!StringUtil.isEmpty(userId)) {
            XcCourseTablesDto learningStatus = myCourseTableService.getLearningStatus(userId, courseId);

            if (learningStatus.getLearnStatus().equals("702002")) {
                return RestResponse.validfail("无法学习,因为没有选课或没有支付");
            }else if (learningStatus.getLearnStatus().equals("702003")) {
                return RestResponse.validfail("课程已过期,需要重新支付");
            }else{
                // 返回课程的url
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);

            }
        }

        // 没有登录的情况下需要判断课程是否是免费的,如果是免费的,则可以以游客的方式学习
        String charge = coursepublish.getCharge();
        if("201000".equals(charge)) {
            // 课程免费,返回课程url
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }else{
            return RestResponse.validfail("该课程为付费课程,请登录后确认已支付再进行学习");
        }

    }
}
