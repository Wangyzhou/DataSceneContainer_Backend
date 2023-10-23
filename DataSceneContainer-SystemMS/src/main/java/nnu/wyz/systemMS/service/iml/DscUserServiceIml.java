package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.jwt.JWT;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.systemMS.dao.DscUserDAO;
import nnu.wyz.systemMS.model.dto.ReturnLoginUserDTO;
import nnu.wyz.systemMS.model.dto.ReturnUsersByEmailLikeDTO;
import nnu.wyz.systemMS.model.dto.UserLoginDTO;
import nnu.wyz.systemMS.model.dto.UserRegisterDTO;
import nnu.wyz.systemMS.model.entity.DscUser;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscUserService;
import nnu.wyz.systemMS.service.MailService;
//import nnu.wyz.systemMS.service.UserAuthService;
import nnu.wyz.systemMS.utils.RedisCache;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/28 20:40
 */
@Service
//@Transactional
@SuppressWarnings("all")
public class DscUserServiceIml implements DscUserService {

//    @Autowired
//    private UserAuthService userAuthService;          //OpenFeign

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DscUserDAO dscUserDAO;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MailService iMailService;
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private DscCatalogService dscCatalogService;

    @Value("${gateway_addr}")
    private String gateway_addr;

    @Value("${frontend_url}")
    private String frontend_url;

    @Value("${user_active_url}")
    private String user_active_url;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public CommonResult<String> register(UserRegisterDTO userRegisterDTO) {
        DscUser oUser = dscUserDAO.findDscUserByEmail(userRegisterDTO.getEmail());
        if (!Objects.isNull(oUser)) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, "用户已存在！");
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
        String subject = "数据场景容器平台注册激活邮件";
        String context = "<head>\n" +
                "    <base target=\"_blank\" />\n" +
                "    <style type=\"text/css\">\n" +
                "        ::-webkit-scrollbar {\n" +
                "            display: none;\n" +
                "        }\n" +
                "    </style>\n" +
                "    <style id=\"cloudAttachStyle\" type=\"text/css\">\n" +
                "        #divNeteaseBigAttach,\n" +
                "        #divNeteaseBigAttach_bak {\n" +
                "            display: none;\n" +
                "        }\n" +
                "    </style>\n" +
                "    <style id=\"blockquoteStyle\" type=\"text/css\">\n" +
                "        blockquote {\n" +
                "            display: none;\n" +
                "        }\n" +
                "    </style>\n" +
                "    <style type=\"text/css\">\n" +
                "        body {\n" +
                "            font-size: 14px;\n" +
                "            font-family: arial, verdana, sans-serif;\n" +
                "            line-height: 1.666;\n" +
                "            padding: 0;\n" +
                "            margin: 0;\n" +
                "            overflow: auto;\n" +
                "            white-space: normal;\n" +
                "            word-wrap: break-word;\n" +
                "            min-height: 100px\n" +
                "        }\n" +
                "\n" +
                "        td,\n" +
                "        input,\n" +
                "        button,\n" +
                "        select,\n" +
                "        body {\n" +
                "            font-family: Helvetica, 'Microsoft Yahei', verdana\n" +
                "        }\n" +
                "\n" +
                "        pre {\n" +
                "            white-space: pre-wrap;\n" +
                "            white-space: -moz-pre-wrap;\n" +
                "            white-space: -pre-wrap;\n" +
                "            white-space: -o-pre-wrap;\n" +
                "            word-wrap: break-word;\n" +
                "            width: 95%\n" +
                "        }\n" +
                "\n" +
                "        th,\n" +
                "        td {\n" +
                "            font-family: arial, verdana, sans-serif;\n" +
                "            line-height: 1.666\n" +
                "        }\n" +
                "\n" +
                "        img {\n" +
                "            border: 0\n" +
                "        }\n" +
                "\n" +
                "        header,\n" +
                "        footer,\n" +
                "        section,\n" +
                "        aside,\n" +
                "        article,\n" +
                "        nav,\n" +
                "        hgroup,\n" +
                "        figure,\n" +
                "        figcaption {\n" +
                "            display: block\n" +
                "        }\n" +
                "\n" +
                "        blockquote {\n" +
                "            margin-right: 0px\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body tabindex=\"0\" role=\"listitem\">\n" +
                "    <table width=\"700\" border=\"0\" align=\"center\" cellspacing=\"0\" style=\"width:700px;\">\n" +
                "        <tbody>\n" +
                "            <tr>\n" +
                "                <td>\n" +
                "                    <div style=\"width:700px;margin:0 auto;border-bottom:1px solid #ccc;margin-bottom:30px;\">\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"700\" height=\"39\"\n" +
                "                            style=\"font:12px Tahoma, Arial, 宋体;\">\n" +
                "                            <tbody>\n" +
                "                                <tr>\n" +
                "                                    <td width=\"210\"></td>\n" +
                "                                </tr>\n" +
                "                            </tbody>\n" +
                "                        </table>\n" +
                "                    </div>\n" +
                "                    <div style=\"width:680px;padding:0 10px;margin:0 auto;\">\n" +
                "                        <div style=\"line-height:1.5;font-size:14px;margin-bottom:25px;color:#4d4d4d;\">\n" +
                "                            <strong style=\"display:block;margin-bottom:15px;\">尊敬的" + userRegisterDTO.getUsername() + "老师：<span\n" +
                "                                    style=\"color:#f60;font-size: 16px;\"></span>您好！</strong>\n" +
                "                            <strong style=\"display:block;margin-bottom:15px;\">\n" +
                "                                您正在进行<span style=\"color: red\">账号激活</span>操作，请点击右侧链接进行账号激活：<a href=\"" + MessageFormat.format(frontend_url, activeCode) + "\">点击激活</a>\n" +
                "                            </strong>\n" +
                "                        </div>\n" +
                "                        <div style=\"margin-bottom:30px;\">\n" +
                "                            <small style=\"display:block;margin-bottom:20px;font-size:12px;\">\n" +
                "                                <p style=\"color:#747474;\">\n" +
                "                                    注意：此操作将会开通该账号的全部权限，如非本人操作，请勿随意激活！\n" +
                "                                    <br>（工作人员不会向你索取激活链接，请勿泄漏！)\n" +
                "                                </p>\n" +
                "                            </small>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                    <div style=\"width:700px;margin:0 auto;\">\n" +
                "                        <div\n" +
                "                            style=\"padding:10px 10px 0;border-top:1px solid #ccc;color:#747474;margin-bottom:20px;line-height:1.3em;font-size:12px;\">\n" +
                "                            <p>此为系统邮件，请勿回复<br>\n" +
                "                                请保管好您的邮箱，避免账号被他人盗用\n" +
                "                            </p>\n" +
                "                            <p>OpenGMS团队</p>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "        </tbody>\n" +
                "    </table>\n" +
                "</body>";
        iMailService.sendHtmlMail(userRegisterDTO.getEmail(), subject, context);
        return CommonResult.success(activeCode, "注册成功！激活邮件已发送至您注册的邮箱中，请根据邮件提示完成用户激活！激活有效期24小时。");
    }

    @Override
    public CommonResult<JSONObject> login(UserLoginDTO userLoginDTO) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("client_id", userLoginDTO.getClient_id());
        parameters.add("client_secret", userLoginDTO.getClient_secret());
        parameters.add("grant_type", userLoginDTO.getGrant_type());
        parameters.add("username", userLoginDTO.getUsername());
        parameters.add("password", userLoginDTO.getPassword());
        logger.info("用户：" + userLoginDTO.getUsername() + "已登录。");
        return restTemplate.postForObject(gateway_addr + "/dsc-auth-central/oauth/token", parameters, CommonResult.class);
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
        dscCatalogService.createRootCatalog(dscUser.getId());
        logger.info("用户：" + dscUser.getUserName() + "创建根目录。");
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

    @Override
    public CommonResult<String> sendCode2Email(String email) {
        int code = RandomUtil.randomInt(100001, 999999);
        DscUser user = dscUserDAO.findDscUserByEmail(email);
        if (Objects.isNull(user)) {
            return CommonResult.failed("未找到该用户！");
        }
        redisCache.setCacheObject(Integer.toString(code), email);
        redisCache.expire(Integer.toString(code), 300);
        String subject = "数据场景容器平台重置密码邮件";
        String context = "<head>\n" +
                "    <base target=\"_blank\" />\n" +
                "    <style type=\"text/css\">::-webkit-scrollbar{ display: none; }</style>\n" +
                "    <style id=\"cloudAttachStyle\" type=\"text/css\">#divNeteaseBigAttach, #divNeteaseBigAttach_bak{display:none;}</style>\n" +
                "    <style id=\"blockquoteStyle\" type=\"text/css\">blockquote{display:none;}</style>\n" +
                "    <style type=\"text/css\">\n" +
                "        body{font-size:14px;font-family:arial,verdana,sans-serif;line-height:1.666;padding:0;margin:0;overflow:auto;white-space:normal;word-wrap:break-word;min-height:100px}\n" +
                "        td, input, button, select, body{font-family:Helvetica, 'Microsoft Yahei', verdana}\n" +
                "        pre {white-space:pre-wrap;white-space:-moz-pre-wrap;white-space:-pre-wrap;white-space:-o-pre-wrap;word-wrap:break-word;width:95%}\n" +
                "        th,td{font-family:arial,verdana,sans-serif;line-height:1.666}\n" +
                "        img{ border:0}\n" +
                "        header,footer,section,aside,article,nav,hgroup,figure,figcaption{display:block}\n" +
                "        blockquote{margin-right:0px}\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body tabindex=\"0\" role=\"listitem\">\n" +
                "<table width=\"700\" border=\"0\" align=\"center\" cellspacing=\"0\" style=\"width:700px;\">\n" +
                "    <tbody>\n" +
                "    <tr>\n" +
                "        <td>\n" +
                "            <div style=\"width:700px;margin:0 auto;border-bottom:1px solid #ccc;margin-bottom:30px;\">\n" +
                "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"700\" height=\"39\" style=\"font:12px Tahoma, Arial, 宋体;\">\n" +
                "                    <tbody><tr><td width=\"210\"></td></tr></tbody>\n" +
                "                </table>\n" +
                "            </div>\n" +
                "            <div style=\"width:680px;padding:0 10px;margin:0 auto;\">\n" +
                "                <div style=\"line-height:1.5;font-size:14px;margin-bottom:25px;color:#4d4d4d;\">\n" +
                "                    <strong style=\"display:block;margin-bottom:15px;\">尊敬的" + user.getUserName() + "老师：<span style=\"color:#f60;font-size: 16px;\"></span>您好！</strong>\n" +
                "                    <strong style=\"display:block;margin-bottom:15px;\">\n" +
                "                        您正在进行<span style=\"color: red\">重置密码</span>操作，请在验证码输入框中输入：<span style=\"color:rgb(26, 60, 134);font-size: 24px\">" + code + "</span>，以完成操作。\n" +
                "                    </strong>\n" +
                "                </div>\n" +
                "                <div style=\"margin-bottom:30px;\">\n" +
                "                    <small style=\"display:block;margin-bottom:20px;font-size:12px;\">\n" +
                "                        <p style=\"color:#747474;\">\n" +
                "                            注意：此操作会修改您的密码。如非本人操作，请及时登录并修改密码以保证帐户安全\n" +
                "                            <br>（工作人员不会向你索取此验证码，请勿泄漏！)\n" +
                "                        </p>\n" +
                "                    </small>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            <div style=\"width:700px;margin:0 auto;\">\n" +
                "                <div style=\"padding:10px 10px 0;border-top:1px solid #ccc;color:#747474;margin-bottom:20px;line-height:1.3em;font-size:12px;\">\n" +
                "                    <p>此为系统邮件，请勿回复<br>\n" +
                "                        请保管好您的邮箱，避免账号被他人盗用\n" +
                "                    </p>\n" +
                "                    <p>OpenGMS团队</p>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "    </tbody>\n" +
                "</table>\n" +
                "</body>\n";
        iMailService.sendHtmlMail(email, subject, context);
        return CommonResult.success("重置密码验证码已发送至您邮箱，请注意查收！有效期：5分钟。");
    }

    @Override
    public CommonResult<JSONObject> validateCode(String email, String code) {
        Object userEmail = redisCache.getCacheObject(code);
        if (Objects.isNull(userEmail) || !userEmail.toString().equals(email)) {
            return CommonResult.failed("验证码已过期，请重新发送！");
        }
        String resetToken = UUID.randomUUID().toString().replace("-", "");
        redisCache.setCacheObject(email + "_reset_token", resetToken);
        redisCache.expire(email + "_reset_token", 300);
        HashMap<String, Object> returnV = new HashMap<>();
        returnV.put("resetToken", resetToken);
        return CommonResult.success(new JSONObject(returnV), "验证成功！");
    }

    @Override
    public CommonResult<String> resetPassword(String resetToken, String email, String password) {
        Object resetTokenRedis = redisCache.getCacheObject(email + "_reset_token");
        if(Objects.isNull(resetTokenRedis)) {
            return CommonResult.failed("重置密码操作超时，请返回重新操作！");
        }
        if(!resetTokenRedis.toString().equals(resetToken)) {
            return CommonResult.failed("访问不合法！");
        }
        DscUser user = dscUserDAO.findDscUserByEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        dscUserDAO.save(user);
        return CommonResult.success("重置密码成功！");
    }

    @Override
    public CommonResult<List<ReturnUsersByEmailLikeDTO>> getUserByEmailLike(String keyWord) {
        Criteria criteria = new Criteria();
        Pattern pattern = Pattern.compile("^" + keyWord + ".*$");
        Query query = Query.query(criteria.andOperator(Criteria.where("email").regex(pattern), Criteria.where("enabled").is(1)));
        List<DscUser> dscUser = mongoTemplate.find(query, DscUser.class, "dscUser");
        ArrayList<ReturnUsersByEmailLikeDTO> returnUsers = new ArrayList<>();
        dscUser.forEach(dscUser1 -> {
            ReturnUsersByEmailLikeDTO returnUser = BeanUtil.copyProperties(dscUser1, ReturnUsersByEmailLikeDTO.class);
            returnUsers.add(returnUser);
        });
        return CommonResult.success(returnUsers, "获取成功！");
    }
}
