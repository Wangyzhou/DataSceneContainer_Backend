package nnu.wyz.listener;

import lombok.extern.slf4j.Slf4j;
import nnu.wyz.service.impl.MongoClientDetailsServiceIml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/23 15:28
 */
@Component
@Slf4j
public class InitClientListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private MongoClientDetailsServiceIml mongoClientDetailsServiceIml;

    @Autowired
    PasswordEncoder bCryptPasswordEncoder;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("*******初始化client信息*******");
        ClientDetails sceneContainer = mongoClientDetailsServiceIml.loadClientByClientId("scene_container");
        if (Objects.isNull(sceneContainer)) {
            log.info("创建客户端信息");
            BaseClientDetails baseClientDetails = new BaseClientDetails();
            baseClientDetails.setClientId("scene_container");
            baseClientDetails.setClientSecret(bCryptPasswordEncoder.encode("opengms@uo~U%VGPm38S5HV"));
            baseClientDetails.setAccessTokenValiditySeconds(7200);
            baseClientDetails.setRefreshTokenValiditySeconds(259200);
            ArrayList<String> scopes = new ArrayList<>();
            scopes.add("all");
            baseClientDetails.setScope(scopes);
            ArrayList<String> grantedTypes = new ArrayList<>();
            grantedTypes.add("password");
            grantedTypes.add("refresh_token");
            baseClientDetails.setAuthorizedGrantTypes(grantedTypes);
            mongoClientDetailsServiceIml.addClientDetails(baseClientDetails);
        }
        log.info("*******初始化client信息完成*******");
    }
}
