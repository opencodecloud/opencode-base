package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Required Keys Validator
 * 必填键验证器
 *
 * <p>Validates that required configuration keys are present.</p>
 * <p>验证必填的配置键是否存在。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Required key validation - 必填键验证</li>
 *   <li>Multiple key support - 支持多个键</li>
 *   <li>Clear error messages - 清晰的错误消息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validate required keys
 * RequiredValidator validator = new RequiredValidator(
 *     "database.url",
 *     "database.username",
 *     "database.password",
 *     "api.key"
 * );
 *
 * ValidationResult result = validator.validate(config);
 * if (!result.isValid()) {
 *     System.err.println("Missing required configuration: " + result.getErrors());
 * }
 *
 * // Use in builder
 * Config config = OpenConfig.builder()
 *     .required("database.url", "api.key")
 *     .build();
 * }</pre>
 *
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class RequiredValidator implements ConfigValidator {

    private final String[] requiredKeys;

    /**
     * Create required validator
     * 创建必填验证器
     *
     * @param requiredKeys required configuration keys | 必填配置键
     */
    public RequiredValidator(String... requiredKeys) {
        this.requiredKeys = requiredKeys;
    }

    @Override
    public ValidationResult validate(Config config) {
        List<String> missingKeys = new ArrayList<>();

        for (String key : requiredKeys) {
            if (!config.hasKey(key)) {
                missingKeys.add(key);
            }
        }

        if (missingKeys.isEmpty()) {
            return ValidationResult.valid();
        }

        List<String> errors = missingKeys.stream()
            .map(key -> "Required configuration key missing: " + key)
            .toList();

        return ValidationResult.invalid(errors);
    }
}
