package com.atguigu.spzx.manager.Service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.atguigu.spzx.manager.Service.ValidateCodeService;
import com.atguigu.spzx.model.vo.system.ValidateCodeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ValidateCodeServiceImpl implements ValidateCodeService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Override
    public ValidateCodeVo generateValidateCode() {
        //1. 通过工具生成图片验证码 hutool
        //int width, int height, int codeCount, int circleCount
        // codeCount = 验证码的位数, 比如 是4位数还是 6 位数的验证码
        // circleCount = 干扰线
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(150, 48, 4, 2);
        String code = circleCaptcha.getCode();  //生成的验证码的值
        String imageBase64 = circleCaptcha.getImageBase64();    //生成验证码的图片(但是被编码成值了, 其实他是图片)

        //2. 要把验证码存储到redis里面, 设置redis的key以及value
            //  redis key = uuid    value = 验证码值
            // 设置过期时间
        String key = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set("user:validate"+key, code, 5, TimeUnit.MINUTES);

        //返回validateCodeVo对象
        ValidateCodeVo validateCodeVo = new ValidateCodeVo();
        validateCodeVo.setCodeKey(key);
        validateCodeVo.setCodeValue("data:image/png;base64," + imageBase64);


        return validateCodeVo;
    }
}
