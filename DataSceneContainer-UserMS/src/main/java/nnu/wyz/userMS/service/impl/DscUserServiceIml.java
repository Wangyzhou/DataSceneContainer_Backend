package nnu.wyz.userMS.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.userMS.dao.DscUserDAO;
import nnu.wyz.userMS.entity.DscUser;
import nnu.wyz.userMS.entity.dto.ReturnLoginUserDTO;
import nnu.wyz.userMS.entity.dto.UserRegisterDTO;
import nnu.wyz.userMS.service.IDscUserService;
import nnu.wyz.userMS.service.IMailService;
import nnu.wyz.userMS.utils.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/18 21:16
 */
@Service
public class DscUserServiceIml implements IDscUserService {

    @Autowired
    private DscUserDAO dscUserDAO;
    @Autowired
    private IMailService iMailService;
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Value("${gateway_addr}")
    private String gateway_addr;

    @Value("${user_active_url}")
    private String user_active_url;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public CommonResult<String> register(UserRegisterDTO userRegisterDTO) {
        DscUser oUser = dscUserDAO.findDscUserByEmail(userRegisterDTO.getEmail());
        if (!Objects.isNull(oUser)) {
            return CommonResult.failed(ResultCode.FAILED, "用户已存在！");
        }
        DscUser dscUser = new DscUser();
        dscUser.setId(IdUtil.objectId());
        dscUser.setEmail(userRegisterDTO.getEmail());
        dscUser.setPassword(bCryptPasswordEncoder.encode(userRegisterDTO.getPassword()));
        dscUser.setUserName(userRegisterDTO.getUsername());
        dscUser.setInstitution(userRegisterDTO.getInstitution());
        dscUser.setRegisterDate(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscUser.setEnabled(0);
        String activeCode = RandomUtil.randomString(5);
        dscUser.setActiveCode(activeCode);
        dscUserDAO.insert(dscUser);
        logger.info("用户：" + userRegisterDTO.getUsername() + "注册成功！(未激活)");
        //TODO: 之后设计到的一些权限开通、其他模块的添加
        //发邮件
        String subject = "数据场景容器注册激活邮件";
        String context = "  <h2>数据场景容器平台：</h2><br />\n" +
                "  <div style=\"left:100px; position: absolute;\">\n" +
                "    <p>尊敬的" + userRegisterDTO.getUsername() + "老师，您好！</p><br />\n" +
                "    <p>欢迎使用数据场景容器平台！</p><br />\n" +
                "    <div style=\"display: flex; align-items: center;\">\n" +
                "      <p>请点击右侧链接完成用户激活: </p>&nbsp;&nbsp;\n" +
                "      <a href=\"" + gateway_addr + MessageFormat.format(user_active_url, activeCode) + "\">激活链接</a>\n" +
                "    </div><br /><br /><br />\n" +
                "    <p>(这是一封自动发出的激活邮件，请勿回复！)</p>\n" +
                "  </div>";
        iMailService.sendHtmlMail(userRegisterDTO.getEmail(), subject, context);
        return CommonResult.success(activeCode, "注册成功！激活邮件已发送至您注册的邮箱中，请根据邮件提示完成用户激活！激活有效期24小时。");
    }

    @Override
    public CommonResult<String> active(String code) {
        DscUser dscUser = dscUserDAO.findDscUserByActiveCode(code);
        if (Objects.isNull(dscUser)) {
            return CommonResult.failed(ResultCode.FAILED, "激活码已过期，请重新注册！");
        }
        if (dscUser.getEnabled() == 1) {
            return CommonResult.failed(ResultCode.FAILED, "该用户已激活！");
        }
        dscUser.setEnabled(1);
        dscUserDAO.save(dscUser);
        //TODO: 其他模块的开通工作
        logger.info("用户：" + dscUser.getUserName() + "激活账户成功！");
        return CommonResult.success("用户激活成功！");
    }

    @Override
    public CommonResult<String> logout(String token) throws JsonProcessingException {
        //解析jwt获取jti、user_name、exp
        JWT jwt = new JWT();
        JWT parseRes = jwt.parse(token.replaceFirst("Bearer ", ""));
        String jti = (String) parseRes.getPayload().getClaim("jti");
        Object expObj = parseRes.getPayload().getClaim("exp");
        Long exp = Convert.toLong(expObj);
        String dscUserObj = (String) parseRes.getPayload().getClaim("user_name");
        ObjectMapper mapper = new ObjectMapper();
        ReturnLoginUserDTO dscUser = mapper.readValue(dscUserObj, ReturnLoginUserDTO.class);
        //计算距JWT过期的时间，毫秒
        Date currentDate = new Date();
        LocalDateTime currentLocalTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentDate.getTime()), ZoneId.systemDefault());
        LocalDateTime jwtExpiredLocalTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(exp * 1000L), ZoneId.systemDefault());
        Duration timeSpan = Duration.between(currentLocalTime, jwtExpiredLocalTime);
        redisCache.setCacheObject(jti, dscUser.getId() + ":logout");
        redisCache.expire(jti, timeSpan.toMillis(), TimeUnit.MILLISECONDS);
        return CommonResult.success("注销成功！");
    }
}
