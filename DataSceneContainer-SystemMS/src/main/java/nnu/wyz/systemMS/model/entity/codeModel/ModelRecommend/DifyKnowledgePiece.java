package nnu.wyz.systemMS.model.entity.codeModel.ModelRecommend;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActModelDTO;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModel;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModelParams.GeoActModelParams;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "知识库片段")
public class DifyKnowledgePiece {
    @ApiModelProperty(value = "模型id")
    private String id;

    @ApiModelProperty(value = "模型名")
    private String name;

//    @ApiModelProperty(value = "是否公开")
//    private String isPublic;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "模型参数")
    private GeoActModelParams params;

    @ApiModelProperty(value = "适用场景")
    private Scenario scenario;

    public DifyKnowledgePiece(GeoActModel geoActModel){
        this.id = geoActModel.getId();
        this.name = geoActModel.getName();
        this.description = geoActModel.getDescription();
        this.params = geoActModel.getParameters();
    }
}
