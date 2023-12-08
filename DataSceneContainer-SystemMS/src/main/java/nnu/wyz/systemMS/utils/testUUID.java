package nnu.wyz.systemMS.utils;

import cn.hutool.core.util.IdUtil;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/11/10 10:53
 */

public class testUUID {

    public static void main(String[] args) {
        int i = 3;

        while (i > 0) {
            System.out.println("IdUtil.randomUUID() = " + IdUtil.randomUUID());
            i--;
        }
    }
}
