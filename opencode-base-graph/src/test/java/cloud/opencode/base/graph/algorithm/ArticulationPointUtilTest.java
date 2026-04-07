package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * ArticulationPointUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("ArticulationPointUtil 测试")
class ArticulationPointUtilTest {

    @Nested
    @DisplayName("findArticulationPoints 测试")
    class FindArticulationPointsTests {

        @Test
        @DisplayName("简单桥: A-B-C, B是割点")
        void testSimpleBridge() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Set<String> points = ArticulationPointUtil.findArticulationPoints(graph);

            assertThat(points).containsExactly("B");
        }

        @Test
        @DisplayName("完全图K4无割点")
        void testCompleteGraphK4() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("A", "D");
            graph.addEdge("B", "C");
            graph.addEdge("B", "D");
            graph.addEdge("C", "D");

            Set<String> points = ArticulationPointUtil.findArticulationPoints(graph);

            assertThat(points).isEmpty();
        }

        @Test
        @DisplayName("三角形+悬挂边: A-B, B-C, C-A, C-D, C是割点")
        void testTriangleWithPendant() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");
            graph.addEdge("C", "D");

            Set<String> points = ArticulationPointUtil.findArticulationPoints(graph);

            assertThat(points).containsExactly("C");
        }

        @Test
        @DisplayName("多连通分量")
        void testMultipleComponents() {
            Graph<String> graph = OpenGraph.undirected();
            // Component 1: A-B-C (B is articulation)
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            // Component 2: D-E (no articulation)
            graph.addEdge("D", "E");

            Set<String> points = ArticulationPointUtil.findArticulationPoints(graph);

            assertThat(points).containsExactly("B");
        }

        @Test
        @DisplayName("单个顶点无割点")
        void testSingleVertex() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            Set<String> points = ArticulationPointUtil.findArticulationPoints(graph);

            assertThat(points).isEmpty();
        }

        @Test
        @DisplayName("两个顶点无割点")
        void testTwoVertices() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            Set<String> points = ArticulationPointUtil.findArticulationPoints(graph);

            assertThat(points).isEmpty();
        }

        @Test
        @DisplayName("null图返回空集")
        void testNullGraph() {
            Set<String> points = ArticulationPointUtil.findArticulationPoints(null);

            assertThat(points).isEmpty();
        }

        @Test
        @DisplayName("空图返回空集")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();

            Set<String> points = ArticulationPointUtil.findArticulationPoints(graph);

            assertThat(points).isEmpty();
        }

        @Test
        @DisplayName("有向图按无向图处理")
        void testDirectedGraphTreatedAsUndirected() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Set<String> points = ArticulationPointUtil.findArticulationPoints(graph);

            assertThat(points).containsExactly("B");
        }

        @Test
        @DisplayName("链式图: A-B-C-D-E, 中间节点都是割点")
        void testChainGraph() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");
            graph.addEdge("D", "E");

            Set<String> points = ArticulationPointUtil.findArticulationPoints(graph);

            assertThat(points).containsExactlyInAnyOrder("B", "C", "D");
        }
    }

    @Nested
    @DisplayName("findBridges 测试")
    class FindBridgesTests {

        @Test
        @DisplayName("简单桥: A-B-C, 两条边都是桥")
        void testSimpleBridge() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Set<Edge<String>> bridges = ArticulationPointUtil.findBridges(graph);

            assertThat(bridges).hasSize(2);
        }

        @Test
        @DisplayName("三角形无桥")
        void testTriangleNoBridges() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            Set<Edge<String>> bridges = ArticulationPointUtil.findBridges(graph);

            assertThat(bridges).isEmpty();
        }

        @Test
        @DisplayName("三角形+悬挂边: C-D是桥")
        void testTriangleWithPendantBridge() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");
            graph.addEdge("C", "D");

            Set<Edge<String>> bridges = ArticulationPointUtil.findBridges(graph);

            assertThat(bridges).hasSize(1);
            Edge<String> bridge = bridges.iterator().next();
            assertThat(Set.of(bridge.from(), bridge.to())).containsExactlyInAnyOrder("C", "D");
        }

        @Test
        @DisplayName("null图返回空集")
        void testNullGraph() {
            Set<Edge<String>> bridges = ArticulationPointUtil.findBridges(null);

            assertThat(bridges).isEmpty();
        }

        @Test
        @DisplayName("有向图按无向图处理")
        void testDirectedGraph() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Set<Edge<String>> bridges = ArticulationPointUtil.findBridges(graph);

            assertThat(bridges).hasSize(2);
        }
    }

    @Nested
    @DisplayName("isBiconnected 测试")
    class IsBiconnectedTests {

        @Test
        @DisplayName("完全图K4是双连通的")
        void testK4Biconnected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("A", "D");
            graph.addEdge("B", "C");
            graph.addEdge("B", "D");
            graph.addEdge("C", "D");

            assertThat(ArticulationPointUtil.isBiconnected(graph)).isTrue();
        }

        @Test
        @DisplayName("三角形是双连通的")
        void testTriangleBiconnected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            assertThat(ArticulationPointUtil.isBiconnected(graph)).isTrue();
        }

        @Test
        @DisplayName("链式图不是双连通的")
        void testChainNotBiconnected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(ArticulationPointUtil.isBiconnected(graph)).isFalse();
        }

        @Test
        @DisplayName("单顶点不是双连通的")
        void testSingleVertexNotBiconnected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            assertThat(ArticulationPointUtil.isBiconnected(graph)).isFalse();
        }

        @Test
        @DisplayName("根节点只有1个子节点时不是割点")
        void testRootWithOneChildIsNotArticulationPoint() {
            // Chain: A-B-C. B is the articulation point, but A (root in DFS) has only one child
            // and should NOT be identified as an articulation point.
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Set<String> points = ArticulationPointUtil.findArticulationPoints(graph);

            // A is the leaf/root with one DFS child — not an articulation point
            assertThat(points).doesNotContain("A");
            assertThat(points).contains("B");
        }

        @Test
        @DisplayName("null图不是双连通的")
        void testNullNotBiconnected() {
            assertThat(ArticulationPointUtil.isBiconnected(null)).isFalse();
        }

        @Test
        @DisplayName("非连通图不是双连通的")
        void testDisconnectedNotBiconnected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "A");
            graph.addVertex("C");

            assertThat(ArticulationPointUtil.isBiconnected(graph)).isFalse();
        }
    }
}
