package nnu.wyz.systemMS.model.entity.codeModel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscCodeModelDTO;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModel;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @author tjk
 * @date 2025/1/2
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "Model对象", description = "工作流集成模型与自定义封装描述对象")
public class  DscCodeModel {
    @Id
    @ApiModelProperty(value = "模型ID")
    private String id;

    @ApiModelProperty(value = "模型名称")
    private String name;

    @ApiModelProperty(value = "模型所属类别")
    private String category;

    @ApiModelProperty(value = "模型描述")
    private String description;

    @ApiModelProperty(value = "模型可用性")
    private boolean isEnabled;

    @ApiModelProperty(value = "模型参数")
    private DscCodeModelParams parameters;

    @ApiModelProperty(value = "模型执行代码")
    private String script;

    @ApiModelProperty(value = "模型参考文献")
    private List<String> references;

    @ApiModelProperty(value = "模型作者")
    private String author;

    @ApiModelProperty(value = "模型拥有者ID")
//    private List<String> ownerId;   当需要模型共享时再启用
    private String ownerId;

    @ApiModelProperty(value = "模型创建时间")
    private String createDate;

    public DscCodeModel(DscCodeModelDTO dscCodeModelDTO) {
        this.name = dscCodeModelDTO.getName();
        this.script = dscCodeModelDTO.getScript();
        this.category = dscCodeModelDTO.getCategory();
        this.parameters = dscCodeModelDTO.getParams();
        this.description = dscCodeModelDTO.getDescription();
        this.author = dscCodeModelDTO.getCreater();
        this.ownerId = dscCodeModelDTO.getCreaterId();
        this.references = dscCodeModelDTO.getReference();
        this.createDate = dscCodeModelDTO.getCreateDate();
    }

}

