package org.sonar.plugins.cas;

import org.sonar.api.PropertyType;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:xyz327@outlook.com">xizhou</a>
 * @since 2020/9/1 7:38 下午
 */
@ServerSide
public class CasSettings {

    private static final Supplier<? extends IllegalStateException> DEFAULT_VALUE_MISSING = () -> new IllegalStateException("Should have a default value");
    public static final String DEFAULT_CAS_SERVER_LOGIN_PATH = "/login";
    public static final String DEFAULT_CAS_SERVER_LOGOUT_PATH = "/logout";
    public static final String ENABLED = "sonar.auth.cas.enabled";
    public static final String CAS_SERVER_URL = "sonar.auth.cas.serverUrl";
    public static final String CAS_SERVER_LOGIN_PATH = "sonar.auth.cas.server.loginPath";
    public static final String CAS_SERVER_LOGOUT_PATH = "sonar.auth.cas.server.logoutPath";
    public static final String LOGOUT_REDIRECT = "sonar.auth.cas.server.logoutRedirect";
    public static final String DISABLE_CERT_VALIDATION = "sonar.auth.cas.disableCertValidation";
    public static final String CAS_PROTOCOL = "sonar.auth.cas.protocol";
    public static final String ROLES_ATTRIBUTE = "sonar.auth.cas.rolesAttributes";
    public static final String FULL_NAME_ATTRIBUTE = "sonar.auth.cas.fullNameAttribute";
    public static final String EMAIL_ATTRIBUTE = "sonar.auth.cas.emailAttribute";


    public static final String CATEGORY = "security";
    public static final String SUBCATEGORY = "cas";

    private final Configuration config;

    public CasSettings(Configuration config) {
        this.config = config;
    }
    public String getSonarServerUrl(){
        return config.get("sonar.core.serverBaseURL").orElseThrow(()->new IllegalStateException("请配置 sonar 服务地址"));
    }
    //@CheckForNull
    public String getCasServerUrl() {
        return config.get(CAS_SERVER_URL).orElse(null);
    }

    //@CheckForNull
    public String getCasServerLoginPath() {
        return config.get(CAS_SERVER_LOGIN_PATH).orElse(DEFAULT_CAS_SERVER_LOGIN_PATH);
    }

    public String getCasServerLoginUrl() {
        String casServerUrl = getCasServerUrl();
        if(casServerUrl.endsWith("/")){
            casServerUrl = casServerUrl.substring(0, casServerUrl.length()-1);
        }
        String casServerLoginPath = getCasServerLoginPath();
        if(!casServerLoginPath.startsWith("/")){
            casServerLoginPath = "/"+ casServerLoginPath;
        }
        return casServerUrl + casServerLoginPath;
    }
    //@CheckForNull
    public String getCasServerLogoutUrl() {
        String casServerUrl = getCasServerUrl();
        if(casServerUrl.endsWith("/")){
            casServerUrl = casServerUrl.substring(0, casServerUrl.length()-1);
        }
        String casServerLogoutPath = getCasServerLogoutPath();
        if(!casServerLogoutPath.startsWith("/")){
            casServerLogoutPath = "/"+ casServerLogoutPath;
        }
        return casServerUrl + casServerLogoutPath;
    }
    public boolean getLogoutRedirect(){
        return config.getBoolean(LOGOUT_REDIRECT).orElse(true);
    }
    public String getCasServerLogoutPath() {
        return config.get(CAS_SERVER_LOGOUT_PATH).orElse(DEFAULT_CAS_SERVER_LOGOUT_PATH);
    }

    public String getCasProtocol(){
        return config.get(CAS_PROTOCOL).orElse("cas3");
    }

    public String getRolesAttribute(){
        return config.get(ROLES_ATTRIBUTE).orElseThrow(DEFAULT_VALUE_MISSING);
    }
    public String getFullNameAttribute(){
        return config.get(FULL_NAME_ATTRIBUTE).orElseThrow(DEFAULT_VALUE_MISSING);
    }
    public String getEmailAttribute(){
        return config.get(EMAIL_ATTRIBUTE).orElseThrow(DEFAULT_VALUE_MISSING);
    }

    public boolean isEnabled() {
        return config.getBoolean(ENABLED).orElse(false) && getCasServerUrl() != null;
    }
    public boolean getDisableCertValidation() {
        return config.getBoolean(DISABLE_CERT_VALIDATION).orElse(false);
    }
    private static String urlWithEndingSlash(String url) {
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }

    public static List<PropertyDefinition> definitions() {
        int index = 1;
        return Arrays.asList(
                PropertyDefinition.builder(ENABLED)
                        .name("Enabled")
                        .description("是否开启 cas sso.")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY)
                        .type(PropertyType.BOOLEAN)
                        .defaultValue(String.valueOf(false))
                        .index(index++)
                        .build(),
                PropertyDefinition.builder(CAS_SERVER_URL)
                        .name("cas server url")
                        .description("cas 服务的 url")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY)
                        .index(index++)
                        .build(),
                PropertyDefinition.builder(CAS_SERVER_LOGIN_PATH)
                        .name("cas server login path")
                        .description("cas 服务的登陆路径,默认:/login.")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY)
                        .defaultValue(DEFAULT_CAS_SERVER_LOGIN_PATH)
                        .index(index++)
                        .build(),
                PropertyDefinition.builder(CAS_SERVER_LOGOUT_PATH)
                        .name("cas server logout path")
                        .description("cas 服务的退出路径,默认:/logout.")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY)
                        .defaultValue(DEFAULT_CAS_SERVER_LOGOUT_PATH)
                        .index(index++)
                        .build(),
                PropertyDefinition.builder(LOGOUT_REDIRECT)
                        .name("cas server logout redirect")
                        .description("从 cas 退出后是否重定向会 sonar 服务.")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY)
                        .type(PropertyType.BOOLEAN)
                        .defaultValue("true")
                        .index(index++)
                        .build(),
                PropertyDefinition.builder(CAS_PROTOCOL)
                        .name("cas protocol")
                        .description("cas 认证协议. 默认: cas3.")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY)
                        .defaultValue("cas3")
                        .index(index++)
                        .build(),
                PropertyDefinition.builder(DISABLE_CERT_VALIDATION)
                        .name("disableCertValidation")
                        .description("是否禁用 ssl 验证.")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY)
                        .type(PropertyType.BOOLEAN)
                        .defaultValue("true")
                        .index(index++)
                        .build(),
                PropertyDefinition.builder(ROLES_ATTRIBUTE)
                        .name("rolesAttributes")
                        .description("用户角色属性.")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY)
                        .index(index++)
                        .build(),
                PropertyDefinition.builder(FULL_NAME_ATTRIBUTE)
                        .name("fullNameAttribute")
                        .description("用户姓名属性.")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY)
                        .index(index++)
                        .build(),
                PropertyDefinition.builder(EMAIL_ATTRIBUTE)
                        .name("emailAttribute")
                        .description("用户邮箱属性.")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY)
                        .index(index)
                        .build()
                );
    }
}
