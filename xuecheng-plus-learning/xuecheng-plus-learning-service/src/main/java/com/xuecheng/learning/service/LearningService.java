package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

public interface LearningService {
    RestResponse<String> getVideo(Long courseId,Long teachplanId,String userId,String mediaId);
}
