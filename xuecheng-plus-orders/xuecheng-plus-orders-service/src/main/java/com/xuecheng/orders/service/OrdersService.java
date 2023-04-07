package com.xuecheng.orders.service;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;

public interface OrdersService {
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    XcPayRecord getPayRecordByPayNo(String payNo);

    PayRecordDto queryPayResult(String payNo);

    void saveALiPayStatus(PayStatusDto payStatusDto);

    void notifyResult(MqMessage message);
}
