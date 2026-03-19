package cloud.opencode.base.core.exception;

import java.io.Serial;

/**
 * Illegal Argument Exception - Parameter validation exception
 * 参数校验异常 - 方法参数验证异常
 *
 * <p>Thrown when method arguments do not meet expected conditions. Replaces {@link IllegalArgumentException}.</p>
 * <p>当方法参数不满足预期条件时抛出此异常。替代 {@link IllegalArgumentException}，提供统一的异常处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Not null check (notNull) - 非空检查</li>
 *   <li>Not empty check (notEmpty) - 非空字符串检查</li>
 *   <li>Not blank check (notBlank) - 非空白检查</li>
 *   <li>Range validation (positive, nonNegative, outOfRange) - 范围验证</li>
 *   <li>Index bounds check (indexOutOfBounds) - 索引边界检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * if (name == null || name.isEmpty()) {
 *     throw new OpenIllegalArgumentException("name must not be empty");
 * }
 *
 * // Static factory methods - 静态工厂方法
 * throw OpenIllegalArgumentException.notNull("userId");
 * throw OpenIllegalArgumentException.notEmpty("name");
 * throw OpenIllegalArgumentException.positive("count", -1);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Serializable: Yes - 可序列化: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class OpenIllegalArgumentException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Core";
    private static final String ERROR_CODE = "ILLEGAL_ARGUMENT";

    /**
     * Creates
     * 创建参数异常
     *
     * @param message the value | 异常消息
     */
    public OpenIllegalArgumentException(String message) {
        super(COMPONENT, ERROR_CODE, message);
    }

    /**
     * Creates
     * 创建参数异常（带原因）
     *
     * @param message the value | 异常消息
     * @param cause the value | 原始异常
     */
    public OpenIllegalArgumentException(String message, Throwable cause) {
        super(COMPONENT, ERROR_CODE, message, cause);
    }

    // ==================== 静态工厂方法 ====================

    /**
     * Creates
     * 创建"参数不能为 null"异常
     *
     * @param paramName the value | 参数名
     * @return the result | 异常实例
     */
    public static OpenIllegalArgumentException notNull(String paramName) {
        return new OpenIllegalArgumentException(paramName + " must not be null");
    }

    /**
     * Creates
     * 创建"参数不能为空"异常
     *
     * @param paramName the value | 参数名
     * @return the result | 异常实例
     */
    public static OpenIllegalArgumentException notEmpty(String paramName) {
        return new OpenIllegalArgumentException(paramName + " must not be empty");
    }

    /**
     * Creates
     * 创建"参数不能为空白"异常
     *
     * @param paramName the value | 参数名
     * @return the result | 异常实例
     */
    public static OpenIllegalArgumentException notBlank(String paramName) {
        return new OpenIllegalArgumentException(paramName + " must not be blank");
    }

    /**
     * Creates
     * 创建"参数必须为正数"异常
     *
     * @param paramName the value | 参数名
     * @param value the value | 实际值
     * @return the result | 异常实例
     */
    public static OpenIllegalArgumentException positive(String paramName, Number value) {
        return new OpenIllegalArgumentException(paramName + " must be positive, but was: " + value);
    }

    /**
     * Creates
     * 创建"参数必须为非负数"异常
     *
     * @param paramName the value | 参数名
     * @param value the value | 实际值
     * @return the result | 异常实例
     */
    public static OpenIllegalArgumentException nonNegative(String paramName, Number value) {
        return new OpenIllegalArgumentException(paramName + " must not be negative, but was: " + value);
    }

    /**
     * Creates
     * 创建"参数超出范围"异常
     *
     * @param paramName the value | 参数名
     * @param value the value | 实际值
     * @param min the value | 最小值
     * @param max the value | 最大值
     * @return the result | 异常实例
     */
    public static OpenIllegalArgumentException outOfRange(String paramName, Number value, Number min, Number max) {
        return new OpenIllegalArgumentException(
                paramName + " must be between " + min + " and " + max + ", but was: " + value);
    }

    /**
     * Creates
     * 创建"索引超出范围"异常
     *
     * @param index the value | 索引值
     * @param size the value | 集合大小
     * @return the result | 异常实例
     */
    public static OpenIllegalArgumentException indexOutOfBounds(int index, int size) {
        return new OpenIllegalArgumentException(
                "Index: " + index + ", Size: " + size);
    }
}
