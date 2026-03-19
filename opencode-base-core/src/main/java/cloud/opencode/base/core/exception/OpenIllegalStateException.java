package cloud.opencode.base.core.exception;

import java.io.Serial;

/**
 * Illegal State Exception - Object state validation exception
 * 状态异常 - 对象状态验证异常
 *
 * <p>Thrown when object state does not meet operation preconditions. Replaces {@link IllegalStateException}.</p>
 * <p>当对象状态不满足操作的前置条件时抛出此异常。替代 {@link IllegalStateException}，提供统一的异常处理。</p>
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
public class OpenIllegalStateException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Core";
    private static final String ERROR_CODE = "ILLEGAL_STATE";

    /**
     * Creates
     * 创建状态异常
     *
     * @param message the value | 异常消息
     */
    public OpenIllegalStateException(String message) {
        super(COMPONENT, ERROR_CODE, message);
    }

    /**
     * Creates
     * 创建状态异常（带原因）
     *
     * @param message the value | 异常消息
     * @param cause the value | 原始异常
     */
    public OpenIllegalStateException(String message, Throwable cause) {
        super(COMPONENT, ERROR_CODE, message, cause);
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
