package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;

import java.util.*;

/**
 * Graph Metrics - Utility class for computing graph metrics and statistics
 * 图度量 - 计算图度量和统计信息的工具类
 *
 * <p>Provides static methods for computing various graph metrics including density,
 * eccentricity, diameter, radius, center, average path length, and clustering coefficient.</p>
 * <p>提供计算各种图度量的静态方法，包括密度、离心率、直径、半径、中心、平均路径长度和聚类系数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Edge density calculation - 边密度计算</li>
 *   <li>Eccentricity, diameter, and radius - 离心率、直径和半径</li>
 *   <li>Graph center identification - 图中心识别</li>
 *   <li>Average path length (BFS-based, unweighted) - 平均路径长度（基于BFS，无权）</li>
 *   <li>Local and average clustering coefficient - 局部和平均聚类系数</li>
 *   <li>Full graph summary - 完整图摘要</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> graph = OpenGraph.undirected();
 * graph.addEdge("A", "B");
 * graph.addEdge("B", "C");
 *
 * double d = GraphMetrics.density(graph);
 * int diam = GraphMetrics.diameter(graph);
 * GraphMetrics.GraphSummary summary = GraphMetrics.summary(graph);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public final class GraphMetrics {

    private GraphMetrics() {
        // Utility class
    }

    /**
     * Graph summary record containing key metrics
     * 包含关键度量的图摘要记录
     *
     * @param vertexCount number of vertices | 顶点数
     * @param edgeCount number of edges | 边数
     * @param density edge density | 边密度
     * @param directed whether the graph is directed | 是否有向图
     * @param connected whether the graph is fully connected | 是否完全连通
     * @param componentCount number of connected components | 连通分量数
     * @param diameter graph diameter | 图直径
     * @param radius graph radius | 图半径
     * @param averagePathLength average shortest path length | 平均最短路径长度
     * @param averageClusteringCoefficient average clustering coefficient | 平均聚类系数
     */
    public record GraphSummary(int vertexCount, int edgeCount, double density, boolean directed,
                               boolean connected, int componentCount, int diameter, int radius,
                               double averagePathLength, double averageClusteringCoefficient) {
    }

    /**
     * Compute the edge density of a graph
     * 计算图的边密度
     *
     * <p>For directed graphs: E / (V * (V - 1)).
     * For undirected graphs: 2 * E / (V * (V - 1)).
     * Returns 0 if V &lt;= 1.</p>
     * <p>有向图: E / (V * (V - 1))。无向图: 2 * E / (V * (V - 1))。当 V &lt;= 1 时返回 0。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return the edge density in range [0, 1] | 边密度，范围 [0, 1]
     * @throws NullPointerException if graph is null | 当图为null时抛出
     */
    public static <V> double density(Graph<V> graph) {
        Objects.requireNonNull(graph, "Graph must not be null");
        int v = graph.vertexCount();
        if (v <= 1) {
            return 0.0;
        }
        long maxEdges = (long) v * (v - 1);
        if (!graph.isDirected()) {
            // undirected: 2E / (V*(V-1))
            return (2.0 * graph.edgeCount()) / maxEdges;
        }
        return (double) graph.edgeCount() / maxEdges;
    }

    /**
     * Compute the eccentricity of a vertex (BFS-based, unweighted)
     * 计算顶点的离心率（基于BFS，无权）
     *
     * <p>The eccentricity is the maximum shortest path distance from the given vertex
     * to any other reachable vertex. Returns {@link Integer#MAX_VALUE} if any vertex
     * is unreachable from the given vertex.</p>
     * <p>离心率是从给定顶点到任何其他可达顶点的最大最短路径距离。
     * 如果存在从该顶点不可达的顶点，则返回 {@link Integer#MAX_VALUE}。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param vertex the vertex | 顶点
     * @return the eccentricity | 离心率
     * @throws NullPointerException if graph or vertex is null | 当图或顶点为null时抛出
     * @throws IllegalArgumentException if vertex is not in the graph | 当顶点不在图中时抛出
     */
    public static <V> int eccentricity(Graph<V> graph, V vertex) {
        Objects.requireNonNull(graph, "Graph must not be null");
        Objects.requireNonNull(vertex, "Vertex must not be null");
        if (!graph.containsVertex(vertex)) {
            throw new IllegalArgumentException("Vertex not in graph: " + vertex);
        }

        Map<V, Integer> distances = bfsDistances(graph, vertex);

        // Check if all vertices are reachable
        if (distances.size() < graph.vertexCount()) {
            return Integer.MAX_VALUE;
        }

        int maxDist = 0;
        for (int dist : distances.values()) {
            if (dist > maxDist) {
                maxDist = dist;
            }
        }
        return maxDist;
    }

    /**
     * Compute the diameter of a graph (max eccentricity over all vertices)
     * 计算图的直径（所有顶点的最大离心率）
     *
     * <p>Time complexity: O(V * (V + E)).</p>
     * <p>时间复杂度: O(V * (V + E))。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return the diameter, or 0 for empty/single-vertex graphs, Integer.MAX_VALUE for disconnected graphs |
     *         直径，空图或单顶点图返回0，断开图返回Integer.MAX_VALUE
     * @throws NullPointerException if graph is null | 当图为null时抛出
     */
    public static <V> int diameter(Graph<V> graph) {
        Objects.requireNonNull(graph, "Graph must not be null");
        if (graph.vertexCount() <= 1) {
            return 0;
        }

        int maxEcc = 0;
        for (V v : graph.vertices()) {
            int ecc = eccentricity(graph, v);
            if (ecc > maxEcc) {
                maxEcc = ecc;
            }
        }
        return maxEcc;
    }

    /**
     * Compute the radius of a graph (min eccentricity over all vertices)
     * 计算图的半径（所有顶点的最小离心率）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return the radius, or 0 for empty/single-vertex graphs, Integer.MAX_VALUE for disconnected graphs |
     *         半径，空图或单顶点图返回0，断开图返回Integer.MAX_VALUE
     * @throws NullPointerException if graph is null | 当图为null时抛出
     */
    public static <V> int radius(Graph<V> graph) {
        Objects.requireNonNull(graph, "Graph must not be null");
        if (graph.vertexCount() <= 1) {
            return 0;
        }

        int minEcc = Integer.MAX_VALUE;
        for (V v : graph.vertices()) {
            int ecc = eccentricity(graph, v);
            if (ecc < minEcc) {
                minEcc = ecc;
            }
        }
        return minEcc;
    }

    /**
     * Find the center of a graph (vertices with eccentricity equal to the radius)
     * 查找图的中心（离心率等于半径的顶点集合）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return set of center vertices | 中心顶点集合
     * @throws NullPointerException if graph is null | 当图为null时抛出
     */
    public static <V> Set<V> center(Graph<V> graph) {
        Objects.requireNonNull(graph, "Graph must not be null");
        if (graph.isEmpty()) {
            return Set.of();
        }

        Map<V, Integer> eccentricities = new HashMap<>();
        int minEcc = Integer.MAX_VALUE;

        for (V v : graph.vertices()) {
            int ecc = eccentricity(graph, v);
            eccentricities.put(v, ecc);
            if (ecc < minEcc) {
                minEcc = ecc;
            }
        }

        Set<V> centerVertices = new HashSet<>();
        for (Map.Entry<V, Integer> entry : eccentricities.entrySet()) {
            if (entry.getValue() == minEcc) {
                centerVertices.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(centerVertices);
    }

    /**
     * Compute the average shortest path length (BFS-based, unweighted)
     * 计算平均最短路径长度（基于BFS，无权）
     *
     * <p>Only counts reachable pairs. Returns 0 if no reachable pairs exist.</p>
     * <p>仅计算可达的顶点对。如果不存在可达的顶点对，返回0。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return the average path length | 平均路径长度
     * @throws NullPointerException if graph is null | 当图为null时抛出
     */
    public static <V> double averagePathLength(Graph<V> graph) {
        Objects.requireNonNull(graph, "Graph must not be null");
        if (graph.vertexCount() <= 1) {
            return 0.0;
        }

        long totalDistance = 0;
        long pairCount = 0;

        for (V v : graph.vertices()) {
            Map<V, Integer> distances = bfsDistances(graph, v);
            for (Map.Entry<V, Integer> entry : distances.entrySet()) {
                if (!entry.getKey().equals(v)) {
                    totalDistance += entry.getValue();
                    pairCount++;
                }
            }
        }

        if (pairCount == 0) {
            return 0.0;
        }
        return (double) totalDistance / pairCount;
    }

    /**
     * Compute the local clustering coefficient of a vertex
     * 计算顶点的局部聚类系数
     *
     * <p>For a vertex v with k neighbors, the clustering coefficient is the ratio
     * of actual edges among neighbors to the maximum possible edges.
     * For undirected: edges / (k*(k-1)/2). For directed: edges / (k*(k-1)).
     * Returns 0 if k &lt; 2.</p>
     * <p>对于具有k个邻居的顶点v，聚类系数是邻居之间实际边数与最大可能边数的比率。
     * 无向图: edges / (k*(k-1)/2)。有向图: edges / (k*(k-1))。当k &lt; 2时返回0。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param vertex the vertex | 顶点
     * @return the local clustering coefficient in range [0, 1] | 局部聚类系数，范围 [0, 1]
     * @throws NullPointerException if graph or vertex is null | 当图或顶点为null时抛出
     */
    public static <V> double clusteringCoefficient(Graph<V> graph, V vertex) {
        Objects.requireNonNull(graph, "Graph must not be null");
        Objects.requireNonNull(vertex, "Vertex must not be null");

        Set<V> neighbors = graph.neighbors(vertex);
        int k = neighbors.size();
        if (k < 2) {
            return 0.0;
        }

        // Count directed pairs among neighbors: both (i,j) and (j,i) are counted
        int edgeCount = 0;
        for (V ni : neighbors) {
            for (V nj : neighbors) {
                if (!ni.equals(nj) && graph.containsEdge(ni, nj)) {
                    edgeCount++;
                }
            }
        }

        // For both directed and undirected graphs: edgeCount / (k*(k-1))
        // - Directed: edgeCount is the number of directed edges, max possible = k*(k-1)
        // - Undirected: each undirected edge counted twice, max possible k*(k-1)/2 also doubled
        //   so (edgeCount/2) / (k*(k-1)/2) = edgeCount / (k*(k-1))
        // Note: for directed graphs, this uses out-neighbors only (from Graph.neighbors())
        return (double) edgeCount / ((long) k * (k - 1));
    }

    /**
     * Compute the average clustering coefficient over all vertices
     * 计算所有顶点的平均聚类系数
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return the average clustering coefficient | 平均聚类系数
     * @throws NullPointerException if graph is null | 当图为null时抛出
     */
    public static <V> double averageClusteringCoefficient(Graph<V> graph) {
        Objects.requireNonNull(graph, "Graph must not be null");
        if (graph.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (V v : graph.vertices()) {
            sum += clusteringCoefficient(graph, v);
        }
        return sum / graph.vertexCount();
    }

    /**
     * Compute a full summary of graph metrics
     * 计算完整的图度量摘要
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return the graph summary | 图摘要
     * @throws NullPointerException if graph is null | 当图为null时抛出
     */
    public static <V> GraphSummary summary(Graph<V> graph) {
        Objects.requireNonNull(graph, "Graph must not be null");

        int vertexCount = graph.vertexCount();
        int edgeCount = graph.edgeCount();
        double graphDensity = density(graph);
        boolean directed = graph.isDirected();

        // Single component discovery for both connectivity and count
        int componentCount = ConnectedComponentsUtil.count(graph);
        boolean connected = (componentCount <= 1); // 0 components (empty) or 1 = fully connected

        // Single BFS pass from every vertex: reuse results for diameter, radius, and average path length
        int maxEcc = 0;
        int minEcc = Integer.MAX_VALUE;
        long totalDistance = 0;
        long pairCount = 0;

        if (vertexCount > 1) {
            for (V v : graph.vertices()) {
                Map<V, Integer> distances = bfsDistances(graph, v);

                // Eccentricity
                int ecc;
                if (distances.size() < vertexCount) {
                    ecc = Integer.MAX_VALUE;
                } else {
                    ecc = 0;
                    for (int dist : distances.values()) {
                        if (dist > ecc) ecc = dist;
                    }
                }
                if (ecc > maxEcc) maxEcc = ecc;
                if (ecc < minEcc) minEcc = ecc;

                // Average path length accumulation
                for (Map.Entry<V, Integer> entry : distances.entrySet()) {
                    if (!entry.getKey().equals(v)) {
                        totalDistance += entry.getValue();
                        pairCount++;
                    }
                }
            }
        }

        int diam = vertexCount <= 1 ? 0 : maxEcc;
        int rad = vertexCount <= 1 ? 0 : minEcc;
        double avgPathLength = pairCount == 0 ? 0.0 : (double) totalDistance / pairCount;

        return new GraphSummary(
                vertexCount,
                edgeCount,
                graphDensity,
                directed,
                connected,
                componentCount,
                diam,
                rad,
                avgPathLength,
                averageClusteringCoefficient(graph)
        );
    }

    /**
     * BFS distances from a source vertex to all reachable vertices
     * 从源顶点到所有可达顶点的BFS距离
     */
    private static <V> Map<V, Integer> bfsDistances(Graph<V> graph, V source) {
        Map<V, Integer> distances = new HashMap<>();
        Queue<V> queue = new ArrayDeque<>();

        distances.put(source, 0);
        queue.offer(source);

        while (!queue.isEmpty()) {
            V current = queue.poll();
            int currentDist = distances.get(current);

            for (V neighbor : graph.neighbors(current)) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, currentDist + 1);
                    queue.offer(neighbor);
                }
            }
        }

        return distances;
    }
}
