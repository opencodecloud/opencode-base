package cloud.opencode.base.collections.graph;

import java.util.*;

/**
 * MutableValueGraph - Mutable Value Graph Interface and Implementation
 * MutableValueGraph - 可变值图接口与实现
 *
 * <p>A mutable graph where edges have associated values (weights).
 * Supports both directed and undirected graphs.</p>
 * <p>边带有关联值（权重）的可变图。支持有向图和无向图。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Add/remove nodes - 添加/移除节点</li>
 *   <li>Add/remove weighted edges - 添加/移除带权边</li>
 *   <li>Directed/undirected support - 有向/无向支持</li>
 *   <li>Edge value queries - 边值查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create directed value graph - 创建有向值图
 * MutableValueGraph<String, Double> directed = MutableValueGraph.directed();
 * directed.putEdgeValue("A", "B", 1.5);
 * directed.putEdgeValue("B", "C", 2.0);
 *
 * Optional<Double> weight = directed.edgeValue("A", "B");  // Optional[1.5]
 *
 * // Create undirected value graph - 创建无向值图
 * MutableValueGraph<String, Integer> undirected = MutableValueGraph.undirected();
 * undirected.putEdgeValue("X", "Y", 10);
 * // Both directions queryable - 双向可查
 * undirected.edgeValue("Y", "X");  // Optional[10]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>addNode: O(1) - addNode: O(1)</li>
 *   <li>putEdgeValue: O(1) - putEdgeValue: O(1)</li>
 *   <li>edgeValue: O(1) - edgeValue: O(1)</li>
 *   <li>removeNode: O(degree) - removeNode: O(度数)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No (nulls not allowed for nodes) - 空值安全: 否（节点不允许空值）</li>
 * </ul>
 *
 * @param <N> node type | 节点类型
 * @param <V> edge value type | 边值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public interface MutableValueGraph<N, V> extends ValueGraph<N, V> {

    // ==================== 修改方法 | Modification Methods ====================

    /**
     * Add a node to the graph.
     * 添加节点到图。
     *
     * @param node the node to add | 要添加的节点
     * @return true if added (node was not present) | 如果添加成功则返回 true
     * @throws NullPointerException if node is null | 如果节点为空则抛出异常
     */
    boolean addNode(N node);

    /**
     * Add an edge with a value between two nodes, adding nodes if necessary.
     * Returns the previous value associated with the edge, or {@code null} if no edge existed.
     * 添加两个节点之间带值的边，必要时添加节点。
     * 返回之前与该边关联的值，如果不存在则返回 {@code null}。
     *
     * @param nodeU the source node | 源节点
     * @param nodeV the target node | 目标节点
     * @param value the edge value | 边值
     * @return the previous edge value, or null | 之前的边值或 null
     * @throws NullPointerException if any node is null | 如果节点为空则抛出异常
     */
    V putEdgeValue(N nodeU, N nodeV, V value);

    /**
     * Remove the edge between two nodes.
     * Returns the value of the removed edge, or {@code null} if no edge existed.
     * 移除两个节点之间的边。
     * 返回被移除边的值，如果不存在则返回 {@code null}。
     *
     * @param nodeU the source node | 源节点
     * @param nodeV the target node | 目标节点
     * @return the removed edge value, or null | 被移除的边值或 null
     */
    V removeEdge(N nodeU, N nodeV);

    /**
     * Remove a node and all its edges from the graph.
     * 从图中移除节点及其所有边。
     *
     * @param node the node to remove | 要移除的节点
     * @return true if removed | 如果移除成功则返回 true
     */
    boolean removeNode(N node);

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create a directed mutable value graph.
     * 创建有向可变值图。
     *
     * @param <N> node type | 节点类型
     * @param <V> edge value type | 边值类型
     * @return new directed value graph | 新有向值图
     */
    static <N, V> MutableValueGraph<N, V> directed() {
        return new DefaultMutableValueGraph<>(true);
    }

    /**
     * Create an undirected mutable value graph.
     * 创建无向可变值图。
     *
     * @param <N> node type | 节点类型
     * @param <V> edge value type | 边值类型
     * @return new undirected value graph | 新无向值图
     */
    static <N, V> MutableValueGraph<N, V> undirected() {
        return new DefaultMutableValueGraph<>(false);
    }
}

/**
 * Default implementation of {@link MutableValueGraph} using adjacency maps.
 * 使用邻接表实现的 {@link MutableValueGraph} 默认实现。
 *
 * @param <N> node type | 节点类型
 * @param <V> edge value type | 边值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
final class DefaultMutableValueGraph<N, V> implements MutableValueGraph<N, V> {

    private final boolean directed;
    private final Map<N, Map<N, V>> adjacency;

    DefaultMutableValueGraph(boolean directed) {
        this.directed = directed;
        this.adjacency = new LinkedHashMap<>();
    }

    // ==================== 修改方法 | Modification Methods ====================

    @Override
    public boolean addNode(N node) {
        Objects.requireNonNull(node, "Node cannot be null");
        if (adjacency.containsKey(node)) {
            return false;
        }
        adjacency.put(node, new LinkedHashMap<>());
        return true;
    }

    @Override
    public V putEdgeValue(N nodeU, N nodeV, V value) {
        Objects.requireNonNull(nodeU, "Source node cannot be null");
        Objects.requireNonNull(nodeV, "Target node cannot be null");

        addNode(nodeU);
        addNode(nodeV);

        V previous = adjacency.get(nodeU).put(nodeV, value);

        if (!directed) {
            adjacency.get(nodeV).put(nodeU, value);
        }

        return previous;
    }

    @Override
    public V removeEdge(N nodeU, N nodeV) {
        Map<N, V> neighborsU = adjacency.get(nodeU);
        if (neighborsU == null) {
            return null;
        }

        boolean wasPresent = neighborsU.containsKey(nodeV);
        V removed = neighborsU.remove(nodeV);

        if (wasPresent && !directed) {
            Map<N, V> neighborsV = adjacency.get(nodeV);
            if (neighborsV != null) {
                neighborsV.remove(nodeU);
            }
        }

        return removed;
    }

    @Override
    public boolean removeNode(N node) {
        Map<N, V> neighbors = adjacency.remove(node);
        if (neighbors == null) {
            return false;
        }

        // Remove all outgoing edges from this node in the reverse direction
        if (directed) {
            // For directed: remove this node from other nodes' adjacency lists
            for (Map<N, V> otherNeighbors : adjacency.values()) {
                otherNeighbors.remove(node);
            }
        } else {
            // For undirected: remove the reverse edges
            for (N neighbor : neighbors.keySet()) {
                Map<N, V> neighborMap = adjacency.get(neighbor);
                if (neighborMap != null) {
                    neighborMap.remove(node);
                }
            }
        }

        return true;
    }

    // ==================== 查询方法 | Query Methods ====================

    @Override
    public Set<N> nodes() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    @Override
    public Set<EndpointPair<N>> edges() {
        Set<EndpointPair<N>> result = new LinkedHashSet<>();
        for (Map.Entry<N, Map<N, V>> entry : adjacency.entrySet()) {
            N source = entry.getKey();
            for (N target : entry.getValue().keySet()) {
                if (directed) {
                    result.add(new EndpointPair<>(source, target));
                } else {
                    // For undirected, normalize to avoid duplicates
                    // Use consistent ordering based on hashCode/comparison
                    EndpointPair<N> pair = new EndpointPair<>(source, target);
                    EndpointPair<N> reverse = new EndpointPair<>(target, source);
                    if (!result.contains(reverse)) {
                        result.add(pair);
                    }
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Optional<V> edgeValue(N nodeU, N nodeV) {
        requireNode(nodeU);
        requireNode(nodeV);
        Map<N, V> neighbors = adjacency.get(nodeU);
        if (neighbors == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(neighbors.get(nodeV));
    }

    @Override
    public V edgeValueOrDefault(N nodeU, N nodeV, V defaultValue) {
        return edgeValue(nodeU, nodeV).orElse(defaultValue);
    }

    @Override
    public boolean hasEdgeConnecting(N nodeU, N nodeV) {
        Map<N, V> neighbors = adjacency.get(nodeU);
        return neighbors != null && neighbors.containsKey(nodeV);
    }

    @Override
    public Set<N> adjacentNodes(N node) {
        requireNode(node);
        Set<N> adjacent = new LinkedHashSet<>(adjacency.get(node).keySet());
        if (directed) {
            // Also include predecessors
            for (Map.Entry<N, Map<N, V>> entry : adjacency.entrySet()) {
                if (entry.getValue().containsKey(node)) {
                    adjacent.add(entry.getKey());
                }
            }
        }
        return Collections.unmodifiableSet(adjacent);
    }

    @Override
    public Set<N> predecessors(N node) {
        requireNode(node);
        if (!directed) {
            // For undirected, predecessors == successors
            return successors(node);
        }
        Set<N> result = new LinkedHashSet<>();
        for (Map.Entry<N, Map<N, V>> entry : adjacency.entrySet()) {
            if (entry.getValue().containsKey(node)) {
                result.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Set<N> successors(N node) {
        requireNode(node);
        return Collections.unmodifiableSet(adjacency.get(node).keySet());
    }

    @Override
    public int degree(N node) {
        requireNode(node);
        if (directed) {
            return successors(node).size() + predecessors(node).size();
        } else {
            return adjacency.get(node).size();
        }
    }

    @Override
    public boolean isDirected() {
        return directed;
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValueGraph<?, ?> that)) return false;
        return directed == that.isDirected() &&
                nodes().equals(that.nodes()) &&
                edges().equals(that.edges());
    }

    @Override
    public int hashCode() {
        return Objects.hash(directed, nodes(), edges());
    }

    @Override
    public String toString() {
        return "MutableValueGraph{" +
                "directed=" + directed +
                ", nodes=" + nodes() +
                ", adjacency=" + adjacency +
                '}';
    }

    // ==================== 内部方法 | Internal Methods ====================

    private void requireNode(N node) {
        if (!adjacency.containsKey(node)) {
            throw new IllegalArgumentException("Node not in graph: " + node);
        }
    }
}
