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
 * ConnectedComponentsUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("ConnectedComponentsUtil 测试")
class ConnectedComponentsUtilTest {

    @Nested
    @DisplayName("find测试")
    class FindTests {

        @Test
        @DisplayName("找到单个连通分量")
        void testSingleComponent() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            List<Set<String>> components = ConnectedComponentsUtil.find(graph);

            assertThat(components).hasSize(1);
            assertThat(components.get(0)).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("找到多个连通分量")
        void testMultipleComponents() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("C", "D");
            graph.addVertex("E");

            List<Set<String>> components = ConnectedComponentsUtil.find(graph);

            assertThat(components).hasSize(3);
        }

        @Test
        @DisplayName("null图返回空列表")
        void testNullGraph() {
            List<Set<String>> components = ConnectedComponentsUtil.find(null);

            assertThat(components).isEmpty();
        }

        @Test
        @DisplayName("空图返回空列表")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();

            List<Set<String>> components = ConnectedComponentsUtil.find(graph);

            assertThat(components).isEmpty();
        }
    }

    @Nested
    @DisplayName("isConnected测试")
    class IsConnectedTests {

        @Test
        @DisplayName("连通的顶点返回true")
        void testConnectedVertices() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(ConnectedComponentsUtil.isConnected(graph, "A", "C")).isTrue();
        }

        @Test
        @DisplayName("不连通的顶点返回false")
        void testDisconnectedVertices() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addVertex("C");

            assertThat(ConnectedComponentsUtil.isConnected(graph, "A", "C")).isFalse();
        }

        @Test
        @DisplayName("相同顶点返回true")
        void testSameVertex() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            assertThat(ConnectedComponentsUtil.isConnected(graph, "A", "A")).isTrue();
        }

        @Test
        @DisplayName("null参数返回false")
        void testNullParams() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            assertThat(ConnectedComponentsUtil.isConnected(null, "A", "B")).isFalse();
            assertThat(ConnectedComponentsUtil.isConnected(graph, null, "B")).isFalse();
            assertThat(ConnectedComponentsUtil.isConnected(graph, "A", null)).isFalse();
        }

        @Test
        @DisplayName("顶点不存在返回false")
        void testVertexNotFound() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            assertThat(ConnectedComponentsUtil.isConnected(graph, "A", "X")).isFalse();
            assertThat(ConnectedComponentsUtil.isConnected(graph, "X", "A")).isFalse();
        }
    }

    @Nested
    @DisplayName("isFullyConnected测试")
    class IsFullyConnectedTests {

        @Test
        @DisplayName("完全连通图返回true")
        void testFullyConnected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            assertThat(ConnectedComponentsUtil.isFullyConnected(graph)).isTrue();
        }

        @Test
        @DisplayName("不连通图返回false")
        void testNotFullyConnected() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addVertex("C");

            assertThat(ConnectedComponentsUtil.isFullyConnected(graph)).isFalse();
        }

        @Test
        @DisplayName("空图返回true")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();

            assertThat(ConnectedComponentsUtil.isFullyConnected(graph)).isTrue();
        }

        @Test
        @DisplayName("null图返回true")
        void testNullGraph() {
            assertThat(ConnectedComponentsUtil.isFullyConnected(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("count测试")
    class CountTests {

        @Test
        @DisplayName("计算连通分量数量")
        void testCount() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("C", "D");
            graph.addVertex("E");

            assertThat(ConnectedComponentsUtil.count(graph)).isEqualTo(3);
        }

        @Test
        @DisplayName("空图返回0")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();

            assertThat(ConnectedComponentsUtil.count(graph)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getLargestComponent测试")
    class GetLargestComponentTests {

        @Test
        @DisplayName("获取最大连通分量")
        void testGetLargestComponent() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("D", "E");

            Set<String> largest = ConnectedComponentsUtil.getLargestComponent(graph);

            assertThat(largest).hasSize(3);
            assertThat(largest).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("空图返回空集")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();

            Set<String> largest = ConnectedComponentsUtil.getLargestComponent(graph);

            assertThat(largest).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSmallestComponent测试")
    class GetSmallestComponentTests {

        @Test
        @DisplayName("获取最小连通分量")
        void testGetSmallestComponent() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addVertex("D");

            Set<String> smallest = ConnectedComponentsUtil.getSmallestComponent(graph);

            assertThat(smallest).hasSize(1);
            assertThat(smallest).contains("D");
        }
    }

    @Nested
    @DisplayName("getComponentContaining测试")
    class GetComponentContainingTests {

        @Test
        @DisplayName("获取包含指定顶点的分量")
        void testGetComponentContaining() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addVertex("D");

            Set<String> component = ConnectedComponentsUtil.getComponentContaining(graph, "B");

            assertThat(component).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("顶点不存在返回空集")
        void testVertexNotFound() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");

            Set<String> component = ConnectedComponentsUtil.getComponentContaining(graph, "X");

            assertThat(component).isEmpty();
        }

        @Test
        @DisplayName("null参数返回空集")
        void testNullParams() {
            Graph<String> graph = OpenGraph.undirected();

            assertThat(ConnectedComponentsUtil.getComponentContaining(null, "A")).isEmpty();
            assertThat(ConnectedComponentsUtil.getComponentContaining(graph, null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("有向图连通性测试")
    class DirectedGraphTests {

        @Test
        @DisplayName("有向图弱连通")
        void testDirectedWeakConnectivity() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            // 弱连通：忽略边方向
            List<Set<String>> components = ConnectedComponentsUtil.find(graph);

            assertThat(components).hasSize(1);
            assertThat(components.get(0)).containsExactlyInAnyOrder("A", "B", "C");
        }
    }
}
