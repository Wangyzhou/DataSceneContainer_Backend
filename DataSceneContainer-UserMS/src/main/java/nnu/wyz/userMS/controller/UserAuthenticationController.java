package nnu.wyz.userMS.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/17 15:27
 */
@RestController
@RequestMapping
@Api(value = "用户登录注册等", tags = {"用户接口"})
public class UserAuthenticationController {

    @GetMapping(value = "/test")
    public String testUser() {
        return "测试用户微服务成功！";
    }
    @GetMapping(value = "/hello")
    public String testHello() {
        return "hello";
    }
}
