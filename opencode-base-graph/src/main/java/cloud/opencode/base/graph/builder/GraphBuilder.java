package cloud.opencode.base.graph.builder;

import cloud.opencode.base.graph.DirectedGraph;
import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.UndirectedGraph;
import cloud.opencode.base.graph.node.Edge;
import cloud.opencode.base.graph.validation.GraphValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Graph Builder
 * 图构建器
 *
 * <p>Fluent builder for constructing graphs.</p>
 * <p>用于构建图的流式构建器。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Fluent API for graph construction | 流式API用于图构建</li>
 *   <li>Batch edge/vertex addition | 批量添加边/顶点</li>
 *   <li>Validation support | 验证支持</li>
 *   <li>Initial capacity configuration | 初始容量配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build a directed graph
 * Graph<String> graph = GraphBuilder.<String>directed()
 *     .addEdge("A", "B", 1.0)
 *     .addEdge("B", "C", 2.0)
 *     .addEdge("C", "D", 3.0)
 *     .build();
 *
 * // Build with validation
 * Graph<String> validGraph = GraphBuilder.<String>directed()
 *     .addEdge("A", "B")
 *     .addEdge("B", "C")
 *     .buildAndValidate();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder is not thread-safe, built graph is) - 线程安全: 否（构建器非线程安全，构建的图是）</li>
 *   <li>Null-safe: No (does not validate inputs during building) - 空值安全: 否（构建过程中不验证输入）</li>
 * </ul>
 *
 * @param <V> the vertex type | 顶点类型
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per operation - 时间复杂度: O(1) 每次操作</li>
 *   <li>Space complexity: O(V + E) - 空间复杂度: O(V + E)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class GraphBuilder<V> {

    private final boolean directed;
    private final List<V> vertices;
    private final List<EdgeData<V>> edges;
    private int initialCapacity;

    private GraphBuilder(boolean directed) {
        this.directed = directed;
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.initialCapacity = 16;
    }

    /**
     * Create a builder for directed graph
     * 创建有向图构建器
     *
     * @param <V> the vertex type | 顶点类型
     * @return a new graph builder | 新的图构建器
     */
    public static <V> GraphBuilder<V> directed() {
        return new GraphBuilder<>(true);
    }

    /**
     * Create a builder for undirected graph
     * 创建无向图构建器
     *
     * @param <V> the vertex type | 顶点类型
     * @return a new graph builder | 新的图构建器
     */
    public static <V> GraphBuilder<V> undirected() {
        return new GraphBuilder<>(false);
    }

    /**
     * Set initial capacity for the graph
     * 设置图的初始容量
     *
     * @param capacity the initial capacity | 初始容量
     * @return this builder | 此构建器
     */
    public GraphBuilder<V> initialCapacity(int capacity) {
        this.initialCapacity = capacity;
        return this;
    }

    /**
     * Add a vertex
     * 添加顶点
     *
     * @param vertex the vertex to add | 要添加的顶点
     * @return this builder | 此构建器
     */
    public GraphBuilder<V> addVertex(V vertex) {
        vertices.add(vertex);
        return this;
    }

    /**
     * Add multiple vertices
     * 添加多个顶点
     *
     * @param vertices the vertices to add | 要添加的顶点
     * @return this builder | 此构建器
     */
    @SafeVarargs
    public final GraphBuilder<V> addVertices(V... vertices) {
        for (V vertex : vertices) {
            this.vertices.add(vertex);
        }
        return this;
    }

    /**
     * Add vertices from collection
     * 从集合添加顶点
     *
     * @param vertices the vertices to add | 要添加的顶点
     * @return this builder | 此构建器
     */
    public GraphBuilder<V> addVertices(Collection<V> vertices) {
        this.vertices.addAll(vertices);
        return this;
    }

    /**
     * Add an edge with default weight
     * 添加默认权重的边
     *
     * @param from source vertex | 源顶点
     * @param to target vertex | 目标顶点
     * @return this builder | 此构建器
     */
    public GraphBuilder<V> addEdge(V from, V to) {
        return addEdge(from, to, Edge.DEFAULT_WEIGHT);
    }

    /**
     * Add an edge with weight
     * 添加带权重的边
     *
     * @param from source vertex | 源顶点
     * @param to target vertex | 目标顶点
     * @param weight edge weight | 边权重
     * @return this builder | 此构建器
     */
    public GraphBuilder<V> addEdge(V from, V to, double weight) {
        edges.add(new EdgeData<>(from, to, weight));
        return this;
    }

    /**
     * Add an edge
     * 添加边
     *
     * @param edge the edge to add | 要添加的边
     * @return this builder | 此构建器
     */
    public GraphBuilder<V> addEdge(Edge<V> edge) {
        edges.add(new EdgeData<>(edge.from(), edge.to(), edge.weight()));
        return this;
    }

    /**
     * Add multiple edges
     * 添加多个边
     *
     * @param edges the edges to add | 要添加的边
     * @return this builder | 此构建器
     */
    public GraphBuilder<V> addEdges(Collection<Edge<V>> edges) {
        for (Edge<V> edge : edges) {
            this.edges.add(new EdgeData<>(edge.from(), edge.to(), edge.weight()));
        }
        return this;
    }

    /**
     * Configure the builder
     * 配置构建器
     *
     * @param configurer the configuration function | 配置函数
     * @return this builder | 此构建器
     */
    public GraphBuilder<V> configure(Consumer<GraphBuilder<V>> configurer) {
        configurer.accept(this);
        return this;
    }

    /**
     * Build the graph
     * 构建图
     *
     * @return the built graph | 构建的图
     */
    public Graph<V> build() {
        Graph<V> graph = directed ? new DirectedGraph<>() : new UndirectedGraph<>();

        // Add vertices first
        for (V vertex : vertices) {
            graph.addVertex(vertex);
        }

        // Add edges
        for (EdgeData<V> edge : edges) {
            graph.addEdge(edge.from(), edge.to(), edge.weight());
        }

        return graph;
    }

    /**
     * Build and validate the graph
     * 构建并验证图
     *
     * @return the built and validated graph | 构建并验证的图
     */
    public Graph<V> buildAndValidate() {
        Graph<V> graph = build();
        GraphValidator.validateGraphStructure(graph);
        return graph;
    }

    /**
     * Internal edge data holder
     * 内部边数据持有者
     */
    private record EdgeData<V>(V from, V to, double weight) {
    }
}
