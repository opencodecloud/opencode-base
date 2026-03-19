package cloud.opencode.base.geo.exception;

/**
 * Fence Check Exception
 * 围栏检查异常
 *
 * <p>Exception thrown when a geo fence check operation fails.</p>
 * <p>当地理围栏检查操作失败时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fence containment check failure reporting - 围栏包含检查失败报告</li>
 *   <li>Cause chain support - 异常链支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new FenceCheckException("Point containment check failed");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class FenceCheckException extends FenceException {

    /**
     * Create fence check exception with message
     * 使用消息创建围栏检查异常
     *
     * @param message the error message | 错误消息
     */
    public FenceCheckException(String message) {
        super(message);
    }

    /**
     * Create fence check exception with message and cause
     * 使用消息和原因创建围栏检查异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public FenceCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
