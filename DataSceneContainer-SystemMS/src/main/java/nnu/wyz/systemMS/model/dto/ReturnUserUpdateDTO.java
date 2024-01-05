package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/1/5 14:52
 */
@Data
@ApiModel(value = "ReturnUserUpdateDTO",description = "更改用户信息返回参数")
public class ReturnUserUpdateDTO {

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "机构")
    private String institution;
}
