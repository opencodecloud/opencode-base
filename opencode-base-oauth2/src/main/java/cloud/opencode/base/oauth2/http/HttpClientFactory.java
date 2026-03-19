package cloud.opencode.base.oauth2.http;

import cloud.opencode.base.oauth2.OAuth2Config;

import java.time.Duration;
import java.util.Objects;

/**
 * HTTP Client Factory
 * HTTP 客户端工厂
 *
 * <p>Factory for creating OAuth2 HTTP clients.</p>
 * <p>用于创建 OAuth2 HTTP 客户端的工厂。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create HTTP clients with default settings - 使用默认设置创建 HTTP 客户端</li>
 *   <li>Create HTTP clients with custom timeouts - 使用自定义超时创建 HTTP 客户端</li>
 *   <li>Create HTTP clients from config - 从配置创建 HTTP 客户端</li>
 *   <li>Shared client instance support - 共享客户端实例支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create with defaults
 * OAuth2HttpClient client = HttpClientFactory.create();
 *
 * // Create with custom timeouts
 * OAuth2HttpClient client = HttpClientFactory.create(
 *     Duration.ofSeconds(5),   // connect timeout
 *     Duration.ofSeconds(30)   // read timeout
 * );
 *
 * // Create from config
 * OAuth2HttpClient client = HttpClientFactory.create(config);
 *
 * // Get shared instance
 * OAuth2HttpClient shared = HttpClientFactory.shared();
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is thread-safe.</p>
 * <p>此类是线程安全的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public final class HttpClientFactory {

    /**
     * Default connect timeout
     * 默认连接超时
     */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Default read timeout
     * 默认读取超时
     */
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    private static volatile OAuth2HttpClient sharedInstance;
    private static final Object LOCK = new Object();

    private HttpClientFactory() {
        // Utility class - prevent instantiation
    }

    /**
     * Create an HTTP client with default settings
     * 使用默认设置创建 HTTP 客户端
     *
     * @return the HTTP client | HTTP 客户端
     */
    public static OAuth2HttpClient create() {
        return new OAuth2HttpClient(DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Create an HTTP client with custom timeouts
     * 使用自定义超时创建 HTTP 客户端
     *
     * @param connectTimeout the connection timeout | 连接超时
     * @param readTimeout    the read timeout | 读取超时
     * @return the HTTP client | HTTP 客户端
     */
    public static OAuth2HttpClient create(Duration connectTimeout, Duration readTimeout) {
        return new OAuth2HttpClient(
                connectTimeout != null ? connectTimeout : DEFAULT_CONNECT_TIMEOUT,
                readTimeout != null ? readTimeout : DEFAULT_READ_TIMEOUT
        );
    }

    /**
     * Create an HTTP client from OAuth2 configuration
     * 从 OAuth2 配置创建 HTTP 客户端
     *
     * @param config the OAuth2 configuration | OAuth2 配置
     * @return the HTTP client | HTTP 客户端
     */
    public static OAuth2HttpClient create(OAuth2Config config) {
        Objects.requireNonNull(config, "config cannot be null");
        return new OAuth2HttpClient(config);
    }

    /**
     * Get the shared HTTP client instance
     * 获取共享的 HTTP 客户端实例
     *
     * <p>This returns a singleton HTTP client that can be safely shared
     * across multiple OAuth2 clients. The shared client uses default timeouts.</p>
     *
     * <p><strong>Note:</strong> The shared client should not be closed
     * as it may be used by other components.</p>
     *
     * @return the shared HTTP client | 共享的 HTTP 客户端
     */
    public static OAuth2HttpClient shared() {
        if (sharedInstance == null) {
            synchronized (LOCK) {
                if (sharedInstance == null) {
                    sharedInstance = create();
                }
            }
        }
        return sharedInstance;
    }

    /**
     * Create a builder for customizing the HTTP client
     * 创建用于自定义 HTTP 客户端的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * HTTP Client Builder
     * HTTP 客户端构建器
     */
    public static class Builder {
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private Duration readTimeout = DEFAULT_READ_TIMEOUT;

        /**
         * Set the connection timeout
         * 设置连接超时
         *
         * @param connectTimeout the connection timeout | 连接超时
         * @return this builder | 此构建器
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Set the read timeout
         * 设置读取超时
         *
         * @param readTimeout the read timeout | 读取超时
         * @return this builder | 此构建器
         */
        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * Set connection timeout in seconds
         * 设置连接超时（秒）
         *
         * @param seconds the timeout in seconds | 超时时间（秒）
         * @return this builder | 此构建器
         */
        public Builder connectTimeoutSeconds(long seconds) {
            this.connectTimeout = Duration.ofSeconds(seconds);
            return this;
        }

        /**
         * Set read timeout in seconds
         * 设置读取超时（秒）
         *
         * @param seconds the timeout in seconds | 超时时间（秒）
         * @return this builder | 此构建器
         */
        public Builder readTimeoutSeconds(long seconds) {
            this.readTimeout = Duration.ofSeconds(seconds);
            return this;
        }

        /**
         * Build the HTTP client
         * 构建 HTTP 客户端
         *
         * @return the HTTP client | HTTP 客户端
         */
        public OAuth2HttpClient build() {
            return new OAuth2HttpClient(connectTimeout, readTimeout);
        }
    }
}
