package cloud.opencode.base.tree.traversal;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * TraversalTest Tests
 * TraversalTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("Traversal Tests")
class TraversalTest {

    private List<DefaultTreeNode<Long>> roots;
    private DefaultTreeNode<Long> root;

    @BeforeEach
    void setUp() {
        // Build tree:
        //       1
        //      / \
        //     2   3
        //    / \
        //   4   5
        root = new DefaultTreeNode<>(1L, 0L, "Root");
        DefaultTreeNode<Long> node2 = new DefaultTreeNode<>(2L, 1L, "Node2");
        DefaultTreeNode<Long> node3 = new DefaultTreeNode<>(3L, 1L, "Node3");
        DefaultTreeNode<Long> node4 = new DefaultTreeNode<>(4L, 2L, "Node4");
        DefaultTreeNode<Long> node5 = new DefaultTreeNode<>(5L, 2L, "Node5");

        node2.setChildren(new ArrayList<>(List.of(node4, node5)));
        root.setChildren(new ArrayList<>(List.of(node2, node3)));
        roots = List.of(root);
    }

    @Nested
    @DisplayName("PreOrderTraversal Tests")
    class PreOrderTraversalTests {

        @Test
        @DisplayName("getInstance should return singleton")
        void getInstanceShouldReturnSingleton() {
            PreOrderTraversal instance1 = PreOrderTraversal.getInstance();
            PreOrderTraversal instance2 = PreOrderTraversal.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("traverse should visit nodes in pre-order")
        void traverseShouldVisitNodesInPreOrder() {
            List<Long> visited = new ArrayList<>();
            PreOrderTraversal.getInstance().traverse(roots,
                TreeVisitor.of(node -> visited.add(node.getId())));

            assertThat(visited).containsExactly(1L, 2L, 4L, 5L, 3L);
        }

        @Test
        @DisplayName("traverse should support early termination")
        void traverseShouldSupportEarlyTermination() {
            List<Long> visited = new ArrayList<>();
            PreOrderTraversal.getInstance().traverse(roots, (node, depth) -> {
                visited.add(node.getId());
                return node.getId() != 2L; // Stop after visiting node 2
            });

            assertThat(visited).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("collect should return all nodes in pre-order")
        void collectShouldReturnAllNodesInPreOrder() {
            List<DefaultTreeNode<Long>> collected = PreOrderTraversal.getInstance().collect(roots);

            assertThat(collected).hasSize(5);
            assertThat(collected.get(0).getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("PostOrderTraversal Tests")
    class PostOrderTraversalTests {

        @Test
        @DisplayName("getInstance should return singleton")
        void getInstanceShouldReturnSingleton() {
            PostOrderTraversal instance1 = PostOrderTraversal.getInstance();
            PostOrderTraversal instance2 = PostOrderTraversal.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("traverse should visit nodes in post-order")
        void traverseShouldVisitNodesInPostOrder() {
            List<Long> visited = new ArrayList<>();
            PostOrderTraversal.getInstance().traverse(roots,
                TreeVisitor.of(node -> visited.add(node.getId())));

            assertThat(visited).containsExactly(4L, 5L, 2L, 3L, 1L);
        }
    }

    @Nested
    @DisplayName("LevelOrderTraversal Tests")
    class LevelOrderTraversalTests {

        @Test
        @DisplayName("getInstance should return singleton")
        void getInstanceShouldReturnSingleton() {
            LevelOrderTraversal instance1 = LevelOrderTraversal.getInstance();
            LevelOrderTraversal instance2 = LevelOrderTraversal.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("traverse should visit nodes level by level")
        void traverseShouldVisitNodesLevelByLevel() {
            List<Long> visited = new ArrayList<>();
            LevelOrderTraversal.getInstance().traverse(roots,
                TreeVisitor.of(node -> visited.add(node.getId())));

            assertThat(visited).containsExactly(1L, 2L, 3L, 4L, 5L);
        }

        @Test
        @DisplayName("traverse should handle empty list")
        void traverseShouldHandleEmptyList() {
            List<Long> visited = new ArrayList<>();
            List<DefaultTreeNode<Long>> emptyList = List.of();
            LevelOrderTraversal.getInstance().traverse(emptyList,
                TreeVisitor.of(node -> visited.add(node.getId())));

            assertThat(visited).isEmpty();
        }

        @Test
        @DisplayName("traverse should handle null list")
        void traverseShouldHandleNullList() {
            List<Long> visited = new ArrayList<>();
            List<DefaultTreeNode<Long>> nullList = null;
            LevelOrderTraversal.getInstance().traverse(nullList,
                TreeVisitor.of(node -> visited.add(node.getId())));

            assertThat(visited).isEmpty();
        }
    }

    @Nested
    @DisplayName("DepthLimitedTraversal Tests")
    class DepthLimitedTraversalTests {

        @Test
        @DisplayName("of should create instance with max depth")
        void ofShouldCreateInstanceWithMaxDepth() {
            DepthLimitedTraversal traversal = DepthLimitedTraversal.of(2);

            assertThat(traversal.getMaxDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("traverse should stop at max depth")
        void traverseShouldStopAtMaxDepth() {
            List<Long> visited = new ArrayList<>();
            DepthLimitedTraversal.of(1).traverse(roots,
                TreeVisitor.of(node -> visited.add(node.getId())));

            assertThat(visited).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("traverse with depth 0 should visit only root")
        void traverseWithDepth0ShouldVisitOnlyRoot() {
            List<Long> visited = new ArrayList<>();
            DepthLimitedTraversal.of(0).traverse(roots,
                TreeVisitor.of(node -> visited.add(node.getId())));

            assertThat(visited).containsExactly(1L);
        }

        @Test
        @DisplayName("getMaxDepth should return configured depth")
        void getMaxDepthShouldReturnConfiguredDepth() {
            DepthLimitedTraversal traversal = new DepthLimitedTraversal(5);

            assertThat(traversal.getMaxDepth()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("IterativeTraversal Tests")
    class IterativeTraversalTests {

        @Test
        @DisplayName("getInstance should return singleton")
        void getInstanceShouldReturnSingleton() {
            IterativeTraversal instance1 = IterativeTraversal.getInstance();
            IterativeTraversal instance2 = IterativeTraversal.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("traverse should visit nodes in pre-order")
        void traverseShouldVisitNodesInPreOrder() {
            List<Long> visited = new ArrayList<>();
            IterativeTraversal.getInstance().traverse(roots,
                TreeVisitor.of(node -> visited.add(node.getId())));

            assertThat(visited).containsExactly(1L, 2L, 4L, 5L, 3L);
        }

        @Test
        @DisplayName("traverse should handle empty list")
        void traverseShouldHandleEmptyList() {
            List<Long> visited = new ArrayList<>();
            List<DefaultTreeNode<Long>> emptyList = List.of();
            IterativeTraversal.getInstance().traverse(emptyList,
                TreeVisitor.of(node -> visited.add(node.getId())));

            assertThat(visited).isEmpty();
        }

        @Test
        @DisplayName("traverse should handle null list")
        void traverseShouldHandleNullList() {
            List<Long> visited = new ArrayList<>();
            List<DefaultTreeNode<Long>> nullList = null;
            IterativeTraversal.getInstance().traverse(nullList,
                TreeVisitor.of(node -> visited.add(node.getId())));

            assertThat(visited).isEmpty();
        }
    }

    @Nested
    @DisplayName("TreeVisitor Tests")
    class TreeVisitorTests {

        @Test
        @DisplayName("of should create visitor from consumer")
        void ofShouldCreateVisitorFromConsumer() {
            List<Long> visited = new ArrayList<>();
            TreeVisitor<DefaultTreeNode<Long>> visitor = TreeVisitor.of(
                node -> visited.add(node.getId())
            );

            boolean result = visitor.visit(root, 0);

            assertThat(result).isTrue();
            assertThat(visited).containsExactly(1L);
        }

        @Test
        @DisplayName("withDepth should create visitor with depth consumer")
        void withDepthShouldCreateVisitorWithDepthConsumer() {
            List<String> visited = new ArrayList<>();
            TreeVisitor<DefaultTreeNode<Long>> visitor = TreeVisitor.withDepth(
                (node, depth) -> visited.add(node.getId() + "@" + depth)
            );

            visitor.visit(root, 0);

            assertThat(visited).containsExactly("1@0");
        }
    }

    @Nested
    @DisplayName("TreeTraversal Interface Tests")
    class TreeTraversalInterfaceTests {

        @Test
        @DisplayName("traverse with consumer should delegate to visitor")
        void traverseWithConsumerShouldDelegateToVisitor() {
            List<Long> visited = new ArrayList<>();
            PreOrderTraversal.getInstance().traverse(roots, node -> visited.add(node.getId()));

            assertThat(visited).containsExactly(1L, 2L, 4L, 5L, 3L);
        }
    }
}
