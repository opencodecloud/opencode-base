package cloud.opencode.base.graph.exception;

/**
 * No Path Exception
 * 无路径异常
 *
 * <p>Exception thrown when no path exists between two vertices.</p>
 * <p>当两个顶点之间不存在路径时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries source and target vertex information - 携带源顶点和目标顶点信息</li>
 *   <li>Extends {@link GraphException} with {@link GraphErrorCode#NO_PATH} - 使用NO_PATH错误码扩展GraphException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     // operation requiring path existence
 * } catch (NoPathException e) {
 *     Object source = e.getSource();
 *     Object target = e.getTarget();
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
public class NoPathException extends GraphException {

    private final Object source;
    private final Object target;

    /**
     * Create no path exception
     * 创建无路径异常
     *
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     */
    public NoPathException(Object source, Object target) {
        super("No path exists from " + source + " to " + target, GraphErrorCode.NO_PATH);
        this.source = source;
        this.target = target;
    }

    /**
     * Get the source vertex
     * 获取源顶点
     *
     * @return source vertex | 源顶点
     */
    public Object getSource() {
        return source;
    }

    /**
     * Get the target vertex
     * 获取目标顶点
     *
     * @return target vertex | 目标顶点
     */
    public Object getTarget() {
        return target;
    }
}
