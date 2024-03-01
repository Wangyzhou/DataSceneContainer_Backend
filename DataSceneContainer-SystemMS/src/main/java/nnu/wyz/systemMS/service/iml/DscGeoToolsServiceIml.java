package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscGeoToolExecTaskDAO;
import nnu.wyz.systemMS.dao.DscGeoToolsDAO;
import nnu.wyz.systemMS.model.entity.DscGeoToolExecTask;
import nnu.wyz.systemMS.model.entity.DscGeoTools;
import nnu.wyz.systemMS.model.param.DscInvokeToolParams;
import nnu.wyz.systemMS.service.DscGeoToolExecService;
import nnu.wyz.systemMS.service.DscGeoToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/5 9:47
 */
@Service
@Slf4j
public class DscGeoToolsServiceIml implements DscGeoToolsService {

    @Autowired
    private DscGeoToolsDAO dscGeoToolsDAO;

    @Autowired
    private DscGeoToolExecService dscGeoToolExecService;

    @Autowired
    private DscGeoToolExecTaskDAO dscGeoToolExecTaskDAO;

    @Override
    public CommonResult<DscGeoTools> getGeoToolInfoById(String id) {
        Optional<DscGeoTools> byId = dscGeoToolsDAO.findById(id);
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该工具");
        }
        DscGeoTools dscGeoTool = byId.get();
        return CommonResult.success(dscGeoTool, "获取工具成功!");
    }

    @Override
    public CommonResult<DscGeoToolExecTask> initToolExec(DscInvokeToolParams params) {
        DscGeoToolExecTask dscGeoToolExecTask = new DscGeoToolExecTask();
        String toolId = params.getToolId();
        String taskId = IdUtil.randomUUID();
        String executor = params.getUserId();
        dscGeoToolExecTask.setId(taskId)
                .setTargetTool(toolId)
                .setExecutor(executor)
                .setParams(params.getToolRawParams())
                .setStatus(-2);
        dscGeoToolExecTaskDAO.insert(dscGeoToolExecTask);
        dscGeoToolExecService.execute(dscGeoToolExecTask);    //异步线程执行工具
        return CommonResult.success(dscGeoToolExecTask, "提交工具执行任务成功!");
    }

    @Override
    public CommonResult<DscGeoToolExecTask> getTask(String taskId) {
        Optional<DscGeoToolExecTask> byId = dscGeoToolExecTaskDAO.findById(taskId);
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该任务！");
        }
        DscGeoToolExecTask dscGeoToolExecTask = byId.get();
        return CommonResult.success(dscGeoToolExecTask, "获取成功!");
    }
}
