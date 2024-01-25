package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.PythonDockerConfig;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscRasterSDAO;
import nnu.wyz.systemMS.dao.DscUserRasterSDAO;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.model.param.InitTaskParam;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscFileService;
import nnu.wyz.systemMS.service.DscRasterSService;
import nnu.wyz.systemMS.service.SysUploadTaskService;
import nnu.wyz.systemMS.utils.CompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static final String GDAL_CONTAINER_ID = "0b9ec1ec970f7d8000b7ead841821af61bafe01517820920f8c3f061dc2f65df";

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
        DscUserRasterS isExist = dscUserRasterSDAO.findDscUserRasterSByUserIdAndRasterSNameAndRasterSType(userId, rasterSName, "image");
        if (!Objects.isNull(isExist)) {
            return CommonResult.failed("存在名称相同的Image服务，请更改发布服务的名称！");
        }
        DscFileInfo dscFileInfo = byId.get();
        String rasterSUrl = minioConfig.getEndpoint() + "/" + dscFileInfo.getBucketName() + "/" + dscFileInfo.getObjectKey();
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
    public CommonResult<String> publishTiff2RasterS(PublishTiffDTO publishTiffDTO) {
        String pyPath = rootPath + minioConfig.getPyFilesBucket() + File.separator + "tif2png.py";
        Optional<DscFileInfo> byId = dscFileDAO.findById(publishTiffDTO.getFileId());
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该文件!");
        }
        Optional<DscCatalog> byCatalog = dscCatalogDAO.findById(publishTiffDTO.getOutputCatalogId());
        if (!byCatalog.isPresent()) {
            return CommonResult.failed("未找到载体目录!");
        }
        DscUserRasterS isExist = dscUserRasterSDAO.findDscUserRasterSByUserIdAndRasterSNameAndRasterSType(publishTiffDTO.getUserId(), publishTiffDTO.getName(), "image");
        if (!Objects.isNull(isExist)) {
            return CommonResult.failed("存在名称相同的Image服务，请更改发布服务的名称！");
        }
        DscFileInfo dscFileInfo = byId.get();
        String tiffPath = rootPath + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey();
        String catalogPath = dscCatalogService.getCatalogPath(publishTiffDTO.getOutputCatalogId());
        String outputDirPath = rootPath + minioConfig.getGaOutputBucket() + File.separator + publishTiffDTO.getUserId() + catalogPath;
        String filePhysicalName = IdUtil.randomUUID() + ".png";
        String filePath = outputDirPath + File.separator + filePhysicalName;
        String[] execCommand = {"python", pyPath, tiffPath, filePath};
        System.out.println(Arrays.toString(execCommand));
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
            List<Double> bbox = parseBbox(output);
            // 添加png的文件信息
            File pngFile = new File(filePath);
            if (!pngFile.exists()) {
                return CommonResult.failed("发布失败，tif解析出错！");
            }
            FileInputStream fileInputStream = new FileInputStream(pngFile);
            String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
            String suffix = pngFile.getName().substring(pngFile.getName().lastIndexOf(".") + 1);
            String fileName = pngFile.getName();
            String fileId = IdUtil.objectId();
            DscFileInfo pngFileInfo = new DscFileInfo(fileId, md5, fileName, suffix, false, publishTiffDTO.getUserId(), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), pngFile.length(), 0L, 0L, 0L, 0L, minioConfig.getGaOutputBucket(), publishTiffDTO.getUserId() + catalogPath + File.separator + fileName, 32);
            dscFileDAO.insert(pngFileInfo);
            System.out.println(pngFileInfo);
            // 模拟上传任务，添加文件夹相关记录
            InitTaskParam initTaskParam = new InitTaskParam();
            initTaskParam.setIdentifier(md5);
            initTaskParam.setFileName(fileName);
            initTaskParam.setFileId(fileId);
            initTaskParam.setUserId(publishTiffDTO.getUserId());
            initTaskParam.setTotalSize(pngFile.length());
            initTaskParam.setChunkSize(pngFile.length());
            initTaskParam.setObjectName(fileName.substring(0, fileName.lastIndexOf(".")));
            TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);
            UploadFileDTO uploadFileDTO = new UploadFileDTO(publishTiffDTO.getUserId(), taskInfoDTO.getTaskRecord().getId(), publishTiffDTO.getOutputCatalogId());
            dscFileService.create(uploadFileDTO);
            //  添加栅格服务记录
            DscRasterService dscRasterService = new DscRasterService();
            String rasterId = IdUtil.randomUUID();
            String rasterSUrl = minioConfig.getEndpoint() + File.separator + minioConfig.getGaOutputBucket() + File.separator + pngFileInfo.getObjectKey();
            dscRasterService.setId(rasterId)
                    .setPublisher(publishTiffDTO.getUserId())
                    .setPublishTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                    .setName(publishTiffDTO.getName())
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
                    .setRasterSName(publishTiffDTO.getName())
                    .setRasterSId(rasterId)
                    .setUserId(publishTiffDTO.getUserId())
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
    public CommonResult<PageInfo<DscRasterService>> getRasterServiceList(PageableDTO pageableDTO) {
        String userId = pageableDTO.getCriteria();
        Integer pageIndex = pageableDTO.getPageIndex();
        Integer pageSize = pageableDTO.getPageSize();
        List<DscRasterService> rsListNoLimit = dscUserRasterSDAO.findAllByUserId(userId)
                .stream()
                .map(DscUserRasterS::getRasterSId)
                .map(dscRasterSDAO::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(DscRasterService::getName))
                .collect(Collectors.toList());
        List<DscRasterService> dscRasterServices = rsListNoLimit
                .stream()
                .skip((long) (pageIndex - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        PageInfo<DscRasterService> dscRasterServicePageInfo = new PageInfo<>(dscRasterServices, rsListNoLimit.size(), (rsListNoLimit.size() / pageSize) + 1);
        return CommonResult.success(dscRasterServicePageInfo, "获取成功！");
    }

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
        dscFileDAO.save(dscFileInfo);
        dscUserRasterSDAO.delete(dscUserRasterS);
        dscRasterSDAO.deleteById(rasterSId);
        return CommonResult.success("删除成功!");
    }

    @Override
    public CommonResult<List<DscRasterService>> getRasterServiceListByFileId(String fileId) {
        List<DscRasterService> allByFileId = dscRasterSDAO.findAllByFileId(fileId);
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
}
