package cloud.opencode.base.graph.security;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.algorithm.ShortestPathUtil;
import cloud.opencode.base.graph.exception.GraphLimitExceededException;
import cloud.opencode.base.graph.exception.GraphTimeoutException;
import cloud.opencode.base.graph.exception.InvalidEdgeException;
import cloud.opencode.base.graph.validation.GraphValidator;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Safe Graph Operations
 * 安全图操作
 *
 * <p>Wrapper for graph operations with resource limits and timeout control.</p>
 * <p>带资源限制和超时控制的图操作包装器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Maximum vertex count limit | 最大顶点数量限制</li>
 *   <li>Maximum edge count limit | 最大边数量限制</li>
 *   <li>Timeout control for algorithms | 算法超时控制</li>
 *   <li>Resource exhaustion protection | 资源耗尽保护</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Safe vertex addition
 * SafeGraphOperations.safeAddVertex(graph, vertex);
 *
 * // Safe edge addition
 * SafeGraphOperations.safeAddEdge(graph, from, to, weight);
 *
 * // Shortest path with timeout
 * List<String> path = SafeGraphOperations.safeShortestPath(graph, source, target);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable static configuration) - 线程安全: 否（可变静态配置）</li>
 *   <li>Null-safe: Yes (validates inputs before operations) - 空值安全: 是（操作前验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class SafeGraphOperations {

    /**
     * Default maximum vertex count
     * 默认最大顶点数量
     */
    public static final int DEFAULT_MAX_VERTICES = 100_000;

    /**
     * Default maximum edge count
     * 默认最大边数量
     */
    public static final int DEFAULT_MAX_EDGES = 1_000_000;

    /**
     * Default maximum traversal depth
     * 默认最大遍历深度
     */
    public static final int DEFAULT_MAX_DEPTH = 10_000;

    /**
     * Default timeout duration
     * 默认超时时间
     */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private static int maxVertices = DEFAULT_MAX_VERTICES;
    private static int maxEdges = DEFAULT_MAX_EDGES;
    private static int maxDepth = DEFAULT_MAX_DEPTH;
    private static Duration timeout = DEFAULT_TIMEOUT;

    private SafeGraphOperations() {
        // Utility class
    }

    /**
     * Configure maximum vertex count
     * 配置最大顶点数量
     *
     * @param max the maximum vertex count | 最大顶点数量
     */
    public static void setMaxVertices(int max) {
        maxVertices = max;
    }

    /**
     * Configure maximum edge count
     * 配置最大边数量
     *
     * @param max the maximum edge count | 最大边数量
     */
    public static void setMaxEdges(int max) {
        maxEdges = max;
    }

    /**
     * Configure maximum traversal depth
     * 配置最大遍历深度
     *
     * @param max the maximum depth | 最大深度
     */
    public static void setMaxDepth(int max) {
        maxDepth = max;
    }

    /**
     * Configure timeout duration
     * 配置超时时间
     *
     * @param duration the timeout duration | 超时时间
     */
    public static void setTimeout(Duration duration) {
        timeout = duration;
    }

    /**
     * Get current maximum vertex count
     * 获取当前最大顶点数量
     *
     * @return maximum vertex count | 最大顶点数量
     */
    public static int getMaxVertices() {
        return maxVertices;
    }

    /**
     * Get current maximum edge count
     * 获取当前最大边数量
     *
     * @return maximum edge count | 最大边数量
     */
    public static int getMaxEdges() {
        return maxEdges;
    }

    /**
     * Get current maximum depth
     * 获取当前最大深度
     *
     * @return maximum depth | 最大深度
     */
    public static int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Get current timeout
     * 获取当前超时时间
     *
     * @return timeout duration | 超时时间
     */
    public static Duration getTimeout() {
        return timeout;
    }

    /**
     * Safely add a vertex with limit check
     * 安全添加顶点（带限制检查）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param vertex the vertex to add | 要添加的顶点
     * @throws GraphLimitExceededException if vertex limit exceeded | 如果超出顶点限制则抛出异常
     */
    public static <V> void safeAddVertex(Graph<V> graph, V vertex) {
        GraphValidator.validateVertex(vertex);

        if (graph.vertexCount() >= maxVertices) {
            throw new GraphLimitExceededException(
                "Maximum vertex count exceeded",
                maxVertices,
                graph.vertexCount() + 1
            );
        }

        graph.addVertex(vertex);
    }

    /**
     * Safely add an edge with limit check
     * 安全添加边（带限制检查）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param from source vertex | 源顶点
     * @param to target vertex | 目标顶点
     * @throws GraphLimitExceededException if edge limit exceeded | 如果超出边限制则抛出异常
     */
    public static <V> void safeAddEdge(Graph<V> graph, V from, V to) {
        safeAddEdge(graph, from, to, 1.0);
    }

    /**
     * Safely add an edge with weight and limit check
     * 安全添加带权重的边（带限制检查）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param from source vertex | 源顶点
     * @param to target vertex | 目标顶点
     * @param weight edge weight | 边权重
     * @throws GraphLimitExceededException if edge limit exceeded | 如果超出边限制则抛出异常
     * @throws InvalidEdgeException if weight is invalid | 如果权重无效则抛出异常
     */
    public static <V> void safeAddEdge(Graph<V> graph, V from, V to, double weight) {
        GraphValidator.validateEdge(from, to, weight);

        if (graph.edgeCount() >= maxEdges) {
            throw new GraphLimitExceededException(
                "Maximum edge count exceeded",
                maxEdges,
                graph.edgeCount() + 1
            );
        }

        if (weight < 0) {
            throw new InvalidEdgeException("Edge weight cannot be negative");
        }

        graph.addEdge(from, to, weight);
    }

    /**
     * Safely compute shortest path with timeout
     * 安全计算最短路径（带超时）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @param target the target vertex | 目标顶点
     * @return list of vertices in the shortest path | 最短路径的顶点列表
     * @throws GraphTimeoutException if computation times out | 如果计算超时则抛出异常
     */
    public static <V> List<V> safeShortestPath(Graph<V> graph, V source, V target) {
        return executeWithTimeout(
            () -> ShortestPathUtil.shortestPath(graph, source, target),
            "Shortest path calculation"
        );
    }

    /**
     * Safely compute Dijkstra distances with timeout
     * 安全计算Dijkstra距离（带超时）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param source the source vertex | 源顶点
     * @return map of vertex to shortest distance | 顶点到最短距离的映射
     * @throws GraphTimeoutException if computation times out | 如果计算超时则抛出异常
     */
    public static <V> Map<V, Double> safeDijkstra(Graph<V> graph, V source) {
        return executeWithTimeout(
            () -> ShortestPathUtil.dijkstra(graph, source),
            "Dijkstra calculation"
        );
    }

    /**
     * Execute a callable with timeout
     * 带超时执行可调用任务
     *
     * @param <T> the result type | 结果类型
     * @param callable the callable to execute | 要执行的可调用任务
     * @param operationName the operation name for error messages | 用于错误消息的操作名称
     * @return the result | 结果
     * @throws GraphTimeoutException if execution times out | 如果执行超时则抛出异常
     */
    public static <T> T executeWithTimeout(Callable<T> callable, String operationName) {
        var result = new java.util.concurrent.atomic.AtomicReference<T>();
        var exception = new java.util.concurrent.atomic.AtomicReference<Throwable>();

        Thread vThread = Thread.startVirtualThread(() -> {
            try {
                result.set(callable.call());
            } catch (Throwable t) {
                exception.set(t);
            }
        });

        try {
            vThread.join(Duration.ofMillis(timeout.toMillis()));
        } catch (InterruptedException e) {
            vThread.interrupt();
            Thread.currentThread().interrupt();
            throw new RuntimeException(operationName + " was interrupted", e);
        }

        if (vThread.isAlive()) {
            vThread.interrupt();
            throw new GraphTimeoutException(operationName + " timed out", timeout);
        }

        Throwable t = exception.get();
        if (t != null) {
            if (t instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(operationName + " failed", t);
        }

        return result.get();
    }

    /**
     * Check if adding vertex would exceed limit
     * 检查添加顶点是否会超出限制
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if adding would exceed limit | 如果添加会超出限制返回true
     */
    public static <V> boolean wouldExceedVertexLimit(Graph<V> graph) {
        return graph.vertexCount() >= maxVertices;
    }

    /**
     * Check if adding edge would exceed limit
     * 检查添加边是否会超出限制
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return true if adding would exceed limit | 如果添加会超出限制返回true
     */
    public static <V> boolean wouldExceedEdgeLimit(Graph<V> graph) {
        return graph.edgeCount() >= maxEdges;
    }

    /**
     * Reset all limits to defaults
     * 重置所有限制为默认值
     */
    public static void resetToDefaults() {
        maxVertices = DEFAULT_MAX_VERTICES;
        maxEdges = DEFAULT_MAX_EDGES;
        maxDepth = DEFAULT_MAX_DEPTH;
        timeout = DEFAULT_TIMEOUT;
    }
}
