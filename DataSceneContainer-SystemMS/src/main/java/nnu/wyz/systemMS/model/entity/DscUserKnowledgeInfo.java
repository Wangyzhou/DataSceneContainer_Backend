package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

/**
 * @Desription：用户个人空间数据知识库相关信息
 * @Author：mfz
 * @Date：2024/11/20 16:02
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "用户个人空间数据知识库相关信息实体")
@AllArgsConstructor
@NoArgsConstructor
public class DscUserKnowledgeInfo {

    @Id
    String id;

    @ApiModelProperty(value = "用户Id")
    String userId;

    @ApiModelProperty(value = "用户个人空间数据知识库Id")
    String DatasetId;

    @ApiModelProperty(value = "知识库内文档Id")
    String DocumentId;
}
