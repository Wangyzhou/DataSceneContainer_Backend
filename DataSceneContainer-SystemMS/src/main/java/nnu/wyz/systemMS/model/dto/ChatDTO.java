package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/4/17 16:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatDTO {

    private String sceneId;
    private String email;
    private String question;

}
