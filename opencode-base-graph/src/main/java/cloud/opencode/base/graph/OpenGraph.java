package cloud.opencode.base.graph;

import cloud.opencode.base.graph.algorithm.*;
import cloud.opencode.base.graph.builder.GraphBuilder;

import cloud.opencode.base.graph.node.Edge;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Open Graph
 * 图组件入口类
 *
 * <p>Main entry point for graph operations.</p>
 * <p>图操作的主要入口点。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Graph creation (directed/undirected/immutable) | 图创建（有向/无向/不可变）</li>
 *   <li>Graph traversal (BFS/DFS) | 图遍历（BFS/DFS）</li>
 *   <li>Shortest path (Dijkstra, A-star, Bellman-Ford, Floyd-Warshall) | 最短路径（Dijkstra、A-star、Bellman-Ford、Floyd-Warshall）</li>
 *   <li>Topological sort &amp; DAG operations | 拓扑排序与DAG操作</li>
 *   <li>Connectivity &amp; Strongly connected components | 连通性与强连通分量</li>
 *   <li>Articulation points &amp; bridges | 关节点与桥</li>
 *   <li>Bipartite detection | 二部图检测</li>
 *   <li>Graph metrics &amp; statistics | 图度量与统计</li>
 *   <li>Graph diff, transform, snapshot | 图比较、转换、快照</li>
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

    // ==================== Bellman-Ford | Bellman-Ford算法 ====================

    /**
     * Bellman-Ford single-source shortest paths (supports negative weights)
     * Bellman-Ford单源最短路径（支持负权边）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @return map of vertex to shortest distance | 顶点到最短距离的映射
     */
    public static <V> Map<V, Double> bellmanFord(Graph<V> graph, V source) {
        return BellmanFordUtil.shortestPaths(graph, source);
    }

    /**
     * Check for negative weight cycle reachable from source
     * 检查从源顶点可达的负权环
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @return true if negative cycle exists | 如果存在负权环返回true
     */
    public static <V> boolean hasNegativeCycle(Graph<V> graph, V source) {
        return BellmanFordUtil.hasNegativeCycle(graph, source);
    }

    // ==================== Floyd-Warshall | Floyd-Warshall算法 ====================

    /**
     * Floyd-Warshall all-pairs shortest paths
     * Floyd-Warshall全源最短路径
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return all-pairs shortest path result | 全源最短路径结果
     */
    public static <V> FloydWarshallUtil.AllPairsResult<V> allPairsShortestPaths(Graph<V> graph) {
        return FloydWarshallUtil.compute(graph);
    }

    // ==================== Strongly Connected Components | 强连通分量 ====================

    /**
     * Find all strongly connected components (Tarjan's algorithm)
     * 查找所有强连通分量（Tarjan算法）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @return list of SCCs | 强连通分量列表
     */
    public static <V> List<Set<V>> stronglyConnectedComponents(Graph<V> graph) {
        return StronglyConnectedComponentsUtil.find(graph);
    }

    /**
     * Check if graph is strongly connected
     * 检查图是否强连通
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if strongly connected | 如果强连通返回true
     */
    public static <V> boolean isStronglyConnected(Graph<V> graph) {
        return StronglyConnectedComponentsUtil.isStronglyConnected(graph);
    }

    /**
     * Get condensation graph (DAG of SCCs)
     * 获取冷凝图（强连通分量的DAG）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @return condensation graph | 冷凝图
     */
    public static <V> Graph<Set<V>> condensation(Graph<V> graph) {
        return StronglyConnectedComponentsUtil.condensation(graph);
    }

    // ==================== Articulation Points & Bridges | 关节点与桥 ====================

    /**
     * Find all articulation points (cut vertices)
     * 查找所有关节点（割点）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return set of articulation points | 关节点集合
     */
    public static <V> Set<V> articulationPoints(Graph<V> graph) {
        return ArticulationPointUtil.findArticulationPoints(graph);
    }

    /**
     * Find all bridges (cut edges)
     * 查找所有桥（割边）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return set of bridge edges | 桥边集合
     */
    public static <V> Set<Edge<V>> bridges(Graph<V> graph) {
        return ArticulationPointUtil.findBridges(graph);
    }

    /**
     * Check if graph is biconnected
     * 检查图是否双连通
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if biconnected | 如果双连通返回true
     */
    public static <V> boolean isBiconnected(Graph<V> graph) {
        return ArticulationPointUtil.isBiconnected(graph);
    }

    // ==================== Bipartite | 二部图 ====================

    /**
     * Check if graph is bipartite
     * 检查图是否为二部图
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if bipartite | 如果是二部图返回true
     */
    public static <V> boolean isBipartite(Graph<V> graph) {
        return BipartiteUtil.isBipartite(graph);
    }

    /**
     * Compute bipartite partition or odd cycle witness
     * 计算二部图分区或奇环证据
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return bipartite result | 二部图结果
     */
    public static <V> BipartiteUtil.BipartiteResult<V> bipartitePartition(Graph<V> graph) {
        return BipartiteUtil.partition(graph);
    }

    // ==================== DAG Operations | DAG操作 ====================

    /**
     * Find longest path in DAG (critical path)
     * 查找DAG中的最长路径（关键路径）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the DAG | 有向无环图
     * @return list of vertices in the longest path | 最长路径的顶点列表
     */
    public static <V> List<V> longestPath(Graph<V> graph) {
        return DagUtil.longestPath(graph);
    }

    /**
     * Compute transitive reduction of DAG
     * 计算DAG的传递归约
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the DAG | 有向无环图
     * @return reduced graph | 归约后的图
     */
    public static <V> Graph<V> transitiveReduction(Graph<V> graph) {
        return DagUtil.transitiveReduction(graph);
    }

    /**
     * Compute transitive closure of DAG
     * 计算DAG的传递闭包
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the DAG | 有向无环图
     * @return closure graph | 闭包图
     */
    public static <V> Graph<V> transitiveClosure(Graph<V> graph) {
        return DagUtil.transitiveClosure(graph);
    }

    // ==================== Graph Metrics | 图度量 ====================

    /**
     * Get graph density
     * 获取图密度
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return density value between 0 and 1 | 密度值，0到1之间
     */
    public static <V> double density(Graph<V> graph) {
        return GraphMetrics.density(graph);
    }

    /**
     * Get graph diameter
     * 获取图直径
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return diameter | 直径
     */
    public static <V> int diameter(Graph<V> graph) {
        return GraphMetrics.diameter(graph);
    }

    /**
     * Get graph summary with all key metrics
     * 获取包含所有关键指标的图摘要
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return graph summary | 图摘要
     */
    public static <V> GraphMetrics.GraphSummary summary(Graph<V> graph) {
        return GraphMetrics.summary(graph);
    }

    // ==================== Graph Diff & Transform | 图比较与转换 ====================

    /**
     * Compare two graphs and get differences
     * 比较两个图并获取差异
     *
     * @param <V> the vertex type | 顶点类型
     * @param before the original graph | 原始图
     * @param after the modified graph | 修改后的图
     * @return diff result | 差异结果
     */
    public static <V> GraphDiff.DiffResult<V> diff(Graph<V> before, Graph<V> after) {
        return GraphDiff.compare(before, after);
    }

    /**
     * Transform graph vertices using mapping function
     * 使用映射函数转换图顶点
     *
     * @param <V> the source vertex type | 源顶点类型
     * @param <R> the target vertex type | 目标顶点类型
     * @param graph the graph | 图
     * @param mapper the mapping function | 映射函数
     * @return transformed graph | 转换后的图
     */
    public static <V, R> Graph<R> mapVertices(Graph<V> graph, Function<V, R> mapper) {
        return GraphTransform.mapVertices(graph, mapper);
    }

    /**
     * Reverse a directed graph
     * 反转有向图
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return reversed graph | 反转后的图
     */
    public static <V> Graph<V> reverse(Graph<V> graph) {
        return GraphTransform.reverse(graph);
    }

    /**
     * Filter vertices from a graph by predicate
     * 按谓词过滤图的顶点
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param predicate the vertex filter predicate | 顶点过滤谓词
     * @return a new graph containing only vertices matching the predicate | 仅包含匹配谓词的顶点的新图
     */
    public static <V> Graph<V> filterVertices(Graph<V> graph, Predicate<V> predicate) {
        return GraphTransform.filterVertices(graph, predicate);
    }

    /**
     * Filter edges from a graph by predicate
     * 按谓词过滤图的边
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param predicate the edge filter predicate | 边过滤谓词
     * @return a new graph containing only edges matching the predicate | 仅包含匹配谓词的边的新图
     */
    public static <V> Graph<V> filterEdges(Graph<V> graph, Predicate<Edge<V>> predicate) {
        return GraphTransform.filterEdges(graph, predicate);
    }

    /**
     * Create an immutable snapshot of a graph
     * 创建图的不可变快照
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return immutable graph snapshot | 不可变图快照
     */
    public static <V> Graph<V> snapshot(Graph<V> graph) {
        return ImmutableGraph.copyOf(graph);
    }
}
