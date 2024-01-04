package nnu.wyz.systemMS.utils;

import cn.hutool.core.util.IdUtil;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/11/10 10:53
 */

public class testUUID {

    public static void main(String[] args) {
        String s = "652a48fde4b01213a180bb5a/2d7d74ca-1cab-474c-bef3-4b15c3c68025.zip";
        String[] split = s.split("/");
        System.out.println(split[1].substring(0, split[1].lastIndexOf(".")));
    }
}
