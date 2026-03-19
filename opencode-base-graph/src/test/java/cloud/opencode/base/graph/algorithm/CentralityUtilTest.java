package cloud.opencode.base.graph.algorithm;

import cloud.opencode.base.graph.Graph;
import cloud.opencode.base.graph.OpenGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * CentralityUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("CentralityUtil 测试")
class CentralityUtilTest {

    @Nested
    @DisplayName("degreeCentrality测试")
    class DegreeCentralityTests {

        @Test
        @DisplayName("计算度中心性")
        void testDegreeCentrality() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("A", "D");
            graph.addEdge("B", "C");

            Map<String, Double> centrality = CentralityUtil.degreeCentrality(graph);

            assertThat(centrality).isNotEmpty();
            assertThat(centrality.get("A")).isGreaterThan(centrality.get("D"));
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Graph<String> graph = OpenGraph.directed();

            Map<String, Double> centrality = CentralityUtil.degreeCentrality(graph);

            assertThat(centrality).isEmpty();
        }

        @Test
        @DisplayName("null图返回空映射")
        void testNullGraph() {
            Map<String, Double> centrality = CentralityUtil.degreeCentrality(null);

            assertThat(centrality).isEmpty();
        }
    }

    @Nested
    @DisplayName("inDegreeCentrality测试")
    class InDegreeCentralityTests {

        @Test
        @DisplayName("计算入度中心性")
        void testInDegreeCentrality() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "C");
            graph.addEdge("B", "C");
            graph.addEdge("C", "D");

            Map<String, Double> centrality = CentralityUtil.inDegreeCentrality(graph);

            assertThat(centrality).isNotEmpty();
            assertThat(centrality.get("C")).isGreaterThan(centrality.get("A"));
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, Double> centrality = CentralityUtil.inDegreeCentrality(OpenGraph.directed());

            assertThat(centrality).isEmpty();
        }
    }

    @Nested
    @DisplayName("outDegreeCentrality测试")
    class OutDegreeCentralityTests {

        @Test
        @DisplayName("计算出度中心性")
        void testOutDegreeCentrality() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("A", "C");
            graph.addEdge("A", "D");
            graph.addEdge("B", "D");

            Map<String, Double> centrality = CentralityUtil.outDegreeCentrality(graph);

            assertThat(centrality).isNotEmpty();
            assertThat(centrality.get("A")).isGreaterThan(centrality.get("B"));
        }
    }

    @Nested
    @DisplayName("closenessCentrality测试")
    class ClosenessCentralityTests {

        @Test
        @DisplayName("计算接近中心性")
        void testClosenessCentrality() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("B", "D", 1.0);

            Map<String, Double> centrality = CentralityUtil.closenessCentrality(graph);

            assertThat(centrality).isNotEmpty();
            assertThat(centrality.get("B")).isGreaterThan(centrality.get("A"));
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, Double> centrality = CentralityUtil.closenessCentrality(OpenGraph.directed());

            assertThat(centrality).isEmpty();
        }
    }

    @Nested
    @DisplayName("betweennessCentrality测试")
    class BetweennessCentralityTests {

        @Test
        @DisplayName("计算中介中心性")
        void testBetweennessCentrality() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);
            graph.addEdge("B", "D", 1.0);
            graph.addEdge("C", "E", 1.0);
            graph.addEdge("D", "E", 1.0);

            Map<String, Double> centrality = CentralityUtil.betweennessCentrality(graph);

            assertThat(centrality).isNotEmpty();
        }

        @Test
        @DisplayName("不归一化的中介中心性")
        void testBetweennessCentralityNotNormalized() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B", 1.0);
            graph.addEdge("B", "C", 1.0);

            Map<String, Double> centrality = CentralityUtil.betweennessCentrality(graph, false);

            assertThat(centrality).isNotEmpty();
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, Double> centrality = CentralityUtil.betweennessCentrality(OpenGraph.directed());

            assertThat(centrality).isEmpty();
        }
    }

    @Nested
    @DisplayName("pageRank测试")
    class PageRankTests {

        @Test
        @DisplayName("计算PageRank（默认参数）")
        void testPageRankDefault() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("C", "A");

            Map<String, Double> ranks = CentralityUtil.pageRank(graph);

            assertThat(ranks).isNotEmpty();
            assertThat(ranks).containsKeys("A", "B", "C");
        }

        @Test
        @DisplayName("自定义参数计算PageRank")
        void testPageRankWithParams() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Map<String, Double> ranks = CentralityUtil.pageRank(graph, 0.85, 50);

            assertThat(ranks).isNotEmpty();
        }

        @Test
        @DisplayName("带收敛容差的PageRank")
        void testPageRankWithTolerance() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Map<String, Double> ranks = CentralityUtil.pageRank(graph, 0.85, 100, 1e-6);

            assertThat(ranks).isNotEmpty();
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, Double> ranks = CentralityUtil.pageRank(OpenGraph.directed());

            assertThat(ranks).isEmpty();
        }

        @Test
        @DisplayName("null图返回空映射")
        void testNullGraph() {
            Map<String, Double> ranks = CentralityUtil.pageRank(null);

            assertThat(ranks).isEmpty();
        }
    }

    @Nested
    @DisplayName("eigenvectorCentrality测试")
    class EigenvectorCentralityTests {

        @Test
        @DisplayName("计算特征向量中心性")
        void testEigenvectorCentrality() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");
            graph.addEdge("B", "D");

            Map<String, Double> centrality = CentralityUtil.eigenvectorCentrality(graph);

            assertThat(centrality).isNotEmpty();
        }

        @Test
        @DisplayName("带参数的特征向量中心性")
        void testEigenvectorCentralityWithParams() {
            Graph<String> graph = OpenGraph.undirected();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Map<String, Double> centrality = CentralityUtil.eigenvectorCentrality(graph, 50, 1e-4);

            assertThat(centrality).isNotEmpty();
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, Double> centrality = CentralityUtil.eigenvectorCentrality(OpenGraph.directed());

            assertThat(centrality).isEmpty();
        }
    }

    @Nested
    @DisplayName("katzCentrality测试")
    class KatzCentralityTests {

        @Test
        @DisplayName("计算Katz中心性")
        void testKatzCentrality() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Map<String, Double> centrality = CentralityUtil.katzCentrality(graph, 0.1, 1.0);

            assertThat(centrality).isNotEmpty();
        }

        @Test
        @DisplayName("带完整参数的Katz中心性")
        void testKatzCentralityWithFullParams() {
            Graph<String> graph = OpenGraph.directed();
            graph.addEdge("A", "B");
            graph.addEdge("B", "C");

            Map<String, Double> centrality = CentralityUtil.katzCentrality(graph, 0.1, 1.0, 50, 1e-4);

            assertThat(centrality).isNotEmpty();
        }

        @Test
        @DisplayName("空图返回空映射")
        void testEmptyGraph() {
            Map<String, Double> centrality = CentralityUtil.katzCentrality(OpenGraph.directed(), 0.1, 1.0);

            assertThat(centrality).isEmpty();
        }
    }

    @Nested
    @DisplayName("topK测试")
    class TopKTests {

        @Test
        @DisplayName("获取前K个中心顶点")
        void testTopK() {
            Map<String, Double> centrality = Map.of(
                "A", 0.9, "B", 0.7, "C", 0.5, "D", 0.3
            );

            List<String> top = CentralityUtil.topK(centrality, 2);

            assertThat(top).hasSize(2);
            assertThat(top.get(0)).isEqualTo("A");
            assertThat(top.get(1)).isEqualTo("B");
        }

        @Test
        @DisplayName("空映射返回空列表")
        void testEmptyMap() {
            List<String> top = CentralityUtil.topK(Map.of(), 5);

            assertThat(top).isEmpty();
        }

        @Test
        @DisplayName("null映射返回空列表")
        void testNullMap() {
            List<String> top = CentralityUtil.topK(null, 5);

            assertThat(top).isEmpty();
        }
    }

    @Nested
    @DisplayName("normalize测试")
    class NormalizeTests {

        @Test
        @DisplayName("归一化中心性值")
        void testNormalize() {
            Map<String, Double> centrality = Map.of(
                "A", 10.0, "B", 5.0, "C", 0.0
            );

            Map<String, Double> normalized = CentralityUtil.normalize(centrality);

            assertThat(normalized.get("A")).isEqualTo(1.0);
            assertThat(normalized.get("C")).isEqualTo(0.0);
            assertThat(normalized.get("B")).isEqualTo(0.5);
        }

        @Test
        @DisplayName("空映射返回空映射")
        void testEmptyMap() {
            Map<String, Double> normalized = CentralityUtil.normalize(Map.of());

            assertThat(normalized).isEmpty();
        }

        @Test
        @DisplayName("null映射返回空映射")
        void testNullMap() {
            Map<String, Double> normalized = CentralityUtil.normalize(null);

            assertThat(normalized).isEmpty();
        }
    }

    @Nested
    @DisplayName("getStats测试")
    class GetStatsTests {

        @Test
        @DisplayName("获取中心性统计信息")
        void testGetStats() {
            Map<String, Double> centrality = Map.of(
                "A", 10.0, "B", 5.0, "C", 0.0
            );

            CentralityUtil.CentralityStats stats = CentralityUtil.getStats(centrality);

            assertThat(stats.min()).isEqualTo(0.0);
            assertThat(stats.max()).isEqualTo(10.0);
            assertThat(stats.mean()).isEqualTo(5.0);
            assertThat(stats.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("空映射返回零值统计")
        void testEmptyMap() {
            CentralityUtil.CentralityStats stats = CentralityUtil.getStats(Map.of());

            assertThat(stats.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("null映射返回零值统计")
        void testNullMap() {
            CentralityUtil.CentralityStats stats = CentralityUtil.getStats(null);

            assertThat(stats.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("CentralityStats记录测试")
    class CentralityStatsTests {

        @Test
        @DisplayName("toString方法")
        void testToString() {
            CentralityUtil.CentralityStats stats = new CentralityUtil.CentralityStats(
                0.0, 1.0, 0.5, 0.25, 10
            );

            String str = stats.toString();

            assertThat(str).contains("min=0.0000");
            assertThat(str).contains("max=1.0000");
            assertThat(str).contains("count=10");
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("默认常量值")
        void testConstants() {
            assertThat(CentralityUtil.DEFAULT_DAMPING_FACTOR).isEqualTo(0.85);
            assertThat(CentralityUtil.DEFAULT_MAX_ITERATIONS).isEqualTo(100);
            assertThat(CentralityUtil.DEFAULT_TOLERANCE).isEqualTo(1e-6);
        }
    }
}
