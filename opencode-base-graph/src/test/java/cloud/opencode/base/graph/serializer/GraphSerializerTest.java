package cloud.opencode.base.graph.serializer;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphSerializer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("GraphSerializer 测试")
class GraphSerializerTest {

    @Nested
    @DisplayName("toDot测试")
    class ToDotTests {

        @Test
        @DisplayName("有向图转DOT格式")
        void testDirectedGraphToDot() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            String dot = GraphSerializer.toDot(graph);

            assertThat(dot).contains("digraph");
            assertThat(dot).contains("->");
            assertThat(dot).contains("\"A\"");
            assertThat(dot).contains("\"B\"");
            assertThat(dot).contains("\"C\"");
        }

        @Test
        @DisplayName("无向图转DOT格式")
        void testUndirectedGraphToDot() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            String dot = GraphSerializer.toDot(graph);

            assertThat(dot).contains("graph");
            assertThat(dot).contains("--");
        }

        @Test
        @DisplayName("自定义图名称")
        void testToDotWithCustomName() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            String dot = GraphSerializer.toDot(graph, "MyGraph");

            assertThat(dot).contains("digraph MyGraph");
        }

        @Test
        @DisplayName("带权重的边")
        void testToDotWithWeight() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 2.5);

            String dot = GraphSerializer.toDot(graph);

            assertThat(dot).contains("label=\"2.5\"");
        }

        @Test
        @DisplayName("孤立顶点")
        void testToDotWithIsolatedVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addEdge("C", "D");

            String dot = GraphSerializer.toDot(graph);

            assertThat(dot).contains("\"A\"");
            assertThat(dot).contains("\"B\"");
        }

        @Test
        @DisplayName("null图返回空字符串")
        void testToDotNull() {
            String dot = GraphSerializer.toDot(null);

            assertThat(dot).isEmpty();
        }

        @Test
        @DisplayName("空图")
        void testToDotEmptyGraph() {
            Graph<String> graph = OpenGraph.directed();

            String dot = GraphSerializer.toDot(graph);

            assertThat(dot).contains("digraph");
            assertThat(dot).contains("{");
            assertThat(dot).contains("}");
        }
    }

    @Nested
    @DisplayName("toAdjacencyList测试")
    class ToAdjacencyListTests {

        @Test
        @DisplayName("有向图转邻接表")
        void testDirectedGraphToAdjacencyList() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");

            String adjList = GraphSerializer.toAdjacencyList(graph);

            assertThat(adjList).contains("# directed=true");
            assertThat(adjList).contains("A:");
            assertThat(adjList).contains("B");
            assertThat(adjList).contains("C");
        }

        @Test
        @DisplayName("无向图转邻接表")
        void testUndirectedGraphToAdjacencyList() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            String adjList = GraphSerializer.toAdjacencyList(graph);

            assertThat(adjList).contains("# directed=false");
        }

        @Test
        @DisplayName("带权重的邻接表")
        void testToAdjacencyListWithWeight() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 2.5);

            String adjList = GraphSerializer.toAdjacencyList(graph);

            assertThat(adjList).contains("B(2.5)");
        }

        @Test
        @DisplayName("孤立顶点")
        void testToAdjacencyListWithIsolatedVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            String adjList = GraphSerializer.toAdjacencyList(graph);

            assertThat(adjList).contains("A:");
        }

        @Test
        @DisplayName("null图返回空字符串")
        void testToAdjacencyListNull() {
            String adjList = GraphSerializer.toAdjacencyList(null);

            assertThat(adjList).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromAdjacencyList测试")
    class FromAdjacencyListTests {

        @Test
        @DisplayName("解析有向图邻接表")
        void testFromAdjacencyListDirected() {
            String input = """
                # directed=true
                A:B,C
                B:C
                C:
                """;

            Graph<String> graph = GraphSerializer.fromAdjacencyList(input, true);

            assertThat(graph.isDirected()).isTrue();
            assertThat(graph.containsVertex("A")).isTrue();
            assertThat(graph.containsVertex("B")).isTrue();
            assertThat(graph.containsVertex("C")).isTrue();
            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("A", "C")).isTrue();
            assertThat(graph.containsEdge("B", "C")).isTrue();
        }

        @Test
        @DisplayName("解析无向图邻接表")
        void testFromAdjacencyListUndirected() {
            String input = "A:B\nB:C";

            Graph<String> graph = GraphSerializer.fromAdjacencyList(input, false);

            assertThat(graph.isDirected()).isFalse();
            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("B", "A")).isTrue();
        }

        @Test
        @DisplayName("解析带权重的邻接表")
        void testFromAdjacencyListWithWeight() {
            String input = "A:B(2.5),C(3.0)";

            Graph<String> graph = GraphSerializer.fromAdjacencyList(input, true);

            assertThat(graph.getWeight("A", "B")).isEqualTo(2.5);
            assertThat(graph.getWeight("A", "C")).isEqualTo(3.0);
        }

        @Test
        @DisplayName("跳过注释和空行")
        void testFromAdjacencyListSkipsCommentsAndEmptyLines() {
            String input = """
                # This is a comment

                A:B
                # Another comment
                B:C
                """;

            Graph<String> graph = GraphSerializer.fromAdjacencyList(input, true);

            assertThat(graph.vertexCount()).isEqualTo(3);
            assertThat(graph.edgeCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("null输入返回空图")
        void testFromAdjacencyListNull() {
            Graph<String> graph = GraphSerializer.fromAdjacencyList(null, true);

            assertThat(graph.vertexCount()).isZero();
        }

        @Test
        @DisplayName("空白输入返回空图")
        void testFromAdjacencyListBlank() {
            Graph<String> graph = GraphSerializer.fromAdjacencyList("   ", true);

            assertThat(graph.vertexCount()).isZero();
        }

        @Test
        @DisplayName("无冒号的行被跳过")
        void testFromAdjacencyListNoColon() {
            String input = "A:B\nInvalidLine\nC:D";

            Graph<String> graph = GraphSerializer.fromAdjacencyList(input, true);

            assertThat(graph.containsVertex("A")).isTrue();
            assertThat(graph.containsVertex("C")).isTrue();
            assertThat(graph.containsVertex("InvalidLine")).isFalse();
        }
    }

    @Nested
    @DisplayName("toEdgeList测试")
    class ToEdgeListTests {

        @Test
        @DisplayName("有向图转边列表")
        void testDirectedGraphToEdgeList() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            String edgeList = GraphSerializer.toEdgeList(graph);

            assertThat(edgeList).contains("# directed=true");
            assertThat(edgeList).contains("A B");
            assertThat(edgeList).contains("B C");
        }

        @Test
        @DisplayName("无向图转边列表无重复")
        void testUndirectedGraphToEdgeListNoDuplicates() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            String edgeList = GraphSerializer.toEdgeList(graph);

            assertThat(edgeList).contains("# directed=false");
            // Should only appear once
            long count = edgeList.lines()
                .filter(line -> !line.startsWith("#") && !line.isEmpty())
                .count();
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("带权重的边列表")
        void testToEdgeListWithWeight() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 2.5);

            String edgeList = GraphSerializer.toEdgeList(graph);

            assertThat(edgeList).contains("A B 2.5");
        }

        @Test
        @DisplayName("null图返回空字符串")
        void testToEdgeListNull() {
            String edgeList = GraphSerializer.toEdgeList(null);

            assertThat(edgeList).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromEdgeList测试")
    class FromEdgeListTests {

        @Test
        @DisplayName("解析有向图边列表")
        void testFromEdgeListDirected() {
            String input = """
                # directed=true
                # format: from to [weight]
                A B
                B C
                """;

            Graph<String> graph = GraphSerializer.fromEdgeList(input, true);

            assertThat(graph.isDirected()).isTrue();
            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("B", "C")).isTrue();
        }

        @Test
        @DisplayName("解析无向图边列表")
        void testFromEdgeListUndirected() {
            String input = "A B\nB C";

            Graph<String> graph = GraphSerializer.fromEdgeList(input, false);

            assertThat(graph.isDirected()).isFalse();
            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("B", "A")).isTrue();
        }

        @Test
        @DisplayName("解析带权重的边列表")
        void testFromEdgeListWithWeight() {
            String input = "A B 2.5\nB C 3.0";

            Graph<String> graph = GraphSerializer.fromEdgeList(input, true);

            assertThat(graph.getWeight("A", "B")).isEqualTo(2.5);
            assertThat(graph.getWeight("B", "C")).isEqualTo(3.0);
        }

        @Test
        @DisplayName("跳过注释和空行")
        void testFromEdgeListSkipsCommentsAndEmptyLines() {
            String input = """
                # Comment

                A B

                # Another comment
                B C
                """;

            Graph<String> graph = GraphSerializer.fromEdgeList(input, true);

            assertThat(graph.edgeCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("null输入返回空图")
        void testFromEdgeListNull() {
            Graph<String> graph = GraphSerializer.fromEdgeList(null, true);

            assertThat(graph.vertexCount()).isZero();
        }

        @Test
        @DisplayName("空白输入返回空图")
        void testFromEdgeListBlank() {
            Graph<String> graph = GraphSerializer.fromEdgeList("   ", true);

            assertThat(graph.vertexCount()).isZero();
        }

        @Test
        @DisplayName("单元素行被跳过")
        void testFromEdgeListIncompleteLine() {
            String input = "A B\nC\nD E";

            Graph<String> graph = GraphSerializer.fromEdgeList(input, true);

            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.containsEdge("D", "E")).isTrue();
            assertThat(graph.containsVertex("C")).isFalse();
        }
    }

    @Nested
    @DisplayName("getStatistics测试")
    class GetStatisticsTests {

        @Test
        @DisplayName("获取有向图统计信息")
        void testGetStatisticsDirected() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

            String stats = GraphSerializer.getStatistics(graph);

            assertThat(stats).contains("Graph Statistics:");
            assertThat(stats).contains("Type: Directed");
            assertThat(stats).contains("Vertices: 3");
            assertThat(stats).contains("Edges: 3");
            assertThat(stats).contains("Density:");
            assertThat(stats).contains("Min degree:");
            assertThat(stats).contains("Max degree:");
            assertThat(stats).contains("Avg degree:");
        }

        @Test
        @DisplayName("获取无向图统计信息")
        void testGetStatisticsUndirected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            String stats = GraphSerializer.getStatistics(graph);

            assertThat(stats).contains("Type: Undirected");
        }

        @Test
        @DisplayName("null图统计信息")
        void testGetStatisticsNull() {
            String stats = GraphSerializer.getStatistics(null);

            assertThat(stats).isEqualTo("Graph: null");
        }

        @Test
        @DisplayName("空图统计信息")
        void testGetStatisticsEmptyGraph() {
            Graph<String> graph = OpenGraph.directed();

            String stats = GraphSerializer.getStatistics(graph);

            assertThat(stats).contains("Vertices: 0");
            assertThat(stats).contains("Edges: 0");
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("邻接表往返测试")
        void testAdjacencyListRoundTrip() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("A", "B", 2.0);
            original.addEdge("B", "C", 3.0);
            original.addEdge("A", "C", 1.0);

            String adjList = GraphSerializer.toAdjacencyList(original);
            Graph<String> restored = GraphSerializer.fromAdjacencyList(adjList, true);

            assertThat(restored.vertexCount()).isEqualTo(original.vertexCount());
            assertThat(restored.containsEdge("A", "B")).isTrue();
            assertThat(restored.containsEdge("B", "C")).isTrue();
            assertThat(restored.containsEdge("A", "C")).isTrue();
            assertThat(restored.getWeight("A", "B")).isEqualTo(2.0);
        }

        @Test
        @DisplayName("边列表往返测试")
        void testEdgeListRoundTrip() {
            Graph<String> original = OpenGraph.directed();
            original.addEdge("X", "Y", 5.0);
            original.addEdge("Y", "Z", 2.5);

            String edgeList = GraphSerializer.toEdgeList(original);
            Graph<String> restored = GraphSerializer.fromEdgeList(edgeList, true);

            assertThat(restored.containsEdge("X", "Y")).isTrue();
            assertThat(restored.containsEdge("Y", "Z")).isTrue();
            assertThat(restored.getWeight("X", "Y")).isEqualTo(5.0);
            assertThat(restored.getWeight("Y", "Z")).isEqualTo(2.5);
        }
    }

    @Nested
    @DisplayName("特殊字符转义测试")
    class EscapeTests {

        @Test
        @DisplayName("DOT格式特殊字符转义")
        void testDotEscapeSpecialCharacters() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A\"B", "C\\D");

            String dot = GraphSerializer.toDot(graph);

            assertThat(dot).contains("\\\"");
            assertThat(dot).contains("\\\\");
        }

        @Test
        @DisplayName("包含换行符的顶点名")
        void testDotEscapeNewline() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A\nB", "C");

            String dot = GraphSerializer.toDot(graph);

            assertThat(dot).contains("\\n");
        }
    }
}
