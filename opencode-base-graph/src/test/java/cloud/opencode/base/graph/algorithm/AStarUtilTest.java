package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.*;

/**
 * AStarUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("AStarUtil 测试")
class AStarUtilTest {

    @Nested
    @DisplayName("findPath测试")
    class FindPathTests {

        @Test
        @DisplayName("使用启发式函数查找路径")
        void testFindPathWithHeuristic() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("A", "C", 3.0);

            BiFunction<String, String, Double> heuristic = (a, b) -> 0.0;
            List<String> path = AStarUtil.findPath(graph, "A", "C", heuristic);

            assertThat(path).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("使用零启发式函数（等同Dijkstra）")
        void testFindPathWithZeroHeuristic() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);

            List<String> path = AStarUtil.findPath(graph, "A", "C");

            assertThat(path).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("起点等于终点返回单元素列表")
        void testSameSourceAndTarget() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> path = AStarUtil.findPath(graph, "A", "A", (a, b) -> 0.0);

            assertThat(path).containsExactly("A");
        }

        @Test
        @DisplayName("无路径返回空列表")
        void testNoPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            List<String> path = AStarUtil.findPath(graph, "A", "B", (a, b) -> 0.0);

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> path = AStarUtil.findPath(null, "A", "B", (a, b) -> 0.0);

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("null参数返回空列表")
        void testNullParams() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(AStarUtil.findPath(graph, null, "B", (a, b) -> 0.0)).isEmpty();
            assertThat(AStarUtil.findPath(graph, "A", null, (a, b) -> 0.0)).isEmpty();
            assertThat(AStarUtil.findPath(graph, "A", "B", null)).isEmpty();
        }

        @Test
        @DisplayName("顶点不存在返回空列表")
        void testVertexNotFound() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(AStarUtil.findPath(graph, "A", "X", (a, b) -> 0.0)).isEmpty();
            assertThat(AStarUtil.findPath(graph, "X", "A", (a, b) -> 0.0)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findPathWithCostLimit测试")
    class FindPathWithCostLimitTests {

        @Test
        @DisplayName("路径成本在限制内返回路径")
        void testPathWithinCostLimit() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);

            List<String> path = AStarUtil.findPathWithCostLimit(graph, "A", "C", (a, b) -> 0.0, 5.0);

            assertThat(path).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("路径成本超出限制返回空列表")
        void testPathExceedsCostLimit() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 3.0);
            graph.addEdge("B", "C", 3.0);

            List<String> path = AStarUtil.findPathWithCostLimit(graph, "A", "C", (a, b) -> 0.0, 5.0);

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("无路径返回空列表")
        void testNoPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            List<String> path = AStarUtil.findPathWithCostLimit(graph, "A", "B", (a, b) -> 0.0, 10.0);

            assertThat(path).isEmpty();
        }
    }

    @Nested
    @DisplayName("findPathDetailed测试")
    class FindPathDetailedTests {

        @Test
        @DisplayName("返回详细路径结果")
        void testDetailedResult() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            AStarUtil.PathResult<String> result = AStarUtil.findPathDetailed(
                graph, "A", "C", (a, b) -> 0.0);

            assertThat(result.path()).containsExactly("A", "B", "C");
            assertThat(result.cost()).isEqualTo(3.0);
            assertThat(result.nodesExpanded()).isGreaterThan(0);
            assertThat(result.hasPath()).isTrue();
        }

        @Test
        @DisplayName("起点等于终点")
        void testSameSourceAndTarget() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            AStarUtil.PathResult<String> result = AStarUtil.findPathDetailed(
                graph, "A", "A", (a, b) -> 0.0);

            assertThat(result.path()).containsExactly("A");
            assertThat(result.cost()).isEqualTo(0.0);
            assertThat(result.nodesExpanded()).isEqualTo(1);
        }

        @Test
        @DisplayName("无路径返回空结果")
        void testNoPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            AStarUtil.PathResult<String> result = AStarUtil.findPathDetailed(
                graph, "A", "B", (a, b) -> 0.0);

            assertThat(result.path()).isEmpty();
            assertThat(result.cost()).isEqualTo(Double.MAX_VALUE);
            assertThat(result.hasPath()).isFalse();
        }

        @Test
        @DisplayName("null参数返回空结果")
        void testNullParams() {
            AStarUtil.PathResult<String> result = AStarUtil.findPathDetailed(
                null, "A", "B", (a, b) -> 0.0);

            assertThat(result.path()).isEmpty();
            assertThat(result.hasPath()).isFalse();
        }
    }

    @Nested
    @DisplayName("PathResult记录测试")
    class PathResultTests {

        @Test
        @DisplayName("hasPath方法")
        void testHasPath() {
            AStarUtil.PathResult<String> withPath = new AStarUtil.PathResult<>(
                List.of("A", "B"), 1.0, 2);
            AStarUtil.PathResult<String> noPath = new AStarUtil.PathResult<>(
                List.of(), Double.MAX_VALUE, 5);

            assertThat(withPath.hasPath()).isTrue();
            assertThat(noPath.hasPath()).isFalse();
        }
    }
}
