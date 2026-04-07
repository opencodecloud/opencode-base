package cloud.opencode.base.tree.path;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * LCA (Lowest Common Ancestor) Tests
 * 最近公共祖先测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.3
 */
@DisplayName("Lowest Common Ancestor Tests")
class LCATest {

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

    @Test
    @DisplayName("LCA of siblings should be their parent")
    void lcaOfSiblingsShouldBeParent() {
        Optional<DefaultTreeNode<Long>> lca = PathFinder.findLowestCommonAncestor(roots, 4L, 5L);

        assertThat(lca).isPresent();
        assertThat(lca.get().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("LCA of node and its descendant should be the ancestor node")
    void lcaOfNodeAndDescendantShouldBeAncestor() {
        Optional<DefaultTreeNode<Long>> lca = PathFinder.findLowestCommonAncestor(roots, 2L, 4L);

        assertThat(lca).isPresent();
        assertThat(lca.get().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("LCA of nodes in different subtrees should be root")
    void lcaOfDifferentSubtreesShouldBeRoot() {
        Optional<DefaultTreeNode<Long>> lca = PathFinder.findLowestCommonAncestor(roots, 4L, 3L);

        assertThat(lca).isPresent();
        assertThat(lca.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("LCA of same node should be the node itself")
    void lcaOfSameNodeShouldBeSelf() {
        Optional<DefaultTreeNode<Long>> lca = PathFinder.findLowestCommonAncestor(roots, 3L, 3L);

        assertThat(lca).isPresent();
        assertThat(lca.get().getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("LCA should be empty when one node not found")
    void lcaShouldBeEmptyWhenNodeNotFound() {
        Optional<DefaultTreeNode<Long>> lca = PathFinder.findLowestCommonAncestor(roots, 4L, 99L);

        assertThat(lca).isEmpty();
    }

    @Test
    @DisplayName("LCA should be empty for empty tree")
    void lcaShouldBeEmptyForEmptyTree() {
        List<DefaultTreeNode<Long>> empty = Collections.emptyList();
        Optional<DefaultTreeNode<Long>> lca =
                PathFinder.<DefaultTreeNode<Long>, Long>findLowestCommonAncestor(empty, 1L, 2L);

        assertThat(lca).isEmpty();
    }

    @Test
    @DisplayName("LCA by predicate should work")
    void lcaByPredicateShouldWork() {
        java.util.function.Predicate<DefaultTreeNode<Long>> pred1 = n -> "Node4".equals(n.getName());
        java.util.function.Predicate<DefaultTreeNode<Long>> pred2 = n -> "Node5".equals(n.getName());
        Optional<DefaultTreeNode<Long>> lca = PathFinder.findLowestCommonAncestor(roots, pred1, pred2);

        assertThat(lca).isPresent();
        assertThat(lca.get().getId()).isEqualTo(2L);
    }
}
