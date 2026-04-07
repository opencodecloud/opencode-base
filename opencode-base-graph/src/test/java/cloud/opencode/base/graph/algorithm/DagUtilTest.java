package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import cloud.opencode.base.graph.exception.GraphErrorCode;
import cloud.opencode.base.graph.exception.GraphException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * DagUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("DagUtil 测试")
class DagUtilTest {

    @Nested
    @DisplayName("longestPath 测试")
    class LongestPathTests {

        @Test
        @DisplayName("线性DAG: A->B->C->D")
        void testLinearDag() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            List<String> path = DagUtil.longestPath(graph);

            assertThat(path).containsExactly("A", "B", "C", "D");
        }

        @Test
        @DisplayName("菱形DAG: 带权重")
        void testDiamondDagWithWeights() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("A", "C", 10.0);
            graph.addEdge("B", "D", 1.0);
            graph.addEdge("C", "D", 1.0);

            List<String> path = DagUtil.longestPath(graph);

            // Longest: A->C->D (weight 11) vs A->B->D (weight 2)
            assertThat(path).containsExactly("A", "C", "D");
        }

        @Test
        @DisplayName("单顶点DAG")
        void testSingleVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addVertex("A");

            List<String> path = DagUtil.longestPath(graph);

            assertThat(path).containsExactly("A");
        }

        @Test
        @DisplayName("空DAG")
        void testEmptyDag() {
            Graph<String> graph = OpenGraph.directed();

            List<String> path = DagUtil.longestPath(graph);

            assertThat(path).isEmpty();
        }
    }

    @Nested
    @DisplayName("longestPath(source, target) 测试")
    class LongestPathSourceTargetTests {

        @Test
        @DisplayName("存在路径时返回最长路径")
        void testPathExists() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("A", "C", 5.0);
            graph.addEdge("B", "D", 1.0);
            graph.addEdge("C", "D", 1.0);

            List<String> path = DagUtil.longestPath(graph, "A", "D");

            // A->C->D (6.0) vs A->B->D (2.0)
            assertThat(path).containsExactly("A", "C", "D");
        }

        @Test
        @DisplayName("无路径时返回空列表")
        void testNoPath() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addVertex("C");

            List<String> path = DagUtil.longestPath(graph, "A", "C");

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("源和目标相同")
        void testSameSourceTarget() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            List<String> path = DagUtil.longestPath(graph, "A", "A");

            assertThat(path).containsExactly("A");
        }

        @Test
        @DisplayName("顶点不存在返回空列表")
        void testVertexNotExists() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            List<String> path = DagUtil.longestPath(graph, "A", "Z");

            assertThat(path).isEmpty();
        }
    }

    @Nested
    @DisplayName("longestPathLength 测试")
    class LongestPathLengthTests {

        @Test
        @DisplayName("线性DAG默认权重")
        void testLinearDefaultWeight() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            double length = DagUtil.longestPathLength(graph);

            assertThat(length).isEqualTo(3.0);
        }

        @Test
        @DisplayName("带权重菱形DAG")
        void testWeightedDiamond() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B", 2.0);
            graph.addEdge("A", "C", 5.0);
            graph.addEdge("B", "D", 3.0);
            graph.addEdge("C", "D", 1.0);

            double length = DagUtil.longestPathLength(graph);

            // max(2+3, 5+1) = max(5, 6) = 6
            assertThat(length).isEqualTo(6.0);
        }

        @Test
        @DisplayName("空DAG长度为0")
        void testEmptyDagLength() {
            Graph<String> graph = OpenGraph.directed();

            double length = DagUtil.longestPathLength(graph);

            assertThat(length).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("transitiveReduction 测试")
    class TransitiveReductionTests {

        @Test
        @DisplayName("移除冗余边: A->B->C, A->C => 移除A->C")
        void testRemoveRedundantEdge() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "C"); // redundant

            Graph<String> reduced = DagUtil.transitiveReduction(graph);

            assertThat(reduced.containsEdge("A", "B")).isTrue();
            assertThat(reduced.containsEdge("B", "C")).isTrue();
            assertThat(reduced.containsEdge("A", "C")).isFalse();
        }

        @Test
        @DisplayName("无冗余边时保持不变")
        void testNoRedundantEdges() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Graph<String> reduced = DagUtil.transitiveReduction(graph);

            assertThat(reduced.edgeCount()).isEqualTo(2);
            assertThat(reduced.containsEdge("A", "B")).isTrue();
            assertThat(reduced.containsEdge("B", "C")).isTrue();
        }

        @Test
        @DisplayName("复杂冗余: A->B->C->D, A->C, A->D, B->D")
        void testComplexReduction() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");
            graph.addEdge("A", "C"); // redundant (A->B->C)
            graph.addEdge("A", "D"); // redundant (A->B->C->D)
            graph.addEdge("B", "D"); // redundant (B->C->D)

            Graph<String> reduced = DagUtil.transitiveReduction(graph);

            assertThat(reduced.edgeCount()).isEqualTo(3);
            assertThat(reduced.containsEdge("A", "B")).isTrue();
            assertThat(reduced.containsEdge("B", "C")).isTrue();
            assertThat(reduced.containsEdge("C", "D")).isTrue();
            assertThat(reduced.containsEdge("A", "C")).isFalse();
            assertThat(reduced.containsEdge("A", "D")).isFalse();
            assertThat(reduced.containsEdge("B", "D")).isFalse();
        }
    }

    @Nested
    @DisplayName("transitiveClosure 测试")
    class TransitiveClosureTests {

        @Test
        @DisplayName("添加隐含边: A->B->C => 添加A->C")
        void testAddImpliedEdge() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Graph<String> closure = DagUtil.transitiveClosure(graph);

            assertThat(closure.containsEdge("A", "B")).isTrue();
            assertThat(closure.containsEdge("B", "C")).isTrue();
            assertThat(closure.containsEdge("A", "C")).isTrue();
        }

        @Test
        @DisplayName("链式: A->B->C->D => 添加A->C, A->D, B->D")
        void testChainClosure() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            Graph<String> closure = DagUtil.transitiveClosure(graph);

            // Original 3 edges + A->C, A->D, B->D = 6 edges
            assertThat(closure.edgeCount()).isEqualTo(6);
            assertThat(closure.containsEdge("A", "C")).isTrue();
            assertThat(closure.containsEdge("A", "D")).isTrue();
            assertThat(closure.containsEdge("B", "D")).isTrue();
        }
    }

    @Nested
    @DisplayName("ancestors/descendants 测试")
    class AncestorsDescendantsTests {

        @Test
        @DisplayName("多层DAG祖先查询")
        void testAncestors() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "D");
            graph.addEdge("D", "C");

            Set<String> ancestors = DagUtil.ancestors(graph, "C");

            assertThat(ancestors).containsExactlyInAnyOrder("A", "B", "D");
        }

        @Test
        @DisplayName("多层DAG后代查询")
        void testDescendants() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("A", "D");
            graph.addEdge("D", "C");

            Set<String> descendants = DagUtil.descendants(graph, "A");

            assertThat(descendants).containsExactlyInAnyOrder("B", "C", "D");
        }

        @Test
        @DisplayName("叶子节点无后代")
        void testLeafNoDescendants() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            Set<String> descendants = DagUtil.descendants(graph, "B");

            assertThat(descendants).isEmpty();
        }

        @Test
        @DisplayName("根节点无祖先")
        void testRootNoAncestors() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            Set<String> ancestors = DagUtil.ancestors(graph, "A");

            assertThat(ancestors).isEmpty();
        }

        @Test
        @DisplayName("不存在的顶点返回空集")
        void testNonExistentVertex() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");

            Set<String> ancestors = DagUtil.ancestors(graph, "Z");

            assertThat(ancestors).isEmpty();
        }
    }

    @Nested
    @DisplayName("DAG验证 测试")
    class DagValidationTests {

        @Test
        @DisplayName("非DAG输入抛出GraphException(NOT_DAG)")
        void testNonDagThrows() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A"); // cycle

            assertThatThrownBy(() -> DagUtil.longestPath(graph))
                .isInstanceOf(GraphException.class)
                .satisfies(e -> {
                    GraphException ge = (GraphException) e;
                    assertThat(ge.getGraphErrorCode()).isEqualTo(GraphErrorCode.NOT_DAG);
                });
        }

        @Test
        @DisplayName("无向图抛出GraphException(NOT_DAG)")
        void testUndirectedThrows() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            assertThatThrownBy(() -> DagUtil.longestPath(graph))
                .isInstanceOf(GraphException.class)
                .satisfies(e -> {
                    GraphException ge = (GraphException) e;
                    assertThat(ge.getGraphErrorCode()).isEqualTo(GraphErrorCode.NOT_DAG);
                });
        }

        @Test
        @DisplayName("null图抛出GraphException(NOT_DAG)")
        void testNullGraphThrows() {
            assertThatThrownBy(() -> DagUtil.longestPath(null))
                .isInstanceOf(GraphException.class);
        }

        @Test
        @DisplayName("所有方法都验证DAG: transitiveReduction")
        void testTransitiveReductionValidation() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            assertThatThrownBy(() -> DagUtil.transitiveReduction(graph))
                .isInstanceOf(GraphException.class);
        }

        @Test
        @DisplayName("所有方法都验证DAG: transitiveClosure")
        void testTransitiveClosureValidation() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            assertThatThrownBy(() -> DagUtil.transitiveClosure(graph))
                .isInstanceOf(GraphException.class);
        }

        @Test
        @DisplayName("所有方法都验证DAG: ancestors")
        void testAncestorsValidation() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            assertThatThrownBy(() -> DagUtil.ancestors(graph, "A"))
                .isInstanceOf(GraphException.class);
        }

        @Test
        @DisplayName("所有方法都验证DAG: descendants")
        void testDescendantsValidation() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");

            assertThatThrownBy(() -> DagUtil.descendants(graph, "A"))
                .isInstanceOf(GraphException.class);
        }
    }
}
