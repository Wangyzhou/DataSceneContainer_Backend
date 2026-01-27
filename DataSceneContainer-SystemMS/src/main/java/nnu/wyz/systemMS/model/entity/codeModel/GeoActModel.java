package nnu.wyz.systemMS.model.entity.codeModel;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActModelDTO;
import nnu.wyz.systemMS.model.entity.DscModelParams;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModelParams.GeoActModelParams;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoActModel {
    @ApiModelProperty(value = "模型id")
    private String id;

    @ApiModelProperty(value = "模型名")
    private String name;

    @ApiModelProperty(value = "是否叶节点")
    private String isLeaf;

//    @ApiModelProperty(value = "是否公开")
//    private String isPublic;

    @ApiModelProperty(value = "是否加入知识库")
    private boolean inKnowledgeHubOrNot;

    @ApiModelProperty(value = "可用性")
    private String isEnabled;

    @ApiModelProperty(value = "目录")
    private String category;

    @ApiModelProperty(value = "子目录")
    private String subCategory;

    @ApiModelProperty(value = "脚本内容")
    private String script;

    @ApiModelProperty(value = "源文件id")
    private String fileId;

    @ApiModelProperty(value = "源文件名")
    private String fileName;

    @ApiModelProperty(value = "模型文件名")
    private String modelScriptName;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "模型参数")
    private GeoActModelParams parameters;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "作者id")
    private String ownerId;

    @ApiModelProperty(value = "参考文献")
    private String[] references;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    public GeoActModel(GeoActModelDTO geoActModelDTO ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String nowStr = LocalDateTime.now().format(formatter);
        this.id = "CUSTOM-" + UUID.randomUUID();
        this.name = geoActModelDTO.getName();
        this.isLeaf = geoActModelDTO.getIsLeaf();
        this.isEnabled = geoActModelDTO.getIsEnabled();
        this.category = geoActModelDTO.getCategory();
        this.subCategory = geoActModelDTO.getSubCategory();
        this.script = geoActModelDTO.getScript();
        this.fileId = geoActModelDTO.getFileId();
        this.fileName = geoActModelDTO.getFileName();
        this.description = geoActModelDTO.getDescription();
        this.parameters = geoActModelDTO.getParameters();
        this.author = geoActModelDTO.getAuthor();
        this.ownerId = geoActModelDTO.getOwnerId();
        this.references = geoActModelDTO.getReferences();
        this.createTime = nowStr;
        this.inKnowledgeHubOrNot = geoActModelDTO.isAddToKnowledgeHubOrNot();
    }
}
