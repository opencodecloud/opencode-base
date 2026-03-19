package cloud.opencode.base.graph.exception;

/**
 * Graph Limit Exceeded Exception
 * 图限制超出异常
 *
 * <p>Exception thrown when graph operation exceeds configured limits.</p>
 * <p>当图操作超出配置限制时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries configured limit and actual value - 携带配置限制和实际值</li>
 *   <li>Extends {@link GraphException} with {@link GraphErrorCode#LIMIT_EXCEEDED} - 使用LIMIT_EXCEEDED错误码扩展GraphException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     SafeGraphOperations.safeAddVertex(graph, vertex);
 * } catch (GraphLimitExceededException e) {
 *     long limit = e.getLimit();
 *     long actual = e.getActual();
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
public class GraphLimitExceededException extends GraphException {

    private final long limit;
    private final long actual;

    /**
     * Create graph limit exceeded exception
     * 创建图限制超出异常
     *
     * @param message the error message | 错误消息
     */
    public GraphLimitExceededException(String message) {
        super(message, GraphErrorCode.LIMIT_EXCEEDED);
        this.limit = -1;
        this.actual = -1;
    }

    /**
     * Create graph limit exceeded exception with details
     * 使用详情创建图限制超出异常
     *
     * @param message the error message | 错误消息
     * @param limit the configured limit | 配置的限制
     * @param actual the actual value | 实际值
     */
    public GraphLimitExceededException(String message, long limit, long actual) {
        super(message + " (limit: " + limit + ", actual: " + actual + ")", GraphErrorCode.LIMIT_EXCEEDED);
        this.limit = limit;
        this.actual = actual;
    }

    /**
     * Get the configured limit
     * 获取配置的限制
     *
     * @return the limit | 限制
     */
    public long getLimit() {
        return limit;
    }

    /**
     * Get the actual value
     * 获取实际值
     *
     * @return the actual value | 实际值
     */
    public long getActual() {
        return actual;
    }
}
