package cloud.opencode.base.graph;

import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Immutable Graph - An unmodifiable snapshot of a graph
 * 不可变图 - 图的不可修改快照
 *
 * <p>Creates a deep copy of a graph that implements {@link Graph} but throws
 * {@link UnsupportedOperationException} on all mutation methods.</p>
 * <p>创建图的深拷贝，实现 {@link Graph} 接口，但在所有修改方法上抛出
 * {@link UnsupportedOperationException}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deep copy detached from original graph - 与原图完全分离的深拷贝</li>
 *   <li>All query methods work normally - 所有查询方法正常工作</li>
 *   <li>All mutation methods throw UnsupportedOperationException - 所有修改方法抛出UnsupportedOperationException</li>
 *   <li>Thread-safe (immutable) - 线程安全（不可变）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> original = OpenGraph.directed();
 * original.addEdge("A", "B");
 * Graph<String> snapshot = ImmutableGraph.copyOf(original);
 * // snapshot.addVertex("C"); // throws UnsupportedOperationException
 * }</pre>
 *
 * @param <V> the vertex type | 顶点类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public final class ImmutableGraph<V> implements Graph<V> {

    private final Map<V, Set<Edge<V>>> outEdgesMap;
    private final Map<V, Set<Edge<V>>> inEdgesMap;
    private final Map<V, Set<V>> neighborsMap;
    private final Set<V> vertexSet;
    private final Set<Edge<V>> edgeSet;
    private final boolean directed;
    private final int edgeCount;

    private ImmutableGraph(Graph<V> source) {
        this.directed = source.isDirected();

        Set<V> srcVertices = source.vertices();
        int vCount = srcVertices.size();

        Map<V, Set<Edge<V>>> outMap = HashMap.newHashMap(vCount);
        Map<V, Set<Edge<V>>> inMap = HashMap.newHashMap(vCount);
        Map<V, Set<V>> neighborsMap = HashMap.newHashMap(vCount);

        for (V vertex : srcVertices) {
            Set<Edge<V>> outEdges = Set.copyOf(source.outEdges(vertex));
            outMap.put(vertex, outEdges);
            inMap.put(vertex, Set.copyOf(source.inEdges(vertex)));

            // Pre-compute neighbors
            Set<V> neighbors = HashSet.newHashSet(outEdges.size());
            for (Edge<V> edge : outEdges) {
                neighbors.add(edge.to());
            }
            neighborsMap.put(vertex, Collections.unmodifiableSet(neighbors));
        }

        this.outEdgesMap = Collections.unmodifiableMap(outMap);
        this.inEdgesMap = Collections.unmodifiableMap(inMap);
        this.neighborsMap = Collections.unmodifiableMap(neighborsMap);
        this.vertexSet = Set.copyOf(srcVertices);
        // Use source.edges() to get properly deduplicated edges (important for undirected graphs)
        this.edgeSet = Set.copyOf(source.edges());
        this.edgeCount = source.edgeCount();
    }

    /**
     * Create an immutable copy of the given graph
     * 创建给定图的不可变副本
     *
     * <p>If the graph is already an {@code ImmutableGraph}, the same instance is returned
     * without copying — the graph is already immutable, so no defensive copy is needed.</p>
     * <p>如果图已经是 {@code ImmutableGraph}，则直接返回同一实例而不复制——该图已不可变，无需防御性拷贝。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the source graph | 源图
     * @return an immutable copy, or the same instance if already immutable | 不可变副本，如果已是不可变图则返回同一实例
     * @throws NullPointerException if graph is null | 当图为null时抛出
     */
    public static <V> ImmutableGraph<V> copyOf(Graph<V> graph) {
        Objects.requireNonNull(graph, "Graph must not be null");
        if (graph instanceof ImmutableGraph<V> immutable) {
            return immutable;
        }
        return new ImmutableGraph<>(graph);
    }

    /**
     * Always throws UnsupportedOperationException
     * 始终抛出 UnsupportedOperationException
     *
     * @param vertex ignored | 忽略
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void addVertex(V vertex) {
        throw new UnsupportedOperationException("ImmutableGraph does not support addVertex");
    }

    /**
     * Always throws UnsupportedOperationException
     * 始终抛出 UnsupportedOperationException
     *
     * @param from ignored | 忽略
     * @param to ignored | 忽略
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void addEdge(V from, V to) {
        throw new UnsupportedOperationException("ImmutableGraph does not support addEdge");
    }

    /**
     * Always throws UnsupportedOperationException
     * 始终抛出 UnsupportedOperationException
     *
     * @param from ignored | 忽略
     * @param to ignored | 忽略
     * @param weight ignored | 忽略
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void addEdge(V from, V to, double weight) {
        throw new UnsupportedOperationException("ImmutableGraph does not support addEdge");
    }

    /**
     * Always throws UnsupportedOperationException
     * 始终抛出 UnsupportedOperationException
     *
     * @param vertex ignored | 忽略
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void removeVertex(V vertex) {
        throw new UnsupportedOperationException("ImmutableGraph does not support removeVertex");
    }

    /**
     * Always throws UnsupportedOperationException
     * 始终抛出 UnsupportedOperationException
     *
     * @param from ignored | 忽略
     * @param to ignored | 忽略
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void removeEdge(V from, V to) {
        throw new UnsupportedOperationException("ImmutableGraph does not support removeEdge");
    }

    @Override
    public Set<V> vertices() {
        return vertexSet;
    }

    @Override
    public Set<Edge<V>> edges() {
        return edgeSet;
    }

    @Override
    public Set<V> neighbors(V vertex) {
        Set<V> result = neighborsMap.get(vertex);
        return result != null ? result : Set.of();
    }

    @Override
    public Set<Edge<V>> outEdges(V vertex) {
        Set<Edge<V>> out = outEdgesMap.get(vertex);
        return out != null ? out : Set.of();
    }

    @Override
    public Set<Edge<V>> inEdges(V vertex) {
        Set<Edge<V>> in = inEdgesMap.get(vertex);
        return in != null ? in : Set.of();
    }

    @Override
    public int vertexCount() {
        return vertexSet.size();
    }

    @Override
    public int edgeCount() {
        return edgeCount;
    }

    @Override
    public boolean containsVertex(V vertex) {
        return vertex != null && vertexSet.contains(vertex);
    }

    @Override
    public boolean containsEdge(V from, V to) {
        if (from == null || to == null) {
            return false;
        }
        Set<Edge<V>> out = outEdgesMap.get(from);
        if (out == null) {
            return false;
        }
        for (Edge<V> edge : out) {
            if (edge.to().equals(to)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getWeight(V from, V to) {
        if (from == null || to == null) {
            return Double.MAX_VALUE;
        }
        Set<Edge<V>> out = outEdgesMap.get(from);
        if (out == null) {
            return Double.MAX_VALUE;
        }
        for (Edge<V> edge : out) {
            if (edge.to().equals(to)) {
                return edge.weight();
            }
        }
        return Double.MAX_VALUE;
    }

    @Override
    public boolean isDirected() {
        return directed;
    }

    /**
     * Always throws UnsupportedOperationException
     * 始终抛出 UnsupportedOperationException
     *
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("ImmutableGraph does not support clear");
    }

    @Override
    public String toString() {
        return "ImmutableGraph{vertices=" + vertexCount() + ", edges=" + edgeCount() +
                ", directed=" + directed + "}";
    }
}
