package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscGeoToolExecTaskDAO;
import nnu.wyz.systemMS.dao.DscGeoToolsDAO;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.DscGeoToolExecTask;
import nnu.wyz.systemMS.model.entity.DscGeoTools;
import nnu.wyz.systemMS.model.entity.GeoToolExecTaskStatus;
import nnu.wyz.systemMS.model.param.DscToolRawParams;
import nnu.wyz.systemMS.service.DscCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/13 14:37
 */
@Service
public class DscGeoToolExecService {

    @Autowired
    private DscGeoToolExecTaskDAO dscGeoToolExecTaskDAO;

    @Autowired
    private DscGeoToolsDAO dscGeoToolsDAO;

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscCatalogService dscCatalogService;

    @Autowired
    private MinioConfig minioConfig;

    @Value("${fileSavePath}")
    private String rootPath;

    @Autowired
    private DockerClient dockerClient;

    private static final String CONTAINER_ID = "963ce847c1511e3913db97ac6d5145d364f481390775ecc7905a4d631fd9b67e";

    @Async
    void execute(DscGeoToolExecTask dscGeoToolExecTask) {
        //执行工具
        String toolId = dscGeoToolExecTask.getTargetTool();
        Optional<DscGeoTools> byId = dscGeoToolsDAO.findById(toolId);
        if (!byId.isPresent()) {
            stopTask(dscGeoToolExecTask, "This tool does not exist!");
            return;
        }
        DscGeoTools tool = byId.get();
        String toolName = tool.getName();
        //更新任务状态
        startTask(dscGeoToolExecTask, toolName);
        JSONObject toolParams = tool.getParameter();
        ArrayList<DscFileInfo> outputFiles = new ArrayList<>();     //定义输出文件集合
        String[] params = this.getExecCommand(dscGeoToolExecTask.getExecutor(), toolName, dscGeoToolExecTask.getParams(), (List<Map<String, Object>>) toolParams.get("parameters"), outputFiles);
        ExecCreateCmdResponse containerResponse = dockerClient.execCreateCmd(CONTAINER_ID)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(params)
                .exec();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        try {
            dockerClient.execStartCmd(containerResponse.getId())
                    .exec(new ExecStartResultCallback(stdout, stderr) {
                        @Override
                        public void onNext(Frame frame) {
                            //TODO: WebSocket传输工具执行输出
                            System.out.println(frame.toString().replace("STDOUT: ", "").replace("STDERR: ", ""));
                            super.onNext(frame);
                        }
                    })
                    .awaitCompletion();
            //工具执行出错
            if(!Objects.equals(stderr.toString(), "")) {
                stopTask(dscGeoToolExecTask, stderr.toString());
                return;
            }
            //TODO: 输出文件入库
            if(outputFiles.size() > 0){

            }
            finishTask(dscGeoToolExecTask, toolName);
        } catch (InterruptedException e) {
            stopTask(dscGeoToolExecTask, e.getMessage());
        }
    }

    private String[] getExecCommand(String executor, String toolName, List<DscToolRawParams> invokeParams, List<Map<String, Object>> parameters, ArrayList<DscFileInfo> outputFiles) {
        ArrayList<String> params = new ArrayList<>();
        params.add("whitebox_tools");
        params.add("-r=" + toolName);
        for (int i = 0; i < parameters.size(); i++) {
            DscToolRawParams invokeParam = invokeParams.get(i);
            if (invokeParam.getValue() == null || invokeParam.getValue().isEmpty()) {
                continue;
            }
            String parameterType = parameters.get(i).get("parameter_type").toString();
            Object flags = parameters.get(i).get("flags");
            String flag = ((List<String>) flags).get(0);
            if (parameterType.contains("ExistingFile")) {
                String fileId = invokeParam.getValue();
                Optional<DscFileInfo> dscFileDAOById = dscFileDAO.findById(fileId);
                if (!dscFileDAOById.isPresent()) {
                    throw new RuntimeException("未找到该文件");
                }
                DscFileInfo fileInfo = dscFileDAOById.get();
                String filePath = rootPath + fileInfo.getBucketName() + File.separator + fileInfo.getObjectKey();
                params.add(MessageFormat.format("{0}={1}", flag, filePath));
                continue;
            } else if (parameterType.contains("NewFile")) {
                //TODO: 1、根据extra的目录id找到目录的物理路径 2、组装新文件路径
                String fileId = IdUtil.objectId();
                String fileName = invokeParam.getValue();
                String suffix = fileName.substring(fileName.lastIndexOf("."));
                String physicalName = IdUtil.randomUUID() + suffix;
                String objectKey = executor + "/" + physicalName;
                String filePath = rootPath + minioConfig.getBucketName() + File.separator + objectKey;
                DscFileInfo fileInfo = new DscFileInfo(fileId, null, fileName, suffix, false, executor, null, null, null, 0L, 0L, 0L, 0L, minioConfig.getBucketName(), objectKey, 32);
                outputFiles.add(fileInfo);
                params.add(MessageFormat.format("{0}={1}", flag, filePath));
                continue;
            }
            params.add(MessageFormat.format("{0}={1}", flag, invokeParam.getValue()));
        }
        return params.toArray(new String[params.size()]);
    }

    void startTask(DscGeoToolExecTask dscGeoToolExecTask, String toolName) {
        dscGeoToolExecTask.setStatus(GeoToolExecTaskStatus.RUNNING);
        dscGeoToolExecTask.setStartTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscGeoToolExecTask.setDescription(toolName + " is running.");
        dscGeoToolExecTaskDAO.save(dscGeoToolExecTask);
    }

    void stopTask(DscGeoToolExecTask dscGeoToolExecTask, String message) {
        dscGeoToolExecTask.setStatus(GeoToolExecTaskStatus.FAILED);
        dscGeoToolExecTask.setEndTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscGeoToolExecTask.setDescription(message);
        dscGeoToolExecTaskDAO.save(dscGeoToolExecTask);
    }

    void finishTask(DscGeoToolExecTask dscGeoToolExecTask, String toolName) {
        dscGeoToolExecTask.setStatus(GeoToolExecTaskStatus.SUCCEED);
        dscGeoToolExecTask.setEndTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscGeoToolExecTask.setDescription(toolName + " is finished.");
        dscGeoToolExecTaskDAO.save(dscGeoToolExecTask);
    }
}
