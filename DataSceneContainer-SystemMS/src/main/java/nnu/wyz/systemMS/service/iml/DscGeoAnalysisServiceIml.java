package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.util.IdUtil;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscGeoAnalysisExecTaskDAO;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
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
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 10:04
 */
@Service
public class DscGeoAnalysisServiceIml implements DscGeoAnalysisService {

//    @Autowired
//    private DscGeoAnalysisExecService dscGeoAnalysisExecService;

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
        DscGeoAnalysisExecTask dscGeoAnalysisExecTask = new DscGeoAnalysisExecTask();
        String taskId = IdUtil.randomUUID();
        String executor = params.getExecutor();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDateTime = dateFormat.format(new Date());
        DscCatalog sceneCatalog = dscCatalogDAO.findDscCatalogById(params.getSceneCatalog());
        CreateCatalogDTO createCatalogDTO = new CreateCatalogDTO();
        createCatalogDTO.setUserId(executor);
        createCatalogDTO.setParentCatalogId(sceneCatalog.getId());
        createCatalogDTO.setCatalogName(formattedDateTime);
        createCatalogDTO.setTaskId("-1");
        CommonResult<String> createCatalogRes = dscCatalogService.create(createCatalogDTO);
        String outputCatalog = createCatalogRes.getData();
        String outputDir = rootPath + minioConfig.getGaOutputBucket() + File.separator + executor + File.separator + params.getSceneCatalog() + File.separator + outputCatalog;
        File file = new File(outputDir);
        if (!file.exists()) {
            file.mkdirs();
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
        //创建工具输出目录，

        //异步执行任务
        return CommonResult.success(dscGeoAnalysisExecTask, "任务已提交运行！");
    }

    @Override
    public CommonResult<DscGeoToolExecTask> getGATask(String taskId) {
        return null;
    }
}
