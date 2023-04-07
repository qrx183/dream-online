package com.xuecheng.learning.service.Impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.service.MyCourseTableService;
import com.xuecheng.learning.service.ReceivePayNotifyService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReceivePayNotifyServiceImpl implements ReceivePayNotifyService {

    @Autowired
    MyCourseTableService myCourseTableService;

    @Override
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message) {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte[] body = message.getBody();

        String jsonString = new String(body);
        MqMessage mqMessage = JSON.parseObject(jsonString,MqMessage.class);
        String chooseCourseId = mqMessage.getBusinessKey1();
        String orderType =  mqMessage.getBusinessKey2();

        if ("60201".equals(orderType) && PayNotifyConfig.MESSAGE_TYPE.equals(mqMessage.getMessageType())
        ) {
            boolean b = myCourseTableService.saveChooseCourseSuccess(chooseCourseId);
            if(!b) {
                XueChengPlusException.cast("收到支付结果，添加选课失败");
            }
        }
    }
}
