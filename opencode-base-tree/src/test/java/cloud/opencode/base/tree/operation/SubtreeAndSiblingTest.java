package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Subtree Extraction and Siblings Tests
 * 子树提取和兄弟节点测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.3
 */
@DisplayName("Subtree & Siblings Tests")
class SubtreeAndSiblingTest {

    private List<DefaultTreeNode<Long>> roots;

    @BeforeEach
    void setUp() {
        DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
        DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
        DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
        DefaultTreeNode<Long> child3 = new DefaultTreeNode<>(6L, 1L, "Child3");
        DefaultTreeNode<Long> grandChild1 = new DefaultTreeNode<>(4L, 2L, "GC1");
        DefaultTreeNode<Long> grandChild2 = new DefaultTreeNode<>(5L, 2L, "GC2");

        child1.setChildren(new ArrayList<>(List.of(grandChild1, grandChild2)));
        root.setChildren(new ArrayList<>(List.of(child1, child2, child3)));
        roots = List.of(root);
    }

    @Nested
    @DisplayName("ExtractSubtree Tests")
    class ExtractSubtreeTests {

        @Test
        @DisplayName("should extract subtree with children")
        void shouldExtractSubtreeWithChildren() {
            Optional<DefaultTreeNode<Long>> subtree = TreeUtil.extractSubtree(roots, 2L);

            assertThat(subtree).isPresent();
            assertThat(subtree.get().getId()).isEqualTo(2L);
            assertThat(subtree.get().getChildren()).hasSize(2);
        }

        @Test
        @DisplayName("should return empty for non-existent id")
        void shouldReturnEmptyForNonExistentId() {
            Optional<DefaultTreeNode<Long>> subtree = TreeUtil.extractSubtree(roots, 99L);

            assertThat(subtree).isEmpty();
        }

        @Test
        @DisplayName("should extract leaf node")
        void shouldExtractLeafNode() {
            Optional<DefaultTreeNode<Long>> subtree = TreeUtil.extractSubtree(roots, 4L);

            assertThat(subtree).isPresent();
            assertThat(subtree.get().getChildren()).isNullOrEmpty();
        }
    }

    @Nested
    @DisplayName("GetSiblings Tests")
    class GetSiblingsTests {

        @Test
        @DisplayName("should return siblings excluding self")
        void shouldReturnSiblingsExcludingSelf() {
            List<DefaultTreeNode<Long>> siblings = TreeUtil.getSiblings(roots, 2L);

            assertThat(siblings).hasSize(2);
            assertThat(siblings).extracting(DefaultTreeNode::getId)
                    .containsExactlyInAnyOrder(3L, 6L);
        }

        @Test
        @DisplayName("should return empty for root node")
        void shouldReturnEmptyForRootNode() {
            List<DefaultTreeNode<Long>> siblings = TreeUtil.getSiblings(roots, 1L);

            assertThat(siblings).isEmpty();
        }

        @Test
        @DisplayName("should return empty for non-existent id")
        void shouldReturnEmptyForNonExistentId() {
            List<DefaultTreeNode<Long>> siblings = TreeUtil.getSiblings(roots, 99L);

            assertThat(siblings).isEmpty();
        }

        @Test
        @DisplayName("should return sibling for grandchild")
        void shouldReturnSiblingForGrandchild() {
            List<DefaultTreeNode<Long>> siblings = TreeUtil.getSiblings(roots, 4L);

            assertThat(siblings).hasSize(1);
            assertThat(siblings.getFirst().getId()).isEqualTo(5L);
        }
    }
}
