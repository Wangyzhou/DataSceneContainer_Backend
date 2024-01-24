package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.util.IdUtil;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscGeoAnalysisExecTaskDAO;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
import nnu.wyz.systemMS.model.dto.CreateCatalogDTO;
import nnu.wyz.systemMS.model.entity.DscCatalog;
import nnu.wyz.systemMS.model.entity.DscGeoToolExecTask;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGAInvokeParams;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGARawParams;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscGeoAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Override
    public CommonResult<DscGeoAnalysisExecTask> submitGATask(DscGAInvokeParams params) {
        //TODO:参数校验
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
        dscGeoAnalysisExecTask.setId(taskId)
                .setTargetTool(params.getToolId())
                .setParams(dscGARawParams)
                .setExecutor(executor)
                .setStatus(-2);
        dscGeoAnalysisExecTaskDAO.insert(dscGeoAnalysisExecTask);
        //异步执行任务
        dscGeoAnalysisExecService.invoke(dscGeoAnalysisExecTask);
        return CommonResult.success(dscGeoAnalysisExecTask, "任务已提交运行！");
    }

    @Override
    public CommonResult<DscGeoAnalysisExecTask> getGATask(String taskId) {
        Optional<DscGeoAnalysisExecTask> byId = dscGeoAnalysisExecTaskDAO.findById(taskId);
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该任务");
        }
        return CommonResult.success(byId.get(), "获取任务成功");
    }
}
