package nnu.wyz.systemMS.service.iml;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.PythonDockerConfig;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscRasterSDAO;
import nnu.wyz.systemMS.dao.DscUserRasterSDAO;
import nnu.wyz.systemMS.model.dto.FalseColorCompositeDTO;
import nnu.wyz.systemMS.model.dto.RenderTifDTO;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.DscRasterService;
import nnu.wyz.systemMS.model.entity.DscUserRasterS;
import nnu.wyz.systemMS.service.DscTifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/2/22 15:46
 */
@Service
@Slf4j
public class DscTifServiceIml implements DscTifService {

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscRasterSDAO dscRasterSDAO;

    @Autowired
    private DscUserRasterSDAO dscUserRasterSDAO;

    @Autowired
    private PythonDockerConfig pythonDockerConfig;

    @Autowired
    private MinioConfig minioConfig;

    @Value("${fileSavePath}")
    private String rootPath;

    private static final String GDAL_CONTAINER_ID = "d00338b7a11e81935cf3d815284fb7316d928880ce3a999c7d20ed494a1cbdf1";

    private String pyPath;

    @PostConstruct
    public void init() {
        pyPath = rootPath + minioConfig.getPyFilesBucket() + File.separator + "tif_service.py";
    }

    @Override
    public CommonResult<Integer> getBandCount(String userId, String rasterSId) {
        DscUserRasterS dscUserRasterS = dscUserRasterSDAO.findByUserIdAndRasterSId(userId, rasterSId);
        if (Objects.isNull(dscUserRasterS)) {
            return CommonResult.failed("未找到该服务！");
        }
        DscRasterService dscRasterService = dscRasterSDAO.findDscRasterServiceById(rasterSId);
        Optional<DscFileInfo> byId = dscFileDAO.findById(dscRasterService.getOriFileId());
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该文件!");
        }
        DscFileInfo dscFileInfo = byId.get();
        String tiffPath = rootPath + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey();
        String[] execCommand = {"python", pyPath, tiffPath, "get_band_count"};
        DockerClient dockerClient = pythonDockerConfig.getDockerClient();
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(GDAL_CONTAINER_ID)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(execCommand)
                .exec();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String output = null;
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(new ExecStartResultCallback(outputStream, System.err) {
                    }).awaitCompletion();
            output = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            return CommonResult.success(Integer.parseInt(output.trim()), "获取成功！");
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return CommonResult.failed("获取失败，未知的错误！");
        }
    }

    @Override
    public CommonResult<String> changeColorMap(RenderTifDTO renderTifDTO) {
        DscUserRasterS dscUserRasterS = dscUserRasterSDAO.findByUserIdAndRasterSId(renderTifDTO.getUserId(), renderTifDTO.getRasterSId());
        if (Objects.isNull(dscUserRasterS)) {
            return CommonResult.failed("未找到该服务！");
        }
        DscRasterService dscRasterService = dscRasterSDAO.findDscRasterServiceById(renderTifDTO.getRasterSId());
        //  查找对应的png文件
        Optional<DscFileInfo> byId = dscFileDAO.findById(dscRasterService.getFileId());
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到服务引用的文件！");
        }
        //  查找对应的tif文件
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(dscRasterService.getOriFileId());
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到服务源文件！");
        }

        DscFileInfo pngFileInfo = byId.get();
        DscFileInfo tifFileInfo = byId1.get();
        String filePath = rootPath + pngFileInfo.getBucketName() + File.separator + pngFileInfo.getObjectKey();
        String tiffPath = rootPath + tifFileInfo.getBucketName() + File.separator + tifFileInfo.getObjectKey();
        String[] execCommand = null;
        //  如果开启地形阴影
        if (renderTifDTO.isShade()) {
            Map<String, Object> shadeParams = renderTifDTO.getShadeParams();
            execCommand = new String[]{"python", pyPath, tiffPath, "render_tif_png", Integer.toString(renderTifDTO.getBand()), renderTifDTO.getColorMap(), filePath, String.valueOf(renderTifDTO.isShade()), shadeParams.get("mode").toString(), shadeParams.get("azdeg").toString(), shadeParams.get("altdeg").toString(), shadeParams.get("vert_exag").toString()};
        } else {
            execCommand = new String[]{"python", pyPath, tiffPath, "render_tif_png", Integer.toString(renderTifDTO.getBand()), renderTifDTO.getColorMap(), filePath, String.valueOf(renderTifDTO.isShade())};
        }
        System.out.println(Arrays.toString(execCommand));

        return processTifRender(execCommand);
    }

    @Override
    public CommonResult<String> falseColorComposite(FalseColorCompositeDTO falseColorCompositeDTO) {
        DscUserRasterS dscUserRasterS = dscUserRasterSDAO.findByUserIdAndRasterSId(falseColorCompositeDTO.getUserId(), falseColorCompositeDTO.getRasterSId());
        if (Objects.isNull(dscUserRasterS)) {
            return CommonResult.failed("未找到该服务！");
        }
        DscRasterService dscRasterService = dscRasterSDAO.findDscRasterServiceById(falseColorCompositeDTO.getRasterSId());
        //  查找对应的png文件
        Optional<DscFileInfo> byId = dscFileDAO.findById(dscRasterService.getFileId());
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到服务引用的文件！");
        }
        //  查找对应的tif文件
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(dscRasterService.getOriFileId());
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到服务源文件！");
        }

        DscFileInfo pngFileInfo = byId.get();
        DscFileInfo tifFileInfo = byId1.get();
        String filePath = rootPath + pngFileInfo.getBucketName() + File.separator + pngFileInfo.getObjectKey();
        String tiffPath = rootPath + tifFileInfo.getBucketName() + File.separator + tifFileInfo.getObjectKey();
        List<Integer> bandList = falseColorCompositeDTO.getBandList();
        String[] execCommand = {"python", pyPath, tiffPath, "false_color_composite", bandList.get(0).toString(), bandList.get(1).toString(), bandList.get(2).toString(), filePath};
        System.out.println(Arrays.toString(execCommand));

        return processTifRender(execCommand);
    }


    private CommonResult<String> processTifRender(String[] execCommand) {
        //  直接向原png文件物理路径重新输出新png，其他信息不变
        DockerClient dockerClient = pythonDockerConfig.getDockerClient();
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(GDAL_CONTAINER_ID)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(execCommand)
                .exec();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(new ExecStartResultCallback(outputStream, System.err) {
                    }).awaitCompletion();
            return CommonResult.success("渲染成功！");
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return CommonResult.failed("渲染失败，未知的错误！");
        }
    }
}
