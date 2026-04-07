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

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.node.Edge;

import java.util.*;

/**
 * Centrality Util - Graph Centrality Algorithms
 * 中心性工具类 - 图中心性算法
 *
 * <p>Utility class providing various centrality measures for graphs.</p>
 * <p>提供图的各种中心性度量的工具类。</p>
 *
 * <p><strong>Supported Metrics | 支持的指标:</strong></p>
 * <ul>
 *   <li>Degree Centrality - 度中心性</li>
 *   <li>Closeness Centrality - 接近中心性</li>
 *   <li>Betweenness Centrality - 中介中心性</li>
 *   <li>PageRank - PageRank算法</li>
 *   <li>Eigenvector Centrality - 特征向量中心性</li>
 *   <li>Katz Centrality - Katz中心性</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Degree, closeness, and betweenness centrality - 度、接近和中介中心性</li>
 *   <li>PageRank with configurable damping factor - 可配置阻尼因子的PageRank</li>
 *   <li>Eigenvector and Katz centrality - 特征向量和Katz中心性</li>
 *   <li>Top-k central vertex extraction - 前k个中心顶点提取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Degree centrality
 * Map<String, Double> degree = CentralityUtil.degreeCentrality(graph);
 *
 * // PageRank
 * Map<String, Double> pageRank = CentralityUtil.pageRank(graph, 0.85, 100);
 *
 * // Betweenness centrality
 * Map<String, Double> betweenness = CentralityUtil.betweennessCentrality(graph);
 *
 * // Find top-k central vertices
 * List<String> topVertices = CentralityUtil.topKCentral(graph, 5, CentralityUtil::pageRank);
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
 *   <li>Time complexity: O(V * (V + E)) for betweenness centrality - 时间复杂度: O(V * (V + E))（中介中心性）</li>
 *   <li>Space complexity: O(V^2) - 空间复杂度: O(V^2)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class CentralityUtil {

    /** Default damping factor for PageRank | PageRank默认阻尼因子 */
    public static final double DEFAULT_DAMPING_FACTOR = 0.85;

    /** Default maximum iterations | 默认最大迭代次数 */
    public static final int DEFAULT_MAX_ITERATIONS = 100;

    /** Default convergence threshold | 默认收敛阈值 */
    public static final double DEFAULT_TOLERANCE = 1e-6;

    /** Epsilon for floating-point distance comparison in Brandes' algorithm | Brandes算法中浮点距离比较的容差 */
    private static final double DISTANCE_EPSILON = 1e-10;

    private CentralityUtil() {
        // Utility class
    }

    // ==================== Degree Centrality | 度中心性 ====================

    /**
     * Calculate degree centrality for all vertices.
     * 计算所有顶点的度中心性。
     *
     * <p>Degree centrality is the number of edges connected to a vertex,
     * normalized by the maximum possible degree (n-1).</p>
     * <p>度中心性是与顶点相连的边数，按最大可能度数(n-1)归一化。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return map of vertex to degree centrality | 顶点到度中心性的映射
     */
    public static <V> Map<V, Double> degreeCentrality(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Double> centrality = new HashMap<>();
        int n = graph.vertexCount();
        double normalizer = n > 1 ? 1.0 / (n - 1) : 1.0;

        for (V vertex : graph.vertices()) {
            int degree = graph.isDirected()
                    ? graph.outDegree(vertex) + graph.inDegree(vertex)
                    : graph.neighbors(vertex).size();
            centrality.put(vertex, degree * normalizer);
        }

        return centrality;
    }

    /**
     * Calculate in-degree centrality for directed graphs.
     * 计算有向图的入度中心性。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return map of vertex to in-degree centrality | 顶点到入度中心性的映射
     */
    public static <V> Map<V, Double> inDegreeCentrality(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Double> centrality = new HashMap<>();
        int n = graph.vertexCount();
        double normalizer = n > 1 ? 1.0 / (n - 1) : 1.0;

        for (V vertex : graph.vertices()) {
            centrality.put(vertex, graph.inDegree(vertex) * normalizer);
        }

        return centrality;
    }

    /**
     * Calculate out-degree centrality for directed graphs.
     * 计算有向图的出度中心性。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return map of vertex to out-degree centrality | 顶点到出度中心性的映射
     */
    public static <V> Map<V, Double> outDegreeCentrality(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Double> centrality = new HashMap<>();
        int n = graph.vertexCount();
        double normalizer = n > 1 ? 1.0 / (n - 1) : 1.0;

        for (V vertex : graph.vertices()) {
            centrality.put(vertex, graph.outDegree(vertex) * normalizer);
        }

        return centrality;
    }

    // ==================== Closeness Centrality | 接近中心性 ====================

    /**
     * Calculate closeness centrality for all vertices.
     * 计算所有顶点的接近中心性。
     *
     * <p>Closeness centrality is the reciprocal of the average shortest path distance
     * to all other vertices. Higher values indicate more central vertices.</p>
     * <p>接近中心性是到所有其他顶点的平均最短路径距离的倒数。值越高表示顶点越中心。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return map of vertex to closeness centrality | 顶点到接近中心性的映射
     */
    public static <V> Map<V, Double> closenessCentrality(Graph<V> graph) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Double> centrality = new HashMap<>();
        int n = graph.vertexCount();

        for (V vertex : graph.vertices()) {
            Map<V, Double> distances = ShortestPathUtil.dijkstra(graph, vertex);

            double totalDistance = 0.0;
            int reachable = 0;

            for (V other : graph.vertices()) {
                if (!other.equals(vertex)) {
                    double dist = distances.getOrDefault(other, Double.MAX_VALUE);
                    if (dist < Double.MAX_VALUE) {
                        totalDistance += dist;
                        reachable++;
                    }
                }
            }

            if (reachable > 0 && totalDistance > 0) {
                // Normalized closeness: (n-1) / sum of distances
                double closeness = (n - 1.0) / totalDistance;
                // Adjust for disconnected graphs
                closeness *= ((double) reachable / (n - 1));
                centrality.put(vertex, closeness);
            } else {
                centrality.put(vertex, 0.0);
            }
        }

        return centrality;
    }

    // ==================== Betweenness Centrality | 中介中心性 ====================

    /**
     * Calculate betweenness centrality for all vertices.
     * 计算所有顶点的中介中心性。
     *
     * <p>Betweenness centrality measures the number of shortest paths that pass
     * through each vertex. Uses Brandes' algorithm for efficiency.</p>
     * <p>中介中心性衡量通过每个顶点的最短路径数。使用Brandes算法提高效率。</p>
     *
     * <p><strong>Time Complexity | 时间复杂度:</strong> O(VE)</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return map of vertex to betweenness centrality | 顶点到中介中心性的映射
     */
    public static <V> Map<V, Double> betweennessCentrality(Graph<V> graph) {
        return betweennessCentrality(graph, true);
    }

    /**
     * Calculate betweenness centrality with optional normalization.
     * 计算可选归一化的中介中心性。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param normalized whether to normalize results | 是否归一化结果
     * @return map of vertex to betweenness centrality | 顶点到中介中心性的映射
     */
    public static <V> Map<V, Double> betweennessCentrality(Graph<V> graph, boolean normalized) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Double> centrality = new HashMap<>();
        for (V v : graph.vertices()) {
            centrality.put(v, 0.0);
        }

        // Brandes' algorithm: accumulate single-source contributions
        for (V source : graph.vertices()) {
            brandesSingleSource(graph, source, centrality);
        }

        // Normalize
        if (normalized) {
            int n = graph.vertexCount();
            double factor = graph.isDirected()
                    ? 1.0 / ((long) (n - 1) * (n - 2))
                    : 2.0 / ((long) (n - 1) * (n - 2));

            if (n > 2) {
                for (V v : centrality.keySet()) {
                    centrality.put(v, centrality.get(v) * factor);
                }
            }
        }

        return centrality;
    }

    /**
     * Perform BFS/Dijkstra from a single source and back-propagate dependency scores (Brandes' algorithm).
     * 从单个源执行BFS/Dijkstra并反向传播依赖分数（Brandes算法）。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param centrality the centrality accumulator map (updated in place) | 中心性累加映射（就地更新）
     */
    private static <V> void brandesSingleSource(Graph<V> graph, V source, Map<V, Double> centrality) {
        // Single-source shortest paths
        Deque<V> stack = new ArrayDeque<>();
        Map<V, List<V>> predecessors = new HashMap<>();
        Map<V, Double> dist = new HashMap<>();
        Map<V, Double> sigma = new HashMap<>();

        for (V v : graph.vertices()) {
            predecessors.put(v, new ArrayList<>());
            dist.put(v, Double.MAX_VALUE);
            sigma.put(v, 0.0);
        }

        dist.put(source, 0.0);
        sigma.put(source, 1.0);

        PriorityQueue<VertexDist<V>> queue = new PriorityQueue<>(
                Comparator.comparingDouble(VertexDist::dist));
        queue.offer(new VertexDist<>(source, 0.0));

        while (!queue.isEmpty()) {
            VertexDist<V> current = queue.poll();
            V v = current.vertex();

            if (current.dist() > dist.get(v)) {
                continue;
            }

            stack.push(v);

            for (Edge<V> edge : graph.outEdges(v)) {
                V w = edge.to();
                double vwDist = dist.get(v) + edge.weight();

                if (vwDist < dist.get(w)) {
                    dist.put(w, vwDist);
                    queue.offer(new VertexDist<>(w, vwDist));
                    sigma.put(w, 0.0);
                    predecessors.get(w).clear();
                }

                if (Math.abs(vwDist - dist.get(w)) < DISTANCE_EPSILON) {
                    sigma.merge(w, sigma.get(v), Double::sum);
                    predecessors.get(w).add(v);
                }
            }
        }

        // Accumulation (back-propagation of dependency scores)
        Map<V, Double> delta = new HashMap<>();
        for (V v : graph.vertices()) {
            delta.put(v, 0.0);
        }

        while (!stack.isEmpty()) {
            V w = stack.pop();
            for (V v : predecessors.get(w)) {
                double contribution = (sigma.get(v) / sigma.get(w)) * (1.0 + delta.get(w));
                delta.merge(v, contribution, Double::sum);
            }
            if (!w.equals(source)) {
                centrality.merge(w, delta.get(w), Double::sum);
            }
        }
    }

    // ==================== PageRank | PageRank算法 ====================

    /**
     * Calculate PageRank for all vertices using default parameters.
     * 使用默认参数计算所有顶点的PageRank。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return map of vertex to PageRank score | 顶点到PageRank分数的映射
     */
    public static <V> Map<V, Double> pageRank(Graph<V> graph) {
        return pageRank(graph, DEFAULT_DAMPING_FACTOR, DEFAULT_MAX_ITERATIONS, DEFAULT_TOLERANCE);
    }

    /**
     * Calculate PageRank for all vertices.
     * 计算所有顶点的PageRank。
     *
     * <p>PageRank algorithm simulates a random walker on the graph.
     * The damping factor represents the probability of following a link
     * vs. jumping to a random vertex.</p>
     * <p>PageRank算法模拟图上的随机游走。阻尼因子表示跟随链接与跳转到随机顶点的概率。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param dampingFactor damping factor (typically 0.85) | 阻尼因子（通常为0.85）
     * @param maxIterations maximum iterations | 最大迭代次数
     * @return map of vertex to PageRank score | 顶点到PageRank分数的映射
     */
    public static <V> Map<V, Double> pageRank(Graph<V> graph, double dampingFactor, int maxIterations) {
        return pageRank(graph, dampingFactor, maxIterations, DEFAULT_TOLERANCE);
    }

    /**
     * Calculate PageRank with convergence tolerance.
     * 带收敛容差计算PageRank。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param dampingFactor damping factor (typically 0.85) | 阻尼因子（通常为0.85）
     * @param maxIterations maximum iterations | 最大迭代次数
     * @param tolerance convergence tolerance | 收敛容差
     * @return map of vertex to PageRank score | 顶点到PageRank分数的映射
     */
    public static <V> Map<V, Double> pageRank(Graph<V> graph, double dampingFactor,
                                               int maxIterations, double tolerance) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        int n = graph.vertexCount();
        Map<V, Double> rank = new HashMap<>();
        Map<V, Double> newRank = new HashMap<>();

        // Initialize with uniform distribution
        double initial = 1.0 / n;
        for (V vertex : graph.vertices()) {
            rank.put(vertex, initial);
        }

        // Find dangling nodes (no outgoing edges)
        Set<V> danglingNodes = new HashSet<>();
        for (V vertex : graph.vertices()) {
            if (graph.outDegree(vertex) == 0) {
                danglingNodes.add(vertex);
            }
        }

        // Power iteration
        for (int iter = 0; iter < maxIterations; iter++) {
            // Calculate dangling node contribution
            double danglingSum = 0.0;
            for (V dangling : danglingNodes) {
                danglingSum += rank.get(dangling);
            }
            double danglingContribution = dampingFactor * danglingSum / n;

            // Calculate new ranks
            double maxDiff = 0.0;
            for (V vertex : graph.vertices()) {
                double sum = 0.0;

                // Sum contributions from incoming edges
                for (Edge<V> inEdge : graph.inEdges(vertex)) {
                    V from = inEdge.from();
                    int outDegree = graph.outDegree(from);
                    if (outDegree > 0) {
                        sum += rank.get(from) / outDegree;
                    }
                }

                double value = (1 - dampingFactor) / n + dampingFactor * sum + danglingContribution;
                newRank.put(vertex, value);
                maxDiff = Math.max(maxDiff, Math.abs(value - rank.get(vertex)));
            }

            // Swap and check convergence
            Map<V, Double> temp = rank;
            rank = newRank;
            newRank = temp;

            if (maxDiff < tolerance) {
                break;
            }
        }

        return rank;
    }

    // ==================== Eigenvector Centrality | 特征向量中心性 ====================

    /**
     * Calculate eigenvector centrality for all vertices.
     * 计算所有顶点的特征向量中心性。
     *
     * <p>Eigenvector centrality measures a vertex's importance based on the importance
     * of its neighbors. A vertex with few high-scoring neighbors may have a higher
     * eigenvector centrality than a vertex with many low-scoring neighbors.</p>
     * <p>特征向量中心性根据邻居的重要性来衡量顶点的重要性。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return map of vertex to eigenvector centrality | 顶点到特征向量中心性的映射
     */
    public static <V> Map<V, Double> eigenvectorCentrality(Graph<V> graph) {
        return eigenvectorCentrality(graph, DEFAULT_MAX_ITERATIONS, DEFAULT_TOLERANCE);
    }

    /**
     * Calculate eigenvector centrality with parameters.
     * 带参数计算特征向量中心性。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param maxIterations maximum iterations | 最大迭代次数
     * @param tolerance convergence tolerance | 收敛容差
     * @return map of vertex to eigenvector centrality | 顶点到特征向量中心性的映射
     */
    public static <V> Map<V, Double> eigenvectorCentrality(Graph<V> graph, int maxIterations,
                                                           double tolerance) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Double> centrality = new HashMap<>();
        Map<V, Double> newCentrality = new HashMap<>();

        // Initialize with uniform values
        double initial = 1.0 / Math.sqrt(graph.vertexCount());
        for (V vertex : graph.vertices()) {
            centrality.put(vertex, initial);
        }

        // Power iteration
        for (int iter = 0; iter < maxIterations; iter++) {
            double maxDiff = 0.0;
            double norm = 0.0;

            // Calculate new values
            for (V vertex : graph.vertices()) {
                double sum = 0.0;
                for (V neighbor : graph.neighbors(vertex)) {
                    sum += centrality.get(neighbor);
                }
                // Also consider incoming edges for directed graphs
                if (graph.isDirected()) {
                    for (Edge<V> inEdge : graph.inEdges(vertex)) {
                        if (!graph.neighbors(vertex).contains(inEdge.from())) {
                            sum += centrality.get(inEdge.from());
                        }
                    }
                }
                newCentrality.put(vertex, sum);
                norm += sum * sum;
            }

            // Normalize
            norm = Math.sqrt(norm);
            if (norm > 0) {
                for (V vertex : graph.vertices()) {
                    double normalized = newCentrality.get(vertex) / norm;
                    maxDiff = Math.max(maxDiff, Math.abs(normalized - centrality.get(vertex)));
                    newCentrality.put(vertex, normalized);
                }
            }

            // Swap
            Map<V, Double> temp = centrality;
            centrality = newCentrality;
            newCentrality = temp;

            if (maxDiff < tolerance) {
                break;
            }
        }

        return centrality;
    }

    // ==================== Katz Centrality | Katz中心性 ====================

    /**
     * Calculate Katz centrality for all vertices.
     * 计算所有顶点的Katz中心性。
     *
     * <p>Katz centrality is similar to eigenvector centrality but adds a small
     * constant to prevent nodes with no incoming edges from having zero centrality.</p>
     * <p>Katz中心性类似于特征向量中心性，但添加一个小常数以防止没有入边的节点具有零中心性。</p>
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param alpha attenuation factor (should be less than 1/largest eigenvalue) | 衰减因子
     * @param beta base centrality for each node | 每个节点的基础中心性
     * @return map of vertex to Katz centrality | 顶点到Katz中心性的映射
     */
    public static <V> Map<V, Double> katzCentrality(Graph<V> graph, double alpha, double beta) {
        return katzCentrality(graph, alpha, beta, DEFAULT_MAX_ITERATIONS, DEFAULT_TOLERANCE);
    }

    /**
     * Calculate Katz centrality with full parameters.
     * 带完整参数计算Katz中心性。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param alpha attenuation factor | 衰减因子
     * @param beta base centrality | 基础中心性
     * @param maxIterations maximum iterations | 最大迭代次数
     * @param tolerance convergence tolerance | 收敛容差
     * @return map of vertex to Katz centrality | 顶点到Katz中心性的映射
     */
    public static <V> Map<V, Double> katzCentrality(Graph<V> graph, double alpha, double beta,
                                                     int maxIterations, double tolerance) {
        if (graph == null || graph.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<V, Double> centrality = new HashMap<>();
        Map<V, Double> newCentrality = new HashMap<>();

        // Initialize
        for (V vertex : graph.vertices()) {
            centrality.put(vertex, 0.0);
        }

        // Power iteration
        for (int iter = 0; iter < maxIterations; iter++) {
            double maxDiff = 0.0;

            for (V vertex : graph.vertices()) {
                double sum = beta;
                for (Edge<V> inEdge : graph.inEdges(vertex)) {
                    sum += alpha * centrality.get(inEdge.from());
                }
                newCentrality.put(vertex, sum);
                maxDiff = Math.max(maxDiff, Math.abs(sum - centrality.get(vertex)));
            }

            // Swap
            Map<V, Double> temp = centrality;
            centrality = newCentrality;
            newCentrality = temp;

            if (maxDiff < tolerance) {
                break;
            }
        }

        return centrality;
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Get the top-K most central vertices.
     * 获取前K个最中心的顶点。
     *
     * @param <V> the vertex type | 顶点类型
     * @param centrality the centrality map | 中心性映射
     * @param k the number of top vertices to return | 返回的顶部顶点数
     * @return list of top-K vertices sorted by centrality | 按中心性排序的前K个顶点列表
     */
    public static <V> List<V> topK(Map<V, Double> centrality, int k) {
        if (centrality == null || centrality.isEmpty()) {
            return Collections.emptyList();
        }

        return centrality.entrySet().stream()
                .sorted(Map.Entry.<V, Double>comparingByValue().reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Normalize centrality values to [0, 1] range.
     * 将中心性值归一化到[0, 1]范围。
     *
     * @param <V> the vertex type | 顶点类型
     * @param centrality the centrality map | 中心性映射
     * @return normalized centrality map | 归一化的中心性映射
     */
    public static <V> Map<V, Double> normalize(Map<V, Double> centrality) {
        if (centrality == null || centrality.isEmpty()) {
            return Collections.emptyMap();
        }

        double min = centrality.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = centrality.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double range = max - min;

        if (range == 0) {
            Map<V, Double> result = new HashMap<>();
            for (V v : centrality.keySet()) {
                result.put(v, centrality.isEmpty() ? 0.0 : 1.0);
            }
            return result;
        }

        Map<V, Double> normalized = new HashMap<>();
        for (Map.Entry<V, Double> entry : centrality.entrySet()) {
            normalized.put(entry.getKey(), (entry.getValue() - min) / range);
        }
        return normalized;
    }

    /**
     * Get centrality statistics.
     * 获取中心性统计信息。
     *
     * @param <V> the vertex type | 顶点类型
     * @param centrality the centrality map | 中心性映射
     * @return centrality statistics | 中心性统计信息
     */
    public static <V> CentralityStats getStats(Map<V, Double> centrality) {
        if (centrality == null || centrality.isEmpty()) {
            return new CentralityStats(0, 0, 0, 0, 0);
        }

        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (double value : centrality.values()) {
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        double mean = sum / centrality.size();

        double varianceSum = 0;
        for (double value : centrality.values()) {
            varianceSum += (value - mean) * (value - mean);
        }
        double stdDev = Math.sqrt(varianceSum / centrality.size());

        return new CentralityStats(min, max, mean, stdDev, centrality.size());
    }

    /**
     * Centrality statistics record.
     * 中心性统计信息记录。
     *
     * @param min minimum value | 最小值
     * @param max maximum value | 最大值
     * @param mean average value | 平均值
     * @param stdDev standard deviation | 标准差
     * @param count vertex count | 顶点数
     */
    public record CentralityStats(double min, double max, double mean, double stdDev, int count) {
        @Override
        public String toString() {
            return String.format("CentralityStats{min=%.4f, max=%.4f, mean=%.4f, stdDev=%.4f, count=%d}",
                    min, max, mean, stdDev, count);
        }
    }

    /**
     * Helper record for vertex with distance
     */
    private record VertexDist<V>(V vertex, double dist) {}
}
