package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nnu.wyz.systemMS.model.entity.DscModelParams;

import java.util.List;

/**
 * @author tjk
 * @date 2025/1/2
 * @Description
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DscWorkflowModelDTO {

    private String id;

    private String name;

    private String category;

    private String description;

    private DscModelParams params;

    private String modelJson;

    private String author;

//    private List<String> ownerId;
    private String ownerId;

    private String createTime;

    private String updateTime;
}
