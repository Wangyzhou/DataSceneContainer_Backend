package nnu.wyz.fileMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/4 17:32
 */
@SpringBootApplication
@EnableDiscoveryClient
@RefreshScope
@EnableFeignClients
@EnableTransactionManagement
public class DscFileMain {
    public static void main(String[] args) {
        SpringApplication.run(DscFileMain.class, args);
    }
}
