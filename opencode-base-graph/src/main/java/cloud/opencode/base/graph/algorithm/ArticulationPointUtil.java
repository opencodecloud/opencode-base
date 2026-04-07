package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Articulation Point and Bridge Finder Utility
 * 割点与桥查找工具类
 *
 * <p>Finds articulation points (cut vertices) and bridges (cut edges) in a graph
 * using Tarjan's algorithm with iterative DFS to avoid stack overflow on large graphs.</p>
 * <p>使用Tarjan算法和迭代DFS在图中查找割点（关节点）和桥（割边），
 * 避免大图上的栈溢出。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Find all articulation points (cut vertices) - 查找所有割点</li>
 *   <li>Find all bridges (cut edges) - 查找所有桥</li>
 *   <li>Check if graph is biconnected - 检查图是否双连通</li>
 *   <li>Iterative DFS for stack safety - 迭代DFS保证栈安全</li>
 *   <li>Directed graphs treated as undirected - 有向图按无向图处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> graph = OpenGraph.undirected();
 * graph.addEdge("A", "B");
 * graph.addEdge("B", "C");
 *
 * Set<String> articulationPoints = ArticulationPointUtil.findArticulationPoints(graph);
 * // articulationPoints = {"B"}
 *
 * Set<Edge<String>> bridges = ArticulationPointUtil.findBridges(graph);
 * // bridges = {A->B, B->C}
 *
 * boolean biconnected = ArticulationPointUtil.isBiconnected(graph);
 * // biconnected = false
 * }</pre>
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
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public final class ArticulationPointUtil {

    private ArticulationPointUtil() {
        // Utility class
    }

    /**
     * Find all articulation points (cut vertices) in the graph.
     * 查找图中的所有割点（关节点）。
     *
     * <p>For directed graphs, edges are treated as undirected.</p>
     * <p>对于有向图，边按无向处理。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to analyze | 要分析的图
     * @return set of articulation points, or empty set if null graph | 割点集合，null图返回空集
     */
    public static <V> Set<V> findArticulationPoints(Graph<V> graph) {
        if (graph == null || graph.vertexCount() == 0) {
            return Collections.emptySet();
        }

        Map<V, Set<V>> adj = buildUndirectedAdjacency(graph);
        Set<V> result = new LinkedHashSet<>();
        Map<V, Integer> disc = new HashMap<>();
        Map<V, Integer> low = new HashMap<>();
        Map<V, V> parent = new HashMap<>();
        int[] timer = {0};

        for (V start : graph.vertices()) {
            if (!disc.containsKey(start)) {
                tarjanArticulationIterative(start, adj, disc, low, parent, timer, result);
            }
        }

        return Collections.unmodifiableSet(result);
    }

    /**
     * Find all bridges (cut edges) in the graph.
     * 查找图中的所有桥（割边）。
     *
     * <p>For directed graphs, edges are treated as undirected.</p>
     * <p>对于有向图，边按无向处理。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to analyze | 要分析的图
     * @return set of bridge edges, or empty set if null graph | 桥边集合，null图返回空集
     */
    public static <V> Set<Edge<V>> findBridges(Graph<V> graph) {
        if (graph == null || graph.vertexCount() == 0) {
            return Collections.emptySet();
        }

        Map<V, Set<V>> adj = buildUndirectedAdjacency(graph);
        Set<Edge<V>> result = new LinkedHashSet<>();
        Map<V, Integer> disc = new HashMap<>();
        Map<V, Integer> low = new HashMap<>();
        Map<V, V> parent = new HashMap<>();
        int[] timer = {0};

        for (V start : graph.vertices()) {
            if (!disc.containsKey(start)) {
                tarjanBridgeIterative(start, adj, disc, low, parent, timer, result);
            }
        }

        return Collections.unmodifiableSet(result);
    }

    /**
     * Check if the graph is biconnected.
     * 检查图是否双连通。
     *
     * <p>A graph is biconnected if it is connected, has at least 2 vertices,
     * and has no articulation points.</p>
     * <p>如果图是连通的、至少有2个顶点且没有割点，则图是双连通的。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to check | 要检查的图
     * @return true if graph is biconnected | 如果图是双连通的返回true
     */
    public static <V> boolean isBiconnected(Graph<V> graph) {
        if (graph == null || graph.vertexCount() < 2) {
            return false;
        }

        // Check connectivity using BFS on undirected view
        Map<V, Set<V>> adj = buildUndirectedAdjacency(graph);
        V start = graph.vertices().iterator().next();
        Set<V> visited = new HashSet<>();
        Deque<V> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            V v = queue.poll();
            for (V neighbor : adj.getOrDefault(v, Collections.emptySet())) {
                if (visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        if (visited.size() != graph.vertexCount()) {
            return false;
        }

        // No articulation points
        return findArticulationPoints(graph).isEmpty();
    }

    /**
     * Build undirected adjacency map from any graph (directed or undirected).
     * 从任意图（有向或无向）构建无向邻接映射。
     */
    private static <V> Map<V, Set<V>> buildUndirectedAdjacency(Graph<V> graph) {
        Map<V, Set<V>> adj = new HashMap<>();
        for (V v : graph.vertices()) {
            adj.put(v, new LinkedHashSet<>());
        }
        for (Edge<V> edge : graph.edges()) {
            V from = edge.from();
            V to = edge.to();
            if (!from.equals(to)) { // skip self-loops
                adj.get(from).add(to);
                adj.get(to).add(from);
            }
        }
        return adj;
    }

    /**
     * Iterative Tarjan's algorithm for finding articulation points.
     * 查找割点的迭代Tarjan算法。
     *
     * <p>Uses an explicit stack of DFS frames to simulate recursion.</p>
     * <p>使用显式DFS帧栈来模拟递归。</p>
     */
    private static <V> void tarjanArticulationIterative(
            V start, Map<V, Set<V>> adj,
            Map<V, Integer> disc, Map<V, Integer> low,
            Map<V, V> parent, int[] timer,
            Set<V> articulationPoints) {

        // Stack frame: [vertex, iterator over neighbors, child count in DFS tree]
        Deque<Object[]> stack = new ArrayDeque<>();
        Map<V, Integer> childCount = new HashMap<>();

        disc.put(start, timer[0]);
        low.put(start, timer[0]);
        timer[0]++;
        childCount.put(start, 0);

        Set<V> neighbors = adj.getOrDefault(start, Collections.emptySet());
        stack.push(new Object[]{start, neighbors.iterator()});

        while (!stack.isEmpty()) {
            Object[] frame = stack.peek();
            @SuppressWarnings("unchecked")
            V u = (V) frame[0];
            @SuppressWarnings("unchecked")
            Iterator<V> iter = (Iterator<V>) frame[1];

            if (iter.hasNext()) {
                V v = iter.next();

                if (!disc.containsKey(v)) {
                    // Tree edge: u -> v
                    parent.put(v, u);
                    childCount.merge(u, 1, Integer::sum);
                    disc.put(v, timer[0]);
                    low.put(v, timer[0]);
                    timer[0]++;
                    childCount.put(v, 0);

                    Set<V> vNeighbors = adj.getOrDefault(v, Collections.emptySet());
                    stack.push(new Object[]{v, vNeighbors.iterator()});
                } else if (!v.equals(parent.get(u))) {
                    // Back edge: update low-link
                    low.put(u, Math.min(low.get(u), disc.get(v)));
                }
            } else {
                // Finished processing u, backtrack
                stack.pop();

                if (!stack.isEmpty()) {
                    Object[] parentFrame = stack.peek();
                    @SuppressWarnings("unchecked")
                    V p = (V) parentFrame[0];

                    // Update parent's low-link value
                    low.put(p, Math.min(low.get(p), low.get(u)));

                    // Non-root articulation point: low[u] >= disc[p]
                    if (parent.containsKey(p) && low.get(u) >= disc.get(p)) {
                        articulationPoints.add(p);
                    }
                } else {
                    // u is root of DFS tree
                    if (childCount.getOrDefault(u, 0) >= 2) {
                        articulationPoints.add(u);
                    }
                }
            }
        }
    }

    /**
     * Iterative Tarjan's algorithm for finding bridges.
     * 查找桥的迭代Tarjan算法。
     *
     * <p>Uses an explicit stack of DFS frames to simulate recursion.</p>
     * <p>使用显式DFS帧栈来模拟递归。</p>
     */
    private static <V> void tarjanBridgeIterative(
            V start, Map<V, Set<V>> adj,
            Map<V, Integer> disc, Map<V, Integer> low,
            Map<V, V> parent, int[] timer,
            Set<Edge<V>> bridges) {

        Deque<Object[]> stack = new ArrayDeque<>();

        disc.put(start, timer[0]);
        low.put(start, timer[0]);
        timer[0]++;

        Set<V> neighbors = adj.getOrDefault(start, Collections.emptySet());
        stack.push(new Object[]{start, neighbors.iterator()});

        while (!stack.isEmpty()) {
            Object[] frame = stack.peek();
            @SuppressWarnings("unchecked")
            V u = (V) frame[0];
            @SuppressWarnings("unchecked")
            Iterator<V> iter = (Iterator<V>) frame[1];

            if (iter.hasNext()) {
                V v = iter.next();

                if (!disc.containsKey(v)) {
                    // Tree edge
                    parent.put(v, u);
                    disc.put(v, timer[0]);
                    low.put(v, timer[0]);
                    timer[0]++;

                    Set<V> vNeighbors = adj.getOrDefault(v, Collections.emptySet());
                    stack.push(new Object[]{v, vNeighbors.iterator()});
                } else if (!v.equals(parent.get(u))) {
                    // Back edge
                    low.put(u, Math.min(low.get(u), disc.get(v)));
                }
            } else {
                // Finished processing u, backtrack
                stack.pop();

                if (!stack.isEmpty()) {
                    Object[] parentFrame = stack.peek();
                    @SuppressWarnings("unchecked")
                    V p = (V) parentFrame[0];

                    // Update parent's low-link
                    low.put(p, Math.min(low.get(p), low.get(u)));

                    // Bridge condition: low[u] > disc[p]
                    if (low.get(u) > disc.get(p)) {
                        bridges.add(new Edge<>(p, u));
                    }
                }
            }
        }
    }
}
