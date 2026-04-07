package cloud.opencode.base.collections.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ValueGraph and MutableValueGraph tests.
 * ValueGraph 和 MutableValueGraph 测试类。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("ValueGraph 测试")
class ValueGraphTest {

    // ==================== 有向图测试 | Directed Graph Tests ====================

    @Nested
    @DisplayName("有向值图测试 | Directed ValueGraph Tests")
    class DirectedValueGraphTests {

        @Test
        @DisplayName("addNode - 添加节点")
        void testAddNode() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();

            assertThat(graph.addNode("A")).isTrue();
            assertThat(graph.addNode("B")).isTrue();
            assertThat(graph.addNode("A")).isFalse(); // duplicate

            assertThat(graph.nodes()).containsExactlyInAnyOrder("A", "B");
            assertThat(graph.nodeCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("addNode - null 节点抛异常")
        void testAddNodeNull() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();

            assertThatNullPointerException()
                    .isThrownBy(() -> graph.addNode(null));
        }

        @Test
        @DisplayName("putEdgeValue - 添加带权边")
        void testPutEdgeValue() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();

            Double putResult = graph.putEdgeValue("A", "B", 3.5);
            assertThat(putResult).isNull(); // first time, no previous value

            assertThat(graph.nodes()).containsExactlyInAnyOrder("A", "B");
            assertThat(graph.hasEdgeConnecting("A", "B")).isTrue();
            assertThat(graph.hasEdgeConnecting("B", "A")).isFalse(); // directed
        }

        @Test
        @DisplayName("putEdgeValue - 覆盖已有边值")
        void testPutEdgeValueOverwrite() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();

            graph.putEdgeValue("A", "B", 1.0);
            Double previous = graph.putEdgeValue("A", "B", 2.0);

            assertThat(previous).isEqualTo(1.0);
            assertThat(graph.edgeValue("A", "B")).hasValue(2.0);
        }

        @Test
        @DisplayName("edgeValue - 查询边值")
        void testEdgeValue() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 5.0);
            graph.putEdgeValue("B", "C", 3.0);

            assertThat(graph.edgeValue("A", "B")).hasValue(5.0);
            assertThat(graph.edgeValue("B", "C")).hasValue(3.0);
            assertThat(graph.edgeValue("A", "C")).isEmpty();
        }

        @Test
        @DisplayName("edgeValueOrDefault - 默认值查询")
        void testEdgeValueOrDefault() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 5.0);
            graph.addNode("C");

            assertThat(graph.edgeValueOrDefault("A", "B", -1.0)).isEqualTo(5.0);
            assertThat(graph.edgeValueOrDefault("A", "C", -1.0)).isEqualTo(-1.0);
        }

        @Test
        @DisplayName("removeEdge - 移除边")
        void testRemoveEdge() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.putEdgeValue("B", "C", 2.0);

            Double removed = graph.removeEdge("A", "B");
            assertThat(removed).isEqualTo(1.0);
            assertThat(graph.hasEdgeConnecting("A", "B")).isFalse();

            // Non-existent edge
            assertThat(graph.removeEdge("A", "C")).isNull();
        }

        @Test
        @DisplayName("removeNode - 移除节点及关联边")
        void testRemoveNode() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.putEdgeValue("B", "C", 2.0);
            graph.putEdgeValue("C", "A", 3.0);

            assertThat(graph.removeNode("B")).isTrue();
            assertThat(graph.nodes()).containsExactlyInAnyOrder("A", "C");
            assertThat(graph.hasEdgeConnecting("A", "B")).isFalse();
            assertThat(graph.hasEdgeConnecting("B", "C")).isFalse();
            assertThat(graph.hasEdgeConnecting("C", "A")).isTrue();

            // Remove non-existent node
            assertThat(graph.removeNode("X")).isFalse();
        }

        @Test
        @DisplayName("degree - 度数")
        void testDegree() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.putEdgeValue("C", "B", 2.0);
            graph.putEdgeValue("B", "D", 3.0);

            // B has 2 predecessors (A, C) and 1 successor (D)
            assertThat(graph.degree("B")).isEqualTo(3);
            assertThat(graph.degree("A")).isEqualTo(1); // 0 predecessors + 1 successor
        }

        @Test
        @DisplayName("successors and predecessors - 后继和前驱")
        void testSuccessorsAndPredecessors() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.putEdgeValue("A", "C", 2.0);
            graph.putEdgeValue("D", "A", 3.0);

            assertThat(graph.successors("A")).containsExactlyInAnyOrder("B", "C");
            assertThat(graph.predecessors("A")).containsExactly("D");
            assertThat(graph.adjacentNodes("A")).containsExactlyInAnyOrder("B", "C", "D");
        }

        @Test
        @DisplayName("edges - 边集合")
        void testEdges() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.putEdgeValue("B", "C", 2.0);

            Set<ValueGraph.EndpointPair<String>> edges = graph.edges();
            assertThat(edges).hasSize(2);
            assertThat(edges).contains(
                    new ValueGraph.EndpointPair<>("A", "B"),
                    new ValueGraph.EndpointPair<>("B", "C")
            );
        }

        @Test
        @DisplayName("isDirected - 有向标识")
        void testIsDirected() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            assertThat(graph.isDirected()).isTrue();
        }

        @Test
        @DisplayName("edgeValue - 不存在的节点抛异常")
        void testEdgeValueNonExistentNode() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.addNode("A");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> graph.edgeValue("X", "A"));
        }
    }

    // ==================== 无向图测试 | Undirected Graph Tests ====================

    @Nested
    @DisplayName("无向值图测试 | Undirected ValueGraph Tests")
    class UndirectedValueGraphTests {

        @Test
        @DisplayName("putEdgeValue - 无向边双向可查")
        void testPutEdgeValueBidirectional() {
            MutableValueGraph<String, Integer> graph = MutableValueGraph.undirected();

            graph.putEdgeValue("A", "B", 10);

            assertThat(graph.hasEdgeConnecting("A", "B")).isTrue();
            assertThat(graph.hasEdgeConnecting("B", "A")).isTrue();
            assertThat(graph.edgeValue("A", "B")).hasValue(10);
            assertThat(graph.edgeValue("B", "A")).hasValue(10);
        }

        @Test
        @DisplayName("removeEdge - 无向边双向删除")
        void testRemoveEdgeBidirectional() {
            MutableValueGraph<String, Integer> graph = MutableValueGraph.undirected();
            graph.putEdgeValue("A", "B", 10);

            Integer removed = graph.removeEdge("A", "B");
            assertThat(removed).isEqualTo(10);
            assertThat(graph.hasEdgeConnecting("A", "B")).isFalse();
            assertThat(graph.hasEdgeConnecting("B", "A")).isFalse();
        }

        @Test
        @DisplayName("removeEdge - 无向边反向删除也生效")
        void testRemoveEdgeReverse() {
            MutableValueGraph<String, Integer> graph = MutableValueGraph.undirected();
            graph.putEdgeValue("A", "B", 10);

            Integer removed = graph.removeEdge("B", "A");
            assertThat(removed).isEqualTo(10);
            assertThat(graph.hasEdgeConnecting("A", "B")).isFalse();
            assertThat(graph.hasEdgeConnecting("B", "A")).isFalse();
        }

        @Test
        @DisplayName("isDirected - 无向标识")
        void testIsUndirected() {
            MutableValueGraph<String, Integer> graph = MutableValueGraph.undirected();
            assertThat(graph.isDirected()).isFalse();
        }

        @Test
        @DisplayName("degree - 无向图度数")
        void testDegreeUndirected() {
            MutableValueGraph<String, Integer> graph = MutableValueGraph.undirected();
            graph.putEdgeValue("A", "B", 1);
            graph.putEdgeValue("A", "C", 2);
            graph.putEdgeValue("A", "D", 3);

            assertThat(graph.degree("A")).isEqualTo(3);
            assertThat(graph.degree("B")).isEqualTo(1);
        }

        @Test
        @DisplayName("successors - 无向图后继等于邻居")
        void testSuccessorsUndirected() {
            MutableValueGraph<String, Integer> graph = MutableValueGraph.undirected();
            graph.putEdgeValue("A", "B", 1);
            graph.putEdgeValue("C", "A", 2);

            assertThat(graph.successors("A")).containsExactlyInAnyOrder("B", "C");
            assertThat(graph.predecessors("A")).containsExactlyInAnyOrder("B", "C");
        }

        @Test
        @DisplayName("removeNode - 无向图移除节点")
        void testRemoveNodeUndirected() {
            MutableValueGraph<String, Integer> graph = MutableValueGraph.undirected();
            graph.putEdgeValue("A", "B", 1);
            graph.putEdgeValue("B", "C", 2);

            graph.removeNode("B");
            assertThat(graph.nodes()).containsExactlyInAnyOrder("A", "C");
            assertThat(graph.hasEdgeConnecting("A", "B")).isFalse();
            assertThat(graph.hasEdgeConnecting("B", "C")).isFalse();
            assertThat(graph.edgeCount()).isEqualTo(0);
        }
    }

    // ==================== Dijkstra 测试 | Dijkstra Tests ====================

    @Nested
    @DisplayName("Dijkstra 最短路径测试 | Dijkstra Shortest Path Tests")
    class DijkstraTests {

        @Test
        @DisplayName("dijkstra - 简单图最短距离")
        void testDijkstraSimple() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.putEdgeValue("B", "C", 2.0);
            graph.putEdgeValue("A", "C", 5.0);

            Map<String, Double> distances = GraphTraversalUtil.dijkstra(graph, "A");

            assertThat(distances).containsEntry("A", 0.0);
            assertThat(distances).containsEntry("B", 1.0);
            assertThat(distances).containsEntry("C", 3.0); // A->B->C = 1+2 = 3 < 5
        }

        @Test
        @DisplayName("dijkstra - 不可达节点不在结果中")
        void testDijkstraUnreachable() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.addNode("C"); // isolated node

            Map<String, Double> distances = GraphTraversalUtil.dijkstra(graph, "A");

            assertThat(distances).containsKeys("A", "B");
            assertThat(distances).doesNotContainKey("C");
        }

        @Test
        @DisplayName("dijkstra - 源节点不存在返回空")
        void testDijkstraNonExistentSource() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);

            Map<String, Double> distances = GraphTraversalUtil.dijkstra(graph, "X");

            assertThat(distances).isEmpty();
        }

        @Test
        @DisplayName("dijkstra - 负权边抛异常")
        void testDijkstraNegativeWeight() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", -1.0);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GraphTraversalUtil.dijkstra(graph, "A"))
                    .withMessageContaining("Negative edge weight");
        }

        @Test
        @DisplayName("dijkstra - 多路径选择最短")
        void testDijkstraMultiplePaths() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.putEdgeValue("A", "C", 4.0);
            graph.putEdgeValue("B", "C", 1.0);
            graph.putEdgeValue("B", "D", 5.0);
            graph.putEdgeValue("C", "D", 1.0);

            Map<String, Double> distances = GraphTraversalUtil.dijkstra(graph, "A");

            assertThat(distances.get("D")).isEqualTo(3.0); // A->B->C->D = 1+1+1 = 3
        }

        @Test
        @DisplayName("dijkstra - Integer 边权")
        void testDijkstraIntegerWeights() {
            MutableValueGraph<String, Integer> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 2);
            graph.putEdgeValue("B", "C", 3);

            Map<String, Double> distances = GraphTraversalUtil.dijkstra(graph, "A");

            assertThat(distances.get("C")).isEqualTo(5.0);
        }
    }

    // ==================== shortestWeightedPath 测试 ====================

    @Nested
    @DisplayName("shortestWeightedPath 测试 | Shortest Weighted Path Tests")
    class ShortestWeightedPathTests {

        @Test
        @DisplayName("shortestWeightedPath - 找到最短路径")
        void testShortestWeightedPath() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.putEdgeValue("B", "C", 2.0);
            graph.putEdgeValue("A", "C", 5.0);

            List<String> path = GraphTraversalUtil.shortestWeightedPath(graph, "A", "C");

            assertThat(path).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("shortestWeightedPath - 不可达返回空列表")
        void testShortestWeightedPathUnreachable() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.addNode("C"); // isolated

            List<String> path = GraphTraversalUtil.shortestWeightedPath(graph, "A", "C");

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("shortestWeightedPath - 源等于目标")
        void testShortestWeightedPathSameNode() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.addNode("A");

            List<String> path = GraphTraversalUtil.shortestWeightedPath(graph, "A", "A");

            assertThat(path).containsExactly("A");
        }

        @Test
        @DisplayName("shortestWeightedPath - 源不存在返回空列表")
        void testShortestWeightedPathNonExistentSource() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.addNode("A");

            List<String> path = GraphTraversalUtil.shortestWeightedPath(graph, "X", "A");

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("shortestWeightedPath - 负权边抛异常")
        void testShortestWeightedPathNegativeWeight() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", -1.0);
            graph.putEdgeValue("B", "C", 2.0);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> GraphTraversalUtil.shortestWeightedPath(graph, "A", "C"))
                    .withMessageContaining("Negative edge weight");
        }

        @Test
        @DisplayName("shortestWeightedPath - 多跳最短路径")
        void testShortestWeightedPathMultiHop() {
            MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
            graph.putEdgeValue("A", "B", 1.0);
            graph.putEdgeValue("A", "C", 4.0);
            graph.putEdgeValue("B", "C", 1.0);
            graph.putEdgeValue("B", "D", 5.0);
            graph.putEdgeValue("C", "D", 1.0);

            List<String> path = GraphTraversalUtil.shortestWeightedPath(graph, "A", "D");

            assertThat(path).containsExactly("A", "B", "C", "D");
        }
    }
}
