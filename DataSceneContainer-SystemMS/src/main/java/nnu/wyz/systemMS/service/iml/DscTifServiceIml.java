package nnu.wyz.systemMS.service.iml;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.PythonDockerConfig;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.dto.FalseColorCompositeDTO;
import nnu.wyz.systemMS.model.dto.RenderTifDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.DscTifService;
import nnu.wyz.systemMS.utils.DockerUtil;
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

    @Value("${scriptPath}")
    private String scriptPath;

    private String GDAL_CONTAINER_ID;

    private String pyPath;

    @Autowired
    private DscComputeContainerImageDAO dscComputeContainerImageDAO;

    @Autowired
    private DscComputeContainerInstanceDAO dscComputeContainerInstanceDAO;

    private static final String TOOL_CATEGORY = "System Tool";

    @PostConstruct
    public void init() {
        pyPath = scriptPath + "tif_service.py";
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
        Optional<DscComputeContainerImage> optional = dscComputeContainerImageDAO.findByIdentifier(TOOL_CATEGORY);
        if (!optional.isPresent()) {
            return CommonResult.failed("无可用计算容器镜像！");
        }
        List<DscComputeContainerInstance> allAvailableImages = dscComputeContainerInstanceDAO.findAllByImageId(optional.get().getId());
        if (allAvailableImages.isEmpty()) {
            return CommonResult.failed("无可用计算容器实例！");
        }
        // TODO: 容器调度
        DscComputeContainerInstance dscComputeContainerInstance = allAvailableImages.get(0);
        // TODO: 检查计算容器实例健康状态
        DockerClient dockerClient = DockerUtil.getDockerClient(dscComputeContainerInstance);
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(dscComputeContainerInstance.getContainerId())
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
        //  查找对应场景的png副本文件
        DscFileInfo pngFileInfo = null;
        List<RasterSRef> refs = dscRasterService.getReferences();
        Iterator<RasterSRef> iterator = refs.iterator();
        while (iterator.hasNext()) {
            RasterSRef rasterSRef = iterator.next();
            if (rasterSRef.getSceneId().equals(renderTifDTO.getSceneId())) {
                Optional<DscFileInfo> byId = dscFileDAO.findById(rasterSRef.getFileId());
                if (!byId.isPresent()) {
                    return CommonResult.failed("未找到服务引用的文件！");
                }
                pngFileInfo = byId.get();
                break;
            }
        }
        //  查找对应的tif文件
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(dscRasterService.getOriFileId());
        if (!byId1.isPresent()) {
            return CommonResult.failed("未找到服务源文件！");
        }
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
        //  查找对应场景的png副本文件
        DscFileInfo pngFileInfo = null;
        List<RasterSRef> refs = dscRasterService.getReferences();
        Iterator<RasterSRef> iterator = refs.iterator();
        while (iterator.hasNext()) {
            RasterSRef rasterSRef = iterator.next();
            if (rasterSRef.getSceneId().equals(falseColorCompositeDTO.getSceneId())) {
                Optional<DscFileInfo> byId = dscFileDAO.findById(rasterSRef.getFileId());
                if (!byId.isPresent()) {
                    return CommonResult.failed("未找到服务引用的文件！");
                }
                pngFileInfo = byId.get();
                break;
            }
        }
        //  查找对应的tif文件
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(dscRasterService.getOriFileId());
        if (!byId1.isPresent()) {
            return CommonResult.failed("未找到服务源文件！");
        }
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
        Optional<DscComputeContainerImage> optional = dscComputeContainerImageDAO.findByIdentifier(TOOL_CATEGORY);
        if (!optional.isPresent()) {
            return CommonResult.failed("无可用计算容器镜像！");
        }
        List<DscComputeContainerInstance> allAvailableImages = dscComputeContainerInstanceDAO.findAllByImageId(optional.get().getId());
        if (allAvailableImages.isEmpty()) {
            return CommonResult.failed("无可用计算容器实例！");
        }
        // TODO: 容器调度
        DscComputeContainerInstance dscComputeContainerInstance = allAvailableImages.get(0);
        // TODO: 检查计算容器实例健康状态
        DockerClient dockerClient = DockerUtil.getDockerClient(dscComputeContainerInstance);
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(dscComputeContainerInstance.getContainerId())
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
