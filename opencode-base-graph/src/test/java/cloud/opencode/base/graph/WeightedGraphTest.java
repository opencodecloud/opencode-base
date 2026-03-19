package cloud.opencode.base.graph;

import cloud.opencode.base.graph.node.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * WeightedGraph 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("WeightedGraph 测试")
class WeightedGraphTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("创建有向加权图")
        void testDirectedFactory() {
            WeightedGraph<String> graph = WeightedGraph.directed();

            assertThat(graph).isNotNull();
            assertThat(graph.isDirected()).isTrue();
        }

        @Test
        @DisplayName("创建无向加权图")
        void testUndirectedFactory() {
            WeightedGraph<String> graph = WeightedGraph.undirected();

            assertThat(graph).isNotNull();
            assertThat(graph.isDirected()).isFalse();
        }
    }

    @Nested
    @DisplayName("setWeight测试 - 有向图")
    class SetWeightDirectedTests {

        @Test
        @DisplayName("更新已存在边的权重")
        void testSetWeightExistingEdge() {
            WeightedGraph<String> graph = WeightedGraph.directed();
            graph.addEdge("A", "B", 5.0);

            boolean result = graph.setWeight("A", "B", 10.0);

            assertThat(result).isTrue();
            assertThat(graph.getWeight("A", "B")).isEqualTo(10.0);
        }

        @Test
        @DisplayName("更新不存在边的权重返回false")
        void testSetWeightNonExistentEdge() {
            WeightedGraph<String> graph = WeightedGraph.directed();

            boolean result = graph.setWeight("A", "B", 10.0);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("setWeight测试 - 无向图")
    class SetWeightUndirectedTests {

        @Test
        @DisplayName("更新已存在边的权重（双向）")
        void testSetWeightBidirectional() {
            WeightedGraph<String> graph = WeightedGraph.undirected();
            graph.addEdge("A", "B", 5.0);

            boolean result = graph.setWeight("A", "B", 10.0);

            assertThat(result).isTrue();
            assertThat(graph.getWeight("A", "B")).isEqualTo(10.0);
            assertThat(graph.getWeight("B", "A")).isEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("totalWeight测试")
    class TotalWeightTests {

        @Test
        @DisplayName("计算有向图总权重")
        void testTotalWeightDirected() {
            WeightedGraph<String> graph = WeightedGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 3.0);

            assertThat(graph.totalWeight()).isEqualTo(6.0);
        }

        @Test
        @DisplayName("计算无向图总权重")
        void testTotalWeightUndirected() {
            WeightedGraph<String> graph = WeightedGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 2.0);

            // 无向图的edges()返回的是去重后的边集合
            assertThat(graph.totalWeight()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("空图总权重为0")
        void testTotalWeightEmpty() {
            WeightedGraph<String> graph = WeightedGraph.directed();

            assertThat(graph.totalWeight()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("minWeight测试")
    class MinWeightTests {

        @Test
        @DisplayName("获取最小权重")
        void testMinWeight() {
            WeightedGraph<String> graph = WeightedGraph.directed();
            graph.addEdge("A", "B", 5.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 8.0);

            assertThat(graph.minWeight()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("空图返回MAX_VALUE")
        void testMinWeightEmpty() {
            WeightedGraph<String> graph = WeightedGraph.directed();

            assertThat(graph.minWeight()).isEqualTo(Double.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("maxWeight测试")
    class MaxWeightTests {

        @Test
        @DisplayName("获取最大权重")
        void testMaxWeight() {
            WeightedGraph<String> graph = WeightedGraph.directed();
            graph.addEdge("A", "B", 5.0);
            graph.addEdge("B", "C", 2.0);
            graph.addEdge("A", "C", 8.0);

            assertThat(graph.maxWeight()).isEqualTo(8.0);
        }

        @Test
        @DisplayName("空图返回MIN_VALUE")
        void testMaxWeightEmpty() {
            WeightedGraph<String> graph = WeightedGraph.directed();

            assertThat(graph.maxWeight()).isEqualTo(Double.MIN_VALUE);
        }
    }

    @Nested
    @DisplayName("edgesInWeightRange测试")
    class EdgesInWeightRangeTests {

        @Test
        @DisplayName("获取权重范围内的边")
        void testEdgesInWeightRange() {
            WeightedGraph<String> graph = WeightedGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 3.0);
            graph.addEdge("C", "D", 5.0);
            graph.addEdge("D", "E", 7.0);

            Set<Edge<String>> edges = graph.edgesInWeightRange(2.0, 6.0);

            assertThat(edges).hasSize(2);
        }

        @Test
        @DisplayName("边界值包含")
        void testEdgesInWeightRangeInclusive() {
            WeightedGraph<String> graph = WeightedGraph.directed();
            graph.addEdge("A", "B", 3.0);

            Set<Edge<String>> edges = graph.edgesInWeightRange(3.0, 3.0);

            assertThat(edges).hasSize(1);
        }

        @Test
        @DisplayName("无符合条件的边返回空集")
        void testEdgesInWeightRangeEmpty() {
            WeightedGraph<String> graph = WeightedGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 10.0);

            Set<Edge<String>> edges = graph.edgesInWeightRange(3.0, 5.0);

            assertThat(edges).isEmpty();
        }
    }

    @Nested
    @DisplayName("继承Graph接口测试")
    class InheritanceTests {

        @Test
        @DisplayName("实现Graph接口方法")
        void testGraphInterfaceMethods() {
            WeightedGraph<String> graph = WeightedGraph.directed();
            graph.addEdge("A", "B", 5.0);

            assertThat(graph.containsVertex("A")).isTrue();
            assertThat(graph.containsEdge("A", "B")).isTrue();
            assertThat(graph.vertexCount()).isEqualTo(2);
            assertThat(graph.edgeCount()).isEqualTo(1);
        }
    }
}
