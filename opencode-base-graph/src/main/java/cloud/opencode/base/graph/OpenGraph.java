package cloud.opencode.base.graph;

import cloud.opencode.base.graph.algorithm.*;
import cloud.opencode.base.graph.builder.GraphBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Open Graph
 * 图组件入口类
 *
 * <p>Main entry point for graph operations.</p>
 * <p>图操作的主要入口点。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Graph creation (directed/undirected) | 图创建（有向/无向）</li>
 *   <li>Graph traversal (BFS/DFS) | 图遍历（BFS/DFS）</li>
 *   <li>Shortest path (Dijkstra/A*) | 最短路径（Dijkstra/A*）</li>
 *   <li>Topological sort | 拓扑排序</li>
 *   <li>Connectivity analysis | 连通性分析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a directed graph
 * Graph<String> graph = OpenGraph.directed();
 * graph.addEdge("A", "B", 1.0);
 * graph.addEdge("B", "C", 2.0);
 *
 * // BFS traversal
 * List<String> bfsResult = OpenGraph.bfs(graph, "A");
 *
 * // Shortest path
 * List<String> path = OpenGraph.shortestPath(graph, "A", "C");
 *
 * // Topological sort
 * List<String> order = OpenGraph.topologicalSort(graph);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (delegates to null-safe implementations) - 空值安全: 是（委托给空值安全的实现）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class OpenGraph {

    private OpenGraph() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== Graph Creation | 图创建 ====================

    /**
     * Create a directed graph
     * 创建有向图
     *
     * @param <V> the vertex type | 顶点类型
     * @return a new directed graph | 新的有向图
     */
    public static <V> Graph<V> directed() {
        return new DirectedGraph<>();
    }

    /**
     * Create an undirected graph
     * 创建无向图
     *
     * @param <V> the vertex type | 顶点类型
     * @return a new undirected graph | 新的无向图
     */
    public static <V> Graph<V> undirected() {
        return new UndirectedGraph<>();
    }

    /**
     * Create a directed weighted graph
     * 创建有向加权图
     *
     * @param <V> the vertex type | 顶点类型
     * @return a new directed weighted graph | 新的有向加权图
     */
    public static <V> WeightedGraph<V> directedWeighted() {
        return WeightedGraph.directed();
    }

    /**
     * Create an undirected weighted graph
     * 创建无向加权图
     *
     * @param <V> the vertex type | 顶点类型
     * @return a new undirected weighted graph | 新的无向加权图
     */
    public static <V> WeightedGraph<V> undirectedWeighted() {
        return WeightedGraph.undirected();
    }

    /**
     * Create a graph builder for directed graph
     * 创建有向图构建器
     *
     * @param <V> the vertex type | 顶点类型
     * @return a new graph builder | 新的图构建器
     */
    public static <V> GraphBuilder<V> directedBuilder() {
        return GraphBuilder.directed();
    }

    /**
     * Create a graph builder for undirected graph
     * 创建无向图构建器
     *
     * @param <V> the vertex type | 顶点类型
     * @return a new graph builder | 新的图构建器
     */
    public static <V> GraphBuilder<V> undirectedBuilder() {
        return GraphBuilder.undirected();
    }

    // ==================== Graph Traversal | 图遍历 ====================

    /**
     * Breadth-first search
     * 广度优先搜索
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to traverse | 要遍历的图
     * @param start the starting vertex | 起始顶点
     * @return list of vertices in BFS order | BFS顺序的顶点列表
     */
    public static <V> List<V> bfs(Graph<V> graph, V start) {
        return GraphTraversalUtil.bfs(graph, start);
    }

    /**
     * Depth-first search
     * 深度优先搜索
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to traverse | 要遍历的图
     * @param start the starting vertex | 起始顶点
     * @return list of vertices in DFS order | DFS顺序的顶点列表
     */
    public static <V> List<V> dfs(Graph<V> graph, V start) {
        return GraphTraversalUtil.dfs(graph, start);
    }

    /**
     * Safe iterative depth-first search (avoids stack overflow)
     * 安全的迭代式深度优先搜索（避免栈溢出）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to traverse | 要遍历的图
     * @param start the starting vertex | 起始顶点
     * @return list of vertices in DFS order | DFS顺序的顶点列表
     */
    public static <V> List<V> dfsIterative(Graph<V> graph, V start) {
        return SafeGraphTraversalUtil.dfsIterative(graph, start);
    }

    // ==================== Shortest Path | 最短路径 ====================

    /**
     * Dijkstra's shortest path algorithm
     * Dijkstra最短路径算法
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @return map of vertex to shortest distance from source | 顶点到源的最短距离映射
     */
    public static <V> Map<V, Double> dijkstra(Graph<V> graph, V source) {
        return ShortestPathUtil.dijkstra(graph, source);
    }

    /**
     * Find shortest path between two vertices
     * 查找两个顶点之间的最短路径
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return list of vertices in the shortest path | 最短路径的顶点列表
     */
    public static <V> List<V> shortestPath(Graph<V> graph, V source, V target) {
        return ShortestPathUtil.shortestPath(graph, source, target);
    }

    /**
     * Find shortest path using A* algorithm
     * 使用A*算法查找最短路径
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @param heuristic the heuristic function | 启发式函数
     * @return list of vertices in the shortest path | 最短路径的顶点列表
     */
    public static <V> List<V> aStar(Graph<V> graph, V source, V target,
                                     BiFunction<V, V, Double> heuristic) {
        return AStarUtil.findPath(graph, source, target, heuristic);
    }

    /**
     * Find path using bidirectional BFS (efficient for large graphs)
     * 使用双向BFS查找路径（对大图高效）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return list of vertices in the path | 路径的顶点列表
     */
    public static <V> List<V> bidirectionalBfs(Graph<V> graph, V source, V target) {
        return BidirectionalBfsUtil.findPath(graph, source, target);
    }

    // ==================== Topological Sort | 拓扑排序 ====================

    /**
     * Topological sort using Kahn's algorithm
     * 使用Kahn算法进行拓扑排序
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @return list of vertices in topological order | 拓扑顺序的顶点列表
     */
    public static <V> List<V> topologicalSort(Graph<V> graph) {
        return TopologicalSortUtil.sort(graph);
    }

    /**
     * Check if topological sort is possible (graph is DAG)
     * 检查是否可以进行拓扑排序（图是DAG）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if topological sort is possible | 如果可以进行拓扑排序返回true
     */
    public static <V> boolean canTopologicalSort(Graph<V> graph) {
        return TopologicalSortUtil.canSort(graph);
    }

    // ==================== Cycle Detection | 环检测 ====================

    /**
     * Check if graph contains a cycle
     * 检查图是否包含环
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if graph contains a cycle | 如果图包含环返回true
     */
    public static <V> boolean hasCycle(Graph<V> graph) {
        return CycleDetectionUtil.hasCycle(graph);
    }

    /**
     * Find one cycle if exists
     * 如果存在则找到一个环
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return list of vertices forming a cycle | 形成环的顶点列表
     */
    public static <V> List<V> findCycle(Graph<V> graph) {
        return CycleDetectionUtil.findCycle(graph);
    }

    // ==================== Connectivity | 连通性 ====================

    /**
     * Find all connected components
     * 查找所有连通分量
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return list of connected components | 连通分量列表
     */
    public static <V> List<Set<V>> connectedComponents(Graph<V> graph) {
        return ConnectedComponentsUtil.find(graph);
    }

    /**
     * Check if two vertices are connected
     * 检查两个顶点是否连通
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param v1 first vertex | 第一个顶点
     * @param v2 second vertex | 第二个顶点
     * @return true if connected | 如果连通返回true
     */
    public static <V> boolean isConnected(Graph<V> graph, V v1, V v2) {
        return ConnectedComponentsUtil.isConnected(graph, v1, v2);
    }

    /**
     * Check if graph is fully connected
     * 检查图是否完全连通
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if fully connected | 如果完全连通返回true
     */
    public static <V> boolean isFullyConnected(Graph<V> graph) {
        return ConnectedComponentsUtil.isFullyConnected(graph);
    }

    /**
     * Get the number of connected components
     * 获取连通分量数量
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return number of connected components | 连通分量数量
     */
    public static <V> int connectedComponentCount(Graph<V> graph) {
        return ConnectedComponentsUtil.count(graph);
    }

    // ==================== Minimum Spanning Tree | 最小生成树 ====================

    /**
     * Find minimum spanning tree using Prim's algorithm
     * 使用Prim算法查找最小生成树
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the undirected graph | 无向图
     * @return set of edges in the MST | 最小生成树的边集合
     */
    public static <V> Set<cloud.opencode.base.graph.node.Edge<V>> prim(Graph<V> graph) {
        return MinimumSpanningTreeUtil.prim(graph);
    }

    /**
     * Find minimum spanning tree using Prim's algorithm starting from a vertex
     * 使用Prim算法从指定顶点开始查找最小生成树
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the undirected graph | 无向图
     * @param start the starting vertex | 起始顶点
     * @return set of edges in the MST | 最小生成树的边集合
     */
    public static <V> Set<cloud.opencode.base.graph.node.Edge<V>> prim(Graph<V> graph, V start) {
        return MinimumSpanningTreeUtil.prim(graph, start);
    }

    /**
     * Find minimum spanning tree using Kruskal's algorithm
     * 使用Kruskal算法查找最小生成树
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the undirected graph | 无向图
     * @return set of edges in the MST | 最小生成树的边集合
     */
    public static <V> Set<cloud.opencode.base.graph.node.Edge<V>> kruskal(Graph<V> graph) {
        return MinimumSpanningTreeUtil.kruskal(graph);
    }

    /**
     * Calculate the total weight of the minimum spanning tree
     * 计算最小生成树的总权重
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the undirected graph | 无向图
     * @return total weight of MST | 最小生成树的总权重
     */
    public static <V> double mstWeight(Graph<V> graph) {
        return MinimumSpanningTreeUtil.mstWeight(graph);
    }

    /**
     * Check if a graph has a spanning tree (is connected)
     * 检查图是否有生成树（是否连通）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if graph is connected | 如果图连通返回true
     */
    public static <V> boolean hasSpanningTree(Graph<V> graph) {
        return MinimumSpanningTreeUtil.hasSpanningTree(graph);
    }

    // ==================== Network Flow | 网络流 ====================

    /**
     * Calculate maximum flow using Ford-Fulkerson algorithm (Edmonds-Karp)
     * 使用Ford-Fulkerson算法（Edmonds-Karp）计算最大流
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph with edge weights as capacities | 边权重作为容量的有向图
     * @param source the source vertex | 源顶点
     * @param sink the sink vertex | 汇顶点
     * @return the maximum flow value | 最大流值
     */
    public static <V> double maxFlow(Graph<V> graph, V source, V sink) {
        return NetworkFlowUtil.maxFlow(graph, source, sink);
    }

    /**
     * Get flow on each edge
     * 获取每条边上的流量
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @param source the source vertex | 源顶点
     * @param sink the sink vertex | 汇顶点
     * @return map of edge to flow value | 边到流量值的映射
     */
    public static <V> Map<cloud.opencode.base.graph.node.Edge<V>, Double> getFlows(
            Graph<V> graph, V source, V sink) {
        return NetworkFlowUtil.getFlows(graph, source, sink);
    }

    /**
     * Find minimum cut edges
     * 查找最小割边
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @param source the source vertex | 源顶点
     * @param sink the sink vertex | 汇顶点
     * @return set of edges in the minimum cut | 最小割中的边集合
     */
    public static <V> Set<cloud.opencode.base.graph.node.Edge<V>> minCut(
            Graph<V> graph, V source, V sink) {
        return NetworkFlowUtil.minCut(graph, source, sink);
    }

    /**
     * Compute flow result with detailed information
     * 计算包含详细信息的流结果
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @param source the source vertex | 源顶点
     * @param sink the sink vertex | 汇顶点
     * @return the flow result with max flow, edge flows, and min-cut | 包含最大流、边流量和最小割的流结果
     */
    public static <V> NetworkFlowUtil.FlowResult<V> computeFlow(
            Graph<V> graph, V source, V sink) {
        return NetworkFlowUtil.computeFlow(graph, source, sink);
    }
}
