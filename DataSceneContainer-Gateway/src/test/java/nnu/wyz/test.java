package nnu.wyz;

import cn.hutool.core.util.IdUtil;
import nnu.wyz.utils.RedisCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/17 20:01
 */
@SpringBootTest
public class test {

    @Autowired
    private RedisCache redisCache;

    @Test
    void test1() {
        String s = IdUtil.objectId();
        System.out.println("s = " + s);
    }

    @Test
    void test2() {
        redisCache.setCacheObject("test1", "哈哈哈");
        boolean test1 = redisCache.expire("test1", 60);
        System.out.println("redisCache = " + redisCache.getCacheObject("test1"));
    }


}
