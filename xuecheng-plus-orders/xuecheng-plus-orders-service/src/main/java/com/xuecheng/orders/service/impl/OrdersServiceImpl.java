package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayAcquireQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Correlation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {
    @Value("${pay.qrCode}")
    String urlFormat;

    @Autowired
    XcOrdersMapper xcOrdersMapper;

    @Autowired
    XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Autowired
    XcPayRecordMapper xcPayRecordMapper;

    @Autowired
    OrdersServiceImpl currentProxy;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Override
    @Transactional
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {

        // 一个选课对应唯一一个订单,所以需要作幂等性判断

        // 插入订单表,订单明细表
        XcOrders xcOrders = saveOrders(userId, addOrderDto);
        // 插入支付记录表
        XcPayRecord payRecord = createPayRecord(xcOrders);
        // 生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        String url = String.format(urlFormat, payRecord.getId());
        String qrCode = "";
        try {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayNo(String payNo) {
        return xcPayRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {
        // 调用支付宝接口拿到支付结果
        PayStatusDto payStatusDto = queryPayResultFromAliPay(payNo);

        // 根据支付结果更新数据表状态
        currentProxy.saveALiPayStatus(payStatusDto);
        XcPayRecord payRecord = getPayRecordByPayNo(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        return payRecordDto;
    }

    private PayStatusDto queryPayResultFromAliPay(String payNo) {
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);

        request.setBizContent(bizContent.toString());
        String body = null;
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                XueChengPlusException.cast("交易失败");
            }
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        Map<String, String> map = JSONObject.parseObject(body, Map.class);

        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(map.get("out_trade_no"));
        payStatusDto.setTrade_no(map.get("trade_no"));
        payStatusDto.setTrade_status(map.get("trade_status"));
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTotal_amount(map.get("total_moment"));

        return payStatusDto;

    }

    @Override
    @Transactional
    public void saveALiPayStatus(PayStatusDto payStatusDto) {
        String payNo = payStatusDto.getOut_trade_no();
        XcPayRecord xcPayRecord = getPayRecordByPayNo(payNo);
        if (xcPayRecord == null) {
            XueChengPlusException.cast("找不到相关的支付记录");
        }
        String status = xcPayRecord.getStatus();
        Long orderId = xcPayRecord.getOrderId();
        XcOrders orders = xcOrdersMapper.selectById(orderId);
        if (orders == null) {
            XueChengPlusException.cast("找不到相关的订单");
        }
        if ("601002".equals(status)) {
            // 查询数据库,如果支付状态已经是支付成功,则无需再更新
            return;
        }

        String tradeStatus = payStatusDto.getTrade_status();
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            xcPayRecord.setStatus("601002");
            xcPayRecord.setOutPayNo(payStatusDto.getTrade_no());
            xcPayRecord.setOutPayChannel("ALiPay");
            xcPayRecord.setPaySuccessTime(LocalDateTime.now());
            int i = xcPayRecordMapper.updateById(xcPayRecord);
            if (i <= 0) {
                XueChengPlusException.cast("更新支付记录失败");
            }
            orders.setStatus("600002");
            i = xcOrdersMapper.updateById(orders);
            if (i <= 0) {
                XueChengPlusException.cast("更新订单失败");
            }
        }


        // 将支付回复消息持久到数据库
        MqMessage mqMessage = mqMessageService.addMessage("payresult_notify",orders.getOutBusinessId(),orders.getOrderType(),null);
        // 发送消息
        notifyResult(mqMessage);

    }

    @Override
    public void notifyResult(MqMessage message) {
        String s = JSON.toJSONString(message);
        Message buildMessage = MessageBuilder.withBody(s.getBytes(StandardCharsets.UTF_8)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();

        Long id = message.getId();
        CorrelationData correlationData = new CorrelationData();
        correlationData.getFuture().addCallback(result -> {
                if (result.isAck()) {
                    log.debug("消息发送成功:{}",s);
                    mqMessageService.completed(id);
                } else {
                    log.debug("消息发送失败:{}",s);
                }
            }, ex->{
                    log.debug("消息发送异常:{}",s);
                }

        );
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT,"",buildMessage,correlationData);
    }

    private XcOrders saveOrders(String userId, AddOrderDto addOrderDto) {
        XcOrders orders = getOrdersByBusinessId(addOrderDto.getOutBusinessId());
        if (orders != null) {
            return orders;
        }

        // 插入订单表
        orders = new XcOrders();
        orders.setId(IdWorkerUtils.getInstance().nextId()); // 利用雪花算法生成订单号
        orders.setTotalPrice(addOrderDto.getTotalPrice()); // 课程总价
        orders.setCreateDate(LocalDateTime.now()); // 订单创建时间
        orders.setStatus("600001"); // 订单状态:未支付
        orders.setUserId(userId); // 用户id
        orders.setOrderType("60201"); // 订单类型
        orders.setOrderName(addOrderDto.getOrderName()); // 订单名称
        orders.setOrderDetail(addOrderDto.getOrderDetail()); // 订单详细
        orders.setOrderDescrip(addOrderDto.getOrderDescrip()); // 订单描述
        orders.setOutBusinessId(addOrderDto.getOutBusinessId()); // 订单对应的选课id
        long orderId = orders.getId();
        int i = xcOrdersMapper.insert(orders);
        if (i <= 0) {
            XueChengPlusException.cast("添加订单失败");
        }

        String ordersDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(ordersDetailJson, XcOrdersGoods.class);
        xcOrdersGoods.forEach(item -> {
            item.setOrderId(orderId);
            int insert = xcOrdersGoodsMapper.insert(item);
            if (insert <= 0) {
                XueChengPlusException.cast("添加订单明细失败");
            }
        });

        return orders;

    }

    private XcPayRecord createPayRecord(XcOrders xcOrders) {
        long orderId = xcOrders.getId();
        XcOrders orders = xcOrdersMapper.selectById(orderId);
        if (orders == null) {
            XueChengPlusException.cast("订单不存在");
        }

        if ("601002".equals(orders.getStatus())) {
            XueChengPlusException.cast("订单已支付,无需重复支付");
        }
        XcPayRecord payRecord = new XcPayRecord();
        payRecord.setPayNo(IdWorkerUtils.getInstance().nextId()); // 支付记录号,将此号传给支付宝作为订单号
        payRecord.setOrderId(orderId); // 订单号
        payRecord.setTotalPrice(orders.getTotalPrice()); // 此次支付的价格
        payRecord.setCurrency("CNY"); // 货币种类
        payRecord.setCreateDate(LocalDateTime.now()); // 支付时间
        payRecord.setStatus("601001"); // 支付状态:未支付
        payRecord.setUserId(orders.getUserId()); // 支付者Id
        int i = xcPayRecordMapper.insert(payRecord);
        if (i <= 0) {
            XueChengPlusException.cast("添加支付记录失败");
        }
        return payRecord;
    }

    private XcOrders getOrdersByBusinessId(String businessId) {
        return xcOrdersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
    }
}
