package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeStatisticsTest Tests
 * TreeStatisticsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.3
 */
@DisplayName("TreeStatistics Tests")
class TreeStatisticsTest {

    private List<DefaultTreeNode<Long>> roots;

    @BeforeEach
    void setUp() {
        DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
        DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
        DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
        DefaultTreeNode<Long> grandChild1 = new DefaultTreeNode<>(4L, 2L, "GrandChild1");
        DefaultTreeNode<Long> grandChild2 = new DefaultTreeNode<>(5L, 2L, "GrandChild2");

        child1.setChildren(new ArrayList<>(List.of(grandChild1, grandChild2)));
        root.setChildren(new ArrayList<>(List.of(child1, child2)));
        roots = List.of(root);
    }

    @Nested
    @DisplayName("Basic Statistics Tests")
    class BasicTests {

        @Test
        @DisplayName("should count all nodes")
        void shouldCountAllNodes() {
            TreeStatistics stats = TreeStatistics.of(roots);
            assertThat(stats.nodeCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("should count leaf nodes")
        void shouldCountLeafNodes() {
            TreeStatistics stats = TreeStatistics.of(roots);
            assertThat(stats.leafCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("should calculate max depth")
        void shouldCalculateMaxDepth() {
            TreeStatistics stats = TreeStatistics.of(roots);
            assertThat(stats.maxDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("should calculate max width")
        void shouldCalculateMaxWidth() {
            TreeStatistics stats = TreeStatistics.of(roots);
            assertThat(stats.maxWidth()).isEqualTo(2);
        }

        @Test
        @DisplayName("should calculate average branching factor")
        void shouldCalculateAvgBranchingFactor() {
            TreeStatistics stats = TreeStatistics.of(roots);
            assertThat(stats.avgBranchingFactor()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("should track width by level")
        void shouldTrackWidthByLevel() {
            TreeStatistics stats = TreeStatistics.of(roots);
            Map<Integer, Integer> widths = stats.widthByLevel();

            assertThat(widths).containsEntry(0, 1);
            assertThat(widths).containsEntry(1, 2);
            assertThat(widths).containsEntry(2, 2);
        }
    }

    @Nested
    @DisplayName("Derived Metrics Tests")
    class DerivedTests {

        @Test
        @DisplayName("should calculate internal node count")
        void shouldCalculateInternalNodeCount() {
            TreeStatistics stats = TreeStatistics.of(roots);
            assertThat(stats.internalNodeCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("should calculate leaf ratio")
        void shouldCalculateLeafRatio() {
            TreeStatistics stats = TreeStatistics.of(roots);
            assertThat(stats.leafRatio()).isCloseTo(0.6, within(0.001));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty tree")
        void shouldHandleEmptyTree() {
            List<DefaultTreeNode<Long>> empty = Collections.emptyList();
            TreeStatistics stats = TreeStatistics.of(empty);

            assertThat(stats.nodeCount()).isZero();
            assertThat(stats.leafCount()).isZero();
            assertThat(stats.maxDepth()).isZero();
            assertThat(stats.maxWidth()).isZero();
            assertThat(stats.avgBranchingFactor()).isZero();
            assertThat(stats.leafRatio()).isZero();
        }

        @Test
        @DisplayName("should handle single node")
        void shouldHandleSingleNode() {
            DefaultTreeNode<Long> single = new DefaultTreeNode<>(1L, 0L, "Single");
            TreeStatistics stats = TreeStatistics.of(List.of(single));

            assertThat(stats.nodeCount()).isEqualTo(1);
            assertThat(stats.leafCount()).isEqualTo(1);
            assertThat(stats.maxDepth()).isZero();
            assertThat(stats.leafRatio()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should produce readable summary")
        void shouldProduceReadableSummary() {
            TreeStatistics stats = TreeStatistics.of(roots);
            String summary = stats.summary();

            assertThat(summary).contains("nodes=5");
            assertThat(summary).contains("leaves=3");
        }

        @Test
        @DisplayName("widthByLevel should be immutable")
        void widthByLevelShouldBeImmutable() {
            TreeStatistics stats = TreeStatistics.of(roots);

            assertThatThrownBy(() -> stats.widthByLevel().put(99, 1))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
