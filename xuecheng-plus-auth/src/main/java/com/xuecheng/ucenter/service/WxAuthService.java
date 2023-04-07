package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

public interface WxAuthService {

    /**
     *
     * @param code
     * @return
     */
    public XcUser WxAuth(String code);
}
