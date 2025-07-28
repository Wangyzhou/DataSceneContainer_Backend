package nnu.wyz.systemMS.model.entity.codeModel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActScriptDTO;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@ApiModel(value = "Geo-act代码文件", description = "Geo-act文件描述对象")
public class GeoActScriptFile {
    @Id
    @ApiModelProperty(value = "文件id")
    private String id;

    @ApiModelProperty(value = "文件名")
    private String fileName;

    @ApiModelProperty(value = "文件类型")
    private String type;

    @ApiModelProperty(value = "父文件夹")
    private String parentId;

    @ApiModelProperty(value = "场景id")
    private String sceneId;

    @ApiModelProperty(value = "创建人")
    private String executor;

    @ApiModelProperty(value = "创建时间")
    private String createdTime;

    @ApiModelProperty(value = "更新时间")
    private String modifiedTime;

    public GeoActScriptFile() {}

    public GeoActScriptFile(GeoActScriptDTO geoActScriptDTO) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String nowStr = LocalDateTime.now().format(formatter);
        this.id = UUID.randomUUID().toString();
        this.fileName = geoActScriptDTO.getFileName();
        this.type = geoActScriptDTO.getType();
        this.parentId = geoActScriptDTO.getParentId();
        this.sceneId = geoActScriptDTO.getSceneId();
        this.executor = geoActScriptDTO.getExecutor();
        this.createdTime = nowStr;
        this.modifiedTime = nowStr;
    }
}
