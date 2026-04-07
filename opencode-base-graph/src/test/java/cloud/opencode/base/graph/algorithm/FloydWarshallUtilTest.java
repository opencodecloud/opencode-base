package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.DirectedGraph;
import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.exception.GraphErrorCode;
import cloud.opencode.base.graph.exception.GraphException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * FloydWarshallUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("FloydWarshallUtil 测试")
class FloydWarshallUtilTest {

    @Nested
    @DisplayName("compute测试")
    class ComputeTests {

        @Test
        @DisplayName("基本全源最短路径")
        void basicAllPairs() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 3.0);
            graph.addEdge("A", "C", 8.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("C", "D", 1.0);
            graph.addEdge("B", "D", 7.0);

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            // A→B = 3
            assertThat(result.distance("A", "B")).isEqualTo(3.0);
            // A→C = min(8, 3+2) = 5
            assertThat(result.distance("A", "C")).isEqualTo(5.0);
            // A→D = min(A→B→D=10, A→C→D=6, A→B→C→D=6) = 6
            assertThat(result.distance("A", "D")).isEqualTo(6.0);
            // B→C = 2
            assertThat(result.distance("B", "C")).isEqualTo(2.0);
            // B→D = min(7, 2+1) = 3
            assertThat(result.distance("B", "D")).isEqualTo(3.0);
            // C→D = 1
            assertThat(result.distance("C", "D")).isEqualTo(1.0);
            // Self-distances
            assertThat(result.distance("A", "A")).isEqualTo(0.0);
        }

        @Test
        @DisplayName("路径重建")
        void pathReconstruction() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 3.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("C", "D", 1.0);
            graph.addEdge("A", "D", 10.0);

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            // Shortest path A→D should be A→B→C→D = 6
            List<String> path = result.path("A", "D");
            assertThat(path).containsExactly("A", "B", "C", "D");
        }

        @Test
        @DisplayName("负权重无负环")
        void negativeWeightsNoCycle() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 4.0);
            graph.addEdge("B", "C", -2.0);
            graph.addEdge("A", "C", 5.0);

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            assertThat(result.hasNegativeCycle()).isFalse();
            // A→C = min(5, 4+(-2)) = 2
            assertThat(result.distance("A", "C")).isEqualTo(2.0);
        }

        @Test
        @DisplayName("负环检测")
        void negativeCycleDetection() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", -3.0);
            graph.addEdge("C", "A", 1.0);

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            assertThat(result.hasNegativeCycle()).isTrue();
        }

        @Test
        @DisplayName("不可达顶点距离为正无穷")
        void disconnectedVertices() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addVertex("C");

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            assertThat(result.distance("A", "C")).isEqualTo(Double.POSITIVE_INFINITY);
            assertThat(result.path("A", "C")).isEmpty();
        }

        @Test
        @DisplayName("单个顶点")
        void singleVertex() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addVertex("A");

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            assertThat(result.distance("A", "A")).isEqualTo(0.0);
            assertThat(result.path("A", "A")).containsExactly("A");
            assertThat(result.hasNegativeCycle()).isFalse();
        }

        @Test
        @DisplayName("空图")
        void emptyGraph() {
            FloydWarshallUtil.AllPairsResult<String> result =
                    FloydWarshallUtil.compute(new DirectedGraph<>());

            assertThat(result.hasNegativeCycle()).isFalse();
            assertThat(result.distanceMatrix()).isEmpty();
        }

        @Test
        @DisplayName("null图")
        void nullGraph() {
            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(null);

            assertThat(result.hasNegativeCycle()).isFalse();
            assertThat(result.distanceMatrix()).isEmpty();
            assertThat(result.distance("A", "B")).isEqualTo(Double.POSITIVE_INFINITY);
            assertThat(result.path("A", "B")).isEmpty();
        }

        @Test
        @DisplayName("自环不影响其他顶点间的距离")
        void selfLoopDoesNotAffectOtherPaths() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "A", 5.0); // self-loop
            graph.addEdge("A", "B", 2.0);
            graph.addEdge("B", "C", 3.0);

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            // Self-loop with positive weight: dist[A][A] stays 0 (not updated by Floyd-Warshall)
            assertThat(result.distance("A", "A")).isEqualTo(0.0);
            assertThat(result.distance("A", "B")).isEqualTo(2.0);
            assertThat(result.distance("A", "C")).isEqualTo(5.0);
            assertThat(result.hasNegativeCycle()).isFalse();
        }

        @Test
        @DisplayName("负自环被检测为负环")
        void negativeSelfLoopDetectedAsNegativeCycle() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "A", -1.0); // negative self-loop
            graph.addEdge("A", "B", 2.0);

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            assertThat(result.hasNegativeCycle()).isTrue();
            // With negative cycle, distance and path are unreliable
            assertThat(result.distance("A", "B")).isEqualTo(Double.POSITIVE_INFINITY);
            assertThat(result.path("A", "B")).isEmpty();
        }
    }

    @Nested
    @DisplayName("distanceMatrix测试")
    class DistanceMatrixTests {

        @Test
        @DisplayName("距离矩阵包含所有顶点对")
        void matrixContainsAllPairs() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);
            Map<String, Map<String, Double>> matrix = result.distanceMatrix();

            assertThat(matrix).hasSize(3);
            assertThat(matrix.get("A")).hasSize(3);
            assertThat(matrix.get("A").get("B")).isEqualTo(1.0);
            assertThat(matrix.get("A").get("C")).isEqualTo(3.0);
        }
    }

    @Nested
    @DisplayName("限制测试")
    class LimitTests {

        @Test
        @DisplayName("超过1000顶点应抛出GraphException")
        void exceedsVertexLimit() {
            Graph<Integer> graph = new DirectedGraph<>();
            for (int i = 0; i <= 1000; i++) {
                graph.addVertex(i);
            }

            assertThatThrownBy(() -> FloydWarshallUtil.compute(graph))
                    .isInstanceOf(GraphException.class)
                    .hasFieldOrPropertyWithValue("graphErrorCode", GraphErrorCode.LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("路径测试")
    class PathTests {

        @Test
        @DisplayName("同一顶点路径")
        void sameVertexPath() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            assertThat(result.path("A", "A")).containsExactly("A");
        }

        @Test
        @DisplayName("不存在路径返回空")
        void noPathReturnsEmpty() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addVertex("A");
            graph.addVertex("B");

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            assertThat(result.path("A", "B")).isEmpty();
        }

        @Test
        @DisplayName("未知顶点路径返回空")
        void unknownVertexReturnsEmpty() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);

            FloydWarshallUtil.AllPairsResult<String> result = FloydWarshallUtil.compute(graph);

            assertThat(result.path("A", "Z")).isEmpty();
            assertThat(result.distance("A", "Z")).isEqualTo(Double.POSITIVE_INFINITY);
        }
    }
}
