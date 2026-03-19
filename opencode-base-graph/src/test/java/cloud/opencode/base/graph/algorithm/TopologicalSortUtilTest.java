package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import cloud.opencode.base.graph.exception.CycleDetectedException;
import cloud.opencode.base.graph.exception.GraphException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * TopologicalSortUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("TopologicalSortUtil 测试")
class TopologicalSortUtilTest {

    @Nested
    @DisplayName("sort (Kahn算法) 测试")
    class SortKahnTests {

        @Test
        @DisplayName("简单DAG拓扑排序")
        void testSimpleDAG() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");
            graph.addEdge("C", "D");

            List<String> order = TopologicalSortUtil.sort(graph);

            assertThat(order).hasSize(4);
            assertThat(order.indexOf("A")).isLessThan(order.indexOf("B"));
            assertThat(order.indexOf("A")).isLessThan(order.indexOf("C"));
            assertThat(order.indexOf("B")).isLessThan(order.indexOf("D"));
            assertThat(order.indexOf("C")).isLessThan(order.indexOf("D"));
        }

        @Test
        @DisplayName("有环图抛出异常")
        void testCyclicGraphThrows() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            assertThatThrownBy(() -> TopologicalSortUtil.sort(graph))
                .isInstanceOf(CycleDetectedException.class);
        }

        @Test
        @DisplayName("无向图抛出异常")
        void testUndirectedGraphThrows() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            assertThatThrownBy(() -> TopologicalSortUtil.sort(graph))
                .isInstanceOf(GraphException.class);
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> order = TopologicalSortUtil.sort(null);

            assertThat(order).isEmpty();
        }

        @Test
        @DisplayName("空图返回空列表")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.directed();

            List<String> order = TopologicalSortUtil.sort(graph);

            assertThat(order).isEmpty();
        }
    }

    @Nested
    @DisplayName("sortDfs (DFS算法) 测试")
    class SortDfsTests {

        @Test
        @DisplayName("DFS拓扑排序")
        void testDfsSort() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            List<String> order = TopologicalSortUtil.sortDfs(graph);

            assertThat(order).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("有环图抛出异常")
        void testCyclicGraphThrows() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "A");

            assertThatThrownBy(() -> TopologicalSortUtil.sortDfs(graph))
                .isInstanceOf(CycleDetectedException.class);
        }
    }

    @Nested
    @DisplayName("canSort测试")
    class CanSortTests {

        @Test
        @DisplayName("DAG可以排序")
        void testDAGCanSort() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(TopologicalSortUtil.canSort(graph)).isTrue();
        }

        @Test
        @DisplayName("有环图不能排序")
        void testCyclicCannotSort() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "A");

            assertThat(TopologicalSortUtil.canSort(graph)).isFalse();
        }

        @Test
        @DisplayName("null图不能排序")
        void testNullCannotSort() {
            assertThat(TopologicalSortUtil.canSort(null)).isFalse();
        }

        @Test
        @DisplayName("无向图不能排序")
        void testUndirectedCannotSort() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            assertThat(TopologicalSortUtil.canSort(graph)).isFalse();
        }
    }

    @Nested
    @DisplayName("getSourceVertices测试")
    class GetSourceVerticesTests {

        @Test
        @DisplayName("获取源顶点（入度为0）")
        void testGetSourceVertices() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "C");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            Set<String> sources = TopologicalSortUtil.getSourceVertices(graph);

            assertThat(sources).containsExactlyInAnyOrder("A", "B");
        }

        @Test
        @DisplayName("空图返回空集")
        void testEmptyGraph() {
            Set<String> sources = TopologicalSortUtil.getSourceVertices(OpenGraph.directed());

            assertThat(sources).isEmpty();
        }

        @Test
        @DisplayName("null图返回空集")
        void testNullGraph() {
            Set<String> sources = TopologicalSortUtil.getSourceVertices(null);

            assertThat(sources).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSinkVertices测试")
    class GetSinkVerticesTests {

        @Test
        @DisplayName("获取汇顶点（出度为0）")
        void testGetSinkVertices() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("B", "D");
            graph.addEdge("C", "D");

            Set<String> sinks = TopologicalSortUtil.getSinkVertices(graph);

            assertThat(sinks).containsExactly("D");
        }

        @Test
        @DisplayName("空图返回空集")
        void testEmptyGraph() {
            Set<String> sinks = TopologicalSortUtil.getSinkVertices(OpenGraph.directed());

            assertThat(sinks).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDependencyDepths测试")
    class GetDependencyDepthsTests {

        @Test
        @DisplayName("获取依赖深度")
        void testGetDependencyDepths() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

            Map<String, Integer> depths = TopologicalSortUtil.getDependencyDepths(graph);

            assertThat(depths.get("A")).isEqualTo(0);
            assertThat(depths.get("B")).isEqualTo(1);
            assertThat(depths.get("C")).isEqualTo(2); // Max path: A->B->C
        }

        @Test
        @DisplayName("空图返回空map")
        void testEmptyGraph() {
            Map<String, Integer> depths = TopologicalSortUtil.getDependencyDepths(OpenGraph.directed());

            assertThat(depths).isEmpty();
        }
    }
}
