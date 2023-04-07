package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper userMapper;

    @Autowired
    XcMenuMapper xcMenuMapper;
    @Autowired
    ApplicationContext applicationContext;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try{
            authParamsDto = JSON.parseObject(s,AuthParamsDto.class);
        }catch (Exception e){
            log.info("认证请求不符合项目要求:{}",s);
            throw new RuntimeException("认证请求数据格式不对");

        }

        String authType = authParamsDto.getAuthType();
        AuthService authService = applicationContext.getBean(authType + "_auth_service",AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        return getUserDetails(xcUserExt);

    }

    private UserDetails getUserDetails(XcUserExt xcUserExt) {
        String password = xcUserExt.getPassword();
        String userId = xcUserExt.getId();
        List<String> authorities = new ArrayList<>();
        List<XcMenu> xcMenuList = xcMenuMapper.selectPermissionByUserId(userId);
        if (xcMenuList.size() > 0) {
            xcMenuList.forEach(m -> {
                authorities.add(m.getCode());
            });
        }
        // 添加权限
        xcUserExt.setPermissions(authorities);
        xcUserExt.setPassword(null);
        String userJson = JSON.toJSONString(xcUserExt);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities.toArray(new String[0])).build();
        return userDetails;
    }
}
