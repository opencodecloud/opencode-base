package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.exception.GraphLimitExceededException;

import java.util.*;
import java.util.function.Consumer;

/**
 * Graph Traversal Util
 * 图遍历工具类
 *
 * <p>Utility class for graph traversal algorithms (BFS/DFS).</p>
 * <p>图遍历算法（BFS/DFS）的工具类。</p>
 *
 * <p><strong>Algorithms | 算法:</strong></p>
 * <ul>
 *   <li>BFS - Breadth-First Search | 广度优先搜索</li>
 *   <li>DFS - Depth-First Search | 深度优先搜索</li>
 * </ul>
 *
 * <p><strong>Time Complexity | 时间复杂度:</strong> O(V + E)</p>
 * <p><strong>Space Complexity | 空间复杂度:</strong> O(V)</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Breadth-first search (BFS) traversal - 广度优先搜索遍历</li>
 *   <li>Depth-first search (DFS) traversal - 深度优先搜索遍历</li>
 *   <li>Visitor callback support - 访问者回调支持</li>
 *   <li>Configurable max depth to prevent stack overflow - 可配置最大深度防止栈溢出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<String> bfsResult = GraphTraversalUtil.bfs(graph, "A");
 * List<String> dfsResult = GraphTraversalUtil.dfs(graph, "A");
 *
 * // With visitor
 * GraphTraversalUtil.bfs(graph, "A", vertex -> System.out.println(vertex));
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
public final class GraphTraversalUtil {

    /**
     * Default maximum recursion depth for DFS to prevent StackOverflowError.
     * DFS 的默认最大递归深度，用于防止 StackOverflowError。
     */
    private static final int DEFAULT_MAX_DEPTH = 10_000;

    private GraphTraversalUtil() {
        // Utility class
    }

    /**
     * Breadth-First Search
     * 广度优先搜索
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param start the starting vertex | 起始顶点
     * @return list of vertices in BFS order | BFS顺序的顶点列表
     */
    public static <V> List<V> bfs(Graph<V> graph, V start) {
        if (graph == null || start == null || !graph.containsVertex(start)) {
            return Collections.emptyList();
        }

        List<V> result = new ArrayList<>();
        Set<V> visited = new HashSet<>();
        Queue<V> queue = new LinkedList<>();

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            V vertex = queue.poll();
            result.add(vertex);

            for (V neighbor : graph.neighbors(vertex)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return result;
    }

    /**
     * Breadth-First Search with visitor
     * 带访问器的广度优先搜索
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param start the starting vertex | 起始顶点
     * @param visitor the vertex visitor | 顶点访问器
     */
    public static <V> void bfs(Graph<V> graph, V start, Consumer<V> visitor) {
        if (graph == null || start == null || visitor == null || !graph.containsVertex(start)) {
            return;
        }

        Set<V> visited = new HashSet<>();
        Queue<V> queue = new LinkedList<>();

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            V vertex = queue.poll();
            visitor.accept(vertex);

            for (V neighbor : graph.neighbors(vertex)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
    }

    /**
     * Depth-First Search (iterative)
     * 深度优先搜索（迭代）
     *
     * <p>Uses an explicit stack instead of recursion to avoid StackOverflowError on deep graphs.</p>
     * <p>使用显式栈代替递归，避免深层图上的 StackOverflowError。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param start the starting vertex | 起始顶点
     * @return list of vertices in DFS order | DFS顺序的顶点列表
     */
    public static <V> List<V> dfs(Graph<V> graph, V start) {
        if (graph == null || start == null || !graph.containsVertex(start)) {
            return Collections.emptyList();
        }

        List<V> result = new ArrayList<>();
        Set<V> visited = new HashSet<>();
        Deque<V> stack = new ArrayDeque<>();

        stack.push(start);

        while (!stack.isEmpty()) {
            if (stack.size() > DEFAULT_MAX_DEPTH) {
                throw new GraphLimitExceededException(
                        "DFS depth exceeded maximum limit | DFS深度超出最大限制",
                        DEFAULT_MAX_DEPTH, stack.size());
            }

            V vertex = stack.pop();
            if (visited.contains(vertex)) {
                continue;
            }
            visited.add(vertex);
            result.add(vertex);

            // Push neighbors in reverse order to maintain consistent traversal order
            List<V> neighbors = new ArrayList<>();
            for (V neighbor : graph.neighbors(vertex)) {
                if (!visited.contains(neighbor)) {
                    neighbors.add(neighbor);
                }
            }
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                stack.push(neighbors.get(i));
            }
        }

        return result;
    }

    /**
     * Internal recursive DFS helper used by dfsAll.
     * dfsAll 使用的内部递归 DFS 辅助方法。
     */
    private static <V> void dfsHelper(Graph<V> graph, V start, Set<V> visited, List<V> result, int depthIgnored) {
        Deque<V> stack = new ArrayDeque<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            if (stack.size() > DEFAULT_MAX_DEPTH) {
                throw new GraphLimitExceededException(
                        "DFS depth exceeded maximum limit | DFS深度超出最大限制",
                        DEFAULT_MAX_DEPTH, stack.size());
            }

            V vertex = stack.pop();
            if (visited.contains(vertex)) {
                continue;
            }
            visited.add(vertex);
            result.add(vertex);

            List<V> neighbors = new ArrayList<>();
            for (V neighbor : graph.neighbors(vertex)) {
                if (!visited.contains(neighbor)) {
                    neighbors.add(neighbor);
                }
            }
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                stack.push(neighbors.get(i));
            }
        }
    }

    /**
     * Depth-First Search with visitor (iterative)
     * 带访问器的深度优先搜索（迭代）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param start the starting vertex | 起始顶点
     * @param visitor the vertex visitor | 顶点访问器
     */
    public static <V> void dfs(Graph<V> graph, V start, Consumer<V> visitor) {
        if (graph == null || start == null || visitor == null || !graph.containsVertex(start)) {
            return;
        }

        Set<V> visited = new HashSet<>();
        Deque<V> stack = new ArrayDeque<>();

        stack.push(start);

        while (!stack.isEmpty()) {
            if (stack.size() > DEFAULT_MAX_DEPTH) {
                throw new GraphLimitExceededException(
                        "DFS depth exceeded maximum limit | DFS深度超出最大限制",
                        DEFAULT_MAX_DEPTH, stack.size());
            }

            V vertex = stack.pop();
            if (visited.contains(vertex)) {
                continue;
            }
            visited.add(vertex);
            visitor.accept(vertex);

            List<V> neighbors = new ArrayList<>();
            for (V neighbor : graph.neighbors(vertex)) {
                if (!visited.contains(neighbor)) {
                    neighbors.add(neighbor);
                }
            }
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                stack.push(neighbors.get(i));
            }
        }
    }

    /**
     * Traverse all vertices (handles disconnected components)
     * 遍历所有顶点（处理不连通分量）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return list of all vertices in BFS order | BFS顺序的所有顶点列表
     */
    public static <V> List<V> bfsAll(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyList();
        }

        List<V> result = new ArrayList<>();
        Set<V> visited = new HashSet<>();

        for (V vertex : graph.vertices()) {
            if (!visited.contains(vertex)) {
                Queue<V> queue = new LinkedList<>();
                queue.offer(vertex);
                visited.add(vertex);

                while (!queue.isEmpty()) {
                    V current = queue.poll();
                    result.add(current);

                    for (V neighbor : graph.neighbors(current)) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.offer(neighbor);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Traverse all vertices using DFS (handles disconnected components)
     * 使用DFS遍历所有顶点（处理不连通分量）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return list of all vertices in DFS order | DFS顺序的所有顶点列表
     */
    public static <V> List<V> dfsAll(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyList();
        }

        List<V> result = new ArrayList<>();
        Set<V> visited = new HashSet<>();

        for (V vertex : graph.vertices()) {
            if (!visited.contains(vertex)) {
                dfsHelper(graph, vertex, visited, result, 0);
            }
        }

        return result;
    }
}
