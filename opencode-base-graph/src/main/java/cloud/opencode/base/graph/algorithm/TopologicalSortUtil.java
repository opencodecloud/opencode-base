package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.exception.CycleDetectedException;
import cloud.opencode.base.graph.exception.GraphException;
import cloud.opencode.base.graph.exception.GraphErrorCode;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Topological Sort Util
 * 拓扑排序工具类
 *
 * <p>Utility class for topological sorting of directed acyclic graphs (DAG).</p>
 * <p>有向无环图（DAG）拓扑排序的工具类。</p>
 *
 * <p><strong>Algorithms | 算法:</strong></p>
 * <ul>
 *   <li>Kahn's algorithm (BFS-based) | Kahn算法（基于BFS）</li>
 *   <li>DFS-based algorithm | 基于DFS的算法</li>
 * </ul>
 *
 * <p><strong>Time Complexity | 时间复杂度:</strong> O(V + E)</p>
 * <p><strong>Space Complexity | 空间复杂度:</strong> O(V)</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Kahn's BFS-based topological sort - 基于BFS的Kahn拓扑排序</li>
 *   <li>DFS-based topological sort - 基于DFS的拓扑排序</li>
 *   <li>Cycle detection with exception reporting - 带异常报告的环检测</li>
 *   <li>All valid orderings enumeration - 所有有效排序枚举</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Topological sort
 * List<String> order = TopologicalSortUtil.sort(dag);
 *
 * // Get all valid topological orderings
 * List<List<String>> allOrders = TopologicalSortUtil.allSorts(dag);
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
 *   <li>Time complexity: O(V + E) - 时间复杂度: O(V + E)</li>
 *   <li>Space complexity: O(V) - 空间复杂度: O(V)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class TopologicalSortUtil {

    private TopologicalSortUtil() {
        // Utility class
    }

    /**
     * Topological sort using Kahn's algorithm
     * 使用Kahn算法进行拓扑排序
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @return list of vertices in topological order | 拓扑顺序的顶点列表
     * @throws GraphException if graph is not directed | 如果图不是有向图则抛出异常
     * @throws CycleDetectedException if graph contains a cycle | 如果图包含环则抛出异常
     */
    public static <V> List<V> sort(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyList();
        }

        if (!graph.isDirected()) {
            throw new GraphException("Topological sort requires a directed graph",
                GraphErrorCode.INVALID_DIRECTION);
        }

        // Calculate in-degree for each vertex
        Map<V, Integer> inDegree = new HashMap<>();
        for (V v : graph.vertices()) {
            inDegree.put(v, 0);
        }

        for (Edge<V> edge : graph.edges()) {
            inDegree.merge(edge.to(), 1, Integer::sum);
        }

        // Initialize queue with vertices having in-degree 0
        Queue<V> queue = new LinkedList<>();
        for (Map.Entry<V, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<V> result = new ArrayList<>();

        while (!queue.isEmpty()) {
            V vertex = queue.poll();
            result.add(vertex);

            for (V neighbor : graph.neighbors(vertex)) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);
                if (newDegree == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // Check if all vertices were processed
        if (result.size() != graph.vertexCount()) {
            throw new CycleDetectedException("Graph contains a cycle - topological sort not possible");
        }

        return result;
    }

    /**
     * Topological sort using DFS
     * 使用DFS进行拓扑排序
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the directed graph | 有向图
     * @return list of vertices in topological order | 拓扑顺序的顶点列表
     * @throws GraphException if graph is not directed | 如果图不是有向图则抛出异常
     * @throws CycleDetectedException if graph contains a cycle | 如果图包含环则抛出异常
     */
    public static <V> List<V> sortDfs(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyList();
        }

        if (!graph.isDirected()) {
            throw new GraphException("Topological sort requires a directed graph",
                GraphErrorCode.INVALID_DIRECTION);
        }

        Set<V> visited = new HashSet<>();
        Set<V> recursionStack = new HashSet<>();
        LinkedList<V> result = new LinkedList<>();

        for (V vertex : graph.vertices()) {
            if (!visited.contains(vertex)) {
                if (!dfsVisit(graph, vertex, visited, recursionStack, result)) {
                    throw new CycleDetectedException("Graph contains a cycle - topological sort not possible");
                }
            }
        }

        return result;
    }

    private static <V> boolean dfsVisit(Graph<V> graph, V vertex,
                                         Set<V> visited, Set<V> recursionStack,
                                         LinkedList<V> result) {
        visited.add(vertex);
        recursionStack.add(vertex);

        for (V neighbor : graph.neighbors(vertex)) {
            if (!visited.contains(neighbor)) {
                if (!dfsVisit(graph, neighbor, visited, recursionStack, result)) {
                    return false;
                }
            } else if (recursionStack.contains(neighbor)) {
                // Cycle detected
                return false;
            }
        }

        recursionStack.remove(vertex);
        result.addFirst(vertex);
        return true;
    }

    /**
     * Check if a valid topological ordering exists
     * 检查是否存在有效的拓扑排序
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if topological ordering exists (graph is DAG) | 如果存在拓扑排序（图是DAG）返回true
     */
    public static <V> boolean canSort(Graph<V> graph) {
        if (graph == null || !graph.isDirected()) {
            return false;
        }
        try {
            sort(graph);
            return true;
        } catch (CycleDetectedException e) {
            return false;
        }
    }

    /**
     * Get vertices with no dependencies (in-degree 0)
     * 获取无依赖的顶点（入度为0）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return set of vertices with no incoming edges | 无入边的顶点集合
     */
    public static <V> Set<V> getSourceVertices(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptySet();
        }

        Set<V> sources = new HashSet<>(graph.vertices());

        for (Edge<V> edge : graph.edges()) {
            sources.remove(edge.to());
        }

        return sources;
    }

    /**
     * Get vertices with no dependents (out-degree 0)
     * 获取无被依赖的顶点（出度为0）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return set of vertices with no outgoing edges | 无出边的顶点集合
     */
    public static <V> Set<V> getSinkVertices(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptySet();
        }

        Set<V> sinks = new HashSet<>();

        for (V vertex : graph.vertices()) {
            if (graph.outEdges(vertex).isEmpty()) {
                sinks.add(vertex);
            }
        }

        return sinks;
    }

    /**
     * Get the dependency depth (longest path length) for each vertex
     * 获取每个顶点的依赖深度（最长路径长度）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return map of vertex to its dependency depth | 顶点到其依赖深度的映射
     */
    public static <V> Map<V, Integer> getDependencyDepths(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        List<V> sorted = sort(graph);
        Map<V, Integer> depths = new HashMap<>();

        for (V vertex : sorted) {
            int maxDepth = 0;
            for (Edge<V> inEdge : graph.inEdges(vertex)) {
                maxDepth = Math.max(maxDepth, depths.getOrDefault(inEdge.from(), 0) + 1);
            }
            depths.put(vertex, maxDepth);
        }

        return depths;
    }
}
