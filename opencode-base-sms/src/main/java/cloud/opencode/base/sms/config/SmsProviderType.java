package cloud.opencode.base.sms.config;

/**
 * SMS Provider Type
 * 短信提供商类型
 *
 * <p>Supported SMS provider types.</p>
 * <p>支持的短信提供商类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Built-in providers: Aliyun, Tencent, Huawei, Baidu, Twilio, AWS SNS - 内置提供商</li>
 *   <li>Console mock provider for testing - 控制台模拟提供商用于测试</li>
 *   <li>Custom provider extension support - 自定义提供商扩展支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SmsProviderType type = SmsProviderType.ALIYUN;
 * String code = type.getCode(); // "aliyun"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 线程安全: 是（枚举不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public enum SmsProviderType {

    /**
     * Aliyun SMS (阿里云短信)
     */
    ALIYUN("aliyun"),

    /**
     * Tencent Cloud SMS (腾讯云短信)
     */
    TENCENT("tencent"),

    /**
     * Huawei Cloud SMS (华为云短信)
     */
    HUAWEI("huawei"),

    /**
     * Baidu Cloud SMS (百度云短信)
     */
    BAIDU("baidu"),

    /**
     * Twilio SMS
     */
    TWILIO("twilio"),

    /**
     * AWS SNS
     */
    AWS_SNS("aws_sns"),

    /**
     * Console mock provider (控制台模拟提供商)
     */
    CONSOLE("console"),

    /**
     * Custom provider (自定义提供商)
     */
    CUSTOM("custom");

    private final String code;

    SmsProviderType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static SmsProviderType fromCode(String code) {
        for (SmsProviderType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown provider type: " + code);
    }
}
