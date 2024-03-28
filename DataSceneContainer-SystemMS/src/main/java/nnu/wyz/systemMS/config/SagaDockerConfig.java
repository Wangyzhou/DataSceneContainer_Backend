package nnu.wyz.systemMS.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.LocalDirectorySSLConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Objects;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/15 16:30
 */
@Configuration
public class SagaDockerConfig {

    @Autowired
    private DockerConfig dockerConfig;

    public DockerClient getDockerClient() {
        DefaultDockerClientConfig.Builder builder =
                DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withDockerHost("tcp://" + dockerConfig.getDocker_url())
                        .withApiVersion(dockerConfig.getDocker_api_version());
        if(dockerConfig.getDocker_tls_verify().equals("yes")) {
            builder.withDockerTlsVerify(true);
            builder.withDockerCertPath(dockerConfig.getDocker_cert_path());
        }
        return DockerClientBuilder.getInstance(builder.build()).build();
    }
}
