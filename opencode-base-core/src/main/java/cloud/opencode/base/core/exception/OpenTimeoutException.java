package cloud.opencode.base.core.exception;

import java.io.Serial;
import java.time.Duration;

/**
 * Timeout Exception - Operation timeout exception
 * 超时异常 - 操作超时异常
 *
 * <p>Thrown when an operation exceeds the specified time limit.</p>
 * <p>当操作超过预定时间限制时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Duration support (getTimeout) - Duration 支持</li>
 *   <li>Static factory (of) - 静态工厂方法</li>
 *   <li>Auto duration formatting (ms/s/m) - 自动时长格式化</li>
 *   <li>Cause chain support - 原因链支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * if (elapsedTime > timeout) {
 *     throw new OpenTimeoutException("Operation timed out after " + timeout + "ms");
 * }
 *
 * // Static factory methods - 静态工厂方法
 * throw OpenTimeoutException.of("Database query", Duration.ofSeconds(30));
 * throw OpenTimeoutException.of("HTTP request", Duration.ofMinutes(1), cause);
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
public class OpenTimeoutException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Core";
    private static final String ERROR_CODE = "TIMEOUT";

    /**
     * Timeout duration
     * 超时时长
     */
    private final Duration timeout;

    /**
     * Creates
     * 创建超时异常
     *
     * @param message the value | 异常消息
     */
    public OpenTimeoutException(String message) {
        super(COMPONENT, ERROR_CODE, message);
        this.timeout = null;
    }

    /**
     * Creates
     * 创建超时异常（带原因）
     *
     * @param message the value | 异常消息
     * @param cause the value | 原始异常
     */
    public OpenTimeoutException(String message, Throwable cause) {
        super(COMPONENT, ERROR_CODE, message, cause);
        this.timeout = null;
    }

    /**
     * Creates
     * 创建超时异常（带超时时长）
     *
     * @param message the value | 异常消息
     * @param timeout the value | 超时时长
     */
    public OpenTimeoutException(String message, Duration timeout) {
        super(COMPONENT, ERROR_CODE, message);
        this.timeout = timeout;
    }

    /**
     * Creates
     * 创建超时异常（完整参数）
     *
     * @param message the value | 异常消息
     * @param timeout the value | 超时时长
     * @param cause the value | 原始异常
     */
    public OpenTimeoutException(String message, Duration timeout, Throwable cause) {
        super(COMPONENT, ERROR_CODE, message, cause);
        this.timeout = timeout;
    }

    /**
     * Gets
     * 获取超时时长
     *
     * @return the result | 超时时长，可能为 null
     */
    public Duration getTimeout() {
        return timeout;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * Creates
     * 创建超时异常
     *
     * @param operation the operation name | 操作名称
     * @param timeout the value | 超时时长
     * @return the result | 异常实例
     */
    public static OpenTimeoutException of(String operation, Duration timeout) {
        return new OpenTimeoutException(
                operation + " timed out after " + formatDuration(timeout), timeout);
    }

    /**
     * Creates
     * 创建超时异常（带原因）
     *
     * @param operation the operation name | 操作名称
     * @param timeout the value | 超时时长
     * @param cause the value | 原始异常
     * @return the result | 异常实例
     */
    public static OpenTimeoutException of(String operation, Duration timeout, Throwable cause) {
        return new OpenTimeoutException(
                operation + " timed out after " + formatDuration(timeout), timeout, cause);
    }

    /**
     * Formats
     * 格式化时长
     */
    private static String formatDuration(Duration duration) {
        if (duration == null) {
            return "unknown";
        }
        long millis = duration.toMillis();
        if (millis < 1000) {
            return millis + "ms";
        } else if (millis < 60000) {
            return String.format("%.1fs", millis / 1000.0);
        } else {
            return String.format("%.1fm", millis / 60000.0);
        }
    }
}
