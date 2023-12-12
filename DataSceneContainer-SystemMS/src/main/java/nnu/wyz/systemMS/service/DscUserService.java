package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.ReturnUsersByEmailLikeDTO;
import nnu.wyz.systemMS.model.dto.UserLoginDTO;
import nnu.wyz.systemMS.model.dto.UserRegisterDTO;
import nnu.wyz.systemMS.model.entity.DscUser;

import java.util.List;

public interface DscUserService {
    CommonResult<String> register(UserRegisterDTO userRegisterDTO);

    CommonResult<JSONObject> login(UserLoginDTO userLoginDTO);

    CommonResult<String> active(String code);

    CommonResult<String> logout(String token) throws JsonProcessingException;

    CommonResult<String> sendCode2Email(String email);

    CommonResult<JSONObject> validateCode(String email, String code);

    CommonResult<String> resetPassword(String resetToken,String email, String password);

    CommonResult<List<ReturnUsersByEmailLikeDTO>> getUserByEmailLike(String keyWord);

    CommonResult<DscUser> getUserInfo(String userId);

}
