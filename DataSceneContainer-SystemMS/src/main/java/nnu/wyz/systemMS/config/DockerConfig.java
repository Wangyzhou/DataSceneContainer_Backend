package nnu.wyz.systemMS.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/3/27 22:52
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "docker")
public class DockerConfig {

    private String docker_url;

    private String docker_tls_verify;

    private String docker_cert_path;

    private String docker_api_version;

}
