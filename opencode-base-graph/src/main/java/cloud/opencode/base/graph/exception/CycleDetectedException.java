package cloud.opencode.base.graph.exception;

import java.util.List;

/**
 * Cycle Detected Exception
 * 检测到环异常
 *
 * <p>Exception thrown when a cycle is detected in a graph where cycles are not allowed.</p>
 * <p>当在不允许环的图中检测到环时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries the detected cycle vertices - 携带检测到的环顶点</li>
 *   <li>Extends {@link GraphException} with {@link GraphErrorCode#CYCLE_DETECTED} - 使用CYCLE_DETECTED错误码扩展GraphException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     List<String> order = TopologicalSortUtil.sort(graph);
 * } catch (CycleDetectedException e) {
 *     List<?> cycle = e.getCycle();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes (cycle may be null if not provided) - 空值安全: 是（未提供时cycle可能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public class CycleDetectedException extends GraphException {

    private final List<?> cycle;

    /**
     * Create cycle detected exception
     * 创建检测到环异常
     *
     * @param cycle the detected cycle | 检测到的环
     */
    public CycleDetectedException(List<?> cycle) {
        super("Cycle detected in graph: " + cycle, GraphErrorCode.CYCLE_DETECTED);
        this.cycle = cycle;
    }

    /**
     * Create cycle detected exception with message
     * 使用消息创建检测到环异常
     *
     * @param message the error message | 错误消息
     */
    public CycleDetectedException(String message) {
        super(message, GraphErrorCode.CYCLE_DETECTED);
        this.cycle = null;
    }

    /**
     * Get the detected cycle
     * 获取检测到的环
     *
     * @return the cycle vertices | 环的顶点
     */
    public List<?> getCycle() {
        return cycle;
    }
}
