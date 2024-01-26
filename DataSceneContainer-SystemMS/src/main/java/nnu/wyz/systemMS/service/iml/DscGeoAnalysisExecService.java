package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.SagaDockerConfig;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.DscGeoAnalysis.*;
import nnu.wyz.systemMS.model.dto.TaskInfoDTO;
import nnu.wyz.systemMS.model.dto.UploadFileDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.model.param.InitTaskParam;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscFileService;
import nnu.wyz.systemMS.service.SysUploadTaskService;
import nnu.wyz.systemMS.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 22:36
 */
@Service
@Slf4j
public class DscGeoAnalysisExecService {

    @Autowired
    private DscGeoAnalysisDAO dscGeoAnalysisDAO;

    @Autowired
    private DscGeoAnalysisExecTaskDAO dscGeoAnalysisExecTaskDAO;

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscCatalogService dscCatalogService;

    @Autowired
    private SagaDockerConfig sagaDockerConfig;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private DscUserDAO dscUserDAO;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private DscFileService dscFileService;

    @Autowired
    private SysUploadTaskService sysUploadTaskService;
    @Value("${fileSavePath}")
    private String root;

    private static final String CONTAINER_ID = "e904431d1b38ec6fba77361019321875536deaf0169ebd6872af66bbab67d879";

    @SneakyThrows
    @Async
    public void invoke(DscGeoAnalysisExecTask dscGeoAnalysisExecTask) {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById(dscGeoAnalysisExecTask.getTargetTool().get("id").toString());
        if (!byId.isPresent()) {
            stopTask(dscGeoAnalysisExecTask, "Target tool not found.");
            return;
        }
        DscGeoAnalysisTool dscGeoAnalysisTool = byId.get();
        startTask(dscGeoAnalysisExecTask, dscGeoAnalysisTool.getName());
        DscUser executor = dscUserDAO.findDscUserById(dscGeoAnalysisExecTask.getExecutor().get("id").toString());
        ArrayList<GeoAnalysisOutputRecDTO> outputRecords = new ArrayList<>();
        String[] execCommand = getExecCommand(dscGeoAnalysisExecTask, outputRecords);
        log.info(Arrays.toString(execCommand));
        ExecCreateCmdResponse containerResponse = sagaDockerConfig.getDockerClient().execCreateCmd(CONTAINER_ID)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(execCommand)
                .exec();
        PrintStream stdout = System.out;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream stderr = new PrintStream(baos);
        try {
            sagaDockerConfig.getDockerClient().execStartCmd(containerResponse.getId())
                    .exec(new ExecStartResultCallback(stdout, stderr) {
                        @Override
                        public void onNext(Frame frame) {
//                            String logs = frame.toString().replace("STDOUT: ", "").replace("STDERR: ", "");
//                            log.info(logs);
                            Message message = new Message();
                            message.setFrom("system")
                                    .setTo(executor.getEmail())
                                    .setType("tool-execute")
                                    .setTopic(dscGeoAnalysisTool.getName())
                                    .setText(frame.toString())
                                    .setIsRead(false);
                            webSocketServer.sendInfo(executor.getEmail(), JSONObject.toJSONString(message));
                            super.onNext(frame);
                        }
                    })
                    .awaitCompletion();
            //工具执行出错
            final String utf8 = StandardCharsets.UTF_8.name();
            String errMsg = baos.toString(utf8);
            if (!Objects.equals(errMsg, "")) {
                log.error(errMsg);
                stopTask(dscGeoAnalysisExecTask, errMsg);
                if (outputRecords.size() == 0) {     //说明工具执行中出错，此时未输出文件，直接return
                    return;
                }
            }
            //遍历输出文件目录，进行入库
            String catalogPath = dscCatalogService.getCatalogPath(dscGeoAnalysisExecTask.getParams().getWorkingDir());
            File outputDir = new File(root + minioConfig.getGaOutputBucket() + File.separator + dscGeoAnalysisExecTask.getExecutor() + catalogPath);
            for (GeoAnalysisOutputRecDTO geoAnalysisOutputRecDTO : outputRecords) {
                //拿到所有前缀一样的文件集合
                File[] files = outputDir.listFiles(pathname -> pathname.getName().startsWith(geoAnalysisOutputRecDTO.getPhysicalNameWithoutSuffix()));
                if (files != null) {
                    for (File file : files) {
                        log.info("当前文件： " + file.getName());
                        FileInputStream fileInputStream = new FileInputStream(file);
                        String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
                        String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                        String fileName = geoAnalysisOutputRecDTO.getFileNameWithoutSuffix() + file.getName().substring(file.getName().indexOf("."));
                        String fileId = IdUtil.objectId();
                        DscFileInfo dscFileInfo = new DscFileInfo(fileId, md5, fileName, suffix, false, dscGeoAnalysisExecTask.getExecutor().get("id").toString(), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), file.length(), 0L, 0L, 0L, 0L, minioConfig.getGaOutputBucket(), dscGeoAnalysisExecTask.getExecutor() + catalogPath + File.separator + file.getName(), 32);
                        dscFileDAO.insert(dscFileInfo);
                        InitTaskParam initTaskParam = new InitTaskParam();
                        initTaskParam.setIdentifier(md5);
                        initTaskParam.setFileName(file.getName());
                        initTaskParam.setFileId(fileId);
                        initTaskParam.setUserId(dscGeoAnalysisExecTask.getExecutor().get("id").toString());
                        initTaskParam.setTotalSize(file.length());
                        initTaskParam.setChunkSize(file.length());
                        initTaskParam.setObjectName(file.getName().substring(0, file.getName().lastIndexOf(".")));
                        TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);
                        UploadFileDTO uploadFileDTO = new UploadFileDTO(dscGeoAnalysisExecTask.getExecutor().get("id").toString(), taskInfoDTO.getTaskRecord().getId(), dscGeoAnalysisExecTask.getParams().getWorkingDir());
                        dscFileService.create(uploadFileDTO);
                    }
                }
            }
            finishTask(dscGeoAnalysisExecTask, dscGeoAnalysisTool.getName());
        } catch (InterruptedException | IOException e) {
            stopTask(dscGeoAnalysisExecTask, e.getMessage());
        } finally {
            stderr.close();
            baos.close();
        }
    }

    String[] getExecCommand(DscGeoAnalysisExecTask dscGeoAnalysisExecTask, ArrayList<GeoAnalysisOutputRecDTO> outputRecords) {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById(dscGeoAnalysisExecTask.getTargetTool().get("id").toString());
        DscGeoAnalysisTool dscGeoAnalysisTool = byId.get();
        ArrayList<String> commands = new ArrayList<>();
        commands.add("saga_cmd");
        commands.add(dscGeoAnalysisTool.getLibrary());
        commands.add(dscGeoAnalysisTool.getIdentifier());
        //格式化Input输入,目前只支持对场景文件的输入
        for (DscGeoAnalysisToolInnerParams input : dscGeoAnalysisTool.getParameters().getInputs()) {
            if(input.getIsOptional() && !dscGeoAnalysisExecTask.getParams().getInput().containsKey(input.getName())){
                continue;
            }
            Optional<DscFileInfo> byId1 = dscFileDAO.findById(dscGeoAnalysisExecTask.getParams().getInput().get(input.getName()));
            DscFileInfo dscFileInfo = byId1.get();
            String filePath = root + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey();
            commands.add(MessageFormat.format("-{0}={1}", input.getIdentifier(), filePath));
        }
        //格式化Output输出，记录
        String catalogPath = dscCatalogService.getCatalogPath(dscGeoAnalysisExecTask.getParams().getWorkingDir());
        String outputDir = root + minioConfig.getGaOutputBucket() + File.separator + dscGeoAnalysisExecTask.getExecutor() + catalogPath;
        for (DscGeoAnalysisToolInnerParams output : dscGeoAnalysisTool.getParameters().getOutputs()) {
            String filePhysicalName = IdUtil.randomUUID();
            String filePath = outputDir + File.separator + filePhysicalName;
            if (output.getType().equals("Table, output") || output.getType().equals("Table, output, optional")) {
                filePath += ".csv";     //表格输出不指定类型为csv会导致输出文件无后缀名，默认输出为csv
            }
            outputRecords.add(new GeoAnalysisOutputRecDTO(filePhysicalName, output.getName())); //记录输出的一系列文件
            commands.add(MessageFormat.format("-{0}={1}", output.getIdentifier(), filePath));
        }
        //格式化Options配置
        for (DscGeoAnalysisToolInnerParams option : dscGeoAnalysisTool.getParameters().getOptions()) {
            Map<String, Object> options = dscGeoAnalysisExecTask.getParams().getOptions();
            if(!options.containsKey(option.getName())){
                continue;
            }
            Object o = options.get(option.getName());
            if (o == null) {
                continue;
            }
            if(option.getType().equals("Value Range")) {

            }
            commands.add(MessageFormat.format("-{0}={1}", option.getIdentifier(), o));
        }
        return commands.toArray(new String[commands.size()]);
    }

    void startTask(DscGeoAnalysisExecTask dscGeoAnalysisExecTask, String toolName) {
        dscGeoAnalysisExecTask.setStatus(GeoToolExecTaskStatus.RUNNING);
        dscGeoAnalysisExecTask.setStartTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscGeoAnalysisExecTask.setDescription(toolName + " is running.");
        dscGeoAnalysisExecTaskDAO.save(dscGeoAnalysisExecTask);
    }

    void stopTask(DscGeoAnalysisExecTask dscGeoAnalysisExecTask, String message) {
        dscGeoAnalysisExecTask.setStatus(GeoToolExecTaskStatus.FAILED);
        dscGeoAnalysisExecTask.setEndTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscGeoAnalysisExecTask.setDescription(message);
        dscGeoAnalysisExecTaskDAO.save(dscGeoAnalysisExecTask);
    }

    void finishTask(DscGeoAnalysisExecTask dscGeoAnalysisExecTask, String toolName) {
        dscGeoAnalysisExecTask.setStatus(GeoToolExecTaskStatus.SUCCEED);
        dscGeoAnalysisExecTask.setEndTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscGeoAnalysisExecTask.setDescription(toolName + " is finished.");
        dscGeoAnalysisExecTaskDAO.save(dscGeoAnalysisExecTask);
    }
}
