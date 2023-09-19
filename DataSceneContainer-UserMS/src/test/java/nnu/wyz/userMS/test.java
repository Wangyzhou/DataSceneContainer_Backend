package nnu.wyz.userMS;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import nnu.wyz.userMS.entity.DscUser;
import nnu.wyz.userMS.service.IMailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/19 17:13
 */

@SpringBootTest
public class test {
    @Autowired
    private IMailService iMailService;

    @Test
    void test1() {
        String code = "1j1h233";
        String user = "王亚洲";
        String subject = "数据场景容器注册激活邮件";
        String context = "  <h2>数据场景容器平台：</h2><br />\n" +
                "  <div style=\"left:100px; position: absolute;\">\n" +
                "    <p>尊敬的" + user + "，您好！</p><br />\n" +
                "    <p>欢迎使用数据场景容器平台！</p><br />\n" +
                "    <div style=\"display: flex; align-items: center;\">\n" +
                "      <p>请点击右侧链接完成用户激活: </p>&nbsp;&nbsp;\n" +
                "      <a href=\"https://www.baidu.com\">激活链接</a>\n" +
                "    </div><br /><br /><br />\n" +
                "    <p>(这是一封自动发出的激活邮件，请勿回复！)</p>\n" +
                "  </div>";
        //发送激活邮件
        iMailService.sendHtmlMail("2740962735@qq.com", subject, context);
    }
    @Test
    void test2() {
        String format = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        System.out.println("format = " + format);
    }
    @Test
    void test3() {
        String s = RandomUtil.randomStringUpper(5);
        System.out.println("s = " + s);
    }
    @Test
    void test4() {
        Date currentTime = new Date();
        long time = currentTime.getTime();
        System.out.println("time = " + time);
        LocalDateTime current = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        LocalDateTime jwt = LocalDateTime.ofInstant(Instant.ofEpochMilli(1692611372 * 1000L), ZoneId.systemDefault());
        Duration between = Duration.between(current, jwt);
        System.out.println("between.toMillis() = " + between.toMillis());


//        System.out.println("当前时间 = " + new Date(time));
        System.out.println("jwt过期时间 = " + new Date(1692882000 * 1000L));
//        int compare = DateUtil.compare(new Date(time), new Date(1692595200 * 1000L));
//        System.out.println("compare = " + compare);
    }
}
