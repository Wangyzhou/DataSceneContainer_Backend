package nnu.wyz.systemMS.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import nnu.wyz.systemMS.model.entity.DscComputeContainerInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/4/13 15:17
 */
@Component
public class DockerUtil {
    @Value("${docker_cert_path}")
    private String dockerCertPath;

    private static String dockerCertPathThis;

    @PostConstruct
    public void init() {
        dockerCertPathThis = dockerCertPath;
    }

    public static DockerClient getDockerClient(DscComputeContainerInstance dscComputeContainerInstance) {
        DefaultDockerClientConfig.Builder builder =
                DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withDockerHost("tcp://" + dscComputeContainerInstance.getHost() + ":" + dscComputeContainerInstance.getPort())
                        .withApiVersion("1.43");
        if (dscComputeContainerInstance.isTls()) {
            builder.withDockerTlsVerify(true);
            builder.withDockerCertPath(dockerCertPathThis + File.separator + dscComputeContainerInstance.getHost() + File.separator);
        }
        return DockerClientBuilder.getInstance(builder.build()).build();
    }

}
