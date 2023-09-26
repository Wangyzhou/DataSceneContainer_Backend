package nnu.wyz.systemMS.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/19 16:57
 */
@Data
public class MapParamsDTO {
    private List<Double> center;
    private Double zoom;
}
