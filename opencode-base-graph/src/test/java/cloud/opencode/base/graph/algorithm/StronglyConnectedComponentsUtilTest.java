package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.DirectedGraph;
import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.UndirectedGraph;
import cloud.opencode.base.graph.exception.InvalidVertexException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * StronglyConnectedComponentsUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("StronglyConnectedComponentsUtil 测试")
class StronglyConnectedComponentsUtilTest {

    @Nested
    @DisplayName("find测试")
    class FindTests {

        @Test
        @DisplayName("单个顶点应返回1个SCC")
        void singleVertex() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addVertex("A");

            List<Set<String>> sccs = StronglyConnectedComponentsUtil.find(graph);

            assertThat(sccs).hasSize(1);
            assertThat(sccs.getFirst()).containsExactly("A");
        }

        @Test
        @DisplayName("线性链A→B→C应返回3个SCC")
        void linearChain() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            List<Set<String>> sccs = StronglyConnectedComponentsUtil.find(graph);

            assertThat(sccs).hasSize(3);
            // Each SCC should contain exactly one vertex
            for (Set<String> scc : sccs) {
                assertThat(scc).hasSize(1);
            }
            // All vertices should be covered
            Set<String> allVertices = Set.of("A", "B", "C");
            assertThat(sccs.stream().flatMap(Set::stream).toList())
                    .containsExactlyInAnyOrderElementsOf(allVertices);
        }

        @Test
        @DisplayName("简单环A→B→C→A应返回1个SCC")
        void simpleCycle() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            List<Set<String>> sccs = StronglyConnectedComponentsUtil.find(graph);

            assertThat(sccs).hasSize(1);
            assertThat(sccs.getFirst()).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("经典多SCC示例")
        void multipleSCCs() {
            // SCC1: A→B→C→A, SCC2: D→E→D, bridge: C→D
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");
            graph.addEdge("C", "D");
            graph.addEdge("D", "E");
            graph.addEdge("E", "D");

            List<Set<String>> sccs = StronglyConnectedComponentsUtil.find(graph);

            assertThat(sccs).hasSize(2);

            // Find each SCC by content
            Set<String> scc1 = sccs.stream()
                    .filter(s -> s.contains("A"))
                    .findFirst().orElseThrow();
            Set<String> scc2 = sccs.stream()
                    .filter(s -> s.contains("D"))
                    .findFirst().orElseThrow();

            assertThat(scc1).containsExactlyInAnyOrder("A", "B", "C");
            assertThat(scc2).containsExactlyInAnyOrder("D", "E");
        }

        @Test
        @DisplayName("空图应返回空列表")
        void emptyGraph() {
            Graph<String> graph = new DirectedGraph<>();

            List<Set<String>> sccs = StronglyConnectedComponentsUtil.find(graph);

            assertThat(sccs).isEmpty();
        }

        @Test
        @DisplayName("null图应返回空列表")
        void nullGraph() {
            List<Set<String>> sccs = StronglyConnectedComponentsUtil.find(null);

            assertThat(sccs).isEmpty();
        }
    }

    @Nested
    @DisplayName("count测试")
    class CountTests {

        @Test
        @DisplayName("计算SCC数量")
        void countSCCs() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");
            graph.addEdge("C", "D");
            graph.addEdge("D", "E");
            graph.addEdge("E", "D");

            assertThat(StronglyConnectedComponentsUtil.count(graph)).isEqualTo(2);
        }

        @Test
        @DisplayName("空图返回0")
        void emptyGraphCount() {
            assertThat(StronglyConnectedComponentsUtil.count(new DirectedGraph<>())).isZero();
        }
    }

    @Nested
    @DisplayName("componentOf测试")
    class ComponentOfTests {

        @Test
        @DisplayName("查找顶点所在的SCC")
        void findComponentOfVertex() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");
            graph.addEdge("C", "D");

            Set<String> component = StronglyConnectedComponentsUtil.componentOf(graph, "B");

            assertThat(component).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("null顶点应抛出InvalidVertexException")
        void nullVertexThrows() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addVertex("A");

            assertThatThrownBy(() -> StronglyConnectedComponentsUtil.componentOf(graph, null))
                    .isInstanceOf(InvalidVertexException.class);
        }

        @Test
        @DisplayName("不存在的顶点应返回空集合")
        void vertexNotFound() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addVertex("A");

            Set<String> component = StronglyConnectedComponentsUtil.componentOf(graph, "Z");

            assertThat(component).isEmpty();
        }
    }

    @Nested
    @DisplayName("isStronglyConnected测试")
    class IsStronglyConnectedTests {

        @Test
        @DisplayName("强连通图返回true")
        void stronglyConnected() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            assertThat(StronglyConnectedComponentsUtil.isStronglyConnected(graph)).isTrue();
        }

        @Test
        @DisplayName("非强连通图返回false")
        void notStronglyConnected() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(StronglyConnectedComponentsUtil.isStronglyConnected(graph)).isFalse();
        }

        @Test
        @DisplayName("单顶点图返回true")
        void singleVertex() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addVertex("A");

            assertThat(StronglyConnectedComponentsUtil.isStronglyConnected(graph)).isTrue();
        }

        @Test
        @DisplayName("空图返回true")
        void emptyGraph() {
            assertThat(StronglyConnectedComponentsUtil.isStronglyConnected(
                    new DirectedGraph<String>())).isTrue();
        }
    }

    @Nested
    @DisplayName("condensation测试")
    class CondensationTests {

        @Test
        @DisplayName("缩合图应为DAG")
        void condensationIsDAG() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");
            graph.addEdge("C", "D");
            graph.addEdge("D", "E");
            graph.addEdge("E", "D");

            Graph<Set<String>> dag = StronglyConnectedComponentsUtil.condensation(graph);

            // DAG should have 2 vertices (2 SCCs)
            assertThat(dag.vertexCount()).isEqualTo(2);

            // DAG should have 1 edge (from {A,B,C} to {D,E})
            assertThat(dag.edgeCount()).isEqualTo(1);

            // The DAG should be directed
            assertThat(dag.isDirected()).isTrue();
        }

        @Test
        @DisplayName("完全强连通图缩合为单个顶点")
        void fullyConnectedCondensation() {
            Graph<String> graph = new DirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "A");

            Graph<Set<String>> dag = StronglyConnectedComponentsUtil.condensation(graph);

            assertThat(dag.vertexCount()).isEqualTo(1);
            assertThat(dag.edgeCount()).isZero();
        }

        @Test
        @DisplayName("null图返回空图")
        void nullGraphCondensation() {
            Graph<Set<String>> dag = StronglyConnectedComponentsUtil.condensation(null);

            assertThat(dag.vertexCount()).isZero();
        }
    }

    @Nested
    @DisplayName("无向图回退测试")
    class UndirectedFallbackTests {

        @Test
        @DisplayName("无向图返回连通分量")
        void undirectedReturnsConnectedComponents() {
            Graph<String> graph = new UndirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addVertex("D");

            List<Set<String>> components = StronglyConnectedComponentsUtil.find(graph);

            assertThat(components).hasSize(2);
        }

        @Test
        @DisplayName("无向连通图返回1个分量")
        void undirectedConnectedGraph() {
            Graph<String> graph = new UndirectedGraph<>();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(StronglyConnectedComponentsUtil.isStronglyConnected(graph)).isTrue();
        }
    }
}
