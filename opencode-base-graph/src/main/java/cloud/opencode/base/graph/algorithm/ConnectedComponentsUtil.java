package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;

import java.util.*;

/**
 * Connected Components Util
 * 连通分量工具类
 *
 * <p>Utility class for finding connected components in graphs.</p>
 * <p>查找图中连通分量的工具类。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Find all connected components | 查找所有连通分量</li>
 *   <li>Check if two vertices are connected | 检查两个顶点是否连通</li>
 *   <li>Check if graph is fully connected | 检查图是否完全连通</li>
 * </ul>
 *
 * <p><strong>Time Complexity | 时间复杂度:</strong> O(V + E)</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Find all connected components
 * List<Set<String>> components = ConnectedComponentsUtil.find(graph);
 *
 * // Check if two vertices are connected
 * boolean connected = ConnectedComponentsUtil.isConnected(graph, "A", "B");
 *
 * // Check if graph is fully connected
 * boolean fullyConnected = ConnectedComponentsUtil.isFullyConnected(graph);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns empty/false for null inputs) - 空值安全: 是（null输入返回空/false）</li>
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
public final class ConnectedComponentsUtil {

    private ConnectedComponentsUtil() {
        // Utility class
    }

    /**
     * Find all connected components
     * 查找所有连通分量
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return list of connected components (each component is a set of vertices) | 连通分量列表（每个分量是顶点集合）
     */
    public static <V> List<Set<V>> find(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyList();
        }

        List<Set<V>> components = new ArrayList<>();
        Set<V> visited = new HashSet<>();

        for (V vertex : graph.vertices()) {
            if (!visited.contains(vertex)) {
                Set<V> component = new HashSet<>();
                exploreComponent(graph, vertex, visited, component);
                components.add(component);
            }
        }

        return components;
    }

    private static <V> void exploreComponent(Graph<V> graph, V start,
                                              Set<V> visited, Set<V> component) {
        Queue<V> queue = new LinkedList<>();
        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            V vertex = queue.poll();
            component.add(vertex);

            // For undirected graphs, neighbors are bidirectional
            // For directed graphs, we consider both directions for weak connectivity
            Set<V> neighbors = new HashSet<>(graph.neighbors(vertex));

            // Also add vertices that have edges pointing to this vertex
            // Use inEdges() for O(1) access instead of iterating all vertices O(V)
            for (var edge : graph.inEdges(vertex)) {
                neighbors.add(edge.from());
            }

            for (V neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
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
        if (graph == null || v1 == null || v2 == null) {
            return false;
        }
        if (!graph.containsVertex(v1) || !graph.containsVertex(v2)) {
            return false;
        }
        if (v1.equals(v2)) {
            return true;
        }

        // BFS to check connectivity
        Set<V> visited = new HashSet<>();
        Queue<V> queue = new LinkedList<>();

        queue.offer(v1);
        visited.add(v1);

        while (!queue.isEmpty()) {
            V vertex = queue.poll();

            Set<V> neighbors = new HashSet<>(graph.neighbors(vertex));
            // For directed graphs, also consider reverse edges
            // Use inEdges() for O(1) access instead of iterating all vertices O(V)
            for (var edge : graph.inEdges(vertex)) {
                neighbors.add(edge.from());
            }

            for (V neighbor : neighbors) {
                if (neighbor.equals(v2)) {
                    return true;
                }
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return false;
    }

    /**
     * Check if graph is fully connected (single component)
     * 检查图是否完全连通（单一分量）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if fully connected | 如果完全连通返回true
     */
    public static <V> boolean isFullyConnected(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return true;
        }
        List<Set<V>> components = find(graph);
        return components.size() == 1;
    }

    /**
     * Get the number of connected components
     * 获取连通分量的数量
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return number of connected components | 连通分量数量
     */
    public static <V> int count(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return 0;
        }
        return find(graph).size();
    }

    /**
     * Get the largest connected component
     * 获取最大连通分量
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return the largest connected component, or empty set if graph is empty | 最大连通分量，图为空时返回空集合
     */
    public static <V> Set<V> getLargestComponent(Graph<V> graph) {
        List<Set<V>> components = find(graph);
        return components.stream()
            .max(Comparator.comparingInt(Set::size))
            .orElse(Collections.emptySet());
    }

    /**
     * Get the smallest connected component
     * 获取最小连通分量
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return the smallest connected component, or empty set if graph is empty | 最小连通分量，图为空时返回空集合
     */
    public static <V> Set<V> getSmallestComponent(Graph<V> graph) {
        List<Set<V>> components = find(graph);
        return components.stream()
            .min(Comparator.comparingInt(Set::size))
            .orElse(Collections.emptySet());
    }

    /**
     * Get the component containing a specific vertex
     * 获取包含特定顶点的分量
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param vertex the vertex | 顶点
     * @return the component containing the vertex, or empty set if vertex not found | 包含该顶点的分量，顶点未找到时返回空集合
     */
    public static <V> Set<V> getComponentContaining(Graph<V> graph, V vertex) {
        if (graph == null || vertex == null || !graph.containsVertex(vertex)) {
            return Collections.emptySet();
        }

        Set<V> component = new HashSet<>();
        Set<V> visited = new HashSet<>();
        exploreComponent(graph, vertex, visited, component);
        return component;
    }
}
