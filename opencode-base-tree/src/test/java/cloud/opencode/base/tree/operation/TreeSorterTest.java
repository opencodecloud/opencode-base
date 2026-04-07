package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeSorterTest Tests
 * TreeSorterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.3
 */
@DisplayName("TreeSorter Tests")
class TreeSorterTest {

    private List<DefaultTreeNode<Long>> roots;
    private static final Comparator<DefaultTreeNode<Long>> BY_NAME =
            Comparator.comparing(DefaultTreeNode::getName);

    @BeforeEach
    void setUp() {
        DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "C-Root");
        DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "B-Child");
        DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "A-Child");
        DefaultTreeNode<Long> grandChild1 = new DefaultTreeNode<>(4L, 2L, "Z-Grand");
        DefaultTreeNode<Long> grandChild2 = new DefaultTreeNode<>(5L, 2L, "A-Grand");

        child1.setChildren(new ArrayList<>(List.of(grandChild1, grandChild2)));
        root.setChildren(new ArrayList<>(List.of(child1, child2)));
        roots = new ArrayList<>(List.of(root));
    }

    @Nested
    @DisplayName("Sort Tests")
    class SortTests {

        @Test
        @DisplayName("should sort children recursively by name")
        void shouldSortChildrenRecursivelyByName() {
            TreeSorter.sort(roots, BY_NAME);

            // Root's children: A-Child before B-Child
            assertThat(roots.getFirst().getChildren().get(0).getName()).isEqualTo("A-Child");
            assertThat(roots.getFirst().getChildren().get(1).getName()).isEqualTo("B-Child");

            // B-Child's children: A-Grand before Z-Grand
            DefaultTreeNode<Long> bChild = roots.getFirst().getChildren().get(1);
            assertThat(bChild.getChildren().get(0).getName()).isEqualTo("A-Grand");
            assertThat(bChild.getChildren().get(1).getName()).isEqualTo("Z-Grand");
        }

        @Test
        @DisplayName("should handle empty list")
        void shouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> empty = new ArrayList<>();
            assertThatNoException().isThrownBy(() -> TreeSorter.sort(empty, BY_NAME));
        }
    }

    @Nested
    @DisplayName("SortBy Tests")
    class SortByTests {

        @Test
        @DisplayName("should sort by extracted key")
        void shouldSortByExtractedKey() {
            TreeSorter.<DefaultTreeNode<Long>, Long, String>sortBy(roots, DefaultTreeNode::getName);

            assertThat(roots.getFirst().getChildren().get(0).getName()).isEqualTo("A-Child");
        }
    }

    @Nested
    @DisplayName("SortReversed Tests")
    class SortReversedTests {

        @Test
        @DisplayName("should sort in reversed order")
        void shouldSortInReversedOrder() {
            TreeSorter.sortReversed(roots, BY_NAME);

            assertThat(roots.getFirst().getChildren().get(0).getName()).isEqualTo("B-Child");
            assertThat(roots.getFirst().getChildren().get(1).getName()).isEqualTo("A-Child");
        }
    }

    @Nested
    @DisplayName("IsSorted Tests")
    class IsSortedTests {

        @Test
        @DisplayName("should return false for unsorted tree")
        void shouldReturnFalseForUnsortedTree() {
            boolean sorted = TreeSorter.isSorted(roots, BY_NAME);
            assertThat(sorted).isFalse();
        }

        @Test
        @DisplayName("should return true after sorting")
        void shouldReturnTrueAfterSorting() {
            TreeSorter.sort(roots, BY_NAME);
            boolean sorted = TreeSorter.isSorted(roots, BY_NAME);
            assertThat(sorted).isTrue();
        }

        @Test
        @DisplayName("should return true for empty list")
        void shouldReturnTrueForEmptyList() {
            List<DefaultTreeNode<Long>> empty = List.of();
            boolean sorted = TreeSorter.isSorted(empty, BY_NAME);
            assertThat(sorted).isTrue();
        }
    }
}
