package cloud.opencode.base.tree.traversal;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * DepthLimitedTraversalTest Tests
 * DepthLimitedTraversalTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("DepthLimitedTraversal Tests")
class DepthLimitedTraversalTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor should set max depth")
        void constructorShouldSetMaxDepth() {
            DepthLimitedTraversal traversal = new DepthLimitedTraversal(5);

            assertThat(traversal.getMaxDepth()).isEqualTo(5);
        }

        @Test
        @DisplayName("of should create traversal with max depth")
        void ofShouldCreateTraversalWithMaxDepth() {
            DepthLimitedTraversal traversal = DepthLimitedTraversal.of(3);

            assertThat(traversal.getMaxDepth()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Traverse Tests")
    class TraverseTests {

        @Test
        @DisplayName("traverse should visit nodes up to max depth")
        void traverseShouldVisitNodesUpToMaxDepth() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            DefaultTreeNode<Long> grandchild = new DefaultTreeNode<>(3L, 2L, "Grandchild");
            DefaultTreeNode<Long> greatGrandchild = new DefaultTreeNode<>(4L, 3L, "GreatGrandchild");
            grandchild.setChildren(new ArrayList<>(List.of(greatGrandchild)));
            child.setChildren(new ArrayList<>(List.of(grandchild)));
            root.setChildren(new ArrayList<>(List.of(child)));

            DepthLimitedTraversal traversal = DepthLimitedTraversal.of(2);
            List<Long> visited = new ArrayList<>();

            traversal.traverse(List.of(root), (node, depth) -> {
                visited.add(node.getId());
                return true;
            });

            // Should visit root (depth 0), child (depth 1), grandchild (depth 2)
            // Should NOT visit greatGrandchild (depth 3)
            assertThat(visited).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("traverse with depth 0 should visit only root")
        void traverseWithDepth0ShouldVisitOnlyRoot() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            DepthLimitedTraversal traversal = DepthLimitedTraversal.of(0);
            List<Long> visited = new ArrayList<>();

            traversal.traverse(List.of(root), (node, depth) -> {
                visited.add(node.getId());
                return true;
            });

            assertThat(visited).containsExactly(1L);
        }

        @Test
        @DisplayName("traverse should stop when visitor returns false")
        void traverseShouldStopWhenVisitorReturnsFalse() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            DepthLimitedTraversal traversal = DepthLimitedTraversal.of(10);
            List<Long> visited = new ArrayList<>();

            traversal.traverse(List.of(root), (node, depth) -> {
                visited.add(node.getId());
                return node.getId() != 2L; // Stop after visiting node 2
            });

            assertThat(visited).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("traverse should handle empty list")
        void traverseShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();
            DepthLimitedTraversal traversal = DepthLimitedTraversal.of(5);
            List<Long> visited = new ArrayList<>();

            traversal.traverse(emptyList, (node, depth) -> {
                visited.add(node.getId());
                return true;
            });

            assertThat(visited).isEmpty();
        }

        @Test
        @DisplayName("traverse should handle multiple roots")
        void traverseShouldHandleMultipleRoots() {
            DefaultTreeNode<Long> root1 = new DefaultTreeNode<>(1L, null, "Root1");
            DefaultTreeNode<Long> root2 = new DefaultTreeNode<>(2L, null, "Root2");

            DepthLimitedTraversal traversal = DepthLimitedTraversal.of(5);
            List<Long> visited = new ArrayList<>();

            traversal.traverse(List.of(root1, root2), (node, depth) -> {
                visited.add(node.getId());
                return true;
            });

            assertThat(visited).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("traverse should provide correct depth to visitor")
        void traverseShouldProvideCorrectDepthToVisitor() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            DefaultTreeNode<Long> grandchild = new DefaultTreeNode<>(3L, 2L, "Grandchild");
            child.setChildren(new ArrayList<>(List.of(grandchild)));
            root.setChildren(new ArrayList<>(List.of(child)));

            DepthLimitedTraversal traversal = DepthLimitedTraversal.of(10);
            List<Integer> depths = new ArrayList<>();

            traversal.traverse(List.of(root), (node, depth) -> {
                depths.add(depth);
                return true;
            });

            assertThat(depths).containsExactly(0, 1, 2);
        }
    }
}
