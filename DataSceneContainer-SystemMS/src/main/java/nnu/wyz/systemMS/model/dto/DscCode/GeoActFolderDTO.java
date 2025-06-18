package nnu.wyz.systemMS.model.dto.DscCode;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoActFolderDTO {
    @ApiModelProperty(value = "文件名")
    private String folderName;

    @ApiModelProperty(value = "父文件夹")
    private String parentId;

    @ApiModelProperty(value = "场景id")
    private String sceneId;

    @ApiModelProperty(value = "创建人")
    private String executor;
}
