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
    private DscGeoAnalysisSagaExecService dscGeoAnalysisSagaExecService;

    @Autowired
    private DscGeoAnalysisDIYExecService dscGeoAnalysisDIYExecService;
    @Value("${fileSavePath}")
    private String root;

    private static final String CONTAINER_ID = "e904431d1b38ec6fba77361019321875536deaf0169ebd6872af66bbab67d879";

    @SneakyThrows
    @Async
    public void invoke(DscGeoAnalysisExecTask dscGeoAnalysisExecTask) {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById(dscGeoAnalysisExecTask.getTargetTool().get("id").toString());
        DscGeoAnalysisTool dscGeoAnalysisTool = byId.get();
        AvailableGisToolEnum toolEnum = AvailableGisToolEnum.fromCode(dscGeoAnalysisTool.getCategory());
        switch (Objects.requireNonNull(toolEnum)) {
            case SAGA_GIS_TOOL:
                dscGeoAnalysisSagaExecService.invoke(dscGeoAnalysisExecTask);
                break;
            case GRASS_GIS_TOOL:
                // TODO: grass
                break;
            case DIY_GIS_TOOL:
                dscGeoAnalysisDIYExecService.invoke(dscGeoAnalysisExecTask);
                break;
            default:
                break;
        }
    }

}
