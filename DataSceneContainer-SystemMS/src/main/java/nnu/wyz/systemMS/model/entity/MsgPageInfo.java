package nnu.wyz.systemMS.model.entity;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/21 14:35
 */
@Data
public class MsgPageInfo {

    private List<Message> msgs;

    private Integer count;

    private Integer pageNum;

}
