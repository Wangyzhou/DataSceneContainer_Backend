package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/1/6 15:20
 */

@Data
public class CreateDASceneDTO {

    @ApiModelProperty(value = "创建用户")
    private String userId;

    @ApiModelProperty(value = "场景名称")
    private String name;
}
