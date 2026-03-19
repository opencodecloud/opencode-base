package cloud.opencode.base.sms.config;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP SMS Config
 * HTTP短信配置
 *
 * <p>Configuration for HTTP-based SMS providers.</p>
 * <p>基于HTTP的短信提供商配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HTTP-based SMS provider configuration - 基于HTTP的短信提供商配置</li>
 *   <li>Builder pattern for construction - 构建器模式构建</li>
 *   <li>Configuration validation check - 配置验证检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HttpSmsConfig config = HttpSmsConfig.builder()
 *     .name("myProvider")
 *     .apiUrl("https://sms-api.example.com/send")
 *     .appId("appId").appKey("appKey")
 *     .signName("MySign")
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param name the provider name | 提供商名称
 * @param apiUrl the API URL | API地址
 * @param appId the app ID | 应用ID
 * @param appKey the app key | 应用密钥
 * @param signName the sign name | 签名名称
 * @param extra extra configuration | 额外配置
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public record HttpSmsConfig(
    String name,
    String apiUrl,
    String appId,
    String appKey,
    String signName,
    Map<String, String> extra
) {

    /**
     * Create builder
     * 创建构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Check if configured
     * 检查是否已配置
     *
     * @return true if configured | 如果已配置返回true
     */
    public boolean isConfigured() {
        return apiUrl != null && !apiUrl.isEmpty();
    }

    /**
     * HTTP SMS Config Builder
     * HTTP短信配置构建器
     */
    public static class Builder {
        private String name = "http";
        private String apiUrl;
        private String appId;
        private String appKey;
        private String signName;
        private final Map<String, String> extra = new HashMap<>();

        /**
         * Set name
         * 设置名称
         *
         * @param name the name | 名称
         * @return this builder | 此构建器
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set API URL
         * 设置API地址
         *
         * @param apiUrl the API URL | API地址
         * @return this builder | 此构建器
         */
        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        /**
         * Set app ID
         * 设置应用ID
         *
         * @param appId the app ID | 应用ID
         * @return this builder | 此构建器
         */
        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        /**
         * Set app key
         * 设置应用密钥
         *
         * @param appKey the app key | 应用密钥
         * @return this builder | 此构建器
         */
        public Builder appKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        /**
         * Set sign name
         * 设置签名名称
         *
         * @param signName the sign name | 签名名称
         * @return this builder | 此构建器
         */
        public Builder signName(String signName) {
            this.signName = signName;
            return this;
        }

        /**
         * Add extra config
         * 添加额外配置
         *
         * @param key the key | 键
         * @param value the value | 值
         * @return this builder | 此构建器
         */
        public Builder extra(String key, String value) {
            this.extra.put(key, value);
            return this;
        }

        /**
         * Build the config
         * 构建配置
         *
         * @return the config | 配置
         */
        public HttpSmsConfig build() {
            return new HttpSmsConfig(name, apiUrl, appId, appKey, signName, Map.copyOf(extra));
        }
    }
}
