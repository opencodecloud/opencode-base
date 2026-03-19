package cloud.opencode.base.tree.path;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * PathFinderTest Tests
 * PathFinderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("PathFinder Tests")
class PathFinderTest {

    private List<DefaultTreeNode<Long>> roots;
    private DefaultTreeNode<Long> root;

    @BeforeEach
    void setUp() {
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
    @DisplayName("FindPathById Tests")
    class FindPathByIdTests {

        @Test
        @DisplayName("findPathById should return path to node")
        void findPathByIdShouldReturnPathToNode() {
            Optional<TreePath<DefaultTreeNode<Long>>> path = PathFinder.findPathById(roots, 4L);

            assertThat(path).isPresent();
            assertThat(path.get().length()).isEqualTo(3);
            assertThat(path.get().getRoot().getId()).isEqualTo(1L);
            assertThat(path.get().getTarget().getId()).isEqualTo(4L);
        }

        @Test
        @DisplayName("findPathById should return empty for non-existent id")
        void findPathByIdShouldReturnEmptyForNonExistentId() {
            Optional<TreePath<DefaultTreeNode<Long>>> path = PathFinder.findPathById(roots, 99L);

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("findPathById should find root node")
        void findPathByIdShouldFindRootNode() {
            Optional<TreePath<DefaultTreeNode<Long>>> path = PathFinder.findPathById(roots, 1L);

            assertThat(path).isPresent();
            assertThat(path.get().length()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("FindPath Tests")
    class FindPathTests {

        @Test
        @DisplayName("findPath should return path to matching node")
        void findPathShouldReturnPathToMatchingNode() {
            Optional<TreePath<DefaultTreeNode<Long>>> path = PathFinder.findPath(roots,
                node -> "Node5".equals(node.getName()));

            assertThat(path).isPresent();
            assertThat(path.get().getTarget().getName()).isEqualTo("Node5");
        }

        @Test
        @DisplayName("findPath should return empty when no match")
        void findPathShouldReturnEmptyWhenNoMatch() {
            Optional<TreePath<DefaultTreeNode<Long>>> path = PathFinder.findPath(roots,
                node -> "NotExist".equals(node.getName()));

            assertThat(path).isEmpty();
        }
    }

    @Nested
    @DisplayName("FindAllPaths Tests")
    class FindAllPathsTests {

        @Test
        @DisplayName("findAllPaths should return all matching paths")
        void findAllPathsShouldReturnAllMatchingPaths() {
            List<TreePath<DefaultTreeNode<Long>>> paths = PathFinder.findAllPaths(roots,
                node -> node.getId() > 3L);

            assertThat(paths).hasSize(2);
        }

        @Test
        @DisplayName("findAllPaths should return empty for no matches")
        void findAllPathsShouldReturnEmptyForNoMatches() {
            List<TreePath<DefaultTreeNode<Long>>> paths = PathFinder.findAllPaths(roots,
                node -> node.getId() > 100L);

            assertThat(paths).isEmpty();
        }
    }

    @Nested
    @DisplayName("FindAllLeafPaths Tests")
    class FindAllLeafPathsTests {

        @Test
        @DisplayName("findAllLeafPaths should return paths to all leaves")
        void findAllLeafPathsShouldReturnPathsToAllLeaves() {
            List<TreePath<DefaultTreeNode<Long>>> paths = PathFinder.findAllLeafPaths(roots);

            assertThat(paths).hasSize(3);
            for (TreePath<DefaultTreeNode<Long>> path : paths) {
                DefaultTreeNode<Long> target = path.getTarget();
                assertThat(target.getChildren() == null || target.getChildren().isEmpty()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("GetAncestorIds Tests")
    class GetAncestorIdsTests {

        @Test
        @DisplayName("getAncestorIds should return ancestor ids excluding target")
        void getAncestorIdsShouldReturnAncestorIdsExcludingTarget() {
            List<Long> ancestors = PathFinder.getAncestorIds(roots, 4L);

            assertThat(ancestors).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("getAncestorIds should return empty for root")
        void getAncestorIdsShouldReturnEmptyForRoot() {
            List<Long> ancestors = PathFinder.getAncestorIds(roots, 1L);

            assertThat(ancestors).isEmpty();
        }

        @Test
        @DisplayName("getAncestorIds should return empty for non-existent id")
        void getAncestorIdsShouldReturnEmptyForNonExistentId() {
            List<Long> ancestors = PathFinder.getAncestorIds(roots, 99L);

            assertThat(ancestors).isEmpty();
        }
    }

    @Nested
    @DisplayName("GetDepth Tests")
    class GetDepthTests {

        @Test
        @DisplayName("getDepth should return depth of node")
        void getDepthShouldReturnDepthOfNode() {
            int depth = PathFinder.getDepth(roots, 4L);

            assertThat(depth).isEqualTo(2);
        }

        @Test
        @DisplayName("getDepth should return 0 for root")
        void getDepthShouldReturn0ForRoot() {
            int depth = PathFinder.getDepth(roots, 1L);

            assertThat(depth).isZero();
        }

        @Test
        @DisplayName("getDepth should return -1 for non-existent id")
        void getDepthShouldReturnNegative1ForNonExistentId() {
            int depth = PathFinder.getDepth(roots, 99L);

            assertThat(depth).isEqualTo(-1);
        }
    }
}
