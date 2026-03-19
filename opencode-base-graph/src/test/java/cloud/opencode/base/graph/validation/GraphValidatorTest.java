package cloud.opencode.base.graph.validation;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import cloud.opencode.base.graph.exception.InvalidEdgeException;
import cloud.opencode.base.graph.exception.InvalidVertexException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphValidator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("GraphValidator 测试")
class GraphValidatorTest {

    @Nested
    @DisplayName("validateVertex测试")
    class ValidateVertexTests {

        @Test
        @DisplayName("有效顶点不抛异常")
        void testValidVertex() {
            assertThatCode(() -> GraphValidator.validateVertex("A"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null顶点抛出异常")
        void testNullVertex() {
            assertThatThrownBy(() -> GraphValidator.validateVertex(null))
                .isInstanceOf(InvalidVertexException.class)
                .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("validateEdge测试")
    class ValidateEdgeTests {

        @Test
        @DisplayName("有效边不抛异常")
        void testValidEdge() {
            assertThatCode(() -> GraphValidator.validateEdge("A", "B"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null源顶点抛出异常")
        void testNullFrom() {
            assertThatThrownBy(() -> GraphValidator.validateEdge(null, "B"))
                .isInstanceOf(InvalidVertexException.class);
        }

        @Test
        @DisplayName("null目标顶点抛出异常")
        void testNullTo() {
            assertThatThrownBy(() -> GraphValidator.validateEdge("A", null))
                .isInstanceOf(InvalidVertexException.class);
        }

        @Test
        @DisplayName("有效权重边不抛异常")
        void testValidEdgeWithWeight() {
            assertThatCode(() -> GraphValidator.validateEdge("A", "B", 5.0))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("NaN权重抛出异常")
        void testNaNWeight() {
            assertThatThrownBy(() -> GraphValidator.validateEdge("A", "B", Double.NaN))
                .isInstanceOf(InvalidEdgeException.class);
        }

        @Test
        @DisplayName("Infinite权重抛出异常")
        void testInfiniteWeight() {
            assertThatThrownBy(() -> GraphValidator.validateEdge("A", "B", Double.POSITIVE_INFINITY))
                .isInstanceOf(InvalidEdgeException.class);
        }
    }

    @Nested
    @DisplayName("validateWeight测试")
    class ValidateWeightTests {

        @Test
        @DisplayName("有效权重不抛异常")
        void testValidWeight() {
            assertThatCode(() -> GraphValidator.validateWeight(5.0))
                .doesNotThrowAnyException();
            assertThatCode(() -> GraphValidator.validateWeight(-5.0))
                .doesNotThrowAnyException();
            assertThatCode(() -> GraphValidator.validateWeight(0.0))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("NaN权重抛出异常")
        void testNaNWeight() {
            assertThatThrownBy(() -> GraphValidator.validateWeight(Double.NaN))
                .isInstanceOf(InvalidEdgeException.class);
        }

        @Test
        @DisplayName("Infinite权重抛出异常")
        void testInfiniteWeight() {
            assertThatThrownBy(() -> GraphValidator.validateWeight(Double.POSITIVE_INFINITY))
                .isInstanceOf(InvalidEdgeException.class);
            assertThatThrownBy(() -> GraphValidator.validateWeight(Double.NEGATIVE_INFINITY))
                .isInstanceOf(InvalidEdgeException.class);
        }
    }

    @Nested
    @DisplayName("validateGraph测试")
    class ValidateGraphTests {

        @Test
        @DisplayName("null图返回错误")
        void testNullGraph() {
            ValidationResult result = GraphValidator.validateGraph(null);

            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).anyMatch(e -> e.contains("null"));
        }

        @Test
        @DisplayName("有效图返回无错误")
        void testValidGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            ValidationResult result = GraphValidator.validateGraph(graph);

            assertThat(result.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("检测自环警告")
        void testSelfLoopWarning() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "A", 1.0);

            ValidationResult result = GraphValidator.validateGraph(graph);

            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("Self-loop"));
        }

        @Test
        @DisplayName("检测孤立顶点警告")
        void testIsolatedVertexWarning() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addVertex("C");

            ValidationResult result = GraphValidator.validateGraph(graph);

            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("Isolated vertex"));
        }

        @Test
        @DisplayName("检测负权重警告")
        void testNegativeWeightWarning() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", -5.0);

            ValidationResult result = GraphValidator.validateGraph(graph);

            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("Negative"));
        }
    }

    @Nested
    @DisplayName("validateGraphStructure测试")
    class ValidateGraphStructureTests {

        @Test
        @DisplayName("有效图不抛异常")
        void testValidGraphStructure() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);

            assertThatCode(() -> GraphValidator.validateGraphStructure(graph))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validateDAG测试")
    class ValidateDAGTests {

        @Test
        @DisplayName("null图返回错误")
        void testNullGraph() {
            ValidationResult result = GraphValidator.validateDAG(null);

            assertThat(result.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("无向图返回错误")
        void testUndirectedGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            ValidationResult result = GraphValidator.validateDAG(graph);

            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).anyMatch(e -> e.contains("directed"));
        }

        @Test
        @DisplayName("有效DAG返回无错误")
        void testValidDAG() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            ValidationResult result = GraphValidator.validateDAG(graph);

            assertThat(result.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("带环图返回错误")
        void testCyclicGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            ValidationResult result = GraphValidator.validateDAG(graph);

            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).anyMatch(e -> e.contains("cycle"));
        }
    }

    @Nested
    @DisplayName("vertexExists测试")
    class VertexExistsTests {

        @Test
        @DisplayName("存在的顶点返回true")
        void testExistingVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(GraphValidator.vertexExists(graph, "A")).isTrue();
        }

        @Test
        @DisplayName("不存在的顶点返回false")
        void testNonExistingVertex() {
            Graph<String> graph = OpenGraph.directed();

            assertThat(GraphValidator.vertexExists(graph, "A")).isFalse();
        }

        @Test
        @DisplayName("null参数返回false")
        void testNullParams() {
            Graph<String> graph = OpenGraph.directed();

            assertThat(GraphValidator.vertexExists(null, "A")).isFalse();
            assertThat(GraphValidator.vertexExists(graph, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("edgeExists测试")
    class EdgeExistsTests {

        @Test
        @DisplayName("存在的边返回true")
        void testExistingEdge() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            assertThat(GraphValidator.edgeExists(graph, "A", "B")).isTrue();
        }

        @Test
        @DisplayName("不存在的边返回false")
        void testNonExistingEdge() {
            Graph<String> graph = OpenGraph.directed();

            assertThat(GraphValidator.edgeExists(graph, "A", "B")).isFalse();
        }

        @Test
        @DisplayName("null参数返回false")
        void testNullParams() {
            Graph<String> graph = OpenGraph.directed();

            assertThat(GraphValidator.edgeExists(null, "A", "B")).isFalse();
            assertThat(GraphValidator.edgeExists(graph, null, "B")).isFalse();
            assertThat(GraphValidator.edgeExists(graph, "A", null)).isFalse();
        }
    }
}
