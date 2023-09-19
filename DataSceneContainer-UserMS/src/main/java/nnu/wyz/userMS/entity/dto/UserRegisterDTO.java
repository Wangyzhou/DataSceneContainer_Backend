package nnu.wyz.userMS.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.Value;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/18 20:42
 */
@Data
@ApiModel(value = "UserRegisterDTO", description = "用户注册实体")
public class UserRegisterDTO {

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "机构")
    private String institution;
}
