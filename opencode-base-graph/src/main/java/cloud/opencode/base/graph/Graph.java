package cloud.opencode.base.graph;

import cloud.opencode.base.graph.node.Edge;

import java.util.Set;

/**
 * Graph Interface
 * 图接口
 *
 * <p>Core interface for graph data structure operations.</p>
 * <p>图数据结构操作的核心接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Add/remove vertices and edges - 添加/删除顶点和边</li>
 *   <li>Query neighbors and degree - 查询邻居和度</li>
 *   <li>Support weighted and unweighted edges - 支持加权和无权边</li>
 *   <li>Implementations: {@link DirectedGraph}, {@link UndirectedGraph}, {@link WeightedGraph} - 实现类</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> graph = OpenGraph.directed();
 * graph.addVertex("A");
 * graph.addEdge("A", "B", 1.0);
 * Set<String> neighbors = graph.neighbors("A");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementations reject null vertices - 空值安全: 实现类拒绝null顶点</li>
 * </ul>
 *
 * @param <V> the vertex type | 顶点类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public interface Graph<V> {

    /**
     * Add a vertex to the graph
     * 添加顶点到图
     *
     * @param vertex the vertex to add | 要添加的顶点
     */
    void addVertex(V vertex);

    /**
     * Add an unweighted edge
     * 添加无权重边
     *
     * @param from the source vertex | 源顶点
     * @param to the target vertex | 目标顶点
     */
    void addEdge(V from, V to);

    /**
     * Add a weighted edge
     * 添加加权边
     *
     * @param from the source vertex | 源顶点
     * @param to the target vertex | 目标顶点
     * @param weight the edge weight | 边权重
     */
    void addEdge(V from, V to, double weight);

    /**
     * Remove a vertex and all its edges
     * 移除顶点及其所有边
     *
     * @param vertex the vertex to remove | 要移除的顶点
     */
    void removeVertex(V vertex);

    /**
     * Remove an edge
     * 移除边
     *
     * @param from the source vertex | 源顶点
     * @param to the target vertex | 目标顶点
     */
    void removeEdge(V from, V to);

    /**
     * Get all vertices
     * 获取所有顶点
     *
     * @return set of vertices | 顶点集合
     */
    Set<V> vertices();

    /**
     * Get all edges
     * 获取所有边
     *
     * @return set of edges | 边集合
     */
    Set<Edge<V>> edges();

    /**
     * Get neighboring vertices
     * 获取邻接顶点
     *
     * @param vertex the vertex | 顶点
     * @return set of neighboring vertices | 邻接顶点集合
     */
    Set<V> neighbors(V vertex);

    /**
     * Get outgoing edges from a vertex
     * 获取顶点的出边
     *
     * @param vertex the vertex | 顶点
     * @return set of outgoing edges | 出边集合
     */
    Set<Edge<V>> outEdges(V vertex);

    /**
     * Get incoming edges to a vertex
     * 获取顶点的入边
     *
     * @param vertex the vertex | 顶点
     * @return set of incoming edges | 入边集合
     */
    Set<Edge<V>> inEdges(V vertex);

    /**
     * Get the number of vertices
     * 获取顶点数量
     *
     * @return vertex count | 顶点数量
     */
    int vertexCount();

    /**
     * Get the number of edges
     * 获取边数量
     *
     * @return edge count | 边数量
     */
    int edgeCount();

    /**
     * Check if the graph contains a vertex
     * 检查图是否包含顶点
     *
     * @param vertex the vertex to check | 要检查的顶点
     * @return true if contains | 如果包含返回true
     */
    boolean containsVertex(V vertex);

    /**
     * Check if the graph contains an edge
     * 检查图是否包含边
     *
     * @param from the source vertex | 源顶点
     * @param to the target vertex | 目标顶点
     * @return true if contains | 如果包含返回true
     */
    boolean containsEdge(V from, V to);

    /**
     * Get the weight of an edge
     * 获取边的权重
     *
     * @param from the source vertex | 源顶点
     * @param to the target vertex | 目标顶点
     * @return the edge weight, or Double.MAX_VALUE if no edge exists | 边权重，如果边不存在则返回Double.MAX_VALUE
     */
    double getWeight(V from, V to);

    /**
     * Check if this is a directed graph
     * 检查是否为有向图
     *
     * @return true if directed | 如果是有向图返回true
     */
    boolean isDirected();

    /**
     * Get the out-degree of a vertex
     * 获取顶点的出度
     *
     * @param vertex the vertex | 顶点
     * @return the out-degree | 出度
     */
    default int outDegree(V vertex) {
        return outEdges(vertex).size();
    }

    /**
     * Get the in-degree of a vertex
     * 获取顶点的入度
     *
     * @param vertex the vertex | 顶点
     * @return the in-degree | 入度
     */
    default int inDegree(V vertex) {
        return inEdges(vertex).size();
    }

    /**
     * Check if the graph is empty
     * 检查图是否为空
     *
     * @return true if empty | 如果为空返回true
     */
    default boolean isEmpty() {
        return vertexCount() == 0;
    }

    /**
     * Clear the graph
     * 清空图
     */
    void clear();
}
