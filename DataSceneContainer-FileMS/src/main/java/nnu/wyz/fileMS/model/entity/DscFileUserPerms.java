package nnu.wyz.fileMS.model.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

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
public class DscFileUserPerms implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String fileId;

    private String userId;

    private Integer perms;


}
