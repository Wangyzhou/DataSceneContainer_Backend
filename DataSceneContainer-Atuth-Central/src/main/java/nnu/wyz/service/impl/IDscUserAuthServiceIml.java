package nnu.wyz.service.impl;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.dao.DscUserDAO;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.entity.DscUser;
import nnu.wyz.service.IDscUserAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/22 14:53
 */
@Service
public class IDscUserAuthServiceIml implements IDscUserAuthService {

    @Autowired
    private TokenEndpoint tokenEndpoint;

    @Autowired
    PasswordEncoder bcryptPasswordEncoder;

    @Autowired
    private DscUserDAO dscUserDAO;

    @Override
    public CommonResult<JSONObject> login(Principal principal, Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        HashMap<String, Object> returnToken = new HashMap<>();
        if (parameters.get("grant_type").equals("refresh_token")) {
            //TODO: 判断刷新令牌是否过期
            returnToken.put("access_token", Objects.requireNonNull(tokenEndpoint.postAccessToken(principal, parameters).getBody()).getValue());
            returnToken.put("refresh_token", Objects.requireNonNull(tokenEndpoint.postAccessToken(principal, parameters).getBody()).getRefreshToken().toString());
            return CommonResult.success(new JSONObject(returnToken), "刷新成功！");
        }
        String email = parameters.get("username");
        DscUser dscUser = dscUserDAO.findDscUserByEmail(email);
        if (Objects.isNull(dscUser)) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, "用户不存在，请进行注册！");
        }
        if (dscUser.getEnabled() == 0) {
            return CommonResult.failed(ResultCode.FORBIDDEN, "用户未激活，请使用注册邮箱中的激活邮件激活账户！");
        }
        boolean isMatched = bcryptPasswordEncoder.matches(parameters.get("password"), dscUser.getPassword());
        if (!isMatched) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, "密码错误！");
        }
        returnToken.put("access_token", Objects.requireNonNull(tokenEndpoint.postAccessToken(principal, parameters).getBody()).getValue());
        returnToken.put("refresh_token", Objects.requireNonNull(tokenEndpoint.postAccessToken(principal, parameters).getBody()).getRefreshToken().toString());
        return CommonResult.success(new JSONObject(returnToken), "登录成功！");
    }
}
