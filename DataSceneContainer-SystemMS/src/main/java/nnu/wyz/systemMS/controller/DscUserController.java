package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.model.entity.DscUser;
import nnu.wyz.systemMS.service.DscUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 测试用接口
     * @param userId
     */
    @ApiOperation(value = "查询个人信息")
    @GetMapping(value = "/getUserInfo/{userId}")
    public CommonResult<DscUser> getUserInfo(@PathVariable("userId") String userId) {
        return dscUserService.getUserInfo(userId);
    }

    @ApiOperation(value = "修改用户信息")
    @PostMapping(value = "/updateUserInfo")
    public CommonResult<ReturnUserUpdateDTO> updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
        return dscUserService.updateUserInfo(userUpdateDTO);
    }

    @ApiOperation(value = "修改用户头像")
    @PostMapping(value = "/updateUserAvatar")
    public CommonResult<String> updateUserAvatar(@RequestParam("userId") String userId,
                                                 @RequestParam("avatar") MultipartFile avatar) {
        System.out.println(userId);
        return dscUserService.updateUserAvatar(userId, avatar);
    }
}
