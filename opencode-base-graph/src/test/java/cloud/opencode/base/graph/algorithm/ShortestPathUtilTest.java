package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ShortestPathUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("ShortestPathUtil 测试")
class ShortestPathUtilTest {

    @Nested
    @DisplayName("dijkstra测试")
    class DijkstraTests {

        @Test
        @DisplayName("计算简单图的最短路径")
        void testSimpleGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 5.0);

            Map<String, Double> distances = ShortestPathUtil.dijkstra(graph, "A");

            assertThat(distances.get("A")).isEqualTo(0.0);
            assertThat(distances.get("B")).isEqualTo(1.0);
            assertThat(distances.get("C")).isEqualTo(3.0); // A->B->C = 3, not A->C = 5
        }

        @Test
        @DisplayName("复杂图的最短路径")
        void testComplexGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 4.0);
            graph.addEdge("A", "C", 2.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("B", "D", 5.0);
            graph.addEdge("C", "D", 8.0);
            graph.addEdge("C", "E", 10.0);
            graph.addEdge("D", "E", 2.0);

            Map<String, Double> distances = ShortestPathUtil.dijkstra(graph, "A");

            assertThat(distances.get("A")).isEqualTo(0.0);
            assertThat(distances.get("C")).isEqualTo(2.0);
            assertThat(distances.get("D")).isEqualTo(9.0); // A->B->D = 9
        }

        @Test
        @DisplayName("null图返回空map")
        void testNullGraph() {
            Map<String, Double> distances = ShortestPathUtil.dijkstra(null, "A");

            assertThat(distances).isEmpty();
        }

        @Test
        @DisplayName("null源顶点返回空map")
        void testNullSource() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            Map<String, Double> distances = ShortestPathUtil.dijkstra(graph, null);

            assertThat(distances).isEmpty();
        }

        @Test
        @DisplayName("源顶点不存在返回空map")
        void testSourceNotFound() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            Map<String, Double> distances = ShortestPathUtil.dijkstra(graph, "X");

            assertThat(distances).isEmpty();
        }
    }

    @Nested
    @DisplayName("shortestPath测试")
    class ShortestPathTests {

        @Test
        @DisplayName("找到最短路径")
        void testFindShortestPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 5.0);

            List<String> path = ShortestPathUtil.shortestPath(graph, "A", "C");

            assertThat(path).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("源和目标相同")
        void testSameSourceTarget() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> path = ShortestPathUtil.shortestPath(graph, "A", "A");

            assertThat(path).containsExactly("A");
        }

        @Test
        @DisplayName("无路径返回空列表")
        void testNoPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            List<String> path = ShortestPathUtil.shortestPath(graph, "A", "B");

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("null参数返回空列表")
        void testNullParams() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            assertThat(ShortestPathUtil.shortestPath(null, "A", "B")).isEmpty();
            assertThat(ShortestPathUtil.shortestPath(graph, null, "B")).isEmpty();
            assertThat(ShortestPathUtil.shortestPath(graph, "A", null)).isEmpty();
        }

        @Test
        @DisplayName("顶点不存在返回空列表")
        void testVertexNotFound() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            assertThat(ShortestPathUtil.shortestPath(graph, "X", "B")).isEmpty();
            assertThat(ShortestPathUtil.shortestPath(graph, "A", "Y")).isEmpty();
        }
    }

    @Nested
    @DisplayName("shortestDistance测试")
    class ShortestDistanceTests {

        @Test
        @DisplayName("计算最短距离")
        void testShortestDistance() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            double distance = ShortestPathUtil.shortestDistance(graph, "A", "C");

            assertThat(distance).isEqualTo(3.0);
        }

        @Test
        @DisplayName("无路径返回MAX_VALUE")
        void testNoPathDistance() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            double distance = ShortestPathUtil.shortestDistance(graph, "A", "B");

            assertThat(distance).isEqualTo(Double.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("hasPath测试")
    class HasPathTests {

        @Test
        @DisplayName("存在路径返回true")
        void testPathExists() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(ShortestPathUtil.hasPath(graph, "A", "C")).isTrue();
        }

        @Test
        @DisplayName("不存在路径返回false")
        void testNoPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            assertThat(ShortestPathUtil.hasPath(graph, "A", "B")).isFalse();
        }

        @Test
        @DisplayName("相同顶点返回true")
        void testSameVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(ShortestPathUtil.hasPath(graph, "A", "A")).isTrue();
        }

        @Test
        @DisplayName("null参数返回false")
        void testNullParams() {
            Graph<String> graph = OpenGraph.directed();

            assertThat(ShortestPathUtil.hasPath(null, "A", "B")).isFalse();
            assertThat(ShortestPathUtil.hasPath(graph, null, "B")).isFalse();
            assertThat(ShortestPathUtil.hasPath(graph, "A", null)).isFalse();
        }
    }
}
