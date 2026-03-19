package cloud.opencode.base.sms.config;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Provider Config - SMS provider specific configuration
 * 服务商配置 - 短信服务商特定配置
 *
 * <p>Configuration for specific SMS provider settings such as endpoints,
 * API versions, and provider-specific options.</p>
 * <p>特定短信服务商设置的配置，如端点、API版本和服务商特定选项。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Provider-specific endpoint and API version config - 服务商特定端点和API版本配置</li>
 *   <li>Timeout and retry configuration - 超时和重试配置</li>
 *   <li>Builder pattern with defaults - 带默认值的构建器模式</li>
 *   <li>Extra options map for extensibility - 额外选项映射用于扩展</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * // Aliyun SMS configuration
 * ProviderConfig aliConfig = ProviderConfig.builder()
 *     .name("aliyun")
 *     .endpoint("dysmsapi.aliyuncs.com")
 *     .apiVersion("2017-05-25")
 *     .region("cn-hangzhou")
 *     .maxRetries(3)
 *     .build();
 *
 * // Tencent Cloud SMS configuration
 * ProviderConfig tencentConfig = ProviderConfig.builder()
 *     .name("tencent")
 *     .endpoint("sms.tencentcloudapi.com")
 *     .apiVersion("2021-01-11")
 *     .sdkAppId("1400xxxxx")
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (defaults applied in compact constructor) - 空值安全: 是（紧凑构造器中应用默认值）</li>
 * </ul>
 *
 * @param name           the provider name | 服务商名称
 * @param endpoint       the API endpoint | API端点
 * @param apiVersion     the API version | API版本
 * @param region         the region | 区域
 * @param timeout        the request timeout | 请求超时
 * @param connectTimeout the connection timeout | 连接超时
 * @param maxRetries     the max retry attempts | 最大重试次数
 * @param sdkAppId       the SDK app ID (for Tencent) | SDK应用ID(腾讯云)
 * @param extraOptions   extra provider-specific options | 额外服务商特定选项
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public record ProviderConfig(
        String name,
        String endpoint,
        String apiVersion,
        String region,
        Duration timeout,
        Duration connectTimeout,
        int maxRetries,
        String sdkAppId,
        Map<String, String> extraOptions
) {
    /**
     * Canonical constructor with validation.
     * 带验证的规范构造函数。
     */
    public ProviderConfig {
        Objects.requireNonNull(name, "name cannot be null");
        timeout = timeout != null ? timeout : Duration.ofSeconds(30);
        connectTimeout = connectTimeout != null ? connectTimeout : Duration.ofSeconds(10);
        maxRetries = maxRetries >= 0 ? maxRetries : 3;
        extraOptions = extraOptions != null ? Map.copyOf(extraOptions) : Map.of();
    }

    /**
     * Creates a builder.
     * 创建构建器。
     *
     * @return new builder | 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a default Aliyun configuration.
     * 创建默认阿里云配置。
     *
     * @param region the region | 区域
     * @return the config | 配置
     */
    public static ProviderConfig aliyun(String region) {
        return builder()
                .name("aliyun")
                .endpoint("dysmsapi.aliyuncs.com")
                .apiVersion("2017-05-25")
                .region(region)
                .build();
    }

    /**
     * Creates a default Tencent configuration.
     * 创建默认腾讯云配置。
     *
     * @param sdkAppId the SDK app ID | SDK应用ID
     * @return the config | 配置
     */
    public static ProviderConfig tencent(String sdkAppId) {
        return builder()
                .name("tencent")
                .endpoint("sms.tencentcloudapi.com")
                .apiVersion("2021-01-11")
                .sdkAppId(sdkAppId)
                .build();
    }

    /**
     * Creates a default Huawei configuration.
     * 创建默认华为云配置。
     *
     * @param region the region | 区域
     * @return the config | 配置
     */
    public static ProviderConfig huawei(String region) {
        return builder()
                .name("huawei")
                .endpoint("smsapi." + region + ".myhuaweicloud.com")
                .apiVersion("v1")
                .region(region)
                .build();
    }

    /**
     * Gets an extra option value.
     * 获取额外选项值。
     *
     * @param key the option key | 选项键
     * @return the value or null | 值或null
     */
    public String getOption(String key) {
        return extraOptions.get(key);
    }

    /**
     * Gets an extra option value with default.
     * 获取带默认值的额外选项值。
     *
     * @param key          the option key | 选项键
     * @param defaultValue the default value | 默认值
     * @return the value or default | 值或默认值
     */
    public String getOption(String key, String defaultValue) {
        return extraOptions.getOrDefault(key, defaultValue);
    }

    /**
     * Builder for ProviderConfig.
     * ProviderConfig的构建器。
     */
    public static class Builder {
        private String name;
        private String endpoint;
        private String apiVersion;
        private String region;
        private Duration timeout = Duration.ofSeconds(30);
        private Duration connectTimeout = Duration.ofSeconds(10);
        private int maxRetries = 3;
        private String sdkAppId;
        private Map<String, String> extraOptions = Map.of();

        /**
         * Sets the provider name.
         * 设置服务商名称。
         *
         * @param name the name | 名称
         * @return this builder | 此构建器
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the API endpoint.
         * 设置API端点。
         *
         * @param endpoint the endpoint | 端点
         * @return this builder | 此构建器
         */
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Sets the API version.
         * 设置API版本。
         *
         * @param apiVersion the API version | API版本
         * @return this builder | 此构建器
         */
        public Builder apiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        /**
         * Sets the region.
         * 设置区域。
         *
         * @param region the region | 区域
         * @return this builder | 此构建器
         */
        public Builder region(String region) {
            this.region = region;
            return this;
        }

        /**
         * Sets the request timeout.
         * 设置请求超时。
         *
         * @param timeout the timeout | 超时
         * @return this builder | 此构建器
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets the connection timeout.
         * 设置连接超时。
         *
         * @param connectTimeout the connect timeout | 连接超时
         * @return this builder | 此构建器
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Sets the max retries.
         * 设置最大重试次数。
         *
         * @param maxRetries the max retries | 最大重试次数
         * @return this builder | 此构建器
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets the SDK app ID (for Tencent).
         * 设置SDK应用ID(腾讯云)。
         *
         * @param sdkAppId the SDK app ID | SDK应用ID
         * @return this builder | 此构建器
         */
        public Builder sdkAppId(String sdkAppId) {
            this.sdkAppId = sdkAppId;
            return this;
        }

        /**
         * Sets extra options.
         * 设置额外选项。
         *
         * @param extraOptions the extra options | 额外选项
         * @return this builder | 此构建器
         */
        public Builder extraOptions(Map<String, String> extraOptions) {
            this.extraOptions = extraOptions;
            return this;
        }

        /**
         * Adds an extra option.
         * 添加额外选项。
         *
         * @param key   the key | 键
         * @param value the value | 值
         * @return this builder | 此构建器
         */
        public Builder option(String key, String value) {
            this.extraOptions = new java.util.HashMap<>(this.extraOptions);
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the ProviderConfig.
         * 构建ProviderConfig。
         *
         * @return the config | 配置
         */
        public ProviderConfig build() {
            return new ProviderConfig(name, endpoint, apiVersion, region, timeout,
                    connectTimeout, maxRetries, sdkAppId, extraOptions);
        }
    }
}
