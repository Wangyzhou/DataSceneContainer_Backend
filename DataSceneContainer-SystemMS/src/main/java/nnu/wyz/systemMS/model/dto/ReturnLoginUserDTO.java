package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/19 14:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnLoginUserDTO {
    private String id;
    private String username;
    private String email;
    private String institution;
    private String avatar;
    private String rootCatalog;
}
