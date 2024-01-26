package nnu.wyz.systemMS.model.entity;

import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/26 10:37
 */

@Data
public class DASSceneLog {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 日志记录时间
     */
    private String time;

    /**
     * 日志人物
     */
    private String user;

    /**
     * 工具名称（事物）
     */
    private String tool;

    /**
     * 状态
     */
    private String status;
}
