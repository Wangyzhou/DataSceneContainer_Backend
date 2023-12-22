package nnu.wyz.systemMS.service.iml;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscGeoToolExecTaskDAO;
import nnu.wyz.systemMS.dao.DscGeoToolsDAO;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.DscGeoToolExecTask;
import nnu.wyz.systemMS.model.entity.DscGeoTools;
import nnu.wyz.systemMS.model.param.DscGeoToolExecParams;
import nnu.wyz.systemMS.model.param.DscToolRawParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Value("${fileSavePath}")
    private String rootPath;

    private static final String EXEC_CMD = "whitebox_tools -r={0} {1}";

    @Async
    void execute(DscGeoToolExecTask dscGeoToolExecTask) {
        try {
            dscGeoToolExecTask.setStatus(0);
            dscGeoToolExecTaskDAO.save(dscGeoToolExecTask);
            //TODO: 执行工具
            String toolId = dscGeoToolExecTask.getTargetTool();
            Optional<DscGeoTools> byId = dscGeoToolsDAO.findById(toolId);
            if (!byId.isPresent()) {
                throw new RuntimeException("未找到该工具");
            }
            DscGeoTools tool = byId.get();
            String toolName = tool.getName();
            JSONObject toolParams = tool.getParameter();
            List<Map<String, Object>> parameters = (List<Map<String, Object>>) toolParams.get("parameters");
            List<DscToolRawParams> invokeParams = dscGeoToolExecTask.getParams();
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 0; i < parameters.size(); i++){
                if (invokeParams.get(i).getValue() == null || invokeParams.get(i).getValue().isEmpty()) {
                    continue;
                }
                String parameterType = parameters.get(i).get("parameter_type").toString();
                Object flags = parameters.get(i).get("flags");
                String flag = ((List<String>) flags).get(0);
                if(parameterType.contains("ExistingFile")) {
                    String fileId = invokeParams.get(i).getValue();
                    Optional<DscFileInfo> dscFileDAOById = dscFileDAO.findById(fileId);
                    if (!dscFileDAOById.isPresent()) {
                        throw new RuntimeException("未找到该文件");
                    }
                    DscFileInfo fileInfo = dscFileDAOById.get();
                    String filePath = rootPath + fileInfo.getBucketName() + File.separator + fileInfo.getObjectKey();
                    stringBuilder.append(MessageFormat.format("{0}={1}", flag, filePath));
                    continue;
                } else if(parameterType.contains("NewFile")) {
                    stringBuilder.append(MessageFormat.format("{0}={1}", flag, invokeParams.get(i).getValue()));
                    continue;
                }
                stringBuilder.append(MessageFormat.format("{0}={1}", flag, invokeParams.get(i).getValue()));
            }
            System.out.println("stringBuilder = " + stringBuilder);
            Thread.sleep(3000);
            System.out.println("dscGeoToolExecParams = " + dscGeoToolExecTask);
            System.out.println("执行工具成功！");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
