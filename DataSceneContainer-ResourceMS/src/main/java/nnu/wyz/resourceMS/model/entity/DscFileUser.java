package nnu.wyz.resourceMS.model.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author yzwang
 * @since 2023-08-25
 */
@Data
@Accessors(chain = true)
@ApiModel(value="DscResUser对象", description="")
public class DscFileUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileId;

    private String userId;

    private Integer perms;


}
