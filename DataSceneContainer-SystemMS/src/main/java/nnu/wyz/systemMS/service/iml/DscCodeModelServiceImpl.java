package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscCode.DscCodeModelDAO;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscGeoAnalysisExecTaskDAO;
import nnu.wyz.systemMS.dao.DscUserDAO;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGARawParams;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
import nnu.wyz.systemMS.model.DscGeoAnalysis.GeoAnalysisOutputRecDTO;
import nnu.wyz.systemMS.model.dto.CreateCatalogDTO;
import nnu.wyz.systemMS.model.dto.DscCodeModelDTO;
import nnu.wyz.systemMS.model.dto.TaskInfoDTO;
import nnu.wyz.systemMS.model.dto.UploadFileDTO;
import nnu.wyz.systemMS.model.entity.DscCatalog;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.GeoToolExecTaskStatus;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModel;
import nnu.wyz.systemMS.model.param.InitTaskParam;
import nnu.wyz.systemMS.model.param.code.CodeParams;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscCode.DscCodeModelService;
import nnu.wyz.systemMS.service.DscFileService;
import nnu.wyz.systemMS.service.SysUploadTaskService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class DscCodeModelServiceImpl implements DscCodeModelService {

    @Autowired
    private DscCodeModelDAO dscCodeModelDAO;

    @Autowired
    private DscCatalogDAO dscCatalogDAO;

    @Autowired
    private DscCatalogService dscCatalogService;

    @Value("${fileSavePath}")
    private String rootPath;

    @Value("${codeOutPutHub}")
    private String codeOutPutHub;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private DscUserDAO dscUserDAO;

    @Autowired
    private DscGeoAnalysisExecTaskDAO dscCodeAnalysisExecTaskDAO;

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private SysUploadTaskService sysUploadTaskService;

    @Autowired
    private DscFileService dscFileService;



    @Override
    public CommonResult<String> encapsulate(DscCodeModelDTO dscCodeModelDTO){
        // 为文件分配一个唯一的 UUID
        String id = UUID.randomUUID().toString();

        // 获取当前日期时间并格式化为字符串
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 将 DTO 转换为 Entity
        DscCodeModel codeModelEntity = new DscCodeModel(dscCodeModelDTO);

        // 设置 UUID 和当前日期时间
        codeModelEntity.setId(id);
        codeModelEntity.setCreateDate(currentDateTime);
        codeModelEntity.setEnabled(true);

        // 保存到数据库
        dscCodeModelDAO.save(codeModelEntity);

        return CommonResult.success("模型封装成功！");
    }

    @Override
    public CommonResult<DscCodeModel> getToolInfo(String toolId){
        Optional<DscCodeModel> optionalTool = dscCodeModelDAO.findById(toolId);
        // 返回工具的内容，或者继续处理这个工具对象
        // 如果找不到该工具，返回null或者抛出异常
        return CommonResult.success(optionalTool.orElse(null));
    }

    @Override
    public CommonResult<String> delete(String toolId){
        dscCodeModelDAO.deleteById(toolId);
        // 删除后，通过查找该ID的实体对象来验证是否删除成功
        Optional<DscCodeModel> toolAfterDelete = dscCodeModelDAO.findById(toolId);
        if(toolAfterDelete.isPresent()){
            return CommonResult.failed("删除失败！");
        }else {
            return CommonResult.success("删除成功！");// 如果找不到该ID，则返回true，表示删除成功
        }
    }

    @Override
    public CommonResult<?> getExecuteResult(CodeParams params) {
        String taskId = params.getTaskId();
        String executor = params.getExecutor();
        DscGeoAnalysisExecTask dscCodeAnalysisExecTask = new DscGeoAnalysisExecTask();
        //创建工具输出目录
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDateTime = dateFormat.format(new Date());
        String catalogName = "Code Execute Output("+formattedDateTime+")";
        DscCatalog sceneCatalog = dscCatalogDAO.findDscCatalogById(params.getSceneCatalogId());
        CreateCatalogDTO createCatalogDTO = new CreateCatalogDTO();
        createCatalogDTO.setUserId(executor);
        createCatalogDTO.setParentCatalogId(sceneCatalog.getId());
        createCatalogDTO.setCatalogName(catalogName);
        createCatalogDTO.setTaskId(taskId);
        CommonResult<String> createCatalogRes = dscCatalogService.create(createCatalogDTO);//创建场景内输出文件夹
        String outputCatalog = createCatalogRes.getData();
        String catalogPath = dscCatalogService.getCatalogPath(outputCatalog);
        String outputDir = rootPath + minioConfig.getGaOutputBucket() + File.separator + executor + catalogPath;
        File file = new File(outputDir);
        boolean isMakeDir = file.mkdirs();
        if (!isMakeDir) {
            dscCatalogService.delete(outputCatalog);
            return CommonResult.failed("创建任务失败!");
        }
        JSONObject executorJson = new JSONObject();
        executorJson.put("id", executor);
        executorJson.put("name", dscUserDAO.findDscUserById(executor).getUserName());
        DscGARawParams dscGARawParams = new DscGARawParams();
        dscGARawParams.setOutput(null);
        dscCodeAnalysisExecTask.setId(taskId)
                .setExecutor(executorJson)
                .setParams(dscGARawParams)
                .setStatus(-2)
                .setDescription("code task is running");
        dscCodeAnalysisExecTaskDAO.insert(dscCodeAnalysisExecTask);
        //遍历输出文件目录，进行入库
        try {
            String[] fileNameList = params.getFileNames(); // 多个文件的绝对路径
            List<JSONObject> codeTaskOutputs = new ArrayList<>();

            for (String memberFileName : fileNameList) {
                String originalFilePath = codeOutPutHub + File.separator + params.getExecutor() + File.separator + params.getTaskId() + File.separator + memberFileName;
                File originalFile = new File(originalFilePath);

                if (!originalFile.exists() || !originalFile.isFile()) {
                    log.warn("路径无效或不是文件：{}", originalFilePath);
                    continue;
                }

                log.info("原始文件： {}", originalFile.getAbsolutePath());

                // 生成物理文件名，并构建目标路径
                String filePhysicalName = IdUtil.randomUUID();
                String suffix = originalFile.getName().substring(originalFile.getName().lastIndexOf(".") + 1);
                String logicalFileName = originalFile.getName().substring(0, originalFile.getName().lastIndexOf("."));
                String finalFileName = "code result_"+filePhysicalName + "." + suffix;
                String minioPath = outputDir + File.separator + finalFileName;

                File targetFile = new File(minioPath);
                try {
                    // ✅ 拷贝文件到 MinIO 存储路径（outputDir）
                    FileUtils.copyFile(originalFile, targetFile);
                } catch (IOException ioException) {
                    log.error("文件拷贝失败：{}", originalFile.getAbsolutePath(), ioException);
                    continue;
                }

                JSONObject codeTaskOutput = new JSONObject();

                try (FileInputStream fileInputStream = new FileInputStream(targetFile)) {
                    String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
                    String fileId = IdUtil.objectId();

                    DscFileInfo codeOutputFileInfo = new DscFileInfo(
                            fileId,
                            md5,
                            finalFileName, // 最终文件名
                            suffix,
                            false,
                            dscCodeAnalysisExecTask.getExecutor().get("id").toString(),
                            DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"),
                            DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"),
                            targetFile.length(),
                            0L, 0L, 0L, 0L,
                            minioConfig.getGaOutputBucket(),
                            dscCodeAnalysisExecTask.getExecutor().getString("id") + catalogPath + File.separator + finalFileName,
                            32
                    );
                    dscFileDAO.insert(codeOutputFileInfo);

                    InitTaskParam initTaskParam = new InitTaskParam();
                    initTaskParam.setIdentifier(md5);
                    initTaskParam.setFileName(finalFileName); // 最终文件名
                    initTaskParam.setFileId(fileId);
                    initTaskParam.setUserId(dscCodeAnalysisExecTask.getExecutor().get("id").toString());
                    initTaskParam.setTotalSize(targetFile.length());
                    initTaskParam.setChunkSize(targetFile.length());
                    initTaskParam.setObjectName(filePhysicalName);

                    TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);

                    UploadFileDTO uploadFileDTO = new UploadFileDTO(
                            dscCodeAnalysisExecTask.getExecutor().get("id").toString(),
                            taskInfoDTO.getTaskRecord().getId(),
                            outputCatalog
                    );

                    dscFileService.create(uploadFileDTO, false, false);

                    codeTaskOutput.put("id", fileId);
                    codeTaskOutput.put("name", logicalFileName);
                    codeTaskOutputs.add(codeTaskOutput);
                }
            }

            dscCodeAnalysisExecTask.getParams().setOutputs(codeTaskOutputs);
            finishTask(dscCodeAnalysisExecTask);
            return CommonResult.success("代码分析完成");
        } catch (Exception e) {
            log.error("处理文件失败", e);
            return CommonResult.failed("代码分析执行异常！");
        }
    }

    void finishTask(DscGeoAnalysisExecTask dscCodeAnalysisExecTask) {
        dscCodeAnalysisExecTask.setStatus(GeoToolExecTaskStatus.SUCCEED);
        dscCodeAnalysisExecTask.setEndTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscCodeAnalysisExecTask.setDescription("code analysis task is finished.");
        dscCodeAnalysisExecTaskDAO.save(dscCodeAnalysisExecTask);
    }
}
