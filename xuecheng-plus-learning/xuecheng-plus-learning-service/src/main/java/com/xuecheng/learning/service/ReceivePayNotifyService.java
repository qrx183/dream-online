package com.xuecheng.learning.service;

import com.xuecheng.learning.config.PayNotifyConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public interface ReceivePayNotifyService {
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    void receive(Message message);
}
