package cloud.opencode.base.graph;

import cloud.opencode.base.graph.node.Edge;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Graph Transform - Utility for transforming graphs through vertex/edge mapping and filtering
 * 图变换 - 通过顶点/边映射和过滤来变换图的工具类
 *
 * <p>Provides static methods for common graph transformations including vertex mapping,
 * vertex filtering (induced subgraph), edge filtering, and edge reversal.</p>
 * <p>提供常见图变换的静态方法，包括顶点映射、顶点过滤（诱导子图）、边过滤和边反转。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Map vertices to new types - 将顶点映射为新类型</li>
 *   <li>Filter vertices (induced subgraph) - 过滤顶点（诱导子图）</li>
 *   <li>Filter edges - 过滤边</li>
 *   <li>Reverse directed graphs - 反转有向图</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<Integer> intGraph = OpenGraph.directed();
 * intGraph.addEdge(1, 2, 3.0);
 *
 * // Map vertices: Integer -> String
 * Graph<String> strGraph = GraphTransform.mapVertices(intGraph, String::valueOf);
 *
 * // Filter vertices
 * Graph<Integer> filtered = GraphTransform.filterVertices(intGraph, v -> v > 0);
 *
 * // Reverse edges
 * Graph<Integer> reversed = GraphTransform.reverse(intGraph);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public final class GraphTransform {

    private GraphTransform() {
        // Utility class
    }

    /**
     * Map all vertices to a new type, preserving edges and weights
     * 将所有顶点映射为新类型，保留边和权重
     *
     * <p><strong>Note:</strong> If the mapper is not injective (i.e. two distinct vertices are mapped
     * to the same value), the resulting vertices are silently merged. Edges from both original
     * vertices will appear to originate from the single merged vertex. Callers should ensure
     * the mapper produces unique values when vertex identity must be preserved.</p>
     * <p><strong>注意:</strong> 如果映射函数不是单射（即两个不同的顶点映射为相同的值），
     * 则结果中这些顶点会被静默合并。如需保留顶点唯一性，调用方应确保映射函数产生唯一值。</p>
     *
     * @param <V> the original vertex type | 原始顶点类型
     * @param <R> the new vertex type | 新顶点类型
     * @param graph the source graph | 源图
     * @param mapper the vertex mapping function (should be injective to preserve structure) | 顶点映射函数（应为单射以保留结构）
     * @return a new graph with mapped vertices | 映射顶点后的新图
     * @throws NullPointerException if graph or mapper is null | 当图或映射函数为null时抛出
     */
    public static <V, R> Graph<R> mapVertices(Graph<V> graph, Function<V, R> mapper) {
        Objects.requireNonNull(graph, "Graph must not be null");
        Objects.requireNonNull(mapper, "Mapper must not be null");

        Graph<R> result = graph.isDirected() ? new DirectedGraph<>() : new UndirectedGraph<>();

        for (V vertex : graph.vertices()) {
            result.addVertex(mapper.apply(vertex));
        }

        for (Edge<V> edge : graph.edges()) {
            R from = mapper.apply(edge.from());
            R to = mapper.apply(edge.to());
            result.addEdge(from, to, edge.weight());
        }

        return result;
    }

    /**
     * Filter vertices, keeping only those matching the predicate (induced subgraph)
     * 过滤顶点，仅保留匹配谓词的顶点（诱导子图）
     *
     * <p>Edges are kept only if both endpoints match the predicate.</p>
     * <p>仅当边的两个端点都匹配谓词时才保留边。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param predicate the vertex filter predicate | 顶点过滤谓词
     * @return a new graph with filtered vertices and their connecting edges | 过滤后的新图
     * @throws NullPointerException if graph or predicate is null | 当图或谓词为null时抛出
     */
    public static <V> Graph<V> filterVertices(Graph<V> graph, Predicate<V> predicate) {
        Objects.requireNonNull(graph, "Graph must not be null");
        Objects.requireNonNull(predicate, "Predicate must not be null");

        Graph<V> result = graph.isDirected() ? new DirectedGraph<>() : new UndirectedGraph<>();

        for (V vertex : graph.vertices()) {
            if (predicate.test(vertex)) {
                result.addVertex(vertex);
            }
        }

        for (Edge<V> edge : graph.edges()) {
            if (predicate.test(edge.from()) && predicate.test(edge.to())) {
                result.addEdge(edge.from(), edge.to(), edge.weight());
            }
        }

        return result;
    }

    /**
     * Filter edges, keeping all vertices but only edges matching the predicate
     * 过滤边，保留所有顶点但仅保留匹配谓词的边
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @param predicate the edge filter predicate | 边过滤谓词
     * @return a new graph with all vertices and filtered edges | 包含所有顶点和过滤后边的新图
     * @throws NullPointerException if graph or predicate is null | 当图或谓词为null时抛出
     */
    public static <V> Graph<V> filterEdges(Graph<V> graph, Predicate<Edge<V>> predicate) {
        Objects.requireNonNull(graph, "Graph must not be null");
        Objects.requireNonNull(predicate, "Predicate must not be null");

        Graph<V> result = graph.isDirected() ? new DirectedGraph<>() : new UndirectedGraph<>();

        for (V vertex : graph.vertices()) {
            result.addVertex(vertex);
        }

        for (Edge<V> edge : graph.edges()) {
            if (predicate.test(edge)) {
                result.addEdge(edge.from(), edge.to(), edge.weight());
            }
        }

        return result;
    }

    /**
     * Reverse all edges in a directed graph; for undirected graphs, return a copy
     * 反转有向图的所有边；对于无向图，返回副本
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @return a new graph with reversed edges (directed) or a copy (undirected) | 反转边的新图（有向）或副本（无向）
     * @throws NullPointerException if graph is null | 当图为null时抛出
     */
    public static <V> Graph<V> reverse(Graph<V> graph) {
        Objects.requireNonNull(graph, "Graph must not be null");

        Graph<V> result = graph.isDirected() ? new DirectedGraph<>() : new UndirectedGraph<>();

        for (V vertex : graph.vertices()) {
            result.addVertex(vertex);
        }

        for (Edge<V> edge : graph.edges()) {
            if (graph.isDirected()) {
                result.addEdge(edge.to(), edge.from(), edge.weight());
            } else {
                result.addEdge(edge.from(), edge.to(), edge.weight());
            }
        }

        return result;
    }
}
