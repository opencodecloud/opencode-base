package cloud.opencode.base.yml.schema;

/**
 * ValidationError - Represents a single validation error in YAML schema validation
 * ValidationError - 表示 YAML 模式验证中的单个验证错误
 *
 * <p>An immutable record containing the path where the error occurred,
 * a human-readable message, and the type of error.</p>
 * <p>一个不可变记录，包含错误发生的路径、人类可读的消息和错误类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Path-aware error reporting - 路径感知的错误报告</li>
 *   <li>Typed error categorization - 类型化的错误分类</li>
 *   <li>Immutable record type - 不可变记录类型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ValidationError error = new ValidationError(
 *     "server.port", "Expected Integer but got String", ErrorType.TYPE_MISMATCH
 * );
 * System.out.println(error.path());    // "server.port"
 * System.out.println(error.message()); // "Expected Integer but got String"
 * System.out.println(error.type());    // TYPE_MISMATCH
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
public record ValidationError(String path, String message, ErrorType type) {

    /**
     * Error type enumeration for categorizing validation errors.
     * 用于分类验证错误的错误类型枚举。
     */
    public enum ErrorType {

        /**
         * A required key is missing.
         * 缺少必需的键。
         */
        MISSING_REQUIRED,

        /**
         * The value type does not match the expected type.
         * 值类型与预期类型不匹配。
         */
        TYPE_MISMATCH,

        /**
         * The value is outside the allowed range.
         * 值超出允许范围。
         */
        OUT_OF_RANGE,

        /**
         * The value does not match the required pattern.
         * 值不匹配要求的模式。
         */
        PATTERN_MISMATCH,

        /**
         * A custom validation rule failed.
         * 自定义验证规则失败。
         */
        CUSTOM_RULE_FAILED
    }

    @Override
    public String toString() {
        return "[" + type + "] " + path + ": " + message;
    }
}
