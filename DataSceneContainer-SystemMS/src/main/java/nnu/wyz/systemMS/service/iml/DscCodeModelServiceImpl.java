package nnu.wyz.systemMS.service.iml;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscCode.DscCodeModelDAO;
import nnu.wyz.systemMS.model.dto.DscCodeModelDTO;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModel;
import nnu.wyz.systemMS.service.DscCode.DscCodeModelService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DscCodeModelServiceImpl implements DscCodeModelService {

    @Autowired
    private DscCodeModelDAO dscCodeModelDAO;

    @Override
    public CommonResult<String> encapsulate(DscCodeModelDTO dscCodeModelDTO){
        // 为文件分配一个唯一的 UUID
        String id = UUID.randomUUID().toString();

        // 获取当前日期时间并格式化为字符串
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 将 DTO 转换为 Entity
        DscCodeModel codeModelEntity = new DscCodeModel(dscCodeModelDTO);

        // 设置 UUID 和当前日期时间
        codeModelEntity.setId(id);
        codeModelEntity.setCreateDate(currentDateTime);
        codeModelEntity.setEnabled(true);

        // 保存到数据库
        dscCodeModelDAO.save(codeModelEntity);

        return CommonResult.success("模型封装成功！");
    }

    @Override
    public DscCodeModel getToolInfo(String toolId){
        Optional<DscCodeModel> optionalTool = dscCodeModelDAO.findById(toolId);
        // 返回工具的内容，或者继续处理这个工具对象
        // 如果找不到该工具，返回null或者抛出异常
        return optionalTool.orElse(null);
    }

    @Override
    public CommonResult<String> delete(String toolId){
        dscCodeModelDAO.deleteById(toolId);
        // 删除后，通过查找该ID的实体对象来验证是否删除成功
        Optional<DscCodeModel> toolAfterDelete = dscCodeModelDAO.findById(toolId);
        if(toolAfterDelete.isPresent()){
            return CommonResult.failed("删除失败！");
        }else {
            return CommonResult.success("删除成功！");// 如果找不到该ID，则返回true，表示删除成功
        }
    }
}
