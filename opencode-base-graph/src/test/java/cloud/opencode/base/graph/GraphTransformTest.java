package cloud.opencode.base.graph;

import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphTransform 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("GraphTransform 测试")
class GraphTransformTest {

    @Nested
    @DisplayName("mapVertices测试")
    class MapVerticesTests {

        @Test
        @DisplayName("Integer映射为String")
        void testMapIntegerToString() {
            Graph<Integer> intGraph = OpenGraph.directed();
            intGraph.addEdge(1, 2, 3.0);
            intGraph.addEdge(2, 3, 4.0);

            Graph<String> strGraph = GraphTransform.mapVertices(intGraph, String::valueOf);

            assertThat(strGraph.vertexCount()).isEqualTo(3);
            assertThat(strGraph.containsVertex("1")).isTrue();
            assertThat(strGraph.containsVertex("2")).isTrue();
            assertThat(strGraph.containsVertex("3")).isTrue();
            assertThat(strGraph.containsEdge("1", "2")).isTrue();
            assertThat(strGraph.containsEdge("2", "3")).isTrue();
            assertThat(strGraph.isDirected()).isTrue();
        }

        @Test
        @DisplayName("映射保留权重")
        void testMapPreservesWeights() {
            Graph<Integer> intGraph = OpenGraph.directed();
            intGraph.addEdge(1, 2, 5.5);

            Graph<String> strGraph = GraphTransform.mapVertices(intGraph, String::valueOf);

            assertThat(strGraph.getWeight("1", "2")).isEqualTo(5.5);
        }

        @Test
        @DisplayName("映射无向图保持无向")
        void testMapUndirectedRemainsUndirected() {
            Graph<Integer> intGraph = OpenGraph.undirected();
            intGraph.addEdge(1, 2);

            Graph<String> strGraph = GraphTransform.mapVertices(intGraph, String::valueOf);

            assertThat(strGraph.isDirected()).isFalse();
            assertThat(strGraph.containsEdge("1", "2")).isTrue();
            assertThat(strGraph.containsEdge("2", "1")).isTrue();
        }
    }

    @Nested
    @DisplayName("filterVertices测试")
    class FilterVerticesTests {

        @Test
        @DisplayName("过滤保留匹配顶点及其边")
        void testFilterKeepsMatchingVerticesAndEdges() {
            Graph<Integer> graph = OpenGraph.directed();
            graph.addEdge(1, 2, 1.0);
            graph.addEdge(2, 3, 2.0);
            graph.addEdge(3, 4, 3.0);

            Graph<Integer> filtered = GraphTransform.filterVertices(graph, v -> v <= 3);

            assertThat(filtered.vertexCount()).isEqualTo(3);
            assertThat(filtered.containsVertex(4)).isFalse();
            assertThat(filtered.containsEdge(1, 2)).isTrue();
            assertThat(filtered.containsEdge(2, 3)).isTrue();
            assertThat(filtered.containsEdge(3, 4)).isFalse();
        }

        @Test
        @DisplayName("过滤移除所有顶点")
        void testFilterRemovesAll() {
            Graph<Integer> graph = OpenGraph.directed();
            graph.addEdge(1, 2);

            Graph<Integer> filtered = GraphTransform.filterVertices(graph, v -> v > 10);

            assertThat(filtered.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("filterEdges测试")
    class FilterEdgesTests {

        @Test
        @DisplayName("过滤边保留所有顶点")
        void testFilterEdgesKeepsAllVertices() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 5.0);

            Graph<String> filtered = GraphTransform.filterEdges(graph, e -> e.weight() < 3.0);

            assertThat(filtered.vertexCount()).isEqualTo(3);
            assertThat(filtered.containsEdge("A", "B")).isTrue();
            assertThat(filtered.containsEdge("B", "C")).isFalse();
        }

        @Test
        @DisplayName("按权重过滤边保留权重")
        void testFilterEdgesPreservesWeight() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 2.5);
            graph.addEdge("B", "C", 7.0);

            Graph<String> filtered = GraphTransform.filterEdges(graph, e -> e.weight() < 5.0);

            assertThat(filtered.getWeight("A", "B")).isEqualTo(2.5);
        }
    }

    @Nested
    @DisplayName("reverse测试")
    class ReverseTests {

        @Test
        @DisplayName("反转有向图")
        void testReverseDirectedGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 2.0);
            graph.addEdge("B", "C", 3.0);

            Graph<String> reversed = GraphTransform.reverse(graph);

            assertThat(reversed.isDirected()).isTrue();
            assertThat(reversed.vertexCount()).isEqualTo(3);
            assertThat(reversed.containsEdge("B", "A")).isTrue();
            assertThat(reversed.containsEdge("C", "B")).isTrue();
            assertThat(reversed.containsEdge("A", "B")).isFalse();
            assertThat(reversed.containsEdge("B", "C")).isFalse();
        }

        @Test
        @DisplayName("反转有向图保留权重")
        void testReversePreservesWeights() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 4.5);

            Graph<String> reversed = GraphTransform.reverse(graph);

            assertThat(reversed.getWeight("B", "A")).isEqualTo(4.5);
        }

        @Test
        @DisplayName("反转无向图返回副本")
        void testReverseUndirectedReturnsCopy() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 2.0);
            graph.addEdge("B", "C", 3.0);

            Graph<String> reversed = GraphTransform.reverse(graph);

            assertThat(reversed.isDirected()).isFalse();
            assertThat(reversed.vertexCount()).isEqualTo(3);
            assertThat(reversed.edgeCount()).isEqualTo(2);
            assertThat(reversed.containsEdge("A", "B")).isTrue();
            assertThat(reversed.containsEdge("B", "A")).isTrue();
            assertThat(reversed.containsEdge("B", "C")).isTrue();
        }

        @Test
        @DisplayName("反转有向图保留孤立顶点")
        void testReversePreservesIsolatedVertices() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addEdge("A", "B");

            Graph<String> reversed = GraphTransform.reverse(graph);

            assertThat(reversed.containsVertex("A")).isTrue();
            assertThat(reversed.containsVertex("B")).isTrue();
            assertThat(reversed.containsEdge("B", "A")).isTrue();
        }
    }

    @Nested
    @DisplayName("null参数测试")
    class NullParameterTests {

        @Test
        @DisplayName("mapVertices(null, mapper)应抛出NullPointerException")
        void mapVerticesNullGraphThrows() {
            assertThatThrownBy(() -> GraphTransform.mapVertices(null, String::valueOf))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("mapVertices(graph, null)应抛出NullPointerException")
        void mapVerticesNullMapperThrows() {
            assertThatThrownBy(() -> GraphTransform.mapVertices(OpenGraph.directed(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("filterVertices(null, predicate)应抛出NullPointerException")
        void filterVerticesNullGraphThrows() {
            assertThatThrownBy(() -> GraphTransform.filterVertices(null, v -> true))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("filterEdges(null, predicate)应抛出NullPointerException")
        void filterEdgesNullGraphThrows() {
            assertThatThrownBy(() -> GraphTransform.filterEdges(null, e -> true))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("reverse(null)应抛出NullPointerException")
        void reverseNullGraphThrows() {
            assertThatThrownBy(() -> GraphTransform.reverse(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
