package cloud.opencode.base.graph.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validation Result
 * 验证结果
 *
 * <p>Represents the result of graph validation.</p>
 * <p>表示图验证的结果。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Collects warnings and errors | 收集警告和错误</li>
 *   <li>Provides validation status | 提供验证状态</li>
 *   <li>Immutable result structure | 不可变的结果结构</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ValidationResult result = GraphValidator.validateGraph(graph);
 * if (result.hasErrors()) {
 *     result.errors().forEach(System.err::println);
 * }
 * if (result.hasWarnings()) {
 *     result.warnings().forEach(System.out::println);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes (stores unmodifiable lists) - 空值安全: 是（存储不可修改的列表）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class ValidationResult {

    private final List<String> warnings;
    private final List<String> errors;

    /**
     * Create a validation result
     * 创建验证结果
     *
     * @param warnings list of warnings | 警告列表
     * @param errors list of errors | 错误列表
     */
    public ValidationResult(List<String> warnings, List<String> errors) {
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    /**
     * Create a successful validation result
     * 创建成功的验证结果
     *
     * @return a successful validation result | 成功的验证结果
     */
    public static ValidationResult success() {
        return new ValidationResult(Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Create a validation result with a single error
     * 创建带单个错误的验证结果
     *
     * @param error the error message | 错误消息
     * @return a validation result with the error | 带错误的验证结果
     */
    public static ValidationResult error(String error) {
        return new ValidationResult(Collections.emptyList(), List.of(error));
    }

    /**
     * Create a validation result with a single warning
     * 创建带单个警告的验证结果
     *
     * @param warning the warning message | 警告消息
     * @return a validation result with the warning | 带警告的验证结果
     */
    public static ValidationResult warning(String warning) {
        return new ValidationResult(List.of(warning), Collections.emptyList());
    }

    /**
     * Get the list of warnings
     * 获取警告列表
     *
     * @return list of warnings | 警告列表
     */
    public List<String> warnings() {
        return warnings;
    }

    /**
     * Get the list of errors
     * 获取错误列表
     *
     * @return list of errors | 错误列表
     */
    public List<String> errors() {
        return errors;
    }

    /**
     * Check if validation passed (no errors)
     * 检查验证是否通过（无错误）
     *
     * @return true if valid | 如果有效返回true
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Check if there are errors
     * 检查是否有错误
     *
     * @return true if there are errors | 如果有错误返回true
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Check if there are warnings
     * 检查是否有警告
     *
     * @return true if there are warnings | 如果有警告返回true
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Get the count of errors
     * 获取错误数量
     *
     * @return error count | 错误数量
     */
    public int errorCount() {
        return errors.size();
    }

    /**
     * Get the count of warnings
     * 获取警告数量
     *
     * @return warning count | 警告数量
     */
    public int warningCount() {
        return warnings.size();
    }

    /**
     * Merge with another validation result
     * 与另一个验证结果合并
     *
     * @param other the other validation result | 另一个验证结果
     * @return merged validation result | 合并的验证结果
     */
    public ValidationResult merge(ValidationResult other) {
        List<String> mergedWarnings = new ArrayList<>(this.warnings);
        mergedWarnings.addAll(other.warnings);

        List<String> mergedErrors = new ArrayList<>(this.errors);
        mergedErrors.addAll(other.errors);

        return new ValidationResult(mergedWarnings, mergedErrors);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationResult{");
        sb.append("valid=").append(isValid());
        sb.append(", errors=").append(errors.size());
        sb.append(", warnings=").append(warnings.size());

        if (!errors.isEmpty()) {
            sb.append(", errorMessages=").append(errors);
        }
        if (!warnings.isEmpty()) {
            sb.append(", warningMessages=").append(warnings);
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Builder for ValidationResult
     * ValidationResult构建器
     */
    public static class Builder {
        private final List<String> warnings = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        /**
         * Add a warning
         * 添加警告
         *
         * @param warning the warning message | 警告消息
         * @return this builder | 此构建器
         */
        public Builder addWarning(String warning) {
            warnings.add(warning);
            return this;
        }

        /**
         * Add an error
         * 添加错误
         *
         * @param error the error message | 错误消息
         * @return this builder | 此构建器
         */
        public Builder addError(String error) {
            errors.add(error);
            return this;
        }

        /**
         * Build the validation result
         * 构建验证结果
         *
         * @return the built validation result | 构建的验证结果
         */
        public ValidationResult build() {
            return new ValidationResult(warnings, errors);
        }
    }
}
