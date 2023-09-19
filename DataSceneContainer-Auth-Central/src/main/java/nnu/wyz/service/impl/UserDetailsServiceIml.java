package nnu.wyz.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.dao.DscCatalogDAO;
import nnu.wyz.dao.DscUserDAO;
import nnu.wyz.entity.DscCatalog;
import nnu.wyz.entity.DscUser;
import nnu.wyz.entity.dto.ReturnLoginUserDTO;
import nnu.wyz.service.UserDetailsService;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/16 16:44
 */

@Service
@Slf4j
public class UserDetailsServiceIml implements UserDetailsService {


    @Autowired
    private DscUserDAO dscUserDAO;
    @Autowired
    private DscCatalogDAO dscCatalogDAO;
    Logger logger = LoggerFactory.getLogger(UserDetailsServiceIml.class);

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        DscUser dscUser = dscUserDAO.findDscUserByEmail(email);
        if (Objects.isNull(dscUser)) {
            logger.info("用户：" + email + "不存在！");
            throw new UsernameNotFoundException("用户名不存在！");
        }
        DscCatalog catalog = dscCatalogDAO.findDscCatalogByUserIdAndParent(dscUser.getId(), "-1");
        ReturnLoginUserDTO loginUser = new ReturnLoginUserDTO(dscUser.getId(), dscUser.getUserName(), dscUser.getEmail(), dscUser.getInstitution(), Base64.encodeBase64String(dscUser.getAvatar()), catalog.getId());
        return User.withUsername(JSON.toJSONString(loginUser)).password(dscUser.getPassword()).roles("user").build(); //必须配置role或者权限，不然会出错 java.lang.IllegalArgumentException: Cannot pass a null GrantedAuthority collection
    }
}
