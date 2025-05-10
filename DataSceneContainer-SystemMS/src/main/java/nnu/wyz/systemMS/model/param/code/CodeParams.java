package nnu.wyz.systemMS.model.param.code;

import lombok.Data;

@Data
public class CodeParams {
    private String executor;
    private String sceneCatalogId;
    private String taskId;
    private String[] fileNames;
}
