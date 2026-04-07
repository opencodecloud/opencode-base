package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Bipartite Graph Detection Utility
 * 二部图检测工具类
 *
 * <p>Determines whether a graph is bipartite using BFS 2-coloring,
 * and computes the vertex partition or provides an odd-cycle witness.</p>
 * <p>使用BFS双着色判断图是否为二部图，并计算顶点分区或提供奇数环证据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Check if a graph is bipartite - 检查图是否为二部图</li>
 *   <li>Compute left/right vertex partition - 计算左/右顶点分区</li>
 *   <li>Return odd cycle witness when not bipartite - 非二部图时返回奇数环证据</li>
 *   <li>Handle disconnected components - 处理非连通分量</li>
 *   <li>Directed graphs treated as undirected - 有向图按无向图处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> graph = OpenGraph.undirected();
 * graph.addEdge("A", "B");
 * graph.addEdge("B", "C");
 * graph.addEdge("C", "D");
 * graph.addEdge("D", "A");
 *
 * boolean bipartite = BipartiteUtil.isBipartite(graph);  // true (even cycle)
 *
 * BipartiteUtil.BipartiteResult<String> result = BipartiteUtil.partition(graph);
 * Set<String> left = result.left();   // e.g., {"A", "C"}
 * Set<String> right = result.right(); // e.g., {"B", "D"}
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
public final class BipartiteUtil {

    private BipartiteUtil() {
        // Utility class
    }

    /**
     * Result of bipartite check containing partition sets or odd cycle witness.
     * 二部图检查结果，包含分区集合或奇数环证据。
     *
     * @param bipartite whether the graph is bipartite | 图是否为二部图
     * @param left the left partition (empty if not bipartite) | 左分区（非二部图时为空）
     * @param right the right partition (empty if not bipartite) | 右分区（非二部图时为空）
     * @param oddCycle the odd cycle witness (empty if bipartite) | 奇数环证据（二部图时为空）
     * @param <V> the vertex type | 顶点类型
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-graph V1.0.3
     */
    public record BipartiteResult<V>(boolean bipartite, Set<V> left, Set<V> right, List<V> oddCycle) {

        /**
         * Create a bipartite result with left and right partitions.
         * 创建包含左右分区的二部图结果。
         *
         * @param <V> the vertex type | 顶点类型
         * @param left the left partition | 左分区
         * @param right the right partition | 右分区
         * @return bipartite result | 二部图结果
         */
        public static <V> BipartiteResult<V> ofBipartite(Set<V> left, Set<V> right) {
            return new BipartiteResult<>(true, Set.copyOf(left), Set.copyOf(right), List.of());
        }

        /**
         * Create a not-bipartite result with an odd cycle witness.
         * 创建包含奇数环证据的非二部图结果。
         *
         * @param <V> the vertex type | 顶点类型
         * @param oddCycle the odd cycle witness | 奇数环证据
         * @return not-bipartite result | 非二部图结果
         */
        public static <V> BipartiteResult<V> ofNotBipartite(List<V> oddCycle) {
            return new BipartiteResult<>(false, Set.of(), Set.of(), List.copyOf(oddCycle));
        }
    }

    /**
     * Check if the graph is bipartite.
     * 检查图是否为二部图。
     *
     * <p>For directed graphs, edges are treated as undirected.</p>
     * <p>对于有向图，边按无向处理。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to check | 要检查的图
     * @return true if the graph is bipartite | 如果图是二部图返回true
     */
    public static <V> boolean isBipartite(Graph<V> graph) {
        if (graph == null || graph.vertexCount() == 0) {
            return true;
        }
        return partition(graph).bipartite();
    }

    /**
     * Compute the bipartite partition or find an odd cycle witness.
     * 计算二部图分区或查找奇数环证据。
     *
     * <p>For directed graphs, edges are treated as undirected.
     * Handles disconnected components by iterating over all unvisited vertices.</p>
     * <p>对于有向图，边按无向处理。通过遍历所有未访问顶点来处理非连通分量。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to partition | 要分区的图
     * @return bipartite result with partition or odd cycle | 包含分区或奇数环的二部图结果
     */
    public static <V> BipartiteResult<V> partition(Graph<V> graph) {
        if (graph == null || graph.vertexCount() == 0) {
            return BipartiteResult.ofBipartite(Set.of(), Set.of());
        }

        Map<V, Set<V>> adj = buildUndirectedAdjacency(graph);
        Map<V, Integer> color = new HashMap<>(); // 0 = left, 1 = right
        Map<V, V> parentMap = new HashMap<>();
        Set<V> left = new LinkedHashSet<>();
        Set<V> right = new LinkedHashSet<>();

        for (V start : graph.vertices()) {
            if (color.containsKey(start)) {
                continue;
            }

            // BFS 2-coloring for this component
            Deque<V> queue = new ArrayDeque<>();
            color.put(start, 0);
            left.add(start);
            parentMap.put(start, null);
            queue.add(start);

            while (!queue.isEmpty()) {
                V u = queue.poll();
                int uColor = color.get(u);
                int neighborColor = 1 - uColor;

                for (V v : adj.getOrDefault(u, Collections.emptySet())) {
                    if (!color.containsKey(v)) {
                        color.put(v, neighborColor);
                        parentMap.put(v, u);
                        if (neighborColor == 0) {
                            left.add(v);
                        } else {
                            right.add(v);
                        }
                        queue.add(v);
                    } else if (color.get(v).intValue() == uColor) {
                        // Same color conflict => odd cycle
                        List<V> oddCycle = reconstructOddCycle(u, v, parentMap);
                        return BipartiteResult.ofNotBipartite(oddCycle);
                    }
                }
            }
        }

        return BipartiteResult.ofBipartite(left, right);
    }

    /**
     * Build undirected adjacency map from any graph.
     * 从任意图构建无向邻接映射。
     */
    private static <V> Map<V, Set<V>> buildUndirectedAdjacency(Graph<V> graph) {
        Map<V, Set<V>> adj = new HashMap<>();
        for (V v : graph.vertices()) {
            adj.put(v, new LinkedHashSet<>());
        }
        for (Edge<V> edge : graph.edges()) {
            V from = edge.from();
            V to = edge.to();
            if (!from.equals(to)) {
                adj.get(from).add(to);
                adj.get(to).add(from);
            }
        }
        return adj;
    }

    /**
     * Reconstruct the odd cycle from the BFS parent map when u-v conflict is found.
     * 当发现u-v冲突时，从BFS父映射中重建奇数环。
     *
     * <p>Finds the lowest common ancestor (LCA) of u and v in the BFS tree,
     * then constructs the cycle: path from LCA to u + edge u-v + path from v to LCA.</p>
     */
    private static <V> List<V> reconstructOddCycle(V u, V v, Map<V, V> parentMap) {
        int maxDepth = parentMap.size() + 1;

        // Collect ancestors of u (bounded by maxDepth to guard against corrupt parent maps)
        Set<V> uAncestors = new LinkedHashSet<>();
        V current = u;
        int steps = 0;
        while (current != null && steps++ < maxDepth) {
            uAncestors.add(current);
            current = parentMap.get(current);
        }

        // Find LCA by walking up from v (bounded by maxDepth)
        V lca = v;
        steps = 0;
        while (lca != null && !uAncestors.contains(lca) && steps++ < maxDepth) {
            lca = parentMap.get(lca);
        }
        if (lca == null) {
            // Fallback: shouldn't happen in connected component
            return List.of(u, v, u);
        }

        // Build cycle: lca -> ... -> u -> v -> ... -> lca
        LinkedList<V> pathU = new LinkedList<>();
        current = u;
        steps = 0;
        while (!current.equals(lca) && steps++ < maxDepth) {
            pathU.addFirst(current);
            current = parentMap.get(current);
        }
        pathU.addFirst(lca);

        List<V> pathV = new ArrayList<>();
        current = v;
        steps = 0;
        while (!current.equals(lca) && steps++ < maxDepth) {
            pathV.add(current);
            current = parentMap.get(current);
        }

        List<V> cycle = new ArrayList<>(pathU);
        cycle.addAll(pathV);
        cycle.add(lca); // close the cycle

        return cycle;
    }
}
