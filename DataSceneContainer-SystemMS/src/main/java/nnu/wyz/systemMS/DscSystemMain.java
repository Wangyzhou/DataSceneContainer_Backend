package nnu.wyz.systemMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.cloud.openfeign.EnableFeignClients;
//import org.springframework.retry.annotation.EnableRetry;
//import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/28 20:25
 */
@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
@EnableAsync
//@EnableFeignClients
//@RefreshScope
//@EnableTransactionManagement
//@EnableRetry
public class DscSystemMain {
    public static void main(String[] args) {
        SpringApplication.run(DscSystemMain.class, args);
    }
}
