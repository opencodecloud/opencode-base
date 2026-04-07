package cloud.opencode.base.core.exception;

import java.io.Serial;

/**
 * Illegal State Exception - Object state validation exception
 * 状态异常 - 对象状态验证异常
 *
 * <p>Thrown when object state does not meet operation preconditions.
 * Extends {@link IllegalStateException} so it is catchable by standard JDK catch clauses,
 * while also carrying component/errorCode metadata consistent with the OpenCode exception model.</p>
 * <p>当对象状态不满足操作的前置条件时抛出此异常。继承 {@link IllegalStateException}，
 * 既可被标准JDK catch子句捕获，又携带与OpenCode异常模型一致的组件/错误码元数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Not initialized check (notInitialized) - 未初始化检查</li>
 *   <li>Already closed check (alreadyClosed) - 已关闭检查</li>
 *   <li>Already exists check (alreadyExists) - 已存在检查</li>
 *   <li>Not found check (notFound) - 未找到检查</li>
 *   <li>Invalid state check (invalidState) - 无效状态检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * if (!initialized) {
 *     throw new OpenIllegalStateException("Component not initialized");
 * }
 *
 * // Static factory methods - 静态工厂方法
 * throw OpenIllegalStateException.notInitialized("CacheManager");
 * throw OpenIllegalStateException.alreadyClosed("Connection");
 * throw OpenIllegalStateException.notFound("User", userId);
 * }</pre>
 *
 * <p><strong>Important | 重要说明:</strong>
 * Note: This exception extends {@link IllegalStateException} (not {@link OpenException})
 * to maintain compatibility with standard JDK exception handling.
 * Use {@code catch(IllegalStateException e)} or catch this class directly.
 * {@code catch(OpenException e)} will NOT catch this exception.</p>
 * <p>注意：此异常继承自 {@link IllegalStateException}（而非 {@link OpenException}），
 * 以保持与标准 JDK 异常处理的兼容性。请使用 {@code catch(IllegalStateException e)} 或直接捕获此类。
 * {@code catch(OpenException e)} 无法捕获此异常。</p>
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
public class OpenIllegalStateException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Core";
    private static final String ERROR_CODE = "ILLEGAL_STATE";

    /**
     * Error code
     * 错误码
     */
    private final String errorCode;

    /**
     * Component name
     * 组件名称
     */
    private final String component;

    /**
     * Creates
     * 创建状态异常
     *
     * @param message the value | 异常消息
     */
    public OpenIllegalStateException(String message) {
        super(formatMessage(COMPONENT, ERROR_CODE, message));
        this.component = COMPONENT;
        this.errorCode = ERROR_CODE;
    }

    /**
     * Creates
     * 创建状态异常（带原因）
     *
     * @param message the value | 异常消息
     * @param cause the value | 原始异常
     */
    public OpenIllegalStateException(String message, Throwable cause) {
        super(formatMessage(COMPONENT, ERROR_CODE, message), cause);
        this.component = COMPONENT;
        this.errorCode = ERROR_CODE;
    }

    /**
     * Gets the error code
     * 获取错误码
     *
     * @return the error code | 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the component name
     * 获取组件名称
     *
     * @return the component name | 组件名称
     */
    public String getComponent() {
        return component;
    }

    private static String formatMessage(String component, String errorCode, String message) {
        if (component != null && errorCode != null) {
            return "[" + component + "] (" + errorCode + ") " + message;
        } else if (component != null) {
            return "[" + component + "] " + message;
        } else if (errorCode != null) {
            return "(" + errorCode + ") " + message;
        }
        return message;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * Creates
     * 创建"未初始化"异常
     *
     * @param componentName the value | 组件名
     * @return the result | 异常实例
     */
    public static OpenIllegalStateException notInitialized(String componentName) {
        return new OpenIllegalStateException(componentName + " has not been initialized");
    }

    /**
     * Creates
     * 创建"已关闭"异常
     *
     * @param resourceName the value | 资源名
     * @return the result | 异常实例
     */
    public static OpenIllegalStateException alreadyClosed(String resourceName) {
        return new OpenIllegalStateException(resourceName + " has already been closed");
    }

    /**
     * Creates
     * 创建"已存在"异常
     *
     * @param itemName the value | 项目名
     * @param key the key | 键
     * @return the result | 异常实例
     */
    public static OpenIllegalStateException alreadyExists(String itemName, Object key) {
        return new OpenIllegalStateException(itemName + " already exists: " + key);
    }

    /**
     * Creates
     * 创建"未找到"异常
     *
     * @param itemName the value | 项目名
     * @param key the key | 键
     * @return the result | 异常实例
     */
    public static OpenIllegalStateException notFound(String itemName, Object key) {
        return new OpenIllegalStateException(itemName + " not found: " + key);
    }

    /**
     * Creates
     * 创建"无效状态"异常
     *
     * @param expected the value | 期望状态
     * @param actual the value | 实际状态
     * @return the result | 异常实例
     */
    public static OpenIllegalStateException invalidState(String expected, String actual) {
        return new OpenIllegalStateException(
                "Expected state: " + expected + ", but was: " + actual);
    }
}
