package nnu.wyz.systemMS.model.dto;


import lombok.Data;

import java.util.Map;

@Data
public class DscCustomModelDTO {
    private String toolId;
    private String sceneCatalog;
    private String executor;
    private Map<String, String> input;
    private Map<String, String> output;
    private Map<String, String> options;
    private String script;
}
