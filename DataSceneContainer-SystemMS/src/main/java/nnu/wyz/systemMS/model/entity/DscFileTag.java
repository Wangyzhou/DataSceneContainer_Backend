package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @Desription：文件tag
 * @Author：mfz
 * @Date：2024/11/18 19:35
 */

@Data
@Accessors(chain = true)
@ApiModel(value = "文件tag实体")
@AllArgsConstructor
@NoArgsConstructor
public class DscFileTag {

    @Id
    String id;

    @ApiModelProperty(value = "用户Id")
    String createdUser;

    @ApiModelProperty(value = "关联的文件Id")
    String fileId;

    @ApiModelProperty(value = "文件类型标签")
    String type;

    @ApiModelProperty(value = "文件位置标签")
    List<String> location;

    @ApiModelProperty(value = "文件属性标签")
    List<String> attr;

    @ApiModelProperty(value = "文件其他标签")
    List<String> other;

}
