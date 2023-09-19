package nnu.wyz.fileMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 10:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CatalogChildrenDTO {

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
     * 最后更新时间，若为目录，则为目录中孩子节点的最新更新时间
     */
    private String updatedTime;

    /**
     * 文件大小
     */
    private Long size;
}
