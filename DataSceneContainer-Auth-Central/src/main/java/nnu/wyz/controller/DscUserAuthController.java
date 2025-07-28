package nnu.wyz.controller;


import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.entity.dto.RefreshTokenDTO;
import nnu.wyz.service.IDscUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import java.security.interfaces.RSAPublicKey;
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

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Autowired
    private RSAPublicKey publicKey;
    @PostMapping("/token")
    public CommonResult<JSONObject> login(@ApiIgnore Principal principal,
                                          @ApiIgnore @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        return iDscUserAuthService.login(principal, parameters);

    }

    @PostMapping("/check_token")
    public CommonResult<?> checkToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            // 使用你的公钥构建解码器
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

            Jwt jwt = decoder.decode(token);

            return CommonResult.success(jwt.getClaims()); // 返回 payload
        } catch (JwtException e) {
            if (e instanceof JwtValidationException || e.getMessage().contains("expired")) {
                return CommonResult.unauthorized("expired token: " + e.getMessage());
            }else {
                return CommonResult.failed("invalid token:" + e.getMessage());
            }

        }
    }

    @PostMapping("/refresh_token")
    public CommonResult<?> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) throws HttpRequestMethodNotSupportedException {
        return iDscUserAuthService.refreshToken(refreshTokenDTO);
    }

    @GetMapping("/test")
    public String test(@ApiIgnore Principal principal,
                       @ApiIgnore @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
//        System.out.println("tokenEndpoint = " + tokenEndpoint.postAccessToken(principal, parameters).getBody());
//        return tokenEndpoint.postAccessToken(principal, parameters).getBody()

        return "1111";
    }
}
