package cloud.opencode.base.classloader.dependency;

import java.util.List;
import java.util.Objects;

/**
 * Cyclic Dependency - Represents a dependency cycle among classes
 * 循环依赖 - 表示类之间的依赖循环
 *
 * <p>Holds an ordered list of class names forming a cycle,
 * where the last element depends on the first.</p>
 * <p>持有形成循环的类名有序列表，其中最后一个元素依赖于第一个。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record with defensive copy - 不可变记录，使用防御性复制</li>
 *   <li>Ordered cycle path - 有序的循环路径</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CyclicDependency cycle = new CyclicDependency(List.of("A", "B", "C"));
 * // A → B → C → A
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param cyclePath ordered list of class names forming a cycle (last depends on first)
 *                  | 形成循环的类名有序列表（最后一个依赖于第一个）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see DependencyGraph
 * @see ClassDependencyAnalyzer
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record CyclicDependency(List<String> cyclePath) {

    /**
     * Canonical constructor with null check and defensive copy.
     * 规范构造器，包含空值检查和防御性复制。
     *
     * @param cyclePath the cycle path | 循环路径
     */
    public CyclicDependency {
        Objects.requireNonNull(cyclePath, "cyclePath must not be null | cyclePath 不能为 null");
        cyclePath = List.copyOf(cyclePath);
    }
}
