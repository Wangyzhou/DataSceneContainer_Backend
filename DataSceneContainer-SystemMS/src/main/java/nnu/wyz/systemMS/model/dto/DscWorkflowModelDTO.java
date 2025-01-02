package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String modelJson;

    private String userId;

    private String createTime;

    private String updateTime;
}
