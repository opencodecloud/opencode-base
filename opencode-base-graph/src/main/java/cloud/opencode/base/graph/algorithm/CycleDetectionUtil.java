package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;

import java.util.*;

/**
 * Cycle Detection Util
 * 环检测工具类
 *
 * <p>Utility class for detecting cycles in graphs.</p>
 * <p>图中环检测的工具类。</p>
 *
 * <p><strong>Algorithms | 算法:</strong></p>
 * <ul>
 *   <li>DFS coloring for directed graphs | 有向图的DFS着色算法</li>
 *   <li>Union-Find for undirected graphs | 无向图的并查集算法</li>
 * </ul>
 *
 * <p><strong>Time Complexity | 时间复杂度:</strong> O(V + E)</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>DFS coloring cycle detection for directed graphs - 有向图的DFS着色环检测</li>
 *   <li>Union-find cycle detection for undirected graphs - 无向图的并查集环检测</li>
 *   <li>Cycle path extraction - 环路径提取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if graph has a cycle
 * boolean hasCycle = CycleDetectionUtil.hasCycle(graph);
 *
 * // Find one cycle if exists
 * List<String> cycle = CycleDetectionUtil.findCycle(graph);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns false/empty for null inputs) - 空值安全: 是（null输入返回false/空）</li>
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
public final class CycleDetectionUtil {

    private CycleDetectionUtil() {
        // Utility class
    }

    /**
     * Check if graph contains a cycle
     * 检查图是否包含环
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if graph contains a cycle | 如果图包含环返回true
     */
    public static <V> boolean hasCycle(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return false;
        }

        if (graph.isDirected()) {
            return hasCycleDirected(graph);
        } else {
            return hasCycleUndirected(graph);
        }
    }

    /**
     * Check for cycle in directed graph using DFS coloring
     * 使用DFS着色检查有向图中的环
     */
    private static <V> boolean hasCycleDirected(Graph<V> graph) {
        // WHITE = 0, GRAY = 1, BLACK = 2
        Map<V, Integer> colors = new HashMap<>();
        for (V v : graph.vertices()) {
            colors.put(v, 0); // WHITE
        }

        for (V vertex : graph.vertices()) {
            if (colors.get(vertex) == 0) {
                if (dfsCycleCheck(graph, vertex, colors)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static <V> boolean dfsCycleCheck(Graph<V> graph, V vertex, Map<V, Integer> colors) {
        colors.put(vertex, 1); // GRAY - being processed

        for (V neighbor : graph.neighbors(vertex)) {
            int neighborColor = colors.get(neighbor);
            if (neighborColor == 1) {
                // Found a back edge - cycle detected
                return true;
            }
            if (neighborColor == 0 && dfsCycleCheck(graph, neighbor, colors)) {
                return true;
            }
        }

        colors.put(vertex, 2); // BLACK - finished
        return false;
    }

    /**
     * Check for cycle in undirected graph
     * 检查无向图中的环
     */
    private static <V> boolean hasCycleUndirected(Graph<V> graph) {
        Set<V> visited = new HashSet<>();

        for (V vertex : graph.vertices()) {
            if (!visited.contains(vertex)) {
                if (dfsCycleCheckUndirected(graph, vertex, null, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static <V> boolean dfsCycleCheckUndirected(Graph<V> graph, V vertex,
                                                        V parent, Set<V> visited) {
        visited.add(vertex);

        for (V neighbor : graph.neighbors(vertex)) {
            if (!visited.contains(neighbor)) {
                if (dfsCycleCheckUndirected(graph, neighbor, vertex, visited)) {
                    return true;
                }
            } else if (!neighbor.equals(parent)) {
                // Found a cycle
                return true;
            }
        }

        return false;
    }

    /**
     * Find one cycle if exists
     * 如果存在环则找到一个
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return list of vertices forming a cycle, or empty list if no cycle | 形成环的顶点列表，无环时返回空列表
     */
    public static <V> List<V> findCycle(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyList();
        }

        if (graph.isDirected()) {
            return findCycleDirected(graph);
        } else {
            return findCycleUndirected(graph);
        }
    }

    private static <V> List<V> findCycleDirected(Graph<V> graph) {
        Map<V, Integer> colors = new HashMap<>();
        Map<V, V> parent = new HashMap<>();

        for (V v : graph.vertices()) {
            colors.put(v, 0);
        }

        for (V vertex : graph.vertices()) {
            if (colors.get(vertex) == 0) {
                List<V> cycle = dfsFindCycle(graph, vertex, colors, parent);
                if (!cycle.isEmpty()) {
                    return cycle;
                }
            }
        }

        return Collections.emptyList();
    }

    private static <V> List<V> dfsFindCycle(Graph<V> graph, V vertex,
                                             Map<V, Integer> colors, Map<V, V> parent) {
        colors.put(vertex, 1);

        for (V neighbor : graph.neighbors(vertex)) {
            int neighborColor = colors.get(neighbor);

            if (neighborColor == 1) {
                // Found cycle - reconstruct it
                return reconstructCycle(vertex, neighbor, parent);
            }

            if (neighborColor == 0) {
                parent.put(neighbor, vertex);
                List<V> cycle = dfsFindCycle(graph, neighbor, colors, parent);
                if (!cycle.isEmpty()) {
                    return cycle;
                }
            }
        }

        colors.put(vertex, 2);
        return Collections.emptyList();
    }

    private static <V> List<V> reconstructCycle(V start, V cycleStart, Map<V, V> parent) {
        LinkedList<V> cycle = new LinkedList<>();
        cycle.addFirst(cycleStart);

        V current = start;
        while (current != null && !current.equals(cycleStart)) {
            cycle.addFirst(current);
            current = parent.get(current);
        }
        cycle.addFirst(cycleStart);

        return cycle;
    }

    private static <V> List<V> findCycleUndirected(Graph<V> graph) {
        Map<V, V> parent = new HashMap<>();
        Set<V> visited = new HashSet<>();

        for (V vertex : graph.vertices()) {
            if (!visited.contains(vertex)) {
                List<V> cycle = dfsFindCycleUndirected(graph, vertex, null, visited, parent);
                if (!cycle.isEmpty()) {
                    return cycle;
                }
            }
        }

        return Collections.emptyList();
    }

    private static <V> List<V> dfsFindCycleUndirected(Graph<V> graph, V vertex,
                                                       V parentVertex, Set<V> visited,
                                                       Map<V, V> parent) {
        visited.add(vertex);
        parent.put(vertex, parentVertex);

        for (V neighbor : graph.neighbors(vertex)) {
            if (!visited.contains(neighbor)) {
                List<V> cycle = dfsFindCycleUndirected(graph, neighbor, vertex, visited, parent);
                if (!cycle.isEmpty()) {
                    return cycle;
                }
            } else if (!neighbor.equals(parentVertex)) {
                // Found cycle
                return reconstructCycleUndirected(vertex, neighbor, parent);
            }
        }

        return Collections.emptyList();
    }

    private static <V> List<V> reconstructCycleUndirected(V start, V cycleVertex, Map<V, V> parent) {
        LinkedList<V> cycle = new LinkedList<>();
        cycle.addFirst(cycleVertex);

        V current = start;
        while (current != null && !current.equals(cycleVertex)) {
            cycle.addFirst(current);
            current = parent.get(current);
        }
        cycle.addFirst(cycleVertex);

        return cycle;
    }

    /**
     * Check if adding an edge would create a cycle
     * 检查添加边是否会创建环
     *
     * <p>For directed graphs, checks if there's a directed path from 'to' to 'from'.
     * For undirected graphs, checks if 'from' and 'to' are already connected.</p>
     * <p>对于有向图，检查是否存在从 'to' 到 'from' 的有向路径。
     * 对于无向图，检查 'from' 和 'to' 是否已连通。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param from source vertex | 源顶点
     * @param to target vertex | 目标顶点
     * @return true if adding the edge would create a cycle | 如果添加边会创建环返回true
     */
    public static <V> boolean wouldCreateCycle(Graph<V> graph, V from, V to) {
        if (graph == null || from == null || to == null) {
            return false;
        }
        if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
            return false;
        }
        if (from.equals(to)) {
            return true; // Self-loop
        }

        if (graph.isDirected()) {
            // For directed graphs: adding edge from->to creates cycle iff
            // there's a directed path from to->from
            return hasDirectedPath(graph, to, from);
        } else {
            // For undirected graphs: adding edge creates cycle iff
            // the vertices are already connected
            return ConnectedComponentsUtil.isConnected(graph, to, from);
        }
    }

    /**
     * Check if there's a directed path from source to target
     * 检查是否存在从源到目标的有向路径
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source source vertex | 源顶点
     * @param target target vertex | 目标顶点
     * @return true if a directed path exists | 如果存在有向路径返回true
     */
    private static <V> boolean hasDirectedPath(Graph<V> graph, V source, V target) {
        Set<V> visited = new HashSet<>();
        Queue<V> queue = new LinkedList<>();

        queue.offer(source);
        visited.add(source);

        while (!queue.isEmpty()) {
            V vertex = queue.poll();

            // Only follow outgoing edges (directed path)
            for (V neighbor : graph.neighbors(vertex)) {
                if (neighbor.equals(target)) {
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
}
