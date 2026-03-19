package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Minimum Spanning Tree Util
 * 最小生成树工具类
 *
 * <p>Utility class for minimum spanning tree algorithms.</p>
 * <p>最小生成树算法的工具类。</p>
 *
 * <p><strong>Algorithms | 算法:</strong></p>
 * <ul>
 *   <li>Prim - O((V+E)logV) with priority queue | 使用优先队列 O((V+E)logV)</li>
 *   <li>Kruskal - O(ElogE) with union-find | 使用并查集 O(ElogE)</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Prim's algorithm with priority queue - 使用优先队列的Prim算法</li>
 *   <li>Kruskal's algorithm with union-find - 使用并查集的Kruskal算法</li>
 *   <li>MST total weight calculation - 最小生成树总权重计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get MST using Prim's algorithm
 * Set<Edge<String>> mstPrim = MinimumSpanningTreeUtil.prim(graph, "A");
 *
 * // Get MST using Kruskal's algorithm
 * Set<Edge<String>> mstKruskal = MinimumSpanningTreeUtil.kruskal(graph);
 *
 * // Get total weight
 * double weight = MinimumSpanningTreeUtil.mstWeight(graph);
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
 *   <li>Time complexity: O(E log E) - 时间复杂度: O(E log E)</li>
 *   <li>Space complexity: O(V + E) - 空间复杂度: O(V + E)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class MinimumSpanningTreeUtil {

    private MinimumSpanningTreeUtil() {
        // Utility class
    }

    // ==================== Prim's Algorithm | Prim算法 ====================

    /**
     * Find minimum spanning tree using Prim's algorithm
     * 使用Prim算法查找最小生成树
     *
     * <p>Starts from the given vertex and greedily adds the minimum weight edge
     * that connects a vertex in the tree to a vertex outside the tree.</p>
     * <p>从给定顶点开始，贪婪地添加连接树内顶点和树外顶点的最小权重边。</p>
     *
     * <p><strong>Time Complexity | 时间复杂度:</strong> O((V+E)logV)</p>
     * <p><strong>Space Complexity | 空间复杂度:</strong> O(V+E)</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the undirected graph | 无向图
     * @param start the starting vertex | 起始顶点
     * @return set of edges in the MST | 最小生成树的边集合
     */
    public static <V> Set<Edge<V>> prim(Graph<V> graph, V start) {
        if (graph == null || start == null || !graph.containsVertex(start)) {
            return Collections.emptySet();
        }
        if (graph.vertexCount() < 2) {
            return Collections.emptySet();
        }

        Set<Edge<V>> mst = new HashSet<>();
        Set<V> inTree = new HashSet<>();
        PriorityQueue<Edge<V>> pq = new PriorityQueue<>(
            Comparator.comparingDouble(Edge::weight)
        );

        // Start from the given vertex
        inTree.add(start);
        pq.addAll(graph.outEdges(start));

        while (!pq.isEmpty() && inTree.size() < graph.vertexCount()) {
            Edge<V> minEdge = pq.poll();
            V to = minEdge.to();

            if (inTree.contains(to)) {
                continue;
            }

            // Add edge to MST
            mst.add(minEdge);
            inTree.add(to);

            // Add all edges from the new vertex
            for (Edge<V> edge : graph.outEdges(to)) {
                if (!inTree.contains(edge.to())) {
                    pq.offer(edge);
                }
            }
        }

        return mst;
    }

    /**
     * Find minimum spanning tree using Prim's algorithm (auto-select start)
     * 使用Prim算法查找最小生成树（自动选择起点）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the undirected graph | 无向图
     * @return set of edges in the MST | 最小生成树的边集合
     */
    public static <V> Set<Edge<V>> prim(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptySet();
        }
        V start = graph.vertices().iterator().next();
        return prim(graph, start);
    }

    // ==================== Kruskal's Algorithm | Kruskal算法 ====================

    /**
     * Find minimum spanning tree using Kruskal's algorithm
     * 使用Kruskal算法查找最小生成树
     *
     * <p>Sorts all edges by weight and greedily adds edges that don't create a cycle.</p>
     * <p>按权重排序所有边，贪婪地添加不会创建环的边。</p>
     *
     * <p><strong>Time Complexity | 时间复杂度:</strong> O(ElogE)</p>
     * <p><strong>Space Complexity | 空间复杂度:</strong> O(V)</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the undirected graph | 无向图
     * @return set of edges in the MST | 最小生成树的边集合
     */
    public static <V> Set<Edge<V>> kruskal(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptySet();
        }
        if (graph.vertexCount() < 2) {
            return Collections.emptySet();
        }

        Set<Edge<V>> mst = new HashSet<>();
        UnionFind<V> uf = new UnionFind<>(graph.vertices());

        // Get all edges and sort by weight
        List<Edge<V>> sortedEdges = new ArrayList<>(getUniqueEdges(graph));
        sortedEdges.sort(Comparator.comparingDouble(Edge::weight));

        for (Edge<V> edge : sortedEdges) {
            V from = edge.from();
            V to = edge.to();

            // Check if adding this edge would create a cycle
            if (!uf.connected(from, to)) {
                uf.union(from, to);
                mst.add(edge);

                // MST is complete when we have V-1 edges
                if (mst.size() == graph.vertexCount() - 1) {
                    break;
                }
            }
        }

        return mst;
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Calculate the total weight of the minimum spanning tree
     * 计算最小生成树的总权重
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the undirected graph | 无向图
     * @return total weight of MST | 最小生成树的总权重
     */
    public static <V> double mstWeight(Graph<V> graph) {
        Set<Edge<V>> mst = kruskal(graph);
        return mst.stream().mapToDouble(Edge::weight).sum();
    }

    /**
     * Calculate the total weight of a set of edges
     * 计算边集合的总权重
     *
     * @param <V> the vertex type | 顶点类型
     * @param edges the set of edges | 边集合
     * @return total weight | 总权重
     */
    public static <V> double totalWeight(Set<Edge<V>> edges) {
        if (edges == null) {
            return 0.0;
        }
        return edges.stream().mapToDouble(Edge::weight).sum();
    }

    /**
     * Check if a graph has a spanning tree (is connected)
     * 检查图是否有生成树（是否连通）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if graph is connected | 如果图连通返回true
     */
    public static <V> boolean hasSpanningTree(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return false;
        }
        if (graph.vertexCount() == 1) {
            return true;
        }
        Set<Edge<V>> mst = kruskal(graph);
        return mst.size() == graph.vertexCount() - 1;
    }

    /**
     * Find minimum spanning forest for disconnected graph
     * 为不连通图查找最小生成森林
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return set of edges in the minimum spanning forest | 最小生成森林的边集合
     */
    public static <V> Set<Edge<V>> minimumSpanningForest(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Edge<V>> forest = new HashSet<>();
        UnionFind<V> uf = new UnionFind<>(graph.vertices());

        // Sort all edges
        List<Edge<V>> sortedEdges = new ArrayList<>(getUniqueEdges(graph));
        sortedEdges.sort(Comparator.comparingDouble(Edge::weight));

        // Add edges that don't create cycles
        for (Edge<V> edge : sortedEdges) {
            V from = edge.from();
            V to = edge.to();

            if (!uf.connected(from, to)) {
                uf.union(from, to);
                forest.add(edge);
            }
        }

        return forest;
    }

    /**
     * Get the count of connected components after MST/MSF construction
     * 获取MST/MSF构建后的连通分量数
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return number of connected components | 连通分量数
     */
    public static <V> int componentCount(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return 0;
        }
        Set<Edge<V>> forest = minimumSpanningForest(graph);
        // Component count = V - E in forest
        return graph.vertexCount() - forest.size();
    }

    // ==================== Helper Methods | 辅助方法 ====================

    /**
     * Get unique edges (for undirected graph, only count each edge once).
     * Uses vertex identity (not toString) for deduplication.
     */
    private static <V> Set<Edge<V>> getUniqueEdges(Graph<V> graph) {
        Set<Edge<V>> uniqueEdges = new HashSet<>();
        Set<VertexPair<V>> seen = new HashSet<>();

        for (Edge<V> edge : graph.edges()) {
            VertexPair<V> pair = new VertexPair<>(edge.from(), edge.to());
            if (seen.add(pair)) {
                uniqueEdges.add(edge);
            }
        }

        return uniqueEdges;
    }

    /**
     * Unordered vertex pair for edge deduplication.
     * Treats (A,B) and (B,A) as equal.
     */
    private record VertexPair<V>(V a, V b) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof VertexPair<?> that)) return false;
            return (Objects.equals(a, that.a) && Objects.equals(b, that.b))
                || (Objects.equals(a, that.b) && Objects.equals(b, that.a));
        }

        @Override
        public int hashCode() {
            // Commutative hash: order-independent
            return Objects.hashCode(a) + Objects.hashCode(b);
        }
    }

    // ==================== Union-Find Data Structure | 并查集数据结构 ====================

    /**
     * Union-Find (Disjoint Set Union) data structure
     * 并查集数据结构
     *
     * <p>Supports path compression and union by rank for optimal performance.</p>
     * <p>支持路径压缩和按秩合并以获得最佳性能。</p>
     *
     * @param <V> the vertex type | 顶点类型
     */
    public static class UnionFind<V> {
        private final Map<V, V> parent;
        private final Map<V, Integer> rank;

        /**
         * Create a union-find structure for the given vertices
         * 为给定顶点创建并查集结构
         *
         * @param vertices the vertices | 顶点集合
         */
        public UnionFind(Set<V> vertices) {
            parent = new HashMap<>();
            rank = new HashMap<>();
            for (V v : vertices) {
                parent.put(v, v);
                rank.put(v, 0);
            }
        }

        /**
         * Find the root of a vertex (with path compression)
         * 查找顶点的根（带路径压缩）
         *
         * @param v the vertex | 顶点
         * @return the root vertex | 根顶点
         */
        public V find(V v) {
            if (!parent.get(v).equals(v)) {
                parent.put(v, find(parent.get(v))); // Path compression
            }
            return parent.get(v);
        }

        /**
         * Union two vertices (by rank)
         * 合并两个顶点（按秩合并）
         *
         * @param v1 first vertex | 第一个顶点
         * @param v2 second vertex | 第二个顶点
         */
        public void union(V v1, V v2) {
            V root1 = find(v1);
            V root2 = find(v2);

            if (root1.equals(root2)) {
                return;
            }

            // Union by rank
            int rank1 = rank.get(root1);
            int rank2 = rank.get(root2);

            if (rank1 < rank2) {
                parent.put(root1, root2);
            } else if (rank1 > rank2) {
                parent.put(root2, root1);
            } else {
                parent.put(root2, root1);
                rank.put(root1, rank1 + 1);
            }
        }

        /**
         * Check if two vertices are connected
         * 检查两个顶点是否连通
         *
         * @param v1 first vertex | 第一个顶点
         * @param v2 second vertex | 第二个顶点
         * @return true if connected | 如果连通返回true
         */
        public boolean connected(V v1, V v2) {
            return find(v1).equals(find(v2));
        }
    }
}
