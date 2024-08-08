package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;

/**
 * @Desription：公共资源：服务数据（不单独区分矢量和栅格，这里只记录服务条目，具体服务信息在对应服务表中，通过id关联)
 * @Author：mfz
 * @Date：2024/8/6 16:04
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DscPublicService {

    @Id
    @ApiModelProperty(value = "服务ID")
    private String id;

    @ApiModelProperty(value = "服务名称")
    private String serviceName;

    @ApiModelProperty(value = "服务类型")
    private String serviceType;

    @ApiModelProperty(value = "贡献者")
    private String publisher;
}
