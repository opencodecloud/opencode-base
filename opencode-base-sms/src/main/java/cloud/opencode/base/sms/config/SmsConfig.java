package cloud.opencode.base.sms.config;

import java.time.Duration;
import java.util.Map;

/**
 * SMS Config
 * 短信配置
 *
 * <p>Configuration for SMS provider.</p>
 * <p>短信提供商配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record-based configuration - 基于记录的不可变配置</li>
 *   <li>Builder pattern for fluent construction - 构建器模式流畅构建</li>
 *   <li>Defensive copy of extra properties - 额外属性防御性复制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SmsConfig config = SmsConfig.builder()
 *     .providerType(SmsProviderType.ALIYUN)
 *     .accessKey("yourAccessKey")
 *     .secretKey("yourSecretKey")
 *     .signName("YourSign")
 *     .region("cn-hangzhou")
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (defaults applied in compact constructor) - 空值安全: 是（紧凑构造器中应用默认值）</li>
 * </ul>
 *
 * @param providerType the provider type | 提供商类型
 * @param accessKey the access key | 访问密钥
 * @param secretKey the secret key | 密钥
 * @param signName the sign name | 签名名称
 * @param region the region | 区域
 * @param timeout the timeout | 超时
 * @param extraProperties extra properties | 额外属性
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public record SmsConfig(
    SmsProviderType providerType,
    String accessKey,
    String secretKey,
    String signName,
    String region,
    Duration timeout,
    Map<String, String> extraProperties
) {
    public SmsConfig {
        extraProperties = extraProperties != null ? Map.copyOf(extraProperties) : Map.of();
        timeout = timeout != null ? timeout : Duration.ofSeconds(30);
    }

    /**
     * Builder for SmsConfig
     * SmsConfig构建器
     */
    public static class Builder {
        private SmsProviderType providerType;
        private String accessKey;
        private String secretKey;
        private String signName;
        private String region;
        private Duration timeout = Duration.ofSeconds(30);
        private Map<String, String> extraProperties = Map.of();

        public Builder providerType(SmsProviderType providerType) {
            this.providerType = providerType;
            return this;
        }

        public Builder accessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        public Builder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder signName(String signName) {
            this.signName = signName;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder extraProperties(Map<String, String> extraProperties) {
            this.extraProperties = extraProperties;
            return this;
        }

        public SmsConfig build() {
            return new SmsConfig(providerType, accessKey, secretKey, signName, region, timeout, extraProperties);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
