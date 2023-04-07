package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("password_auth_service")
public class PasswordAuthService implements AuthService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String username = authParamsDto.getUsername();

        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));

        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        // 校验验证码
        String checkNode = authParamsDto.getCheckcode();
        String checkNodeKey = authParamsDto.getCheckcodekey();

        Boolean verify = checkCodeClient.verify(checkNodeKey, checkNode);
        if (verify == null || !verify) {
            throw new RuntimeException("验证码输入错误");
        }
        String passwordDB = user.getPassword();
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        String password = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(password, passwordDB);
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }
        return xcUserExt;
    }
}
