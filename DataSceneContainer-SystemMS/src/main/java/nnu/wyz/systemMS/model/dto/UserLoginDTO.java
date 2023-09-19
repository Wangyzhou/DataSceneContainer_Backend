package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/18 21:14
 */
@Data
@ApiModel(value = "用户Oauth2认证的登录参数体")
public class UserLoginDTO {

    @ApiModelProperty(value = "客户端Id")
    private String client_id;

    @ApiModelProperty(value = "客户端密码")
    private String client_secret;

    @ApiModelProperty(value = "授权类型")
    private String grant_type;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;
}
