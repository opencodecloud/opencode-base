package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CycleDetectionUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("CycleDetectionUtil 测试")
class CycleDetectionUtilTest {

    @Nested
    @DisplayName("hasCycle - 有向图测试")
    class HasCycleDirectedTests {

        @Test
        @DisplayName("有向图有环返回true")
        void testDirectedWithCycle() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            assertThat(CycleDetectionUtil.hasCycle(graph)).isTrue();
        }

        @Test
        @DisplayName("有向图无环返回false")
        void testDirectedWithoutCycle() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C");

            assertThat(CycleDetectionUtil.hasCycle(graph)).isFalse();
        }

        @Test
        @DisplayName("自环返回true")
        void testSelfLoop() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "A");

            assertThat(CycleDetectionUtil.hasCycle(graph)).isTrue();
        }
    }

    @Nested
    @DisplayName("hasCycle - 无向图测试")
    class HasCycleUndirectedTests {

        @Test
        @DisplayName("无向图有环返回true")
        void testUndirectedWithCycle() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            assertThat(CycleDetectionUtil.hasCycle(graph)).isTrue();
        }

        @Test
        @DisplayName("无向图无环返回false（树）")
        void testUndirectedTree() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "D");

            assertThat(CycleDetectionUtil.hasCycle(graph)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasCycle - 边界情况测试")
    class HasCycleEdgeCasesTests {

        @Test
        @DisplayName("null图返回false")
        void testNullGraph() {
            assertThat(CycleDetectionUtil.hasCycle(null)).isFalse();
        }

        @Test
        @DisplayName("空图返回false")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.directed();

            assertThat(CycleDetectionUtil.hasCycle(graph)).isFalse();
        }

        @Test
        @DisplayName("单顶点无环")
        void testSingleVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(CycleDetectionUtil.hasCycle(graph)).isFalse();
        }
    }

    @Nested
    @DisplayName("findCycle - 有向图测试")
    class FindCycleDirectedTests {

        @Test
        @DisplayName("找到有向图中的环")
        void testFindCycleDirected() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            List<String> cycle = CycleDetectionUtil.findCycle(graph);

            assertThat(cycle).isNotEmpty();
            assertThat(cycle).containsAll(List.of("A", "B", "C"));
        }

        @Test
        @DisplayName("无环返回空列表")
        void testNoCycleDirected() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            List<String> cycle = CycleDetectionUtil.findCycle(graph);

            assertThat(cycle).isEmpty();
        }
    }

    @Nested
    @DisplayName("findCycle - 无向图测试")
    class FindCycleUndirectedTests {

        @Test
        @DisplayName("找到无向图中的环")
        void testFindCycleUndirected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            List<String> cycle = CycleDetectionUtil.findCycle(graph);

            assertThat(cycle).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("findCycle - 边界情况测试")
    class FindCycleEdgeCasesTests {

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<String> cycle = CycleDetectionUtil.findCycle(null);

            assertThat(cycle).isEmpty();
        }

        @Test
        @DisplayName("空图返回空列表")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.directed();

            List<String> cycle = CycleDetectionUtil.findCycle(graph);

            assertThat(cycle).isEmpty();
        }
    }

    @Nested
    @DisplayName("wouldCreateCycle测试")
    class WouldCreateCycleTests {

        @Test
        @DisplayName("添加边会创建环返回true")
        void testWouldCreateCycle() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            // Adding C->A would create a cycle
            assertThat(CycleDetectionUtil.wouldCreateCycle(graph, "C", "A")).isTrue();
        }

        @Test
        @DisplayName("添加边不会创建环返回false")
        void testWouldNotCreateCycle() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            // Adding A->C would not create a cycle
            assertThat(CycleDetectionUtil.wouldCreateCycle(graph, "A", "C")).isFalse();
        }

        @Test
        @DisplayName("自环返回true")
        void testSelfLoopWouldCreateCycle() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(CycleDetectionUtil.wouldCreateCycle(graph, "A", "A")).isTrue();
        }

        @Test
        @DisplayName("null参数返回false")
        void testNullParams() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(CycleDetectionUtil.wouldCreateCycle(null, "A", "B")).isFalse();
            assertThat(CycleDetectionUtil.wouldCreateCycle(graph, null, "B")).isFalse();
            assertThat(CycleDetectionUtil.wouldCreateCycle(graph, "A", null)).isFalse();
        }

        @Test
        @DisplayName("顶点不存在返回false")
        void testVertexNotFound() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            assertThat(CycleDetectionUtil.wouldCreateCycle(graph, "X", "Y")).isFalse();
        }
    }
}
