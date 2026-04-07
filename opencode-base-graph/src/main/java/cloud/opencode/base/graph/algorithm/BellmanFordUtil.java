package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.exception.GraphErrorCode;
import cloud.opencode.base.graph.exception.GraphException;
import cloud.opencode.base.graph.exception.InvalidVertexException;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Bellman-Ford Util - Single-source shortest paths with negative weight support
 * Bellman-Ford工具类 - 支持负权重的单源最短路径
 *
 * <p>Implements the Bellman-Ford algorithm for computing single-source shortest paths
 * in graphs that may contain negative weight edges. Detects negative cycles.</p>
 * <p>实现Bellman-Ford算法，用于计算可能包含负权重边的图中的单源最短路径。可检测负环。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single-source shortest paths with negative weights - 支持负权重的单源最短路径</li>
 *   <li>Negative cycle detection - 负环检测</li>
 *   <li>Negative cycle extraction - 负环提取</li>
 *   <li>Path reconstruction between two vertices - 两顶点间路径重建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> graph = new DirectedGraph<>();
 * graph.addEdge("A", "B", 4.0);
 * graph.addEdge("B", "C", -2.0);
 *
 * Map<String, Double> distances = BellmanFordUtil.shortestPaths(graph, "A");
 * List<String> path = BellmanFordUtil.shortestPath(graph, "A", "C");
 * boolean hasNeg = BellmanFordUtil.hasNegativeCycle(graph, "A");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns empty results for null graph, throws for null vertex) - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(V * E) - 时间复杂度: O(V * E)</li>
 *   <li>Space complexity: O(V) - 空间复杂度: O(V)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public final class BellmanFordUtil {

    private BellmanFordUtil() {
        // Utility class
    }

    /**
     * Compute single-source shortest paths using Bellman-Ford algorithm.
     * Supports negative edge weights. Throws if a negative cycle is reachable from source.
     * 使用Bellman-Ford算法计算单源最短路径。支持负权重边。如果从源可达负环则抛出异常。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @return map of vertex to shortest distance from source | 从源顶点到各顶点的最短距离映射
     * @throws InvalidVertexException if source is null | 当源顶点为null时抛出
     * @throws GraphException with NEGATIVE_CYCLE if a negative cycle is reachable from source |
     *         当从源可达负环时抛出NEGATIVE_CYCLE
     */
    public static <V> Map<V, Double> shortestPaths(Graph<V> graph, V source) {
        if (source == null) {
            throw new InvalidVertexException("Source vertex cannot be null");
        }
        if (graph == null || graph.isEmpty() || !graph.containsVertex(source)) {
            return Collections.emptyMap();
        }

        BellmanFordResult<V> result = runBellmanFord(graph, source);

        if (result.hasNegativeCycle) {
            throw new GraphException("Negative cycle detected reachable from source: " + source,
                    GraphErrorCode.NEGATIVE_CYCLE);
        }

        return result.distances;
    }

    /**
     * Find shortest path between two vertices using Bellman-Ford algorithm.
     * 使用Bellman-Ford算法查找两顶点间的最短路径。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return list of vertices in the shortest path, or empty list if no path |
     *         最短路径的顶点列表，无路径时返回空列表
     * @throws InvalidVertexException if source or target is null | 当源或目标顶点为null时抛出
     * @throws GraphException with NEGATIVE_CYCLE if a negative cycle is reachable from source |
     *         当从源可达负环时抛出NEGATIVE_CYCLE
     */
    public static <V> List<V> shortestPath(Graph<V> graph, V source, V target) {
        if (source == null) {
            throw new InvalidVertexException("Source vertex cannot be null");
        }
        if (target == null) {
            throw new InvalidVertexException("Target vertex cannot be null");
        }
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyList();
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            return Collections.emptyList();
        }
        if (source.equals(target)) {
            return List.of(source);
        }

        BellmanFordResult<V> result = runBellmanFord(graph, source);

        if (result.hasNegativeCycle) {
            throw new GraphException("Negative cycle detected reachable from source: " + source,
                    GraphErrorCode.NEGATIVE_CYCLE);
        }

        Double dist = result.distances.get(target);
        if (dist == null || dist == Double.POSITIVE_INFINITY) {
            return Collections.emptyList();
        }

        return buildPath(result.predecessors, source, target);
    }

    /**
     * Check if a negative cycle is reachable from the given source vertex.
     * 检查从给定源顶点是否可达负环。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @return true if a negative cycle is reachable from source | 如果从源可达负环返回true
     * @throws InvalidVertexException if source is null | 当源顶点为null时抛出
     */
    public static <V> boolean hasNegativeCycle(Graph<V> graph, V source) {
        if (source == null) {
            throw new InvalidVertexException("Source vertex cannot be null");
        }
        if (graph == null || graph.isEmpty() || !graph.containsVertex(source)) {
            return false;
        }

        BellmanFordResult<V> result = runBellmanFord(graph, source);
        return result.hasNegativeCycle;
    }

    /**
     * Find and return a negative cycle reachable from the given source vertex.
     * 查找并返回从给定源顶点可达的负环。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @return list of vertices forming a negative cycle, or empty list if no negative cycle |
     *         形成负环的顶点列表，无负环时返回空列表
     * @throws InvalidVertexException if source is null | 当源顶点为null时抛出
     */
    public static <V> List<V> findNegativeCycle(Graph<V> graph, V source) {
        if (source == null) {
            throw new InvalidVertexException("Source vertex cannot be null");
        }
        if (graph == null || graph.isEmpty() || !graph.containsVertex(source)) {
            return Collections.emptyList();
        }

        BellmanFordResult<V> result = runBellmanFord(graph, source);
        if (!result.hasNegativeCycle || result.negativeCycleVertex == null) {
            return Collections.emptyList();
        }

        return extractNegativeCycle(result.predecessors, result.negativeCycleVertex,
                graph.vertexCount());
    }

    /**
     * Run Bellman-Ford and return full result state.
     */
    private static <V> BellmanFordResult<V> runBellmanFord(Graph<V> graph, V source) {
        Map<V, Double> distances = new HashMap<>();
        Map<V, V> predecessors = new HashMap<>();
        Set<V> vertices = graph.vertices();

        // Initialize distances
        for (V v : vertices) {
            distances.put(v, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);

        int vertexCount = graph.vertexCount();

        // Relax edges (V-1) times
        for (int i = 0; i < vertexCount - 1; i++) {
            boolean changed = false;
            for (V u : vertices) {
                double du = distances.get(u);
                if (du == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (Edge<V> edge : graph.outEdges(u)) {
                    V v = edge.to();
                    double newDist = du + edge.weight();
                    if (newDist < distances.get(v)) {
                        distances.put(v, newDist);
                        predecessors.put(v, u);
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break; // Early termination — no relaxation happened
            }
        }

        // Check for negative cycles (V-th relaxation)
        V negativeCycleVertex = null;
        for (V u : vertices) {
            double du = distances.get(u);
            if (du == Double.POSITIVE_INFINITY) {
                continue;
            }
            for (Edge<V> edge : graph.outEdges(u)) {
                V v = edge.to();
                double newDist = du + edge.weight();
                if (newDist < distances.get(v)) {
                    negativeCycleVertex = v;
                    break;
                }
            }
            if (negativeCycleVertex != null) {
                break;
            }
        }

        return new BellmanFordResult<>(distances, predecessors,
                negativeCycleVertex != null, negativeCycleVertex);
    }

    /**
     * Extract the negative cycle from the predecessor map.
     */
    private static <V> List<V> extractNegativeCycle(Map<V, V> predecessors,
                                                     V cycleVertex, int vertexCount) {
        // Walk back vertexCount times to ensure we are inside the cycle
        V v = cycleVertex;
        for (int i = 0; i < vertexCount; i++) {
            v = predecessors.get(v);
            if (v == null) {
                return Collections.emptyList();
            }
        }

        // Now v is guaranteed to be inside the negative cycle
        V start = v;
        List<V> cycle = new ArrayList<>();
        cycle.add(start);
        Set<V> seen = new HashSet<>();
        seen.add(start);
        v = predecessors.get(start);
        while (v != null && !v.equals(start)) {
            if (!seen.add(v)) {
                // Broken cycle structure (multiple negative cycles) — bail out safely
                return Collections.emptyList();
            }
            cycle.add(v);
            v = predecessors.get(v);
        }
        cycle.add(start); // Complete the cycle
        Collections.reverse(cycle);

        return cycle;
    }

    /**
     * Build path from predecessor map.
     */
    private static <V> List<V> buildPath(Map<V, V> predecessors, V source, V target) {
        LinkedList<V> path = new LinkedList<>();
        V current = target;
        Set<V> visited = new HashSet<>();

        while (current != null) {
            if (!visited.add(current)) {
                return Collections.emptyList(); // Cycle in predecessors — should not happen
            }
            path.addFirst(current);
            if (current.equals(source)) {
                return path;
            }
            current = predecessors.get(current);
        }

        return Collections.emptyList();
    }

    /**
     * Internal result holder for Bellman-Ford computation.
     */
    private record BellmanFordResult<V>(
            Map<V, Double> distances,
            Map<V, V> predecessors,
            boolean hasNegativeCycle,
            V negativeCycleVertex
    ) {}
}
