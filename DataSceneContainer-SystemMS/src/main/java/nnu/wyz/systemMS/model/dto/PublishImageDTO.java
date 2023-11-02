package nnu.wyz.systemMS.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/11/2 10:48
 */
@Data
public class PublishImageDTO {

    private String userId;

    private String fileId;

    private String name;

    private List<Double> bbox;

}
