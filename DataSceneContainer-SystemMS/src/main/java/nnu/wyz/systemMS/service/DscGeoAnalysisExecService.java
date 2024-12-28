package nnu.wyz.systemMS.service;

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
import nnu.wyz.systemMS.enums.AvailableGisToolEnum;
import nnu.wyz.systemMS.model.DscGeoAnalysis.*;
import nnu.wyz.systemMS.model.dto.TaskInfoDTO;
import nnu.wyz.systemMS.model.dto.UploadFileDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.model.param.InitTaskParam;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscFileService;
import nnu.wyz.systemMS.service.SysUploadTaskService;
import nnu.wyz.systemMS.utils.DockerUtil;
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
    private DscComputeContainerImageDAO dscComputeContainerImageDAO;

    @Autowired
    private DscComputeContainerInstanceDAO dscComputeContainerInstanceDAO;

    @Autowired
    private DscGeoAnalysisExecTaskDAO dscGeoAnalysisExecTaskDAO;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private DscFileService dscFileService;

    @Autowired
    private SysUploadTaskService sysUploadTaskService;

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscUserDAO dscUserDAO;

    @Autowired
    private DscCatalogService dscCatalogService;

    @Autowired
    private MinioConfig minioConfig;

    @Value("${fileSavePath}")
    private String root;

    @SneakyThrows
    @Async
    public void invoke(DscGeoAnalysisExecTask dscGeoAnalysisExecTask) {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById(dscGeoAnalysisExecTask.getTargetTool().get("id").toString());
        DscGeoAnalysisTool dscGeoAnalysisTool = byId.get();
        DscUser executor = dscUserDAO.findDscUserById(dscGeoAnalysisExecTask.getExecutor().get("id").toString());
        startTask(dscGeoAnalysisExecTask, dscGeoAnalysisTool.getName());
        String identifier = dscGeoAnalysisTool.getCategory();
        Optional<DscComputeContainerImage> optional = dscComputeContainerImageDAO.findByIdentifier(identifier);
        if (!optional.isPresent()) {
            stopTask(dscGeoAnalysisExecTask, "无可用计算镜像！");
            return;
        }
        List<DscComputeContainerInstance> allAvailableImages = dscComputeContainerInstanceDAO.findAllByImageId(optional.get().getId());
        if (allAvailableImages.isEmpty()) {
            stopTask(dscGeoAnalysisExecTask, "无可用计算容器实例！");
            return;
        }
        // TODO: 容器调度
        DscComputeContainerInstance dscComputeContainerInstance = allAvailableImages.get(0);
        // TODO: 检查计算容器实例健康状态
        DockerClient dockerClient = DockerUtil.getDockerClient(dscComputeContainerInstance);
        ArrayList<GeoAnalysisOutputRecDTO> outputRecords = new ArrayList<>();
        String[] execCommand = getExecCommand(dscGeoAnalysisExecTask, outputRecords);
        log.info(Arrays.toString(execCommand));
        ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(dscComputeContainerInstance.getContainerId())
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(execCommand)
                .exec();
        PrintStream stdout = System.out;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream stderr = new PrintStream(baos);
        try {
            dockerClient.execStartCmd(cmdResponse.getId())
                    .exec(new ExecStartResultCallback(stdout, stderr) {
                        @Override
                        public void onNext(Frame frame) {
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
                //说明工具执行中出错，此时未输出文件，直接return
                if (outputRecords.size() == 0) {
                    stopTask(dscGeoAnalysisExecTask, errMsg);
                    return;
                }
            }
            //遍历输出文件目录，进行入库
            String catalogPath = dscCatalogService.getCatalogPath(dscGeoAnalysisExecTask.getParams().getWorkingDir());
            File outputDir = new File(root + minioConfig.getGaOutputBucket() + File.separator + dscGeoAnalysisExecTask.getExecutor().get("id").toString() + catalogPath);
            List<JSONObject> gaTaskOutputs = new ArrayList<>();
            for (GeoAnalysisOutputRecDTO geoAnalysisOutputRecDTO : outputRecords) {
                //拿到所有前缀一样的文件集合
                File[] files = outputDir.listFiles(pathname -> pathname.getName().startsWith(geoAnalysisOutputRecDTO.getPhysicalNameWithoutSuffix()));
                if (files != null) {
                    for (File file : files) {
                        JSONObject gaTaskOutput = new JSONObject();
                        log.info("当前文件： " + file.getName());
                        FileInputStream fileInputStream = new FileInputStream(file);
                        String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
                        String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                        String fileName = geoAnalysisOutputRecDTO.getFileNameWithoutSuffix() + file.getName().substring(file.getName().indexOf("."));
                        String fileId = IdUtil.objectId();
                        DscFileInfo dscFileInfo = new DscFileInfo(fileId, md5, fileName, suffix, false, dscGeoAnalysisExecTask.getExecutor().get("id").toString(), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), file.length(), 0L, 0L, 0L, 0L, minioConfig.getGaOutputBucket(), dscGeoAnalysisExecTask.getExecutor().getString("id") + catalogPath + File.separator + file.getName(), 32);
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
                        dscFileService.create(uploadFileDTO, false,false);
                        gaTaskOutput.put("id", fileId);
                        gaTaskOutput.put("name", fileName);
                        gaTaskOutputs.add(gaTaskOutput);
                    }
                }
            }
            dscGeoAnalysisExecTask.getParams().setOutputs(gaTaskOutputs);
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
        List<String> commands = dscGeoAnalysisTool.getInvokeCmd();
        //格式化Input输入,目前只支持对场景文件的输入
        for (DscGeoAnalysisToolInnerParams input : dscGeoAnalysisTool.getParameters().getInputs()) {
            //(tjk12.18 modify) ************************************************************************* getName ==> getIdentifier
            if (input.getIsOptional() && !dscGeoAnalysisExecTask.getParams().getInput().containsKey(input.getIdentifier())) {
                continue;
            }
            String[] inputIds = dscGeoAnalysisExecTask.getParams().getInput().get(input.getIdentifier()).split(",");
            StringBuilder totalPath = new StringBuilder();
            for (String id : inputIds) {
                Optional<DscFileInfo> byId1 = dscFileDAO.findById(id);
                DscFileInfo dscFileInfo = byId1.get();
                String filePath = root + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey();
                totalPath.append(filePath).append(";");
            }
            commands.add(MessageFormat.format("-{0}={1}", input.getIdentifier(), totalPath.substring(0, totalPath.length() - 1)));
        }
        //格式化Output输出，记录
        String catalogPath = dscCatalogService.getCatalogPath(dscGeoAnalysisExecTask.getParams().getWorkingDir());
        String outputDir = root + minioConfig.getGaOutputBucket() + File.separator + dscGeoAnalysisExecTask.getExecutor().get("id").toString() + catalogPath;
        Map<String, String> outputs = dscGeoAnalysisExecTask.getParams().getOutput();
        System.out.println("outputs="+outputs);
        for (DscGeoAnalysisToolInnerParams output : dscGeoAnalysisTool.getParameters().getOutputs()) {
            if(!outputs.containsKey((output.getIdentifier()))) {
                continue;
            }
            String filePhysicalName = IdUtil.randomUUID();
            String filePath = outputDir + File.separator + filePhysicalName;
            //表格输出不指定类型为csv会导致输出文件无后缀名，默认输出为csv
            if ("Table, output".equals(output.getType()) || "Table, output, optional".equals(output.getType())) {
                filePath += ".csv";
            }
            //指定栅格数据的格式为.tif，省略sgrd2tif的过程
            if("Grid, output".equals(output.getType()) || "Grid, output, optional".equals(output.getType())) {
                filePath += ".tif";
            }
            // 获取outputs中与output.getIdentifier()匹配的value
            String outputFileName = outputs.get(output.getIdentifier());
            //处理缺省情况
            outputFileName = (outputFileName != null) ? outputFileName : output.getName();
            System.out.println("outputFileName1="+outputFileName);
            String type = output.getType();
            // 判断 outputFileName 是否有后缀，如果有则删除对应后缀
            if (("Grid, output".equals(type) || "Grid, output, optional".equals(type)) && outputFileName.endsWith(".tif")) {
                outputFileName = outputFileName.substring(0, outputFileName.length() - 4);
            } else if (("Shapes, output".equals(type) || "Shapes, output, optional".equals(type)) && !outputFileName.endsWith(".shp")) {
                outputFileName = outputFileName.substring(0, outputFileName.length() - 4);
            }
            System.out.println("outputFileName2="+outputFileName);

            //记录输出的一系列文件
            outputRecords.add(new GeoAnalysisOutputRecDTO(filePhysicalName, outputFileName));
            commands.add(MessageFormat.format("-{0}={1}", output.getIdentifier(), filePath));
        }
        //格式化Options配置
        for (DscGeoAnalysisToolInnerParams option : dscGeoAnalysisTool.getParameters().getOptions()) {
            Map<String, Object> options = dscGeoAnalysisExecTask.getParams().getOptions();
            if (!options.containsKey(option.getIdentifier())) {
                continue;
            }
            Object o = options.get(option.getIdentifier());
            if (o == null) {
                continue;
            }
            // 当类型为Value range时，前端传的值是一个数组，为saga做特殊处理
            if (option.getType().equals("Value Range")) {
                ArrayList<Double> valueRange = (ArrayList<Double>) o;
                commands.add(MessageFormat.format("-{0}={1}", option.getIdentifier() + "_MIN", valueRange.get(0)));
                commands.add(MessageFormat.format("-{0}={1}", option.getIdentifier() + "_MAX", valueRange.get(1)));
                continue;
            }
            //当类型为Static table时，前端传递的是一个文件id
            if(option.getType().equals("Static table")) {
                Optional<DscFileInfo> byId1 = dscFileDAO.findById(o.toString());
                DscFileInfo dscFileInfo = byId1.get();
                String filePath = root + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey();
                commands.add(MessageFormat.format("-{0}={1}", option.getIdentifier(), filePath));
                continue;
            }
            commands.add(MessageFormat.format("-{0}={1}", option.getIdentifier(), o));
        }
        System.out.println(commands);
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
