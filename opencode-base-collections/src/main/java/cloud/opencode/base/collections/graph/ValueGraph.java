package cloud.opencode.base.collections.graph;

import java.util.Optional;
import java.util.Set;

/**
 * ValueGraph - Value Graph Interface
 * ValueGraph - 值图接口
 *
 * <p>An interface for graph data structures where edges have associated values (weights).</p>
 * <p>边带有关联值（权重）的图数据结构接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Node and edge operations - 节点和边操作</li>
 *   <li>Edge value queries - 边值查询</li>
 *   <li>Directed/undirected support - 有向/无向支持</li>
 *   <li>Adjacency queries - 邻接查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
 * graph.addNode("A");
 * graph.addNode("B");
 * graph.putEdgeValue("A", "B", 3.5);
 *
 * Optional<Double> weight = graph.edgeValue("A", "B");  // Optional[3.5]
 * boolean connected = graph.hasEdgeConnecting("A", "B"); // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (interface, implementation-dependent) - 否（接口，取决于实现）</li>
 *   <li>Null-safe: No - 否</li>
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
public interface ValueGraph<N, V> {

    // ==================== 节点查询 | Node Queries ====================

    /**
     * Return the set of all nodes in this graph.
     * 返回此图中所有节点的集合。
     *
     * @return the set of nodes | 节点集合
     */
    Set<N> nodes();

    /**
     * Return the set of all edges in this graph.
     * 返回此图中所有边的集合。
     *
     * @return the set of edges | 边集合
     */
    Set<EndpointPair<N>> edges();

    /**
     * Return the value of the edge connecting {@code nodeU} to {@code nodeV}, if present.
     * 返回连接 {@code nodeU} 到 {@code nodeV} 的边的值（如果存在）。
     *
     * @param nodeU the source node | 源节点
     * @param nodeV the target node | 目标节点
     * @return the edge value, or empty if no edge exists | 边值，如果不存在则返回空
     * @throws IllegalArgumentException if either node is not in the graph | 如果节点不在图中则抛出异常
     */
    Optional<V> edgeValue(N nodeU, N nodeV);

    /**
     * Return the value of the edge connecting {@code nodeU} to {@code nodeV},
     * or {@code defaultValue} if no edge exists.
     * 返回连接 {@code nodeU} 到 {@code nodeV} 的边的值，如果不存在则返回默认值。
     *
     * @param nodeU        the source node | 源节点
     * @param nodeV        the target node | 目标节点
     * @param defaultValue the default value | 默认值
     * @return the edge value, or default if no edge exists | 边值或默认值
     * @throws IllegalArgumentException if either node is not in the graph | 如果节点不在图中则抛出异常
     */
    V edgeValueOrDefault(N nodeU, N nodeV, V defaultValue);

    /**
     * Check if there is an edge connecting {@code nodeU} to {@code nodeV}.
     * 检查是否存在从 {@code nodeU} 到 {@code nodeV} 的边。
     *
     * @param nodeU the source node | 源节点
     * @param nodeV the target node | 目标节点
     * @return true if edge exists | 如果边存在则返回 true
     */
    boolean hasEdgeConnecting(N nodeU, N nodeV);

    // ==================== 邻接查询 | Adjacency Queries ====================

    /**
     * Return all adjacent nodes (both successors and predecessors).
     * 返回所有邻接节点（后继和前驱）。
     *
     * @param node the node | 节点
     * @return the set of adjacent nodes | 邻接节点集合
     * @throws IllegalArgumentException if node is not in the graph | 如果节点不在图中则抛出异常
     */
    Set<N> adjacentNodes(N node);

    /**
     * Return the predecessors (incoming neighbors) of a node.
     * 返回节点的前驱（入边邻居）。
     *
     * @param node the node | 节点
     * @return the set of predecessors | 前驱集合
     * @throws IllegalArgumentException if node is not in the graph | 如果节点不在图中则抛出异常
     */
    Set<N> predecessors(N node);

    /**
     * Return the successors (outgoing neighbors) of a node.
     * 返回节点的后继（出边邻居）。
     *
     * @param node the node | 节点
     * @return the set of successors | 后继集合
     * @throws IllegalArgumentException if node is not in the graph | 如果节点不在图中则抛出异常
     */
    Set<N> successors(N node);

    /**
     * Return the degree of a node (number of edges incident to it).
     * 返回节点的度数（入射边的数量）。
     *
     * @param node the node | 节点
     * @return the degree | 度数
     * @throws IllegalArgumentException if node is not in the graph | 如果节点不在图中则抛出异常
     */
    int degree(N node);

    /**
     * Check if this graph is directed.
     * 检查此图是否有向。
     *
     * @return true if directed | 如果有向则返回 true
     */
    boolean isDirected();

    // ==================== 计数方法 | Count Methods ====================

    /**
     * Return the number of nodes.
     * 返回节点数量。
     *
     * @return the number of nodes | 节点数量
     */
    default int nodeCount() {
        return nodes().size();
    }

    /**
     * Return the number of edges.
     * 返回边数量。
     *
     * @return the number of edges | 边数量
     */
    default int edgeCount() {
        return edges().size();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Endpoint pair representing an edge between two nodes.
     * 表示两个节点之间边的端点对。
     *
     * @param source the source node | 源节点
     * @param target the target node | 目标节点
     * @param <N>    node type | 节点类型
     */
    record EndpointPair<N>(N source, N target) {
    }
}
