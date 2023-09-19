package nnu.wyz.controller;


import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.service.IDscUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author wyz
 * @since 2023-08-16
 */
@RestController
@RequestMapping("/oauth")
public class DscUserAuthController {
    @Autowired
    private IDscUserAuthService iDscUserAuthService;
    @PostMapping("/token")
    public CommonResult<JSONObject> login(@ApiIgnore Principal principal,
                                          @ApiIgnore @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        return iDscUserAuthService.login(principal, parameters);

    }

    @GetMapping("/test")
    public String test(@ApiIgnore Principal principal,
                       @ApiIgnore @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
//        System.out.println("tokenEndpoint = " + tokenEndpoint.postAccessToken(principal, parameters).getBody());
//        return tokenEndpoint.postAccessToken(principal, parameters).getBody()

        return "1111";
    }
}
