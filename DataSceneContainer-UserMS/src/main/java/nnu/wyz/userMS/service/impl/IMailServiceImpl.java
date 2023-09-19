package nnu.wyz.userMS.service.impl;

import cn.hutool.core.io.resource.ClassPathResource;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.userMS.service.IMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Objects;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/19 17:15
 */


@Slf4j
@Service
public class IMailServiceImpl implements IMailService {

    /**
     * Spring Boot 提供了一个发送邮件的简单抽象，使用的是下面这个接口，这里直接注入即可使用
     */
    @Autowired
    private JavaMailSender mailSender;


    /**
     * 配置文件中我的qq邮箱
     */
    @Value("${spring.mail.from}")
    private String from;

    /**
     * 简单文本邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     */
    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        //创建SimpleMailMessage对象
        SimpleMailMessage message = new SimpleMailMessage();
        //邮件发送人
        message.setFrom(from);
        //邮件接收人
        message.setTo(to);
        //邮件主题
        message.setSubject(subject);
        //邮件内容
        message.setText(content);
        //发送邮件
        mailSender.send(message);
    }

    /**
     * html邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     */
    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        //获取MimeMessage对象
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
        try {
            messageHelper = new MimeMessageHelper(message, true);
            //邮件发送人
            messageHelper.setFrom(from);
            //邮件接收人
            messageHelper.setTo(to);
            //邮件主题
            message.setSubject(subject);
            //邮件内容，html格式
            messageHelper.setText(content, true);
            //发送
            mailSender.send(message);
            //日志信息
            log.info("邮件已经发送...");
        } catch (MessagingException e) {
            log.error("发送邮件时发生异常！", e);
        }
    }

    /**
     * 带附件的邮件
     * @param to       收件人
     * @param subject  主题
     * @param content  内容
     * @param filePath 附件
     */
    @Override
    public void sendAttachmentsMail(String to, String subject, String content, String filePath) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            //FileSystemResource file = new FileSystemResource(new File(filePath));
            ClassPathResource resource = new ClassPathResource(filePath);
            FileSystemResource file = new FileSystemResource(resource.getFile());
            helper.addAttachment(Objects.requireNonNull(file.getFilename()), file);
            //可以同时添加多个附件,只需要在这里直接添加第2,第3...附件就行了.
            //helper.addAttachment(fileName2, file2);
            mailSender.send(message);
            //日志信息
            log.info("邮件已经发送...");
        } catch (MessagingException e) {
            log.error("发送邮件时发生异常！", e);
        }
    }

}
