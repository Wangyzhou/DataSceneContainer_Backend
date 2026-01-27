package nnu.wyz.systemMS.model.dto.DscCode;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nnu.wyz.systemMS.model.entity.DscModelParams;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModelParams.GeoActModelParams;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoActModelDTO {
    @ApiModelProperty(value = "模型名")
    private String name;

    @ApiModelProperty(value = "是否叶节点")
    private String isLeaf;

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

    @ApiModelProperty(value = "是否加入叶节点")
    private boolean addToKnowledgeHubOrNot;
}
