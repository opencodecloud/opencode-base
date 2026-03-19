package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;

import java.util.*;
import java.util.function.Consumer;

/**
 * Safe Graph Traversal Util
 * 安全图遍历工具类
 *
 * <p>Iterative implementations of graph traversal to avoid stack overflow.</p>
 * <p>图遍历的迭代实现，避免栈溢出。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Iterative DFS (no recursion) | 迭代式DFS（无递归）</li>
 *   <li>Depth-limited traversal | 深度限制遍历</li>
 *   <li>Suitable for large graphs | 适用于大图</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Iterative DFS for large graphs
 * List<String> result = SafeGraphTraversalUtil.dfsIterative(graph, "A");
 *
 * // Depth-limited DFS
 * List<String> limited = SafeGraphTraversalUtil.dfsWithLimit(graph, "A", 100);
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
public final class SafeGraphTraversalUtil {

    private SafeGraphTraversalUtil() {
        // Utility class
    }

    /**
     * Iterative Depth-First Search (avoids stack overflow)
     * 迭代式深度优先搜索（避免栈溢出）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param start the starting vertex | 起始顶点
     * @return list of vertices in DFS order | DFS顺序的顶点列表
     */
    public static <V> List<V> dfsIterative(Graph<V> graph, V start) {
        if (graph == null || start == null || !graph.containsVertex(start)) {
            return Collections.emptyList();
        }

        List<V> result = new ArrayList<>();
        Set<V> visited = new HashSet<>();
        Deque<V> stack = new ArrayDeque<>();

        stack.push(start);

        while (!stack.isEmpty()) {
            V vertex = stack.pop();

            if (visited.contains(vertex)) {
                continue;
            }

            visited.add(vertex);
            result.add(vertex);

            // Reverse order to maintain consistent traversal order
            List<V> neighbors = new ArrayList<>(graph.neighbors(vertex));
            Collections.reverse(neighbors);
            for (V neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }

        return result;
    }

    /**
     * Iterative DFS with visitor
     * 带访问器的迭代式DFS
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param start the starting vertex | 起始顶点
     * @param visitor the vertex visitor | 顶点访问器
     */
    public static <V> void dfsIterative(Graph<V> graph, V start, Consumer<V> visitor) {
        if (graph == null || start == null || visitor == null || !graph.containsVertex(start)) {
            return;
        }

        Set<V> visited = new HashSet<>();
        Deque<V> stack = new ArrayDeque<>();

        stack.push(start);

        while (!stack.isEmpty()) {
            V vertex = stack.pop();

            if (visited.contains(vertex)) {
                continue;
            }

            visited.add(vertex);
            visitor.accept(vertex);

            List<V> neighbors = new ArrayList<>(graph.neighbors(vertex));
            Collections.reverse(neighbors);
            for (V neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }
    }

    /**
     * Depth-limited DFS
     * 深度限制的DFS
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param start the starting vertex | 起始顶点
     * @param maxDepth maximum depth to traverse | 最大遍历深度
     * @return list of vertices within depth limit | 深度限制内的顶点列表
     */
    public static <V> List<V> dfsWithLimit(Graph<V> graph, V start, int maxDepth) {
        if (graph == null || start == null || !graph.containsVertex(start) || maxDepth < 0) {
            return Collections.emptyList();
        }

        List<V> result = new ArrayList<>();
        Set<V> visited = new HashSet<>();
        dfsLimitedHelper(graph, start, visited, result, 0, maxDepth);
        return result;
    }

    private static <V> void dfsLimitedHelper(Graph<V> graph, V vertex,
                                              Set<V> visited, List<V> result,
                                              int currentDepth, int maxDepth) {
        if (currentDepth > maxDepth) {
            return;
        }

        visited.add(vertex);
        result.add(vertex);

        for (V neighbor : graph.neighbors(vertex)) {
            if (!visited.contains(neighbor)) {
                dfsLimitedHelper(graph, neighbor, visited, result, currentDepth + 1, maxDepth);
            }
        }
    }

    /**
     * Iterative depth-limited DFS
     * 迭代式深度限制DFS
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param start the starting vertex | 起始顶点
     * @param maxDepth maximum depth to traverse | 最大遍历深度
     * @return list of vertices within depth limit | 深度限制内的顶点列表
     */
    public static <V> List<V> dfsIterativeWithLimit(Graph<V> graph, V start, int maxDepth) {
        if (graph == null || start == null || !graph.containsVertex(start) || maxDepth < 0) {
            return Collections.emptyList();
        }

        List<V> result = new ArrayList<>();
        Set<V> visited = new HashSet<>();
        Deque<VertexWithDepth<V>> stack = new ArrayDeque<>();

        stack.push(new VertexWithDepth<>(start, 0));

        while (!stack.isEmpty()) {
            VertexWithDepth<V> current = stack.pop();
            V vertex = current.vertex;
            int depth = current.depth;

            if (visited.contains(vertex) || depth > maxDepth) {
                continue;
            }

            visited.add(vertex);
            result.add(vertex);

            if (depth < maxDepth) {
                List<V> neighbors = new ArrayList<>(graph.neighbors(vertex));
                Collections.reverse(neighbors);
                for (V neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        stack.push(new VertexWithDepth<>(neighbor, depth + 1));
                    }
                }
            }
        }

        return result;
    }

    /**
     * BFS with maximum distance limit
     * 带最大距离限制的BFS
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param start the starting vertex | 起始顶点
     * @param maxDistance maximum distance from start | 距起始点的最大距离
     * @return list of vertices within distance limit | 距离限制内的顶点列表
     */
    public static <V> List<V> bfsWithLimit(Graph<V> graph, V start, int maxDistance) {
        if (graph == null || start == null || !graph.containsVertex(start) || maxDistance < 0) {
            return Collections.emptyList();
        }

        List<V> result = new ArrayList<>();
        Set<V> visited = new HashSet<>();
        Queue<VertexWithDepth<V>> queue = new LinkedList<>();

        queue.offer(new VertexWithDepth<>(start, 0));
        visited.add(start);

        while (!queue.isEmpty()) {
            VertexWithDepth<V> current = queue.poll();
            result.add(current.vertex);

            if (current.depth < maxDistance) {
                for (V neighbor : graph.neighbors(current.vertex)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.offer(new VertexWithDepth<>(neighbor, current.depth + 1));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Helper record for vertex with depth tracking
     */
    private record VertexWithDepth<V>(V vertex, int depth) {}
}
