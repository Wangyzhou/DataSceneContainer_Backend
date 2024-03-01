package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscGeoAnalysisDAO;
import nnu.wyz.systemMS.dao.DscGeoAnalysisExecTaskDAO;
import nnu.wyz.systemMS.dao.DscUserDAO;
import nnu.wyz.systemMS.model.DscGeoAnalysis.*;
import nnu.wyz.systemMS.model.dto.CreateCatalogDTO;
import nnu.wyz.systemMS.model.entity.DscCatalog;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscGeoAnalysisExecService;
import nnu.wyz.systemMS.service.DscGeoAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 10:04
 */
@Service
public class DscGeoAnalysisServiceIml implements DscGeoAnalysisService {

    @Autowired
    private DscGeoAnalysisExecService dscGeoAnalysisExecService;

    @Value("${fileSavePath}")
    private String rootPath;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private DscGeoAnalysisExecTaskDAO dscGeoAnalysisExecTaskDAO;

    @Autowired
    private DscCatalogDAO dscCatalogDAO;

    @Autowired
    private DscCatalogService dscCatalogService;

    @Autowired
    private DscGeoAnalysisDAO dscGeoAnalysisDAO;

    @Autowired
    private DscUserDAO dscUserDAO;

    @Override
    public CommonResult<DscGeoAnalysisExecTask> submitGATask(DscGAInvokeParams params) {
        //TODO:参数校验
        CommonResult<String> examineParamRes = this.examineParams(params);
        if(examineParamRes.getCode() != 200){
            return CommonResult.failed(examineParamRes.getMessage());
        }
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById(params.getToolId());
        DscGeoAnalysisTool tool = byId.get();
        DscGeoAnalysisExecTask dscGeoAnalysisExecTask = new DscGeoAnalysisExecTask();
        String taskId = IdUtil.randomUUID();
        String executor = params.getExecutor();
        //创建工具输出目录
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDateTime = dateFormat.format(new Date());
        DscCatalog sceneCatalog = dscCatalogDAO.findDscCatalogById(params.getSceneCatalog());
        CreateCatalogDTO createCatalogDTO = new CreateCatalogDTO();
        createCatalogDTO.setUserId(executor);
        createCatalogDTO.setParentCatalogId(sceneCatalog.getId());
        createCatalogDTO.setCatalogName(formattedDateTime);
        createCatalogDTO.setTaskId(taskId);
        CommonResult<String> createCatalogRes = dscCatalogService.create(createCatalogDTO);
        String outputCatalog = createCatalogRes.getData();
        String catalogPath = dscCatalogService.getCatalogPath(outputCatalog);
        String outputDir = rootPath + minioConfig.getGaOutputBucket() + File.separator + executor + catalogPath;
        File file = new File(outputDir);
        boolean isMakeDir = file.mkdirs();
        if (!isMakeDir) {
            dscCatalogService.delete(outputCatalog);
            return CommonResult.failed("创建任务失败!");
        }
        DscGARawParams dscGARawParams = new DscGARawParams();
        dscGARawParams.setWorkingDir(outputCatalog);
        dscGARawParams.setInput(params.getInput());
        dscGARawParams.setOptions(params.getOptions());
        JSONObject targetTool = new JSONObject();
        targetTool.put("id", tool.getId());
        targetTool.put("name", tool.getName());
        JSONObject executorJson = new JSONObject();
        executorJson.put("id", executor);
        executorJson.put("name", dscUserDAO.findDscUserById(executor).getUserName());
        dscGeoAnalysisExecTask.setId(taskId)
                .setTargetTool(targetTool)
                .setParams(dscGARawParams)
                .setExecutor(executorJson)
                .setStatus(-2);
        dscGeoAnalysisExecTaskDAO.insert(dscGeoAnalysisExecTask);
        //异步执行任务
        dscGeoAnalysisExecService.invoke(dscGeoAnalysisExecTask);
        return CommonResult.success(dscGeoAnalysisExecTask, "任务已提交运行！");
    }

    @Override
    public CommonResult<DscGeoAnalysisExecTask> getGATask(String taskId) {
        Optional<DscGeoAnalysisExecTask> byId = dscGeoAnalysisExecTaskDAO.findById(taskId);
        return byId.map(dscGeoAnalysisExecTask -> CommonResult.success(dscGeoAnalysisExecTask, "获取任务成功")).orElseGet(() -> CommonResult.failed("未找到该任务"));
    }

    CommonResult<String> examineParams(DscGAInvokeParams params) {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById(params.getToolId());
        DscGeoAnalysisTool tool;
        if (!byId.isPresent()) {
            return CommonResult.failed("工具不可用!");
        }
        tool = byId.get();
        for (DscGeoAnalysisToolInnerParams option : tool.getParameters().getOptions()) {
            if (option.getConstraints().getMinimum() != null && (Double) params.getOptions().get(option.getName()) < option.getConstraints().getMinimum()) {
                return CommonResult.failed(option.getName() + ": " + "value must be greater than " + option.getConstraints().getMinimum());
            }
            if (option.getConstraints().getMaximum() != null && (Double) params.getOptions().get(option.getName()) > option.getConstraints().getMaximum()) {
                return CommonResult.failed(option.getName() + ": " + "value must be less than " + option.getConstraints().getMaximum());
            }
        }
        return CommonResult.success("参数校验成功！");
    }

}
