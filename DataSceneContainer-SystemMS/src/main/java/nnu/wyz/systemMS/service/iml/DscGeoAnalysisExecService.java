//package nnu.wyz.systemMS.service.iml;
//
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.core.util.IdUtil;
//import com.alibaba.fastjson.JSONObject;
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.command.ExecCreateCmdResponse;
//import com.github.dockerjava.api.model.Frame;
//import com.github.dockerjava.core.command.ExecStartResultCallback;
//import lombok.extern.slf4j.Slf4j;
//import nnu.wyz.systemMS.config.MinioConfig;
//import nnu.wyz.systemMS.dao.DscFileDAO;
//import nnu.wyz.systemMS.dao.DscGeoAnalysisDAO;
//import nnu.wyz.systemMS.dao.DscGeoAnalysisExecTaskDAO;
//import nnu.wyz.systemMS.dao.DscGeoToolsDAO;
//import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
//import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;
//import nnu.wyz.systemMS.model.dto.TaskInfoDTO;
//import nnu.wyz.systemMS.model.dto.UploadFileDTO;
//import nnu.wyz.systemMS.model.entity.*;
//import nnu.wyz.systemMS.model.param.DscToolRawParams;
//import nnu.wyz.systemMS.model.param.InitTaskParam;
//import nnu.wyz.systemMS.service.DscCatalogService;
//import nnu.wyz.systemMS.service.DscFileService;
//import nnu.wyz.systemMS.service.SysUploadTaskService;
//import nnu.wyz.systemMS.websocket.WebSocketServer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.util.DigestUtils;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.text.MessageFormat;
//import java.util.*;
//
///**
// * @description:
// * @author: yzwang
// * @time: 2023/12/13 14:37
// */
//@Service
//@Slf4j
//public class DscGeoAnalysisExecService {
//
//    @Autowired
//    private DscGeoAnalysisExecTaskDAO dscGeoAnalysisExecTaskDAO;
//
//    @Autowired
//    private DscGeoAnalysisDAO dscGeoAnalysisToolDAO;
//
//    @Autowired
//    private DscFileDAO dscFileDAO;
//
//    @Autowired
//    private DscCatalogService dscCatalogService;
//
//    @Autowired
//    private MinioConfig minioConfig;
//
//    @Value("${fileSavePath}")
//    private String rootPath;
//
//    @Autowired
//    private DockerClient dockerClient;
//
//    @Autowired
//    private SysUploadTaskService sysUploadTaskService;
//
//    @Autowired
//    private DscFileService dscFileService;
//
//    @Autowired
//    private WebSocketServer webSocketServer;
//
//    private static final String CONTAINER_ID = "963ce847c1511e3913db97ac6d5145d364f481390775ecc7905a4d631fd9b67e";
//
//    @Async
//    void execute(DscGeoAnalysisExecTask dscGeoAnalysisExecTask) {
//        //执行工具
//        String toolId = dscGeoAnalysisExecTask.getTargetTool();
//        String executor = dscGeoAnalysisExecTask.getExecutor();
//        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisToolDAO.findById(toolId);
//        if (!byId.isPresent()) {
//            stopTask(dscGeoAnalysisExecTask, "This tool does not exist!");
//            return;
//        }
//        DscGeoAnalysisTool tool = byId.get();
//        String toolName = tool.getName();
//        //更新任务状态
//        startTask(dscGeoAnalysisExecTask, toolName);
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        JSONObject toolParams = tool.getParameter();
//        ArrayList<OutputFileRecord> outputFileRecords = new ArrayList<>();     //定义输出文件记录集合
//        String[] params = this.getExecCommand();
//        log.info(Arrays.toString(params));
//        ExecCreateCmdResponse containerResponse = dockerClient.execCreateCmd(CONTAINER_ID)
//                .withAttachStdout(true)
//                .withAttachStderr(true)
//                .withCmd(params)
//                .exec();
//        PrintStream stdout = System.out;
//        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        PrintStream stderr = new PrintStream(baos);
//        try {
//            dockerClient.execStartCmd(containerResponse.getId())
//                    .exec(new ExecStartResultCallback(stdout, stderr) {
//                        @Override
//                        public void onNext(Frame frame) {
//                            String logs = frame.toString().replace("STDOUT: ", "").replace("STDERR: ", "");
////                            log.info(logs);
//                            Message message = new Message();
//                            message.setFrom("system")
//                                    .setTo("1132691603@qq.com")
//                                    .setType("tool-execute")
//                                    .setTopic(toolName)
//                                    .setText(logs)
//                                    .setIsRead(false);
//                            webSocketServer.sendInfo("1132691603@qq.com", JSONObject.toJSONString(message));
//                            super.onNext(frame);
//                        }
//                    })
//                    .awaitCompletion();
//            //工具执行出错
//
//            final String utf8 = StandardCharsets.UTF_8.name();
//            String errMsg = baos.toString(utf8);
//            if (!Objects.equals(errMsg, "")) {
//                log.error(errMsg);
//                stopTask(dscGeoAnalysisExecTask, errMsg);
//                return;
//            }
//            //TODO: 输出文件入库
//            if (outputFileRecords.size() > 0) {
//                for (OutputFileRecord outputFileRecord : outputFileRecords) {
//                    DscFileInfo fileInfo = outputFileRecord.getFile();
//                    String catalogId = outputFileRecord.getCatalogId();
//                    InitTaskParam initTaskParam = new InitTaskParam();
//                    String filePhysicalPath = rootPath + fileInfo.getBucketName() + File.separator + fileInfo.getObjectKey();
//                    File file = new File(filePhysicalPath);
//                    if (!file.exists()) {
//                        stopTask(dscGeoToolExecTask, "Unknown error!");
//                        return;
//                    }
//                    String objectKey = fileInfo.getObjectKey();
//                    String[] split = objectKey.split("/");
//                    String objectName = split[1].substring(0, split[1].lastIndexOf("."));
//                    String md5 = DigestUtils.md5DigestAsHex(Files.newInputStream(Paths.get(filePhysicalPath)));
//                    String time = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
//                    Long size = file.length();
//                    fileInfo.setMd5(md5).setCreatedTime(time).setUpdatedTime(time).setSize(size);
//                    dscFileDAO.insert(fileInfo);
//                    initTaskParam.setIdentifier(md5);
//                    initTaskParam.setFileName(fileInfo.getFileName());
//                    initTaskParam.setFileId(fileInfo.getId());
//                    initTaskParam.setUserId(executor);
//                    initTaskParam.setTotalSize(size);
//                    initTaskParam.setChunkSize(size);
//                    initTaskParam.setObjectName(objectName);
//                    TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);
//                    UploadFileDTO uploadFileDTO = new UploadFileDTO(executor, taskInfoDTO.getTaskRecord().getId(), catalogId);
//                    dscFileService.create(uploadFileDTO);
//                }
//            }
//            finishTask(dscGeoAnalysisExecTask, toolName);
//            baos.close();
//        } catch (InterruptedException | IOException e) {
//            stopTask(dscGeoAnalysisExecTask, e.getMessage());
//        } finally {
//            stdout.close();
//            stderr.close();
//        }
//    }
//
//    private String[] getExecCommand(String executor, String toolName, List<DscToolRawParams> invokeParams, List<Map<String, Object>> parameters, ArrayList<OutputFileRecord> outputFileRecords) {
//        ArrayList<String> params = new ArrayList<>();
//        params.add("whitebox_tools");
//        params.add("-r=" + toolName);
//        params.add("-v");
//        for (int i = 0; i < parameters.size(); i++) {
//            DscToolRawParams invokeParam = invokeParams.get(i);
//            if (invokeParam.getValue() == null || invokeParam.getValue().isEmpty()) {
//                continue;
//            }
//            String parameterType = parameters.get(i).get("parameter_type").toString();
//            Object flags = parameters.get(i).get("flags");
//            String flag = ((List<String>) flags).get(0);
//            if (parameterType.contains("ExistingFile")) {
//                String fileId = invokeParam.getValue();
//                Optional<DscFileInfo> dscFileDAOById = dscFileDAO.findById(fileId);
//                if (!dscFileDAOById.isPresent()) {
//                    throw new RuntimeException("未找到该文件");
//                }
//                DscFileInfo fileInfo = dscFileDAOById.get();
//                String filePath = rootPath + fileInfo.getBucketName() + File.separator + fileInfo.getObjectKey();
//                params.add(MessageFormat.format("{0}={1}", flag, filePath));
//                continue;
//            } else if (parameterType.contains("NewFile")) {
//                //TODO: 1、根据extra的目录id找到目录的物理路径 2、组装新文件路径
//                String fileId = IdUtil.objectId();
//                String fileName = invokeParam.getValue();
//                String suffix = fileName.substring(fileName.lastIndexOf("."));
//                String physicalName = IdUtil.randomUUID() + suffix;
//                String objectKey = executor + "/" + physicalName;
//                String filePath = rootPath + minioConfig.getBucketName() + File.separator + objectKey;
//                DscFileInfo fileInfo = new DscFileInfo(fileId, null, fileName, suffix, false, executor, null, null, null, 0L, 0L, 0L, 1L, minioConfig.getBucketName(), objectKey, 32);
//                outputFileRecords.add(new OutputFileRecord().setFile(fileInfo).setCatalogId(invokeParam.getExtra()));
//                params.add(MessageFormat.format("{0}={1}", flag, filePath));
//                continue;
//            }
//            params.add(MessageFormat.format("{0}={1}", flag, invokeParam.getValue()));
//        }
//        return params.toArray(new String[params.size()]);
//    }
//
//
//    private String[] getExecCommand() {
//
//    }
//    void startTask(DscGeoAnalysisExecTask dscGeoAnalysisExecTask, String toolName) {
//        dscGeoAnalysisExecTask.setStatus(GeoToolExecTaskStatus.RUNNING);
//        dscGeoAnalysisExecTask.setStartTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
//        dscGeoAnalysisExecTask.setDescription(toolName + " is running.");
//        dscGeoAnalysisExecTaskDAO.save(dscGeoAnalysisExecTask);
//    }
//
//    void stopTask(DscGeoAnalysisExecTask dscGeoAnalysisExecTask, String message) {
//        dscGeoAnalysisExecTask.setStatus(GeoToolExecTaskStatus.FAILED);
//        dscGeoAnalysisExecTask.setEndTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
//        dscGeoAnalysisExecTask.setDescription(message);
//        dscGeoAnalysisExecTaskDAO.save(dscGeoAnalysisExecTask);
//    }
//
//    void finishTask(DscGeoAnalysisExecTask dscGeoAnalysisExecTask, String toolName) {
//        dscGeoAnalysisExecTask.setStatus(GeoToolExecTaskStatus.SUCCEED);
//        dscGeoAnalysisExecTask.setEndTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
//        dscGeoAnalysisExecTask.setDescription(toolName + " has been finished.");
//        dscGeoAnalysisExecTaskDAO.save(dscGeoAnalysisExecTask);
//    }
//
//    void deleteOutputFiles(ArrayList<OutputFileRecord> outputFileRecords) {
//        for (OutputFileRecord outputFileRecord : outputFileRecords) {
//
//        }
//    }
//}
