package com.atguigu.spzx.manager.Controller;

import com.atguigu.spzx.manager.Service.SysUserService;
import com.atguigu.spzx.manager.Service.ValidateCodeService;
import com.atguigu.spzx.model.dto.system.LoginDto;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.system.LoginVo;
import com.atguigu.spzx.model.vo.system.ValidateCodeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户接口")
@RestController
@RequestMapping(value = "/admin/system/index")
public class IndexController {

    @Autowired
    private SysUserService sysUserService ;

    @Autowired
    private ValidateCodeService validateCodeService;

    @GetMapping(value = "/logout")
    public Result logout(@RequestHeader(name="token") String token) {
        sysUserService.logout(token);

        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    //生成图片验证码
    @GetMapping(value = "/generateValidateCode")
    public Result<ValidateCodeVo> generateValidateCode() {
        ValidateCodeVo validateCodeVo =  validateCodeService.generateValidateCode();

        return Result.build(validateCodeVo, ResultCodeEnum.SUCCESS);
    }

    //获取当前登入用户信息
    @GetMapping("/getUserInfo")
    public Result<SysUser> getUserInfo(@RequestHeader(name="token") String token) {
        //1. 从请求头获取token

        //2. 根据token查询redis获取用户信息
        SysUser sysUser = sysUserService.getUserInfo(token);
        //3. 用户信息返回

        return Result.build(sysUser, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "登录接口")
    @PostMapping(value = "/login")
    public Result<LoginVo> login(@RequestBody LoginDto loginDto) {
        LoginVo loginVo = sysUserService.login(loginDto) ;
        return Result.build(loginVo , ResultCodeEnum.SUCCESS) ;
    }



}
