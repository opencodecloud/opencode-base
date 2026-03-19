package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;

/**
 * Configuration Validator Interface
 * 配置验证器接口
 *
 * <p>Validates configuration after loading with customizable validation rules.</p>
 * <p>加载后验证配置,支持自定义验证规则。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configuration validation - 配置验证</li>
 *   <li>Custom validation rules - 自定义验证规则</li>
 *   <li>Validation result reporting - 验证结果报告</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Built-in validators
 * ConfigValidator required = new RequiredValidator("database.url", "api.key");
 *
 * // Custom validator
 * ConfigValidator portRange = config -> {
 *     int port = config.getInt("server.port", 0);
 *     if (port < 1024 || port > 65535) {
 *         return ValidationResult.invalid("Port must be between 1024-65535");
 *     }
 *     return ValidationResult.valid();
 * };
 *
 * // Use in builder
 * Config config = OpenConfig.builder()
 *     .addValidator(required)
 *     .addValidator(portRange)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@FunctionalInterface
public interface ConfigValidator {

    /**
     * Validate configuration
     * 验证配置
     *
     * @param config configuration to validate | 要验证的配置
     * @return validation result | 验证结果
     */
    ValidationResult validate(Config config);
}
