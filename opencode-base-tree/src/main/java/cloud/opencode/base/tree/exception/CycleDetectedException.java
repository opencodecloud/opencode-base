package cloud.opencode.base.tree.exception;

import java.util.List;

/**
 * Cycle Detected Exception
 * 循环检测异常
 *
 * <p>Exception thrown when a cycle is detected in tree.</p>
 * <p>在树中检测到循环时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cycle detection exception with path - 带路径的循环检测异常</li>
 *   <li>Immutable cycle path storage - 不可变循环路径存储</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     CycleDetector.checkNoCycle(roots);
 * } catch (CycleDetectedException e) {
 *     List<?> cyclePath = e.getCyclePath();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 是（不可变）</li>
 *   <li>Null-safe: Yes (null path defaults to empty) - 是（null路径默认为空）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class CycleDetectedException extends TreeException {

    private final List<?> cyclePath;

    public CycleDetectedException(String message) {
        super(TreeErrorCode.CYCLE_DETECTED, message);
        this.cyclePath = List.of();
    }

    public CycleDetectedException(List<?> cyclePath) {
        super(TreeErrorCode.CYCLE_DETECTED, "Cycle detected: " + cyclePath);
        this.cyclePath = List.copyOf(cyclePath);
    }

    public CycleDetectedException(String message, List<?> cyclePath) {
        super(TreeErrorCode.CYCLE_DETECTED, message);
        this.cyclePath = cyclePath != null ? List.copyOf(cyclePath) : List.of();
    }

    /**
     * Get the cycle path
     * 获取循环路径
     *
     * @return the cycle path | 循环路径
     */
    public List<?> getCyclePath() {
        return cyclePath;
    }
}
