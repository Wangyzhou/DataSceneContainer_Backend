package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.entity.DscGeoToolExecTask;
import nnu.wyz.systemMS.model.entity.DscGeoTools;
import nnu.wyz.systemMS.model.param.DscInvokeToolParams;
import org.springframework.scheduling.annotation.Async;

public interface DscGeoToolsService {

    CommonResult<DscGeoTools> getGeoToolInfoById(String id);


    CommonResult<DscGeoToolExecTask> initToolExec(DscInvokeToolParams params);

//    @Async
//    void execute(DscInvokeToolParams params);
}
