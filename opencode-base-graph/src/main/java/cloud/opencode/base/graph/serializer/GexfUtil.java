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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GEXF Util - GEXF Format Import/Export
 * GEXF工具类 - GEXF格式导入/导出
 *
 * <p>Utility class for importing and exporting graphs in GEXF format.
 * GEXF (Graph Exchange XML Format) is designed for complex network analysis
 * and visualization tools like Gephi.</p>
 * <p>用于导入和导出GEXF格式图的工具类。GEXF是为复杂网络分析和可视化工具（如Gephi）设计的格式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Export graphs to GEXF XML | 导出图到GEXF XML</li>
 *   <li>Import graphs from GEXF XML | 从GEXF XML导入图</li>
 *   <li>Support for vertex/edge attributes | 支持顶点/边属性</li>
 *   <li>Support for edge weights | 支持边权重</li>
 *   <li>Support for visualization data (positions, colors, sizes) | 支持可视化数据</li>
 *   <li>Compatible with Gephi | 兼容Gephi</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Export to GEXF string
 * String gexf = GexfUtil.toGexf(graph);
 *
 * // Export to file
 * GexfUtil.writeToFile(graph, Path.of("graph.gexf"));
 *
 * // Export with visualization data
 * Map<String, VisualData> visuals = new HashMap<>();
 * visuals.put("A", new VisualData(100, 200, 10, "#FF0000"));
 * String gexf = GexfUtil.toGexf(graph, null, visuals);
 *
 * // Import from file
 * Graph<String> graph = GexfUtil.readFromFile(Path.of("graph.gexf"));
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
 * @see <a href="https://gephi.org/gexf/format/">GEXF Format Specification</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public final class GexfUtil {

    private static final String GEXF_VERSION = "1.3";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private static final Pattern NODE_PATTERN = Pattern.compile(
            "<node\\s+id=\"([^\"]+)\"(?:\\s+label=\"([^\"]*)\")?(?:[^>]*?)(?:/>|>([\\s\\S]*?)</node>)",
            Pattern.MULTILINE);

    private static final Pattern EDGE_PATTERN = Pattern.compile(
            "<edge\\s+(?:id=\"[^\"]*\"\\s+)?source=\"([^\"]+)\"\\s+target=\"([^\"]+)\"(?:\\s+weight=\"([^\"]*)\")?(?:[^>]*?)(?:/>|>([\\s\\S]*?)</edge>)",
            Pattern.MULTILINE);

    private static final Pattern GRAPH_MODE_PATTERN = Pattern.compile(
            "<graph[^>]+defaultedgetype=\"(directed|undirected)\"",
            Pattern.MULTILINE);

    private static final Pattern ATTVALUE_PATTERN = Pattern.compile(
            "<attvalue\\s+for=\"([^\"]+)\"\\s+value=\"([^\"]*)\"",
            Pattern.MULTILINE);

    private static final Pattern VIZ_POSITION_PATTERN = Pattern.compile(
            "<viz:position\\s+x=\"([^\"]+)\"\\s+y=\"([^\"]+)\"(?:\\s+z=\"([^\"]*)\")?",
            Pattern.MULTILINE);

    private static final Pattern VIZ_SIZE_PATTERN = Pattern.compile(
            "<viz:size\\s+value=\"([^\"]+)\"",
            Pattern.MULTILINE);

    private static final Pattern VIZ_COLOR_PATTERN = Pattern.compile(
            "<viz:color\\s+r=\"([^\"]+)\"\\s+g=\"([^\"]+)\"\\s+b=\"([^\"]+)\"(?:\\s+a=\"([^\"]*)\")?",
            Pattern.MULTILINE);

    /**
     * Maximum GEXF input size for parsing (10 MB).
     * GEXF解析输入的最大大小（10 MB）。
     */
    private static final int MAX_INPUT_LENGTH = 10 * 1024 * 1024;

    private GexfUtil() {
        // Utility class
    }

    // ==================== Export Methods | 导出方法 ====================

    /**
     * Export graph to GEXF format.
     * 将图导出为GEXF格式。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to export | 要导出的图
     * @return GEXF XML string | GEXF XML字符串
     */
    public static <V> String toGexf(Graph<V> graph) {
        return toGexf(graph, null, null);
    }

    /**
     * Export graph to GEXF format with attributes and visual data.
     * 将图导出为带属性和可视化数据的GEXF格式。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to export | 要导出的图
     * @param vertexAttributes vertex attributes (vertex -> key -> value) | 顶点属性
     * @param visualData visualization data (vertex -> visual data) | 可视化数据
     * @return GEXF XML string | GEXF XML字符串
     */
    public static <V> String toGexf(Graph<V> graph,
                                     Map<V, Map<String, String>> vertexAttributes,
                                     Map<V, VisualData> visualData) {
        if (graph == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append(String.format("""
                <gexf xmlns="http://gexf.net/1.3"
                      xmlns:viz="http://gexf.net/1.3/viz"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="http://gexf.net/1.3 http://gexf.net/1.3/gexf.xsd"
                      version="%s">
                """, GEXF_VERSION));

        // Meta
        sb.append("  <meta lastmodifieddate=\"")
                .append(LocalDateTime.now().format(DATE_FORMATTER))
                .append("\">\n");
        sb.append("    <creator>OpenCode Graph Library</creator>\n");
        sb.append("    <description>Graph exported by opencode-base-graph</description>\n");
        sb.append("  </meta>\n");

        // Graph
        String edgeType = graph.isDirected() ? "directed" : "undirected";
        sb.append(String.format("  <graph mode=\"static\" defaultedgetype=\"%s\">\n", edgeType));

        // Attribute definitions
        Set<String> attrKeys = new LinkedHashSet<>();
        if (vertexAttributes != null) {
            for (Map<String, String> attrs : vertexAttributes.values()) {
                attrKeys.addAll(attrs.keySet());
            }
        }

        if (!attrKeys.isEmpty()) {
            sb.append("    <attributes class=\"node\">\n");
            int attrId = 0;
            Map<String, Integer> attrIdMap = new HashMap<>();
            for (String key : attrKeys) {
                attrIdMap.put(key, attrId);
                sb.append(String.format("      <attribute id=\"%d\" title=\"%s\" type=\"string\"/>\n",
                        attrId++, escapeXml(key)));
            }
            sb.append("    </attributes>\n");

            // Store mapping for later use
            for (Map.Entry<String, Integer> entry : attrIdMap.entrySet()) {
                attrKeys.remove(entry.getKey());
            }
        }

        // Nodes
        sb.append("    <nodes>\n");
        int attrCounter = 0;
        Map<String, Integer> attrIdMap = new HashMap<>();
        for (String key : attrKeys) {
            attrIdMap.put(key, attrCounter++);
        }
        if (vertexAttributes != null) {
            for (Map<String, String> attrs : vertexAttributes.values()) {
                for (String key : attrs.keySet()) {
                    if (!attrIdMap.containsKey(key)) {
                        attrIdMap.put(key, attrCounter++);
                    }
                }
            }
        }

        for (V vertex : graph.vertices()) {
            String id = escapeXml(String.valueOf(vertex));
            sb.append(String.format("      <node id=\"%s\" label=\"%s\"", id, id));

            Map<String, String> attrs = vertexAttributes != null ? vertexAttributes.get(vertex) : null;
            VisualData visual = visualData != null ? visualData.get(vertex) : null;

            if ((attrs == null || attrs.isEmpty()) && visual == null) {
                sb.append("/>\n");
            } else {
                sb.append(">\n");

                // Attributes
                if (attrs != null && !attrs.isEmpty()) {
                    sb.append("        <attvalues>\n");
                    for (Map.Entry<String, String> attr : attrs.entrySet()) {
                        Integer attrId = attrIdMap.get(attr.getKey());
                        if (attrId != null) {
                            sb.append(String.format("          <attvalue for=\"%d\" value=\"%s\"/>\n",
                                    attrId, escapeXml(attr.getValue())));
                        }
                    }
                    sb.append("        </attvalues>\n");
                }

                // Visual data
                if (visual != null) {
                    if (visual.x() != 0 || visual.y() != 0) {
                        sb.append(String.format("        <viz:position x=\"%f\" y=\"%f\" z=\"0.0\"/>\n",
                                visual.x(), visual.y()));
                    }
                    if (visual.size() > 0) {
                        sb.append(String.format("        <viz:size value=\"%f\"/>\n", visual.size()));
                    }
                    if (visual.color() != null) {
                        int[] rgb = parseColor(visual.color());
                        sb.append(String.format("        <viz:color r=\"%d\" g=\"%d\" b=\"%d\"/>\n",
                                rgb[0], rgb[1], rgb[2]));
                    }
                }

                sb.append("      </node>\n");
            }
        }
        sb.append("    </nodes>\n");

        // Edges
        sb.append("    <edges>\n");
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

                sb.append(String.format("      <edge id=\"%d\" source=\"%s\" target=\"%s\"",
                        edgeId++, escapeXml(from), escapeXml(to)));

                if (edge.weight() != 1.0) {
                    sb.append(String.format(" weight=\"%f\"", edge.weight()));
                }

                sb.append("/>\n");
            }
        }
        sb.append("    </edges>\n");

        sb.append("  </graph>\n");
        sb.append("</gexf>\n");

        return sb.toString();
    }

    /**
     * Write graph to GEXF file.
     * 将图写入GEXF文件。
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
     * Write graph to GEXF file with attributes and visual data.
     * 将带属性和可视化数据的图写入GEXF文件。
     *
     * @param <V> the vertex type | 顶点类型
     * @param graph the graph to export | 要导出的图
     * @param path the file path | 文件路径
     * @param vertexAttributes vertex attributes | 顶点属性
     * @param visualData visualization data | 可视化数据
     * @throws IOException if writing fails | 如果写入失败
     */
    public static <V> void writeToFile(Graph<V> graph, Path path,
                                        Map<V, Map<String, String>> vertexAttributes,
                                        Map<V, VisualData> visualData) throws IOException {
        String gexf = toGexf(graph, vertexAttributes, visualData);
        Files.writeString(path, gexf, StandardCharsets.UTF_8);
    }

    // ==================== Import Methods | 导入方法 ====================

    /**
     * Import graph from GEXF string.
     * 从GEXF字符串导入图。
     *
     * @param gexf the GEXF XML string | GEXF XML字符串
     * @return the imported graph | 导入的图
     */
    public static Graph<String> fromGexf(String gexf) {
        return fromGexf(gexf, null, null);
    }

    /**
     * Import graph from GEXF string with attribute and visual data maps.
     * 从带属性和可视化数据映射的GEXF字符串导入图。
     *
     * @param gexf the GEXF XML string | GEXF XML字符串
     * @param vertexAttributes output map for vertex attributes | 顶点属性输出映射
     * @param visualData output map for visual data | 可视化数据输出映射
     * @return the imported graph | 导入的图
     */
    public static Graph<String> fromGexf(String gexf,
                                          Map<String, Map<String, String>> vertexAttributes,
                                          Map<String, VisualData> visualData) {
        if (gexf == null || gexf.isBlank()) {
            return new DirectedGraph<>();
        }
        if (gexf.length() > MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException(
                    "GEXF input exceeds maximum size of " + MAX_INPUT_LENGTH + " characters");
        }

        // Determine if directed
        boolean directed = true;
        Matcher modeMatcher = GRAPH_MODE_PATTERN.matcher(gexf);
        if (modeMatcher.find()) {
            directed = "directed".equals(modeMatcher.group(1));
        }

        Graph<String> graph = directed ? new DirectedGraph<>() : new UndirectedGraph<>();

        // Parse nodes
        Matcher nodeMatcher = NODE_PATTERN.matcher(gexf);
        while (nodeMatcher.find()) {
            String id = unescapeXml(nodeMatcher.group(1));
            graph.addVertex(id);

            String nodeContent = nodeMatcher.group(3);
            if (nodeContent != null) {
                // Parse attributes
                if (vertexAttributes != null) {
                    Map<String, String> attrs = new HashMap<>();
                    Matcher attrMatcher = ATTVALUE_PATTERN.matcher(nodeContent);
                    while (attrMatcher.find()) {
                        attrs.put(unescapeXml(attrMatcher.group(1)), unescapeXml(attrMatcher.group(2)));
                    }
                    if (!attrs.isEmpty()) {
                        vertexAttributes.put(id, attrs);
                    }
                }

                // Parse visual data
                if (visualData != null) {
                    double x = 0, y = 0, size = 0;
                    String color = null;

                    Matcher posMatcher = VIZ_POSITION_PATTERN.matcher(nodeContent);
                    if (posMatcher.find()) {
                        x = Double.parseDouble(posMatcher.group(1));
                        y = Double.parseDouble(posMatcher.group(2));
                    }

                    Matcher sizeMatcher = VIZ_SIZE_PATTERN.matcher(nodeContent);
                    if (sizeMatcher.find()) {
                        size = Double.parseDouble(sizeMatcher.group(1));
                    }

                    Matcher colorMatcher = VIZ_COLOR_PATTERN.matcher(nodeContent);
                    if (colorMatcher.find()) {
                        int r = Integer.parseInt(colorMatcher.group(1));
                        int g = Integer.parseInt(colorMatcher.group(2));
                        int b = Integer.parseInt(colorMatcher.group(3));
                        color = String.format("#%02X%02X%02X", r, g, b);
                    }

                    if (x != 0 || y != 0 || size != 0 || color != null) {
                        visualData.put(id, new VisualData(x, y, size, color));
                    }
                }
            }
        }

        // Parse edges
        Matcher edgeMatcher = EDGE_PATTERN.matcher(gexf);
        while (edgeMatcher.find()) {
            String source = unescapeXml(edgeMatcher.group(1));
            String target = unescapeXml(edgeMatcher.group(2));

            double weight = 1.0;
            if (edgeMatcher.group(3) != null && !edgeMatcher.group(3).isEmpty()) {
                try {
                    weight = Double.parseDouble(edgeMatcher.group(3));
                } catch (NumberFormatException ignored) {
                }
            }

            graph.addEdge(source, target, weight);
        }

        return graph;
    }

    /**
     * Read graph from GEXF file.
     * 从GEXF文件读取图。
     *
     * @param path the file path | 文件路径
     * @return the imported graph | 导入的图
     * @throws IOException if reading fails | 如果读取失败
     */
    public static Graph<String> readFromFile(Path path) throws IOException {
        return readFromFile(path, null, null);
    }

    /**
     * Read graph from GEXF file with attribute and visual data maps.
     * 从带属性和可视化数据映射的GEXF文件读取图。
     *
     * @param path the file path | 文件路径
     * @param vertexAttributes output map for vertex attributes | 顶点属性输出映射
     * @param visualData output map for visual data | 可视化数据输出映射
     * @return the imported graph | 导入的图
     * @throws IOException if reading fails | 如果读取失败
     */
    public static Graph<String> readFromFile(Path path,
                                              Map<String, Map<String, String>> vertexAttributes,
                                              Map<String, VisualData> visualData) throws IOException {
        if (Files.size(path) > MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException(
                    "GEXF file exceeds maximum size of " + MAX_INPUT_LENGTH + " bytes");
        }
        String gexf = Files.readString(path, StandardCharsets.UTF_8);
        return fromGexf(gexf, vertexAttributes, visualData);
    }

    // ==================== Visual Data | 可视化数据 ====================

    /**
     * Visual data for graph vertices.
     * 图顶点的可视化数据。
     *
     * @param x x-coordinate | x坐标
     * @param y y-coordinate | y坐标
     * @param size node size | 节点大小
     * @param color color in hex format (#RRGGBB) | 十六进制颜色格式
     */
    public record VisualData(double x, double y, double size, String color) {

        /**
         * Create visual data with position only.
         * 仅创建带位置的可视化数据。
         *
         * @param x x-coordinate | x坐标
         * @param y y-coordinate | y坐标
         * @return visual data | 可视化数据
         */
        public static VisualData position(double x, double y) {
            return new VisualData(x, y, 0, null);
        }

        /**
         * Create visual data with all properties.
         * 创建带所有属性的可视化数据。
         *
         * @param x x-coordinate | x坐标
         * @param y y-coordinate | y坐标
         * @param size node size | 节点大小
         * @param color color | 颜色
         * @return visual data | 可视化数据
         */
        public static VisualData of(double x, double y, double size, String color) {
            return new VisualData(x, y, size, color);
        }
    }

    // ==================== Helper Methods | 辅助方法 ====================

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

    private static int[] parseColor(String color) {
        if (color == null || color.length() < 7) {
            return new int[]{128, 128, 128};
        }
        try {
            String hex = color.startsWith("#") ? color.substring(1) : color;
            return new int[]{
                    Integer.parseInt(hex.substring(0, 2), 16),
                    Integer.parseInt(hex.substring(2, 4), 16),
                    Integer.parseInt(hex.substring(4, 6), 16)
            };
        } catch (Exception e) {
            return new int[]{128, 128, 128};
        }
    }
}
