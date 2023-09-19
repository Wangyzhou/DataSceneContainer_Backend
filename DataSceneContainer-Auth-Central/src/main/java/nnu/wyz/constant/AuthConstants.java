package nnu.wyz.constant;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/16 22:37
 */

public class AuthConstants {
    /**
     * 认证请求头key
     */
    public static final String AUTHORIZATION_KEY = "Authorization";

    /**
     * JWT令牌前缀
     */
    public static final String AUTHORIZATION_PREFIX = "bearer ";


    /**
     * Basic认证前缀
     */
    public static final String BASIC_PREFIX = "Basic ";

    /**
     * JWT载体key
     */
    public static final String JWT_PAYLOAD_KEY = "payload";

    /**
     * JWT ID 唯一标识
     */
    public static final String JWT_JTI = "jti";

    /**
     * JWT ID 唯一标识
     */
    public static final String JWT_EXP = "exp";

    /**
     * Redis缓存权限规则key
     */
    public static final String PERMISSION_ROLES_KEY = "auth:permission:roles";

    /**
     * 黑名单token前缀
     */
    public static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";

    public static final String CLIENT_DETAILS_FIELDS = "client_id, CONCAT('{noop}',client_secret) as client_secret, resource_ids, scope, "
            + "authorized_grant_types, redirect_uri, authorities, access_token_validity, "
            + "refresh_token_validity, additional_information, autoapprove";

    public static final String BASE_CLIENT_DETAILS_SQL = "select " + CLIENT_DETAILS_FIELDS + " from oauth_client_details";

    public static final String FIND_CLIENT_DETAILS_SQL = BASE_CLIENT_DETAILS_SQL + " order by client_id";

    public static final String SELECT_CLIENT_DETAILS_SQL = BASE_CLIENT_DETAILS_SQL + " where client_id = ?";

    /**
     * 密码加密方式
     */
    public static final String BCRYPT = "{bcrypt}";

    public static final String USER_ID_KEY = "user_id";

    public static final String USER_NAME_KEY = "username";

    public static final String CLIENT_ID_KEY = "client_id";

    /**
     * JWT存储权限前缀
     */
    public static final String AUTHORITY_PREFIX = "ROLE_";

    /**
     * JWT存储权限属性
     */
    public static final String JWT_AUTHORITIES_KEY = "authorities";


    /**
     * 有来商城后台管理客户端ID
     */
    public static final String ADMIN_CLIENT_ID = "youlai-admin";


    /**
     * 有来商城微信小程序客户端ID
     */
    public static final String WEAPP_CLIENT_ID = "youlai-mall-weapp";

    /**
     * 后台管理接口路径匹配
     */
    public static final String ADMIN_URL_PATTERN = "*_/youlai-admin/**";


    public static final String LOGOUT_PATH = "/youlai-auth/oauth/logout";


    public static final String GRANT_TYPE_KEY = "grant_type";

    public static final String REFRESH_TOKEN = "refresh_token";

}
