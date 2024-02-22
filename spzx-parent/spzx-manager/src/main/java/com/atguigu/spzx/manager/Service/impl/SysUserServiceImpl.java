package com.atguigu.spzx.manager.Service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.comoon.exception.GuiguException;
import com.atguigu.spzx.manager.Service.SysUserService;
import com.atguigu.spzx.manager.mapper.SysUserMapper;
import com.atguigu.spzx.model.dto.system.LoginDto;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.system.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper ;

    @Autowired
    private RedisTemplate<String , String> redisTemplate ;

    //用户退出

    @Override
    public SysUser getUserInfo(String token) {
        // 从 redis里面获取token 对比token已获得user
        String userJsonData = redisTemplate.opsForValue().get("user:login" + token);
        SysUser sysUser = JSON.parseObject(userJsonData, SysUser.class);          //这个就是把userJsonData里面的转换成 SysUser的实体类

        return sysUser;
    }

    @Override
    public LoginVo login(LoginDto loginDto) {
        /**
         * 1. 获取验证码和存储到redis的key名称 loginDto获取
         * 2. 根据获取的redis里面key,查询redis里面存储的验证码
         * 3. 对比输入的验证码和redis存储验证码是否一致
         * 4. 如果不一致, 报错, 校正失败,
         * 5. 如果一致, 删除redis里面的验证码
         */
        String captcha = loginDto.getCaptcha();
        String codeKey = loginDto.getCodeKey();
        String redis_captcha = redisTemplate.opsForValue().get("user:validate" + codeKey);

        //这里对比redis_captcha是否为空 是因为怕token过期了
        if(StrUtil.isEmpty(redis_captcha) || !StrUtil.equalsIgnoreCase(redis_captcha , captcha)) {
            throw new GuiguException(ResultCodeEnum.VALIDATECODE_ERROR) ;
        }

        redisTemplate.delete("user:validate" + codeKey);




        // 1. 获取提交用户名, loginDto获取到
        String userName = loginDto.getUserName();

        //2. 根据用户名查询数据库表 sys_user表
        SysUser sysUser = sysUserMapper.seletUserInfoByUserName(userName);

        //3. 如果根据用户名查找不到对应信息, 用户不存在, 返回错误信息
        if(sysUser == null) {
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }

        //4 如果根据用户名查询到用户信息, 用户存在
        //5 获取输入的密码, 比较输入的密码和数据库密码是否一致
        String databaes_password = sysUser.getPassword();
        String input_password = loginDto.getPassword();
        String encrypt_password = DigestUtils.md5DigestAsHex(input_password.getBytes());

        //6. 如果密码一致, 登入成功, 如果密码不一致 登入失败
        if (! encrypt_password.equals(databaes_password)) {
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }

        //7 登入成功, 生成用户唯一标识token
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        //8 把登入成功用户信息放到redis里面
        redisTemplate.opsForValue().set("user:login" + token,
                JSON.toJSONString(sysUser),         //这里就是把user转换成了Json的形式来保存
                14,
                TimeUnit.DAYS);

        //9 返回loginvo对象
        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);

        return loginVo;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete("user:login"+token);
    }
}