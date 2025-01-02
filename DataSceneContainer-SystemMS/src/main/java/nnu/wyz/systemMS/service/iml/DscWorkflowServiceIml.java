package nnu.wyz.systemMS.service.iml;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscWorkflowModelDAO;
import nnu.wyz.systemMS.model.dto.DscWorkflowModelDTO;
import nnu.wyz.systemMS.model.entity.DscWorkflowModel;
import nnu.wyz.systemMS.service.DscTableService;
import nnu.wyz.systemMS.service.DscWorkflowModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * @author tjk
 * @date 2025/1/2
 * @Description
 */
@Service
public class DscWorkflowServiceIml implements DscWorkflowModelService {

    @Autowired
    private DscWorkflowModelDAO dscWorkflowModelDAO;

//    @Autowired
//    private DscWorkflowModelService dscWorkflowModelService;

    //DTO转Entity
    public DscWorkflowModel convert2Model(DscWorkflowModelDTO dscWorkflowModelDTO) {
        DscWorkflowModel dscWorkflowModel = new DscWorkflowModel();
        dscWorkflowModel.setName(dscWorkflowModelDTO.getName());
        dscWorkflowModel.setId(dscWorkflowModelDTO.getId());
        dscWorkflowModel.setModelJson(dscWorkflowModelDTO.getModelJson());
        dscWorkflowModel.setUserId(dscWorkflowModelDTO.getUserId());
        return dscWorkflowModel;
    }

    @Override
    public CommonResult<String> saveWorkflowModel(DscWorkflowModelDTO dscWorkflowModelDTO) {

        try {
            // 生成UUID作为模型的ID
            String id = UUID.randomUUID().toString();
            // 获取当前时间，并格式化为指定格式
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // 将DTO转换为实体
            DscWorkflowModel dscWorkflowModel = convert2Model(dscWorkflowModelDTO);
            // 设置ID和时间字段
            dscWorkflowModel.setId(id);
            dscWorkflowModel.setCreateTime(currentDateTime);
            dscWorkflowModel.setUpdateTime(currentDateTime);
            // 保存实体到数据库
            dscWorkflowModelDAO.save(dscWorkflowModel);
            // 返回保存成功的响应
            return CommonResult.success("模型保存成功");
        } catch (Exception e) {
            // 捕获异常并返回失败响应(如数据库连接失败、数据格式错误等)
            return CommonResult.failed("模型保存失败，错误信息：" + e.getMessage());
        }

    }

    @Override
    public CommonResult<DscWorkflowModel> getDscWorkflowModel(String modelId) {
        DscWorkflowModel dscWorkflowModel = dscWorkflowModelDAO.findDscWorkflowModelById(modelId);
        if(dscWorkflowModel != null) {
            return CommonResult.success(dscWorkflowModel,"加载成功！");
        }else{
            return CommonResult.failed("加载失败！");
        }
    }

    @Override
    public CommonResult<String> deleteDscWorkflowModel(String modelId) {
        Optional<DscWorkflowModel> optional = Optional.ofNullable(dscWorkflowModelDAO.findDscWorkflowModelById(modelId));
        if (optional.isPresent()) {
            DscWorkflowModel dscWorkflowModel = optional.get();
            dscWorkflowModelDAO.delete(dscWorkflowModel);
            return CommonResult.success("模型删除成功！");
        } else {
            return CommonResult.failed("模型删除失败，未找到指定模型！");
        }
    }

    @Override
    public CommonResult<String> updateDscWorkflowModel(DscWorkflowModelDTO dscWorkflowModelDTO) {
        String id = dscWorkflowModelDTO.getId();
        Optional<DscWorkflowModel> optional = Optional.ofNullable(dscWorkflowModelDAO.findDscWorkflowModelById(id));
        if (!optional.isPresent()) {
            return CommonResult.failed("模型不存在，请先保存该模型！");
        }

        DscWorkflowModel existingModel = optional.get();
        existingModel.setModelJson(dscWorkflowModelDTO.getModelJson());

        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        existingModel.setUpdateTime(currentDateTime);
        dscWorkflowModelDAO.save(existingModel);
        return CommonResult.success("模型更新成功！");
    }


}
