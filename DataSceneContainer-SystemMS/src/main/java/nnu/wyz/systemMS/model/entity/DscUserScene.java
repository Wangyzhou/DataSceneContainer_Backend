package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/12 20:38
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "用户场景对应表")
public class DscUserScene {

    private String id;

    private String userId;

    private String sceneId;

    private String sceneName;
}
