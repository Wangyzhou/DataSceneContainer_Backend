package nnu.wyz.systemMS.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/4/10 13:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DscComputeContainerInstance {

    @Id
    private String id;

    private String containerId;

    private String containerName;

    private String imageId;

    private String host;

    private String port;

    private boolean isTls;

    private String createdTime;

}
