package nnu.wyz.service.impl;

import com.alibaba.fastjson.JSON;
import nnu.wyz.entity.DscUser;
import nnu.wyz.entity.DscUser_Role;
import nnu.wyz.mapper.DscUserMapper;
import nnu.wyz.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/16 16:44
 */

@Service
public class UserDetailsServiceIml implements UserDetailsService {

    @Autowired
    private DscUserMapper dscUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("username=" + username);
        DscUser dscUser = dscUserMapper.selectUserByUserName(username);
        System.out.println("dscUser = " + dscUser);
        if (Objects.isNull(dscUser)) {
            throw new UsernameNotFoundException("用户名不存在！");
        }
        //TODO:加入权限！
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        List<DscUser_Role> DscUser_Roles = dscUserMapper.findAuthoritiesByUserId(dscUser.getId());
        DscUser_Roles.forEach(perms -> authorities.add(new SimpleGrantedAuthority(perms.getPerm_url())));
        return User.withUsername(JSON.toJSONString(dscUser)).password(dscUser.getPassword()).authorities(authorities).build();
    }
}
