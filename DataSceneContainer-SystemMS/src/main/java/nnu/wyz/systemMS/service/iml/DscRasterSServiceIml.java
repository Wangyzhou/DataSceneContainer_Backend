package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.PythonDockerConfig;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.model.param.InitTaskParam;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscFileService;
import nnu.wyz.systemMS.service.DscRasterSService;
import nnu.wyz.systemMS.service.SysUploadTaskService;
import nnu.wyz.systemMS.utils.DockerUtil;
import nnu.wyz.systemMS.utils.FileUtils;
import nnu.wyz.systemMS.utils.GeoToolsUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/11/2 10:46
 */
@Service
@Slf4j
public class DscRasterSServiceIml implements DscRasterSService {

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscRasterSDAO dscRasterSDAO;

    @Autowired
    private DscUserRasterSDAO dscUserRasterSDAO;

    @Autowired
    private DscCatalogDAO dscCatalogDAO;

    @Autowired
    private DscCatalogService dscCatalogService;

    @Autowired
    private PythonDockerConfig pythonDockerConfig;

    @Autowired
    private DscFileService dscFileService;

    @Autowired
    private SysUploadTaskService sysUploadTaskService;

    @Autowired
    private MinioConfig minioConfig;

    @Value("${fileSavePath}")
    private String rootPath;

    @Value("${scriptPath}")
    private String scriptPath;

    @Value("${tms_root_url}")
    private String tms_root_url;

    @Value("${spring.application.name}")
    private String appName;

    private String GDAL_CONTAINER_ID;

    @Autowired
    private DscComputeContainerImageDAO dscComputeContainerImageDAO;

    @Autowired
    private DscComputeContainerInstanceDAO dscComputeContainerInstanceDAO;

    private final String TOOL_CATEGORY = "System Tool";

    @PostConstruct
    public void init() {
        dscComputeContainerImageDAO.findByIdentifier(TOOL_CATEGORY).ifPresent(dscComputeContainerImage -> {
            List<DscComputeContainerInstance> allByImageId = dscComputeContainerInstanceDAO.findAllByImageId(dscComputeContainerImage.getId());
            if(allByImageId.size() > 0) {
                GDAL_CONTAINER_ID = allByImageId.get(0).getContainerId();
            }
        });
    }

    @Override
    public CommonResult<String> publishImage2RasterS(PublishImageDTO publishImageDTO) {
        String userId = publishImageDTO.getUserId();
        String fileId = publishImageDTO.getFileId();
        String rasterSName = publishImageDTO.getName();
        List<Double> bbox = publishImageDTO.getBbox();
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该文件!");
        }
        DscFileInfo dscFileInfo = byId.get();
        String rasterSUrl = minioConfig.getEndpoint() + File.separator + dscFileInfo.getBucketName() + "/" + dscFileInfo.getObjectKey();
        DscRasterService dscRasterService = new DscRasterService();
        String rasterId = IdUtil.randomUUID();
        dscRasterService.setId(rasterId)
                .setPublisher(userId)
                .setPublishTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                .setName(rasterSName)
                .setFileId(fileId)
                .setBbox(bbox)
                .setType("image")
                .setOwnerCount(1L)
                .setUrl(rasterSUrl);
        dscRasterSDAO.insert(dscRasterService);
        DscUserRasterS dscUserRasterS = new DscUserRasterS();
        dscUserRasterS
                .setId(IdUtil.randomUUID())
                .setRasterSName(rasterSName)
                .setRasterSId(rasterId)
                .setUserId(userId)
                .setRasterSType("image");
        dscUserRasterSDAO.insert(dscUserRasterS);
        //增加文件发布记录
        dscFileInfo.setPublishCount(dscFileInfo.getPublishCount() + 1);
        dscFileDAO.save(dscFileInfo);
        return CommonResult.success("发布成功!");
    }

    @Override
    public CommonResult<String> publishTiff2RasterS(PublishTiff2ImageDTO publishTiff2ImageDTO) {
        String pyPath = scriptPath + "tif2png.py";
        Optional<DscFileInfo> byId = dscFileDAO.findById(publishTiff2ImageDTO.getFileId());
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该文件!");
        }
        Optional<DscCatalog> byCatalog = dscCatalogDAO.findById(publishTiff2ImageDTO.getOutputCatalogId());
        if (!byCatalog.isPresent()) {
            return CommonResult.failed("未找到载体目录!");
        }
        DscFileInfo dscFileInfo = byId.get();
        String tiffPath = rootPath + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey();
        String catalogPath = dscCatalogService.getCatalogPath(publishTiff2ImageDTO.getOutputCatalogId());
        //  物理存储在dsc-files桶
        String outputDirPath = rootPath + minioConfig.getBucketName() + File.separator + publishTiff2ImageDTO.getUserId();
        String filePhysicalName = IdUtil.randomUUID() + ".png";
        String filePath = outputDirPath + File.separator + filePhysicalName;
        String[] execCommand = {"python", pyPath, tiffPath, filePath};
        System.out.println(Arrays.toString(execCommand));
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
            List<Double> bbox = parseBbox(output);
            // 添加png的文件信息
            File pngFile = new File(filePath);
            if (!pngFile.exists()) {
                return CommonResult.failed("发布失败，tif解析出错！");
            }
            FileInputStream fileInputStream = new FileInputStream(pngFile);
            String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
            //  用于标识服务用途的特殊png
            String suffix = "s" + pngFile.getName().substring(pngFile.getName().lastIndexOf(".") + 1);
            //  png和tif同名
            String fileName = dscFileInfo.getFileName().substring(0, dscFileInfo.getFileName().lastIndexOf(".")) + "." + suffix;
            String fileId = IdUtil.objectId();
            DscFileInfo pngFileInfo = new DscFileInfo(fileId, md5, fileName, suffix, false, publishTiff2ImageDTO.getUserId(), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), pngFile.length(), 0L, 0L, 0L, 0L, minioConfig.getBucketName(), publishTiff2ImageDTO.getUserId() + File.separator + pngFile.getName(), 32);
            dscFileDAO.insert(pngFileInfo);
            System.out.println(pngFileInfo);
            // 模拟上传任务，添加文件夹相关记录
            InitTaskParam initTaskParam = new InitTaskParam();
            initTaskParam.setIdentifier(md5);
            initTaskParam.setFileName(fileName);
            initTaskParam.setFileId(fileId);
            initTaskParam.setUserId(publishTiff2ImageDTO.getUserId());
            initTaskParam.setTotalSize(pngFile.length());
            initTaskParam.setChunkSize(pngFile.length());
            initTaskParam.setObjectName(fileName.substring(0, fileName.lastIndexOf(".")));
            TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);
            UploadFileDTO uploadFileDTO = new UploadFileDTO(publishTiff2ImageDTO.getUserId(), taskInfoDTO.getTaskRecord().getId(), publishTiff2ImageDTO.getOutputCatalogId());
            dscFileService.create(uploadFileDTO);
            //  添加栅格服务记录
            DscRasterService dscRasterService = new DscRasterService();
            String rasterId = IdUtil.randomUUID();
            String rasterSUrl = minioConfig.getEndpoint() + File.separator + minioConfig.getBucketName() + File.separator + pngFileInfo.getObjectKey();
            dscRasterService.setId(rasterId)
                    .setPublisher(publishTiff2ImageDTO.getUserId())
                    .setPublishTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                    .setName(publishTiff2ImageDTO.getName())
                    .setFileId(fileId)
                    .setOriFileId(dscFileInfo.getId())
                    .setBbox(bbox)
                    .setType("image")
                    .setOwnerCount(1L)
                    .setUrl(rasterSUrl);
            dscRasterSDAO.insert(dscRasterService);
            DscUserRasterS dscUserRasterS = new DscUserRasterS();
            dscUserRasterS
                    .setId(IdUtil.randomUUID())
                    .setRasterSName(publishTiff2ImageDTO.getName())
                    .setRasterSId(rasterId)
                    .setUserId(publishTiff2ImageDTO.getUserId())
                    .setRasterSType("image");
            dscUserRasterSDAO.insert(dscUserRasterS);
            //增加文件发布记录（tif）
            dscFileInfo.setPublishCount(dscFileInfo.getPublishCount() + 1);
            dscFileDAO.save(dscFileInfo);
            return CommonResult.success("发布成功!");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return CommonResult.failed("发布失败，未知的错误！");
        }
    }

    @Override
    public CommonResult<String> publishTiff2TMS(PublishTiff2TMSDTO publishTiff2TMSDTO) {
        String userId = publishTiff2TMSDTO.getUserId();
        String serviceName = publishTiff2TMSDTO.getServiceName();
        String tifFileId = publishTiff2TMSDTO.getTifFileId();
        String tilesZipId = publishTiff2TMSDTO.getTilesZipId();
        Optional<DscFileInfo> tifById = dscFileDAO.findById(tifFileId);
        Optional<DscFileInfo> zipById = dscFileDAO.findById(tilesZipId);
        if (!tifById.isPresent() || !zipById.isPresent()) {
            return CommonResult.failed("tif文件、瓦片数据集缺少，发布失败！");
        }
        // 找到zip包所在物理路径、解压到rasterTiles文件夹的个人目录下
        DscFileInfo rasterTilesZip = zipById.get();
        DscFileInfo tifFile = tifById.get();
        String zipPath = rootPath + rasterTilesZip.getBucketName() + File.separator + rasterTilesZip.getObjectKey();
        String tifPath = rootPath + tifFile.getBucketName() + File.separator + tifFile.getObjectKey();
        System.out.println("tifPath = " + tifPath);
        System.out.println("zipPath = " + zipPath);
        GeoToolsUtil.init(tifPath);
        try {
            String tiffEpsgCode = GeoToolsUtil.getTiffEpsgCode();
            if (!tiffEpsgCode.equals("4326")) {
                return CommonResult.failed("发布失败,tif含有无效投影!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return CommonResult.failed("发布失败, 系统错误!");
        }
        String serviceId = IdUtil.objectId();
        String unzipPath = rootPath + minioConfig.getRasterTilesBucket() + File.separator + userId + File.separator + serviceId;
        File file = new File(unzipPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("unzip", zipPath, "-d", unzipPath + File.separator);
        BufferedReader bf;
        try {
            Process process = pb.start();
            bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = bf.readLine()) != null) {
                output.append(line);
            }
            if (Objects.requireNonNull(file.listFiles()).length == 0) {
                return CommonResult.failed("发布失败,瓦片数据集为空!");
            }
            //  添加栅格服务记录
            String serviceUrl = tms_root_url + "/" + appName + "/dsc-raster-service/getRasterTiles" + "/" + userId + "/" + serviceId + "/{z}/{x}/{y}.png";
            List<Double> tiffBbox = GeoToolsUtil.getTiffBbox();
            DscRasterService dscRasterService = new DscRasterService(serviceId, serviceName, "tiles", serviceUrl, tifFileId, tifFileId, tiffBbox, userId, 1L, DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            dscRasterSDAO.insert(dscRasterService);
            DscUserRasterS userRasterS = new DscUserRasterS(IdUtil.randomUUID(), userId, serviceId, serviceName, "tiles");
            dscUserRasterSDAO.insert(userRasterS);
            tifFile.setPublishCount(tifFile.getPublishCount() + 1);
            dscFileDAO.save(tifFile);
            return CommonResult.success("发布成功！");
        } catch (IOException e) {
            e.printStackTrace();
            return CommonResult.failed("发布失败！");
        }
    }

    @Override
    public CommonResult<PageInfo<DscRasterService>> getRasterServiceList(PageableDTO pageableDTO) {
        String userId = pageableDTO.getCriteria();
        String keyword = pageableDTO.getKeyword(); // 新增关键词参数
        Integer pageIndex = pageableDTO.getPageIndex();
        Integer pageSize = pageableDTO.getPageSize();
        List<DscRasterService> rsListNoLimit = dscUserRasterSDAO.findAllByUserId(userId)
                .stream()
                .map(DscUserRasterS::getRasterSId)
                .map(dscRasterSDAO::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(info -> info.getName().contains(keyword)) // 根据关键词进行模糊匹配
                .sorted(Comparator.comparing(DscRasterService::getName))
                .collect(Collectors.toList());
        List<DscRasterService> dscRasterServices = rsListNoLimit
                .stream()
                .skip((long) (pageIndex - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        PageInfo<DscRasterService> dscRasterServicePageInfo = new PageInfo<>(dscRasterServices, rsListNoLimit.size(), (int) Math.ceil((double) rsListNoLimit.size() / pageSize));
        return CommonResult.success(dscRasterServicePageInfo, "获取成功！");
    }

    // TODO: 有冗余，当type是image时，spng文件不会随服务一起删
    @Override
    public CommonResult<String> deleteRasterService(String userId, String rasterSId) {
        DscUserRasterS dscUserRasterS = dscUserRasterSDAO.findByUserIdAndRasterSId(userId, rasterSId);
        if (Objects.isNull(dscUserRasterS)) {
            return CommonResult.failed("未找到该服务");
        }
        DscRasterService dscRasterService = dscRasterSDAO.findDscRasterServiceById(rasterSId);
        String fileId = dscRasterService.getFileId();
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(fileId);
        if (!byId1.isPresent()) {
            return CommonResult.failed("未找到该文件");
        }
        DscFileInfo dscFileInfo = byId1.get();
        dscFileInfo.setPublishCount(dscFileInfo.getPublishCount() - 1);
        if(dscRasterService.getType().equals("tiles")){     //删除瓦片目录
            String tilesDirPath = rootPath + minioConfig.getRasterTilesBucket() + File.separator + userId + File.separator + rasterSId;
            FileUtils.deleteDirectory(tilesDirPath);
        }
        dscFileDAO.save(dscFileInfo);
        dscUserRasterSDAO.delete(dscUserRasterS);
        dscRasterSDAO.deleteById(rasterSId);
        return CommonResult.success("删除成功!");
    }

    @Override
    public CommonResult<List<DscRasterService>> getRasterServiceListByFileId(String fileId) {
        List<DscRasterService> allByFileId = dscRasterSDAO.findAllByFileIdOrOriFileId(fileId);
        return CommonResult.success(allByFileId, "获取成功!");
    }

    private static List<Double> parseBbox(String bboxString) {
        bboxString = bboxString.replaceAll("\\[|\\]|\\n", ""); // 去掉方括号和换行符
        List<Double> bbox = new ArrayList<>();
        String[] coordinates = bboxString.split(", ");
        for (String coordinate : coordinates) {
            bbox.add(Double.parseDouble(coordinate));
        }
        return bbox;
    }

    @Override
    public void getRasterTiles(Integer z, Integer x, Integer y, String userId, String rasterSId, HttpServletResponse response) {
        response.setContentType("image/png");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(
                    rootPath +
                            minioConfig.getRasterTilesBucket() +
                            File.separator +
                            userId +
                            File.separator +
                            rasterSId +
                            File.separator +
                            z +
                            File.separator +
                            x +
                            File.separator +
                            y +
                            ".png");
            IOUtils.copy(fis, response.getOutputStream());
        } catch (IOException ignored) {

        }
    }
}
