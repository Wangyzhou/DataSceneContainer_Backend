package nnu.wyz.systemMS.service.iml;

import cn.hutool.json.JSON;
import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscWorkflowModelDAO;
import nnu.wyz.systemMS.model.dto.DscWorkflowModelDTO;
import nnu.wyz.systemMS.model.dto.DscWorkflowModelListDTO;
import nnu.wyz.systemMS.model.entity.DscCatalog;
import nnu.wyz.systemMS.model.entity.DscModel;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscWorkflowModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private DscCatalogService dscCatalogService;


    //DTO转Entity
    public DscModel convert2Model(DscWorkflowModelDTO dscWorkflowModelDTO) {
        DscModel dscModel = new DscModel();
        dscModel.setName(dscWorkflowModelDTO.getName());
        dscModel.setId(dscWorkflowModelDTO.getId());
        dscModel.setDescription(dscWorkflowModelDTO.getDescription());
        dscModel.setParams(dscWorkflowModelDTO.getParams());
        dscModel.setModelJson(dscWorkflowModelDTO.getModelJson());
        dscModel.setAuthor(dscWorkflowModelDTO.getAuthor());
        dscModel.setOwnerId(dscWorkflowModelDTO.getOwnerId());
        return dscModel;
    }

    @Override
    public CommonResult<String> saveWorkflowModel(DscWorkflowModelDTO dscWorkflowModelDTO , String userId ) {

        try {
            // 生成UUID作为模型的ID
            String id = UUID.randomUUID().toString();
            // 获取当前时间，并格式化为指定格式
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // 将DTO转换为实体
            DscModel dscModel = convert2Model(dscWorkflowModelDTO);
            // 设置ID和时间字段
            dscModel.setId(id);
            dscModel.setCreateTime(currentDateTime);
            dscModel.setUpdateTime(currentDateTime);
            dscModel.setCategory("Workflow Model");
            // 保存实体到数据库
            dscWorkflowModelDAO.save(dscModel);

            dscCatalogService.addModelAsChildren2WorkflowModelCatalog(dscModel, userId);
            // 返回保存成功的响应
            return CommonResult.success("模型保存成功");
        } catch (Exception e) {
            // 捕获异常并返回失败响应(如数据库连接失败、数据格式错误等)
            return CommonResult.failed("模型保存失败，错误信息：" + e.getMessage());
        }

    }

    @Override
    public CommonResult<DscModel> getDscWorkflowModel(String modelId) {
        DscModel dscModel = dscWorkflowModelDAO.findDscWorkflowModelById(modelId);
        if(dscModel != null) {
            return CommonResult.success(dscModel,"加载成功！");
        }else{
            return CommonResult.failed("加载失败！");
        }
    }

    @Override
    public CommonResult<String> deleteDscWorkflowModel(String modelId) {
        Optional<DscModel> optional = Optional.ofNullable(dscWorkflowModelDAO.findDscWorkflowModelById(modelId));
        if (optional.isPresent()) {
            DscModel dscModel = optional.get();
            dscWorkflowModelDAO.delete(dscModel);
            return CommonResult.success("模型删除成功！");
        } else {
            return CommonResult.failed("模型删除失败，未找到指定模型！");
        }
    }

    @Override
    public CommonResult<String> updateDscWorkflowModel(DscWorkflowModelDTO dscWorkflowModelDTO) {
        String id = dscWorkflowModelDTO.getId();
        Optional<DscModel> optional = Optional.ofNullable(dscWorkflowModelDAO.findDscWorkflowModelById(id));
        if (!optional.isPresent()) {
            return CommonResult.failed("模型不存在，请先保存该模型！");
        }

        DscModel existingModel = optional.get();
        existingModel.setModelJson(dscWorkflowModelDTO.getModelJson());

        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        existingModel.setUpdateTime(currentDateTime);
        dscWorkflowModelDAO.save(existingModel);
        return CommonResult.success("模型更新成功！");
    }

    @Override
    public CommonResult<List<JSONObject>> getDscWorkflowModelList(String ownerId) {
        List<DscWorkflowModelListDTO> workflowModelsByOwnerId = dscWorkflowModelDAO.findDscWorkflowModelsByOwnerId(ownerId);
        System.out.println("workflowModelByOwnerId="+workflowModelsByOwnerId);
        ArrayList<JSONObject> treeData = new ArrayList<>();

        if (workflowModelsByOwnerId != null && !workflowModelsByOwnerId.isEmpty()) {
            // 遍历每个模型，将其转化为 JSON 格式
            for (DscWorkflowModelListDTO model : workflowModelsByOwnerId) {
                JSONObject modelData = new JSONObject();
                modelData.put("id", model.getId());
                modelData.put("label", model.getName());
                modelData.put("category", model.getCategory());
                modelData.put("isLeaf", true);

                // 将每个模型添加到列表中
                treeData.add(modelData);
            }

            // 返回成功结果，包含模型列表
            return CommonResult.success(treeData, "用户所有模型获取成功！");
        } else {
            // 如果没有找到模型数据，返回加载失败结果
            return CommonResult.failed("加载失败！");
        }
    }
}
