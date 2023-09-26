package nnu.wyz.systemMS.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/23 14:44
 */
@Data
public class FileShareDTO {

    private String fromUser;

    private List<String> toUsers;

    private String fileId;

}
