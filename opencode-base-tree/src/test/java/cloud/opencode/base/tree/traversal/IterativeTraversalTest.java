package cloud.opencode.base.tree.traversal;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * IterativeTraversalTest Tests
 * IterativeTraversalTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("IterativeTraversal Tests")
class IterativeTraversalTest {

    private IterativeTraversal traversal;

    @BeforeEach
    void setUp() {
        traversal = IterativeTraversal.getInstance();
    }

    @Nested
    @DisplayName("Singleton Tests")
    class SingletonTests {

        @Test
        @DisplayName("getInstance should return same instance")
        void getInstanceShouldReturnSameInstance() {
            assertThat(IterativeTraversal.getInstance()).isSameAs(traversal);
        }
    }

    @Nested
    @DisplayName("Traverse Tests")
    class TraverseTests {

        @Test
        @DisplayName("should traverse in pre-order using iterative approach")
        void shouldTraverseInPreOrder() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            List<Long> visited = new ArrayList<>();
            traversal.traverse(List.of(root), (node, depth) -> {
                visited.add(node.getId());
                return true;
            });

            // Pre-order: root, child1, child2
            assertThat(visited).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("should traverse deeply nested tree")
        void shouldTraverseDeeplyNestedTree() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            DefaultTreeNode<Long> grandchild = new DefaultTreeNode<>(3L, 2L, "Grandchild");
            child.setChildren(new ArrayList<>(List.of(grandchild)));
            root.setChildren(new ArrayList<>(List.of(child)));

            List<Long> visited = new ArrayList<>();
            traversal.traverse(List.of(root), (node, depth) -> {
                visited.add(node.getId());
                return true;
            });

            assertThat(visited).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("should handle null roots")
        void shouldHandleNullRoots() {
            List<Long> visited = new ArrayList<>();
            traversal.traverse(null, (DefaultTreeNode<Long> node, int depth) -> {
                visited.add(node.getId());
                return true;
            });
            assertThat(visited).isEmpty();
        }

        @Test
        @DisplayName("should handle empty roots list")
        void shouldHandleEmptyRoots() {
            List<Long> visited = new ArrayList<>();
            traversal.traverse(List.of(), (DefaultTreeNode<Long> node, int depth) -> {
                visited.add(node.getId());
                return true;
            });
            assertThat(visited).isEmpty();
        }

        @Test
        @DisplayName("should handle multiple roots")
        void shouldHandleMultipleRoots() {
            DefaultTreeNode<Long> root1 = new DefaultTreeNode<>(1L, null, "Root1");
            DefaultTreeNode<Long> root2 = new DefaultTreeNode<>(2L, null, "Root2");

            List<Long> visited = new ArrayList<>();
            traversal.traverse(List.of(root1, root2), (node, depth) -> {
                visited.add(node.getId());
                return true;
            });

            assertThat(visited).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("should stop when visitor returns false")
        void shouldStopWhenVisitorReturnsFalse() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            List<Long> visited = new ArrayList<>();
            traversal.traverse(List.of(root), (node, depth) -> {
                visited.add(node.getId());
                return node.getId() != 1L; // stop immediately after root
            });

            assertThat(visited).containsExactly(1L);
        }

        @Test
        @DisplayName("should provide correct depth values")
        void shouldProvideCorrectDepthValues() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            DefaultTreeNode<Long> grandchild = new DefaultTreeNode<>(3L, 2L, "Grandchild");
            child.setChildren(new ArrayList<>(List.of(grandchild)));
            root.setChildren(new ArrayList<>(List.of(child)));

            List<Integer> depths = new ArrayList<>();
            traversal.traverse(List.of(root), (node, depth) -> {
                depths.add(depth);
                return true;
            });

            assertThat(depths).containsExactly(0, 1, 2);
        }
    }
}
