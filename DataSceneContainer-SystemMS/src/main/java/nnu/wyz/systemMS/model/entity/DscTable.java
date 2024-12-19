package nnu.wyz.systemMS.model.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DscTable {
    private List<String> columns;
    private List<Map<String, String>> rows;
}
