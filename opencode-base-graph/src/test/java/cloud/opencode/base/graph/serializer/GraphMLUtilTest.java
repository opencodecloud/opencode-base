package cloud.opencode.base.graph.serializer;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphMLUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("GraphMLUtil 测试")
class GraphMLUtilTest {

    @Nested
    @DisplayName("toGraphML测试")
    class ToGraphMLTests {

        @Test
        @DisplayName("有向图转GraphML")
        void testDirectedGraphToGraphML() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            String graphml = GraphMLUtil.toGraphML(graph);

            assertThat(graphml).contains("<?xml version=\"1.0\"");
            assertThat(graphml).contains("<graphml");
            assertThat(graphml).contains("edgedefault=\"directed\"");
            assertThat(graphml).contains("<node id=\"A\"");
            assertThat(graphml).contains("<node id=\"B\"");
            assertThat(graphml).contains("<node id=\"C\"");
            assertThat(graphml).contains("source=\"A\" target=\"B\"");
        }

        @Test
        @DisplayName("无向图转GraphML")
        void testUndirectedGraphToGraphML() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            String graphml = GraphMLUtil.toGraphML(graph);

            assertThat(graphml).contains("edgedefault=\"undirected\"");
        }

        @Test
        @DisplayName("带权重的边")
        void testToGraphMLWithWeight() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 2.5);

            String graphml = GraphMLUtil.toGraphML(graph);

            assertThat(graphml).contains("<data key=\"weight\">2.5</data>");
        }

        @Test
        @DisplayName("带顶点属性")
        void testToGraphMLWithVertexAttributes() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            Map<String, Map<String, String>> vertexAttrs = new HashMap<>();
            vertexAttrs.put("A", Map.of("label", "Node A", "color", "red"));
            vertexAttrs.put("B", Map.of("label", "Node B"));

            String graphml = GraphMLUtil.toGraphML(graph, vertexAttrs, null);

            assertThat(graphml).contains("<key id=\"label\"");
            assertThat(graphml).contains("<key id=\"color\"");
            assertThat(graphml).contains("<data key=\"label\">Node A</data>");
            assertThat(graphml).contains("<data key=\"color\">red</data>");
        }

        @Test
        @DisplayName("带边属性")
        void testToGraphMLWithEdgeAttributes() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            Map<String, Map<String, String>> edgeAttrs = new HashMap<>();
            edgeAttrs.put("A:B", Map.of("label", "Edge AB", "type", "dependency"));

            String graphml = GraphMLUtil.toGraphML(graph, null, edgeAttrs);

            assertThat(graphml).contains("<data key=\"label\">Edge AB</data>");
            assertThat(graphml).contains("<data key=\"type\">dependency</data>");
        }

        @Test
        @DisplayName("null图返回空字符串")
        void testToGraphMLNull() {
            String graphml = GraphMLUtil.toGraphML(null);

            assertThat(graphml).isEmpty();
        }

        @Test
        @DisplayName("空图转GraphML")
        void testToGraphMLEmptyGraph() {
            Graph<String> graph = OpenGraph.directed();

            String graphml = GraphMLUtil.toGraphML(graph);

            assertThat(graphml).contains("<graphml");
            assertThat(graphml).contains("</graphml>");
        }

        @Test
        @DisplayName("孤立顶点转GraphML")
        void testToGraphMLWithIsolatedVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            String graphml = GraphMLUtil.toGraphML(graph);

            assertThat(graphml).contains("<node id=\"A\"/>");
        }
    }

    @Nested
    @DisplayName("fromGraphML测试")
    class FromGraphMLTests {

        @Test
        @DisplayName("解析有向图GraphML")
        void testFromGraphMLDirected() {
            String graphml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <graph id="G" edgedefault="directed">
                    <node id="A"/>
                    <node id="B"/>
                    <edge source="A" target="B"/>
                  </graph>
                </graphml>
                """;

            Graph<String> graph = GraphMLUtil.fromGraphML(graphml);

            assertThat(graph.isDirected()).isTrue();
            assertThat(graph.containsVertex("A")).isTrue();
            assertThat(graph.containsVertex("B")).isTrue();
            assertThat(graph.containsEdge("A", "B")).isTrue();
        }

        @Test
        @DisplayName("解析无向图GraphML")
        void testFromGraphMLUndirected() {
            String graphml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <graph id="G" edgedefault="undirected">
                    <node id="A"/>
                    <node id="B"/>
                    <edge source="A" target="B"/>
                  </graph>
                </graphml>
                """;

            Graph<String> graph = GraphMLUtil.fromGraphML(graphml);

            assertThat(graph.isDirected()).isFalse();
            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("B", "A")).isTrue();
        }

        @Test
        @DisplayName("解析带权重的边")
        void testFromGraphMLWithWeight() {
            String graphml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <key id="weight" for="edge" attr.name="weight" attr.type="double"/>
                  <graph id="G" edgedefault="directed">
                    <node id="A"/>
                    <node id="B"/>
                    <edge source="A" target="B">
                      <data key="weight">2.5</data>
                    </edge>
                  </graph>
                </graphml>
                """;

            Graph<String> graph = GraphMLUtil.fromGraphML(graphml);

            assertThat(graph.getWeight("A", "B")).isEqualTo(2.5);
        }

        @Test
        @DisplayName("解析带顶点属性")
        void testFromGraphMLWithVertexAttributes() {
            String graphml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <key id="label" for="node" attr.name="label" attr.type="string"/>
                  <graph id="G" edgedefault="directed">
                    <node id="A">
                      <data key="label">Node A</data>
                    </node>
                  </graph>
                </graphml>
                """;

            Map<String, Map<String, String>> vertexAttrs = new HashMap<>();
            Graph<String> graph = GraphMLUtil.fromGraphML(graphml, vertexAttrs, null);

            assertThat(vertexAttrs).containsKey("A");
            assertThat(vertexAttrs.get("A")).containsEntry("label", "Node A");
        }

        @Test
        @DisplayName("解析带边属性")
        void testFromGraphMLWithEdgeAttributes() {
            String graphml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <key id="type" for="edge" attr.name="type" attr.type="string"/>
                  <graph id="G" edgedefault="directed">
                    <node id="A"/>
                    <node id="B"/>
                    <edge source="A" target="B">
                      <data key="type">dependency</data>
                    </edge>
                  </graph>
                </graphml>
                """;

            Map<String, Map<String, String>> edgeAttrs = new HashMap<>();
            Graph<String> graph = GraphMLUtil.fromGraphML(graphml, null, edgeAttrs);

            assertThat(edgeAttrs).containsKey("A:B");
            assertThat(edgeAttrs.get("A:B")).containsEntry("type", "dependency");
        }

        @Test
        @DisplayName("null输入返回空有向图")
        void testFromGraphMLNull() {
            Graph<String> graph = GraphMLUtil.fromGraphML(null);

            assertThat(graph.isDirected()).isTrue();
            assertThat(graph.vertexCount()).isZero();
        }

        @Test
        @DisplayName("空白输入返回空有向图")
        void testFromGraphMLBlank() {
            Graph<String> graph = GraphMLUtil.fromGraphML("   ");

            assertThat(graph.vertexCount()).isZero();
        }
    }

    @Nested
    @DisplayName("文件读写测试")
    class FileIOTests {

        @Test
        @DisplayName("写入并读取GraphML文件")
        void testWriteAndReadFile(@TempDir Path tempDir) throws IOException {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B", 2.0);
            original.addEdge("B", "C", 3.0);

            Path file = tempDir.resolve("test.graphml");
            GraphMLUtil.writeToFile(original, file);
            Graph<String> restored = GraphMLUtil.readFromFile(file);

            assertThat(restored.isDirected()).isTrue();
            assertThat(restored.containsEdge("A", "B")).isTrue();
            assertThat(restored.containsEdge("B", "C")).isTrue();
            assertThat(restored.getWeight("A", "B")).isEqualTo(2.0);
        }

        @Test
        @DisplayName("写入带属性的GraphML文件")
        void testWriteFileWithAttributes(@TempDir Path tempDir) throws IOException {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            Map<String, Map<String, String>> vertexAttrs = new HashMap<>();
            vertexAttrs.put("A", Map.of("label", "Node A"));

            Path file = tempDir.resolve("test-attrs.graphml");
            GraphMLUtil.writeToFile(graph, file, vertexAttrs, null);

            Map<String, Map<String, String>> restoredAttrs = new HashMap<>();
            Graph<String> restored = GraphMLUtil.readFromFile(file, restoredAttrs, null);

            assertThat(restored.containsVertex("A")).isTrue();
            assertThat(restoredAttrs).containsKey("A");
            assertThat(restoredAttrs.get("A")).containsEntry("label", "Node A");
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("有向图往返")
        void testDirectedGraphRoundTrip() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B", 2.0);
            original.addEdge("B", "C", 3.0);
            original.addEdge("A", "C", 1.5);
            original.addVertex("D"); // Isolated vertex

            String graphml = GraphMLUtil.toGraphML(original);
            Graph<String> restored = GraphMLUtil.fromGraphML(graphml);

            assertThat(restored.isDirected()).isTrue();
            assertThat(restored.vertexCount()).isEqualTo(4);
            assertThat(restored.containsEdge("A", "B")).isTrue();
            assertThat(restored.containsEdge("B", "C")).isTrue();
            assertThat(restored.containsEdge("A", "C")).isTrue();
            assertThat(restored.getWeight("A", "B")).isEqualTo(2.0);
            assertThat(restored.containsVertex("D")).isTrue();
        }

        @Test
        @DisplayName("无向图往返")
        void testUndirectedGraphRoundTrip() {
            Graph<String> original = OpenGraph.undirected();
            original.addEdge("X", "Y", 5.0);
            original.addEdge("Y", "Z", 2.5);

            String graphml = GraphMLUtil.toGraphML(original);
            Graph<String> restored = GraphMLUtil.fromGraphML(graphml);

            assertThat(restored.isDirected()).isFalse();
            assertThat(restored.containsEdge("X", "Y")).isTrue();
            assertThat(restored.containsEdge("Y", "X")).isTrue();
            assertThat(restored.getWeight("X", "Y")).isEqualTo(5.0);
        }

        @Test
        @DisplayName("带属性往返")
        void testAttributesRoundTrip() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B");

            Map<String, Map<String, String>> originalVertexAttrs = new HashMap<>();
            originalVertexAttrs.put("A", Map.of("label", "Node A", "color", "red"));
            originalVertexAttrs.put("B", Map.of("label", "Node B"));

            Map<String, Map<String, String>> originalEdgeAttrs = new HashMap<>();
            originalEdgeAttrs.put("A:B", Map.of("type", "dependency"));

            String graphml = GraphMLUtil.toGraphML(original, originalVertexAttrs, originalEdgeAttrs);

            Map<String, Map<String, String>> restoredVertexAttrs = new HashMap<>();
            Map<String, Map<String, String>> restoredEdgeAttrs = new HashMap<>();
            Graph<String> restored = GraphMLUtil.fromGraphML(graphml, restoredVertexAttrs, restoredEdgeAttrs);

            assertThat(restored.containsEdge("A", "B")).isTrue();
            assertThat(restoredVertexAttrs.get("A")).containsEntry("label", "Node A");
            assertThat(restoredVertexAttrs.get("A")).containsEntry("color", "red");
            assertThat(restoredEdgeAttrs.get("A:B")).containsEntry("type", "dependency");
        }
    }

    @Nested
    @DisplayName("XML转义测试")
    class XmlEscapeTests {

        @Test
        @DisplayName("顶点ID包含特殊字符")
        void testSpecialCharactersInVertexId() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A&B", "C<D");

            String graphml = GraphMLUtil.toGraphML(graph);

            assertThat(graphml).contains("&amp;");
            assertThat(graphml).contains("&lt;");

            Graph<String> restored = GraphMLUtil.fromGraphML(graphml);

            assertThat(restored.containsVertex("A&B")).isTrue();
            assertThat(restored.containsVertex("C<D")).isTrue();
        }

        @Test
        @DisplayName("属性值包含特殊字符")
        void testSpecialCharactersInAttributes() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            Map<String, Map<String, String>> vertexAttrs = new HashMap<>();
            vertexAttrs.put("A", Map.of("desc", "Value with <special> & \"characters\""));

            String graphml = GraphMLUtil.toGraphML(graph, vertexAttrs, null);

            assertThat(graphml).contains("&lt;special&gt;");
            assertThat(graphml).contains("&amp;");
            assertThat(graphml).contains("&quot;");
        }
    }

    @Nested
    @DisplayName("默认图类型测试")
    class DefaultGraphTypeTests {

        @Test
        @DisplayName("无edgedefault属性默认有向图")
        void testDefaultDirected() {
            String graphml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <graph id="G">
                    <node id="A"/>
                    <node id="B"/>
                    <edge source="A" target="B"/>
                  </graph>
                </graphml>
                """;

            Graph<String> graph = GraphMLUtil.fromGraphML(graphml);

            assertThat(graph.isDirected()).isTrue();
        }
    }
}
