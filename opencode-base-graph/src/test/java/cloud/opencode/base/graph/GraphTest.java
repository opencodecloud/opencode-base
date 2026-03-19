package cloud.opencode.base.graph;

import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Graph 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("Graph 接口测试")
class GraphTest {

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("DirectedGraph实现Graph接口")
        void testDirectedGraphImplementsGraph() {
            Graph<String> graph = new DirectedGraph<>();

            assertThat(graph).isInstanceOf(Graph.class);
        }

        @Test
        @DisplayName("UndirectedGraph实现Graph接口")
        void testUndirectedGraphImplementsGraph() {
            Graph<String> graph = new UndirectedGraph<>();

            assertThat(graph).isInstanceOf(Graph.class);
        }
    }

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodsTests {

        @Test
        @DisplayName("addVertex通过接口调用")
        void testAddVertexViaInterface() {
            Graph<String> graph = new DirectedGraph<>();

            graph.addVertex("A");

            assertThat(graph.containsVertex("A")).isTrue();
        }

        @Test
        @DisplayName("addEdge通过接口调用")
        void testAddEdgeViaInterface() {
            Graph<String> graph = new DirectedGraph<>();

            graph.addEdge("A", "B", 5.0);

            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.getWeight("A", "B")).isEqualTo(5.0);
        }

        @Test
        @DisplayName("removeVertex通过接口调用")
        void testRemoveVertexViaInterface() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");

            graph.removeVertex("A");

            assertThat(graph.containsVertex("A")).isFalse();
        }

        @Test
        @DisplayName("removeEdge通过接口调用")
        void testRemoveEdgeViaInterface() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");

            graph.removeEdge("A", "B");

            assertThat(graph.containsEdge("A", "B")).isFalse();
        }

        @Test
        @DisplayName("vertices通过接口调用")
        void testVerticesViaInterface() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");

            Set<String> vertices = graph.vertices();

            assertThat(vertices).containsExactlyInAnyOrder("A", "B");
        }

        @Test
        @DisplayName("edges通过接口调用")
        void testEdgesViaInterface() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            Set<Edge<String>> edges = graph.edges();

            assertThat(edges).hasSize(2);
        }

        @Test
        @DisplayName("neighbors通过接口调用")
        void testNeighborsViaInterface() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");

            Set<String> neighbors = graph.neighbors("A");

            assertThat(neighbors).containsExactlyInAnyOrder("B", "C");
        }
    }

    @Nested
    @DisplayName("多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("作为方法参数使用")
        void testAsMethodParameter() {
            Graph<String> directed = new DirectedGraph<>();
            Graph<String> undirected = new UndirectedGraph<>();

            directed.addEdge("A", "B");
            undirected.addEdge("X", "Y");

            assertThat(countEdges(directed)).isEqualTo(1);
            assertThat(countEdges(undirected)).isEqualTo(1);
        }

        private int countEdges(Graph<String> graph) {
            return graph.edgeCount();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("isEmpty方法")
        void testIsEmpty() {
            Graph<String> graph = new DirectedGraph<>();

            assertThat(graph.isEmpty()).isTrue();

            graph.addVertex("A");

            assertThat(graph.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("泛型测试")
    class GenericTests {

        @Test
        @DisplayName("Integer类型顶点")
        void testIntegerVertices() {
            Graph<Integer> graph = new DirectedGraph<>();
            graph.addEdge(1, 2);
            graph.addEdge(2, 3);

            assertThat(graph.containsVertex(1)).isTrue();
            assertThat(graph.containsEdge(1, 2)).isTrue();
        }

        @Test
        @DisplayName("自定义类型顶点")
        void testCustomTypeVertices() {
            record City(String name) {}
            Graph<City> graph = new DirectedGraph<>();
            City beijing = new City("Beijing");
            City shanghai = new City("Shanghai");

            graph.addEdge(beijing, shanghai, 1000.0);

            assertThat(graph.containsVertex(beijing)).isTrue();
            assertThat(graph.containsEdge(beijing, shanghai)).isTrue();
        }
    }
}
