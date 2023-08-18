//package nnu.wyz.service.impl;
//
//import lombok.SneakyThrows;
//import org.springframework.security.oauth2.provider.ClientDetails;
//import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
//import org.springframework.stereotype.Service;
//
//import javax.sql.DataSource;
//
///**
// * @description:
// * @author: yzwang
// * @time: 2023/8/16 22:30
// */
//@Service
//public class JdbcClientDetailsServiceImpl extends JdbcClientDetailsService {
//
//    public JdbcClientDetailsServiceImpl(DataSource dataSource) {
//        super(dataSource);
//    }
//
//    @Override
//    @SneakyThrows
//    public ClientDetails loadClientByClientId(String clientId)  {
//        return super.loadClientByClientId(clientId);
//    }
//}
//
