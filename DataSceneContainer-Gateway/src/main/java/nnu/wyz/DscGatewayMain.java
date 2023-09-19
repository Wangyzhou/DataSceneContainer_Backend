package nnu.wyz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/15 16:46
 */
@SpringBootApplication
@EnableDiscoveryClient
@RefreshScope
public class DscGatewayMain {
//    @Value("${spring.cloud.gateway.config}")
//    private static String config;
    public static void main(String[] args) {
        SpringApplication.run(DscGatewayMain.class,args);
//        System.out.println(config);
    }
}
