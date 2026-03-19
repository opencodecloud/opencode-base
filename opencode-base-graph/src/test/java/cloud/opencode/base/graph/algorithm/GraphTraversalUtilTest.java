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
 * GraphTraversalUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("GraphTraversalUtil 测试")
class GraphTraversalUtilTest {

    @Nested
    @DisplayName("BFS测试")
    class BfsTests {

        @Test
        @DisplayName("BFS遍历简单图")
        void testSimpleBfs() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");
            graph.addEdge("C", "D");

            List<String> result = GraphTraversalUtil.bfs(graph, "A");

            assertThat(result).startsWith("A");
            assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D");
        }

        @Test
        @DisplayName("BFS层级遍历")
        void testBfsLevelOrder() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");
            graph.addEdge("C", "E");

            List<String> result = GraphTraversalUtil.bfs(graph, "A");

            // A first, then B and C, then D and E
            assertThat(result.indexOf("A")).isEqualTo(0);
            assertThat(result.indexOf("B")).isLessThan(result.indexOf("D"));
            assertThat(result.indexOf("C")).isLessThan(result.indexOf("E"));
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> result = GraphTraversalUtil.bfs(null, "A");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null起始顶点返回空列表")
        void testNullStart() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> result = GraphTraversalUtil.bfs(graph, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("起始顶点不存在返回空列表")
        void testStartNotFound() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> result = GraphTraversalUtil.bfs(graph, "X");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("BFS带访问器测试")
    class BfsWithVisitorTests {

        @Test
        @DisplayName("访问器被调用")
        void testVisitorCalled() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            List<String> visited = new ArrayList<>();
            GraphTraversalUtil.bfs(graph, "A", visited::add);

            assertThat(visited).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("null访问器不抛异常")
        void testNullVisitor() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThatCode(() -> GraphTraversalUtil.bfs(graph, "A", null))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("DFS测试")
    class DfsTests {

        @Test
        @DisplayName("DFS遍历简单图")
        void testSimpleDfs() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "D");

            List<String> result = GraphTraversalUtil.dfs(graph, "A");

            assertThat(result).startsWith("A");
            assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D");
        }

        @Test
        @DisplayName("DFS深度优先顺序")
        void testDfsDepthFirst() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            List<String> result = GraphTraversalUtil.dfs(graph, "A");

            // Should follow one path completely before backtracking
            assertThat(result).containsExactly("A", "B", "C", "D");
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> result = GraphTraversalUtil.dfs(null, "A");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("起始顶点不存在返回空列表")
        void testStartNotFound() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> result = GraphTraversalUtil.dfs(graph, "X");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("DFS带访问器测试")
    class DfsWithVisitorTests {

        @Test
        @DisplayName("访问器被调用")
        void testVisitorCalled() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            List<String> visited = new ArrayList<>();
            GraphTraversalUtil.dfs(graph, "A", visited::add);

            assertThat(visited).containsExactly("A", "B", "C");
        }
    }

    @Nested
    @DisplayName("bfsAll测试")
    class BfsAllTests {

        @Test
        @DisplayName("遍历所有顶点（包括不连通分量）")
        void testBfsAllDisconnected() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("C", "D");

            List<String> result = GraphTraversalUtil.bfsAll(graph);

            assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D");
        }

        @Test
        @DisplayName("空图返回空列表")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.directed();

            List<String> result = GraphTraversalUtil.bfsAll(graph);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> result = GraphTraversalUtil.bfsAll(null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("dfsAll测试")
    class DfsAllTests {

        @Test
        @DisplayName("遍历所有顶点（包括不连通分量）")
        void testDfsAllDisconnected() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("C", "D");

            List<String> result = GraphTraversalUtil.dfsAll(graph);

            assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D");
        }

        @Test
        @DisplayName("空图返回空列表")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.directed();

            List<String> result = GraphTraversalUtil.dfsAll(graph);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> result = GraphTraversalUtil.dfsAll(null);

            assertThat(result).isEmpty();
        }
    }
}
