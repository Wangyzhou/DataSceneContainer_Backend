package nnu.wyz.systemMS.model.dto;

import lombok.Data;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/1/17 20:51
 */
@Data
public class PublishTiffDTO {

    private String userId;

    private String fileId;

    private String outputCatalogId;

    private String name;

    // 发布的方式：1、从个人空间发布（user);2、从场景中发布（scene)
    private String method;
}
