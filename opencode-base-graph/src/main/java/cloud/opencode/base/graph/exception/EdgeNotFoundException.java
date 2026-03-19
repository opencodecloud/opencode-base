package cloud.opencode.base.graph.exception;

/**
 * Edge Not Found Exception
 * 边不存在异常
 *
 * <p>Exception thrown when an edge is not found in the graph.</p>
 * <p>当边在图中未找到时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries source and target vertex information - 携带源顶点和目标顶点信息</li>
 *   <li>Extends {@link GraphException} with {@link GraphErrorCode#EDGE_NOT_FOUND} - 使用EDGE_NOT_FOUND错误码扩展GraphException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     // operation that requires edge existence
 * } catch (EdgeNotFoundException e) {
 *     Object from = e.getFrom();
 *     Object to = e.getTo();
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
public class EdgeNotFoundException extends GraphException {

    private final Object from;
    private final Object to;

    /**
     * Create edge not found exception
     * 创建边不存在异常
     *
     * @param from the source vertex | 源顶点
     * @param to the target vertex | 目标顶点
     */
    public EdgeNotFoundException(Object from, Object to) {
        super("Edge not found: " + from + " -> " + to, GraphErrorCode.EDGE_NOT_FOUND);
        this.from = from;
        this.to = to;
    }

    /**
     * Get the source vertex
     * 获取源顶点
     *
     * @return source vertex | 源顶点
     */
    public Object getFrom() {
        return from;
    }

    /**
     * Get the target vertex
     * 获取目标顶点
     *
     * @return target vertex | 目标顶点
     */
    public Object getTo() {
        return to;
    }
}
