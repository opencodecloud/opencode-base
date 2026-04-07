package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.DefaultTreeNode;
import cloud.opencode.base.tree.exception.TreeException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeMergerTest Tests
 * TreeMergerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.3
 */
@DisplayName("TreeMerger Tests")
class TreeMergerTest {

    @Nested
    @DisplayName("MergeKeepLeft Tests")
    class MergeKeepLeftTests {

        @Test
        @DisplayName("should keep left node on conflict")
        void shouldKeepLeftNodeOnConflict() {
            DefaultTreeNode<Long> left1 = new DefaultTreeNode<>(1L, 0L, "Left-Root");
            DefaultTreeNode<Long> right1 = new DefaultTreeNode<>(1L, 0L, "Right-Root");

            List<DefaultTreeNode<Long>> merged = TreeMerger.mergeKeepLeft(
                    new ArrayList<>(List.of(left1)),
                    new ArrayList<>(List.of(right1)));

            assertThat(merged).hasSize(1);
            assertThat(merged.getFirst().getName()).isEqualTo("Left-Root");
        }

        @Test
        @DisplayName("should include nodes only in right")
        void shouldIncludeNodesOnlyInRight() {
            DefaultTreeNode<Long> left1 = new DefaultTreeNode<>(1L, 0L, "Left");
            DefaultTreeNode<Long> right2 = new DefaultTreeNode<>(2L, 0L, "Right-Only");

            List<DefaultTreeNode<Long>> merged = TreeMerger.mergeKeepLeft(
                    new ArrayList<>(List.of(left1)),
                    new ArrayList<>(List.of(right2)));

            assertThat(merged).hasSize(2);
        }

        @Test
        @DisplayName("should handle empty left")
        void shouldHandleEmptyLeft() {
            DefaultTreeNode<Long> right1 = new DefaultTreeNode<>(1L, 0L, "Right");

            List<DefaultTreeNode<Long>> merged = TreeMerger.mergeKeepLeft(
                    new ArrayList<>(),
                    new ArrayList<>(List.of(right1)));

            assertThat(merged).hasSize(1);
            assertThat(merged.getFirst().getName()).isEqualTo("Right");
        }

        @Test
        @DisplayName("should handle empty right")
        void shouldHandleEmptyRight() {
            DefaultTreeNode<Long> left1 = new DefaultTreeNode<>(1L, 0L, "Left");

            List<DefaultTreeNode<Long>> merged = TreeMerger.mergeKeepLeft(
                    new ArrayList<>(List.of(left1)),
                    new ArrayList<>());

            assertThat(merged).hasSize(1);
            assertThat(merged.getFirst().getName()).isEqualTo("Left");
        }
    }

    @Nested
    @DisplayName("MergeKeepRight Tests")
    class MergeKeepRightTests {

        @Test
        @DisplayName("should keep right node on conflict")
        void shouldKeepRightNodeOnConflict() {
            DefaultTreeNode<Long> left1 = new DefaultTreeNode<>(1L, 0L, "Left-Root");
            DefaultTreeNode<Long> right1 = new DefaultTreeNode<>(1L, 0L, "Right-Root");

            List<DefaultTreeNode<Long>> merged = TreeMerger.mergeKeepRight(
                    new ArrayList<>(List.of(left1)),
                    new ArrayList<>(List.of(right1)));

            assertThat(merged).hasSize(1);
            assertThat(merged.getFirst().getName()).isEqualTo("Right-Root");
        }
    }

    @Nested
    @DisplayName("Merge With Strategy Tests")
    class MergeWithStrategyTests {

        @Test
        @DisplayName("should use custom strategy for conflict resolution")
        void shouldUseCustomStrategy() {
            DefaultTreeNode<Long> left1 = new DefaultTreeNode<>(1L, 0L, "Left");
            DefaultTreeNode<Long> right1 = new DefaultTreeNode<>(1L, 0L, "Right");

            List<DefaultTreeNode<Long>> merged = TreeMerger.merge(
                    new ArrayList<>(List.of(left1)),
                    new ArrayList<>(List.of(right1)),
                    (l, r) -> {
                        l.setName(l.getName() + "+" + r.getName());
                        return l;
                    });

            assertThat(merged).hasSize(1);
            assertThat(merged.getFirst().getName()).isEqualTo("Left+Right");
        }

        @Test
        @DisplayName("should recursively merge children")
        void shouldRecursivelyMergeChildren() {
            DefaultTreeNode<Long> left1 = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> leftChild = new DefaultTreeNode<>(2L, 1L, "Left-Child");
            left1.setChildren(new ArrayList<>(List.of(leftChild)));

            DefaultTreeNode<Long> right1 = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> rightChild = new DefaultTreeNode<>(3L, 1L, "Right-Child");
            right1.setChildren(new ArrayList<>(List.of(rightChild)));

            List<DefaultTreeNode<Long>> merged = TreeMerger.mergeKeepLeft(
                    new ArrayList<>(List.of(left1)),
                    new ArrayList<>(List.of(right1)));

            assertThat(merged).hasSize(1);
            assertThat(merged.getFirst().getChildren()).hasSize(2);
        }

        @Test
        @DisplayName("should throw on null parameters")
        void shouldThrowOnNullParameters() {
            List<DefaultTreeNode<Long>> empty = new ArrayList<>();
            assertThatThrownBy(() -> TreeMerger.merge(null, empty, (l, r) -> l))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> TreeMerger.merge(empty, null, (l, r) -> l))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> TreeMerger.<DefaultTreeNode<Long>, Long>merge(empty, empty, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
