package nnu.wyz.fileMS.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/6 20:45
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "矢量图层发布DTO")
public class PublishShapefileDTO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 服务名
     */
    private String name;

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 目录id
     */
    private String catalogId;

    /**
     * espg空间参考
     */
    private String srid;

    /**
     * 文件编码
     */
    private String code;
}
