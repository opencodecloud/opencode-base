package cloud.opencode.base.graph;

import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GraphDiff 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("GraphDiff 测试")
class GraphDiffTest {

    @Nested
    @DisplayName("compare测试")
    class CompareTests {

        @Test
        @DisplayName("相同图无差异")
        void testIdenticalGraphsNoDiff() {
            Graph<String> g1 = OpenGraph.directed();
            g1.addEdge("A", "B", 1.0);
            g1.addEdge("B", "C", 2.0);

            Graph<String> g2 = OpenGraph.directed();
            g2.addEdge("A", "B", 1.0);
            g2.addEdge("B", "C", 2.0);

            GraphDiff.DiffResult<String> diff = GraphDiff.compare(g1, g2);

            assertThat(diff.isEmpty()).isTrue();
            assertThat(diff.hasChanges()).isFalse();
            assertThat(diff.addedVertices()).isEmpty();
            assertThat(diff.removedVertices()).isEmpty();
            assertThat(diff.commonVertices()).containsExactlyInAnyOrder("A", "B", "C");
            assertThat(diff.addedEdges()).isEmpty();
            assertThat(diff.removedEdges()).isEmpty();
            assertThat(diff.commonEdges()).hasSize(2);
        }

        @Test
        @DisplayName("新增顶点")
        void testAddedVertices() {
            Graph<String> before = OpenGraph.directed();
            before.addVertex("A");

            Graph<String> after = OpenGraph.directed();
            after.addVertex("A");
            after.addVertex("B");

            GraphDiff.DiffResult<String> diff = GraphDiff.compare(before, after);

            assertThat(diff.addedVertices()).containsExactly("B");
            assertThat(diff.removedVertices()).isEmpty();
            assertThat(diff.commonVertices()).containsExactly("A");
            assertThat(diff.hasChanges()).isTrue();
        }

        @Test
        @DisplayName("删除顶点")
        void testRemovedVertices() {
            Graph<String> before = OpenGraph.directed();
            before.addVertex("A");
            before.addVertex("B");

            Graph<String> after = OpenGraph.directed();
            after.addVertex("A");

            GraphDiff.DiffResult<String> diff = GraphDiff.compare(before, after);

            assertThat(diff.addedVertices()).isEmpty();
            assertThat(diff.removedVertices()).containsExactly("B");
            assertThat(diff.commonVertices()).containsExactly("A");
        }

        @Test
        @DisplayName("新增边")
        void testAddedEdges() {
            Graph<String> before = OpenGraph.directed();
            before.addEdge("A", "B");

            Graph<String> after = OpenGraph.directed();
            after.addEdge("A", "B");
            after.addEdge("B", "C");

            GraphDiff.DiffResult<String> diff = GraphDiff.compare(before, after);

            assertThat(diff.addedEdges()).hasSize(1);
            assertThat(diff.addedEdges()).anyMatch(e -> e.from().equals("B") && e.to().equals("C"));
            assertThat(diff.removedEdges()).isEmpty();
        }

        @Test
        @DisplayName("删除边")
        void testRemovedEdges() {
            Graph<String> before = OpenGraph.directed();
            before.addEdge("A", "B");
            before.addEdge("B", "C");

            Graph<String> after = OpenGraph.directed();
            after.addEdge("A", "B");
            after.addVertex("C");

            GraphDiff.DiffResult<String> diff = GraphDiff.compare(before, after);

            assertThat(diff.removedEdges()).hasSize(1);
            assertThat(diff.removedEdges()).anyMatch(e -> e.from().equals("B") && e.to().equals("C"));
        }

        @Test
        @DisplayName("完全不同的图")
        void testCompletelyDifferentGraphs() {
            Graph<String> before = OpenGraph.directed();
            before.addEdge("A", "B");

            Graph<String> after = OpenGraph.directed();
            after.addEdge("X", "Y");

            GraphDiff.DiffResult<String> diff = GraphDiff.compare(before, after);

            assertThat(diff.addedVertices()).containsExactlyInAnyOrder("X", "Y");
            assertThat(diff.removedVertices()).containsExactlyInAnyOrder("A", "B");
            assertThat(diff.commonVertices()).isEmpty();
            assertThat(diff.addedEdges()).hasSize(1);
            assertThat(diff.removedEdges()).hasSize(1);
            assertThat(diff.commonEdges()).isEmpty();
        }

        @Test
        @DisplayName("两个空图无差异")
        void testEmptyGraphs() {
            Graph<String> before = OpenGraph.directed();
            Graph<String> after = OpenGraph.directed();

            GraphDiff.DiffResult<String> diff = GraphDiff.compare(before, after);

            assertThat(diff.isEmpty()).isTrue();
            assertThat(diff.hasChanges()).isFalse();
        }

        @Test
        @DisplayName("权重不同视为不同边")
        void testDifferentWeightEdges() {
            Graph<String> before = OpenGraph.directed();
            before.addEdge("A", "B", 1.0);

            Graph<String> after = OpenGraph.directed();
            after.addEdge("A", "B", 2.0);

            GraphDiff.DiffResult<String> diff = GraphDiff.compare(before, after);

            assertThat(diff.addedEdges()).hasSize(1);
            assertThat(diff.removedEdges()).hasSize(1);
            assertThat(diff.commonEdges()).isEmpty();
        }
    }

    @Nested
    @DisplayName("null参数和类型校验测试")
    class ValidationTests {

        @Test
        @DisplayName("compare(null, graph)应抛出NullPointerException")
        void compareNullBeforeThrows() {
            assertThatThrownBy(() -> GraphDiff.compare(null, OpenGraph.directed()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("compare(graph, null)应抛出NullPointerException")
        void compareNullAfterThrows() {
            assertThatThrownBy(() -> GraphDiff.compare(OpenGraph.directed(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("compare(有向图, 无向图)应抛出IllegalArgumentException")
        void compareDirectedVsUndirectedThrows() {
            assertThatThrownBy(() -> GraphDiff.compare(OpenGraph.directed(), OpenGraph.undirected()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("directed");
        }
    }
}
