package nnu.wyz.systemMS.model.dto;

import io.swagger.models.auth.In;
import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/20 19:57
 */
@Data
public class MessagePageDTO {
    private String email;
    private Integer pageIndex;
    private Integer pageSize;
}
