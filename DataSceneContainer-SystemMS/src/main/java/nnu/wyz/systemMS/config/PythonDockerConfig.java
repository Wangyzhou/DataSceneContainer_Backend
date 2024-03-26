package nnu.wyz.systemMS.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.MessageFormat;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/23 21:35
 */
@Configuration
public class PythonDockerConfig {

    @Value("${pythonDockerServer}")
    private String deployIp;

    public DockerClient getDockerClient() {
        String dockerUrl = MessageFormat.format("tcp://{0}", deployIp);
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withRegistryEmail("info@baeldung.com")
                .withRegistryUsername("baeldung")
                .withRegistryPassword("baeldung")
                .withDockerTlsVerify(false)
                .withDockerHost(dockerUrl)
                .build();
        return DockerClientBuilder.getInstance(config).build();
    }
}
