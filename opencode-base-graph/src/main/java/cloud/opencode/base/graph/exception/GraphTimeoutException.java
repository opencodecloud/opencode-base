package cloud.opencode.base.graph.exception;

import java.time.Duration;

/**
 * Graph Timeout Exception
 * 图计算超时异常
 *
 * <p>Exception thrown when a graph computation times out.</p>
 * <p>当图计算超时时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries timeout duration information - 携带超时时长信息</li>
 *   <li>Extends {@link GraphException} with {@link GraphErrorCode#TIMEOUT} - 使用TIMEOUT错误码扩展GraphException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     SafeGraphOperations.safeShortestPath(graph, source, target);
 * } catch (GraphTimeoutException e) {
 *     Duration timeout = e.getTimeout();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes (timeout may be null) - 空值安全: 是（timeout可能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public class GraphTimeoutException extends GraphException {

    private final Duration timeout;

    /**
     * Create graph timeout exception
     * 创建图计算超时异常
     *
     * @param message the error message | 错误消息
     */
    public GraphTimeoutException(String message) {
        super(message, GraphErrorCode.TIMEOUT);
        this.timeout = null;
    }

    /**
     * Create graph timeout exception with timeout duration
     * 使用超时时长创建图计算超时异常
     *
     * @param message the error message | 错误消息
     * @param timeout the timeout duration | 超时时长
     */
    public GraphTimeoutException(String message, Duration timeout) {
        super(message + " (timeout: " + timeout + ")", GraphErrorCode.TIMEOUT);
        this.timeout = timeout;
    }

    /**
     * Get the timeout duration
     * 获取超时时长
     *
     * @return the timeout duration | 超时时长
     */
    public Duration getTimeout() {
        return timeout;
    }
}
