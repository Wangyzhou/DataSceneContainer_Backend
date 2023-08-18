package nnu.wyz;

import cn.hutool.core.util.IdUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/17 20:01
 */
@SpringBootTest
public class test {

    @Test
    void test1() {
        String s = IdUtil.objectId();
        System.out.println("s = " + s);
    }
}
