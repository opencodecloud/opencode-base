package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * BidirectionalBfsUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("BidirectionalBfsUtil 测试")
class BidirectionalBfsUtilTest {

    @Nested
    @DisplayName("findPath测试")
    class FindPathTests {

        @Test
        @DisplayName("查找简单路径")
        void testSimplePath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            List<String> path = BidirectionalBfsUtil.findPath(graph, "A", "D");

            assertThat(path).isNotEmpty();
            assertThat(path).startsWith("A");
            assertThat(path).endsWith("D");
        }

        @Test
        @DisplayName("无向图查找路径")
        void testUndirectedPath() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            List<String> path = BidirectionalBfsUtil.findPath(graph, "A", "C");

            assertThat(path).isNotEmpty();
            assertThat(path).startsWith("A");
            assertThat(path).endsWith("C");
        }

        @Test
        @DisplayName("起点等于终点返回单元素列表")
        void testSameSourceAndTarget() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> path = BidirectionalBfsUtil.findPath(graph, "A", "A");

            assertThat(path).containsExactly("A");
        }

        @Test
        @DisplayName("无路径返回空列表")
        void testNoPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            List<String> path = BidirectionalBfsUtil.findPath(graph, "A", "B");

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> path = BidirectionalBfsUtil.findPath(null, "A", "B");

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("null参数返回空列表")
        void testNullParams() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(BidirectionalBfsUtil.findPath(graph, null, "B")).isEmpty();
            assertThat(BidirectionalBfsUtil.findPath(graph, "A", null)).isEmpty();
        }

        @Test
        @DisplayName("顶点不存在返回空列表")
        void testVertexNotFound() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(BidirectionalBfsUtil.findPath(graph, "A", "X")).isEmpty();
            assertThat(BidirectionalBfsUtil.findPath(graph, "X", "A")).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasPath测试")
    class HasPathTests {

        @Test
        @DisplayName("路径存在返回true")
        void testPathExists() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(BidirectionalBfsUtil.hasPath(graph, "A", "C")).isTrue();
        }

        @Test
        @DisplayName("路径不存在返回false")
        void testPathNotExists() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            assertThat(BidirectionalBfsUtil.hasPath(graph, "A", "B")).isFalse();
        }

        @Test
        @DisplayName("相同顶点返回true")
        void testSameVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(BidirectionalBfsUtil.hasPath(graph, "A", "A")).isTrue();
        }
    }

    @Nested
    @DisplayName("shortestPathLength测试")
    class ShortestPathLengthTests {

        @Test
        @DisplayName("计算最短路径长度")
        void testShortestPathLength() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            int length = BidirectionalBfsUtil.shortestPathLength(graph, "A", "D");

            assertThat(length).isEqualTo(3);
        }

        @Test
        @DisplayName("相同顶点长度为0")
        void testSameVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            int length = BidirectionalBfsUtil.shortestPathLength(graph, "A", "A");

            assertThat(length).isEqualTo(0);
        }

        @Test
        @DisplayName("无路径返回-1")
        void testNoPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");
            graph.addVertex("B");

            int length = BidirectionalBfsUtil.shortestPathLength(graph, "A", "B");

            assertThat(length).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("findVerticesOnPath测试")
    class FindVerticesOnPathTests {

        @Test
        @DisplayName("查找路径上的顶点")
        void testFindVerticesOnPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            Set<String> vertices = BidirectionalBfsUtil.findVerticesOnPath(graph, "A", "D", 5);

            assertThat(vertices).contains("A", "D");
        }

        @Test
        @DisplayName("null参数返回空集")
        void testNullParams() {
            Graph<String> graph = OpenGraph.directed();

            assertThat(BidirectionalBfsUtil.findVerticesOnPath(null, "A", "B", 5)).isEmpty();
            assertThat(BidirectionalBfsUtil.findVerticesOnPath(graph, null, "B", 5)).isEmpty();
            assertThat(BidirectionalBfsUtil.findVerticesOnPath(graph, "A", null, 5)).isEmpty();
        }

        @Test
        @DisplayName("负距离返回空集")
        void testNegativeDistance() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            Set<String> vertices = BidirectionalBfsUtil.findVerticesOnPath(graph, "A", "B", -1);

            assertThat(vertices).isEmpty();
        }
    }
}
