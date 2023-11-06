package nnu.wyz.systemMS.model.dto;

import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/20 19:57
 */
@Data
public class PageableDTO {
    private String criteria;
    private Integer pageIndex;
    private Integer pageSize;
}
