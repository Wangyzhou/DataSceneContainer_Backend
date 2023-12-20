package nnu.wyz.systemMS.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.MessageFormat;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/15 16:30
 */
@Configuration
public class DockerConfig {

    @Value("${whiteboxDockerServer}")
    private String deployIp;

    @Bean
    public DockerClient getDockerClient() {
        String dockerUrl = MessageFormat.format("tcp://{0}:2375", deployIp);
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withRegistryEmail("info@baeldung.com")
                .withRegistryPassword("baeldung")
                .withRegistryUsername("baeldung")
                .withDockerTlsVerify(false)
                .withDockerHost(dockerUrl).build();
        return DockerClientBuilder.getInstance(config).build();
    }
}
