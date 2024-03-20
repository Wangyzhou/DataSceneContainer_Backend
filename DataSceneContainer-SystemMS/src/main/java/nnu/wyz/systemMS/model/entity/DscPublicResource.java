package nnu.wyz.systemMS.model.entity;

import lombok.Data;

import java.util.List;

/**
 * @description:  公共资源类
 * @author: yzwang
 * @time: 2024/3/20 14:16
 */
@Data
public class DscPublicResource {

    private String id;

    private String name;

    private List<String> tags;

    private String type;

    private String description;

    private String resourceId;

    private String publisher;

    private String publishTime;



}
