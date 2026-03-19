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
 * CommunityDetectionUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("CommunityDetectionUtil 测试")
class CommunityDetectionUtilTest {

    @Nested
    @DisplayName("louvain测试")
    class LouvainTests {

        @Test
        @DisplayName("检测简单社区")
        void testSimpleCommunities() {
            Graph<String> graph = OpenGraph.undirected();
            // Community 1: A-B-C
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("A", "C", 1.0);
            // Community 2: D-E-F
            graph.addEdge("D", "E", 1.0);
            graph.addEdge("E", "F", 1.0);
            graph.addEdge("D", "F", 1.0);
            // Weak connection
            graph.addEdge("C", "D", 0.1);

            CommunityDetectionUtil.CommunityResult<String> result = CommunityDetectionUtil.louvain(graph);

            assertThat(result.communities()).isNotEmpty();
            assertThat(result.communityCount()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("带参数的Louvain算法")
        void testLouvainWithParams() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);

            CommunityDetectionUtil.CommunityResult<String> result = CommunityDetectionUtil.louvain(graph, 1.0, 50);

            assertThat(result).isNotNull();
            assertThat(result.iterations()).isLessThanOrEqualTo(50);
        }

        @Test
        @DisplayName("空图返回空结果")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();

            CommunityDetectionUtil.CommunityResult<String> result = CommunityDetectionUtil.louvain(graph);

            assertThat(result.communities()).isEmpty();
            assertThat(result.modularity()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("null图返回空结果")
        void testNullGraph() {
            CommunityDetectionUtil.CommunityResult<String> result = CommunityDetectionUtil.louvain(null);

            assertThat(result.communities()).isEmpty();
        }

        @Test
        @DisplayName("无边图每个顶点是单独社区")
        void testNoEdges() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addVertex("A");
            graph.addVertex("B");
            graph.addVertex("C");

            CommunityDetectionUtil.CommunityResult<String> result = CommunityDetectionUtil.louvain(graph);

            assertThat(result.communityCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("labelPropagation测试")
    class LabelPropagationTests {

        @Test
        @DisplayName("标签传播检测社区")
        void testLabelPropagation() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("A", "C", 1.0);
            graph.addEdge("D", "E", 1.0);

            CommunityDetectionUtil.CommunityResult<String> result = CommunityDetectionUtil.labelPropagation(graph);

            assertThat(result.communities()).isNotEmpty();
        }

        @Test
        @DisplayName("带最大迭代次数的标签传播")
        void testLabelPropagationWithMaxIterations() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);

            CommunityDetectionUtil.CommunityResult<String> result = CommunityDetectionUtil.labelPropagation(graph, 10);

            assertThat(result).isNotNull();
            assertThat(result.iterations()).isLessThanOrEqualTo(10);
        }

        @Test
        @DisplayName("空图返回空结果")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.undirected();

            CommunityDetectionUtil.CommunityResult<String> result = CommunityDetectionUtil.labelPropagation(graph);

            assertThat(result.communities()).isEmpty();
        }

        @Test
        @DisplayName("null图返回空结果")
        void testNullGraph() {
            CommunityDetectionUtil.CommunityResult<String> result = CommunityDetectionUtil.labelPropagation(null);

            assertThat(result.communities()).isEmpty();
        }
    }

    @Nested
    @DisplayName("calculateModularity测试")
    class CalculateModularityTests {

        @Test
        @DisplayName("计算模块度")
        void testCalculateModularity() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);

            List<Set<String>> communities = List.of(Set.of("A", "B", "C"));

            double modularity = CommunityDetectionUtil.calculateModularity(graph, communities);

            assertThat(modularity).isNotNaN();
        }

        @Test
        @DisplayName("带分辨率参数计算模块度")
        void testCalculateModularityWithResolution() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);

            List<Set<String>> communities = List.of(Set.of("A", "B"));

            double modularity = CommunityDetectionUtil.calculateModularity(graph, communities, 1.5);

            assertThat(modularity).isNotNaN();
        }

        @Test
        @DisplayName("空图返回0")
        void testEmptyGraph() {
            double modularity = CommunityDetectionUtil.calculateModularity(OpenGraph.undirected(), List.of());

            assertThat(modularity).isEqualTo(0.0);
        }

        @Test
        @DisplayName("null参数返回0")
        void testNullParams() {
            assertThat(CommunityDetectionUtil.calculateModularity(null, List.of())).isEqualTo(0.0);
            assertThat(CommunityDetectionUtil.calculateModularity(OpenGraph.undirected(), null)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("CommunityResult测试")
    class CommunityResultTests {

        @Test
        @DisplayName("communityCount方法")
        void testCommunityCount() {
            CommunityDetectionUtil.CommunityResult<String> result =
                new CommunityDetectionUtil.CommunityResult<>(
                    List.of(Set.of("A", "B"), Set.of("C")),
                    java.util.Map.of("A", 0, "B", 0, "C", 1),
                    0.5,
                    10
                );

            assertThat(result.communityCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getCommunityOf方法")
        void testGetCommunityOf() {
            CommunityDetectionUtil.CommunityResult<String> result =
                new CommunityDetectionUtil.CommunityResult<>(
                    List.of(Set.of("A", "B"), Set.of("C")),
                    java.util.Map.of("A", 0, "B", 0, "C", 1),
                    0.5,
                    10
                );

            assertThat(result.getCommunityOf("A")).containsExactlyInAnyOrder("A", "B");
            assertThat(result.getCommunityOf("C")).containsExactly("C");
        }

        @Test
        @DisplayName("顶点不存在返回空集")
        void testGetCommunityOfNotFound() {
            CommunityDetectionUtil.CommunityResult<String> result =
                new CommunityDetectionUtil.CommunityResult<>(
                    List.of(Set.of("A")),
                    java.util.Map.of("A", 0),
                    0.5,
                    10
                );

            assertThat(result.getCommunityOf("X")).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCommunity测试")
    class GetCommunityTests {

        @Test
        @DisplayName("获取顶点所在社区")
        void testGetCommunity() {
            CommunityDetectionUtil.CommunityResult<String> result =
                new CommunityDetectionUtil.CommunityResult<>(
                    List.of(Set.of("A", "B")),
                    java.util.Map.of("A", 0, "B", 0),
                    0.5,
                    10
                );

            Set<String> community = CommunityDetectionUtil.getCommunity(result, "A");

            assertThat(community).containsExactlyInAnyOrder("A", "B");
        }

        @Test
        @DisplayName("null参数返回空集")
        void testNullParams() {
            assertThat(CommunityDetectionUtil.getCommunity(null, "A")).isEmpty();
            assertThat(CommunityDetectionUtil.getCommunity(
                new CommunityDetectionUtil.CommunityResult<>(List.of(), java.util.Map.of(), 0, 0),
                null
            )).isEmpty();
        }
    }

    @Nested
    @DisplayName("inSameCommunity测试")
    class InSameCommunityTests {

        @Test
        @DisplayName("同社区返回true")
        void testInSameCommunity() {
            CommunityDetectionUtil.CommunityResult<String> result =
                new CommunityDetectionUtil.CommunityResult<>(
                    List.of(Set.of("A", "B")),
                    java.util.Map.of("A", 0, "B", 0),
                    0.5,
                    10
                );

            assertThat(CommunityDetectionUtil.inSameCommunity(result, "A", "B")).isTrue();
        }

        @Test
        @DisplayName("不同社区返回false")
        void testNotInSameCommunity() {
            CommunityDetectionUtil.CommunityResult<String> result =
                new CommunityDetectionUtil.CommunityResult<>(
                    List.of(Set.of("A"), Set.of("B")),
                    java.util.Map.of("A", 0, "B", 1),
                    0.5,
                    10
                );

            assertThat(CommunityDetectionUtil.inSameCommunity(result, "A", "B")).isFalse();
        }

        @Test
        @DisplayName("null参数返回false")
        void testNullParams() {
            assertThat(CommunityDetectionUtil.inSameCommunity(null, "A", "B")).isFalse();
        }
    }

    @Nested
    @DisplayName("getSortedBySize测试")
    class GetSortedBySizeTests {

        @Test
        @DisplayName("按大小排序社区")
        void testSortedBySize() {
            CommunityDetectionUtil.CommunityResult<String> result =
                new CommunityDetectionUtil.CommunityResult<>(
                    List.of(Set.of("A"), Set.of("B", "C", "D"), Set.of("E", "F")),
                    java.util.Map.of("A", 0, "B", 1, "C", 1, "D", 1, "E", 2, "F", 2),
                    0.5,
                    10
                );

            List<Set<String>> sorted = CommunityDetectionUtil.getSortedBySize(result);

            assertThat(sorted.get(0)).hasSize(3); // B, C, D
            assertThat(sorted.get(1)).hasSize(2); // E, F
            assertThat(sorted.get(2)).hasSize(1); // A
        }

        @Test
        @DisplayName("null结果返回空列表")
        void testNullResult() {
            List<Set<String>> sorted = CommunityDetectionUtil.getSortedBySize(null);

            assertThat(sorted).isEmpty();
        }
    }
}
