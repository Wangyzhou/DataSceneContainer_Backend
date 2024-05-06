package nnu.wyz.systemMS.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.SagaDockerConfig;
import nnu.wyz.systemMS.dao.DscComputeContainerImageDAO;
import nnu.wyz.systemMS.dao.DscComputeContainerInstanceDAO;
import nnu.wyz.systemMS.model.entity.DscComputeContainerImage;
import nnu.wyz.systemMS.model.entity.DscComputeContainerInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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


    private static final String CONTAINER_ID = "2135d7c4e677e3c90d81f7542723a15fe8aa04579010357725cf6d5af8953968";

    private static final String TOOL_IDENTIFIER = "Saga GIS Tool";

    @Autowired
    private DscComputeContainerImageDAO dscComputeContainerImageDAO;

    @Autowired
    private DscComputeContainerInstanceDAO dscComputeContainerInstanceDAO;

    public boolean ConvertSgrd2GeoTIFF(String sgrdPath, String geoTiffPath) {
        String[] cmds = {"saga_cmd", "io_gdal", "2", "-GRIDS=" + sgrdPath, "-FILE" + geoTiffPath};
        Optional<DscComputeContainerImage> optional = dscComputeContainerImageDAO.findByIdentifier(TOOL_IDENTIFIER);
        if (!optional.isPresent()) {
            return false;
        }
        List<DscComputeContainerInstance> allAvailableImages = dscComputeContainerInstanceDAO.findAllByImageId(optional.get().getId());
        if (allAvailableImages.isEmpty()) {
            return false;
        }
        // TODO: 容器调度
        DscComputeContainerInstance dscComputeContainerInstance = allAvailableImages.get(0);
        // TODO: 检查计算容器实例健康状态
        DockerClient dockerClient = DockerUtil.getDockerClient(dscComputeContainerInstance);
        ExecCreateCmdResponse exec = dockerClient
                .execCreateCmd(dscComputeContainerInstance.getContainerId())
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(cmds)
                .exec();
        try {
            PrintStream stdout = System.out;
            PrintStream stderr = System.err;
            dockerClient.execStartCmd(exec.getId()).exec(new ExecStartResultCallback(stdout, stderr) {
                @Override
                public void onNext(Frame item) {
                    super.onNext(item);
                }
            }).awaitCompletion();
            File file = new File(geoTiffPath);
            return file.exists();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return false;
        }
    }


}
