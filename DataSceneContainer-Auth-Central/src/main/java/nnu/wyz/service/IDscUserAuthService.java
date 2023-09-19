package nnu.wyz.service;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.security.Principal;
import java.util.Map;

public interface IDscUserAuthService {
    CommonResult<JSONObject> login(Principal principal, Map<String, String> parameters) throws HttpRequestMethodNotSupportedException;
}
