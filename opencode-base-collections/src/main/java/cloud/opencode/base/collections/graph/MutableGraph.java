package cloud.opencode.base.collections.graph;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * MutableGraph - Mutable Graph Implementation
 * MutableGraph - 可变图实现
 *
 * <p>A mutable graph that supports adding and removing nodes and edges.</p>
 * <p>支持添加和移除节点和边的可变图。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Add/remove nodes - 添加/移除节点</li>
 *   <li>Add/remove edges - 添加/移除边</li>
 *   <li>Directed/undirected support - 有向/无向支持</li>
 *   <li>Self-loop configuration - 自环配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create directed graph - 创建有向图
 * MutableGraph<String> directed = MutableGraph.directed();
 * directed.addNode("A");
 * directed.addNode("B");
 * directed.putEdge("A", "B");
 *
 * // Create undirected graph - 创建无向图
 * MutableGraph<Integer> undirected = MutableGraph.undirected();
 * undirected.putEdge(1, 2);  // Nodes added automatically
 *
 * // Remove operations - 移除操作
 * directed.removeEdge("A", "B");
 * directed.removeNode("A");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>addNode: O(1) - addNode: O(1)</li>
 *   <li>addEdge: O(1) - addEdge: O(1)</li>
 *   <li>hasEdge: O(1) - hasEdge: O(1)</li>
 *   <li>removeNode: O(degree) - removeNode: O(度数)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
 * </ul>
 *
 * @param <N> node type | 节点类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class MutableGraph<N> implements Graph<N>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean directed;
    private final boolean allowsSelfLoops;
    private final Map<N, Set<N>> successors;
    private final Map<N, Set<N>> predecessors;

    // ==================== 构造方法 | Constructors ====================

    private MutableGraph(boolean directed, boolean allowsSelfLoops) {
        this.directed = directed;
        this.allowsSelfLoops = allowsSelfLoops;
        this.successors = new LinkedHashMap<>();
        this.predecessors = new LinkedHashMap<>();
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create a directed mutable graph.
     * 创建有向可变图。
     *
     * @param <N> node type | 节点类型
     * @return new directed graph | 新有向图
     */
    public static <N> MutableGraph<N> directed() {
        return new MutableGraph<>(true, false);
    }

    /**
     * Create an undirected mutable graph.
     * 创建无向可变图。
     *
     * @param <N> node type | 节点类型
     * @return new undirected graph | 新无向图
     */
    public static <N> MutableGraph<N> undirected() {
        return new MutableGraph<>(false, false);
    }

    /**
     * Create a directed mutable graph allowing self-loops.
     * 创建允许自环的有向可变图。
     *
     * @param <N> node type | 节点类型
     * @return new directed graph | 新有向图
     */
    public static <N> MutableGraph<N> directedAllowingSelfLoops() {
        return new MutableGraph<>(true, true);
    }

    /**
     * Create an undirected mutable graph allowing self-loops.
     * 创建允许自环的无向可变图。
     *
     * @param <N> node type | 节点类型
     * @return new undirected graph | 新无向图
     */
    public static <N> MutableGraph<N> undirectedAllowingSelfLoops() {
        return new MutableGraph<>(false, true);
    }

    /**
     * Create a graph with custom configuration.
     * 创建自定义配置的图。
     *
     * @param <N>             node type | 节点类型
     * @param directed        whether directed | 是否有向
     * @param allowsSelfLoops whether allows self-loops | 是否允许自环
     * @return new graph | 新图
     */
    public static <N> MutableGraph<N> create(boolean directed, boolean allowsSelfLoops) {
        return new MutableGraph<>(directed, allowsSelfLoops);
    }

    // ==================== 修改方法 | Modification Methods ====================

    /**
     * Add a node to the graph.
     * 添加节点到图。
     *
     * @param node the node to add | 要添加的节点
     * @return true if added (node was not present) | 如果添加成功则返回 true
     */
    public boolean addNode(N node) {
        Objects.requireNonNull(node, "Node cannot be null");
        if (successors.containsKey(node)) {
            return false;
        }
        successors.put(node, new LinkedHashSet<>());
        predecessors.put(node, new LinkedHashSet<>());
        return true;
    }

    /**
     * Add an edge to the graph, adding nodes if necessary.
     * 添加边到图，必要时添加节点。
     *
     * @param nodeU the source node | 源节点
     * @param nodeV the target node | 目标节点
     * @return true if edge was added | 如果边被添加则返回 true
     * @throws IllegalArgumentException if self-loop not allowed | 如果不允许自环则抛出异常
     */
    public boolean putEdge(N nodeU, N nodeV) {
        Objects.requireNonNull(nodeU, "Source node cannot be null");
        Objects.requireNonNull(nodeV, "Target node cannot be null");

        if (!allowsSelfLoops && nodeU.equals(nodeV)) {
            throw new IllegalArgumentException("Self-loops not allowed: " + nodeU);
        }

        addNode(nodeU);
        addNode(nodeV);

        boolean added = successors.get(nodeU).add(nodeV);
        predecessors.get(nodeV).add(nodeU);

        if (!directed) {
            successors.get(nodeV).add(nodeU);
            predecessors.get(nodeU).add(nodeV);
        }

        return added;
    }

    /**
     * Remove a node and all its edges from the graph.
     * 从图中移除节点及其所有边。
     *
     * @param node the node to remove | 要移除的节点
     * @return true if removed | 如果移除成功则返回 true
     */
    public boolean removeNode(N node) {
        if (!successors.containsKey(node)) {
            return false;
        }

        // Remove all edges from this node
        for (N successor : new ArrayList<>(successors.get(node))) {
            predecessors.get(successor).remove(node);
        }

        // Remove all edges to this node
        for (N predecessor : new ArrayList<>(predecessors.get(node))) {
            successors.get(predecessor).remove(node);
        }

        successors.remove(node);
        predecessors.remove(node);
        return true;
    }

    /**
     * Remove an edge from the graph.
     * 从图中移除边。
     *
     * @param nodeU the source node | 源节点
     * @param nodeV the target node | 目标节点
     * @return true if removed | 如果移除成功则返回 true
     */
    public boolean removeEdge(N nodeU, N nodeV) {
        if (!successors.containsKey(nodeU) || !successors.containsKey(nodeV)) {
            return false;
        }

        boolean removed = successors.get(nodeU).remove(nodeV);
        if (removed) {
            predecessors.get(nodeV).remove(nodeU);

            if (!directed) {
                successors.get(nodeV).remove(nodeU);
                predecessors.get(nodeU).remove(nodeV);
            }
        }
        return removed;
    }

    // ==================== Graph 实现 | Graph Implementation ====================

    @Override
    public boolean isDirected() {
        return directed;
    }

    @Override
    public boolean allowsSelfLoops() {
        return allowsSelfLoops;
    }

    @Override
    public Set<N> nodes() {
        return Collections.unmodifiableSet(successors.keySet());
    }

    @Override
    public Set<EndpointPair<N>> edges() {
        Set<EndpointPair<N>> result = new LinkedHashSet<>();
        for (Map.Entry<N, Set<N>> entry : successors.entrySet()) {
            N source = entry.getKey();
            for (N target : entry.getValue()) {
                // For undirected graphs, SimpleEndpointPair.equals() and hashCode()
                // treat (A,B) and (B,A) as equal, so LinkedHashSet deduplicates correctly.
                result.add(new SimpleEndpointPair<>(source, target, directed));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public boolean hasNode(N node) {
        return successors.containsKey(node);
    }

    @Override
    public Set<N> successors(N node) {
        Set<N> succ = successors.get(node);
        if (succ == null) {
            throw new IllegalArgumentException("Node not in graph: " + node);
        }
        return Collections.unmodifiableSet(succ);
    }

    @Override
    public Set<N> predecessors(N node) {
        Set<N> pred = predecessors.get(node);
        if (pred == null) {
            throw new IllegalArgumentException("Node not in graph: " + node);
        }
        return Collections.unmodifiableSet(pred);
    }

    @Override
    public Set<N> adjacentNodes(N node) {
        if (!successors.containsKey(node)) {
            throw new IllegalArgumentException("Node not in graph: " + node);
        }
        Set<N> adjacent = new LinkedHashSet<>();
        adjacent.addAll(successors.get(node));
        adjacent.addAll(predecessors.get(node));
        return Collections.unmodifiableSet(adjacent);
    }

    @Override
    public int degree(N node) {
        if (!successors.containsKey(node)) {
            throw new IllegalArgumentException("Node not in graph: " + node);
        }
        if (directed) {
            return inDegree(node) + outDegree(node);
        } else {
            int selfLoops = successors.get(node).contains(node) ? 1 : 0;
            return successors.get(node).size() + selfLoops;
        }
    }

    @Override
    public int inDegree(N node) {
        Set<N> pred = predecessors.get(node);
        if (pred == null) {
            throw new IllegalArgumentException("Node not in graph: " + node);
        }
        return pred.size();
    }

    @Override
    public int outDegree(N node) {
        Set<N> succ = successors.get(node);
        if (succ == null) {
            throw new IllegalArgumentException("Node not in graph: " + node);
        }
        return succ.size();
    }

    @Override
    public boolean hasEdge(N nodeU, N nodeV) {
        Set<N> succ = successors.get(nodeU);
        return succ != null && succ.contains(nodeV);
    }

    @Override
    public Set<EndpointPair<N>> incidentEdges(N node) {
        if (!successors.containsKey(node)) {
            throw new IllegalArgumentException("Node not in graph: " + node);
        }
        Set<EndpointPair<N>> result = new LinkedHashSet<>();
        for (N successor : successors.get(node)) {
            result.add(new SimpleEndpointPair<>(node, successor, directed));
        }
        if (directed) {
            for (N predecessor : predecessors.get(node)) {
                result.add(new SimpleEndpointPair<>(predecessor, node, true));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Graph<?> that)) return false;
        return directed == that.isDirected() &&
                allowsSelfLoops == that.allowsSelfLoops() &&
                nodes().equals(that.nodes()) &&
                edges().equals(that.edges());
    }

    @Override
    public int hashCode() {
        return Objects.hash(directed, allowsSelfLoops, nodes(), edges());
    }

    @Override
    public String toString() {
        return "MutableGraph{" +
                "directed=" + directed +
                ", nodes=" + nodes() +
                ", edges=" + edges() +
                '}';
    }

    // ==================== 内部类 | Internal Classes ====================

    private record SimpleEndpointPair<N>(N source, N target, boolean ordered) implements EndpointPair<N>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isOrdered() {
            return ordered;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EndpointPair<?> that)) return false;
            if (ordered != that.isOrdered()) return false;
            if (ordered) {
                return Objects.equals(source, that.source()) && Objects.equals(target, that.target());
            } else {
                return (Objects.equals(source, that.source()) && Objects.equals(target, that.target())) ||
                        (Objects.equals(source, that.target()) && Objects.equals(target, that.source()));
            }
        }

        @Override
        public int hashCode() {
            if (ordered) {
                return Objects.hash(source, target);
            } else {
                return source.hashCode() + target.hashCode();
            }
        }

        @Override
        public String toString() {
            return ordered ? "<" + source + " -> " + target + ">" : "[" + source + ", " + target + "]";
        }
    }
}
