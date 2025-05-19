package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscCode.DscCodeModelDAO;
import nnu.wyz.systemMS.dao.DscCode.DscCodeModelTempPathsDAO;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscGeoAnalysisExecTaskDAO;
import nnu.wyz.systemMS.dao.DscUserDAO;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGARawParams;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
import nnu.wyz.systemMS.model.DscGeoAnalysis.GeoAnalysisOutputRecDTO;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.model.entity.DscCatalog;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.GeoToolExecTaskStatus;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModel;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModelTaskTempPaths;
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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DscCodeModelServiceImpl implements DscCodeModelService {

    @Resource
    private AmazonS3 amazonS3;

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

    @Value("${JupyterInnerOutPutHub}")
    private String JupyterInnerOutPutHub;

    @Value("${fileSavePath}")
    private String fileRootPath;

    @Value("${fileSavePathWin}")
    private String fileRootPathWin;

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

    @Autowired
    private DscCodeModelTempPathsDAO dscCodeModelTempPathsDAO;


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
        DscCodeModelTaskTempPaths temp = dscCodeModelTempPathsDAO.findByTaskId(params.getTaskId());

        // 如果没有查找到临时路径记录，直接返回
        if (temp == null) {
            log.warn("未找到相关模型执行任务");
        }else if(temp.getStatus().equals("done")){
            log.warn("任务已结束");
        }else if(temp.getStatus().equals("todo") && CollUtil.isEmpty(temp.getTempPaths())){
            log.warn("任务未产生临时目录");
        }else {
            log.info("检测到临时目录，进行删除操作");
            List<String> tempPaths = temp.getTempPaths();

            // 遍历所有路径，删除对应目录
            for (String tempPath : tempPaths) {
                if (StrUtil.isNotBlank(tempPath)) {
                    File tempDir = new File(tempPath);
                    if (tempDir.exists() && tempDir.isDirectory()) {
                        FileUtil.del(tempDir); // ✅ 删除整个目录及其内容
                    }
                }
            }

        }

        return registerExecuteOutput(params);
    }


    @Override
    public CommonResult<?> generateCustomModelScript(DscCustomModelDTO dscCustomModelDTO) {
        Map<String, String> inputUrls = new LinkedHashMap<>();
        Map<String, String> optionsMap = new HashMap<>();
        Map<String, String> outputNames = new HashMap<>();
        String executor = dscCustomModelDTO.getExecutor();
        String taskId = "custom-model-"+IdUtil.randomUUID();
        DscCodeModelTaskTempPaths dscCodeModelTaskTempPaths = new DscCodeModelTaskTempPaths();
        dscCodeModelTaskTempPaths.setTaskId(taskId);
        dscCodeModelTaskTempPaths.setId(IdUtil.randomUUID());
        String customModelTaskPath = codeOutPutHub + File.separator + dscCustomModelDTO.getExecutor() + File.separator + taskId;
        File taskCatalog = new File(customModelTaskPath);

        //1. 获取输入文件
        for (Map.Entry<String, String> entry : dscCustomModelDTO.getInput().entrySet()) {
            String inputId = entry.getValue(); // value 是文件 ID
            Optional<DscFileInfo> fileInfoOpt = dscFileDAO.findById(inputId);
            if (fileInfoOpt.isPresent()) {
                DscFileInfo dscFileInfo = fileInfoOpt.get();
                dscFileInfo.setPreviewCount(dscFileInfo.getPreviewCount() + 1);
                dscFileDAO.save(dscFileInfo);
                // 检查是否为 zip 文件
                if ("zip".equalsIgnoreCase(dscFileInfo.getFileSuffix())) {
                    // 构造临时目录路径
                    String fileRoot = System.getProperty("os.name").startsWith("Windows") ? fileRootPathWin : fileRootPath;
                    String fullPath = fileRoot + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey();
                    String relativeUnzipPath = File.separator + dscCustomModelDTO.getExecutor() + File.separator + taskId + File.separator + "input" + File.separator + entry.getKey();
                    String jupyterUnzipPath = JupyterInnerOutPutHub + relativeUnzipPath;
                    String tempUnzipPath = codeOutPutHub + relativeUnzipPath;
                    File tempDir = new File(tempUnzipPath);
                    if (!tempDir.exists() && !tempDir.mkdirs()) {
                        return CommonResult.failed("无法创建临时目录用于解压：" + tempUnzipPath);
                    }
                    // 解压 zip 文件
                    try {
                        nnu.wyz.systemMS.utils.FileUtils.zipUncompress(fullPath, tempUnzipPath);
                        inputUrls.put(entry.getKey(), jupyterUnzipPath);
                        List<String> tempPaths = dscCodeModelTaskTempPaths.getTempPaths();
                        tempPaths.add(tempUnzipPath);
                        dscCodeModelTaskTempPaths.setTempPaths(tempPaths);
                        dscCodeModelTempPathsDAO.save(dscCodeModelTaskTempPaths);
                    } catch (Exception e) {
                        log.error(String.valueOf(e));
                        log.error(fullPath);
                        if(taskCatalog.delete()){
                            return CommonResult.failed("解压失败,文件残留已删除：" + e.getMessage());
                        }else {
                            return CommonResult.failed("解压失败,文件残留删除失败：" + e.getMessage());
                        }

                    }
                } else {
                    // 非 zip 文件，使用 MinIO 直链地址
                    String fileUrl = minioConfig.getEndpoint() + "/" + dscFileInfo.getBucketName() + "/" + dscFileInfo.getObjectKey();
                    inputUrls.put(entry.getKey(), fileUrl);
                }
            } else {
                return CommonResult.failed(ResultCode.VALIDATE_FAILED, "未找到文件ID：" + inputId);
            }
        }

        // 2. 获取 options
        if (dscCustomModelDTO.getOptions() != null) {
            optionsMap.putAll(dscCustomModelDTO.getOptions());
        }

        // 3. 获取 output
        if (dscCustomModelDTO.getOutput() != null) {
            outputNames.putAll(dscCustomModelDTO.getOutput());
        }

        // 4. 替换模型脚本占位符
        String script = dscCustomModelDTO.getScript();
        try {
            script = replaceInputPlaceholders(script, new ArrayList<>(inputUrls.values()), taskCatalog);
            script = replaceOptionPlaceholders(script, new ArrayList<>(optionsMap.values()), taskCatalog);
            script = replaceOutputPlaceholders(script, new ArrayList<>(outputNames.keySet()), taskId, executor, taskCatalog);
        } catch (IllegalArgumentException e) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("script", script);

        return CommonResult.success(result, "模型参数校验成功！");
    }


    private CommonResult<?> registerExecuteOutput(CodeParams params) {
        String taskId = params.getTaskId();
        String executor = params.getExecutor();
        String sceneCatalogId = params.getSceneCatalogId();
        String outputCatalogName = params.getSceneCatalogName();
        List<String> outputFileNameList = params.getFileNames();
        DscGeoAnalysisExecTask dscCodeAnalysisExecTask = new DscGeoAnalysisExecTask();

        // 创建输出目录
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDateTime = dateFormat.format(new Date());
        String catalogName = outputCatalogName + "(" + formattedDateTime + ")";
        DscCatalog sceneCatalog = dscCatalogDAO.findDscCatalogById(sceneCatalogId);

        CreateCatalogDTO createCatalogDTO = new CreateCatalogDTO();
        createCatalogDTO.setUserId(executor);
        createCatalogDTO.setParentCatalogId(sceneCatalog.getId());
        createCatalogDTO.setCatalogName(catalogName);
        createCatalogDTO.setTaskId(taskId);

        CommonResult<String> createCatalogRes = dscCatalogService.create(createCatalogDTO);
        String outputCatalog = createCatalogRes.getData();
        String catalogPath = dscCatalogService.getCatalogPath(outputCatalog);
        String baseDir = rootPath + minioConfig.getGaOutputBucket() + File.separator + executor;
        String outputDir = baseDir + catalogPath;
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
                .setDescription(outputCatalogName + " task is running");

        dscCodeAnalysisExecTaskDAO.insert(dscCodeAnalysisExecTask);

        try {
            List<JSONObject> codeTaskOutputs = new ArrayList<>();

            for (String memberFileName : outputFileNameList) {
                String originalFilePath = codeOutPutHub + File.separator + executor + File.separator + taskId + File.separator + "output" + File.separator + memberFileName;
                File originalFile = new File(originalFilePath);

                if (!originalFile.exists()) {
                    log.warn("路径不存在：{}", originalFilePath);
                    continue;
                }

                List<File> filesToProcess = new ArrayList<>();
                String currentCatalogId = outputCatalog;
                String currentCatalogPath = catalogPath;
                String currentOutputDir = outputDir;

                // 如果是文件夹，创建对应子目录
                if (originalFile.isDirectory()) {
                    CreateCatalogDTO subCatalogDTO = new CreateCatalogDTO();
                    subCatalogDTO.setParentCatalogId(outputCatalog);
                    subCatalogDTO.setCatalogName(memberFileName);
                    subCatalogDTO.setTaskId(taskId);

                    CommonResult<String> subCatalogRes = dscCatalogService.create(subCatalogDTO);
                    currentCatalogId = subCatalogRes.getData();
                    currentCatalogPath = dscCatalogService.getCatalogPath(currentCatalogId);
                    currentOutputDir = baseDir + currentCatalogPath;

                    File subDir = new File(currentOutputDir);
                    if (!subDir.mkdirs()) {
                        dscCatalogService.delete(currentCatalogId);
                        log.warn("子目录创建失败：{}", currentCatalogId);
                        continue;
                    }

                    Collection<File> allFiles = FileUtils.listFiles(originalFile, null, true);
                    filesToProcess.addAll(allFiles);
                } else {
                    filesToProcess.add(originalFile);
                }

                for (File fileToHandle : filesToProcess) {
                    log.info("处理文件： {}", fileToHandle.getAbsolutePath());

                    String originalName = fileToHandle.getName();
                    String suffix = originalName.substring(originalName.lastIndexOf(".") + 1);
                    String logicalFileName = originalName.substring(0, originalName.lastIndexOf("."));

                    // 判断是否为 UUID 格式
                    String filePhysicalName;
                    if (logicalFileName.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")) {
                        filePhysicalName = "code result_" + IdUtil.randomUUID(); // 是 UUID，则重新生成
                    } else {
                        filePhysicalName = logicalFileName; // 不是 UUID，使用原名
                    }

                    String finalFileName = filePhysicalName + "." + suffix;
                    String minioPath = currentOutputDir + File.separator + finalFileName;

                    File targetFile = new File(minioPath);
                    try {
                        FileUtils.copyFile(fileToHandle, targetFile);
                    } catch (IOException ioException) {
                        log.error("文件拷贝失败：{}", fileToHandle.getAbsolutePath(), ioException);
                        continue;
                    }

                    JSONObject codeTaskOutput = new JSONObject();
                    try (FileInputStream fileInputStream = new FileInputStream(targetFile)) {
                        String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
                        String fileId = IdUtil.objectId();

                        DscFileInfo codeOutputFileInfo = new DscFileInfo(
                                fileId,
                                md5,
                                finalFileName,
                                suffix,
                                false,
                                executor,
                                DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"),
                                DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"),
                                targetFile.length(),
                                0L, 0L, 0L, 0L,
                                minioConfig.getGaOutputBucket(),
                                executor + currentCatalogPath + File.separator + finalFileName,
                                32
                        );
                        dscFileDAO.insert(codeOutputFileInfo);

                        InitTaskParam initTaskParam = new InitTaskParam();
                        initTaskParam.setIdentifier(md5);
                        initTaskParam.setFileName(finalFileName);
                        initTaskParam.setFileId(fileId);
                        initTaskParam.setUserId(executor);
                        initTaskParam.setTotalSize(targetFile.length());
                        initTaskParam.setChunkSize(targetFile.length());
                        initTaskParam.setObjectName(filePhysicalName);

                        TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);

                        UploadFileDTO uploadFileDTO = new UploadFileDTO(
                                executor,
                                taskInfoDTO.getTaskRecord().getId(),
                                currentCatalogId
                        );
                        dscFileService.create(uploadFileDTO, false, false);

                        codeTaskOutput.put("id", fileId);
                        codeTaskOutput.put("name", logicalFileName);
                        codeTaskOutputs.add(codeTaskOutput);
                    }
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

    private String replaceInputPlaceholders(String script, List<String> inputUrls, File taskCatalog) {
        return replacePlaceholders(script, inputUrls, "Dsc_Model_Script_Input_Param_", taskCatalog);
    }

    private String replaceOptionPlaceholders(String script, List<String> optionValues, File taskCatalog) {
        return replacePlaceholders(script, optionValues, "Dsc_Model_Script_Option_Param_", taskCatalog);
    }

    private String replaceOutputPlaceholders(String script, List<String> outputKeys, String taskId, String executor, File taskCatalog) {
        List<String> outputDirs = new ArrayList<>();
        for (String outputKey : outputKeys) {
            String fullPath = JupyterInnerOutPutHub + File.separator + executor + File.separator + taskId + File.separator + "output" + File.separator + outputKey;
            outputDirs.add(fullPath);
        }
        return replacePlaceholders(script, outputDirs, "Dsc_Model_Script_Output_Param_", taskCatalog);
    }


    private String replacePlaceholders(String script, List<String> values, String placeholderPrefix, File taskCatalog) {
        if (script == null) {
            taskCatalog.delete();
            throw new IllegalArgumentException("模型内容为空！请检查模型设置或联系管理员");
        }
        String regex = "(['\"])" + escapeRegex(placeholderPrefix) + "\\d+\\1";
        log.info("正则表达式为：{}", regex);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(script);

        List<String> placeholders = new ArrayList<>();
        while (matcher.find()) {
            placeholders.add(matcher.group());
        }

        if (placeholders.size() != values.size()) {
            if(!taskCatalog.delete()){
                log.error("未成功清理残留文件！");
            }else {
                log.info("成功清理残留文件！");
            }
            throw new IllegalArgumentException(String.format(
                    "模型参数数量(%d)与提供的数量(%d)不一致！【前缀: %s】", placeholders.size(), values.size(), placeholderPrefix));
        }

        for (int i = 0; i < placeholders.size(); i++) {
            script = script.replace(placeholders.get(i), "'" +values.get(i)+ "'");
        }

        return script;
    }

    //解决占位符有特殊字符转义问题
    private static String escapeRegex(String input) {
        return input.replaceAll("([\\\\.^$|?*+()\\[\\]{}])", "\\\\$1");
    }


}
