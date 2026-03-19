package cloud.opencode.base.collections.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Graph 接口测试类
 * 通过 MutableGraph 实现类测试 Graph 接口的所有方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("Graph 接口测试")
class GraphTest {

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("isDirected - 有向图")
        void testIsDirectedTrue() {
            Graph<String> graph = MutableGraph.directed();

            assertThat(graph.isDirected()).isTrue();
        }

        @Test
        @DisplayName("isDirected - 无向图")
        void testIsDirectedFalse() {
            Graph<String> graph = MutableGraph.undirected();

            assertThat(graph.isDirected()).isFalse();
        }

        @Test
        @DisplayName("allowsSelfLoops - 允许自环")
        void testAllowsSelfLoopsTrue() {
            MutableGraph<String> graph = MutableGraph.directedAllowingSelfLoops();

            assertThat(graph.allowsSelfLoops()).isTrue();
        }

        @Test
        @DisplayName("allowsSelfLoops - 不允许自环")
        void testAllowsSelfLoopsFalse() {
            Graph<String> graph = MutableGraph.directed();

            assertThat(graph.allowsSelfLoops()).isFalse();
        }

        @Test
        @DisplayName("nodes - 返回所有节点")
        void testNodes() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");

            Set<String> nodes = graph.nodes();

            assertThat(nodes).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("nodes - 空图")
        void testNodesEmpty() {
            Graph<String> graph = MutableGraph.directed();

            assertThat(graph.nodes()).isEmpty();
        }

        @Test
        @DisplayName("edges - 返回所有边")
        void testEdges() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.putEdge("A", "B");

            Set<Graph.EndpointPair<String>> edges = graph.edges();

            assertThat(edges).hasSize(1);
        }

        @Test
        @DisplayName("edges - 空图无边")
        void testEdgesEmpty() {
            Graph<String> graph = MutableGraph.directed();

            assertThat(graph.edges()).isEmpty();
        }
    }

    @Nested
    @DisplayName("节点操作测试")
    class NodeOperationTests {

        @Test
        @DisplayName("hasNode - 节点存在")
        void testHasNodeExists() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            assertThat(graph.hasNode("A")).isTrue();
        }

        @Test
        @DisplayName("hasNode - 节点不存在")
        void testHasNodeNotExists() {
            Graph<String> graph = MutableGraph.directed();

            assertThat(graph.hasNode("A")).isFalse();
        }

        @Test
        @DisplayName("successors - 有向图后继")
        void testSuccessorsDirected() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");

            Set<String> successors = graph.successors("A");

            assertThat(successors).containsExactlyInAnyOrder("B", "C");
        }

        @Test
        @DisplayName("successors - 无后继")
        void testSuccessorsEmpty() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            assertThat(graph.successors("A")).isEmpty();
        }

        @Test
        @DisplayName("predecessors - 有向图前驱")
        void testPredecessorsDirected() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");
            graph.putEdge("A", "C");
            graph.putEdge("B", "C");

            Set<String> predecessors = graph.predecessors("C");

            assertThat(predecessors).containsExactlyInAnyOrder("A", "B");
        }

        @Test
        @DisplayName("predecessors - 无前驱")
        void testPredecessorsEmpty() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            assertThat(graph.predecessors("A")).isEmpty();
        }

        @Test
        @DisplayName("adjacentNodes - 邻接节点")
        void testAdjacentNodes() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");
            graph.putEdge("A", "B");
            graph.putEdge("C", "A");

            Set<String> adjacent = graph.adjacentNodes("A");

            assertThat(adjacent).containsExactlyInAnyOrder("B", "C");
        }

        @Test
        @DisplayName("adjacentNodes - 无向图")
        void testAdjacentNodesUndirected() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.addNode("A");
            graph.addNode("B");
            graph.putEdge("A", "B");

            Set<String> adjacent = graph.adjacentNodes("A");

            assertThat(adjacent).contains("B");
        }

        @Test
        @DisplayName("degree - 节点度数")
        void testDegree() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");
            graph.putEdge("A", "B");
            graph.putEdge("C", "A");

            int degree = graph.degree("A");

            assertThat(degree).isEqualTo(2);
        }

        @Test
        @DisplayName("degree - 孤立节点")
        void testDegreeIsolated() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            assertThat(graph.degree("A")).isEqualTo(0);
        }

        @Test
        @DisplayName("inDegree - 入度")
        void testInDegree() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");
            graph.putEdge("A", "C");
            graph.putEdge("B", "C");

            int inDegree = graph.inDegree("C");

            assertThat(inDegree).isEqualTo(2);
        }

        @Test
        @DisplayName("outDegree - 出度")
        void testOutDegree() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");

            int outDegree = graph.outDegree("A");

            assertThat(outDegree).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("边操作测试")
    class EdgeOperationTests {

        @Test
        @DisplayName("hasEdge - 边存在")
        void testHasEdgeExists() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.putEdge("A", "B");

            assertThat(graph.hasEdge("A", "B")).isTrue();
        }

        @Test
        @DisplayName("hasEdge - 边不存在")
        void testHasEdgeNotExists() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");

            assertThat(graph.hasEdge("A", "B")).isFalse();
        }

        @Test
        @DisplayName("hasEdge - 有向图方向性")
        void testHasEdgeDirected() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.putEdge("A", "B");

            assertThat(graph.hasEdge("A", "B")).isTrue();
            assertThat(graph.hasEdge("B", "A")).isFalse();
        }

        @Test
        @DisplayName("hasEdge - 无向图双向")
        void testHasEdgeUndirected() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.addNode("A");
            graph.addNode("B");
            graph.putEdge("A", "B");

            assertThat(graph.hasEdge("A", "B")).isTrue();
            assertThat(graph.hasEdge("B", "A")).isTrue();
        }

        @Test
        @DisplayName("incidentEdges - 入射边")
        void testIncidentEdges() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");

            Set<Graph.EndpointPair<String>> edges = graph.incidentEdges("A");

            assertThat(edges).hasSize(2);
        }

        @Test
        @DisplayName("incidentEdges - 孤立节点")
        void testIncidentEdgesIsolated() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            assertThat(graph.incidentEdges("A")).isEmpty();
        }
    }

    @Nested
    @DisplayName("计数方法测试")
    class CountMethodTests {

        @Test
        @DisplayName("nodeCount - 节点数量")
        void testNodeCount() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");

            assertThat(graph.nodeCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("nodeCount - 空图")
        void testNodeCountEmpty() {
            Graph<String> graph = MutableGraph.directed();

            assertThat(graph.nodeCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("edgeCount - 边数量")
        void testEdgeCount() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");

            assertThat(graph.edgeCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("edgeCount - 无边图")
        void testEdgeCountEmpty() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            assertThat(graph.edgeCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("EndpointPair 测试")
    class EndpointPairTests {

        @Test
        @DisplayName("source - 获取源节点")
        void testSource() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.putEdge("A", "B");

            Graph.EndpointPair<String> edge = graph.edges().iterator().next();

            assertThat(edge.source()).isEqualTo("A");
        }

        @Test
        @DisplayName("target - 获取目标节点")
        void testTarget() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.putEdge("A", "B");

            Graph.EndpointPair<String> edge = graph.edges().iterator().next();

            assertThat(edge.target()).isEqualTo("B");
        }

        @Test
        @DisplayName("isOrdered - 有向边是有序的")
        void testIsOrderedDirected() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");
            graph.putEdge("A", "B");

            Graph.EndpointPair<String> edge = graph.edges().iterator().next();

            assertThat(edge.isOrdered()).isTrue();
        }

        @Test
        @DisplayName("isOrdered - 无向边是无序的")
        void testIsOrderedUndirected() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.addNode("A");
            graph.addNode("B");
            graph.putEdge("A", "B");

            Graph.EndpointPair<String> edge = graph.edges().iterator().next();

            assertThat(edge.isOrdered()).isFalse();
        }
    }

    @Nested
    @DisplayName("复杂场景测试")
    class ComplexScenarioTests {

        @Test
        @DisplayName("链式图")
        void testChainGraph() {
            MutableGraph<Integer> graph = MutableGraph.directed();
            for (int i = 1; i <= 5; i++) {
                graph.addNode(i);
            }
            for (int i = 1; i < 5; i++) {
                graph.putEdge(i, i + 1);
            }

            assertThat(graph.nodeCount()).isEqualTo(5);
            assertThat(graph.edgeCount()).isEqualTo(4);
            assertThat(graph.successors(1)).containsExactly(2);
            assertThat(graph.predecessors(5)).containsExactly(4);
        }

        @Test
        @DisplayName("星形图")
        void testStarGraph() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.addNode("center");
            for (int i = 1; i <= 5; i++) {
                graph.addNode("leaf" + i);
                graph.putEdge("center", "leaf" + i);
            }

            assertThat(graph.nodeCount()).isEqualTo(6);
            assertThat(graph.degree("center")).isEqualTo(5);
            assertThat(graph.adjacentNodes("center")).hasSize(5);
        }

        @Test
        @DisplayName("环形图")
        void testCycleGraph() {
            MutableGraph<Integer> graph = MutableGraph.directed();
            int n = 4;
            for (int i = 0; i < n; i++) {
                graph.addNode(i);
            }
            for (int i = 0; i < n; i++) {
                graph.putEdge(i, (i + 1) % n);
            }

            assertThat(graph.nodeCount()).isEqualTo(n);
            assertThat(graph.edgeCount()).isEqualTo(n);
            for (int i = 0; i < n; i++) {
                assertThat(graph.inDegree(i)).isEqualTo(1);
                assertThat(graph.outDegree(i)).isEqualTo(1);
            }
        }
    }
}
