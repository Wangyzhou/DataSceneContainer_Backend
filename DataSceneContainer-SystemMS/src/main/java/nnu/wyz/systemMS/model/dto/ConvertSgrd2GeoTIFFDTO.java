package nnu.wyz.systemMS.model.dto;

import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/24 14:15
 */
@Data
public class ConvertSgrd2GeoTIFFDTO {

    private String sgrdFile;

    private String outputDir;

    private String userId;

}
