package cloud.opencode.base.graph.exception;

/**
 * Invalid Edge Exception
 * 无效边异常
 *
 * <p>Exception thrown when an edge is invalid (e.g., invalid weight).</p>
 * <p>当边无效（如无效权重）时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Signals invalid edge parameters (null vertices, NaN/infinite weight) - 表示无效的边参数（null顶点、NaN/无穷权重）</li>
 *   <li>Extends {@link GraphException} with {@link GraphErrorCode#INVALID_EDGE} - 使用INVALID_EDGE错误码扩展GraphException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     GraphValidator.validateEdge(from, to, weight);
 * } catch (InvalidEdgeException e) {
 *     // handle invalid edge
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public class InvalidEdgeException extends GraphException {

    /**
     * Create invalid edge exception
     * 创建无效边异常
     *
     * @param message the error message | 错误消息
     */
    public InvalidEdgeException(String message) {
        super(message, GraphErrorCode.INVALID_EDGE);
    }
}
