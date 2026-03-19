package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SafeGraphTraversalUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("SafeGraphTraversalUtil 测试")
class SafeGraphTraversalUtilTest {

    @Nested
    @DisplayName("dfsIterative测试")
    class DfsIterativeTests {

        @Test
        @DisplayName("迭代DFS遍历简单图")
        void testSimpleGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "D");

            List<String> result = SafeGraphTraversalUtil.dfsIterative(graph, "A");

            assertThat(result).startsWith("A");
            assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D");
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> result = SafeGraphTraversalUtil.dfsIterative(null, "A");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null起始顶点返回空列表")
        void testNullStart() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> result = SafeGraphTraversalUtil.dfsIterative(graph, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("起始顶点不存在返回空列表")
        void testStartNotFound() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> result = SafeGraphTraversalUtil.dfsIterative(graph, "X");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("dfsIterative带访问器测试")
    class DfsIterativeWithVisitorTests {

        @Test
        @DisplayName("访问器被调用")
        void testVisitorCalled() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            List<String> visited = new ArrayList<>();
            SafeGraphTraversalUtil.dfsIterative(graph, "A", visited::add);

            assertThat(visited).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("null访问器不执行")
        void testNullVisitor() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThatCode(() -> SafeGraphTraversalUtil.dfsIterative(graph, "A", null))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null参数不抛异常")
        void testNullParams() {
            assertThatCode(() -> SafeGraphTraversalUtil.dfsIterative(null, "A", v -> {}))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("dfsWithLimit测试")
    class DfsWithLimitTests {

        @Test
        @DisplayName("深度限制为0只返回起始顶点")
        void testDepthZero() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            List<String> result = SafeGraphTraversalUtil.dfsWithLimit(graph, "A", 0);

            assertThat(result).containsExactly("A");
        }

        @Test
        @DisplayName("深度限制为1返回起始顶点和直接邻居")
        void testDepthOne() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");

            List<String> result = SafeGraphTraversalUtil.dfsWithLimit(graph, "A", 1);

            assertThat(result).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("负深度返回空列表")
        void testNegativeDepth() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> result = SafeGraphTraversalUtil.dfsWithLimit(graph, "A", -1);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> result = SafeGraphTraversalUtil.dfsWithLimit(null, "A", 5);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("dfsIterativeWithLimit测试")
    class DfsIterativeWithLimitTests {

        @Test
        @DisplayName("迭代式深度限制DFS")
        void testIterativeWithLimit() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            List<String> result = SafeGraphTraversalUtil.dfsIterativeWithLimit(graph, "A", 2);

            assertThat(result).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("深度限制为0只返回起始顶点")
        void testDepthZero() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            List<String> result = SafeGraphTraversalUtil.dfsIterativeWithLimit(graph, "A", 0);

            assertThat(result).containsExactly("A");
        }

        @Test
        @DisplayName("负深度返回空列表")
        void testNegativeDepth() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> result = SafeGraphTraversalUtil.dfsIterativeWithLimit(graph, "A", -1);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("bfsWithLimit测试")
    class BfsWithLimitTests {

        @Test
        @DisplayName("BFS深度限制")
        void testBfsWithLimit() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");
            graph.addEdge("C", "E");
            graph.addEdge("D", "F");

            List<String> result = SafeGraphTraversalUtil.bfsWithLimit(graph, "A", 2);

            assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D", "E");
        }

        @Test
        @DisplayName("距离限制为0只返回起始顶点")
        void testDistanceZero() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            List<String> result = SafeGraphTraversalUtil.bfsWithLimit(graph, "A", 0);

            assertThat(result).containsExactly("A");
        }

        @Test
        @DisplayName("距离限制为1返回直接邻居")
        void testDistanceOne() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");

            List<String> result = SafeGraphTraversalUtil.bfsWithLimit(graph, "A", 1);

            assertThat(result).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("负距离返回空列表")
        void testNegativeDistance() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> result = SafeGraphTraversalUtil.bfsWithLimit(graph, "A", -1);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> result = SafeGraphTraversalUtil.bfsWithLimit(null, "A", 5);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("起始顶点不存在返回空列表")
        void testStartNotFound() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> result = SafeGraphTraversalUtil.bfsWithLimit(graph, "X", 5);

            assertThat(result).isEmpty();
        }
    }
}
