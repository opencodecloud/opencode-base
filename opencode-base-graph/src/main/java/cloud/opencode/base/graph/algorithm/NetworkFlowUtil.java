package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Network Flow Util
 * 网络流工具类
 *
 * <p>Utility class for network flow algorithms.</p>
 * <p>网络流算法的工具类。</p>
 *
 * <p><strong>Algorithms | 算法:</strong></p>
 * <ul>
 *   <li>Ford-Fulkerson (with BFS/Edmonds-Karp) - O(VE²) | Ford-Fulkerson（使用BFS/Edmonds-Karp）</li>
 *   <li>Ford-Fulkerson (with DFS) - O(EF) where F is max flow | Ford-Fulkerson（使用DFS）</li>
 * </ul>
 *
 * <p><strong>Concepts | 概念:</strong></p>
 * <ul>
 *   <li>Residual Graph - Graph representing remaining capacity | 残余图 - 表示剩余容量的图</li>
 *   <li>Augmenting Path - Path from source to sink with available capacity | 增广路径 - 从源到汇的有可用容量的路径</li>
 *   <li>Min-Cut - Minimum capacity edges that disconnect source from sink | 最小割 - 将源和汇断开的最小容量边</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Maximum flow computation (Edmonds-Karp) - 最大流计算（Edmonds-Karp）</li>
 *   <li>Per-edge flow extraction - 逐边流量提取</li>
 *   <li>Minimum cut identification - 最小割识别</li>
 *   <li>Residual graph construction - 残余图构建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Calculate maximum flow
 * double maxFlow = NetworkFlowUtil.maxFlow(graph, "source", "sink");
 *
 * // Get flow on each edge
 * Map<Edge<String>, Double> flows = NetworkFlowUtil.getFlows(graph, "source", "sink");
 *
 * // Find minimum cut
 * Set<Edge<String>> minCut = NetworkFlowUtil.minCut(graph, "source", "sink");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns zero/empty for null inputs) - 空值安全: 是（null输入返回零/空）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(V * E^2) for Edmonds-Karp - 时间复杂度: O(V * E^2)（Edmonds-Karp算法）</li>
 *   <li>Space complexity: O(V + E) - 空间复杂度: O(V + E)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class NetworkFlowUtil {

    private NetworkFlowUtil() {
        // Utility class
    }

    // ==================== Ford-Fulkerson (Edmonds-Karp) | Ford-Fulkerson (Edmonds-Karp) ====================

    /**
     * Calculate maximum flow using Ford-Fulkerson algorithm with BFS (Edmonds-Karp)
     * 使用BFS的Ford-Fulkerson算法（Edmonds-Karp）计算最大流
     *
     * <p>Uses BFS to find augmenting paths, which guarantees polynomial time complexity.</p>
     * <p>使用BFS查找增广路径，保证多项式时间复杂度。</p>
     *
     * <p><strong>Time Complexity | 时间复杂度:</strong> O(VE²)</p>
     * <p><strong>Space Complexity | 空间复杂度:</strong> O(V+E)</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph with edge weights as capacities | 边权重作为容量的有向图
     * @param source the source vertex | 源顶点
     * @param sink the sink vertex | 汇顶点
     * @return the maximum flow value | 最大流值
     */
    public static <V> double maxFlow(Graph<V> graph, V source, V sink) {
        if (graph == null || source == null || sink == null) {
            return 0.0;
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(sink)) {
            return 0.0;
        }
        if (source.equals(sink)) {
            return 0.0;
        }

        // Build residual graph
        ResidualGraph<V> residual = new ResidualGraph<>(graph);

        double totalFlow = 0.0;

        // Find augmenting paths using BFS
        while (true) {
            // BFS to find shortest augmenting path
            Map<V, V> parent = new HashMap<>();
            Map<V, Double> pathCapacity = new HashMap<>();

            double bottleneck = bfsAugmentingPath(residual, source, sink, parent, pathCapacity);

            if (bottleneck == 0) {
                break; // No augmenting path found
            }

            // Update residual graph along the path
            V current = sink;
            while (!current.equals(source)) {
                V prev = parent.get(current);
                residual.addFlow(prev, current, bottleneck);
                current = prev;
            }

            totalFlow += bottleneck;
        }

        return totalFlow;
    }

    /**
     * Calculate maximum flow using Ford-Fulkerson with DFS
     * 使用DFS的Ford-Fulkerson算法计算最大流
     *
     * <p>Uses DFS to find augmenting paths. Faster for some graphs but not polynomial.</p>
     * <p>使用DFS查找增广路径。对于某些图更快但非多项式。</p>
     *
     * <p><strong>Time Complexity | 时间复杂度:</strong> O(EF) where F is max flow value</p>
     * <p><strong>Space Complexity | 空间复杂度:</strong> O(V+E)</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @param source the source vertex | 源顶点
     * @param sink the sink vertex | 汇顶点
     * @return the maximum flow value | 最大流值
     */
    public static <V> double maxFlowDfs(Graph<V> graph, V source, V sink) {
        if (graph == null || source == null || sink == null) {
            return 0.0;
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(sink)) {
            return 0.0;
        }
        if (source.equals(sink)) {
            return 0.0;
        }

        ResidualGraph<V> residual = new ResidualGraph<>(graph);
        double totalFlow = 0.0;

        while (true) {
            Set<V> visited = new HashSet<>();
            double pathFlow = dfsAugmentingPath(residual, source, sink, Double.MAX_VALUE, visited);

            if (pathFlow == 0) {
                break;
            }
            totalFlow += pathFlow;
        }

        return totalFlow;
    }

    // ==================== Flow Details | 流详情 ====================

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
    public static <V> Map<Edge<V>, Double> getFlows(Graph<V> graph, V source, V sink) {
        if (graph == null || source == null || sink == null) {
            return Collections.emptyMap();
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(sink)) {
            return Collections.emptyMap();
        }

        ResidualGraph<V> residual = new ResidualGraph<>(graph);

        // Run Ford-Fulkerson
        while (true) {
            Map<V, V> parent = new HashMap<>();
            Map<V, Double> pathCapacity = new HashMap<>();

            double bottleneck = bfsAugmentingPath(residual, source, sink, parent, pathCapacity);

            if (bottleneck == 0) {
                break;
            }

            V current = sink;
            while (!current.equals(source)) {
                V prev = parent.get(current);
                residual.addFlow(prev, current, bottleneck);
                current = prev;
            }
        }

        // Extract flows
        Map<Edge<V>, Double> flows = new HashMap<>();
        for (Edge<V> edge : graph.edges()) {
            double flow = residual.getFlow(edge.from(), edge.to());
            if (flow > 0) {
                flows.put(edge, flow);
            }
        }

        return flows;
    }

    /**
     * Get the flow result with detailed information
     * 获取包含详细信息的流结果
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @param source the source vertex | 源顶点
     * @param sink the sink vertex | 汇顶点
     * @return the flow result | 流结果
     */
    public static <V> FlowResult<V> computeFlow(Graph<V> graph, V source, V sink) {
        if (graph == null || source == null || sink == null) {
            return new FlowResult<>(0.0, Collections.emptyMap(), Collections.emptySet());
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(sink)) {
            return new FlowResult<>(0.0, Collections.emptyMap(), Collections.emptySet());
        }
        if (source.equals(sink)) {
            return new FlowResult<>(0.0, Collections.emptyMap(), Collections.emptySet());
        }

        ResidualGraph<V> residual = new ResidualGraph<>(graph);
        double totalFlow = 0.0;

        // Run Ford-Fulkerson
        while (true) {
            Map<V, V> parent = new HashMap<>();
            Map<V, Double> pathCapacity = new HashMap<>();

            double bottleneck = bfsAugmentingPath(residual, source, sink, parent, pathCapacity);

            if (bottleneck == 0) {
                break;
            }

            V current = sink;
            while (!current.equals(source)) {
                V prev = parent.get(current);
                residual.addFlow(prev, current, bottleneck);
                current = prev;
            }
            totalFlow += bottleneck;
        }

        // Extract flows
        Map<Edge<V>, Double> flows = new HashMap<>();
        for (Edge<V> edge : graph.edges()) {
            double flow = residual.getFlow(edge.from(), edge.to());
            if (flow > 0) {
                flows.put(edge, flow);
            }
        }

        // Find min-cut
        Set<Edge<V>> minCut = findMinCut(graph, residual, source);

        return new FlowResult<>(totalFlow, flows, minCut);
    }

    // ==================== Min-Cut | 最小割 ====================

    /**
     * Find minimum cut edges
     * 查找最小割边
     *
     * <p>The minimum cut is the set of edges with minimum total capacity
     * that separates source from sink.</p>
     * <p>最小割是将源和汇分离的最小总容量边集。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @param source the source vertex | 源顶点
     * @param sink the sink vertex | 汇顶点
     * @return set of edges in the minimum cut | 最小割中的边集合
     */
    public static <V> Set<Edge<V>> minCut(Graph<V> graph, V source, V sink) {
        if (graph == null || source == null || sink == null) {
            return Collections.emptySet();
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(sink)) {
            return Collections.emptySet();
        }

        ResidualGraph<V> residual = new ResidualGraph<>(graph);

        // Run Ford-Fulkerson
        while (true) {
            Map<V, V> parent = new HashMap<>();
            Map<V, Double> pathCapacity = new HashMap<>();

            double bottleneck = bfsAugmentingPath(residual, source, sink, parent, pathCapacity);

            if (bottleneck == 0) {
                break;
            }

            V current = sink;
            while (!current.equals(source)) {
                V prev = parent.get(current);
                residual.addFlow(prev, current, bottleneck);
                current = prev;
            }
        }

        return findMinCut(graph, residual, source);
    }

    /**
     * Calculate min-cut capacity
     * 计算最小割容量
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @param source the source vertex | 源顶点
     * @param sink the sink vertex | 汇顶点
     * @return the min-cut capacity (equals max flow) | 最小割容量（等于最大流）
     */
    public static <V> double minCutCapacity(Graph<V> graph, V source, V sink) {
        return maxFlow(graph, source, sink); // By max-flow min-cut theorem
    }

    // ==================== Helper Methods | 辅助方法 ====================

    /**
     * BFS to find augmenting path
     */
    private static <V> double bfsAugmentingPath(
            ResidualGraph<V> residual,
            V source,
            V sink,
            Map<V, V> parent,
            Map<V, Double> pathCapacity) {

        Queue<V> queue = new LinkedList<>();
        Set<V> visited = new HashSet<>();

        queue.offer(source);
        visited.add(source);
        pathCapacity.put(source, Double.MAX_VALUE);

        while (!queue.isEmpty()) {
            V current = queue.poll();

            for (V neighbor : residual.getNeighbors(current)) {
                double capacity = residual.getResidualCapacity(current, neighbor);

                if (!visited.contains(neighbor) && capacity > 0) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    pathCapacity.put(neighbor, Math.min(pathCapacity.get(current), capacity));

                    if (neighbor.equals(sink)) {
                        return pathCapacity.get(sink);
                    }

                    queue.offer(neighbor);
                }
            }
        }

        return 0.0; // No augmenting path
    }

    /**
     * DFS to find augmenting path and update flow
     */
    private static <V> double dfsAugmentingPath(
            ResidualGraph<V> residual,
            V current,
            V sink,
            double flow,
            Set<V> visited) {

        if (current.equals(sink)) {
            return flow;
        }

        visited.add(current);

        for (V neighbor : residual.getNeighbors(current)) {
            double capacity = residual.getResidualCapacity(current, neighbor);

            if (!visited.contains(neighbor) && capacity > 0) {
                double newFlow = dfsAugmentingPath(
                    residual, neighbor, sink, Math.min(flow, capacity), visited);

                if (newFlow > 0) {
                    residual.addFlow(current, neighbor, newFlow);
                    return newFlow;
                }
            }
        }

        return 0.0;
    }

    /**
     * Find min-cut edges from residual graph
     */
    private static <V> Set<Edge<V>> findMinCut(
            Graph<V> graph,
            ResidualGraph<V> residual,
            V source) {

        // BFS to find all vertices reachable from source in residual graph
        Set<V> reachable = new HashSet<>();
        Queue<V> queue = new LinkedList<>();

        queue.offer(source);
        reachable.add(source);

        while (!queue.isEmpty()) {
            V current = queue.poll();

            for (V neighbor : residual.getNeighbors(current)) {
                if (!reachable.contains(neighbor) &&
                    residual.getResidualCapacity(current, neighbor) > 0) {
                    reachable.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        // Min-cut edges are edges from reachable to non-reachable
        Set<Edge<V>> minCut = new HashSet<>();
        for (Edge<V> edge : graph.edges()) {
            if (reachable.contains(edge.from()) && !reachable.contains(edge.to())) {
                minCut.add(edge);
            }
        }

        return minCut;
    }

    // ==================== Residual Graph | 残余图 ====================

    /**
     * Residual graph for Ford-Fulkerson algorithm
     * Ford-Fulkerson算法的残余图
     */
    private static class ResidualGraph<V> {
        private final Map<V, Map<V, Double>> capacity; // Original capacity
        private final Map<V, Map<V, Double>> flow;     // Current flow
        private final Set<V> vertices;

        public ResidualGraph(Graph<V> graph) {
            this.capacity = new HashMap<>();
            this.flow = new HashMap<>();
            this.vertices = new HashSet<>(graph.vertices());

            // Initialize capacity from graph
            for (V v : vertices) {
                capacity.put(v, new HashMap<>());
                flow.put(v, new HashMap<>());
            }

            for (Edge<V> edge : graph.edges()) {
                capacity.get(edge.from()).put(edge.to(), edge.weight());
            }
        }

        public Set<V> getNeighbors(V v) {
            Set<V> neighbors = new HashSet<>();

            // Forward edges with remaining capacity
            if (capacity.containsKey(v)) {
                for (V neighbor : capacity.get(v).keySet()) {
                    if (getResidualCapacity(v, neighbor) > 0) {
                        neighbors.add(neighbor);
                    }
                }
            }

            // Backward edges with flow to cancel
            for (V u : vertices) {
                if (flow.containsKey(u) && flow.get(u).containsKey(v)) {
                    double f = flow.get(u).getOrDefault(v, 0.0);
                    if (f > 0) {
                        neighbors.add(u);
                    }
                }
            }

            return neighbors;
        }

        public double getResidualCapacity(V from, V to) {
            double cap = capacity.getOrDefault(from, Collections.emptyMap())
                                 .getOrDefault(to, 0.0);
            double f = flow.getOrDefault(from, Collections.emptyMap())
                           .getOrDefault(to, 0.0);
            double reverseF = flow.getOrDefault(to, Collections.emptyMap())
                                  .getOrDefault(from, 0.0);

            // Residual capacity = original capacity - flow + reverse flow
            return cap - f + reverseF;
        }

        public void addFlow(V from, V to, double amount) {
            // Check if this is a forward edge
            double cap = capacity.getOrDefault(from, Collections.emptyMap())
                                 .getOrDefault(to, 0.0);

            if (cap > 0) {
                // Forward edge - add flow
                double currentFlow = flow.get(from).getOrDefault(to, 0.0);
                flow.get(from).put(to, currentFlow + amount);
            } else {
                // Backward edge - reduce flow in opposite direction
                double currentFlow = flow.get(to).getOrDefault(from, 0.0);
                flow.get(to).put(from, currentFlow - amount);
            }
        }

        public double getFlow(V from, V to) {
            return flow.getOrDefault(from, Collections.emptyMap())
                       .getOrDefault(to, 0.0);
        }
    }

    // ==================== Result Types | 结果类型 ====================

    /**
     * Flow result containing max flow value, edge flows, and min-cut
     * 包含最大流值、边流量和最小割的流结果
     *
     * @param <V> the vertex type | 顶点类型
     * @param maxFlow the maximum flow value | 最大流值
     * @param edgeFlows flow on each edge | 每条边上的流量
     * @param minCut edges in minimum cut | 最小割中的边
     */
    public record FlowResult<V>(
            double maxFlow,
            Map<Edge<V>, Double> edgeFlows,
            Set<Edge<V>> minCut) {

        /**
         * Get the minimum cut capacity (equals max flow)
         * 获取最小割容量（等于最大流）
         *
         * @return min-cut capacity | 最小割容量
         */
        public double minCutCapacity() {
            return maxFlow;
        }

        /**
         * Check if flow is valid (max flow is positive)
         * 检查流是否有效（最大流为正）
         *
         * @return true if valid | 如果有效返回true
         */
        public boolean hasFlow() {
            return maxFlow > 0;
        }
    }
}
