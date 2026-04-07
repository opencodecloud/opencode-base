package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;

import java.util.*;

/**
 * Bidirectional BFS Util
 * 双向BFS工具类
 *
 * <p>Utility class for bidirectional BFS search.</p>
 * <p>双向BFS搜索的工具类。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Searches from both ends simultaneously | 同时从两端搜索</li>
 *   <li>More efficient for large graphs | 对大图更高效</li>
 *   <li>O(sqrt(V)) instead of O(V) in best case | 最佳情况下O(sqrt(V))而非O(V)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Find path using bidirectional BFS
 * List<String> path = BidirectionalBfsUtil.findPath(graph, "A", "Z");
 *
 * // Check if path exists
 * boolean exists = BidirectionalBfsUtil.hasPath(graph, "A", "Z");
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
 *   <li>Time complexity: O(b^(d/2)) where b=branching factor, d=depth - 时间复杂度: O(b^(d/2))，其中b为分支因子，d为深度</li>
 *   <li>Space complexity: O(b^(d/2)) - 空间复杂度: O(b^(d/2))</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class BidirectionalBfsUtil {

    private BidirectionalBfsUtil() {
        // Utility class
    }

    /**
     * Find path using bidirectional BFS
     * 使用双向BFS查找路径
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return list of vertices in the path, or empty list if no path | 路径的顶点列表，无路径时返回空列表
     */
    public static <V> List<V> findPath(Graph<V> graph, V source, V target) {
        if (graph == null || source == null || target == null) {
            return Collections.emptyList();
        }
        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            return Collections.emptyList();
        }
        if (source.equals(target)) {
            return List.of(source);
        }

        // Forward search from source
        Map<V, V> forwardParent = new HashMap<>();
        Set<V> forwardVisited = new HashSet<>();
        Queue<V> forwardQueue = new LinkedList<>();

        // Backward search from target
        Map<V, V> backwardParent = new HashMap<>();
        Set<V> backwardVisited = new HashSet<>();
        Queue<V> backwardQueue = new LinkedList<>();

        forwardQueue.offer(source);
        forwardVisited.add(source);
        forwardParent.put(source, null);

        backwardQueue.offer(target);
        backwardVisited.add(target);
        backwardParent.put(target, null);

        V meetingPoint = null;

        while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty()) {
            // Expand forward
            meetingPoint = expandLevel(graph, forwardQueue, forwardVisited, forwardParent, backwardVisited, true);
            if (meetingPoint != null) {
                return buildPath(forwardParent, backwardParent, meetingPoint);
            }

            // Expand backward
            meetingPoint = expandLevel(graph, backwardQueue, backwardVisited, backwardParent, forwardVisited, false);
            if (meetingPoint != null) {
                return buildPath(forwardParent, backwardParent, meetingPoint);
            }
        }

        return Collections.emptyList();
    }

    private static <V> V expandLevel(Graph<V> graph, Queue<V> queue,
                                      Set<V> visited, Map<V, V> parent,
                                      Set<V> otherVisited, boolean forward) {
        int levelSize = queue.size();

        for (int i = 0; i < levelSize; i++) {
            V current = queue.poll();

            Set<V> neighbors;
            if (forward) {
                neighbors = graph.neighbors(current);
            } else {
                // For backward search, we need vertices that point to current
                neighbors = new HashSet<>();
                for (V v : graph.vertices()) {
                    if (graph.containsEdge(v, current)) {
                        neighbors.add(v);
                    }
                }
                // For undirected graphs, also add outgoing neighbors
                if (!graph.isDirected()) {
                    neighbors.addAll(graph.neighbors(current));
                }
            }

            for (V neighbor : neighbors) {
                if (otherVisited.contains(neighbor)) {
                    // Found meeting point
                    parent.put(neighbor, current);
                    return neighbor;
                }

                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        return null;
    }

    private static <V> List<V> buildPath(Map<V, V> forwardParent, Map<V, V> backwardParent, V meetingPoint) {
        LinkedList<V> path = new LinkedList<>();

        // Build path from source to meeting point
        V current = meetingPoint;
        while (current != null) {
            path.addFirst(current);
            current = forwardParent.get(current);
        }

        // Build path from meeting point to target
        current = backwardParent.get(meetingPoint);
        while (current != null) {
            path.addLast(current);
            current = backwardParent.get(current);
        }

        return path;
    }

    /**
     * Check if path exists using bidirectional BFS
     * 使用双向BFS检查路径是否存在
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return true if path exists | 如果路径存在返回true
     */
    public static <V> boolean hasPath(Graph<V> graph, V source, V target) {
        return !findPath(graph, source, target).isEmpty();
    }

    /**
     * Find shortest path distance using bidirectional BFS (unweighted)
     * 使用双向BFS查找最短路径距离（无权重）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return the shortest path length, or -1 if no path | 最短路径长度，无路径时返回-1
     */
    public static <V> int shortestPathLength(Graph<V> graph, V source, V target) {
        List<V> path = findPath(graph, source, target);
        return path.isEmpty() ? -1 : path.size() - 1;
    }

    /**
     * Find all vertices within a certain distance using bidirectional approach
     * 使用双向方法查找特定距离内的所有顶点
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @param maxDistance maximum distance from source | 距源的最大距离
     * @return set of vertices on any shortest path between source and target within distance | 在距离内从源到目标的任何最短路径上的顶点集合
     */
    public static <V> Set<V> findVerticesOnPath(Graph<V> graph, V source, V target, int maxDistance) {
        if (graph == null || source == null || target == null || maxDistance < 0) {
            return Collections.emptySet();
        }

        // Get all vertices reachable from source within maxDistance/2
        Set<V> fromSource = new HashSet<>();
        Map<V, Integer> distFromSource = new HashMap<>();
        Queue<V> queue = new LinkedList<>();

        queue.offer(source);
        distFromSource.put(source, 0);
        fromSource.add(source);

        while (!queue.isEmpty()) {
            V current = queue.poll();
            int dist = distFromSource.get(current);

            if (dist < maxDistance) {
                for (V neighbor : graph.neighbors(current)) {
                    if (!distFromSource.containsKey(neighbor)) {
                        distFromSource.put(neighbor, dist + 1);
                        fromSource.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
        }

        // Get all vertices reachable from target within maxDistance
        Set<V> fromTarget = new HashSet<>();
        Map<V, Integer> distFromTarget = new HashMap<>();
        queue.clear();

        queue.offer(target);
        distFromTarget.put(target, 0);
        fromTarget.add(target);

        while (!queue.isEmpty()) {
            V current = queue.poll();
            int dist = distFromTarget.get(current);

            if (dist < maxDistance) {
                // For backward traversal
                for (V v : graph.vertices()) {
                    if (graph.containsEdge(v, current) && !distFromTarget.containsKey(v)) {
                        distFromTarget.put(v, dist + 1);
                        fromTarget.add(v);
                        queue.offer(v);
                    }
                }
            }
        }

        // Intersection
        Set<V> result = new HashSet<>(fromSource);
        result.retainAll(fromTarget);

        // Filter by total distance
        result.removeIf(v -> {
            long totalDist = (long) distFromSource.getOrDefault(v, Integer.MAX_VALUE)
                + distFromTarget.getOrDefault(v, Integer.MAX_VALUE);
            return totalDist > maxDistance;
        });

        return result;
    }
}
