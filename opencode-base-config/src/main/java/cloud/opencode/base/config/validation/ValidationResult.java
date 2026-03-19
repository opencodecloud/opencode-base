package cloud.opencode.base.config.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration Validation Result
 * 配置验证结果
 *
 * <p>Represents the result of configuration validation with error messages.</p>
 * <p>表示配置验证的结果及错误消息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Validation success/failure status - 验证成功/失败状态</li>
 *   <li>Multiple error message support - 支持多个错误消息</li>
 *   <li>Factory methods for common cases - 常见情况的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Valid result
 * ValidationResult result = ValidationResult.valid();
 *
 * // Invalid with single error
 * ValidationResult result = ValidationResult.invalid("Port out of range");
 *
 * // Invalid with multiple errors
 * ValidationResult result = ValidationResult.invalid(List.of(
 *     "Missing required key: database.url",
 *     "Invalid port: must be 1024-65535"
 * ));
 *
 * // Check result
 * if (!result.isValid()) {
 *     System.out.println("Validation failed: " + result.getErrors());
 * }
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
public class ValidationResult {

    private static final ValidationResult VALID_INSTANCE = new ValidationResult(true, List.of());

    private final boolean valid;
    private final List<String> errors;

    private ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(errors);
    }

    /**
     * Check if validation passed
     * 检查验证是否通过
     *
     * @return true if valid | 如果有效返回true
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Get validation errors
     * 获取验证错误
     *
     * @return list of error messages | 错误消息列表
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Create valid result
     * 创建有效结果
     *
     * @return valid result | 有效结果
     */
    public static ValidationResult valid() {
        return VALID_INSTANCE;
    }

    /**
     * Create invalid result with single error
     * 创建带单个错误的无效结果
     *
     * @param error error message | 错误消息
     * @return invalid result | 无效结果
     */
    public static ValidationResult invalid(String error) {
        return new ValidationResult(false, List.of(error));
    }

    /**
     * Create invalid result with multiple errors
     * 创建带多个错误的无效结果
     *
     * @param errors error messages | 错误消息列表
     * @return invalid result | 无效结果
     */
    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult(false, new ArrayList<>(errors));
    }

    @Override
    public String toString() {
        return valid ? "Valid" : "Invalid: " + errors;
    }
}
