package nnu.wyz.systemMS.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PythonConfig {
    @Value("${PythonDocker}")
    private String pythonDockerIp;

    public String getPythonDockerIp() {
        return pythonDockerIp;
    }
}
