package nnu.wyz.userMS.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.userMS.entity.dto.UserRegisterDTO;

public interface IDscUserService {
    CommonResult<String> register(UserRegisterDTO userRegisterDTO);

    CommonResult<String> active(String code);

    CommonResult<String> logout(String token) throws JsonProcessingException;
}
