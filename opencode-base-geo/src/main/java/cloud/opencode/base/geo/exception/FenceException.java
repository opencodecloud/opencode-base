package cloud.opencode.base.geo.exception;

/**
 * Fence Exception
 * 围栏异常
 *
 * <p>Parent exception class for all geo fence-related exceptions.</p>
 * <p>所有地理围栏相关异常的父类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base class for fence-related exceptions - 围栏相关异常的基类</li>
 *   <li>Automatic error code assignment (FENCE_CHECK_FAILED) - 自动分配错误码（FENCE_CHECK_FAILED）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new FenceException("Fence check failed");
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
public class FenceException extends GeoException {

    /**
     * Create fence exception with message
     * 使用消息创建围栏异常
     *
     * @param message the error message | 错误消息
     */
    public FenceException(String message) {
        super(message, GeoErrorCode.FENCE_CHECK_FAILED);
    }

    /**
     * Create fence exception with message and cause
     * 使用消息和原因创建围栏异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public FenceException(String message, Throwable cause) {
        super(message, cause, GeoErrorCode.FENCE_CHECK_FAILED);
    }
}
