package cloud.opencode.base.graph.serializer;

import cloud.opencode.base.graph.DirectedGraph;
import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.UndirectedGraph;
import cloud.opencode.base.graph.node.Edge;

import java.io.*;
import java.util.*;

/**
 * Graph Serializer
 * 图序列化器
 *
 * <p>Utility class for serializing and deserializing graphs.</p>
 * <p>用于序列化和反序列化图的工具类。</p>
 *
 * <p><strong>Supported Formats | 支持的格式:</strong></p>
 * <ul>
 *   <li>DOT format (Graphviz) | DOT格式（Graphviz）</li>
 *   <li>Adjacency list format | 邻接表格式</li>
 *   <li>Edge list format | 边列表格式</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Export to DOT format for Graphviz visualization - 导出为DOT格式用于Graphviz可视化</li>
 *   <li>Serialize and parse adjacency list format - 序列化和解析邻接表格式</li>
 *   <li>Serialize and parse edge list format - 序列化和解析边列表格式</li>
 *   <li>Support for weighted and unweighted edges - 支持有权和无权边</li>
 *   <li>Graph statistics calculation (density, degree distribution) - 图统计信息计算（密度、度分布）</li>
 *   <li>Handles both directed and undirected graphs - 处理有向图和无向图</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Export to DOT format
 * String dot = GraphSerializer.toDot(graph);
 *
 * // Export to adjacency list
 * String adjList = GraphSerializer.toAdjacencyList(graph);
 *
 * // Parse from adjacency list
 * Graph<String> graph = GraphSerializer.fromAdjacencyList(adjListString, true);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns empty string for null graph) - 空值安全: 是（null图返回空字符串）</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns empty string for null graph) - 空值安全: 是（null图返回空字符串）</li>
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
public final class GraphSerializer {

    private GraphSerializer() {
        // Utility class
    }

    // ==================== DOT Format | DOT格式 ====================

    /**
     * Convert graph to DOT format (Graphviz)
     * 将图转换为DOT格式（Graphviz）
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to convert | 要转换的图
     * @return DOT format string | DOT格式字符串
     */
    public static <V> String toDot(Graph<V> graph) {
        return toDot(graph, "G");
    }

    /**
     * Convert graph to DOT format with custom name
     * 将图转换为带自定义名称的DOT格式
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to convert | 要转换的图
     * @param graphName the graph name | 图名称
     * @return DOT format string | DOT格式字符串
     */
    public static <V> String toDot(Graph<V> graph, String graphName) {
        if (graph == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String graphType = graph.isDirected() ? "digraph" : "graph";
        String edgeOperator = graph.isDirected() ? " -> " : " -- ";

        sb.append(graphType).append(" ").append(graphName).append(" {\n");

        // Add vertices (including isolated ones)
        Set<V> verticesWithEdges = new HashSet<>();
        for (Edge<V> edge : graph.edges()) {
            verticesWithEdges.add(edge.from());
            verticesWithEdges.add(edge.to());
        }

        for (V vertex : graph.vertices()) {
            if (!verticesWithEdges.contains(vertex)) {
                sb.append("  \"").append(escapeString(String.valueOf(vertex))).append("\";\n");
            }
        }

        // Add edges
        Set<String> processedEdges = new HashSet<>();
        for (Edge<V> edge : graph.edges()) {
            String from = escapeString(String.valueOf(edge.from()));
            String to = escapeString(String.valueOf(edge.to()));

            // For undirected graphs, avoid duplicate edges
            String edgeKey = graph.isDirected() ? from + "->" + to :
                (from.compareTo(to) < 0 ? from + "--" + to : to + "--" + from);

            if (!processedEdges.contains(edgeKey)) {
                processedEdges.add(edgeKey);

                sb.append("  \"").append(from).append("\"")
                  .append(edgeOperator)
                  .append("\"").append(to).append("\"");

                if (edge.weight() != 1.0) {
                    sb.append(" [label=\"").append(edge.weight()).append("\"]");
                }

                sb.append(";\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    // ==================== Adjacency List Format | 邻接表格式 ====================

    /**
     * Convert graph to adjacency list format
     * 将图转换为邻接表格式
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to convert | 要转换的图
     * @return adjacency list string | 邻接表字符串
     */
    public static <V> String toAdjacencyList(Graph<V> graph) {
        if (graph == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# directed=").append(graph.isDirected()).append("\n");

        for (V vertex : graph.vertices()) {
            sb.append(vertex).append(":");

            List<String> neighbors = new ArrayList<>();
            for (Edge<V> edge : graph.outEdges(vertex)) {
                if (edge.weight() != 1.0) {
                    neighbors.add(edge.to() + "(" + edge.weight() + ")");
                } else {
                    neighbors.add(String.valueOf(edge.to()));
                }
            }

            sb.append(String.join(",", neighbors)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Parse graph from adjacency list format
     * 从邻接表格式解析图
     *
     * @param input the adjacency list string | 邻接表字符串
     * @param directed whether the graph is directed | 图是否有向
     * @return the parsed graph | 解析的图
     */
    public static Graph<String> fromAdjacencyList(String input, boolean directed) {
        Graph<String> graph = directed ? new DirectedGraph<>() : new UndirectedGraph<>();

        if (input == null || input.isBlank()) {
            return graph;
        }

        String[] lines = input.split("\n");
        for (String line : lines) {
            line = line.trim();

            // Skip comments and empty lines
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Parse vertex and neighbors
            int colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                continue;
            }

            String vertex = line.substring(0, colonIndex).trim();
            graph.addVertex(vertex);

            String neighborsStr = line.substring(colonIndex + 1).trim();
            if (neighborsStr.isEmpty()) {
                continue;
            }

            for (String neighborEntry : neighborsStr.split(",")) {
                neighborEntry = neighborEntry.trim();
                if (neighborEntry.isEmpty()) {
                    continue;
                }

                try {
                    // Parse neighbor with optional weight: "B" or "B(2.0)"
                    String neighbor;
                    double weight = 1.0;

                    int parenIndex = neighborEntry.indexOf('(');
                    if (parenIndex != -1) {
                        neighbor = neighborEntry.substring(0, parenIndex).trim();
                        String weightStr = neighborEntry.substring(parenIndex + 1, neighborEntry.length() - 1);
                        weight = Double.parseDouble(weightStr);
                    } else {
                        neighbor = neighborEntry;
                    }

                    graph.addEdge(vertex, neighbor, weight);
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    throw new IllegalArgumentException(
                        "Failed to parse neighbor entry '" + neighborEntry + "' for vertex '" + vertex + "': " + e.getMessage(), e);
                }
            }
        }

        return graph;
    }

    // ==================== Edge List Format | 边列表格式 ====================

    /**
     * Convert graph to edge list format
     * 将图转换为边列表格式
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to convert | 要转换的图
     * @return edge list string | 边列表字符串
     */
    public static <V> String toEdgeList(Graph<V> graph) {
        if (graph == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# directed=").append(graph.isDirected()).append("\n");
        sb.append("# format: from to [weight]\n");

        Set<String> processedEdges = new HashSet<>();
        for (Edge<V> edge : graph.edges()) {
            String from = String.valueOf(edge.from());
            String to = String.valueOf(edge.to());

            // For undirected graphs, avoid duplicate edges
            String edgeKey = graph.isDirected() ? from + "->" + to :
                (from.compareTo(to) < 0 ? from + "--" + to : to + "--" + from);

            if (!processedEdges.contains(edgeKey)) {
                processedEdges.add(edgeKey);

                sb.append(from).append(" ").append(to);
                if (edge.weight() != 1.0) {
                    sb.append(" ").append(edge.weight());
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Parse graph from edge list format
     * 从边列表格式解析图
     *
     * @param input the edge list string | 边列表字符串
     * @param directed whether the graph is directed | 图是否有向
     * @return the parsed graph | 解析的图
     */
    public static Graph<String> fromEdgeList(String input, boolean directed) {
        Graph<String> graph = directed ? new DirectedGraph<>() : new UndirectedGraph<>();

        if (input == null || input.isBlank()) {
            return graph;
        }

        String[] lines = input.split("\n");
        for (String line : lines) {
            line = line.trim();

            // Skip comments and empty lines
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length < 2) {
                continue;
            }

            try {
                String from = parts[0];
                String to = parts[1];
                double weight = parts.length >= 3 ? Double.parseDouble(parts[2]) : 1.0;

                graph.addEdge(from, to, weight);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Failed to parse edge list line '" + line + "': " + e.getMessage(), e);
            }
        }

        return graph;
    }

    // ==================== Statistics | 统计信息 ====================

    /**
     * Get graph statistics as string
     * 获取图统计信息字符串
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph | 图
     * @return statistics string | 统计信息字符串
     */
    public static <V> String getStatistics(Graph<V> graph) {
        if (graph == null) {
            return "Graph: null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Graph Statistics:\n");
        sb.append("  Type: ").append(graph.isDirected() ? "Directed" : "Undirected").append("\n");
        sb.append("  Vertices: ").append(graph.vertexCount()).append("\n");
        sb.append("  Edges: ").append(graph.edgeCount()).append("\n");

        if (graph.vertexCount() > 0) {
            double density = graph.isDirected()
                ? (double) graph.edgeCount() / (graph.vertexCount() * (graph.vertexCount() - 1))
                : (double) (2 * graph.edgeCount()) / (graph.vertexCount() * (graph.vertexCount() - 1));
            sb.append("  Density: ").append(String.format("%.4f", density)).append("\n");
        }

        // Calculate degree statistics
        int minDegree = Integer.MAX_VALUE;
        int maxDegree = Integer.MIN_VALUE;
        int totalDegree = 0;

        for (V vertex : graph.vertices()) {
            int degree = graph.outEdges(vertex).size();
            if (!graph.isDirected()) {
                degree = graph.neighbors(vertex).size();
            }
            minDegree = Math.min(minDegree, degree);
            maxDegree = Math.max(maxDegree, degree);
            totalDegree += degree;
        }

        if (graph.vertexCount() > 0) {
            sb.append("  Min degree: ").append(minDegree == Integer.MAX_VALUE ? 0 : minDegree).append("\n");
            sb.append("  Max degree: ").append(maxDegree == Integer.MIN_VALUE ? 0 : maxDegree).append("\n");
            sb.append("  Avg degree: ").append(String.format("%.2f", (double) totalDegree / graph.vertexCount())).append("\n");
        }

        return sb.toString();
    }

    /**
     * Escape special characters for DOT format
     * 转义DOT格式的特殊字符
     */
    private static String escapeString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
