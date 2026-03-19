package cloud.opencode.base.oauth2;

import java.util.*;

/**
 * OAuth2 Scope
 * OAuth2 权限范围
 *
 * <p>Represents OAuth2 scopes for authorization requests.</p>
 * <p>表示授权请求的 OAuth2 权限范围。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Predefined standard scopes - 预定义标准范围</li>
 *   <li>Provider-specific scopes - 特定提供者范围</li>
 *   <li>Scope combination utilities - 范围组合工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use predefined scopes
 * Set<String> scopes = OAuth2Scope.combine(
 *     OAuth2Scope.OPENID,
 *     OAuth2Scope.EMAIL,
 *     OAuth2Scope.PROFILE
 * );
 *
 * // Google Gmail scope
 * String gmailScope = OAuth2Scope.Google.GMAIL_READONLY;
 *
 * // Microsoft Graph scope
 * String graphScope = OAuth2Scope.Microsoft.USER_READ;
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is immutable and thread-safe.</p>
 * <p>此类是不可变的，线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public final class OAuth2Scope {

    // ==================== OpenID Connect Standard Scopes ====================

    /**
     * OpenID scope - required for OIDC
     * OpenID 范围 - OIDC 必需
     */
    public static final String OPENID = "openid";

    /**
     * Profile scope - access to user profile claims
     * Profile 范围 - 访问用户资料声明
     */
    public static final String PROFILE = "profile";

    /**
     * Email scope - access to email and email_verified claims
     * Email 范围 - 访问电子邮件和电子邮件验证声明
     */
    public static final String EMAIL = "email";

    /**
     * Address scope - access to address claim
     * Address 范围 - 访问地址声明
     */
    public static final String ADDRESS = "address";

    /**
     * Phone scope - access to phone_number and phone_number_verified claims
     * Phone 范围 - 访问电话号码和电话号码验证声明
     */
    public static final String PHONE = "phone";

    /**
     * Offline access scope - request refresh token
     * Offline access 范围 - 请求刷新令牌
     */
    public static final String OFFLINE_ACCESS = "offline_access";

    // ==================== Utility Methods ====================

    /**
     * Combine multiple scopes into a set
     * 将多个范围组合成一个集合
     *
     * @param scopes the scopes to combine | 要组合的范围
     * @return the combined scope set | 组合的范围集合
     */
    public static Set<String> combine(String... scopes) {
        return new LinkedHashSet<>(Arrays.asList(scopes));
    }

    /**
     * Combine scope sets
     * 组合范围集合
     *
     * @param scopeSets the scope sets to combine | 要组合的范围集合
     * @return the combined scope set | 组合的范围集合
     */
    @SafeVarargs
    public static Set<String> combine(Set<String>... scopeSets) {
        Set<String> result = new LinkedHashSet<>();
        for (Set<String> set : scopeSets) {
            result.addAll(set);
        }
        return result;
    }

    /**
     * Convert scopes to space-separated string
     * 将范围转换为空格分隔的字符串
     *
     * @param scopes the scopes | 范围
     * @return the space-separated string | 空格分隔的字符串
     */
    public static String toString(Set<String> scopes) {
        return String.join(" ", scopes);
    }

    /**
     * Convert scopes to space-separated string
     * 将范围转换为空格分隔的字符串
     *
     * @param scopes the scopes | 范围
     * @return the space-separated string | 空格分隔的字符串
     */
    public static String toString(String... scopes) {
        return String.join(" ", scopes);
    }

    /**
     * Parse space-separated scope string to set
     * 将空格分隔的范围字符串解析为集合
     *
     * @param scopeString the scope string | 范围字符串
     * @return the scope set | 范围集合
     */
    public static Set<String> parse(String scopeString) {
        if (scopeString == null || scopeString.isBlank()) {
            return Set.of();
        }
        return new LinkedHashSet<>(Arrays.asList(scopeString.split("\\s+")));
    }

    /**
     * Get default OIDC scopes
     * 获取默认 OIDC 范围
     *
     * @return the default OIDC scopes | 默认 OIDC 范围
     */
    public static Set<String> defaultOidc() {
        return Set.of(OPENID, EMAIL, PROFILE);
    }

    // ==================== Google Scopes ====================

    /**
     * Google-specific scopes
     * Google 特定范围
     */
    public static final class Google {

        /**
         * Gmail read-only access
         * Gmail 只读访问
         */
        public static final String GMAIL_READONLY = "https://www.googleapis.com/auth/gmail.readonly";

        /**
         * Gmail send access
         * Gmail 发送访问
         */
        public static final String GMAIL_SEND = "https://www.googleapis.com/auth/gmail.send";

        /**
         * Gmail full access
         * Gmail 完全访问
         */
        public static final String GMAIL_FULL = "https://mail.google.com/";

        /**
         * Gmail modify access
         * Gmail 修改访问
         */
        public static final String GMAIL_MODIFY = "https://www.googleapis.com/auth/gmail.modify";

        /**
         * Google Calendar read-only
         * Google 日历只读
         */
        public static final String CALENDAR_READONLY = "https://www.googleapis.com/auth/calendar.readonly";

        /**
         * Google Calendar full access
         * Google 日历完全访问
         */
        public static final String CALENDAR = "https://www.googleapis.com/auth/calendar";

        /**
         * Google Drive read-only
         * Google 云端硬盘只读
         */
        public static final String DRIVE_READONLY = "https://www.googleapis.com/auth/drive.readonly";

        /**
         * Google Drive full access
         * Google 云端硬盘完全访问
         */
        public static final String DRIVE = "https://www.googleapis.com/auth/drive";

        private Google() {}
    }

    // ==================== Microsoft Scopes ====================

    /**
     * Microsoft-specific scopes
     * Microsoft 特定范围
     */
    public static final class Microsoft {

        /**
         * User read access
         * 用户读取访问
         */
        public static final String USER_READ = "User.Read";

        /**
         * User read all (admin)
         * 用户读取全部（管理员）
         */
        public static final String USER_READ_ALL = "User.Read.All";

        /**
         * Mail read access
         * 邮件读取访问
         */
        public static final String MAIL_READ = "Mail.Read";

        /**
         * Mail send access
         * 邮件发送访问
         */
        public static final String MAIL_SEND = "Mail.Send";

        /**
         * Mail read/write access
         * 邮件读写访问
         */
        public static final String MAIL_READWRITE = "Mail.ReadWrite";

        /**
         * Calendars read access
         * 日历读取访问
         */
        public static final String CALENDARS_READ = "Calendars.Read";

        /**
         * Calendars read/write access
         * 日历读写访问
         */
        public static final String CALENDARS_READWRITE = "Calendars.ReadWrite";

        /**
         * Files read access
         * 文件读取访问
         */
        public static final String FILES_READ = "Files.Read";

        /**
         * Files read/write access
         * 文件读写访问
         */
        public static final String FILES_READWRITE = "Files.ReadWrite";

        /**
         * Default application scope
         * 默认应用程序范围
         */
        public static final String DEFAULT = ".default";

        private Microsoft() {}
    }

    // ==================== GitHub Scopes ====================

    /**
     * GitHub-specific scopes
     * GitHub 特定范围
     */
    public static final class GitHub {

        /**
         * Read user profile
         * 读取用户资料
         */
        public static final String READ_USER = "read:user";

        /**
         * Read user email
         * 读取用户电子邮件
         */
        public static final String USER_EMAIL = "user:email";

        /**
         * Full user access
         * 完全用户访问
         */
        public static final String USER = "user";

        /**
         * Public repo access
         * 公共仓库访问
         */
        public static final String PUBLIC_REPO = "public_repo";

        /**
         * Full repo access
         * 完全仓库访问
         */
        public static final String REPO = "repo";

        /**
         * Read organization membership
         * 读取组织成员资格
         */
        public static final String READ_ORG = "read:org";

        /**
         * Gist access
         * Gist 访问
         */
        public static final String GIST = "gist";

        private GitHub() {}
    }

    private OAuth2Scope() {
        // Utility class - prevent instantiation
    }
}
