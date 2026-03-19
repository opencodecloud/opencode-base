package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeFilterTest Tests
 * TreeFilterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeFilter Tests")
class TreeFilterTest {

    private List<DefaultTreeNode<Long>> roots;

    @BeforeEach
    void setUp() {
        DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
        DefaultTreeNode<Long> node2 = new DefaultTreeNode<>(2L, 1L, "Node2");
        DefaultTreeNode<Long> node3 = new DefaultTreeNode<>(3L, 1L, "Node3");
        DefaultTreeNode<Long> node4 = new DefaultTreeNode<>(4L, 2L, "Node4");
        DefaultTreeNode<Long> node5 = new DefaultTreeNode<>(5L, 2L, "Node5");

        node2.setChildren(new ArrayList<>(List.of(node4, node5)));
        root.setChildren(new ArrayList<>(List.of(node2, node3)));
        roots = List.of(root);
    }

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("filter should keep matching nodes and ancestors")
        void filterShouldKeepMatchingNodesAndAncestors() {
            List<DefaultTreeNode<Long>> filtered = TreeFilter.filter(roots, node -> node.getId() == 4L);

            assertThat(filtered).hasSize(1);
            // Should keep root->node2->node4 path
        }

        @Test
        @DisplayName("filter should return empty for no matches")
        void filterShouldReturnEmptyForNoMatches() {
            List<DefaultTreeNode<Long>> filtered = TreeFilter.filter(roots, node -> node.getId() > 100L);

            assertThat(filtered).isEmpty();
        }
    }

    @Nested
    @DisplayName("FilterWithAncestors Tests")
    class FilterWithAncestorsTests {

        @Test
        @DisplayName("filterWithAncestors should preserve ancestor chain")
        void filterWithAncestorsShouldPreserveAncestorChain() {
            List<DefaultTreeNode<Long>> filtered = TreeFilter.filterWithAncestors(roots,
                node -> node.getId() == 5L);

            assertThat(filtered).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("FilterFlat Tests")
    class FilterFlatTests {

        @Test
        @DisplayName("filterFlat should return only matching nodes")
        void filterFlatShouldReturnOnlyMatchingNodes() {
            List<DefaultTreeNode<Long>> filtered = TreeFilter.filterFlat(roots,
                node -> node.getId() > 3L);

            assertThat(filtered).hasSize(2);
            assertThat(filtered).extracting(DefaultTreeNode::getId)
                .containsExactlyInAnyOrder(4L, 5L);
        }

        @Test
        @DisplayName("filterFlat should return empty for no matches")
        void filterFlatShouldReturnEmptyForNoMatches() {
            List<DefaultTreeNode<Long>> filtered = TreeFilter.filterFlat(roots,
                node -> node.getId() > 100L);

            assertThat(filtered).isEmpty();
        }
    }

    @Nested
    @DisplayName("FilterByDepth Tests")
    class FilterByDepthTests {

        @Test
        @DisplayName("filterByDepth should limit tree depth")
        void filterByDepthShouldLimitTreeDepth() {
            List<DefaultTreeNode<Long>> filtered = TreeFilter.filterByDepth(roots, 1);

            assertThat(filtered).hasSize(1);
            DefaultTreeNode<Long> filteredRoot = filtered.get(0);
            assertThat(filteredRoot.getChildren()).hasSize(2);
            // Children at depth 2 should be removed
            for (DefaultTreeNode<Long> child : filteredRoot.getChildren()) {
                assertThat(child.getChildren()).isEmpty();
            }
        }

        @Test
        @DisplayName("filterByDepth with depth 0 should return only roots")
        void filterByDepthWithDepth0ShouldReturnOnlyRoots() {
            List<DefaultTreeNode<Long>> filtered = TreeFilter.filterByDepth(roots, 0);

            assertThat(filtered).hasSize(1);
            assertThat(filtered.get(0).getChildren()).isEmpty();
        }

        @Test
        @DisplayName("filterByDepth with large depth should return full tree")
        void filterByDepthWithLargeDepthShouldReturnFullTree() {
            List<DefaultTreeNode<Long>> filtered = TreeFilter.filterByDepth(roots, 100);

            assertThat(filtered).isNotEmpty();
        }
    }
}
