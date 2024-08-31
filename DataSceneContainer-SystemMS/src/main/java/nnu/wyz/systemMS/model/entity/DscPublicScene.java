package nnu.wyz.systemMS.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Desription：公共场景记录表
 * @Author：mfz
 * @Date：2024/8/30 11:11
 */

@Data
@Accessors(chain = true)
public class DscPublicScene {

    private String id;

    private String name;

    private String type;

    private String createdUser;
}
