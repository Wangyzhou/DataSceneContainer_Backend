package nnu.wyz.mapper;

import nnu.wyz.entity.DscUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nnu.wyz.entity.DscUser_Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wyz
 * @since 2023-08-16
 */
@Mapper
public interface DscUserMapper extends BaseMapper<DscUser> {

    DscUser selectUserByUserName(String userName);
    List<DscUser_Role> findAuthoritiesByUserId(String userId);
}
