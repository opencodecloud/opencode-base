package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeUtilTest Tests
 * TreeUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeUtil Tests")
class TreeUtilTest {

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
    @DisplayName("FindById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("findById should return matching node")
        void findByIdShouldReturnMatchingNode() {
            Optional<DefaultTreeNode<Long>> result = TreeUtil.findById(roots, 4L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(4L);
        }

        @Test
        @DisplayName("findById should return empty for non-existent id")
        void findByIdShouldReturnEmptyForNonExistentId() {
            Optional<DefaultTreeNode<Long>> result = TreeUtil.findById(roots, 99L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findById should find root node")
        void findByIdShouldFindRootNode() {
            Optional<DefaultTreeNode<Long>> result = TreeUtil.findById(roots, 1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Find Tests")
    class FindTests {

        @Test
        @DisplayName("find should return first matching node")
        void findShouldReturnFirstMatchingNode() {
            Optional<DefaultTreeNode<Long>> result = TreeUtil.find(roots,
                node -> node.getName().startsWith("Node"));

            assertThat(result).isPresent();
            assertThat(result.get().getName()).startsWith("Node");
        }

        @Test
        @DisplayName("find should return empty when no match")
        void findShouldReturnEmptyWhenNoMatch() {
            Optional<DefaultTreeNode<Long>> result = TreeUtil.find(roots,
                node -> "NotExist".equals(node.getName()));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("FindAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("findAll should return all matching nodes")
        void findAllShouldReturnAllMatchingNodes() {
            List<DefaultTreeNode<Long>> result = TreeUtil.findAll(roots,
                node -> node.getId() > 2L);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("findAll should return empty when no matches")
        void findAllShouldReturnEmptyWhenNoMatches() {
            List<DefaultTreeNode<Long>> result = TreeUtil.findAll(roots,
                node -> node.getId() > 100L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Flatten Tests")
    class FlattenTests {

        @Test
        @DisplayName("flatten should return all nodes in list")
        void flattenShouldReturnAllNodesInList() {
            List<DefaultTreeNode<Long>> result = TreeUtil.flatten(roots);

            assertThat(result).hasSize(5);
        }

        @Test
        @DisplayName("flatten should handle empty list")
        void flattenShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();
            List<DefaultTreeNode<Long>> result = TreeUtil.flatten(emptyList);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {

        @Test
        @DisplayName("count should return total node count")
        void countShouldReturnTotalNodeCount() {
            int count = TreeUtil.count(roots);

            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("count should return 0 for empty list")
        void countShouldReturn0ForEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();
            int count = TreeUtil.count(emptyList);

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("GetMaxDepth Tests")
    class GetMaxDepthTests {

        @Test
        @DisplayName("getMaxDepth should return maximum depth")
        void getMaxDepthShouldReturnMaximumDepth() {
            int maxDepth = TreeUtil.getMaxDepth(roots);

            assertThat(maxDepth).isEqualTo(2);
        }

        @Test
        @DisplayName("getMaxDepth should return 0 for single node")
        void getMaxDepthShouldReturn0ForSingleNode() {
            DefaultTreeNode<Long> single = new DefaultTreeNode<>(1L, 0L, "Single");
            int maxDepth = TreeUtil.getMaxDepth(List.of(single));

            assertThat(maxDepth).isZero();
        }

        @Test
        @DisplayName("getMaxDepth should return 0 for empty list")
        void getMaxDepthShouldReturn0ForEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();
            int maxDepth = TreeUtil.getMaxDepth(emptyList);

            assertThat(maxDepth).isZero();
        }
    }

    @Nested
    @DisplayName("GetLeaves Tests")
    class GetLeavesTests {

        @Test
        @DisplayName("getLeaves should return all leaf nodes")
        void getLeavesShouldReturnAllLeafNodes() {
            List<DefaultTreeNode<Long>> leaves = TreeUtil.getLeaves(roots);

            assertThat(leaves).hasSize(3);
            assertThat(leaves).extracting(DefaultTreeNode::getId)
                .containsExactlyInAnyOrder(3L, 4L, 5L);
        }

        @Test
        @DisplayName("getLeaves should return single node if it's leaf")
        void getLeavesShouldReturnSingleNodeIfItsLeaf() {
            DefaultTreeNode<Long> leaf = new DefaultTreeNode<>(1L, 0L, "Leaf");
            List<DefaultTreeNode<Long>> leaves = TreeUtil.getLeaves(List.of(leaf));

            assertThat(leaves).containsExactly(leaf);
        }
    }

    @Nested
    @DisplayName("Contains Tests")
    class ContainsTests {

        @Test
        @DisplayName("contains should return true for existing id")
        void containsShouldReturnTrueForExistingId() {
            boolean result = TreeUtil.contains(roots, 3L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("contains should return false for non-existent id")
        void containsShouldReturnFalseForNonExistentId() {
            boolean result = TreeUtil.contains(roots, 99L);

            assertThat(result).isFalse();
        }
    }
}
