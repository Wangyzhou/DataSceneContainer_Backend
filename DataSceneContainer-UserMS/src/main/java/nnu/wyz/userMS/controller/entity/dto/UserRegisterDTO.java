package nnu.wyz.userMS.controller.entity.dto;

import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/18 20:42
 */
@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String email;
    private String institution;
}
