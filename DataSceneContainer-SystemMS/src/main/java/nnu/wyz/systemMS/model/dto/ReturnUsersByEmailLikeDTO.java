package nnu.wyz.systemMS.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/23 16:23
 */
@Data
@Accessors(chain = true)
public class ReturnUsersByEmailLikeDTO {

    private String id;

    private String userName;

    private String email;

//    private String avatar;
}
