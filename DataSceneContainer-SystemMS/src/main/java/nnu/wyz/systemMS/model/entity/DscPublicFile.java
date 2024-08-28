package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Desription：公共资源：文件数据
 * @Author：mfz
 * @Date：2024/8/5 19:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DscPublicFile {
    /**
     * 文件md5或目录id
     */
    private String id;

    /**
     * 文件名或目录名
     */
    private String name;

    /**
     * 文件类型或folder
     */
    private String type;

    /**
     * 创建时间
     */
    private String createdTime;

    /**
     * 最后更新时间
     */
    private String updatedTime;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 贡献者
     */
    private String createdUser;
}
