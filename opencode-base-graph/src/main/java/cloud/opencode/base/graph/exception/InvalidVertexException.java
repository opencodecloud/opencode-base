package cloud.opencode.base.graph.exception;

/**
 * Invalid Vertex Exception
 * 无效顶点异常
 *
 * <p>Exception thrown when a vertex is invalid (e.g., null).</p>
 * <p>当顶点无效（如null）时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Signals invalid vertex parameters (typically null) - 表示无效的顶点参数（通常为null）</li>
 *   <li>Extends {@link GraphException} with {@link GraphErrorCode#INVALID_VERTEX} - 使用INVALID_VERTEX错误码扩展GraphException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     graph.addVertex(null);
 * } catch (InvalidVertexException e) {
 *     // handle invalid vertex
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
public class InvalidVertexException extends GraphException {

    /**
     * Create invalid vertex exception
     * 创建无效顶点异常
     *
     * @param message the error message | 错误消息
     */
    public InvalidVertexException(String message) {
        super(message, GraphErrorCode.INVALID_VERTEX);
    }
}
