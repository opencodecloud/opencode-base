/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.DirectedGraph;
import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.UndirectedGraph;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;
import java.util.function.Predicate;

/**
 * Subgraph Util - Subgraph Extraction and Operations
 * 子图工具类 - 子图提取和操作
 *
 * <p>Utility class for creating subgraphs from existing graphs through
 * various selection criteria.</p>
 * <p>通过各种选择条件从现有图创建子图的工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Vertex-induced subgraph - 顶点诱导子图</li>
 *   <li>Edge-induced subgraph - 边诱导子图</li>
 *   <li>Filter by vertex/edge predicates - 按顶点/边谓词过滤</li>
 *   <li>K-hop neighborhood extraction - K跳邻域提取</li>
 *   <li>Graph union/intersection/difference - 图的并集/交集/差集</li>
 *   <li>Reverse graph - 反转图</li>
 *   <li>Ego network extraction - 自我网络提取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Vertex-induced subgraph
 * Graph<String> sub = SubgraphUtil.induced(graph, Set.of("A", "B", "C"));
 *
 * // Filter vertices by predicate
 * Graph<String> filtered = SubgraphUtil.filterVertices(graph, v -> v.startsWith("node"));
 *
 * // K-hop neighborhood
 * Graph<String> neighborhood = SubgraphUtil.neighborhood(graph, "A", 2);
 *
 * // Graph union
 * Graph<String> union = SubgraphUtil.union(graph1, graph2);
 *
 * // Ego network (1-hop neighborhood)
 * Graph<String> ego = SubgraphUtil.egoNetwork(graph, "A");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns empty graphs for null inputs) - 空值安全: 是（null输入返回空图）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(V + E) - 时间复杂度: O(V + E)</li>
 *   <li>Space complexity: O(V + E) - 空间复杂度: O(V + E)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class SubgraphUtil {

    private SubgraphUtil() {
        // Utility class
    }

    // ==================== Induced Subgraphs | 诱导子图 ====================

    /**
     * Create vertex-induced subgraph.
     * 创建顶点诱导子图。
     *
     * <p>The induced subgraph contains all specified vertices and all edges
     * between those vertices that exist in the original graph.</p>
     * <p>诱导子图包含所有指定的顶点以及原图中这些顶点之间存在的所有边。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param vertices the vertices to include | 要包含的顶点
     * @return the induced subgraph | 诱导子图
     */
    public static <V> Graph<V> induced(Graph<V> graph, Set<V> vertices) {
        if (graph == null || vertices == null || vertices.isEmpty()) {
            return createEmptyGraph(graph);
        }

        Graph<V> subgraph = createEmptyGraph(graph);

        // Add vertices
        for (V vertex : vertices) {
            if (graph.containsVertex(vertex)) {
                subgraph.addVertex(vertex);
            }
        }

        // Add edges between included vertices
        for (Edge<V> edge : graph.edges()) {
            if (vertices.contains(edge.from()) && vertices.contains(edge.to())) {
                subgraph.addEdge(edge.from(), edge.to(), edge.weight());
            }
        }

        return subgraph;
    }

    /**
     * Create edge-induced subgraph.
     * 创建边诱导子图。
     *
     * <p>The induced subgraph contains all specified edges and their endpoint vertices.</p>
     * <p>诱导子图包含所有指定的边及其端点顶点。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param edges the edges to include | 要包含的边
     * @return the induced subgraph | 诱导子图
     */
    public static <V> Graph<V> edgeInduced(Graph<V> graph, Set<Edge<V>> edges) {
        if (graph == null || edges == null || edges.isEmpty()) {
            return createEmptyGraph(graph);
        }

        Graph<V> subgraph = createEmptyGraph(graph);

        for (Edge<V> edge : edges) {
            if (graph.containsEdge(edge.from(), edge.to())) {
                subgraph.addEdge(edge.from(), edge.to(), edge.weight());
            }
        }

        return subgraph;
    }

    // ==================== Filtering | 过滤 ====================

    /**
     * Filter graph by vertex predicate.
     * 按顶点谓词过滤图。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param predicate the vertex filter predicate | 顶点过滤谓词
     * @return filtered subgraph | 过滤后的子图
     */
    public static <V> Graph<V> filterVertices(Graph<V> graph, Predicate<V> predicate) {
        if (graph == null || predicate == null) {
            return createEmptyGraph(graph);
        }

        Set<V> vertices = new HashSet<>();
        for (V vertex : graph.vertices()) {
            if (predicate.test(vertex)) {
                vertices.add(vertex);
            }
        }

        return induced(graph, vertices);
    }

    /**
     * Filter graph by edge predicate.
     * 按边谓词过滤图。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param predicate the edge filter predicate | 边过滤谓词
     * @return filtered subgraph | 过滤后的子图
     */
    public static <V> Graph<V> filterEdges(Graph<V> graph, Predicate<Edge<V>> predicate) {
        if (graph == null || predicate == null) {
            return createEmptyGraph(graph);
        }

        Graph<V> subgraph = createEmptyGraph(graph);

        // Add all vertices
        for (V vertex : graph.vertices()) {
            subgraph.addVertex(vertex);
        }

        // Add filtered edges
        for (Edge<V> edge : graph.edges()) {
            if (predicate.test(edge)) {
                subgraph.addEdge(edge.from(), edge.to(), edge.weight());
            }
        }

        return subgraph;
    }

    /**
     * Filter edges by weight range.
     * 按权重范围过滤边。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param minWeight minimum weight (inclusive) | 最小权重（含）
     * @param maxWeight maximum weight (inclusive) | 最大权重（含）
     * @return filtered subgraph | 过滤后的子图
     */
    public static <V> Graph<V> filterByWeight(Graph<V> graph, double minWeight, double maxWeight) {
        return filterEdges(graph, edge -> edge.weight() >= minWeight && edge.weight() <= maxWeight);
    }

    // ==================== Neighborhood Extraction | 邻域提取 ====================

    /**
     * Extract k-hop neighborhood of a vertex.
     * 提取顶点的k跳邻域。
     *
     * <p>Returns the subgraph containing all vertices within k hops of the center vertex
     * and all edges between them.</p>
     * <p>返回包含中心顶点k跳范围内所有顶点及其之间所有边的子图。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param center the center vertex | 中心顶点
     * @param k maximum distance (hops) | 最大距离（跳数）
     * @return neighborhood subgraph | 邻域子图
     */
    public static <V> Graph<V> neighborhood(Graph<V> graph, V center, int k) {
        if (graph == null || center == null || !graph.containsVertex(center) || k < 0) {
            return createEmptyGraph(graph);
        }

        Set<V> vertices = new HashSet<>();
        vertices.add(center);

        Set<V> frontier = new HashSet<>();
        frontier.add(center);

        for (int i = 0; i < k; i++) {
            Set<V> nextFrontier = new HashSet<>();
            for (V v : frontier) {
                for (V neighbor : graph.neighbors(v)) {
                    if (!vertices.contains(neighbor)) {
                        vertices.add(neighbor);
                        nextFrontier.add(neighbor);
                    }
                }
                // For directed graphs, also consider incoming edges
                if (graph.isDirected()) {
                    for (Edge<V> inEdge : graph.inEdges(v)) {
                        V from = inEdge.from();
                        if (!vertices.contains(from)) {
                            vertices.add(from);
                            nextFrontier.add(from);
                        }
                    }
                }
            }
            frontier = nextFrontier;
            if (frontier.isEmpty()) {
                break;
            }
        }

        return induced(graph, vertices);
    }

    /**
     * Extract ego network (1-hop neighborhood).
     * 提取自我网络（1跳邻域）。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param ego the ego vertex | 自我顶点
     * @return ego network subgraph | 自我网络子图
     */
    public static <V> Graph<V> egoNetwork(Graph<V> graph, V ego) {
        return neighborhood(graph, ego, 1);
    }

    /**
     * Extract ego network with specified radius.
     * 提取指定半径的自我网络。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param ego the ego vertex | 自我顶点
     * @param radius the radius (hops) | 半径（跳数）
     * @return ego network subgraph | 自我网络子图
     */
    public static <V> Graph<V> egoNetwork(Graph<V> graph, V ego, int radius) {
        return neighborhood(graph, ego, radius);
    }

    // ==================== Graph Operations | 图操作 ====================

    /**
     * Compute union of two graphs.
     * 计算两个图的并集。
     *
     * <p>The union contains all vertices and edges from both graphs.</p>
     * <p>并集包含两个图的所有顶点和边。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param g1 the first graph | 第一个图
     * @param g2 the second graph | 第二个图
     * @return union graph | 并集图
     */
    public static <V> Graph<V> union(Graph<V> g1, Graph<V> g2) {
        if (g1 == null && g2 == null) {
            return new DirectedGraph<>();
        }
        if (g1 == null) {
            return copy(g2);
        }
        if (g2 == null) {
            return copy(g1);
        }

        Graph<V> result = createEmptyGraph(g1);

        // Add all vertices
        for (V v : g1.vertices()) {
            result.addVertex(v);
        }
        for (V v : g2.vertices()) {
            result.addVertex(v);
        }

        // Add all edges
        for (Edge<V> e : g1.edges()) {
            result.addEdge(e.from(), e.to(), e.weight());
        }
        for (Edge<V> e : g2.edges()) {
            if (!result.containsEdge(e.from(), e.to())) {
                result.addEdge(e.from(), e.to(), e.weight());
            }
        }

        return result;
    }

    /**
     * Compute intersection of two graphs.
     * 计算两个图的交集。
     *
     * <p>The intersection contains vertices and edges present in both graphs.</p>
     * <p>交集包含两个图中都存在的顶点和边。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param g1 the first graph | 第一个图
     * @param g2 the second graph | 第二个图
     * @return intersection graph | 交集图
     */
    public static <V> Graph<V> intersection(Graph<V> g1, Graph<V> g2) {
        if (g1 == null || g2 == null) {
            return new DirectedGraph<>();
        }

        Graph<V> result = createEmptyGraph(g1);

        // Add vertices in both graphs
        for (V v : g1.vertices()) {
            if (g2.containsVertex(v)) {
                result.addVertex(v);
            }
        }

        // Add edges in both graphs
        for (Edge<V> e : g1.edges()) {
            if (g2.containsEdge(e.from(), e.to())) {
                result.addEdge(e.from(), e.to(), e.weight());
            }
        }

        return result;
    }

    /**
     * Compute difference of two graphs (g1 - g2).
     * 计算两个图的差集（g1 - g2）。
     *
     * <p>The difference contains vertices and edges from g1 that are not in g2.</p>
     * <p>差集包含g1中但不在g2中的顶点和边。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param g1 the first graph | 第一个图
     * @param g2 the second graph | 第二个图
     * @return difference graph | 差集图
     */
    public static <V> Graph<V> difference(Graph<V> g1, Graph<V> g2) {
        if (g1 == null) {
            return new DirectedGraph<>();
        }
        if (g2 == null) {
            return copy(g1);
        }

        Graph<V> result = createEmptyGraph(g1);

        // Add vertices not in g2
        for (V v : g1.vertices()) {
            if (!g2.containsVertex(v)) {
                result.addVertex(v);
            }
        }

        // Add edges not in g2 (only between vertices kept)
        for (Edge<V> e : g1.edges()) {
            if (!g2.containsEdge(e.from(), e.to())) {
                // Need to ensure endpoints exist
                if (!g2.containsVertex(e.from()) && !g2.containsVertex(e.to())) {
                    result.addEdge(e.from(), e.to(), e.weight());
                }
            }
        }

        return result;
    }

    /**
     * Compute symmetric difference of two graphs.
     * 计算两个图的对称差集。
     *
     * <p>The symmetric difference contains elements in either graph but not in both.</p>
     * <p>对称差集包含在任一图中但不同时在两个图中的元素。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param g1 the first graph | 第一个图
     * @param g2 the second graph | 第二个图
     * @return symmetric difference graph | 对称差集图
     */
    public static <V> Graph<V> symmetricDifference(Graph<V> g1, Graph<V> g2) {
        return union(difference(g1, g2), difference(g2, g1));
    }

    // ==================== Graph Transformations | 图变换 ====================

    /**
     * Create a reversed (transposed) graph.
     * 创建反转（转置）图。
     *
     * <p>For directed graphs, all edge directions are reversed.
     * For undirected graphs, returns a copy.</p>
     * <p>对于有向图，所有边方向反转。对于无向图，返回副本。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @return reversed graph | 反转图
     */
    public static <V> Graph<V> reverse(Graph<V> graph) {
        if (graph == null) {
            return new DirectedGraph<>();
        }

        Graph<V> result = createEmptyGraph(graph);

        // Add all vertices
        for (V v : graph.vertices()) {
            result.addVertex(v);
        }

        // Add reversed edges
        for (Edge<V> e : graph.edges()) {
            result.addEdge(e.to(), e.from(), e.weight());
        }

        return result;
    }

    /**
     * Create a copy of the graph.
     * 创建图的副本。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @return graph copy | 图副本
     */
    public static <V> Graph<V> copy(Graph<V> graph) {
        if (graph == null) {
            return new DirectedGraph<>();
        }

        Graph<V> result = createEmptyGraph(graph);

        for (V v : graph.vertices()) {
            result.addVertex(v);
        }

        for (Edge<V> e : graph.edges()) {
            result.addEdge(e.from(), e.to(), e.weight());
        }

        return result;
    }

    /**
     * Create the complement of a graph.
     * 创建图的补图。
     *
     * <p>The complement contains all edges not in the original graph.</p>
     * <p>补图包含原图中不存在的所有边。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @return complement graph | 补图
     */
    public static <V> Graph<V> complement(Graph<V> graph) {
        if (graph == null) {
            return new DirectedGraph<>();
        }

        Graph<V> result = createEmptyGraph(graph);
        List<V> vertices = new ArrayList<>(graph.vertices());

        // Add all vertices
        for (V v : vertices) {
            result.addVertex(v);
        }

        // Add edges that don't exist in original
        for (int i = 0; i < vertices.size(); i++) {
            V from = vertices.get(i);
            int start = graph.isDirected() ? 0 : i + 1;
            for (int j = start; j < vertices.size(); j++) {
                if (i == j) continue;
                V to = vertices.get(j);
                if (!graph.containsEdge(from, to)) {
                    result.addEdge(from, to);
                }
            }
        }

        return result;
    }

    /**
     * Remove isolated vertices (vertices with no edges).
     * 移除孤立顶点（没有边的顶点）。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @return graph without isolated vertices | 无孤立顶点的图
     */
    public static <V> Graph<V> removeIsolated(Graph<V> graph) {
        return filterVertices(graph, v -> graph.outDegree(v) > 0 || graph.inDegree(v) > 0);
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Sample a random subgraph with specified number of vertices.
     * 采样具有指定顶点数的随机子图。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param numVertices number of vertices to sample | 要采样的顶点数
     * @param random random number generator | 随机数生成器
     * @return sampled subgraph | 采样子图
     */
    public static <V> Graph<V> sampleVertices(Graph<V> graph, int numVertices, Random random) {
        if (graph == null || numVertices <= 0) {
            return createEmptyGraph(graph);
        }

        List<V> vertices = new ArrayList<>(graph.vertices());
        Collections.shuffle(vertices, random);

        Set<V> sampled = new HashSet<>(vertices.subList(0, Math.min(numVertices, vertices.size())));
        return induced(graph, sampled);
    }

    /**
     * Sample a random subgraph with specified number of edges.
     * 采样具有指定边数的随机子图。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param numEdges number of edges to sample | 要采样的边数
     * @param random random number generator | 随机数生成器
     * @return sampled subgraph | 采样子图
     */
    public static <V> Graph<V> sampleEdges(Graph<V> graph, int numEdges, Random random) {
        if (graph == null || numEdges <= 0) {
            return createEmptyGraph(graph);
        }

        List<Edge<V>> edges = new ArrayList<>(graph.edges());
        Collections.shuffle(edges, random);

        Set<Edge<V>> sampled = new HashSet<>(edges.subList(0, Math.min(numEdges, edges.size())));
        return edgeInduced(graph, sampled);
    }

    // ==================== Helper Methods | 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private static <V> Graph<V> createEmptyGraph(Graph<?> template) {
        if (template == null || template.isDirected()) {
            return new DirectedGraph<>();
        } else {
            return new UndirectedGraph<>();
        }
    }
}
