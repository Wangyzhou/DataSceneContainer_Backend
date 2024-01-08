package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.DscGeoAnalysis.*;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
    private DockerClient dockerClient;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private DscUserDAO dscUserDAO;

    @Autowired
    private WebSocketServer webSocketServer;
    @Value("${fileSavePath}")
    private String root;

    private static final String CONTAINER_ID = "e904431d1b38ec6fba77361019321875536deaf0169ebd6872af66bbab67d879";

    @Async
    public void invoke(DscGeoAnalysisExecTask dscGeoAnalysisExecTask) {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById(dscGeoAnalysisExecTask.getTargetTool());
        if (!byId.isPresent()) {
            stopTask(dscGeoAnalysisExecTask, "Target tool not found.");
            return;
        }
        DscGeoAnalysisTool dscGeoAnalysisTool = byId.get();
        startTask(dscGeoAnalysisExecTask, dscGeoAnalysisTool.getName());
        DscUser executor = dscUserDAO.findDscUserById(dscGeoAnalysisExecTask.getExecutor());
        ArrayList<GeoAnalysisOutputRecDTO> outputRecords = new ArrayList<>();
        String[] execCommand = getExecCommand(dscGeoAnalysisExecTask, outputRecords);
        ExecCreateCmdResponse containerResponse = dockerClient.execCreateCmd(CONTAINER_ID)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(execCommand)
                .exec();
        PrintStream stdout = System.out;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream stderr = new PrintStream(baos);
        try {
            dockerClient.execStartCmd(containerResponse.getId())
                    .exec(new ExecStartResultCallback(stdout, stderr) {
                        @Override
                        public void onNext(Frame frame) {
                            String logs = frame.toString().replace("STDOUT: ", "").replace("STDERR: ", "");
//                            log.info(logs);
                            Message message = new Message();
                            message.setFrom("system")
                                    .setTo(executor.getEmail())
                                    .setType("tool-execute")
                                    .setTopic(dscGeoAnalysisTool.getName())
                                    .setText(logs)
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
                return;
            }
            //遍历输出文件目录，进行入库
            baos.close();
        } catch (InterruptedException | IOException e) {
            stopTask(dscGeoAnalysisExecTask, e.getMessage());
        } finally {
            stdout.close();
            stderr.close();
        }
    }

    String[] getExecCommand(DscGeoAnalysisExecTask
                                    dscGeoAnalysisExecTask, ArrayList<GeoAnalysisOutputRecDTO> outputRecords) {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById(dscGeoAnalysisExecTask.getTargetTool());
        DscGeoAnalysisTool dscGeoAnalysisTool = byId.get();
        ArrayList<String> commands = new ArrayList<>();
        commands.add("saga_cmd.exe");
        commands.add(dscGeoAnalysisTool.getLibirary());
        commands.add(dscGeoAnalysisTool.getIdentifier().toString());
        //格式化Input输入,目前只支持对场景文件的输入
        for (DscGeoAnalysisToolParams input : dscGeoAnalysisTool.getInput()) {
            Optional<DscFileInfo> byId1 = dscFileDAO.findById(dscGeoAnalysisExecTask.getParams().getInput().get(input.getName()));
            DscFileInfo dscFileInfo = byId1.get();
            String filePath = root + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey();
            commands.add(MessageFormat.format("{0}={1}", input.getFlag(), filePath));
        }
        //格式化Output输出，记录
        String catalogPath = dscCatalogService.getCatalogPath(dscGeoAnalysisExecTask.getParams().getWorkingDir());
        String outputDir = root + minioConfig.getGaOutputBucket() + File.separator + dscGeoAnalysisExecTask.getExecutor() + catalogPath;
        for (DscGeoAnalysisToolParams output : dscGeoAnalysisTool.getOutput()) {
            String filePhysicalName = IdUtil.randomUUID();
            String filePath = outputDir + File.separator + filePhysicalName;
            if (output.getType().equals("Table (output)")) {
                filePath += ".csv";     //表格输出不指定类型为csv会导致输出文件无后缀名，默认输出为csv
            }
            outputRecords.add(new GeoAnalysisOutputRecDTO(filePhysicalName, output.getName())); //记录输出的一系列文件
            commands.add(MessageFormat.format("{0}={1}", output.getFlag(), filePath));
        }
        //格式化Options配置
        for (DscGeoAnalysisToolParams option : dscGeoAnalysisTool.getOptions()) {
            Map<String, Object> options = dscGeoAnalysisExecTask.getParams().getOptions();
            Object o = options.get(option.getName());
            if (o == null) {
                continue;
            }
            commands.add(MessageFormat.format("{0}={1}", option.getFlag(), o));
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
