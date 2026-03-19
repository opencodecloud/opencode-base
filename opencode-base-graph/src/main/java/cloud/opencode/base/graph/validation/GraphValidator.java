package cloud.opencode.base.graph.validation;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.exception.InvalidEdgeException;
import cloud.opencode.base.graph.exception.InvalidVertexException;
import cloud.opencode.base.graph.node.Edge;

import java.util.ArrayList;
import java.util.List;

/**
 * Graph Validator
 * 图验证器
 *
 * <p>Utility class for validating graph structures and inputs.</p>
 * <p>用于验证图结构和输入的工具类。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Vertex validation | 顶点验证</li>
 *   <li>Edge validation | 边验证</li>
 *   <li>Graph structure validation | 图结构验证</li>
 *   <li>Weight validation | 权重验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validate a vertex
 * GraphValidator.validateVertex(vertex);
 *
 * // Validate an edge
 * GraphValidator.validateEdge(from, to, weight);
 *
 * // Validate entire graph structure
 * ValidationResult result = GraphValidator.validateGraph(graph);
 * if (result.hasErrors()) {
 *     // Handle errors
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (throws exception for null vertices, returns error result for null graph) - 空值安全: 是（null顶点抛出异常，null图返回错误结果）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class GraphValidator {

    private GraphValidator() {
        // Utility class
    }

    /**
     * Validate a vertex
     * 验证顶点
     *
     * @param <V> the vertex type | 顶点类型
     * @param vertex the vertex to validate | 要验证的顶点
     * @throws InvalidVertexException if vertex is invalid | 如果顶点无效则抛出异常
     */
    public static <V> void validateVertex(V vertex) {
        if (vertex == null) {
            throw new InvalidVertexException("Vertex cannot be null");
        }
    }

    /**
     * Validate an edge
     * 验证边
     *
     * @param <V> the vertex type | 顶点类型
     * @param from source vertex | 源顶点
     * @param to target vertex | 目标顶点
     * @throws InvalidVertexException if vertices are invalid | 如果顶点无效则抛出异常
     */
    public static <V> void validateEdge(V from, V to) {
        validateVertex(from);
        validateVertex(to);
    }

    /**
     * Validate an edge with weight
     * 验证带权重的边
     *
     * @param <V> the vertex type | 顶点类型
     * @param from source vertex | 源顶点
     * @param to target vertex | 目标顶点
     * @param weight edge weight | 边权重
     * @throws InvalidVertexException if vertices are invalid | 如果顶点无效则抛出异常
     * @throws InvalidEdgeException if weight is invalid | 如果权重无效则抛出异常
     */
    public static <V> void validateEdge(V from, V to, double weight) {
        validateEdge(from, to);
        validateWeight(weight);
    }

    /**
     * Validate edge weight
     * 验证边权重
     *
     * @param weight the weight to validate | 要验证的权重
     * @throws InvalidEdgeException if weight is invalid | 如果权重无效则抛出异常
     */
    public static void validateWeight(double weight) {
        if (Double.isNaN(weight)) {
            throw new InvalidEdgeException("Edge weight cannot be NaN");
        }
        if (Double.isInfinite(weight)) {
            throw new InvalidEdgeException("Edge weight cannot be infinite");
        }
    }

    /**
     * Validate graph structure
     * 验证图结构
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to validate | 要验证的图
     * @return validation result | 验证结果
     */
    public static <V> ValidationResult validateGraph(Graph<V> graph) {
        if (graph == null) {
            return ValidationResult.error("Graph cannot be null");
        }

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Check for self-loops
        for (Edge<V> edge : graph.edges()) {
            if (edge.from().equals(edge.to())) {
                warnings.add("Self-loop detected at vertex: " + edge.from());
            }
        }

        // Check for isolated vertices
        for (V vertex : graph.vertices()) {
            if (graph.neighbors(vertex).isEmpty() && graph.inEdges(vertex).isEmpty()) {
                warnings.add("Isolated vertex detected: " + vertex);
            }
        }

        // Check for negative weights (warning for Dijkstra)
        for (Edge<V> edge : graph.edges()) {
            if (edge.weight() < 0) {
                warnings.add("Negative edge weight detected: " + edge.from() + " -> " + edge.to() + " = " + edge.weight());
            }
        }

        // Check for invalid weights
        for (Edge<V> edge : graph.edges()) {
            if (Double.isNaN(edge.weight())) {
                errors.add("NaN edge weight: " + edge.from() + " -> " + edge.to());
            }
            if (Double.isInfinite(edge.weight())) {
                errors.add("Infinite edge weight: " + edge.from() + " -> " + edge.to());
            }
        }

        return new ValidationResult(warnings, errors);
    }

    /**
     * Validate graph structure and throw exception if errors found
     * 验证图结构，如果发现错误则抛出异常
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to validate | 要验证的图
     * @throws InvalidEdgeException if graph has errors | 如果图有错误则抛出异常
     */
    public static <V> void validateGraphStructure(Graph<V> graph) {
        ValidationResult result = validateGraph(graph);
        if (result.hasErrors()) {
            throw new InvalidEdgeException("Graph validation failed: " + String.join(", ", result.errors()));
        }
    }

    /**
     * Check if graph is a valid DAG (Directed Acyclic Graph)
     * 检查图是否为有效的DAG（有向无环图）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to check | 要检查的图
     * @return validation result | 验证结果
     */
    public static <V> ValidationResult validateDAG(Graph<V> graph) {
        if (graph == null) {
            return ValidationResult.error("Graph cannot be null");
        }
        if (!graph.isDirected()) {
            return ValidationResult.error("Graph must be directed to be a DAG");
        }

        ValidationResult baseResult = validateGraph(graph);

        // Check for cycles using DFS
        if (hasCycle(graph)) {
            return baseResult.merge(ValidationResult.error("Graph contains a cycle and is not a DAG"));
        }

        return baseResult;
    }

    /**
     * Check if graph has a cycle
     * 检查图是否有环
     */
    private static <V> boolean hasCycle(Graph<V> graph) {
        java.util.Set<V> visited = new java.util.HashSet<>();
        java.util.Set<V> recursionStack = new java.util.HashSet<>();

        for (V vertex : graph.vertices()) {
            if (hasCycleUtil(graph, vertex, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    private static <V> boolean hasCycleUtil(Graph<V> graph, V vertex,
                                             java.util.Set<V> visited,
                                             java.util.Set<V> recursionStack) {
        if (recursionStack.contains(vertex)) {
            return true;
        }
        if (visited.contains(vertex)) {
            return false;
        }

        visited.add(vertex);
        recursionStack.add(vertex);

        for (V neighbor : graph.neighbors(vertex)) {
            if (hasCycleUtil(graph, neighbor, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(vertex);
        return false;
    }

    /**
     * Validate vertex exists in graph
     * 验证顶点在图中存在
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param vertex the vertex to check | 要检查的顶点
     * @return true if vertex exists | 如果顶点存在返回true
     */
    public static <V> boolean vertexExists(Graph<V> graph, V vertex) {
        return graph != null && vertex != null && graph.containsVertex(vertex);
    }

    /**
     * Validate edge exists in graph
     * 验证边在图中存在
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @param from source vertex | 源顶点
     * @param to target vertex | 目标顶点
     * @return true if edge exists | 如果边存在返回true
     */
    public static <V> boolean edgeExists(Graph<V> graph, V from, V to) {
        return graph != null && from != null && to != null && graph.containsEdge(from, to);
    }
}
