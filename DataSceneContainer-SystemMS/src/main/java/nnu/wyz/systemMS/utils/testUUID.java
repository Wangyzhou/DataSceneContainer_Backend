package nnu.wyz.systemMS.utils;

import cn.hutool.core.util.IdUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/11/10 10:53
 */

public class testUUID {

    public static void main(String[] args) {
//        String s = "652a48fde4b01213a180bb5a/2d7d74ca-1cab-474c-bef3-4b15c3c68025.zip";
//        String[] split = s.split("/");
//        System.out.println(split[1].substring(0, split[1].lastIndexOf(".")));
//        Date currentDate = new Date();
//
//        // 指定日期时间格式
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//
//        // 格式化日期时间并输出
//        String formattedDateTime = dateFormat.format(currentDate);
//        System.out.println("当前时间: " + formattedDateTime);
        System.out.println(IdUtil.randomUUID());
    }
}
