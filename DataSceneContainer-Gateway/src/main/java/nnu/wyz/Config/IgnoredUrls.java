package nnu.wyz.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/18 10:04
 */
@Data
@Component
@ConfigurationProperties(prefix = "secure.ignore")
public class IgnoredUrls {
    private List<String> urls;
}
