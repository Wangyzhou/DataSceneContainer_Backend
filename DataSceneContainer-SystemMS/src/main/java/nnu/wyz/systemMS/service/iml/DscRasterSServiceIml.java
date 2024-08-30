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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private DscUserSceneDAO dscUserSceneDAO;

    @Autowired
    private DscPublicServiceDAO dscPublicServiceDAO;

    @Autowired
    private PythonDockerConfig pythonDockerConfig;

    @Autowired
    @Lazy
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
            if (allByImageId.size() > 0) {
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
    public CommonResult<String> publishTiff2RasterS(PublishTiff2ImageDTO publishTiff2ImageDTO, boolean isPublic) {
        String pyPath = scriptPath + "tif2png.py";
        Optional<DscFileInfo> byId = dscFileDAO.findById(publishTiff2ImageDTO.getFileId());
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该文件!");
        }
//        Optional<DscCatalog> byCatalog = dscCatalogDAO.findById(publishTiff2ImageDTO.getOutputCatalogId());
//        if (!byCatalog.isPresent()) {
//            return CommonResult.failed("未找到载体目录!");
//        }
        DscFileInfo dscFileInfo = byId.get();
        String tiffPath = rootPath + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey();
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
            // 首次插入初始化文件信息，走一天内已上传的文件逻辑
            dscFileDAO.insert(pngFileInfo);
            System.out.println(pngFileInfo);
            // 模拟上传任务，添加任务及文件相关记录
            InitTaskParam initTaskParam = new InitTaskParam();
            initTaskParam.setIdentifier(md5);
            initTaskParam.setFileName(fileName);
            initTaskParam.setFileId(fileId);
            initTaskParam.setUserId(publishTiff2ImageDTO.getUserId());
            initTaskParam.setTotalSize(pngFile.length());
            initTaskParam.setChunkSize(pngFile.length());
            initTaskParam.setObjectName(pngFile.getName());
            TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);
            // UploadFileDTO uploadFileDTO = new UploadFileDTO(publishTiff2ImageDTO.getUserId(), taskInfoDTO.getTaskRecord().getId(), publishTiff2ImageDTO.getOutputCatalogId());
            // spng不增加catalog记录
            UploadFileDTO uploadFileDTO = new UploadFileDTO(publishTiff2ImageDTO.getUserId(), taskInfoDTO.getTaskRecord().getId(), null);
            log.info(dscFileService.create(uploadFileDTO, false).getMessage());
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
            // 区分公共和个人
            if (isPublic) {
                DscPublicService dscPublicService = new DscPublicService();
                dscPublicService
                        .setId(rasterId)
                        .setServiceName(publishTiff2ImageDTO.getName())
                        .setServiceType("image")
                        .setPublisher(publishTiff2ImageDTO.getUserId());
                dscPublicServiceDAO.insert(dscPublicService);
            } else {
                DscUserRasterS dscUserRasterS = new DscUserRasterS();
                dscUserRasterS
                        .setId(IdUtil.randomUUID())
                        .setRasterSName(publishTiff2ImageDTO.getName())
                        .setRasterSId(rasterId)
                        .setUserId(publishTiff2ImageDTO.getUserId())
                        .setRasterSType("image");
                dscUserRasterSDAO.insert(dscUserRasterS);
            }
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
            DscRasterService dscRasterService = new DscRasterService(serviceId, serviceName, "tiles", serviceUrl, tifFileId, tifFileId, tiffBbox, userId, 1L, DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), null);
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
    public CommonResult<PageInfo<DscRasterService>> getRasterServiceList(PageableDTO pageableDTO, boolean isPublic) {
        String userId = pageableDTO.getCriteria();
        String keyword = pageableDTO.getKeyword(); // 新增关键词参数
        Integer pageIndex = pageableDTO.getPageIndex();
        Integer pageSize = pageableDTO.getPageSize();
        List<String> rsIds;
        if (isPublic) {
            rsIds = dscPublicServiceDAO.findAllRasServices().stream().map(DscPublicService::getId).collect(Collectors.toList());
        } else {
            rsIds = dscUserRasterSDAO.findAllByUserId(userId).stream().map(DscUserRasterS::getRasterSId).collect(Collectors.toList());
        }
        List<DscRasterService> rsListNoLimit = rsIds.stream()
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

    @Override
    public CommonResult<String> deleteRasterService(String userId, String rasterSId) {
        DscUserRasterS dscUserRasterS = dscUserRasterSDAO.findByUserIdAndRasterSId(userId, rasterSId);
        if (Objects.isNull(dscUserRasterS)) {
            return CommonResult.failed("未找到该服务");
        }
        DscRasterService dscRasterService = dscRasterSDAO.findDscRasterServiceById(rasterSId);
//        String fileId = dscRasterService.getFileId();
//        String tifId = dscRasterService.getOriFileId();
//        Optional<DscFileInfo> byId1 = dscFileDAO.findById(fileId);
//        if (!byId1.isPresent()) {
//            return CommonResult.failed("未找到该文件");
//        }
//        DscFileInfo dscFileInfo = byId1.get();
        // 不再删除服务对应的spng快照及修改tif的发布记录，迁移至定时任务
//        if (dscRasterService.getType().equals("image") && !Objects.isNull(tifId)) {
//            Optional<DscFileInfo> byId2 = dscFileDAO.findById(tifId);
//            if (!byId2.isPresent()) {
//                return CommonResult.failed("未找到该文件的原始tif文件");
//            }
//            DscFileInfo dscTifInfo = byId2.get();
//            dscTifInfo.setPublishCount(dscTifInfo.getPublishCount() - 1);
//            dscFileDAO.save(dscTifInfo);
//            dscFileInfo.setOwnerCount(dscFileInfo.getOwnerCount() - 1);     //文件拥有者数 - 1
//            log.info("服务快照删除成功");
//        } else {
//            dscFileInfo.setPublishCount(dscFileInfo.getPublishCount() - 1);
//        }
//        if (dscRasterService.getType().equals("tiles")) {     //删除瓦片目录
//            String tilesDirPath = rootPath + minioConfig.getRasterTilesBucket() + File.separator + userId + File.separator + rasterSId;
//            FileUtils.deleteDirectory(tilesDirPath);
//        }
//        dscFileDAO.save(dscFileInfo);
//        dscRasterSDAO.deleteById(rasterSId);
        dscRasterService.setOwnerCount(dscRasterService.getOwnerCount() - 1);
        dscRasterSDAO.save(dscRasterService);
        dscUserRasterSDAO.delete(dscUserRasterS);
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

    @Override
    public CommonResult<String> addRasterSCopy(GetRasterSCopyDTO getRasterSCopyDTO) {
        DscUserScene byUserIdAndSceneId = dscUserSceneDAO.findByUserIdAndSceneId(getRasterSCopyDTO.getUserId(), getRasterSCopyDTO.getSceneId());
        if (Objects.isNull(byUserIdAndSceneId)) {
            return CommonResult.failed("场景不存在！");
        }
        DscUserRasterS dscUserRasterS = dscUserRasterSDAO.findByUserIdAndRasterSId(getRasterSCopyDTO.getUserId(), getRasterSCopyDTO.getRasterSId());
        if (Objects.isNull(dscUserRasterS)) {
            return CommonResult.failed("未找到该服务");
        }
        DscRasterService dscRasterService = dscRasterSDAO.findDscRasterServiceById(getRasterSCopyDTO.getRasterSId());
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(dscRasterService.getFileId());
        if (!byId1.isPresent()) {
            return CommonResult.failed("未找到服务文件");
        }
        DscFileInfo dscFileInfo = byId1.get();
        Path filePath = Paths.get(rootPath + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey());
        String outputDirPath = rootPath + minioConfig.getBucketName() + File.separator + getRasterSCopyDTO.getUserId();
        String filePhysicalName = IdUtil.randomUUID() + ".png";
        String copyFilePath = outputDirPath + File.separator + filePhysicalName;
        // 创建副本文件
        try {
            Files.copy(filePath, Paths.get(copyFilePath), StandardCopyOption.REPLACE_EXISTING);
            // 添加副本png的文件信息
            // 只更改必要信息，其他信息沿用源png
            File copyFile = new File(copyFilePath);
            if (!copyFile.exists()) {
                return CommonResult.failed("获取失败，创建副本出错！");
            }
            FileInputStream fileInputStream = new FileInputStream(copyFile);
            String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
            dscFileInfo.setId(IdUtil.objectId()).
                    setCreatedTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                    .setUpdatedTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                    .setMd5(md5).setSize(copyFile.length())
                    .setObjectKey(getRasterSCopyDTO.getUserId() + File.separator + copyFile.getName())
                    .setOwnerCount(0L);
            // 首次插入初始化文件信息，走一天内已上传的文件逻辑
            dscFileDAO.insert(dscFileInfo);
            System.out.println(dscFileInfo);
            // 模拟上传任务，添加任务及文件相关记录
            InitTaskParam initTaskParam = new InitTaskParam();
            initTaskParam.setIdentifier(md5).setFileName(dscFileInfo.getFileName())
                    .setFileId(dscFileInfo.getId()).setUserId(getRasterSCopyDTO.getUserId())
                    .setTotalSize(copyFile.length()).setChunkSize(copyFile.length())
                    .setObjectName(copyFile.getName());
            TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);
            // spng不增加catalog记录
            UploadFileDTO uploadFileDTO = new UploadFileDTO(getRasterSCopyDTO.getUserId(), taskInfoDTO.getTaskRecord().getId(), null);
            log.info(dscFileService.create(uploadFileDTO, false).getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        //  添加栅格服务的副本记录
        String rasterSUrl = minioConfig.getEndpoint() + File.separator + minioConfig.getBucketName() + File.separator + dscFileInfo.getObjectKey();
        RasterSRef rasterSRef = new RasterSRef(getRasterSCopyDTO.getSceneId(), dscFileInfo.getId(), rasterSUrl);
        List<RasterSRef> references = dscRasterService.getReferences();
        if (references == null) {
            references = new ArrayList<>();
        }
        references.add(rasterSRef);
        dscRasterService.setReferences(references);
        // 服务引用次数+1
        dscRasterService.setOwnerCount(dscRasterService.getOwnerCount() + 1);
        dscRasterSDAO.save(dscRasterService);
        return CommonResult.success(rasterSUrl, "添加副本成功");
    }

    @Override
    public CommonResult<String> deleteRasterSCopy(String sceneId, String rasterSId) {
        DscRasterService dscRasterService = dscRasterSDAO.findDscRasterServiceById(rasterSId);
        if (Objects.isNull(dscRasterService)) {
            return CommonResult.failed("未找到该服务");
        }
        // 删除服务对应的场景子服务
        log.info("*****删除服务" + rasterSId + "对应的场景" + sceneId + "子服务*****");
        List<RasterSRef> refs = dscRasterService.getReferences();
        Iterator<RasterSRef> iterator = refs.iterator();
        while (iterator.hasNext()) {
            RasterSRef rasterSRef = iterator.next();
            if (rasterSRef.getSceneId().equals(sceneId)) {
                // 删除文件
                Optional<DscFileInfo> byId = dscFileDAO.findById(rasterSRef.getFileId());
                if (byId.isPresent()) {
                    log.info("删除子服务文件");
                    DscFileInfo dscFileInfo = byId.get();
                    dscFileInfo.setOwnerCount(dscFileInfo.getOwnerCount() - 1);
                    dscFileDAO.save(dscFileInfo);
                }
                // 摘除子服务
                log.info("摘除子服务记录");
                iterator.remove();
                break;
            }
        }
        dscRasterService.setReferences(refs);
        // 引用次数-1
        dscRasterService.setOwnerCount(dscRasterService.getOwnerCount() - 1);
        dscRasterSDAO.save(dscRasterService);
        return CommonResult.success("删除副本成功");
    }

    @Override
    public CommonResult<String> importRasterS(ServiceShareImportDTO serviceShareImportDTO) {
        // 服务信息的引用次数+1
        Optional<DscRasterService> byId = dscRasterSDAO.findById(serviceShareImportDTO.getServiceId());
        if (!byId.isPresent()) {
            return CommonResult.failed("导入出错：服务不存在！");
        }
        DscRasterService dscRasterService = byId.get();
        dscRasterService.setOwnerCount(dscRasterService.getOwnerCount() + 1);
        dscRasterSDAO.save(dscRasterService);
        // 添加用户栅格服务记录
        DscUserRasterS dscUserRasterS = new DscUserRasterS();
        dscUserRasterS.setId(IdUtil.randomUUID())
                .setUserId(serviceShareImportDTO.getUserId())
                .setRasterSId(serviceShareImportDTO.getServiceId())
                .setRasterSName(dscRasterService.getName())
                .setRasterSType(dscRasterService.getType());
        dscUserRasterSDAO.insert(dscUserRasterS);
        return CommonResult.success("导入成功");
    }

    @Override
    public void updateOwnerCount(List<String> rasterSIds, boolean isPlus) {
        int count = isPlus ? 1 : -1;
        List<DscRasterService> dscRasterServices = dscRasterSDAO.findAllByIds(rasterSIds);
        for (DscRasterService dscRasterService : dscRasterServices) {
            dscRasterService.setOwnerCount(dscRasterService.getOwnerCount() + count);
        }
        dscRasterSDAO.saveAll(dscRasterServices);
    }
}
