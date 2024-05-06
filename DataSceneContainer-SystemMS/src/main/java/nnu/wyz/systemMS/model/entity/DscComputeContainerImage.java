package nnu.wyz.systemMS.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/4/10 15:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DscComputeContainerImage {

    private String id;

    private String imageName;

    private String runType;     //运行类型：one-off or keep-alive

    private String identifier;

    private String typeHub;     //在docker hub中是公开还是私密

    private String hubUserName;     //docker hub的用户名或注册邮箱，typeHub为public时，此项为null

    private String hubUserPass;     //typeHub为public时，此项为null

    private String createdTime;

    private String maintainer;

    private long size;

}
