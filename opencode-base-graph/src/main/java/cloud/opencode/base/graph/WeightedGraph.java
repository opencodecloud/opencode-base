package cloud.opencode.base.graph;

import cloud.opencode.base.graph.node.Edge;

import java.util.Set;

/**
 * Weighted Graph
 * 加权图
 *
 * <p>Interface for weighted graph operations with additional weight-related methods.</p>
 * <p>加权图操作接口，包含额外的权重相关方法。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>All Graph operations | 所有Graph操作</li>
 *   <li>Weight modification | 权重修改</li>
 *   <li>Total weight calculation | 总权重计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * WeightedGraph<String> graph = WeightedGraph.directed();
 * graph.addEdge("A", "B", 5.0);
 * graph.setWeight("A", "B", 10.0);
 * double total = graph.totalWeight();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes (rejects null vertices) - 空值安全: 是（拒绝null顶点）</li>
 * </ul>
 *
 * @param <V> the vertex type | 顶点类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public interface WeightedGraph<V> extends Graph<V> {

    /**
     * Set the weight of an edge
     * 设置边的权重
     *
     * @param from the source vertex | 源顶点
     * @param to the target vertex | 目标顶点
     * @param weight the new weight | 新权重
     * @return true if edge exists and weight was updated | 如果边存在且权重已更新返回true
     */
    boolean setWeight(V from, V to, double weight);

    /**
     * Get the total weight of all edges
     * 获取所有边的总权重
     *
     * @return total weight | 总权重
     */
    default double totalWeight() {
        return edges().stream()
            .mapToDouble(Edge::weight)
            .sum();
    }

    /**
     * Get the minimum edge weight
     * 获取最小边权重
     *
     * @return minimum weight, or Double.MAX_VALUE if no edges | 最小权重，如果无边则返回Double.MAX_VALUE
     */
    default double minWeight() {
        return edges().stream()
            .mapToDouble(Edge::weight)
            .min()
            .orElse(Double.MAX_VALUE);
    }

    /**
     * Get the maximum edge weight
     * 获取最大边权重
     *
     * @return maximum weight, or Double.MIN_VALUE if no edges | 最大权重，如果无边则返回Double.MIN_VALUE
     */
    default double maxWeight() {
        return edges().stream()
            .mapToDouble(Edge::weight)
            .max()
            .orElse(Double.MIN_VALUE);
    }

    /**
     * Get edges with weight in range
     * 获取权重在范围内的边
     *
     * @param minWeight minimum weight (inclusive) | 最小权重（包含）
     * @param maxWeight maximum weight (inclusive) | 最大权重（包含）
     * @return set of edges in range | 范围内的边集合
     */
    default Set<Edge<V>> edgesInWeightRange(double minWeight, double maxWeight) {
        return edges().stream()
            .filter(e -> e.weight() >= minWeight && e.weight() <= maxWeight)
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Create a directed weighted graph
     * 创建有向加权图
     *
     * @param <V> the vertex type | 顶点类型
     * @return a new directed weighted graph | 新的有向加权图
     */
    static <V> WeightedGraph<V> directed() {
        return new DirectedWeightedGraph<>();
    }

    /**
     * Create an undirected weighted graph
     * 创建无向加权图
     *
     * @param <V> the vertex type | 顶点类型
     * @return a new undirected weighted graph | 新的无向加权图
     */
    static <V> WeightedGraph<V> undirected() {
        return new UndirectedWeightedGraph<>();
    }
}

/**
 * Directed Weighted Graph implementation
 * 有向加权图实现
 */
class DirectedWeightedGraph<V> extends DirectedGraph<V> implements WeightedGraph<V> {

    @Override
    public boolean setWeight(V from, V to, double weight) {
        if (!containsEdge(from, to)) {
            return false;
        }
        removeEdge(from, to);
        addEdge(from, to, weight);
        return true;
    }
}

/**
 * Undirected Weighted Graph implementation
 * 无向加权图实现
 */
class UndirectedWeightedGraph<V> extends UndirectedGraph<V> implements WeightedGraph<V> {

    @Override
    public boolean setWeight(V from, V to, double weight) {
        if (!containsEdge(from, to)) {
            return false;
        }
        removeEdge(from, to);
        addEdge(from, to, weight);
        return true;
    }

    @Override
    public double totalWeight() {
        // UndirectedGraph.edges() already deduplicates edges, no division needed
        return edges().stream()
            .mapToDouble(Edge::weight)
            .sum();
    }
}
