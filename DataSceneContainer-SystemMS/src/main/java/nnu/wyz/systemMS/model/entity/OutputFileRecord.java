package nnu.wyz.systemMS.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import nnu.wyz.systemMS.dao.DscFileDAO;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/20 14:16
 */
@Data
@Accessors(chain = true)
public class OutputFileRecord {

    private DscFileInfo file;

    private String catalogId;

}
