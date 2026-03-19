package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;
import java.util.function.BiFunction;

/**
 * A* Algorithm Util
 * A*算法工具类
 *
 * <p>A* pathfinding algorithm with heuristic function support.</p>
 * <p>支持启发式函数的A*寻路算法。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Heuristic-guided search | 启发式引导搜索</li>
 *   <li>Optimal path finding | 最优路径查找</li>
 *   <li>Faster than Dijkstra with good heuristic | 使用好的启发式函数比Dijkstra更快</li>
 * </ul>
 *
 * <p><strong>Time Complexity | 时间复杂度:</strong> O(E) ~ O(V²) depending on heuristic</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // With custom heuristic
 * BiFunction<String, String, Double> heuristic = (a, b) ->
 *     Math.abs(a.hashCode() - b.hashCode());
 * List<String> path = AStarUtil.findPath(graph, "A", "Z", heuristic);
 *
 * // With zero heuristic (equivalent to Dijkstra)
 * List<String> path = AStarUtil.findPath(graph, "A", "Z", (a, b) -> 0.0);
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
 *   <li>Time complexity: O(E log V) with good heuristic - 时间复杂度: O(E log V)（启发式函数较好时）</li>
 *   <li>Space complexity: O(V) - 空间复杂度: O(V)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class AStarUtil {

    private AStarUtil() {
        // Utility class
    }

    /**
     * Find shortest path using A* algorithm
     * 使用A*算法查找最短路径
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @param heuristic the heuristic function h(n) estimating distance to target | 启发式函数h(n)估计到目标的距离
     * @return list of vertices in the shortest path, or empty list if no path | 最短路径的顶点列表，无路径时返回空列表
     */
    public static <V> List<V> findPath(Graph<V> graph, V source, V target,
                                        BiFunction<V, V, Double> heuristic) {
        if (graph == null || source == null || target == null || heuristic == null) {
            return Collections.emptyList();
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            return Collections.emptyList();
        }
        if (source.equals(target)) {
            return List.of(source);
        }

        Map<V, V> cameFrom = new HashMap<>();
        Map<V, Double> gScore = new HashMap<>(); // Cost from start
        Map<V, Double> fScore = new HashMap<>(); // gScore + heuristic
        Set<V> closedSet = new HashSet<>();

        PriorityQueue<V> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(v -> fScore.getOrDefault(v, Double.MAX_VALUE))
        );

        gScore.put(source, 0.0);
        fScore.put(source, heuristic.apply(source, target));
        openSet.offer(source);

        while (!openSet.isEmpty()) {
            V current = openSet.poll();

            if (current.equals(target)) {
                return reconstructPath(cameFrom, current);
            }

            if (closedSet.contains(current)) {
                continue;
            }
            closedSet.add(current);

            for (Edge<V> edge : graph.outEdges(current)) {
                V neighbor = edge.to();

                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGScore = gScore.get(current) + edge.weight();

                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristic.apply(neighbor, target));
                    openSet.offer(neighbor);
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    /**
     * Find path with zero heuristic (equivalent to Dijkstra)
     * 使用零启发式函数查找路径（等同于Dijkstra）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return list of vertices in the shortest path | 最短路径的顶点列表
     */
    public static <V> List<V> findPath(Graph<V> graph, V source, V target) {
        return findPath(graph, source, target, (a, b) -> 0.0);
    }

    /**
     * Find path with cost limit
     * 使用成本限制查找路径
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @param heuristic the heuristic function | 启发式函数
     * @param maxCost maximum allowed path cost | 最大允许路径成本
     * @return list of vertices in the path, or empty list if no path within cost | 路径的顶点列表，超出成本限制时返回空列表
     */
    public static <V> List<V> findPathWithCostLimit(Graph<V> graph, V source, V target,
                                                     BiFunction<V, V, Double> heuristic,
                                                     double maxCost) {
        List<V> path = findPath(graph, source, target, heuristic);

        if (path.isEmpty()) {
            return path;
        }

        // Calculate actual path cost
        double cost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            cost += graph.getWeight(path.get(i), path.get(i + 1));
        }

        return cost <= maxCost ? path : Collections.emptyList();
    }

    private static <V> List<V> reconstructPath(Map<V, V> cameFrom, V current) {
        LinkedList<V> path = new LinkedList<>();
        path.addFirst(current);

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.addFirst(current);
        }

        return path;
    }

    /**
     * Result of A* search including path and cost
     * A*搜索结果，包含路径和成本
     *
     * @param <V> the vertex type | 顶点类型
     * @param path the path vertices | 路径顶点
     * @param cost the total path cost | 总路径成本
     * @param nodesExpanded number of nodes expanded during search | 搜索期间展开的节点数
     */
    public record PathResult<V>(List<V> path, double cost, int nodesExpanded) {
        public boolean hasPath() {
            return !path.isEmpty();
        }
    }

    /**
     * Find path with detailed result
     * 查找路径并返回详细结果
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @param heuristic the heuristic function | 启发式函数
     * @return detailed path result | 详细路径结果
     */
    public static <V> PathResult<V> findPathDetailed(Graph<V> graph, V source, V target,
                                                      BiFunction<V, V, Double> heuristic) {
        if (graph == null || source == null || target == null || heuristic == null) {
            return new PathResult<>(Collections.emptyList(), Double.MAX_VALUE, 0);
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            return new PathResult<>(Collections.emptyList(), Double.MAX_VALUE, 0);
        }
        if (source.equals(target)) {
            return new PathResult<>(List.of(source), 0.0, 1);
        }

        Map<V, V> cameFrom = new HashMap<>();
        Map<V, Double> gScore = new HashMap<>();
        Map<V, Double> fScore = new HashMap<>();
        Set<V> closedSet = new HashSet<>();
        int nodesExpanded = 0;

        PriorityQueue<V> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(v -> fScore.getOrDefault(v, Double.MAX_VALUE))
        );

        gScore.put(source, 0.0);
        fScore.put(source, heuristic.apply(source, target));
        openSet.offer(source);

        while (!openSet.isEmpty()) {
            V current = openSet.poll();
            nodesExpanded++;

            if (current.equals(target)) {
                List<V> path = reconstructPath(cameFrom, current);
                return new PathResult<>(path, gScore.get(current), nodesExpanded);
            }

            if (closedSet.contains(current)) {
                continue;
            }
            closedSet.add(current);

            for (Edge<V> edge : graph.outEdges(current)) {
                V neighbor = edge.to();

                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGScore = gScore.get(current) + edge.weight();

                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristic.apply(neighbor, target));
                    openSet.offer(neighbor);
                }
            }
        }

        return new PathResult<>(Collections.emptyList(), Double.MAX_VALUE, nodesExpanded);
    }
}
