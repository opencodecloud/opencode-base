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
 * GexfUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("GexfUtil 测试")
class GexfUtilTest {

    @Nested
    @DisplayName("toGexf测试")
    class ToGexfTests {

        @Test
        @DisplayName("有向图转GEXF")
        void testDirectedGraphToGexf() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            String gexf = GexfUtil.toGexf(graph);

            assertThat(gexf).contains("<?xml version=\"1.0\"");
            assertThat(gexf).contains("<gexf");
            assertThat(gexf).contains("defaultedgetype=\"directed\"");
            assertThat(gexf).contains("<node id=\"A\"");
            assertThat(gexf).contains("<node id=\"B\"");
            assertThat(gexf).contains("<node id=\"C\"");
            assertThat(gexf).contains("source=\"A\" target=\"B\"");
        }

        @Test
        @DisplayName("无向图转GEXF")
        void testUndirectedGraphToGexf() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            String gexf = GexfUtil.toGexf(graph);

            assertThat(gexf).contains("defaultedgetype=\"undirected\"");
        }

        @Test
        @DisplayName("带权重的边")
        void testToGexfWithWeight() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 2.5);

            String gexf = GexfUtil.toGexf(graph);

            assertThat(gexf).contains("weight=\"2.5");
        }

        @Test
        @DisplayName("带顶点属性")
        void testToGexfWithVertexAttributes() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            Map<String, Map<String, String>> vertexAttrs = new HashMap<>();
            vertexAttrs.put("A", Map.of("label", "Node A", "type", "start"));
            vertexAttrs.put("B", Map.of("label", "Node B"));

            String gexf = GexfUtil.toGexf(graph, vertexAttrs, null);

            assertThat(gexf).contains("<attributes class=\"node\">");
            assertThat(gexf).contains("<attvalues>");
            assertThat(gexf).contains("<attvalue");
        }

        @Test
        @DisplayName("带可视化数据")
        void testToGexfWithVisualData() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            Map<String, GexfUtil.VisualData> visualData = new HashMap<>();
            visualData.put("A", new GexfUtil.VisualData(100.0, 200.0, 10.0, "#FF0000"));
            visualData.put("B", GexfUtil.VisualData.position(300.0, 400.0));

            String gexf = GexfUtil.toGexf(graph, null, visualData);

            assertThat(gexf).contains("<viz:position");
            assertThat(gexf).contains("<viz:size");
            assertThat(gexf).contains("<viz:color");
            assertThat(gexf).contains("x=\"100.0");
            assertThat(gexf).contains("y=\"200.0");
            assertThat(gexf).contains("r=\"255\"");
            assertThat(gexf).contains("g=\"0\"");
            assertThat(gexf).contains("b=\"0\"");
        }

        @Test
        @DisplayName("null图返回空字符串")
        void testToGexfNull() {
            String gexf = GexfUtil.toGexf(null);

            assertThat(gexf).isEmpty();
        }

        @Test
        @DisplayName("空图转GEXF")
        void testToGexfEmptyGraph() {
            Graph<String> graph = OpenGraph.directed();

            String gexf = GexfUtil.toGexf(graph);

            assertThat(gexf).contains("<gexf");
            assertThat(gexf).contains("</gexf>");
            assertThat(gexf).contains("<meta");
            assertThat(gexf).contains("<creator>OpenCode Graph Library</creator>");
        }

        @Test
        @DisplayName("孤立顶点转GEXF")
        void testToGexfWithIsolatedVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            String gexf = GexfUtil.toGexf(graph);

            assertThat(gexf).contains("<node id=\"A\" label=\"A\"/>");
        }
    }

    @Nested
    @DisplayName("fromGexf测试")
    class FromGexfTests {

        @Test
        @DisplayName("解析有向图GEXF")
        void testFromGexfDirected() {
            String gexf = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gexf xmlns="http://gexf.net/1.3">
                  <graph mode="static" defaultedgetype="directed">
                    <nodes>
                      <node id="A" label="A"/>
                      <node id="B" label="B"/>
                    </nodes>
                    <edges>
                      <edge id="0" source="A" target="B"/>
                    </edges>
                  </graph>
                </gexf>
                """;

            Graph<String> graph = GexfUtil.fromGexf(gexf);

            assertThat(graph.isDirected()).isTrue();
            assertThat(graph.containsVertex("A")).isTrue();
            assertThat(graph.containsVertex("B")).isTrue();
            assertThat(graph.containsEdge("A", "B")).isTrue();
        }

        @Test
        @DisplayName("解析无向图GEXF")
        void testFromGexfUndirected() {
            String gexf = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gexf xmlns="http://gexf.net/1.3">
                  <graph mode="static" defaultedgetype="undirected">
                    <nodes>
                      <node id="A" label="A"/>
                      <node id="B" label="B"/>
                    </nodes>
                    <edges>
                      <edge id="0" source="A" target="B"/>
                    </edges>
                  </graph>
                </gexf>
                """;

            Graph<String> graph = GexfUtil.fromGexf(gexf);

            assertThat(graph.isDirected()).isFalse();
            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("B", "A")).isTrue();
        }

        @Test
        @DisplayName("解析带权重的边")
        void testFromGexfWithWeight() {
            String gexf = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gexf xmlns="http://gexf.net/1.3">
                  <graph mode="static" defaultedgetype="directed">
                    <nodes>
                      <node id="A"/>
                      <node id="B"/>
                    </nodes>
                    <edges>
                      <edge id="0" source="A" target="B" weight="2.5"/>
                    </edges>
                  </graph>
                </gexf>
                """;

            Graph<String> graph = GexfUtil.fromGexf(gexf);

            assertThat(graph.getWeight("A", "B")).isEqualTo(2.5);
        }

        @Test
        @DisplayName("解析带顶点属性")
        void testFromGexfWithVertexAttributes() {
            String gexf = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gexf xmlns="http://gexf.net/1.3">
                  <graph mode="static" defaultedgetype="directed">
                    <attributes class="node">
                      <attribute id="0" title="label" type="string"/>
                    </attributes>
                    <nodes>
                      <node id="A" label="A">
                        <attvalues>
                          <attvalue for="0" value="Node A"/>
                        </attvalues>
                      </node>
                    </nodes>
                    <edges/>
                  </graph>
                </gexf>
                """;

            Map<String, Map<String, String>> vertexAttrs = new HashMap<>();
            Graph<String> graph = GexfUtil.fromGexf(gexf, vertexAttrs, null);

            assertThat(vertexAttrs).containsKey("A");
            assertThat(vertexAttrs.get("A")).containsEntry("0", "Node A");
        }

        @Test
        @DisplayName("解析带可视化数据")
        void testFromGexfWithVisualData() {
            String gexf = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gexf xmlns="http://gexf.net/1.3" xmlns:viz="http://gexf.net/1.3/viz">
                  <graph mode="static" defaultedgetype="directed">
                    <nodes>
                      <node id="A" label="A">
                        <viz:position x="100" y="200" z="0"/>
                        <viz:size value="10"/>
                        <viz:color r="255" g="0" b="0"/>
                      </node>
                    </nodes>
                    <edges/>
                  </graph>
                </gexf>
                """;

            Map<String, GexfUtil.VisualData> visualData = new HashMap<>();
            Graph<String> graph = GexfUtil.fromGexf(gexf, null, visualData);

            assertThat(visualData).containsKey("A");
            GexfUtil.VisualData visual = visualData.get("A");
            assertThat(visual.x()).isEqualTo(100.0);
            assertThat(visual.y()).isEqualTo(200.0);
            assertThat(visual.size()).isEqualTo(10.0);
            assertThat(visual.color()).isEqualTo("#FF0000");
        }

        @Test
        @DisplayName("null输入返回空有向图")
        void testFromGexfNull() {
            Graph<String> graph = GexfUtil.fromGexf(null);

            assertThat(graph.isDirected()).isTrue();
            assertThat(graph.vertexCount()).isZero();
        }

        @Test
        @DisplayName("空白输入返回空有向图")
        void testFromGexfBlank() {
            Graph<String> graph = GexfUtil.fromGexf("   ");

            assertThat(graph.vertexCount()).isZero();
        }
    }

    @Nested
    @DisplayName("文件读写测试")
    class FileIOTests {

        @Test
        @DisplayName("写入并读取GEXF文件")
        void testWriteAndReadFile(@TempDir Path tempDir) throws IOException {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B", 2.0);
            original.addEdge("B", "C", 3.0);

            Path file = tempDir.resolve("test.gexf");
            GexfUtil.writeToFile(original, file);
            Graph<String> restored = GexfUtil.readFromFile(file);

            assertThat(restored.isDirected()).isTrue();
            assertThat(restored.containsEdge("A", "B")).isTrue();
            assertThat(restored.containsEdge("B", "C")).isTrue();
            assertThat(restored.getWeight("A", "B")).isEqualTo(2.0);
        }

        @Test
        @DisplayName("写入带属性和可视化数据的GEXF文件")
        void testWriteFileWithAttributesAndVisualData(@TempDir Path tempDir) throws IOException {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addEdge("A", "B");

            Map<String, Map<String, String>> vertexAttrs = new HashMap<>();
            vertexAttrs.put("A", Map.of("label", "Node A"));

            Map<String, GexfUtil.VisualData> visualData = new HashMap<>();
            visualData.put("A", new GexfUtil.VisualData(100.0, 200.0, 10.0, "#FF0000"));

            Path file = tempDir.resolve("test-full.gexf");
            GexfUtil.writeToFile(graph, file, vertexAttrs, visualData);

            Map<String, Map<String, String>> restoredAttrs = new HashMap<>();
            Map<String, GexfUtil.VisualData> restoredVisual = new HashMap<>();
            Graph<String> restored = GexfUtil.readFromFile(file, restoredAttrs, restoredVisual);

            assertThat(restored.containsVertex("A")).isTrue();
            assertThat(restored.containsVertex("B")).isTrue();
            assertThat(restored.containsEdge("A", "B")).isTrue();
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

            String gexf = GexfUtil.toGexf(original);
            Graph<String> restored = GexfUtil.fromGexf(gexf);

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

            String gexf = GexfUtil.toGexf(original);
            Graph<String> restored = GexfUtil.fromGexf(gexf);

            assertThat(restored.isDirected()).isFalse();
            assertThat(restored.containsEdge("X", "Y")).isTrue();
            assertThat(restored.containsEdge("Y", "X")).isTrue();
            assertThat(restored.getWeight("X", "Y")).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("VisualData测试")
    class VisualDataTests {

        @Test
        @DisplayName("创建完整VisualData")
        void testCreateFullVisualData() {
            GexfUtil.VisualData visual = new GexfUtil.VisualData(100.0, 200.0, 10.0, "#FF0000");

            assertThat(visual.x()).isEqualTo(100.0);
            assertThat(visual.y()).isEqualTo(200.0);
            assertThat(visual.size()).isEqualTo(10.0);
            assertThat(visual.color()).isEqualTo("#FF0000");
        }

        @Test
        @DisplayName("position工厂方法")
        void testPositionFactoryMethod() {
            GexfUtil.VisualData visual = GexfUtil.VisualData.position(50.0, 75.0);

            assertThat(visual.x()).isEqualTo(50.0);
            assertThat(visual.y()).isEqualTo(75.0);
            assertThat(visual.size()).isEqualTo(0);
            assertThat(visual.color()).isNull();
        }

        @Test
        @DisplayName("of工厂方法")
        void testOfFactoryMethod() {
            GexfUtil.VisualData visual = GexfUtil.VisualData.of(10.0, 20.0, 5.0, "#00FF00");

            assertThat(visual.x()).isEqualTo(10.0);
            assertThat(visual.y()).isEqualTo(20.0);
            assertThat(visual.size()).isEqualTo(5.0);
            assertThat(visual.color()).isEqualTo("#00FF00");
        }

        @Test
        @DisplayName("VisualData记录相等性")
        void testVisualDataEquality() {
            GexfUtil.VisualData visual1 = new GexfUtil.VisualData(100.0, 200.0, 10.0, "#FF0000");
            GexfUtil.VisualData visual2 = new GexfUtil.VisualData(100.0, 200.0, 10.0, "#FF0000");
            GexfUtil.VisualData visual3 = new GexfUtil.VisualData(100.0, 200.0, 10.0, "#00FF00");

            assertThat(visual1).isEqualTo(visual2);
            assertThat(visual1).isNotEqualTo(visual3);
        }

        @Test
        @DisplayName("VisualData哈希码")
        void testVisualDataHashCode() {
            GexfUtil.VisualData visual1 = new GexfUtil.VisualData(100.0, 200.0, 10.0, "#FF0000");
            GexfUtil.VisualData visual2 = new GexfUtil.VisualData(100.0, 200.0, 10.0, "#FF0000");

            assertThat(visual1.hashCode()).isEqualTo(visual2.hashCode());
        }

        @Test
        @DisplayName("VisualData toString")
        void testVisualDataToString() {
            GexfUtil.VisualData visual = new GexfUtil.VisualData(100.0, 200.0, 10.0, "#FF0000");

            String str = visual.toString();

            assertThat(str).contains("100.0");
            assertThat(str).contains("200.0");
            assertThat(str).contains("10.0");
            assertThat(str).contains("#FF0000");
        }
    }

    @Nested
    @DisplayName("颜色解析测试")
    class ColorParsingTests {

        @Test
        @DisplayName("RGB颜色转换")
        void testRgbColorConversion() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            Map<String, GexfUtil.VisualData> visualData = new HashMap<>();
            visualData.put("A", new GexfUtil.VisualData(0, 0, 10.0, "#FF8040"));

            String gexf = GexfUtil.toGexf(graph, null, visualData);

            assertThat(gexf).contains("r=\"255\"");
            assertThat(gexf).contains("g=\"128\"");
            assertThat(gexf).contains("b=\"64\"");
        }

        @Test
        @DisplayName("从GEXF解析RGB颜色")
        void testParseRgbFromGexf() {
            String gexf = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gexf xmlns="http://gexf.net/1.3" xmlns:viz="http://gexf.net/1.3/viz">
                  <graph mode="static" defaultedgetype="directed">
                    <nodes>
                      <node id="A" label="A">
                        <viz:color r="128" g="64" b="32"/>
                      </node>
                    </nodes>
                    <edges/>
                  </graph>
                </gexf>
                """;

            Map<String, GexfUtil.VisualData> visualData = new HashMap<>();
            GexfUtil.fromGexf(gexf, null, visualData);

            assertThat(visualData.get("A").color()).isEqualTo("#804020");
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

            String gexf = GexfUtil.toGexf(graph);

            assertThat(gexf).contains("&amp;");
            assertThat(gexf).contains("&lt;");

            Graph<String> restored = GexfUtil.fromGexf(gexf);

            assertThat(restored.containsVertex("A&B")).isTrue();
            assertThat(restored.containsVertex("C<D")).isTrue();
        }
    }

    @Nested
    @DisplayName("元数据测试")
    class MetadataTests {

        @Test
        @DisplayName("GEXF包含元数据")
        void testGexfContainsMetadata() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            String gexf = GexfUtil.toGexf(graph);

            assertThat(gexf).contains("<meta");
            assertThat(gexf).contains("lastmodifieddate=");
            assertThat(gexf).contains("<creator>OpenCode Graph Library</creator>");
            assertThat(gexf).contains("<description>");
        }
    }

    @Nested
    @DisplayName("默认图类型测试")
    class DefaultGraphTypeTests {

        @Test
        @DisplayName("无defaultedgetype属性默认有向图")
        void testDefaultDirected() {
            String gexf = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gexf xmlns="http://gexf.net/1.3">
                  <graph mode="static">
                    <nodes>
                      <node id="A"/>
                      <node id="B"/>
                    </nodes>
                    <edges>
                      <edge source="A" target="B"/>
                    </edges>
                  </graph>
                </gexf>
                """;

            Graph<String> graph = GexfUtil.fromGexf(gexf);

            assertThat(graph.isDirected()).isTrue();
        }
    }
}
