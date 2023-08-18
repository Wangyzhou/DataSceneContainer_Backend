package nnu.wyz.userMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/17 15:25
 */
@SpringBootApplication
@EnableDiscoveryClient
@RefreshScope
public class UserMSMain {
    public static void main(String[] args) {
        SpringApplication.run(UserMSMain.class, args);
    }
}
