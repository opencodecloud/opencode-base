package cloud.opencode.base.tree.diff;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeDiffTest Tests
 * TreeDiffTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeDiff Tests")
class TreeDiffTest {

    @Nested
    @DisplayName("Diff Tests")
    class DiffTests {

        @Test
        @DisplayName("diff should detect added nodes")
        void diffShouldDetectAddedNodes() {
            List<DefaultTreeNode<Long>> oldTree = createTree(1L, 2L);
            List<DefaultTreeNode<Long>> newTree = createTree(1L, 2L, 3L);

            TreeDiffResult<DefaultTreeNode<Long>> result = TreeDiff.diff(oldTree, newTree);

            assertThat(result.added()).hasSize(1);
            assertThat(result.added().get(0).getId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("diff should detect removed nodes")
        void diffShouldDetectRemovedNodes() {
            List<DefaultTreeNode<Long>> oldTree = createTree(1L, 2L, 3L);
            List<DefaultTreeNode<Long>> newTree = createTree(1L, 2L);

            TreeDiffResult<DefaultTreeNode<Long>> result = TreeDiff.diff(oldTree, newTree);

            assertThat(result.removed()).hasSize(1);
            assertThat(result.removed().get(0).getId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("diff should detect unchanged nodes with same instances")
        void diffShouldDetectUnchangedNodes() {
            // diff() uses Objects::equals which compares references
            // To detect unchanged, we need to use diffByKey with proper equality
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            List<DefaultTreeNode<Long>> tree = List.of(root);

            // Using diffByKey with ID equality to detect unchanged by ID
            TreeDiffResult<DefaultTreeNode<Long>> result = TreeDiff.diffByKey(
                tree, tree,
                DefaultTreeNode::getId,
                (o, n) -> o.getId().equals(n.getId())
            );

            assertThat(result.unchanged()).hasSize(2);
        }

        @Test
        @DisplayName("diff should handle empty old tree")
        void diffShouldHandleEmptyOldTree() {
            List<DefaultTreeNode<Long>> oldTree = List.of();
            List<DefaultTreeNode<Long>> newTree = createTree(1L, 2L);

            TreeDiffResult<DefaultTreeNode<Long>> result = TreeDiff.diff(oldTree, newTree);

            assertThat(result.added()).hasSize(2);
            assertThat(result.removed()).isEmpty();
        }

        @Test
        @DisplayName("diff should handle empty new tree")
        void diffShouldHandleEmptyNewTree() {
            List<DefaultTreeNode<Long>> oldTree = createTree(1L, 2L);
            List<DefaultTreeNode<Long>> newTree = List.of();

            TreeDiffResult<DefaultTreeNode<Long>> result = TreeDiff.diff(oldTree, newTree);

            assertThat(result.removed()).hasSize(2);
            assertThat(result.added()).isEmpty();
        }

        @Test
        @DisplayName("diff should handle both empty trees")
        void diffShouldHandleBothEmptyTrees() {
            List<DefaultTreeNode<Long>> oldTree = List.of();
            List<DefaultTreeNode<Long>> newTree = List.of();

            TreeDiffResult<DefaultTreeNode<Long>> result = TreeDiff.diff(oldTree, newTree);

            assertThat(result.isEqual()).isTrue();
        }
    }

    @Nested
    @DisplayName("DiffByKey Tests")
    class DiffByKeyTests {

        @Test
        @DisplayName("diffByKey should detect modified nodes")
        void diffByKeyShouldDetectModifiedNodes() {
            DefaultTreeNode<Long> oldNode = new DefaultTreeNode<>(1L, null, "OldName");
            DefaultTreeNode<Long> newNode = new DefaultTreeNode<>(1L, null, "NewName");

            List<DefaultTreeNode<Long>> oldTree = List.of(oldNode);
            List<DefaultTreeNode<Long>> newTree = List.of(newNode);

            // equalityChecker returns true when nodes are EQUAL
            // So return true when names match, false when they differ (triggers modified)
            TreeDiffResult<DefaultTreeNode<Long>> result = TreeDiff.diffByKey(
                oldTree, newTree,
                DefaultTreeNode::getId,
                (o, n) -> o.getName().equals(n.getName())
            );

            assertThat(result.modified()).hasSize(1);
            assertThat(result.modified().get(0).oldNode().getName()).isEqualTo("OldName");
            assertThat(result.modified().get(0).newNode().getName()).isEqualTo("NewName");
        }

        @Test
        @DisplayName("diffByKey should detect no changes when equal")
        void diffByKeyShouldDetectNoChangesWhenEqual() {
            DefaultTreeNode<Long> oldNode = new DefaultTreeNode<>(1L, null, "Same");
            DefaultTreeNode<Long> newNode = new DefaultTreeNode<>(1L, null, "Same");

            List<DefaultTreeNode<Long>> oldTree = List.of(oldNode);
            List<DefaultTreeNode<Long>> newTree = List.of(newNode);

            // equalityChecker returns true when nodes are EQUAL
            TreeDiffResult<DefaultTreeNode<Long>> result = TreeDiff.diffByKey(
                oldTree, newTree,
                DefaultTreeNode::getId,
                (o, n) -> o.getName().equals(n.getName())
            );

            assertThat(result.isEqual()).isTrue();
        }
    }

    private List<DefaultTreeNode<Long>> createTree(Long... ids) {
        if (ids.length == 0) return List.of();

        DefaultTreeNode<Long> root = new DefaultTreeNode<>(ids[0], 0L, "Node" + ids[0]);
        List<DefaultTreeNode<Long>> children = new ArrayList<>();

        for (int i = 1; i < ids.length; i++) {
            children.add(new DefaultTreeNode<>(ids[i], ids[0], "Node" + ids[i]));
        }

        if (!children.isEmpty()) {
            root.setChildren(children);
        }

        return List.of(root);
    }
}
