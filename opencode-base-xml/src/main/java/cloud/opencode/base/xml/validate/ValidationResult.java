package cloud.opencode.base.xml.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validation Result - Contains XML validation results
 * 验证结果 - 包含 XML 验证结果
 *
 * <p>This class holds the results of XML validation including errors and warnings.</p>
 * <p>此类保存 XML 验证的结果，包括错误和警告。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Container for XML validation errors and warnings - XML 验证错误和警告的容器</li>
 *   <li>Separate tracking of errors vs warnings - 错误与警告的分别跟踪</li>
 *   <li>Validity check and error message formatting - 有效性检查和错误消息格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check validation result
 * ValidationResult result = SchemaValidator.validate(xml, schema);
 * if (result.isValid()) {
 *     System.out.println("XML is valid");
 * } else {
 *     result.getErrors().forEach(System.err::println);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable error list) - 线程安全: 否（可变错误列表）</li>
 *   <li>Null-safe: Yes (empty lists by default) - 空值安全: 是（默认空列表）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class ValidationResult {

    private final List<ValidationError> errors = new ArrayList<>();
    private final List<ValidationError> warnings = new ArrayList<>();

    /**
     * Creates a successful validation result.
     * 创建成功的验证结果。
     *
     * @return a valid result | 有效结果
     */
    public static ValidationResult valid() {
        return new ValidationResult();
    }

    /**
     * Creates an invalid result with a single error.
     * 创建带有单个错误的无效结果。
     *
     * @param error the error | 错误
     * @return an invalid result | 无效结果
     */
    public static ValidationResult invalid(ValidationError error) {
        ValidationResult result = new ValidationResult();
        result.addError(error);
        return result;
    }

    /**
     * Creates an invalid result with a single error message.
     * 创建带有单个错误消息的无效结果。
     *
     * @param message the error message | 错误消息
     * @return an invalid result | 无效结果
     */
    public static ValidationResult invalid(String message) {
        return invalid(new ValidationError(message, -1, -1, ValidationError.Severity.ERROR));
    }

    /**
     * Checks if the validation passed with no errors.
     * 检查验证是否通过（无错误）。
     *
     * @return true if valid | 如果有效则返回 true
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Checks if the validation has any errors.
     * 检查验证是否有任何错误。
     *
     * @return true if has errors | 如果有错误则返回 true
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Checks if the validation has any warnings.
     * 检查验证是否有任何警告。
     *
     * @return true if has warnings | 如果有警告则返回 true
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Gets all errors.
     * 获取所有错误。
     *
     * @return list of errors | 错误列表
     */
    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Gets all warnings.
     * 获取所有警告。
     *
     * @return list of warnings | 警告列表
     */
    public List<ValidationError> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Gets all issues (errors + warnings).
     * 获取所有问题（错误 + 警告）。
     *
     * @return list of all issues | 所有问题列表
     */
    public List<ValidationError> getAllIssues() {
        List<ValidationError> all = new ArrayList<>(errors);
        all.addAll(warnings);
        return Collections.unmodifiableList(all);
    }

    /**
     * Gets the error count.
     * 获取错误数量。
     *
     * @return the error count | 错误数量
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Gets the warning count.
     * 获取警告数量。
     *
     * @return the warning count | 警告数量
     */
    public int getWarningCount() {
        return warnings.size();
    }

    /**
     * Gets the first error message, or null if no errors.
     * 获取第一个错误消息，如果没有错误则返回 null。
     *
     * @return the first error message | 第一个错误消息
     */
    public String getFirstErrorMessage() {
        return errors.isEmpty() ? null : errors.getFirst().getMessage();
    }

    /**
     * Adds an error.
     * 添加错误。
     *
     * @param error the error | 错误
     */
    public void addError(ValidationError error) {
        errors.add(error);
    }

    /**
     * Adds an error with message and location.
     * 添加带消息和位置的错误。
     *
     * @param message the message | 消息
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     */
    public void addError(String message, int line, int column) {
        errors.add(new ValidationError(message, line, column, ValidationError.Severity.ERROR));
    }

    /**
     * Adds a warning.
     * 添加警告。
     *
     * @param warning the warning | 警告
     */
    public void addWarning(ValidationError warning) {
        warnings.add(warning);
    }

    /**
     * Adds a warning with message and location.
     * 添加带消息和位置的警告。
     *
     * @param message the message | 消息
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     */
    public void addWarning(String message, int line, int column) {
        warnings.add(new ValidationError(message, line, column, ValidationError.Severity.WARNING));
    }

    /**
     * Merges another result into this one.
     * 将另一个结果合并到此结果中。
     *
     * @param other the other result | 另一个结果
     * @return this result for chaining | 此结果以便链式调用
     */
    public ValidationResult merge(ValidationResult other) {
        errors.addAll(other.errors);
        warnings.addAll(other.warnings);
        return this;
    }

    /**
     * Gets a summary of all errors.
     * 获取所有错误的摘要。
     *
     * @return the error summary | 错误摘要
     */
    public String getErrorSummary() {
        if (errors.isEmpty()) {
            return "No errors";
        }
        return errors.stream()
            .map(ValidationError::toString)
            .collect(Collectors.joining("\n"));
    }

    /**
     * Gets error messages as strings.
     * 获取错误消息字符串列表。
     *
     * @return list of error messages | 错误消息列表
     */
    public List<String> getErrorMessages() {
        return errors.stream()
            .map(ValidationError::toString)
            .toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationResult{valid=").append(isValid());
        sb.append(", errors=").append(errors.size());
        sb.append(", warnings=").append(warnings.size());
        if (!errors.isEmpty()) {
            sb.append(", firstError='").append(getFirstErrorMessage()).append("'");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Validation Error - Represents a single validation error or warning
     * 验证错误 - 表示单个验证错误或警告
     */
    public record ValidationError(String message, int line, int column, Severity severity) {

        /**
         * Error severity levels.
         * 错误严重级别。
         */
        public enum Severity {
            WARNING, ERROR, FATAL
        }

        /**
         * Gets the message.
         * 获取消息。
         *
         * @return the message | 消息
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets the line number.
         * 获取行号。
         *
         * @return the line number | 行号
         */
        public int getLine() {
            return line;
        }

        /**
         * Gets the column number.
         * 获取列号。
         *
         * @return the column number | 列号
         */
        public int getColumn() {
            return column;
        }

        /**
         * Gets the severity.
         * 获取严重级别。
         *
         * @return the severity | 严重级别
         */
        public Severity getSeverity() {
            return severity;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(severity).append("]");
            if (line > 0) {
                sb.append(" Line ").append(line);
                if (column > 0) {
                    sb.append(":").append(column);
                }
                sb.append(":");
            }
            sb.append(" ").append(message);
            return sb.toString();
        }
    }
}
