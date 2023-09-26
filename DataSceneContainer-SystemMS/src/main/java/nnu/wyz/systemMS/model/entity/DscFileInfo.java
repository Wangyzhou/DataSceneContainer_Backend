package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author yzwang
 * @since 2023-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DscFileInfo对象", description = "文件描述对象")
public class DscFileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @ApiModelProperty(value = "文件唯一标识")
    private String id;

    @ApiModelProperty(value = "文件md5")
    private String md5;

    @ApiModelProperty(value = "文件名")
    private String fileName;

    @ApiModelProperty(value = "文件后缀")
    private String fileSuffix;

    @ApiModelProperty(value = "能否发布成服务")
    private Boolean isEnablePublish;

    @ApiModelProperty(value = "创建者")
    private String createdUser;

    @ApiModelProperty(value = "创建时间")
    private String createdTime;

    @ApiModelProperty(value = "最后更新时间")
    private String updatedTime;

    @ApiModelProperty(value = "文件大小")
    private Long size;

    @ApiModelProperty(value = "查看计数")
    private Long previewCount;

    @ApiModelProperty(value = "下载计数")
    private Long downloadCount;

    @ApiModelProperty(value = "文件发布成服务的次数")
    private Long publishCount;

    @ApiModelProperty(value = "文件拥有用户的个数，为0则删除该资源")
    private Long ownerCount;

    @ApiModelProperty(value = "文件所属桶")
    private String bucketName;

    @ApiModelProperty(value = "对象key")
    private String objectKey;

    @ApiModelProperty(value = "文件权限")
    private Integer perms;
}
