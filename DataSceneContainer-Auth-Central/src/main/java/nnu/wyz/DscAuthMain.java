package nnu.wyz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/16 10:29
 */
@SpringBootApplication
@EnableDiscoveryClient
@RefreshScope
public class DscAuthMain {
    public static void main(String[] args) {
        SpringApplication.run(DscAuthMain.class, args);
    }
}
