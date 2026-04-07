package cloud.opencode.base.yml.schema;

import java.util.Collections;
import java.util.List;

/**
 * ValidationResult - Result of YAML schema validation
 * ValidationResult - YAML 模式验证的结果
 *
 * <p>An immutable record representing the outcome of validating YAML data against a schema.
 * Contains a validity flag and an immutable list of errors (empty when valid).</p>
 * <p>一个不可变记录，表示根据模式验证 YAML 数据的结果。
 * 包含有效性标志和不可变的错误列表（有效时为空）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for success and failure - 成功和失败的工厂方法</li>
 *   <li>Immutable error list - 不可变的错误列表</li>
 *   <li>Convenience isValid() check - 便捷的 isValid() 检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Success
 * ValidationResult ok = ValidationResult.success();
 * assert ok.isValid();
 *
 * // Failure
 * ValidationResult fail = ValidationResult.failure(List.of(
 *     new ValidationError("port", "Required key missing", ErrorType.MISSING_REQUIRED)
 * ));
 * assert !fail.isValid();
 * assert fail.getErrors().size() == 1;
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public record ValidationResult(boolean valid, List<ValidationError> errors) {

    /**
     * Canonical constructor that ensures the error list is immutable.
     * 规范构造函数，确保错误列表不可变。
     *
     * @param valid  whether validation passed | 验证是否通过
     * @param errors the list of validation errors | 验证错误列表
     */
    public ValidationResult {
        errors = errors != null
            ? Collections.unmodifiableList(List.copyOf(errors))
            : Collections.emptyList();
    }

    /**
     * Creates a successful validation result with no errors.
     * 创建没有错误的成功验证结果。
     *
     * @return a successful result | 成功结果
     */
    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }

    /**
     * Creates a failure validation result with the given errors.
     * 创建带有给定错误的失败验证结果。
     *
     * @param errors the validation errors | 验证错误
     * @return a failure result | 失败结果
     */
    public static ValidationResult failure(List<ValidationError> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("Failure result must have at least one error");
        }
        return new ValidationResult(false, errors);
    }

    /**
     * Returns whether the validation passed.
     * 返回验证是否通过。
     *
     * @return true if valid | 如果有效则返回 true
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Returns the list of validation errors.
     * 返回验证错误列表。
     *
     * @return immutable list of errors | 不可变的错误列表
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult{valid=true}";
        }
        return "ValidationResult{valid=false, errors=" + errors + "}";
    }
}
