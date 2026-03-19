package cloud.opencode.base.functional.exception;

import java.io.Serial;

/**
 * OpenMatchException - Exception for pattern matching failures
 * OpenMatchException - 模式匹配失败的异常
 *
 * <p>Thrown when pattern matching fails to find a matching case for the input value.</p>
 * <p>当模式匹配无法为输入值找到匹配的分支时抛出。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Captures unmatched value - 捕获未匹配的值</li>
 *   <li>Supports sealed type exhaustiveness errors - 支持密封类型完备性错误</li>
 *   <li>Factory methods for common scenarios - 常见场景的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Thrown when no pattern matches
 * throw OpenMatchException.noMatch(value);
 *
 * // Thrown for non-exhaustive sealed type matching
 * throw OpenMatchException.exhaustive(value, Shape.class);
 *
 * // Access unmatched value
 * try {
 *     OpenMatch.of(value).orElseThrow();
 * } catch (OpenMatchException e) {
 *     Object unmatched = e.unmatchedValue();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Serializable: Yes - 可序列化: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public class OpenMatchException extends OpenFunctionalException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The value that was not matched
     * 未匹配的值
     */
    private final Object unmatchedValue;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Create exception with message
     * 创建带消息的异常
     *
     * @param message error message | 错误消息
     */
    public OpenMatchException(String message) {
        super("MATCH_001", message);
        this.unmatchedValue = null;
    }

    /**
     * Create exception with message and unmatched value
     * 创建带消息和未匹配值的异常
     *
     * @param message        error message | 错误消息
     * @param unmatchedValue the value that was not matched | 未匹配的值
     */
    public OpenMatchException(String message, Object unmatchedValue) {
        super("MATCH_001", message);
        this.unmatchedValue = unmatchedValue;
    }

    /**
     * Create exception with error code, message and unmatched value
     * 创建带错误码、消息和未匹配值的异常
     *
     * @param errorCode      error code | 错误码
     * @param message        error message | 错误消息
     * @param unmatchedValue the value that was not matched | 未匹配的值
     */
    public OpenMatchException(String errorCode, String message, Object unmatchedValue) {
        super(errorCode, message);
        this.unmatchedValue = unmatchedValue;
    }

    // ==================== Getters | 访问方法 ====================

    /**
     * Get the value that was not matched
     * 获取未匹配的值
     *
     * @return the unmatched value, may be null | 未匹配的值，可能为 null
     */
    public Object unmatchedValue() {
        return unmatchedValue;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create exception for no matching pattern
     * 为没有匹配模式创建异常
     *
     * @param value the value that was not matched | 未匹配的值
     * @return exception instance | 异常实例
     */
    public static OpenMatchException noMatch(Object value) {
        String valueStr = value != null ? value.toString() : "null";
        String typeStr = value != null ? value.getClass().getSimpleName() : "null";
        return new OpenMatchException(
            "MATCH_001",
            "No pattern matched for value: " + valueStr + " (type: " + typeStr + ")",
            value
        );
    }

    /**
     * Create exception for non-exhaustive sealed type matching
     * 为密封类型非完备匹配创建异常
     *
     * @param value      the value that was not matched | 未匹配的值
     * @param sealedType the sealed type being matched | 被匹配的密封类型
     * @return exception instance | 异常实例
     */
    public static OpenMatchException exhaustive(Object value, Class<?> sealedType) {
        String valueStr = value != null ? value.toString() : "null";
        return new OpenMatchException(
            "MATCH_002",
            "Non-exhaustive match for sealed type " + sealedType.getSimpleName() +
                ": missing case for " + valueStr,
            value
        );
    }

    /**
     * Create exception for guard condition failure
     * 为守卫条件失败创建异常
     *
     * @param value the value that failed the guard | 守卫失败的值
     * @return exception instance | 异常实例
     */
    public static OpenMatchException guardFailed(Object value) {
        String valueStr = value != null ? value.toString() : "null";
        return new OpenMatchException(
            "MATCH_003",
            "Guard condition failed for value: " + valueStr,
            value
        );
    }

    /**
     * Create exception for type mismatch
     * 为类型不匹配创建异常
     *
     * @param value        the actual value | 实际值
     * @param expectedType the expected type | 期望的类型
     * @return exception instance | 异常实例
     */
    public static OpenMatchException typeMismatch(Object value, Class<?> expectedType) {
        String actualType = value != null ? value.getClass().getSimpleName() : "null";
        return new OpenMatchException(
            "MATCH_004",
            "Type mismatch: expected " + expectedType.getSimpleName() +
                " but got " + actualType,
            value
        );
    }
}
