package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Desription：获取用户信息
 * @Author：mfz
 * @Date：2024/8/9 11:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserInfoDTO {

    private String userName;

    private String email;

    private String institution;

    private String avatar;
}
