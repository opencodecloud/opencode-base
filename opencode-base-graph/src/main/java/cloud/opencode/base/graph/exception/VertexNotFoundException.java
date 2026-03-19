package cloud.opencode.base.graph.exception;

/**
 * Vertex Not Found Exception
 * 顶点不存在异常
 *
 * <p>Exception thrown when a vertex is not found in the graph.</p>
 * <p>当顶点在图中未找到时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries the missing vertex information - 携带缺失的顶点信息</li>
 *   <li>Extends {@link GraphException} with {@link GraphErrorCode#VERTEX_NOT_FOUND} - 使用VERTEX_NOT_FOUND错误码扩展GraphException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     // operation requiring vertex existence
 * } catch (VertexNotFoundException e) {
 *     Object vertex = e.getVertex();
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
public class VertexNotFoundException extends GraphException {

    private final Object vertex;

    /**
     * Create vertex not found exception
     * 创建顶点不存在异常
     *
     * @param vertex the missing vertex | 缺失的顶点
     */
    public VertexNotFoundException(Object vertex) {
        super("Vertex not found: " + vertex, GraphErrorCode.VERTEX_NOT_FOUND);
        this.vertex = vertex;
    }

    /**
     * Get the missing vertex
     * 获取缺失的顶点
     *
     * @return the vertex | 顶点
     */
    public Object getVertex() {
        return vertex;
    }
}
