package nnu.wyz.resourceMS.model.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

/**
 * <p>
 * 
 * </p>
 *
 * @author yzwang
 * @since 2023-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="DscMvtServiceInfo对象", description="")
public class DscMvtServiceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String resId;

    private String fileId;

    private String mvtUrl;


}
