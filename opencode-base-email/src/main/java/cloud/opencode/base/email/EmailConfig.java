package cloud.opencode.base.email;

import cloud.opencode.base.email.security.DkimConfig;

import java.time.Duration;

/**
 * Email Configuration Record
 * 邮件配置记录
 *
 * <p>Immutable configuration for email sending.</p>
 * <p>邮件发送的不可变配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SMTP/SMTPS configuration - SMTP/SMTPS配置</li>
 *   <li>SSL/TLS support - SSL/TLS支持</li>
 *   <li>OAuth2 authentication (Gmail, Outlook) - OAuth2认证</li>
 *   <li>Connection pool settings - 连接池设置</li>
 *   <li>Retry configuration - 重试配置</li>
 *   <li>DKIM signing support - DKIM签名支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic configuration
 * EmailConfig config = EmailConfig.builder()
 *     .host("smtp.example.com")
 *     .port(587)
 *     .username("user@example.com")
 *     .password("password")
 *     .starttls(true)
 *     .defaultFrom("noreply@example.com", "System")
 *     .build();
 *
 * // SSL configuration (port 465)
 * EmailConfig config = EmailConfig.builder()
 *     .host("smtp.example.com")
 *     .port(465)
 *     .ssl(true)
 *     .username("user")
 *     .password("pass")
 *     .build();
 *
 * // OAuth2 configuration (Gmail)
 * EmailConfig config = EmailConfig.builder()
 *     .host("smtp.gmail.com")
 *     .port(587)
 *     .username("user@gmail.com")
 *     .oauth2Token(accessToken)
 *     .starttls(true)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public record EmailConfig(
        String host,
        int port,
        String username,
        String password,
        String oauth2Token,
        boolean ssl,
        boolean starttls,
        String defaultFrom,
        String defaultFromName,
        Duration timeout,
        Duration connectionTimeout,
        int maxRetries,
        int poolSize,
        Duration poolIdleTimeout,
        boolean debug,
        DkimConfig dkim
) {

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Email Configuration Builder
     * 邮件配置构建器
     */
    public static class Builder {
        private String host;
        private int port = 587;
        private String username;
        private String password;
        private String oauth2Token;
        private boolean ssl = false;
        private boolean starttls = true;
        private String defaultFrom;
        private String defaultFromName;
        private Duration timeout = Duration.ofSeconds(30);
        private Duration connectionTimeout = Duration.ofSeconds(10);
        private int maxRetries = 3;
        private int poolSize = 5;
        private Duration poolIdleTimeout = Duration.ofMinutes(5);
        private boolean debug = false;
        private DkimConfig dkim;

        /**
         * Set SMTP host
         * 设置SMTP主机
         *
         * @param host the SMTP host | SMTP主机
         * @return this builder | 构建器
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Set SMTP port
         * 设置SMTP端口
         *
         * @param port the SMTP port | SMTP端口
         * @return this builder | 构建器
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Set username for authentication
         * 设置认证用户名
         *
         * @param username the username | 用户名
         * @return this builder | 构建器
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * Set password for authentication
         * 设置认证密码
         *
         * @param password the password | 密码
         * @return this builder | 构建器
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Set OAuth2 access token for XOAUTH2 authentication
         * 设置OAuth2访问令牌用于XOAUTH2认证
         *
         * <p>Used for Gmail, Outlook and other OAuth2-enabled mail servers.</p>
         * <p>用于Gmail、Outlook和其他支持OAuth2的邮件服务器。</p>
         *
         * @param oauth2Token the OAuth2 access token | OAuth2访问令牌
         * @return this builder | 构建器
         */
        public Builder oauth2Token(String oauth2Token) {
            this.oauth2Token = oauth2Token;
            return this;
        }

        /**
         * Enable SSL connection
         * 启用SSL连接
         *
         * @param ssl true to enable SSL | true启用SSL
         * @return this builder | 构建器
         */
        public Builder ssl(boolean ssl) {
            this.ssl = ssl;
            if (ssl && port == 587) {
                this.port = 465;
            }
            return this;
        }

        /**
         * Enable STARTTLS
         * 启用STARTTLS
         *
         * @param starttls true to enable STARTTLS | true启用STARTTLS
         * @return this builder | 构建器
         */
        public Builder starttls(boolean starttls) {
            this.starttls = starttls;
            return this;
        }

        /**
         * Set default sender email
         * 设置默认发件人邮箱
         *
         * @param from the default sender email | 默认发件人邮箱
         * @return this builder | 构建器
         */
        public Builder defaultFrom(String from) {
            this.defaultFrom = from;
            return this;
        }

        /**
         * Set default sender email with display name
         * 设置默认发件人邮箱和显示名称
         *
         * @param from the default sender email | 默认发件人邮箱
         * @param name the display name | 显示名称
         * @return this builder | 构建器
         */
        public Builder defaultFrom(String from, String name) {
            this.defaultFrom = from;
            this.defaultFromName = name;
            return this;
        }

        /**
         * Set send timeout
         * 设置发送超时
         *
         * @param timeout the timeout duration | 超时时长
         * @return this builder | 构建器
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Set connection timeout
         * 设置连接超时
         *
         * @param timeout the timeout duration | 超时时长
         * @return this builder | 构建器
         */
        public Builder connectionTimeout(Duration timeout) {
            this.connectionTimeout = timeout;
            return this;
        }

        /**
         * Set max retry attempts
         * 设置最大重试次数
         *
         * @param retries the max retries | 最大重试次数
         * @return this builder | 构建器
         */
        public Builder maxRetries(int retries) {
            this.maxRetries = retries;
            return this;
        }

        /**
         * Set connection pool size (reserved for future use)
         * 设置连接池大小（保留供将来使用）
         *
         * <p><strong>Note:</strong> Connection pooling is not implemented in the current version.
         * This setting is reserved for future enhancements.</p>
         * <p><strong>注意：</strong>当前版本未实现连接池。此设置保留供将来增强使用。</p>
         *
         * @param size the pool size | 连接池大小
         * @return this builder | 构建器
         */
        public Builder poolSize(int size) {
            this.poolSize = size;
            return this;
        }

        /**
         * Set pool idle timeout (reserved for future use)
         * 设置连接池空闲超时（保留供将来使用）
         *
         * <p><strong>Note:</strong> Connection pooling is not implemented in the current version.
         * This setting is reserved for future enhancements.</p>
         * <p><strong>注意：</strong>当前版本未实现连接池。此设置保留供将来增强使用。</p>
         *
         * @param timeout the idle timeout | 空闲超时
         * @return this builder | 构建器
         */
        public Builder poolIdleTimeout(Duration timeout) {
            this.poolIdleTimeout = timeout;
            return this;
        }

        /**
         * Enable debug mode
         * 启用调试模式
         *
         * @param debug true to enable debug | true启用调试
         * @return this builder | 构建器
         */
        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        /**
         * Set DKIM signing configuration
         * 设置DKIM签名配置
         *
         * @param dkim the DKIM configuration | DKIM配置
         * @return this builder | 构建器
         */
        public Builder dkim(DkimConfig dkim) {
            this.dkim = dkim;
            return this;
        }

        /**
         * Build the configuration
         * 构建配置
         *
         * @return the configuration | 配置
         */
        public EmailConfig build() {
            return new EmailConfig(
                    host,
                    port,
                    username,
                    password,
                    oauth2Token,
                    ssl,
                    starttls,
                    defaultFrom,
                    defaultFromName,
                    timeout,
                    connectionTimeout,
                    maxRetries,
                    poolSize,
                    poolIdleTimeout,
                    debug,
                    dkim
            );
        }
    }

    /**
     * Check if authentication is required
     * 检查是否需要认证
     *
     * @return true if authentication is required | 需要认证返回true
     */
    public boolean requiresAuth() {
        return hasOAuth2() || (username != null && !username.isBlank()
                && password != null && !password.isBlank());
    }

    /**
     * Check if OAuth2 authentication is configured
     * 检查是否配置了OAuth2认证
     *
     * @return true if OAuth2 is configured | 配置了OAuth2返回true
     */
    public boolean hasOAuth2() {
        return oauth2Token != null && !oauth2Token.isBlank();
    }

    /**
     * Check if DKIM signing is configured
     * 检查是否配置了DKIM签名
     *
     * @return true if DKIM is configured | 配置了DKIM返回true
     */
    public boolean hasDkim() {
        return dkim != null;
    }

    /**
     * Return string representation with masked sensitive fields
     * 返回屏蔽敏感字段的字符串表示
     *
     * <p>Passwords and OAuth2 tokens are masked to prevent accidental exposure in logs.</p>
     * <p>密码和OAuth2令牌被屏蔽以防止在日志中意外暴露。</p>
     *
     * @return the masked string representation | 屏蔽后的字符串表示
     */
    @Override
    public String toString() {
        return "EmailConfig[host=" + host
                + ", port=" + port
                + ", username=" + username
                + ", password=" + (password != null ? "***" : "null")
                + ", oauth2Token=" + (oauth2Token != null ? "***" : "null")
                + ", ssl=" + ssl
                + ", starttls=" + starttls
                + ", defaultFrom=" + defaultFrom
                + ", debug=" + debug
                + "]";
    }
}
