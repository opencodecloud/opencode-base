package cloud.opencode.base.collections.graph;

import java.util.Set;

/**
 * Graph - Graph Interface
 * Graph - 图接口
 *
 * <p>An interface for graph data structures supporting directed and undirected graphs.</p>
 * <p>支持有向图和无向图的图数据结构接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Node operations - 节点操作</li>
 *   <li>Edge operations - 边操作</li>
 *   <li>Directed/undirected support - 有向/无向支持</li>
 *   <li>Adjacency queries - 邻接查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> graph = MutableGraph.directed();
 * graph.addNode("A");
 * graph.addNode("B");
 * graph.addEdge("A", "B");
 *
 * Set<String> neighbors = graph.successors("A");  // [B]
 * boolean hasEdge = graph.hasEdge("A", "B");     // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (interface, implementation-dependent) - 否（接口，取决于实现）</li>
 *   <li>Null-safe: No - 否</li>
 * </ul>
 * @param <N> node type | 节点类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface Graph<N> {

    // ==================== 查询方法 | Query Methods ====================

    /**
     * Check if this graph is directed.
     * 检查此图是否有向。
     *
     * @return true if directed | 如果有向则返回 true
     */
    boolean isDirected();

    /**
     * Check if this graph allows self-loops.
     * 检查此图是否允许自环。
     *
     * @return true if allows self-loops | 如果允许自环则返回 true
     */
    boolean allowsSelfLoops();

    /**
     * Return the set of all nodes.
     * 返回所有节点的集合。
     *
     * @return the set of nodes | 节点集合
     */
    Set<N> nodes();

    /**
     * Return the set of all edges.
     * 返回所有边的集合。
     *
     * @return the set of edges | 边集合
     */
    Set<EndpointPair<N>> edges();

    // ==================== 节点操作 | Node Operations ====================

    /**
     * Check if the node exists.
     * 检查节点是否存在。
     *
     * @param node the node | 节点
     * @return true if exists | 如果存在则返回 true
     */
    boolean hasNode(N node);

    /**
     * Return the successors (outgoing neighbors) of a node.
     * 返回节点的后继（出边邻居）。
     *
     * @param node the node | 节点
     * @return the set of successors | 后继集合
     */
    Set<N> successors(N node);

    /**
     * Return the predecessors (incoming neighbors) of a node.
     * 返回节点的前驱（入边邻居）。
     *
     * @param node the node | 节点
     * @return the set of predecessors | 前驱集合
     */
    Set<N> predecessors(N node);

    /**
     * Return all adjacent nodes (both successors and predecessors).
     * 返回所有邻接节点（后继和前驱）。
     *
     * @param node the node | 节点
     * @return the set of adjacent nodes | 邻接节点集合
     */
    Set<N> adjacentNodes(N node);

    /**
     * Return the degree of a node (number of edges incident to it).
     * 返回节点的度数（入射边的数量）。
     *
     * @param node the node | 节点
     * @return the degree | 度数
     */
    int degree(N node);

    /**
     * Return the in-degree of a node (number of incoming edges).
     * 返回节点的入度（入边数量）。
     *
     * @param node the node | 节点
     * @return the in-degree | 入度
     */
    int inDegree(N node);

    /**
     * Return the out-degree of a node (number of outgoing edges).
     * 返回节点的出度（出边数量）。
     *
     * @param node the node | 节点
     * @return the out-degree | 出度
     */
    int outDegree(N node);

    // ==================== 边操作 | Edge Operations ====================

    /**
     * Check if the edge exists.
     * 检查边是否存在。
     *
     * @param nodeU the source node | 源节点
     * @param nodeV the target node | 目标节点
     * @return true if exists | 如果存在则返回 true
     */
    boolean hasEdge(N nodeU, N nodeV);

    /**
     * Return the edges incident to a node.
     * 返回入射到节点的边。
     *
     * @param node the node | 节点
     * @return the set of incident edges | 入射边集合
     */
    Set<EndpointPair<N>> incidentEdges(N node);

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
     * Endpoint pair representing an edge.
     * 表示边的端点对。
     *
     * @param <N> node type | 节点类型
     */
    interface EndpointPair<N> {
        /**
         * Return the source node (for directed graphs) or one endpoint (for undirected).
         * 返回源节点（有向图）或一个端点（无向图）。
         *
         * @return the source | 源
         */
        N source();

        /**
         * Return the target node (for directed graphs) or the other endpoint (for undirected).
         * 返回目标节点（有向图）或另一个端点（无向图）。
         *
         * @return the target | 目标
         */
        N target();

        /**
         * Check if this is an ordered (directed) pair.
         * 检查此是否为有序（有向）对。
         *
         * @return true if ordered | 如果有序则返回 true
         */
        boolean isOrdered();
    }
}
