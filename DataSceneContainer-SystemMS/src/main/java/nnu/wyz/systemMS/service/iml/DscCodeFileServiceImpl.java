package nnu.wyz.systemMS.service.iml;

import nnu.wyz.systemMS.dao.DscCodeFileDAO;
import nnu.wyz.systemMS.model.dto.DscCodeFileDTO;
import nnu.wyz.systemMS.model.entity.DscCodeFile;
import nnu.wyz.systemMS.service.DscCodeFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DscCodeFileServiceImpl implements DscCodeFileService {

    @Autowired
    private DscCodeFileDAO dscCodeFileDAO;

    @Override
    public void saveCodeFile(DscCodeFileDTO codeFileDTO) {
        // 为文件分配一个唯一的 UUID
        String id = UUID.randomUUID().toString();

        // 获取当前日期时间并格式化为字符串
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 将 DTO 转换为 Entity
        DscCodeFile codeFileEntity = new DscCodeFile(codeFileDTO);

        // 设置 UUID 和当前日期时间
        codeFileEntity.setId(id);
        codeFileEntity.setUpdate_Date(currentDateTime);  // 假设你有 setUpdate_Date 方法

        // 保存到数据库
        dscCodeFileDAO.save(codeFileEntity);
    }
}

