package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.ReturnUsersByEmailLikeDTO;
import nnu.wyz.systemMS.model.dto.UserLoginDTO;
import nnu.wyz.systemMS.model.dto.UserRegisterDTO;
import nnu.wyz.systemMS.service.DscUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/28 20:32
 */
@RestController
@RequestMapping(value = "/dsc-user")
@Api(value = "DscUserController", tags = "用户接口")
public class DscUserController {

    @Autowired
    private DscUserService dscUserService;

    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public CommonResult<String> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        return dscUserService.register(userRegisterDTO);
    }

    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public CommonResult<JSONObject> login(@RequestBody UserLoginDTO userLoginDTO) {
        return dscUserService.login(userLoginDTO);
    }

    @ApiOperation(value = "用户激活")
    @GetMapping("/active/{activeCode}")
    public CommonResult<String> active(@PathVariable("activeCode") String code) {
        return dscUserService.active(code);
    }

    @ApiOperation(value = "用户注销")
    @GetMapping("/logout")
    public CommonResult<String> logout(@RequestHeader("Authorization") String token) throws JsonProcessingException {
        return dscUserService.logout(token);
    }

    @ApiOperation(value = "发送验证码")
    @PostMapping("/resetPassword/sendCode")
    public CommonResult<String> sendCode2Email(@RequestBody Map<String, Object> param) {
        String email = (String) param.get("email");
        return dscUserService.sendCode2Email(email);
    }

    @ApiOperation(value = "验证验证码")
    @PostMapping("/resetPassword/validateCode")
    public CommonResult<JSONObject> validateCode(
            @RequestBody Map<String, Object> param) {
        String email = (String) param.get("email");
        String code = (String) param.get("code");
        return dscUserService.validateCode(email, code);
    }

    @ApiOperation(value = "重置密码")
    @PostMapping("/resetPassword")
    public CommonResult<String> resetPassword(@RequestHeader("resetToken") String resetToken,
                                              @RequestBody Map<String, Object> param) {
        String email = (String) param.get("email");
        String password = (String) param.get("password");
        return dscUserService.resetPassword(resetToken, email, password);
    }

    @ApiOperation(value = "根据邮箱模糊查询用户")
    @GetMapping(value = "/getUserByEmailLike/{keyWord}")
    public CommonResult<List<ReturnUsersByEmailLikeDTO>> getUserByEmailLike(@PathVariable("keyWord") String keyWord) {
        return dscUserService.getUserByEmailLike(keyWord);
    }
}
