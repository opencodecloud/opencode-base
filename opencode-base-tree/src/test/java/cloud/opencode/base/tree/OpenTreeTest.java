package cloud.opencode.base.tree;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenTreeTest Tests
 * OpenTreeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("OpenTree Tests")
class OpenTreeTest {

    @Nested
    @DisplayName("Node Factory Tests")
    class NodeFactoryTests {

        @Test
        @DisplayName("node should create TreeNode with data")
        void nodeShouldCreateTreeNodeWithData() {
            TreeNode<String> node = OpenTree.node("data");

            assertThat(node.getData()).isEqualTo("data");
            assertThat(node.getChildren()).isEmpty();
        }
    }

    @Nested
    @DisplayName("BuildTree Tests")
    class BuildTreeTests {

        @Test
        @DisplayName("buildTree should create tree from flat list")
        void buildTreeShouldCreateTreeFromFlatList() {
            List<DefaultTreeNode<Long>> nodes = Arrays.asList(
                new DefaultTreeNode<>(1L, 0L, "Root"),
                new DefaultTreeNode<>(2L, 1L, "Child1"),
                new DefaultTreeNode<>(3L, 1L, "Child2")
            );

            List<DefaultTreeNode<Long>> roots = OpenTree.buildTree(nodes);

            assertThat(roots).hasSize(1);
            assertThat(roots.get(0).getChildren()).hasSize(2);
        }

        @Test
        @DisplayName("buildTree with rootId should use specified root")
        void buildTreeWithRootIdShouldUseSpecifiedRoot() {
            List<DefaultTreeNode<Long>> nodes = Arrays.asList(
                new DefaultTreeNode<>(1L, 10L, "Root"),
                new DefaultTreeNode<>(2L, 1L, "Child")
            );

            List<DefaultTreeNode<Long>> roots = OpenTree.buildTree(nodes, 10L);

            assertThat(roots).hasSize(1);
            assertThat(roots.get(0).getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("FlattenTree Tests")
    class FlattenTreeTests {

        @Test
        @DisplayName("flattenTree should convert tree to flat list")
        void flattenTreeShouldConvertTreeToFlatList() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            List<DefaultTreeNode<Long>> flat = OpenTree.flattenTree(List.of(root));

            assertThat(flat).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Traversal Tests")
    class TraversalTests {

        @Test
        @DisplayName("traversePreOrder should visit in pre-order")
        void traversePreOrderShouldVisitInPreOrder() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            List<Long> visited = new ArrayList<>();
            OpenTree.traversePreOrder(List.of(root), node -> visited.add(node.getId()));

            assertThat(visited).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("traversePostOrder should visit in post-order")
        void traversePostOrderShouldVisitInPostOrder() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            List<Long> visited = new ArrayList<>();
            OpenTree.traversePostOrder(List.of(root), node -> visited.add(node.getId()));

            assertThat(visited).containsExactly(2L, 1L);
        }

        @Test
        @DisplayName("traverseBreadthFirst should visit level by level")
        void traverseBreadthFirstShouldVisitLevelByLevel() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            List<Long> visited = new ArrayList<>();
            OpenTree.traverseBreadthFirst(List.of(root), node -> visited.add(node.getId()));

            assertThat(visited).containsExactly(1L, 2L, 3L);
        }
    }

    @Nested
    @DisplayName("Find Tests")
    class FindTests {

        @Test
        @DisplayName("find should return matching node")
        void findShouldReturnMatchingNode() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Target");
            root.setChildren(new ArrayList<>(List.of(child)));

            DefaultTreeNode<Long> result = OpenTree.find(List.of(root), 2L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Target");
        }

        @Test
        @DisplayName("findAll should return all matching nodes")
        void findAllShouldReturnAllMatchingNodes() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Match");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Match");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            List<DefaultTreeNode<Long>> result = OpenTree.findAll(List.of(root),
                node -> "Match".equals(node.getName()));

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Path Tests")
    class PathTests {

        @Test
        @DisplayName("getPath should return path to node")
        void getPathShouldReturnPathToNode() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            DefaultTreeNode<Long> grandchild = new DefaultTreeNode<>(3L, 2L, "Target");
            child.setChildren(new ArrayList<>(List.of(grandchild)));
            root.setChildren(new ArrayList<>(List.of(child)));

            List<DefaultTreeNode<Long>> path = OpenTree.getPath(List.of(root), 3L);

            assertThat(path).hasSize(3);
            assertThat(path.get(0).getId()).isEqualTo(1L);
            assertThat(path.get(2).getId()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("Leaf Tests")
    class LeafTests {

        @Test
        @DisplayName("getLeaves should return all leaf nodes")
        void getLeavesShouldReturnAllLeafNodes() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Leaf1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Leaf2");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            List<DefaultTreeNode<Long>> leaves = OpenTree.getLeaves(List.of(root));

            assertThat(leaves).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("filter should keep matching nodes with ancestors")
        void filterShouldKeepMatchingNodesWithAncestors() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Target");
            root.setChildren(new ArrayList<>(List.of(child)));

            List<DefaultTreeNode<Long>> filtered = OpenTree.filter(List.of(root),
                node -> "Target".equals(node.getName()));

            assertThat(filtered).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Depth and Size Tests")
    class DepthAndSizeTests {

        @Test
        @DisplayName("depth should return maximum depth")
        void depthShouldReturnMaximumDepth() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            DefaultTreeNode<Long> grandchild = new DefaultTreeNode<>(3L, 2L, "Grandchild");
            child.setChildren(new ArrayList<>(List.of(grandchild)));
            root.setChildren(new ArrayList<>(List.of(child)));

            int depth = OpenTree.depth(List.of(root));

            assertThat(depth).isEqualTo(3);
        }

        @Test
        @DisplayName("size should return total node count")
        void sizeShouldReturnTotalNodeCount() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, 0L, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            int size = OpenTree.size(List.of(root));

            assertThat(size).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Sort Tests")
    class SortTests {

        @Test
        @DisplayName("sortTree should sort nodes by comparator")
        void sortTreeShouldSortNodesByComparator() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(3L, 1L, "C");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(2L, 1L, "A");
            // Use ArrayList to allow in-place sorting
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            // Use mutable list for sortTree
            List<DefaultTreeNode<Long>> rootList = new ArrayList<>();
            rootList.add(root);
            OpenTree.sortTree(rootList, Comparator.comparing(DefaultTreeNode::getName));

            assertThat(root.getChildren().get(0).getName()).isEqualTo("A");
            assertThat(root.getChildren().get(1).getName()).isEqualTo("C");
        }
    }
}
