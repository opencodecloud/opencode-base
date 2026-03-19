package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Shortest Path Util
 * 最短路径工具类
 *
 * <p>Utility class for shortest path algorithms.</p>
 * <p>最短路径算法的工具类。</p>
 *
 * <p><strong>Algorithms | 算法:</strong></p>
 * <ul>
 *   <li>Dijkstra - Single-source shortest paths | 单源最短路径</li>
 * </ul>
 *
 * <p><strong>Time Complexity | 时间复杂度:</strong> O((V+E)logV)</p>
 * <p><strong>Space Complexity | 空间复杂度:</strong> O(V)</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dijkstra single-source shortest paths - Dijkstra单源最短路径</li>
 *   <li>Shortest path reconstruction between two vertices - 两顶点间最短路径重建</li>
 *   <li>Negative weight edge validation - 负权重边验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get distances from source to all vertices
 * Map<String, Double> distances = ShortestPathUtil.dijkstra(graph, "A");
 *
 * // Get shortest path between two vertices
 * List<String> path = ShortestPathUtil.shortestPath(graph, "A", "D");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns empty results for null inputs) - 空值安全: 是（null输入返回空结果）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O((V + E) log V) - 时间复杂度: O((V + E) log V)</li>
 *   <li>Space complexity: O(V) - 空间复杂度: O(V)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class ShortestPathUtil {

    private ShortestPathUtil() {
        // Utility class
    }

    /**
     * Dijkstra's algorithm for single-source shortest paths
     * Dijkstra单源最短路径算法
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @return map of vertex to shortest distance from source | 从源顶点到各顶点的最短距离映射
     */
    public static <V> Map<V, Double> dijkstra(Graph<V> graph, V source) {
        if (graph == null || source == null || !graph.containsVertex(source)) {
            return Collections.emptyMap();
        }

        // Validate no negative weight edges (Dijkstra requires non-negative weights)
        for (V v : graph.vertices()) {
            for (Edge<V> edge : graph.outEdges(v)) {
                if (edge.weight() < 0) {
                    throw new IllegalArgumentException(
                            "Dijkstra's algorithm does not support negative edge weights. "
                            + "Found weight " + edge.weight()
                            + " on edge from " + edge.from() + " to " + edge.to());
                }
            }
        }

        Map<V, Double> distances = new HashMap<>();
        PriorityQueue<VertexDistance<V>> pq = new PriorityQueue<>(
            Comparator.comparingDouble(VertexDistance::distance)
        );
        Set<V> visited = new HashSet<>();

        // Initialize distances
        for (V v : graph.vertices()) {
            distances.put(v, Double.MAX_VALUE);
        }
        distances.put(source, 0.0);
        pq.offer(new VertexDistance<>(source, 0.0));

        while (!pq.isEmpty()) {
            VertexDistance<V> current = pq.poll();
            V vertex = current.vertex();

            if (visited.contains(vertex)) {
                continue;
            }
            visited.add(vertex);

            double currentDist = distances.get(vertex);
            if (currentDist == Double.MAX_VALUE) {
                continue; // unreachable vertex, skip to avoid overflow
            }

            for (Edge<V> edge : graph.outEdges(vertex)) {
                V neighbor = edge.to();
                double newDist = currentDist + edge.weight();

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    pq.offer(new VertexDistance<>(neighbor, newDist));
                }
            }
        }

        return distances;
    }

    /**
     * Find shortest path between two vertices
     * 查找两个顶点之间的最短路径
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return list of vertices in the shortest path, or empty list if no path | 最短路径的顶点列表，无路径时返回空列表
     */
    public static <V> List<V> shortestPath(Graph<V> graph, V source, V target) {
        if (graph == null || source == null || target == null) {
            return Collections.emptyList();
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            return Collections.emptyList();
        }
        if (source.equals(target)) {
            return List.of(source);
        }

        Map<V, V> predecessors = new HashMap<>();
        Map<V, Double> distances = new HashMap<>();
        PriorityQueue<VertexDistance<V>> pq = new PriorityQueue<>(
            Comparator.comparingDouble(VertexDistance::distance)
        );
        Set<V> visited = new HashSet<>();

        // Initialize
        for (V v : graph.vertices()) {
            distances.put(v, Double.MAX_VALUE);
        }
        distances.put(source, 0.0);
        pq.offer(new VertexDistance<>(source, 0.0));

        while (!pq.isEmpty()) {
            VertexDistance<V> current = pq.poll();
            V vertex = current.vertex();

            if (vertex.equals(target)) {
                return buildPath(predecessors, source, target);
            }

            if (visited.contains(vertex)) {
                continue;
            }
            visited.add(vertex);

            double currentDist = distances.get(vertex);
            if (currentDist == Double.MAX_VALUE) {
                continue; // unreachable vertex, skip to avoid overflow
            }

            for (Edge<V> edge : graph.outEdges(vertex)) {
                V neighbor = edge.to();
                double newDist = currentDist + edge.weight();

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    predecessors.put(neighbor, vertex);
                    pq.offer(new VertexDistance<>(neighbor, newDist));
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    /**
     * Get the shortest distance between two vertices
     * 获取两个顶点之间的最短距离
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return the shortest distance, or Double.MAX_VALUE if no path | 最短距离，无路径时返回Double.MAX_VALUE
     */
    public static <V> double shortestDistance(Graph<V> graph, V source, V target) {
        Map<V, Double> distances = dijkstra(graph, source);
        return distances.getOrDefault(target, Double.MAX_VALUE);
    }

    /**
     * Check if a path exists between two vertices
     * 检查两个顶点之间是否存在路径
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return true if a path exists | 如果存在路径返回true
     */
    public static <V> boolean hasPath(Graph<V> graph, V source, V target) {
        if (graph == null || source == null || target == null) {
            return false;
        }
        if (source.equals(target)) {
            return graph.containsVertex(source);
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            return false;
        }
        Set<V> visited = new HashSet<>();
        Deque<V> queue = new ArrayDeque<>();
        queue.add(source);
        visited.add(source);
        while (!queue.isEmpty()) {
            V v = queue.poll();
            for (var edge : graph.outEdges(v)) {
                V neighbor = edge.to();
                if (neighbor.equals(target)) return true;
                if (visited.add(neighbor)) queue.add(neighbor);
            }
        }
        return false;
    }

    private static <V> List<V> buildPath(Map<V, V> predecessors, V source, V target) {
        LinkedList<V> path = new LinkedList<>();
        V current = target;

        while (current != null) {
            path.addFirst(current);
            if (current.equals(source)) {
                break;
            }
            current = predecessors.get(current);
        }

        if (path.isEmpty() || !path.getFirst().equals(source)) {
            return Collections.emptyList();
        }

        return path;
    }

    /**
     * Helper record for vertex with distance
     */
    private record VertexDistance<V>(V vertex, double distance) {}
}
