package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.DirectedGraph;
import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.exception.GraphErrorCode;
import cloud.opencode.base.graph.exception.GraphException;
import cloud.opencode.base.graph.exception.InvalidVertexException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * BellmanFordUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("BellmanFordUtil 测试")
class BellmanFordUtilTest {

    @Nested
    @DisplayName("shortestPaths测试")
    class ShortestPathsTests {

        @Test
        @DisplayName("正权重最短路径与Dijkstra一致")
        void positiveWeights() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 4.0);
            graph.addEdge("A", "C", 2.0);
            graph.addEdge("B", "D", 3.0);
            graph.addEdge("C", "B", 1.0);
            graph.addEdge("C", "D", 5.0);

            Map<String, Double> distances = BellmanFordUtil.shortestPaths(graph, "A");

            assertThat(distances.get("A")).isEqualTo(0.0);
            assertThat(distances.get("B")).isEqualTo(3.0); // A→C→B = 2+1
            assertThat(distances.get("C")).isEqualTo(2.0);
            assertThat(distances.get("D")).isEqualTo(6.0); // A→C→B→D = 2+1+3
        }

        @Test
        @DisplayName("负权重边计算正确距离")
        void negativeWeights() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 4.0);
            graph.addEdge("A", "C", 2.0);
            graph.addEdge("B", "C", -3.0);
            graph.addEdge("C", "D", 1.0);

            Map<String, Double> distances = BellmanFordUtil.shortestPaths(graph, "A");

            assertThat(distances.get("A")).isEqualTo(0.0);
            assertThat(distances.get("B")).isEqualTo(4.0);
            assertThat(distances.get("C")).isEqualTo(1.0); // A→B→C = 4+(-3) = 1 < A→C = 2
            assertThat(distances.get("D")).isEqualTo(2.0); // A→B→C→D = 4+(-3)+1 = 2
        }

        @Test
        @DisplayName("负环应抛出GraphException")
        void negativeCycleThrows() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", -1.0);
            graph.addEdge("C", "A", -1.0);

            assertThatThrownBy(() -> BellmanFordUtil.shortestPaths(graph, "A"))
                    .isInstanceOf(GraphException.class)
                    .hasFieldOrPropertyWithValue("graphErrorCode", GraphErrorCode.NEGATIVE_CYCLE);
        }

        @Test
        @DisplayName("不可达顶点距离为正无穷")
        void unreachableVertex() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addVertex("C");

            Map<String, Double> distances = BellmanFordUtil.shortestPaths(graph, "A");

            assertThat(distances.get("C")).isEqualTo(Double.POSITIVE_INFINITY);
        }

        @Test
        @DisplayName("单个顶点")
        void singleVertex() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addVertex("A");

            Map<String, Double> distances = BellmanFordUtil.shortestPaths(graph, "A");

            assertThat(distances).hasSize(1);
            assertThat(distances.get("A")).isEqualTo(0.0);
        }

        @Test
        @DisplayName("null源顶点应抛出InvalidVertexException")
        void nullSourceThrows() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addVertex("A");

            assertThatThrownBy(() -> BellmanFordUtil.shortestPaths(graph, null))
                    .isInstanceOf(InvalidVertexException.class);
        }

        @Test
        @DisplayName("null图返回空Map")
        void nullGraph() {
            Map<String, Double> distances = BellmanFordUtil.shortestPaths(null, "A");

            assertThat(distances).isEmpty();
        }
    }

    @Nested
    @DisplayName("shortestPath测试")
    class ShortestPathTests {

        @Test
        @DisplayName("负权重路径重建")
        void pathWithNegativeWeights() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 4.0);
            graph.addEdge("A", "C", 2.0);
            graph.addEdge("B", "C", -3.0);
            graph.addEdge("C", "D", 1.0);

            List<String> path = BellmanFordUtil.shortestPath(graph, "A", "D");

            assertThat(path).containsExactly("A", "B", "C", "D");
        }

        @Test
        @DisplayName("无路径返回空列表")
        void noPath() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addVertex("C");

            List<String> path = BellmanFordUtil.shortestPath(graph, "A", "C");

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("源和目标相同返回单元素")
        void sameSourceAndTarget() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addVertex("A");

            List<String> path = BellmanFordUtil.shortestPath(graph, "A", "A");

            assertThat(path).containsExactly("A");
        }

        @Test
        @DisplayName("null目标应抛出InvalidVertexException")
        void nullTargetThrows() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addVertex("A");

            assertThatThrownBy(() -> BellmanFordUtil.shortestPath(graph, "A", null))
                    .isInstanceOf(InvalidVertexException.class);
        }

        @Test
        @DisplayName("负环应抛出GraphException")
        void negativeCycleThrows() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", -1.0);
            graph.addEdge("C", "A", -1.0);
            graph.addVertex("D");

            assertThatThrownBy(() -> BellmanFordUtil.shortestPath(graph, "A", "D"))
                    .isInstanceOf(GraphException.class)
                    .hasFieldOrPropertyWithValue("graphErrorCode", GraphErrorCode.NEGATIVE_CYCLE);
        }
    }

    @Nested
    @DisplayName("hasNegativeCycle测试")
    class HasNegativeCycleTests {

        @Test
        @DisplayName("存在负环返回true")
        void hasNegativeCycle() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", -2.0);
            graph.addEdge("C", "A", -1.0);

            assertThat(BellmanFordUtil.hasNegativeCycle(graph, "A")).isTrue();
        }

        @Test
        @DisplayName("不存在负环返回false")
        void noNegativeCycle() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            assertThat(BellmanFordUtil.hasNegativeCycle(graph, "A")).isFalse();
        }

        @Test
        @DisplayName("负权重边但无负环返回false")
        void negativeWeightsNoCycle() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", -1.0);
            graph.addEdge("B", "C", 3.0);

            assertThat(BellmanFordUtil.hasNegativeCycle(graph, "A")).isFalse();
        }

        @Test
        @DisplayName("null源顶点应抛出异常")
        void nullSourceThrows() {
            Graph<String> graph = new DirectedGraph<>();

            assertThatThrownBy(() -> BellmanFordUtil.hasNegativeCycle(graph, null))
                    .isInstanceOf(InvalidVertexException.class);
        }
    }

    @Nested
    @DisplayName("findNegativeCycle测试")
    class FindNegativeCycleTests {

        @Test
        @DisplayName("返回实际的负环")
        void returnsActualCycle() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", -2.0);
            graph.addEdge("C", "A", -1.0);

            List<String> cycle = BellmanFordUtil.findNegativeCycle(graph, "A");

            assertThat(cycle).isNotEmpty();
            // The cycle should contain all three vertices and form a cycle
            assertThat(cycle.getFirst()).isEqualTo(cycle.getLast());
            assertThat(cycle).containsAll(List.of("A", "B", "C"));
        }

        @Test
        @DisplayName("无负环返回空列表")
        void noCycleReturnsEmpty() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            List<String> cycle = BellmanFordUtil.findNegativeCycle(graph, "A");

            assertThat(cycle).isEmpty();
        }

        @Test
        @DisplayName("从源不可达的负权环不影响结果")
        void negativeCycleUnreachableFromSource() {
            // A -> B is the reachable part; X -> Y -> X is a negative cycle unreachable from A
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("X", "Y", -1.0);
            graph.addEdge("Y", "X", -1.0);

            // From A's perspective, the negative cycle at X/Y is not reachable
            assertThat(BellmanFordUtil.hasNegativeCycle(graph, "A")).isFalse();

            Map<String, Double> distances = BellmanFordUtil.shortestPaths(graph, "A");
            assertThat(distances.get("A")).isEqualTo(0.0);
            assertThat(distances.get("B")).isEqualTo(1.0);
            // X and Y are not reachable from A
            assertThat(distances.get("X")).isEqualTo(Double.POSITIVE_INFINITY);
        }

        @Test
        @DisplayName("null源顶点应抛出异常")
        void nullSourceThrows() {
            assertThatThrownBy(() -> BellmanFordUtil.findNegativeCycle(new DirectedGraph<>(), null))
                    .isInstanceOf(InvalidVertexException.class);
        }
    }
}
