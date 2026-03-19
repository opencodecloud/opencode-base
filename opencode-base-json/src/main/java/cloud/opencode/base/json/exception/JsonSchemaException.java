
package cloud.opencode.base.json.exception;

import java.io.Serial;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * JSON Schema Exception - Exception for Schema Validation Failures
 * JSON Schema 异常 - Schema 验证失败异常
 *
 * <p>This exception is thrown when JSON Schema validation fails. It contains
 * detailed information about all validation errors encountered.</p>
 * <p>当 JSON Schema 验证失败时抛出此异常。它包含遇到的所有验证错误的详细信息。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * try {
 *     JsonSchemaValidator.validate(json, schema);
 * } catch (JsonSchemaException e) {
 *     for (ValidationError error : e.getErrors()) {
 *         System.err.println(error.getPath() + ": " + error.getMessage());
 *     }
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Detailed validation error information - 详细的验证错误信息</li>
 *   <li>Multiple error collection and reporting - 多错误收集和报告</li>
 *   <li>Builder pattern for constructing complex validation exceptions - 构建复杂验证异常的构建器模式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public class JsonSchemaException extends OpenJsonProcessingException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Validation Error - Represents a Single Schema Validation Error
     * 验证错误 - 表示单个 Schema 验证错误
     */
    public record ValidationError(
            /**
             * JSON Pointer path to the error location
             * 错误位置的 JSON Pointer 路径
             */
            String path,

            /**
             * Schema keyword that caused the error (e.g., "type", "required")
             * 导致错误的 Schema 关键字（如 "type"、"required"）
             */
            String keyword,

            /**
             * Error message
             * 错误消息
             */
            String message,

            /**
             * Expected value from schema
             * Schema 中的预期值
             */
            Object expected,

            /**
             * Actual value found in JSON
             * JSON 中的实际值
             */
            Object actual
    ) {
        /**
         * Creates a validation error with path, keyword, and message.
         * 使用路径、关键字和消息创建验证错误。
         *
         * @param path    the JSON Pointer path - JSON Pointer 路径
         * @param keyword the schema keyword - Schema 关键字
         * @param message the error message - 错误消息
         */
        public ValidationError(String path, String keyword, String message) {
            this(path, keyword, message, null, null);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(path != null ? path : "/");
            sb.append(": ");
            sb.append(message);
            if (keyword != null) {
                sb.append(" [").append(keyword).append("]");
            }
            return sb.toString();
        }
    }

    /**
     * List of validation errors
     * 验证错误列表
     */
    private final List<ValidationError> errors;

    /**
     * The schema URI that was validated against
     * 验证所使用的 Schema URI
     */
    private final String schemaUri;

    /**
     * Constructs a new exception with a single error message.
     * 使用单个错误消息构造新异常。
     *
     * @param message the detail message - 详细消息
     */
    public JsonSchemaException(String message) {
        super(message, ErrorType.PARSE_ERROR);
        this.errors = List.of(new ValidationError("/", null, message));
        this.schemaUri = null;
    }

    /**
     * Constructs a new exception with validation errors.
     * 使用验证错误列表构造新异常。
     *
     * @param message the detail message - 详细消息
     * @param errors  the validation errors - 验证错误列表
     */
    public JsonSchemaException(String message, List<ValidationError> errors) {
        super(message, ErrorType.PARSE_ERROR);
        this.errors = errors != null ? List.copyOf(errors) : Collections.emptyList();
        this.schemaUri = null;
    }

    /**
     * Constructs a new exception with validation errors and schema URI.
     * 使用验证错误列表和 Schema URI 构造新异常。
     *
     * @param message   the detail message - 详细消息
     * @param errors    the validation errors - 验证错误列表
     * @param schemaUri the schema URI - Schema URI
     */
    public JsonSchemaException(String message, List<ValidationError> errors, String schemaUri) {
        super(message, ErrorType.PARSE_ERROR);
        this.errors = errors != null ? List.copyOf(errors) : Collections.emptyList();
        this.schemaUri = schemaUri;
    }

    /**
     * Returns the list of validation errors.
     * 返回验证错误列表。
     *
     * @return unmodifiable list of validation errors - 不可变的验证错误列表
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    /**
     * Returns the number of validation errors.
     * 返回验证错误数量。
     *
     * @return the error count - 错误数量
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Returns the schema URI that was validated against.
     * 返回验证所使用的 Schema URI。
     *
     * @return the schema URI, or null if not specified - Schema URI，未指定时返回 null
     */
    public String getSchemaUri() {
        return schemaUri;
    }

    /**
     * Returns whether there are any errors.
     * 返回是否存在错误。
     *
     * @return true if there are errors - 如果存在错误则返回 true
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns a formatted error report.
     * 返回格式化的错误报告。
     *
     * @return the error report - 错误报告
     */
    public String getErrorReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("JSON Schema validation failed");
        if (schemaUri != null) {
            sb.append(" against schema: ").append(schemaUri);
        }
        sb.append("\n");
        sb.append("Found ").append(errors.size()).append(" error(s):\n");
        for (int i = 0; i < errors.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(errors.get(i)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getMessage() {
        if (errors.isEmpty()) {
            return super.getMessage();
        }
        return super.getMessage() + " (" + errors.size() + " validation error" +
               (errors.size() > 1 ? "s" : "") + ")";
    }

    // ==================== Factory Methods ====================

    /**
     * Creates an exception for a type mismatch error.
     * 创建类型不匹配错误的异常。
     *
     * @param path     the JSON path - JSON 路径
     * @param expected the expected type - 预期类型
     * @param actual   the actual type - 实际类型
     * @return the exception - 异常
     */
    public static JsonSchemaException typeMismatch(String path, String expected, String actual) {
        String message = "Type mismatch at " + path + ": expected " + expected + ", got " + actual;
        List<ValidationError> errors = List.of(
                new ValidationError(path, "type", message, expected, actual)
        );
        return new JsonSchemaException(message, errors);
    }

    /**
     * Creates an exception for a required property error.
     * 创建必需属性错误的异常。
     *
     * @param path     the JSON path - JSON 路径
     * @param property the missing property name - 缺失的属性名
     * @return the exception - 异常
     */
    public static JsonSchemaException missingRequired(String path, String property) {
        String message = "Missing required property '" + property + "' at " + path;
        List<ValidationError> errors = List.of(
                new ValidationError(path, "required", message, property, null)
        );
        return new JsonSchemaException(message, errors);
    }

    /**
     * Creates an exception for a pattern mismatch error.
     * 创建模式不匹配错误的异常。
     *
     * @param path    the JSON path - JSON 路径
     * @param pattern the expected pattern - 预期模式
     * @param value   the actual value - 实际值
     * @return the exception - 异常
     */
    public static JsonSchemaException patternMismatch(String path, String pattern, String value) {
        String message = "Value '" + value + "' does not match pattern '" + pattern + "' at " + path;
        List<ValidationError> errors = List.of(
                new ValidationError(path, "pattern", message, pattern, value)
        );
        return new JsonSchemaException(message, errors);
    }

    /**
     * Creates an exception for a constraint violation error.
     * 创建约束违反错误的异常。
     *
     * @param path       the JSON path - JSON 路径
     * @param constraint the constraint keyword - 约束关键字
     * @param message    the error message - 错误消息
     * @return the exception - 异常
     */
    public static JsonSchemaException constraintViolation(String path, String constraint, String message) {
        List<ValidationError> errors = List.of(
                new ValidationError(path, constraint, message)
        );
        return new JsonSchemaException(message, errors);
    }

    /**
     * Builder for creating exceptions with multiple errors.
     * 用于创建包含多个错误的异常的构建器。
     */
    public static class Builder {
        private final List<ValidationError> errors = new ArrayList<>();
        private String schemaUri;

        /**
         * Sets the schema URI.
         * 设置 Schema URI。
         *
         * @param schemaUri the schema URI - Schema URI
         * @return this builder - 此构建器
         */
        public Builder schemaUri(String schemaUri) {
            this.schemaUri = schemaUri;
            return this;
        }

        /**
         * Adds a validation error.
         * 添加验证错误。
         *
         * @param error the validation error - 验证错误
         * @return this builder - 此构建器
         */
        public Builder addError(ValidationError error) {
            this.errors.add(error);
            return this;
        }

        /**
         * Adds a validation error with path, keyword, and message.
         * 使用路径、关键字和消息添加验证错误。
         *
         * @param path    the JSON Pointer path - JSON Pointer 路径
         * @param keyword the schema keyword - Schema 关键字
         * @param message the error message - 错误消息
         * @return this builder - 此构建器
         */
        public Builder addError(String path, String keyword, String message) {
            this.errors.add(new ValidationError(path, keyword, message));
            return this;
        }

        /**
         * Returns whether any errors have been added.
         * 返回是否添加了任何错误。
         *
         * @return true if errors exist - 如果存在错误则返回 true
         */
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        /**
         * Builds the exception.
         * 构建异常。
         *
         * @return the exception - 异常
         */
        public JsonSchemaException build() {
            String message = errors.isEmpty() ?
                    "JSON Schema validation failed" :
                    "JSON Schema validation failed: " + errors.getFirst().message();
            return new JsonSchemaException(message, errors, schemaUri);
        }

        /**
         * Builds and throws the exception if there are errors.
         * 如果存在错误则构建并抛出异常。
         *
         * @throws JsonSchemaException if there are validation errors - 如果存在验证错误
         */
        public void throwIfErrors() throws JsonSchemaException {
            if (!errors.isEmpty()) {
                throw build();
            }
        }
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return a new builder - 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }
}
