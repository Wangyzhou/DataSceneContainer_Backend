package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/5 14:14
 */
@Data
@ToString
@ApiModel(value = "解压文件DTO")
@Accessors(chain = true)
public class UnzipFileDTO {

    private String userId;

    private String fileId;

    private String catalogId;

}
