package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.exception.GraphErrorCode;
import cloud.opencode.base.graph.exception.GraphException;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Floyd-Warshall Util - All-pairs shortest paths
 * Floyd-Warshall工具类 - 全源最短路径
 *
 * <p>Implements the Floyd-Warshall algorithm for computing all-pairs shortest paths.
 * Supports negative edge weights and detects negative cycles.</p>
 * <p>实现Floyd-Warshall算法，计算全源最短路径。支持负权重边并检测负环。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>All-pairs shortest paths - 全源最短路径</li>
 *   <li>Path reconstruction - 路径重建</li>
 *   <li>Negative cycle detection - 负环检测</li>
 *   <li>Distance matrix - 距离矩阵</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Graph<String> graph = new DirectedGraph<>();
 * graph.addEdge("A", "B", 3.0);
 * graph.addEdge("B", "C", 2.0);
 *
 * FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);
 * double dist = result.distance("A", "C");
 * List<String> path = result.path("A", "C");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns empty result for null graph) - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(V^3) - 时间复杂度: O(V^3)</li>
 *   <li>Space complexity: O(V^2) - 空间复杂度: O(V^2)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public final class FloydWarshallUtil {

    /**
     * Maximum number of vertices allowed for Floyd-Warshall computation.
     * Floyd-Warshall计算允许的最大顶点数。
     */
    private static final int MAX_VERTICES = 1000;

    private FloydWarshallUtil() {
        // Utility class
    }

    /**
     * Compute all-pairs shortest paths using Floyd-Warshall algorithm.
     * 使用Floyd-Warshall算法计算全源最短路径。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return all-pairs shortest path result | 全源最短路径结果
     * @throws GraphException with LIMIT_EXCEEDED if graph has more than 1000 vertices |
     *         当图的顶点数超过1000时抛出LIMIT_EXCEEDED
     */
    public static <V> AllPairsResult<V> compute(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return new AllPairsResultImpl<>(false);
        }

        int n = graph.vertexCount();
        if (n > MAX_VERTICES) {
            throw new GraphException(
                    "Graph vertex count " + n + " exceeds maximum " + MAX_VERTICES
                            + " for Floyd-Warshall algorithm",
                    GraphErrorCode.LIMIT_EXCEEDED);
        }

        // Create vertex-to-index mapping for O(1) access
        List<V> vertexList = new ArrayList<>(graph.vertices());
        Map<V, Integer> vertexIndex = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            vertexIndex.put(vertexList.get(i), i);
        }

        // Initialize distance and predecessor matrices
        double[][] dist = new double[n][n];
        int[][] next = new int[n][n];

        for (int i = 0; i < n; i++) {
            Arrays.fill(dist[i], Double.POSITIVE_INFINITY);
            Arrays.fill(next[i], -1);
            dist[i][i] = 0.0;
        }

        // Fill in edge weights
        for (Edge<V> edge : graph.edges()) {
            int i = vertexIndex.get(edge.from());
            int j = vertexIndex.get(edge.to());
            // If multiple edges, keep the minimum weight
            if (edge.weight() < dist[i][j]) {
                dist[i][j] = edge.weight();
                next[i][j] = j;
            }
        }

        // Floyd-Warshall main loop
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (dist[i][k] == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (dist[k][j] == Double.POSITIVE_INFINITY) {
                        continue;
                    }
                    double newDist = dist[i][k] + dist[k][j];
                    if (newDist < dist[i][j]) {
                        dist[i][j] = newDist;
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

        // Check for negative cycles (diagonal < 0)
        boolean hasNegativeCycle = false;
        for (int i = 0; i < n; i++) {
            if (dist[i][i] < 0.0) {
                hasNegativeCycle = true;
                break;
            }
        }

        return new AllPairsResultImpl<>(vertexIndex, dist, next, vertexList, hasNegativeCycle);
    }

    /**
     * All-pairs shortest path result.
     * 全源最短路径结果。
     *
     * @param <V> the vertex type | 顶点类型
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-graph V1.0.3
     */
    public interface AllPairsResult<V> {

        /**
         * Get the shortest distance between two vertices.
         * 获取两顶点间的最短距离。
         *
         * @param from the source vertex | 源顶点
         * @param to the target vertex | 目标顶点
         * @return shortest distance, or Double.POSITIVE_INFINITY if no path |
         *         最短距离，无路径时返回Double.POSITIVE_INFINITY
         */
        double distance(V from, V to);

        /**
         * Get the shortest path between two vertices.
         * 获取两顶点间的最短路径。
         *
         * @param from the source vertex | 源顶点
         * @param to the target vertex | 目标顶点
         * @return list of vertices in the shortest path, or empty list if no path |
         *         最短路径的顶点列表，无路径时返回空列表
         */
        List<V> path(V from, V to);

        /**
         * Check if a negative cycle exists in the graph.
         * 检查图中是否存在负环。
         *
         * @return true if a negative cycle exists | 如果存在负环返回true
         */
        boolean hasNegativeCycle();

        /**
         * Get the full distance matrix as a nested map.
         * 获取完整的距离矩阵（嵌套映射形式）。
         *
         * @return distance matrix | 距离矩阵
         */
        Map<V, Map<V, Double>> distanceMatrix();
    }

    /**
     * Implementation of AllPairsResult.
     */
    private static final class AllPairsResultImpl<V> implements AllPairsResult<V> {

        private final Map<V, Integer> vertexIndex;
        private final double[][] dist;
        private final int[][] next;
        private final List<V> vertexList;
        private final boolean hasNegativeCycle;
        private final boolean empty;

        /**
         * Constructor for empty result.
         */
        AllPairsResultImpl(boolean hasNegativeCycle) {
            this.vertexIndex = Collections.emptyMap();
            this.dist = new double[0][0];
            this.next = new int[0][0];
            this.vertexList = Collections.emptyList();
            this.hasNegativeCycle = hasNegativeCycle;
            this.empty = true;
        }

        /**
         * Constructor for computed result.
         */
        AllPairsResultImpl(Map<V, Integer> vertexIndex,
                           double[][] dist,
                           int[][] next,
                           List<V> vertexList,
                           boolean hasNegativeCycle) {
            this.vertexIndex = vertexIndex;
            this.dist = dist;
            this.next = next;
            this.vertexList = vertexList;
            this.hasNegativeCycle = hasNegativeCycle;
            this.empty = false;
        }

        @Override
        public double distance(V from, V to) {
            if (empty || hasNegativeCycle) {
                return Double.POSITIVE_INFINITY;
            }
            Integer i = vertexIndex.get(from);
            Integer j = vertexIndex.get(to);
            if (i == null || j == null) {
                return Double.POSITIVE_INFINITY;
            }
            return dist[i][j];
        }

        @Override
        public List<V> path(V from, V to) {
            if (empty) {
                return Collections.emptyList();
            }
            if (hasNegativeCycle) {
                return Collections.emptyList();
            }
            Integer i = vertexIndex.get(from);
            Integer j = vertexIndex.get(to);
            if (i == null || j == null) {
                return Collections.emptyList();
            }
            if (dist[i][j] == Double.POSITIVE_INFINITY) {
                return Collections.emptyList();
            }
            if (from.equals(to)) {
                return List.of(from);
            }

            List<V> path = new ArrayList<>();
            int current = i;
            path.add(vertexList.get(current));
            while (current != j) {
                current = next[current][j];
                if (current == -1) {
                    return Collections.emptyList();
                }
                path.add(vertexList.get(current));
                // Safety: path should not exceed vertex count
                if (path.size() > vertexList.size()) {
                    return Collections.emptyList();
                }
            }

            return path;
        }

        @Override
        public boolean hasNegativeCycle() {
            return hasNegativeCycle;
        }

        @Override
        public Map<V, Map<V, Double>> distanceMatrix() {
            if (empty || hasNegativeCycle) {
                return Collections.emptyMap();
            }
            Map<V, Map<V, Double>> matrix = new LinkedHashMap<>();
            for (int i = 0; i < vertexList.size(); i++) {
                V from = vertexList.get(i);
                Map<V, Double> row = new LinkedHashMap<>();
                for (int j = 0; j < vertexList.size(); j++) {
                    row.put(vertexList.get(j), dist[i][j]);
                }
                matrix.put(from, Collections.unmodifiableMap(row));
            }
            return Collections.unmodifiableMap(matrix);
        }
    }
}
