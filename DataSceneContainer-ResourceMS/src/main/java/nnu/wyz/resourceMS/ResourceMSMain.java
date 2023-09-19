package nnu.wyz.resourceMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/22 15:21
 */
@SpringBootApplication
@EnableDiscoveryClient
@RefreshScope
public class ResourceMSMain {
    public static void main(String[] args) {
        SpringApplication.run(ResourceMSMain.class,args);
    }
}
