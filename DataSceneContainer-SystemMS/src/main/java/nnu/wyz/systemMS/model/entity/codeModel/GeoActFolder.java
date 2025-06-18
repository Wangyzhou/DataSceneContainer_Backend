package nnu.wyz.systemMS.model.entity.codeModel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActFolderDTO;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@ApiModel(value = "Geo-act文件夹", description = "Geo-act文件夹描述对象")
public class GeoActFolder {

    @Id
    @ApiModelProperty(value = "文件夹id")
    private String id;

    @ApiModelProperty(value = "文件夹名")
    private String folderName;

    @ApiModelProperty(value = "子文件（夹）")
    private List<String> children;

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

    // ✅ 显式无参构造函数，供 MongoDB 使用
    public GeoActFolder() {
    }

    // ✅ DTO 构造函数，供业务调用
    public GeoActFolder(GeoActFolderDTO geoActFolderDTO) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String nowStr = LocalDateTime.now().format(formatter);
        this.id = UUID.randomUUID().toString();
        this.folderName = geoActFolderDTO.getFolderName();
        this.children = new ArrayList<>(); // 默认空列表
        this.parentId = geoActFolderDTO.getParentId();
        this.sceneId = geoActFolderDTO.getSceneId();
        this.executor = geoActFolderDTO.getExecutor();
        this.createdTime = nowStr;
        this.modifiedTime = nowStr;
    }
}
