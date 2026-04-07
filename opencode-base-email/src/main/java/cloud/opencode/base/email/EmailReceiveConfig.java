package cloud.opencode.base.email;

import java.time.Duration;

/**
 * Email Receive Configuration Record
 * 邮件接收配置记录
 *
 * <p>Immutable configuration for IMAP/POP3 email receiving.</p>
 * <p>用于IMAP/POP3邮件接收的不可变配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>IMAP/POP3 protocol support - IMAP/POP3协议支持</li>
 *   <li>SSL/TLS configuration - SSL/TLS配置</li>
 *   <li>Connection pool settings - 连接池设置</li>
 *   <li>Timeout configuration - 超时配置</li>
 *   <li>Folder preferences - 文件夹偏好设置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // IMAP configuration
 * EmailReceiveConfig config = EmailReceiveConfig.builder()
 *     .host("imap.example.com")
 *     .username("user@example.com")
 *     .password("password")
 *     .imap()
 *     .ssl(true)
 *     .build();
 *
 * // POP3 configuration
 * EmailReceiveConfig config = EmailReceiveConfig.builder()
 *     .host("pop3.example.com")
 *     .port(995)
 *     .username("user")
 *     .password("pass")
 *     .pop3()
 *     .ssl(true)
 *     .build();
 *
 * // OAuth2 configuration (Gmail/Outlook)
 * EmailReceiveConfig config = EmailReceiveConfig.builder()
 *     .host("imap.gmail.com")
 *     .username("user@gmail.com")
 *     .oauth2Token(accessToken)
 *     .imap()
 *     .ssl(true)
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
public record EmailReceiveConfig(
        String host,
        int port,
        String username,
        String password,
        String oauth2Token,
        Protocol protocol,
        boolean ssl,
        boolean starttls,
        Duration timeout,
        Duration connectionTimeout,
        String defaultFolder,
        int maxMessages,
        boolean deleteAfterReceive,
        boolean markAsReadAfterReceive,
        boolean debug
) {

    /**
     * Email receiving protocol
     * 邮件接收协议
     */
    public enum Protocol {
        /**
         * IMAP protocol (recommended)
         * IMAP协议（推荐）
         */
        IMAP("imap", 143, 993),

        /**
         * POP3 protocol
         * POP3协议
         */
        POP3("pop3", 110, 995);

        private final String name;
        private final int defaultPort;
        private final int defaultSslPort;

        Protocol(String name, int defaultPort, int defaultSslPort) {
            this.name = name;
            this.defaultPort = defaultPort;
            this.defaultSslPort = defaultSslPort;
        }

        /**
         * Get protocol name
         * 获取协议名称
         *
         * @return the protocol name | 协议名称
         */
        public String getName() {
            return name;
        }

        /**
         * Get default port
         * 获取默认端口
         *
         * @return the default port | 默认端口
         */
        public int getDefaultPort() {
            return defaultPort;
        }

        /**
         * Get default SSL port
         * 获取默认SSL端口
         *
         * @return the default SSL port | 默认SSL端口
         */
        public int getDefaultSslPort() {
            return defaultSslPort;
        }

        /**
         * Get Jakarta Mail store protocol name
         * 获取Jakarta Mail存储协议名称
         *
         * @param ssl whether SSL is enabled | 是否启用SSL
         * @return the store protocol name | 存储协议名称
         */
        public String getStoreProtocol(boolean ssl) {
            return ssl ? name + "s" : name;
        }
    }

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
     * Email Receive Configuration Builder
     * 邮件接收配置构建器
     */
    public static class Builder {
        private String host;
        private int port = -1;  // -1 means use protocol default
        private String username;
        private String password;
        private String oauth2Token;
        private Protocol protocol = Protocol.IMAP;
        private boolean ssl = true;
        private boolean starttls = false;
        private Duration timeout = Duration.ofSeconds(30);
        private Duration connectionTimeout = Duration.ofSeconds(10);
        private String defaultFolder = "INBOX";
        private int maxMessages = 100;
        private boolean deleteAfterReceive = false;
        private boolean markAsReadAfterReceive = true;
        private boolean debug = false;

        /**
         * Set mail server host
         * 设置邮件服务器主机
         *
         * @param host the mail server host | 邮件服务器主机
         * @return this builder | 构建器
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Set mail server port
         * 设置邮件服务器端口
         *
         * @param port the mail server port | 邮件服务器端口
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
         * Use IMAP protocol
         * 使用IMAP协议
         *
         * @return this builder | 构建器
         */
        public Builder imap() {
            this.protocol = Protocol.IMAP;
            return this;
        }

        /**
         * Use POP3 protocol
         * 使用POP3协议
         *
         * @return this builder | 构建器
         */
        public Builder pop3() {
            this.protocol = Protocol.POP3;
            return this;
        }

        /**
         * Set protocol
         * 设置协议
         *
         * @param protocol the protocol | 协议
         * @return this builder | 构建器
         */
        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
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
         * Set operation timeout
         * 设置操作超时
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
         * Set default folder to read from
         * 设置默认读取文件夹
         *
         * @param folder the default folder | 默认文件夹
         * @return this builder | 构建器
         */
        public Builder defaultFolder(String folder) {
            this.defaultFolder = folder;
            return this;
        }

        /**
         * Set maximum messages to fetch per request
         * 设置每次请求获取的最大邮件数
         *
         * @param maxMessages the maximum messages | 最大邮件数
         * @return this builder | 构建器
         */
        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        /**
         * Delete messages after receiving
         * 接收后删除邮件
         *
         * @param delete true to delete after receive | true接收后删除
         * @return this builder | 构建器
         */
        public Builder deleteAfterReceive(boolean delete) {
            this.deleteAfterReceive = delete;
            return this;
        }

        /**
         * Mark messages as read after receiving
         * 接收后标记邮件为已读
         *
         * @param markAsRead true to mark as read | true标记为已读
         * @return this builder | 构建器
         */
        public Builder markAsReadAfterReceive(boolean markAsRead) {
            this.markAsReadAfterReceive = markAsRead;
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
         * Build the configuration
         * 构建配置
         *
         * @return the configuration | 配置
         */
        public EmailReceiveConfig build() {
            int effectivePort = port;
            if (effectivePort == -1) {
                effectivePort = ssl ? protocol.getDefaultSslPort() : protocol.getDefaultPort();
            }
            return new EmailReceiveConfig(
                    host,
                    effectivePort,
                    username,
                    password,
                    oauth2Token,
                    protocol,
                    ssl,
                    starttls,
                    timeout,
                    connectionTimeout,
                    defaultFolder,
                    maxMessages,
                    deleteAfterReceive,
                    markAsReadAfterReceive,
                    debug
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
     * Get the store protocol name for Jakarta Mail
     * 获取Jakarta Mail的存储协议名称
     *
     * @return the store protocol name | 存储协议名称
     */
    public String getStoreProtocol() {
        return protocol.getStoreProtocol(ssl);
    }

    /**
     * Check if IMAP protocol is used
     * 检查是否使用IMAP协议
     *
     * @return true if using IMAP | 使用IMAP返回true
     */
    public boolean isImap() {
        return protocol == Protocol.IMAP;
    }

    /**
     * Check if POP3 protocol is used
     * 检查是否使用POP3协议
     *
     * @return true if using POP3 | 使用POP3返回true
     */
    public boolean isPop3() {
        return protocol == Protocol.POP3;
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
        return "EmailReceiveConfig[host=" + host
                + ", port=" + port
                + ", username=" + username
                + ", password=" + (password != null ? "***" : "null")
                + ", oauth2Token=" + (oauth2Token != null ? "***" : "null")
                + ", protocol=" + protocol
                + ", ssl=" + ssl
                + ", defaultFolder=" + defaultFolder
                + ", debug=" + debug
                + "]";
    }
}
