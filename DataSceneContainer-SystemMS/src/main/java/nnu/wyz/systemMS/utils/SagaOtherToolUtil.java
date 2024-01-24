package nnu.wyz.systemMS.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.systemMS.config.SagaDockerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/24 9:42
 */
@Component
@Slf4j
public class SagaOtherToolUtil {

    @Autowired
    private SagaDockerConfig sagaDockerConfig;

    @Autowired
    private static SagaDockerConfig staticSagaDockerConfig;

    private static final String CONTAINER_ID = "e904431d1b38ec6fba77361019321875536deaf0169ebd6872af66bbab67d879";

    @PostConstruct
    public void init() {
        staticSagaDockerConfig = sagaDockerConfig;
    }

    public static boolean ConvertSgrd2GeoTIFF(String sgrdPath, String geoTiffPath) {
        String[] cmds = {"saga_cmd", "io_gdal", "2", "-GRIDS=" + sgrdPath, "-FILE" + geoTiffPath};
        ExecCreateCmdResponse exec = staticSagaDockerConfig.getDockerClient()
                .execCreateCmd(CONTAINER_ID)
                .withCmd(cmds)
                .exec();
        try {
            staticSagaDockerConfig.getDockerClient().execStartCmd(exec.getId()).exec(new ExecStartResultCallback()).awaitCompletion();
            File file = new File(geoTiffPath);
            System.out.println("file.getName() = " + file.getName());
            if(file.exists()) {
                return true;
            }
            return false;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return false;
        }
    }


}
