package nnu.wyz.systemMS.model.entity;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/20 10:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "消息实体")
public class Message {
    @Id
    @ApiModelProperty(value = "消息Id")
    private String id = IdUtil.randomUUID();

    @ApiModelProperty(value = "消息主题")
    private String topic;

    @ApiModelProperty(value = "消息类型")
    private String type;

    @ApiModelProperty(value = "发送者")
    private String from;

    @ApiModelProperty(value = "接收者")
    private String to;

    @ApiModelProperty(value = "资源")
    private String resource;

    @ApiModelProperty(value = "消息文本")
    private String text;

    @ApiModelProperty(value = "发送时间")
    private String date;

    @ApiModelProperty(value = "是否已读")
    private Boolean isRead;
}
