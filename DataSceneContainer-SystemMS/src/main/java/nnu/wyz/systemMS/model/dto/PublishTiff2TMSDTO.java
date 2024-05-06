package nnu.wyz.systemMS.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/4/1 14:07
 */
@Data
public class PublishTiff2TMSDTO {

    @NotNull(message = "用户ID不能为空")
    private String userId;

    private String serviceName;

    private String tifFileId;

    private String tilesZipId;

}
