package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import nnu.wyz.systemMS.model.entity.DscTable;

/**
 * @author tjk
 * @date 2024/12/23
 * @Description
 */
@Data
public class CreateTableFileDTO {

    @ApiModelProperty(value="用户ID")
    private String userId;

    @ApiModelProperty(value="表格文件名")
    private String tableName;

    @ApiModelProperty(value="表格文件类型")
    private String tableType;

    @ApiModelProperty(value = "上传目录")
    private String catalog;

    @ApiModelProperty(value="JSON数据")
    private DscTable data;
}
