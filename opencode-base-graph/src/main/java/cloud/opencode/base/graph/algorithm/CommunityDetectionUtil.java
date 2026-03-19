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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Community Detection Util - Graph community detection algorithms
 * 社区检测工具 - 图社区检测算法
 *
 * <p>Provides algorithms for detecting communities (clusters) in graphs.
 * Communities are groups of vertices that are more densely connected
 * to each other than to vertices in other communities.</p>
 * <p>提供用于检测图中社区（聚类）的算法。
 * 社区是相互之间连接比与其他社区中的顶点连接更紧密的顶点组。</p>
 *
 * <p><strong>Algorithms | 算法:</strong></p>
 * <ul>
 *   <li><strong>Louvain</strong> - Fast modularity-based algorithm | 快速模块度优化算法</li>
 *   <li><strong>Label Propagation</strong> - Simple iterative algorithm | 简单迭代算法</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Louvain modularity optimization - Louvain模块度优化</li>
 *   <li>Label propagation community detection - 标签传播社区检测</li>
 *   <li>Modularity calculation - 模块度计算</li>
 *   <li>Per-vertex community lookup - 逐顶点社区查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Detect communities using Louvain algorithm
 * CommunityResult<String> result = CommunityDetectionUtil.louvain(graph);
 * List<Set<String>> communities = result.communities();
 * double modularity = result.modularity();
 *
 * // Detect communities using Label Propagation
 * CommunityResult<String> result = CommunityDetectionUtil.labelPropagation(graph);
 *
 * // Get community for a specific vertex
 * Set<String> community = CommunityDetectionUtil.getCommunity(result, "A");
 *
 * // Calculate modularity for given communities
 * double modularity = CommunityDetectionUtil.calculateModularity(graph, communities);
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
 *   <li>Time complexity: O(V log V) for label propagation - 时间复杂度: O(V log V)（标签传播算法）</li>
 *   <li>Space complexity: O(V + E) - 空间复杂度: O(V + E)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ConnectedComponentsUtil
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class CommunityDetectionUtil {

    /** Default maximum iterations for iterative algorithms */
    private static final int DEFAULT_MAX_ITERATIONS = 100;

    /** Default resolution parameter for Louvain */
    private static final double DEFAULT_RESOLUTION = 1.0;

    private CommunityDetectionUtil() {
        // Utility class
    }

    // ==================== Result Types | 结果类型 ====================

    /**
     * Community detection result.
     * 社区检测结果。
     *
     * @param <V> the vertex type - 顶点类型
     * @param communities the detected communities - 检测到的社区
     * @param vertexToCommunity mapping from vertex to community index - 顶点到社区索引的映射
     * @param modularity the modularity score - 模块度分数
     * @param iterations number of iterations performed - 执行的迭代次数
     */
    public record CommunityResult<V>(
            List<Set<V>> communities,
            Map<V, Integer> vertexToCommunity,
            double modularity,
            int iterations
    ) {
        /**
         * Gets the number of communities.
         * 获取社区数量。
         */
        public int communityCount() {
            return communities.size();
        }

        /**
         * Gets the community containing a vertex.
         * 获取包含顶点的社区。
         */
        public Set<V> getCommunityOf(V vertex) {
            Integer idx = vertexToCommunity.get(vertex);
            if (idx == null || idx < 0 || idx >= communities.size()) {
                return Collections.emptySet();
            }
            return communities.get(idx);
        }
    }

    // ==================== Louvain Algorithm | Louvain 算法 ====================

    /**
     * Detects communities using the Louvain algorithm.
     * 使用 Louvain 算法检测社区。
     *
     * <p>The Louvain algorithm is a greedy optimization method that
     * attempts to optimize the modularity of a partition.</p>
     * <p>Louvain 算法是一种贪婪优化方法，尝试优化分区的模块度。</p>
     *
     * <p><strong>Time Complexity | 时间复杂度:</strong> O(n log n) average case</p>
     *
     * @param <V> the vertex type - 顶点类型
     * @param graph the graph - 图
     * @return the community detection result - 社区检测结果
     */
    public static <V> CommunityResult<V> louvain(Graph<V> graph) {
        return louvain(graph, DEFAULT_RESOLUTION, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Detects communities using the Louvain algorithm with parameters.
     * 使用带参数的 Louvain 算法检测社区。
     *
     * @param <V> the vertex type - 顶点类型
     * @param graph the graph - 图
     * @param resolution the resolution parameter (higher = more communities) - 分辨率参数（越高社区越多）
     * @param maxIterations maximum iterations - 最大迭代次数
     * @return the community detection result - 社区检测结果
     */
    public static <V> CommunityResult<V> louvain(Graph<V> graph, double resolution, int maxIterations) {
        if (graph == null || graph.isEmpty()) {
            return new CommunityResult<>(Collections.emptyList(), Collections.emptyMap(), 0.0, 0);
        }

        List<V> vertices = new ArrayList<>(graph.vertices());
        int n = vertices.size();

        // Calculate total edge weight (sum of all edge weights)
        double totalWeight = calculateTotalWeight(graph);
        if (totalWeight == 0) {
            // No edges - each vertex is its own community
            return singletonCommunities(vertices);
        }

        // Initialize: each vertex in its own community
        Map<V, Integer> vertexToCommunity = new HashMap<>();
        for (int i = 0; i < n; i++) {
            vertexToCommunity.put(vertices.get(i), i);
        }

        // Precompute degree for each vertex
        Map<V, Double> vertexDegree = new HashMap<>();
        for (V v : vertices) {
            vertexDegree.put(v, calculateWeightedDegree(graph, v));
        }

        int iterations = 0;
        boolean improved = true;

        while (improved && iterations < maxIterations) {
            improved = false;
            iterations++;

            // Shuffle vertices for randomness
            Collections.shuffle(vertices);

            for (V vertex : vertices) {
                int currentCommunity = vertexToCommunity.get(vertex);

                // Find neighboring communities
                Set<Integer> neighborCommunities = new HashSet<>();
                neighborCommunities.add(currentCommunity);
                for (V neighbor : graph.neighbors(vertex)) {
                    neighborCommunities.add(vertexToCommunity.get(neighbor));
                }

                // Find best community to move to
                int bestCommunity = currentCommunity;
                double bestGain = 0.0;

                for (int targetCommunity : neighborCommunities) {
                    if (targetCommunity == currentCommunity) continue;

                    double gain = calculateModularityGain(
                            graph, vertex, currentCommunity, targetCommunity,
                            vertexToCommunity, vertexDegree, totalWeight, resolution
                    );

                    if (gain > bestGain) {
                        bestGain = gain;
                        bestCommunity = targetCommunity;
                    }
                }

                // Move to best community if gain is positive
                if (bestCommunity != currentCommunity && bestGain > 0) {
                    vertexToCommunity.put(vertex, bestCommunity);
                    improved = true;
                }
            }
        }

        // Build final communities
        return buildCommunityResult(graph, vertices, vertexToCommunity, iterations);
    }

    private static <V> double calculateModularityGain(
            Graph<V> graph, V vertex, int fromCommunity, int toCommunity,
            Map<V, Integer> vertexToCommunity, Map<V, Double> vertexDegree,
            double totalWeight, double resolution) {

        double ki = vertexDegree.get(vertex);
        double ki_in_from = 0.0;
        double ki_in_to = 0.0;
        double sumTot_from = 0.0;
        double sumTot_to = 0.0;

        for (V neighbor : graph.neighbors(vertex)) {
            double weight = graph.getWeight(vertex, neighbor);
            if (weight == Double.MAX_VALUE) weight = 1.0;

            int neighborCommunity = vertexToCommunity.get(neighbor);
            if (neighborCommunity == fromCommunity) {
                ki_in_from += weight;
            } else if (neighborCommunity == toCommunity) {
                ki_in_to += weight;
            }
        }

        // Calculate sum of degrees in communities
        for (var entry : vertexToCommunity.entrySet()) {
            if (entry.getValue() == fromCommunity && !entry.getKey().equals(vertex)) {
                sumTot_from += vertexDegree.get(entry.getKey());
            } else if (entry.getValue() == toCommunity) {
                sumTot_to += vertexDegree.get(entry.getKey());
            }
        }

        double m2 = 2.0 * totalWeight;

        // Modularity gain formula
        double gain = (ki_in_to - ki_in_from) / m2
                + resolution * ki * (sumTot_from - sumTot_to - ki) / (m2 * m2);

        return gain;
    }

    // ==================== Label Propagation | 标签传播 ====================

    /**
     * Detects communities using Label Propagation algorithm.
     * 使用标签传播算法检测社区。
     *
     * <p>A simple and fast algorithm where each vertex adopts the label
     * that most of its neighbors have.</p>
     * <p>一种简单快速的算法，每个顶点采用其大多数邻居拥有的标签。</p>
     *
     * <p><strong>Time Complexity | 时间复杂度:</strong> O(k * E) where k is iterations</p>
     *
     * @param <V> the vertex type - 顶点类型
     * @param graph the graph - 图
     * @return the community detection result - 社区检测结果
     */
    public static <V> CommunityResult<V> labelPropagation(Graph<V> graph) {
        return labelPropagation(graph, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Detects communities using Label Propagation with max iterations.
     * 使用带最大迭代次数的标签传播算法检测社区。
     *
     * @param <V> the vertex type - 顶点类型
     * @param graph the graph - 图
     * @param maxIterations maximum iterations - 最大迭代次数
     * @return the community detection result - 社区检测结果
     */
    public static <V> CommunityResult<V> labelPropagation(Graph<V> graph, int maxIterations) {
        if (graph == null || graph.isEmpty()) {
            return new CommunityResult<>(Collections.emptyList(), Collections.emptyMap(), 0.0, 0);
        }

        List<V> vertices = new ArrayList<>(graph.vertices());

        // Initialize: each vertex gets its own label (community)
        Map<V, Integer> labels = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++) {
            labels.put(vertices.get(i), i);
        }

        int iterations = 0;
        boolean changed = true;

        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;

            // Shuffle for randomness
            Collections.shuffle(vertices);

            for (V vertex : vertices) {
                Set<V> neighbors = graph.neighbors(vertex);
                if (neighbors.isEmpty()) continue;

                // Count neighbor labels (weighted)
                Map<Integer, Double> labelWeights = new HashMap<>();
                for (V neighbor : neighbors) {
                    int neighborLabel = labels.get(neighbor);
                    double weight = graph.getWeight(vertex, neighbor);
                    if (weight == Double.MAX_VALUE) weight = 1.0;
                    labelWeights.merge(neighborLabel, weight, Double::sum);
                }

                // Find label with maximum weight
                int currentLabel = labels.get(vertex);
                double maxWeight = 0.0;
                List<Integer> maxLabels = new ArrayList<>();

                for (var entry : labelWeights.entrySet()) {
                    if (entry.getValue() > maxWeight) {
                        maxWeight = entry.getValue();
                        maxLabels.clear();
                        maxLabels.add(entry.getKey());
                    } else if (entry.getValue() == maxWeight) {
                        maxLabels.add(entry.getKey());
                    }
                }

                // Randomly pick among ties (but prefer current label if tied)
                int newLabel;
                if (maxLabels.contains(currentLabel)) {
                    newLabel = currentLabel;
                } else {
                    newLabel = maxLabels.get(ThreadLocalRandom.current().nextInt(maxLabels.size()));
                }

                if (newLabel != currentLabel) {
                    labels.put(vertex, newLabel);
                    changed = true;
                }
            }
        }

        return buildCommunityResult(graph, vertices, labels, iterations);
    }

    // ==================== Modularity Calculation | 模块度计算 ====================

    /**
     * Calculates the modularity score for a given community partition.
     * 计算给定社区分区的模块度分数。
     *
     * <p>Modularity measures the strength of division of a network into communities.
     * Higher values indicate better community structure.</p>
     * <p>模块度衡量网络划分为社区的强度。较高的值表示更好的社区结构。</p>
     *
     * @param <V> the vertex type - 顶点类型
     * @param graph the graph - 图
     * @param communities the communities - 社区
     * @return the modularity score [-0.5, 1.0] - 模块度分数
     */
    public static <V> double calculateModularity(Graph<V> graph, List<Set<V>> communities) {
        return calculateModularity(graph, communities, DEFAULT_RESOLUTION);
    }

    /**
     * Calculates the modularity score with resolution parameter.
     * 使用分辨率参数计算模块度分数。
     *
     * @param <V> the vertex type - 顶点类型
     * @param graph the graph - 图
     * @param communities the communities - 社区
     * @param resolution the resolution parameter - 分辨率参数
     * @return the modularity score - 模块度分数
     */
    public static <V> double calculateModularity(Graph<V> graph, List<Set<V>> communities, double resolution) {
        if (graph == null || graph.isEmpty() || communities == null || communities.isEmpty()) {
            return 0.0;
        }

        double totalWeight = calculateTotalWeight(graph);
        if (totalWeight == 0) {
            return 0.0;
        }

        double m2 = 2.0 * totalWeight;
        double modularity = 0.0;

        // Create vertex to community mapping
        Map<V, Integer> vertexToCommunity = new HashMap<>();
        for (int i = 0; i < communities.size(); i++) {
            for (V v : communities.get(i)) {
                vertexToCommunity.put(v, i);
            }
        }

        // Calculate modularity
        for (V u : graph.vertices()) {
            for (V v : graph.vertices()) {
                if (!vertexToCommunity.get(u).equals(vertexToCommunity.get(v))) {
                    continue; // Different communities
                }

                double weight = graph.getWeight(u, v);
                double Auv = (weight != Double.MAX_VALUE) ? weight : 0.0;

                double ku = calculateWeightedDegree(graph, u);
                double kv = calculateWeightedDegree(graph, v);

                modularity += Auv - resolution * (ku * kv) / m2;
            }
        }

        return modularity / m2;
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Gets the community containing a specific vertex.
     * 获取包含特定顶点的社区。
     *
     * @param <V> the vertex type - 顶点类型
     * @param result the community result - 社区结果
     * @param vertex the vertex - 顶点
     * @return the community set or empty set if not found - 社区集合，未找到时返回空集合
     */
    public static <V> Set<V> getCommunity(CommunityResult<V> result, V vertex) {
        if (result == null || vertex == null) {
            return Collections.emptySet();
        }
        return result.getCommunityOf(vertex);
    }

    /**
     * Checks if two vertices are in the same community.
     * 检查两个顶点是否在同一社区。
     *
     * @param <V> the vertex type - 顶点类型
     * @param result the community result - 社区结果
     * @param v1 first vertex - 第一个顶点
     * @param v2 second vertex - 第二个顶点
     * @return true if same community - 如果在同一社区返回 true
     */
    public static <V> boolean inSameCommunity(CommunityResult<V> result, V v1, V v2) {
        if (result == null || v1 == null || v2 == null) {
            return false;
        }
        Integer c1 = result.vertexToCommunity().get(v1);
        Integer c2 = result.vertexToCommunity().get(v2);
        return c1 != null && c1.equals(c2);
    }

    /**
     * Gets communities sorted by size (largest first).
     * 获取按大小排序的社区（最大优先）。
     *
     * @param <V> the vertex type - 顶点类型
     * @param result the community result - 社区结果
     * @return sorted list of communities - 排序后的社区列表
     */
    public static <V> List<Set<V>> getSortedBySize(CommunityResult<V> result) {
        if (result == null || result.communities().isEmpty()) {
            return Collections.emptyList();
        }
        return result.communities().stream()
                .sorted((a, b) -> Integer.compare(b.size(), a.size()))
                .toList();
    }

    // ==================== Private Helpers | 私有辅助方法 ====================

    private static <V> double calculateTotalWeight(Graph<V> graph) {
        double total = 0.0;
        for (Edge<V> edge : graph.edges()) {
            total += edge.weight();
        }
        // For undirected graphs, each edge is counted once
        // For directed graphs, we consider total weight
        if (!graph.isDirected()) {
            return total;
        }
        return total;
    }

    private static <V> double calculateWeightedDegree(Graph<V> graph, V vertex) {
        double degree = 0.0;
        for (Edge<V> edge : graph.outEdges(vertex)) {
            degree += edge.weight();
        }
        if (!graph.isDirected()) {
            // For undirected, outEdges already includes all edges
            return degree;
        }
        // For directed, also count incoming edges
        for (Edge<V> edge : graph.inEdges(vertex)) {
            degree += edge.weight();
        }
        return degree;
    }

    private static <V> CommunityResult<V> singletonCommunities(List<V> vertices) {
        List<Set<V>> communities = new ArrayList<>();
        Map<V, Integer> vertexToCommunity = new HashMap<>();

        for (int i = 0; i < vertices.size(); i++) {
            V v = vertices.get(i);
            communities.add(new HashSet<>(Collections.singleton(v)));
            vertexToCommunity.put(v, i);
        }

        return new CommunityResult<>(communities, vertexToCommunity, 0.0, 0);
    }

    private static <V> CommunityResult<V> buildCommunityResult(
            Graph<V> graph, List<V> vertices, Map<V, Integer> labels, int iterations) {

        // Consolidate labels (remap to 0, 1, 2, ...)
        Map<Integer, Integer> labelRemap = new HashMap<>();
        int nextLabel = 0;
        Map<V, Integer> finalLabels = new HashMap<>();

        for (V v : vertices) {
            int oldLabel = labels.get(v);
            if (!labelRemap.containsKey(oldLabel)) {
                labelRemap.put(oldLabel, nextLabel++);
            }
            finalLabels.put(v, labelRemap.get(oldLabel));
        }

        // Build community sets
        List<Set<V>> communities = new ArrayList<>();
        for (int i = 0; i < nextLabel; i++) {
            communities.add(new HashSet<>());
        }
        for (V v : vertices) {
            communities.get(finalLabels.get(v)).add(v);
        }

        // Remove empty communities and rebuild the vertex-to-community index map
        // to keep indices consistent with list positions
        List<Set<V>> nonEmptyCommunities = new ArrayList<>();
        Map<Integer, Integer> oldToNewIndex = new HashMap<>();
        for (int i = 0; i < communities.size(); i++) {
            if (!communities.get(i).isEmpty()) {
                oldToNewIndex.put(i, nonEmptyCommunities.size());
                nonEmptyCommunities.add(communities.get(i));
            }
        }

        // Remap finalLabels to match the new community list indices
        for (V v : vertices) {
            Integer oldIndex = finalLabels.get(v);
            Integer newIndex = oldToNewIndex.get(oldIndex);
            if (newIndex != null) {
                finalLabels.put(v, newIndex);
            }
        }

        // Recalculate modularity
        double modularity = calculateModularity(graph, nonEmptyCommunities);

        return new CommunityResult<>(nonEmptyCommunities, finalLabels, modularity, iterations);
    }
}
