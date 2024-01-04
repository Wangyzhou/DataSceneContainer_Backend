package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2023/12/12 22:12
 */

@Data
@ApiModel(value = "UserUpdateDTO",description = "更改用户信息传入参数")
public class UserUpdateDTO {

    @ApiModelProperty(value = "用户Id")
    private String userId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "机构")
    private String institution;
}
