package nnu.wyz.systemMS.model.entity;

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
@ApiModel(value="DscUser对象", description="")
public class DscUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String userName;

    private String password;

    private String email;

    private String institution;

    private String registerDate;

    private String avatar;

    private int enabled;

    private String activeCode;


}
