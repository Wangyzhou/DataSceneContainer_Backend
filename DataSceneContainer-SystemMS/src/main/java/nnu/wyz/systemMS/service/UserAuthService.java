package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;


@FeignClient(value = "dsc-auth-central")
public interface UserAuthService {

    @RequestMapping(value = "/oauth/token", method = RequestMethod.POST, headers = {"Content-type: multipart/form-data"})
    CommonResult<JSONObject> postAccessToken(@RequestBody MultiValueMap<String, String> parameters);
}
