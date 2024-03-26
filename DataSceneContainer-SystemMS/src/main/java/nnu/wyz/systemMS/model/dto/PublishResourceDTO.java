package nnu.wyz.systemMS.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/3/22 14:16
 */
@Data
public class PublishResourceDTO {

    private String name;

    private List<String> tags;

    private String type;

    private String description;

//    private
}
