package com.xuecheng.ucenter.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.html.parser.Entity;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service("wx_auth_service")
public class WechatAuthService implements AuthService, WxAuthService {


    @Autowired
    RestTemplate restTemplate;
    @Value("${wexin.appid}")
    private String appid;
    @Value("${wexin.secret)")
    private String secret;

    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    XcUserRoleMapper xcUserRoleMapper;
    @Autowired
    WechatAuthService wechatAuthService;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String username = authParamsDto.getUsername();
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }

    @Override
    public XcUser WxAuth(String code) {
        // 申请令牌
        Map<String,String> tokenInfo = getAccessToken(code);
        // 携带令牌申请用户信息
        String token = tokenInfo.get("access_token");
        String openId = tokenInfo.get("openid");
        Map<String,String> userInfo = getUserInfoByToken(token,openId);
        // 保存用户信息到数据库
        XcUser xcUser = wechatAuthService.saveUserInfo(userInfo);
        return xcUser;
    }

    private Map<String, String> getAccessToken(String code) {
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(url_template, appid, secret, code);

        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        String result = exchange.getBody();

        log.info("调用微信接口申请access_token: 返回值:{}", result);
        Map<String, String> resultMap = JSON.parseObject(result, Map.class);

        return resultMap;
    }
    private Map<String,String> getUserInfoByToken(String accessToken,String openId) {
        String url_template = "https://api.weixin.qq.com/sns/auth?access_token=%s&openid=%s";
        String url = String.format(url_template,accessToken,openId);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);

        log.info("调用微信接口申请access_token: 返回值:{}", result);
        Map<String, String> resultMap = JSON.parseObject(result, Map.class);

        return resultMap;
    }
    @Transactional
    XcUser saveUserInfo(Map<String, String> userInfo) {
        String unionId = userInfo.get("unionid");
        String nickname = userInfo.get("nickname");
        String userPic = userInfo.get("headimgurl");
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getId,unionId));
        if (xcUser != null) {
            return xcUser;
        }

        xcUser = new XcUser();
        String userId = UUID.randomUUID().toString();
        xcUser.setId(userId);
        xcUser.setWxUnionid(unionId);
        xcUser.setNickname(nickname);
        xcUser.setUsername(unionId);
        xcUser.setName(nickname);
        xcUser.setUserpic(userPic);
        xcUser.setUtype("101001");
        xcUser.setStatus("1");
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);

        XcUserRole xcUserRole = new XcUserRole();
        String roleId = UUID.randomUUID().toString();
        xcUserRole.setId(roleId);
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        xcUserRoleMapper.insert(xcUserRole);

        return xcUser;

    }
}
