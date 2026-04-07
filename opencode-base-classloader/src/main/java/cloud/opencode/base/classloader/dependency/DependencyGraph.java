package cloud.opencode.base.classloader.dependency;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dependency Graph - Immutable graph of class dependencies
 * 依赖图 - 不可变的类依赖关系图
 *
 * <p>Represents a directed graph where each node is a class name and
 * each edge represents a dependency from one class to another.</p>
 * <p>表示一个有向图，每个节点是一个类名，每条边表示从一个类到另一个类的依赖关系。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record with deep defensive copy - 不可变记录，使用深度防御性复制</li>
 *   <li>Forward and reverse dependency lookups - 正向和反向依赖查找</li>
 *   <li>All class names enumeration - 所有类名枚举</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Map<String, Set<String>> adj = Map.of("A", Set.of("B", "C"), "B", Set.of("C"));
 * DependencyGraph graph = new DependencyGraph(adj, 3, 3);
 * Set<String> deps = graph.dependenciesOf("A"); // [B, C]
 * Set<String> dependents = graph.dependentsOf("C"); // [A, B]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param adjacency  class name to set of classes it depends on | 类名到其依赖的类集合的映射
 * @param classCount total number of classes in the graph | 图中类的总数
 * @param edgeCount  total number of dependency edges | 依赖边的总数
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see CyclicDependency
 * @see ClassDependencyAnalyzer
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record DependencyGraph(Map<String, Set<String>> adjacency, int classCount, int edgeCount) {

    /**
     * Canonical constructor with null check and deep defensive copy.
     * 规范构造器，包含空值检查和深度防御性复制。
     *
     * @param adjacency  the adjacency map | 邻接映射
     * @param classCount number of classes | 类数量
     * @param edgeCount  number of edges | 边数量
     */
    public DependencyGraph {
        Objects.requireNonNull(adjacency, "adjacency must not be null | adjacency 不能为 null");
        Map<String, Set<String>> deepCopy = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : adjacency.entrySet()) {
            deepCopy.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }
        adjacency = Map.copyOf(deepCopy);
    }

    /**
     * Get the direct dependencies of a class.
     * 获取一个类的直接依赖。
     *
     * @param className the class name | 类名
     * @return set of class names this class depends on, or empty set if not found
     *         | 该类依赖的类名集合，若未找到则返回空集合
     * @throws NullPointerException if className is null | 如果类名为 null 则抛出空指针异常
     */
    public Set<String> dependenciesOf(String className) {
        Objects.requireNonNull(className, "className must not be null | className 不能为 null");
        return adjacency.getOrDefault(className, Set.of());
    }

    /**
     * Get the classes that depend on the given class (reverse lookup).
     * 获取依赖于给定类的类（反向查找）。
     *
     * @param className the class name | 类名
     * @return set of class names that depend on the given class
     *         | 依赖于给定类的类名集合
     * @throws NullPointerException if className is null | 如果类名为 null 则抛出空指针异常
     */
    public Set<String> dependentsOf(String className) {
        Objects.requireNonNull(className, "className must not be null | className 不能为 null");
        return adjacency.entrySet().stream()
                .filter(e -> e.getValue().contains(className))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Get all class names in this graph.
     * 获取此图中的所有类名。
     *
     * @return unmodifiable set of all class names | 所有类名的不可修改集合
     */
    public Set<String> classNames() {
        return adjacency.keySet();
    }
}
