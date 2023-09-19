package nnu.wyz.userMS.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.userMS.entity.dto.UserRegisterDTO;
import nnu.wyz.userMS.service.IDscUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wyz
 * @since 2023-08-18
 */
@RestController
@RequestMapping("/dsc-user")
@Api(value = "用户Controller", tags = "用户接口")
public class DscUserController {
    @Autowired
    private IDscUserService iDscUserService;

    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public CommonResult<String> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        return iDscUserService.register(userRegisterDTO);
    }
    @ApiOperation(value = "用户激活")
    @GetMapping("/active/{activeCode}")
    public CommonResult<String> active(@PathVariable("activeCode")String code) {
        return iDscUserService.active(code);
    }

    @ApiOperation(value = "用户注销")
    @GetMapping("/logout")
    public CommonResult<String> logout(@RequestHeader("Authorization")String token) throws JsonProcessingException {
        return iDscUserService.logout(token);
    }
}
