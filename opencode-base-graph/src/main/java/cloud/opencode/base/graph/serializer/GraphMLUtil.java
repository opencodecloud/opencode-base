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

package cloud.opencode.base.graph.serializer;

import cloud.opencode.base.graph.DirectedGraph;
import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.UndirectedGraph;
import cloud.opencode.base.graph.node.Edge;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GraphML Util - GraphML Format Import/Export
 * GraphML工具类 - GraphML格式导入/导出
 *
 * <p>Utility class for importing and exporting graphs in GraphML format.
 * GraphML is an XML-based file format for graphs.</p>
 * <p>用于导入和导出GraphML格式图的工具类。GraphML是一种基于XML的图文件格式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Export graphs to GraphML XML | 导出图到GraphML XML</li>
 *   <li>Import graphs from GraphML XML | 从GraphML XML导入图</li>
 *   <li>Support for vertex/edge attributes | 支持顶点/边属性</li>
 *   <li>Support for edge weights | 支持边权重</li>
 *   <li>File and string I/O | 文件和字符串I/O</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Export to GraphML string
 * String graphml = GraphMLUtil.toGraphML(graph);
 *
 * // Export to file
 * GraphMLUtil.writeToFile(graph, Path.of("graph.graphml"));
 *
 * // Import from string
 * Graph<String> graph = GraphMLUtil.fromGraphML(graphmlString);
 *
 * // Import from file
 * Graph<String> graph = GraphMLUtil.readFromFile(Path.of("graph.graphml"));
 *
 * // Export with custom vertex attributes
 * Map<String, Map<String, String>> vertexAttrs = new HashMap<>();
 * vertexAttrs.put("A", Map.of("label", "Node A", "color", "red"));
 * String graphml = GraphMLUtil.toGraphML(graph, vertexAttrs, null);
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
 * @see <a href="http://graphml.graphdrawing.org/">GraphML Specification</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class GraphMLUtil {

    private static final String GRAPHML_HEADER = """
            <?xml version="1.0" encoding="UTF-8"?>
            <graphml xmlns="http://graphml.graphdrawing.org/xmlns"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns
                     http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
            """;

    private static final String GRAPHML_FOOTER = "</graphml>\n";

    private static final Pattern NODE_PATTERN = Pattern.compile(
            "<node\\s+id=\"([^\"]+)\"(?:[^>]*?)(?:/>|>([\\s\\S]*?)</node>)",
            Pattern.MULTILINE);

    private static final Pattern EDGE_PATTERN = Pattern.compile(
            "<edge\\s+(?:id=\"[^\"]*\"\\s+)?source=\"([^\"]+)\"\\s+target=\"([^\"]+)\"(?:[^>]*?)(?:/>|>([\\s\\S]*?)</edge>)",
            Pattern.MULTILINE);

    private static final Pattern DATA_PATTERN = Pattern.compile(
            "<data\\s+key=\"([^\"]+)\">([^<]*)</data>",
            Pattern.MULTILINE);

    private static final Pattern GRAPH_DIRECTED_PATTERN = Pattern.compile(
            "<graph[^>]+edgedefault=\"(directed|undirected)\"",
            Pattern.MULTILINE);

    /**
     * Maximum GraphML input size for parsing (10 MB).
     * GraphML解析输入的最大大小（10 MB）。
     */
    private static final int MAX_INPUT_LENGTH = 10 * 1024 * 1024;

    private GraphMLUtil() {
        // Utility class
    }

    // ==================== Export Methods | 导出方法 ====================

    /**
     * Export graph to GraphML format.
     * 将图导出为GraphML格式。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to export | 要导出的图
     * @return GraphML XML string | GraphML XML字符串
     */
    public static <V> String toGraphML(Graph<V> graph) {
        return toGraphML(graph, null, null);
    }

    /**
     * Export graph to GraphML format with attributes.
     * 将图导出为带属性的GraphML格式。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to export | 要导出的图
     * @param vertexAttributes vertex attributes (vertex -> key -> value) | 顶点属性
     * @param edgeAttributes edge attributes (from:to -> key -> value) | 边属性
     * @return GraphML XML string | GraphML XML字符串
     */
    public static <V> String toGraphML(Graph<V> graph,
                                        Map<V, Map<String, String>> vertexAttributes,
                                        Map<String, Map<String, String>> edgeAttributes) {
        if (graph == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(GRAPHML_HEADER);

        // Collect all attribute keys
        Set<String> nodeAttrKeys = new LinkedHashSet<>();
        Set<String> edgeAttrKeys = new LinkedHashSet<>();
        edgeAttrKeys.add("weight"); // Always include weight

        if (vertexAttributes != null) {
            for (Map<String, String> attrs : vertexAttributes.values()) {
                nodeAttrKeys.addAll(attrs.keySet());
            }
        }
        if (edgeAttributes != null) {
            for (Map<String, String> attrs : edgeAttributes.values()) {
                edgeAttrKeys.addAll(attrs.keySet());
            }
        }

        // Write key definitions
        for (String key : nodeAttrKeys) {
            sb.append(String.format("  <key id=\"%s\" for=\"node\" attr.name=\"%s\" attr.type=\"string\"/>\n",
                    escapeXml(key), escapeXml(key)));
        }
        for (String key : edgeAttrKeys) {
            String type = "weight".equals(key) ? "double" : "string";
            sb.append(String.format("  <key id=\"%s\" for=\"edge\" attr.name=\"%s\" attr.type=\"%s\"/>\n",
                    escapeXml(key), escapeXml(key), type));
        }

        // Write graph
        String edgeDefault = graph.isDirected() ? "directed" : "undirected";
        sb.append(String.format("  <graph id=\"G\" edgedefault=\"%s\">\n", edgeDefault));

        // Write nodes
        for (V vertex : graph.vertices()) {
            String id = escapeXml(String.valueOf(vertex));
            Map<String, String> attrs = vertexAttributes != null ? vertexAttributes.get(vertex) : null;

            if (attrs == null || attrs.isEmpty()) {
                sb.append(String.format("    <node id=\"%s\"/>\n", id));
            } else {
                sb.append(String.format("    <node id=\"%s\">\n", id));
                for (Map.Entry<String, String> attr : attrs.entrySet()) {
                    sb.append(String.format("      <data key=\"%s\">%s</data>\n",
                            escapeXml(attr.getKey()), escapeXml(attr.getValue())));
                }
                sb.append("    </node>\n");
            }
        }

        // Write edges
        Set<String> processedEdges = new HashSet<>();
        int edgeId = 0;
        for (Edge<V> edge : graph.edges()) {
            String from = String.valueOf(edge.from());
            String to = String.valueOf(edge.to());

            // For undirected graphs, avoid duplicates
            String edgeKey = graph.isDirected() ? from + "->" + to :
                    (from.compareTo(to) < 0 ? from + "--" + to : to + "--" + from);

            if (!processedEdges.contains(edgeKey)) {
                processedEdges.add(edgeKey);

                String attrKey = from + ":" + to;
                Map<String, String> attrs = edgeAttributes != null ? edgeAttributes.get(attrKey) : null;

                sb.append(String.format("    <edge id=\"e%d\" source=\"%s\" target=\"%s\">\n",
                        edgeId++, escapeXml(from), escapeXml(to)));

                // Write weight
                if (edge.weight() != 1.0) {
                    sb.append(String.format("      <data key=\"weight\">%s</data>\n", edge.weight()));
                }

                // Write other attributes
                if (attrs != null) {
                    for (Map.Entry<String, String> attr : attrs.entrySet()) {
                        if (!"weight".equals(attr.getKey())) {
                            sb.append(String.format("      <data key=\"%s\">%s</data>\n",
                                    escapeXml(attr.getKey()), escapeXml(attr.getValue())));
                        }
                    }
                }

                sb.append("    </edge>\n");
            }
        }

        sb.append("  </graph>\n");
        sb.append(GRAPHML_FOOTER);

        return sb.toString();
    }

    /**
     * Write graph to GraphML file.
     * 将图写入GraphML文件。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to export | 要导出的图
     * @param path the file path | 文件路径
     * @throws IOException if writing fails | 如果写入失败
     */
    public static <V> void writeToFile(Graph<V> graph, Path path) throws IOException {
        writeToFile(graph, path, null, null);
    }

    /**
     * Write graph to GraphML file with attributes.
     * 将带属性的图写入GraphML文件。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to export | 要导出的图
     * @param path the file path | 文件路径
     * @param vertexAttributes vertex attributes | 顶点属性
     * @param edgeAttributes edge attributes | 边属性
     * @throws IOException if writing fails | 如果写入失败
     */
    public static <V> void writeToFile(Graph<V> graph, Path path,
                                        Map<V, Map<String, String>> vertexAttributes,
                                        Map<String, Map<String, String>> edgeAttributes) throws IOException {
        String graphml = toGraphML(graph, vertexAttributes, edgeAttributes);
        Files.writeString(path, graphml, StandardCharsets.UTF_8);
    }

    // ==================== Import Methods | 导入方法 ====================

    /**
     * Import graph from GraphML string.
     * 从GraphML字符串导入图。
     *
     * @param graphml the GraphML XML string | GraphML XML字符串
     * @return the imported graph | 导入的图
     */
    public static Graph<String> fromGraphML(String graphml) {
        return fromGraphML(graphml, null, null);
    }

    /**
     * Import graph from GraphML string with attribute maps.
     * 从带属性映射的GraphML字符串导入图。
     *
     * @param graphml the GraphML XML string | GraphML XML字符串
     * @param vertexAttributes output map for vertex attributes | 顶点属性输出映射
     * @param edgeAttributes output map for edge attributes | 边属性输出映射
     * @return the imported graph | 导入的图
     */
    public static Graph<String> fromGraphML(String graphml,
                                             Map<String, Map<String, String>> vertexAttributes,
                                             Map<String, Map<String, String>> edgeAttributes) {
        if (graphml == null || graphml.isBlank()) {
            return new DirectedGraph<>();
        }
        if (graphml.length() > MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException(
                    "GraphML input exceeds maximum size of " + MAX_INPUT_LENGTH + " characters");
        }

        // Determine if directed
        boolean directed = true;
        Matcher directedMatcher = GRAPH_DIRECTED_PATTERN.matcher(graphml);
        if (directedMatcher.find()) {
            directed = "directed".equals(directedMatcher.group(1));
        }

        Graph<String> graph = directed ? new DirectedGraph<>() : new UndirectedGraph<>();

        // Parse nodes
        Matcher nodeMatcher = NODE_PATTERN.matcher(graphml);
        while (nodeMatcher.find()) {
            String id = unescapeXml(nodeMatcher.group(1));
            graph.addVertex(id);

            // Parse node attributes
            if (vertexAttributes != null && nodeMatcher.group(2) != null) {
                Map<String, String> attrs = parseDataElements(nodeMatcher.group(2));
                if (!attrs.isEmpty()) {
                    vertexAttributes.put(id, attrs);
                }
            }
        }

        // Parse edges
        Matcher edgeMatcher = EDGE_PATTERN.matcher(graphml);
        while (edgeMatcher.find()) {
            String source = unescapeXml(edgeMatcher.group(1));
            String target = unescapeXml(edgeMatcher.group(2));

            double weight = 1.0;
            Map<String, String> attrs = new HashMap<>();

            if (edgeMatcher.group(3) != null) {
                attrs = parseDataElements(edgeMatcher.group(3));
                if (attrs.containsKey("weight")) {
                    try {
                        weight = Double.parseDouble(attrs.get("weight"));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            graph.addEdge(source, target, weight);

            if (edgeAttributes != null && !attrs.isEmpty()) {
                edgeAttributes.put(source + ":" + target, attrs);
            }
        }

        return graph;
    }

    /**
     * Read graph from GraphML file.
     * 从GraphML文件读取图。
     *
     * @param path the file path | 文件路径
     * @return the imported graph | 导入的图
     * @throws IOException if reading fails | 如果读取失败
     */
    public static Graph<String> readFromFile(Path path) throws IOException {
        return readFromFile(path, null, null);
    }

    /**
     * Read graph from GraphML file with attribute maps.
     * 从带属性映射的GraphML文件读取图。
     *
     * @param path the file path | 文件路径
     * @param vertexAttributes output map for vertex attributes | 顶点属性输出映射
     * @param edgeAttributes output map for edge attributes | 边属性输出映射
     * @return the imported graph | 导入的图
     * @throws IOException if reading fails | 如果读取失败
     */
    public static Graph<String> readFromFile(Path path,
                                              Map<String, Map<String, String>> vertexAttributes,
                                              Map<String, Map<String, String>> edgeAttributes) throws IOException {
        if (Files.size(path) > MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException(
                    "GraphML file exceeds maximum size of " + MAX_INPUT_LENGTH + " bytes");
        }
        String graphml = Files.readString(path, StandardCharsets.UTF_8);
        return fromGraphML(graphml, vertexAttributes, edgeAttributes);
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static Map<String, String> parseDataElements(String content) {
        Map<String, String> data = new HashMap<>();
        Matcher dataMatcher = DATA_PATTERN.matcher(content);
        while (dataMatcher.find()) {
            String key = unescapeXml(dataMatcher.group(1));
            String value = unescapeXml(dataMatcher.group(2));
            data.put(key, value);
        }
        return data;
    }

    private static String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String unescapeXml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
    }
}
