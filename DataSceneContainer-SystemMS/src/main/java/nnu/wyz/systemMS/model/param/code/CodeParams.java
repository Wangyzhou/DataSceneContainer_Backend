package nnu.wyz.systemMS.model.param.code;

import lombok.Data;

import java.util.List;

@Data
public class CodeParams {
    private String executor;
    private String sceneCatalogId;
    private String sceneCatalogName;
    private String taskId;
    private List<String> fileNames;
}
