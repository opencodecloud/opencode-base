package cloud.opencode.base.collections.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MutableGraph 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("MutableGraph 测试")
class MutableGraphTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("directed - 创建有向图")
        void testDirected() {
            MutableGraph<String> graph = MutableGraph.directed();

            assertThat(graph.isDirected()).isTrue();
            assertThat(graph.allowsSelfLoops()).isFalse();
            assertThat(graph.nodes()).isEmpty();
        }

        @Test
        @DisplayName("undirected - 创建无向图")
        void testUndirected() {
            MutableGraph<String> graph = MutableGraph.undirected();

            assertThat(graph.isDirected()).isFalse();
            assertThat(graph.allowsSelfLoops()).isFalse();
        }

        @Test
        @DisplayName("directedAllowingSelfLoops - 允许自环的有向图")
        void testDirectedAllowingSelfLoops() {
            MutableGraph<String> graph = MutableGraph.directedAllowingSelfLoops();

            assertThat(graph.isDirected()).isTrue();
            assertThat(graph.allowsSelfLoops()).isTrue();
        }

        @Test
        @DisplayName("undirectedAllowingSelfLoops - 允许自环的无向图")
        void testUndirectedAllowingSelfLoops() {
            MutableGraph<String> graph = MutableGraph.undirectedAllowingSelfLoops();

            assertThat(graph.isDirected()).isFalse();
            assertThat(graph.allowsSelfLoops()).isTrue();
        }

        @Test
        @DisplayName("create - 自定义配置")
        void testCreate() {
            MutableGraph<String> graph = MutableGraph.create(true, true);

            assertThat(graph.isDirected()).isTrue();
            assertThat(graph.allowsSelfLoops()).isTrue();
        }
    }

    @Nested
    @DisplayName("节点操作测试")
    class NodeOperationTests {

        @Test
        @DisplayName("addNode - 添加节点")
        void testAddNode() {
            MutableGraph<String> graph = MutableGraph.directed();

            boolean added = graph.addNode("A");

            assertThat(added).isTrue();
            assertThat(graph.hasNode("A")).isTrue();
            assertThat(graph.nodeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("addNode - 添加重复节点")
        void testAddDuplicateNode() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            boolean added = graph.addNode("A");

            assertThat(added).isFalse();
            assertThat(graph.nodeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("addNode - null 节点抛异常")
        void testAddNullNode() {
            MutableGraph<String> graph = MutableGraph.directed();

            assertThatThrownBy(() -> graph.addNode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("removeNode - 删除节点")
        void testRemoveNode() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");

            boolean removed = graph.removeNode("A");

            assertThat(removed).isTrue();
            assertThat(graph.hasNode("A")).isFalse();
        }

        @Test
        @DisplayName("removeNode - 删除带边的节点")
        void testRemoveNodeWithEdges() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("C", "A");

            graph.removeNode("A");

            assertThat(graph.hasNode("A")).isFalse();
            assertThat(graph.hasEdge("A", "B")).isFalse();
            assertThat(graph.hasEdge("C", "A")).isFalse();
        }

        @Test
        @DisplayName("removeNode - 删除不存在的节点")
        void testRemoveNonexistentNode() {
            MutableGraph<String> graph = MutableGraph.directed();

            boolean removed = graph.removeNode("A");

            assertThat(removed).isFalse();
        }

        @Test
        @DisplayName("nodes - 获取所有节点")
        void testNodes() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");

            Set<String> nodes = graph.nodes();

            assertThat(nodes).containsExactlyInAnyOrder("A", "B");
        }
    }

    @Nested
    @DisplayName("边操作测试")
    class EdgeOperationTests {

        @Test
        @DisplayName("putEdge - 添加边")
        void testPutEdge() {
            MutableGraph<String> graph = MutableGraph.directed();

            boolean added = graph.putEdge("A", "B");

            assertThat(added).isTrue();
            assertThat(graph.hasEdge("A", "B")).isTrue();
            assertThat(graph.hasNode("A")).isTrue();
            assertThat(graph.hasNode("B")).isTrue();
        }

        @Test
        @DisplayName("putEdge - 添加重复边")
        void testPutDuplicateEdge() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");

            boolean added = graph.putEdge("A", "B");

            assertThat(added).isFalse();
        }

        @Test
        @DisplayName("putEdge - 有向图边方向")
        void testDirectedEdgeDirection() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");

            assertThat(graph.hasEdge("A", "B")).isTrue();
            assertThat(graph.hasEdge("B", "A")).isFalse();
        }

        @Test
        @DisplayName("putEdge - 无向图双向边")
        void testUndirectedEdge() {
            MutableGraph<String> graph = MutableGraph.undirected();
            graph.putEdge("A", "B");

            assertThat(graph.hasEdge("A", "B")).isTrue();
            assertThat(graph.hasEdge("B", "A")).isTrue();
        }

        @Test
        @DisplayName("putEdge - 不允许自环时抛异常")
        void testSelfLoopNotAllowed() {
            MutableGraph<String> graph = MutableGraph.directed();

            assertThatThrownBy(() -> graph.putEdge("A", "A"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("putEdge - 允许自环")
        void testSelfLoopAllowed() {
            MutableGraph<String> graph = MutableGraph.directedAllowingSelfLoops();

            graph.putEdge("A", "A");

            assertThat(graph.hasEdge("A", "A")).isTrue();
        }

        @Test
        @DisplayName("removeEdge - 删除边")
        void testRemoveEdge() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");

            boolean removed = graph.removeEdge("A", "B");

            assertThat(removed).isTrue();
            assertThat(graph.hasEdge("A", "B")).isFalse();
            assertThat(graph.hasNode("A")).isTrue();
            assertThat(graph.hasNode("B")).isTrue();
        }

        @Test
        @DisplayName("removeEdge - 删除不存在的边")
        void testRemoveNonexistentEdge() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.addNode("A");
            graph.addNode("B");

            boolean removed = graph.removeEdge("A", "B");

            assertThat(removed).isFalse();
        }

        @Test
        @DisplayName("edges - 获取所有边")
        void testEdges() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");

            assertThat(graph.edgeCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("邻接查询测试")
    class AdjacencyQueryTests {

        @Test
        @DisplayName("successors - 后继节点")
        void testSuccessors() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");

            Set<String> successors = graph.successors("A");

            assertThat(successors).containsExactlyInAnyOrder("B", "C");
        }

        @Test
        @DisplayName("predecessors - 前驱节点")
        void testPredecessors() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "C");
            graph.putEdge("B", "C");

            Set<String> predecessors = graph.predecessors("C");

            assertThat(predecessors).containsExactlyInAnyOrder("A", "B");
        }

        @Test
        @DisplayName("adjacentNodes - 邻接节点")
        void testAdjacentNodes() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("C", "B");

            Set<String> adjacent = graph.adjacentNodes("B");

            assertThat(adjacent).containsExactlyInAnyOrder("A", "C");
        }

        @Test
        @DisplayName("degree - 度数")
        void testDegree() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("C", "B");
            graph.putEdge("B", "D");

            assertThat(graph.degree("B")).isEqualTo(3);
        }

        @Test
        @DisplayName("inDegree - 入度")
        void testInDegree() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "C");
            graph.putEdge("B", "C");

            assertThat(graph.inDegree("C")).isEqualTo(2);
        }

        @Test
        @DisplayName("outDegree - 出度")
        void testOutDegree() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("A", "C");

            assertThat(graph.outDegree("A")).isEqualTo(2);
        }

        @Test
        @DisplayName("incidentEdges - 关联边")
        void testIncidentEdges() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");
            graph.putEdge("B", "C");

            var edges = graph.incidentEdges("B");

            assertThat(edges).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            MutableGraph<String> graph1 = MutableGraph.directed();
            graph1.putEdge("A", "B");

            MutableGraph<String> graph2 = MutableGraph.directed();
            graph2.putEdge("A", "B");

            assertThat(graph1).isEqualTo(graph2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            MutableGraph<String> graph1 = MutableGraph.directed();
            graph1.putEdge("A", "B");

            MutableGraph<String> graph2 = MutableGraph.directed();
            graph2.putEdge("A", "B");

            assertThat(graph1.hashCode()).isEqualTo(graph2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            MutableGraph<String> graph = MutableGraph.directed();
            graph.putEdge("A", "B");

            String str = graph.toString();

            assertThat(str).contains("directed");
            assertThat(str).contains("A");
            assertThat(str).contains("B");
        }
    }
}
