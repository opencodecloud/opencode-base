package cloud.opencode.base.tree;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeNodeTest Tests
 * TreeNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeNode Tests")
class TreeNodeTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create node with data")
        void shouldCreateNodeWithData() {
            TreeNode<String> node = new TreeNode<>("root");

            assertThat(node.getData()).isEqualTo("root");
            assertThat(node.getChildren()).isEmpty();
            assertThat(node.getParent()).isNull();
        }

        @Test
        @DisplayName("should create node with null data")
        void shouldCreateNodeWithNullData() {
            TreeNode<String> node = new TreeNode<>(null);

            assertThat(node.getData()).isNull();
        }
    }

    @Nested
    @DisplayName("Child Operations Tests")
    class ChildOperationsTests {

        @Test
        @DisplayName("addChild should add child and set parent")
        void addChildShouldAddChildAndSetParent() {
            TreeNode<String> parent = new TreeNode<>("parent");
            TreeNode<String> child = new TreeNode<>("child");

            parent.addChild(child);

            assertThat(parent.getChildren()).containsExactly(child);
            assertThat(child.getParent()).isEqualTo(parent);
        }

        @Test
        @DisplayName("addChild with data should create and add child")
        void addChildWithDataShouldCreateAndAddChild() {
            TreeNode<String> parent = new TreeNode<>("parent");

            TreeNode<String> child = parent.addChild("child");

            assertThat(parent.getChildren()).containsExactly(child);
            assertThat(child.getData()).isEqualTo("child");
            assertThat(child.getParent()).isEqualTo(parent);
        }

        @Test
        @DisplayName("removeChild should remove child and clear parent")
        void removeChildShouldRemoveChildAndClearParent() {
            TreeNode<String> parent = new TreeNode<>("parent");
            TreeNode<String> child = parent.addChild("child");

            boolean removed = parent.removeChild(child);

            assertThat(removed).isTrue();
            assertThat(parent.getChildren()).isEmpty();
            assertThat(child.getParent()).isNull();
        }

        @Test
        @DisplayName("removeChild should return false for non-child")
        void removeChildShouldReturnFalseForNonChild() {
            TreeNode<String> parent = new TreeNode<>("parent");
            TreeNode<String> other = new TreeNode<>("other");

            boolean removed = parent.removeChild(other);

            assertThat(removed).isFalse();
        }
    }

    @Nested
    @DisplayName("Node Properties Tests")
    class NodePropertiesTests {

        @Test
        @DisplayName("isRoot should return true for node without parent")
        void isRootShouldReturnTrueForNodeWithoutParent() {
            TreeNode<String> node = new TreeNode<>("root");

            assertThat(node.isRoot()).isTrue();
        }

        @Test
        @DisplayName("isRoot should return false for node with parent")
        void isRootShouldReturnFalseForNodeWithParent() {
            TreeNode<String> parent = new TreeNode<>("parent");
            TreeNode<String> child = parent.addChild("child");

            assertThat(child.isRoot()).isFalse();
        }

        @Test
        @DisplayName("isLeaf should return true for node without children")
        void isLeafShouldReturnTrueForNodeWithoutChildren() {
            TreeNode<String> node = new TreeNode<>("leaf");

            assertThat(node.isLeaf()).isTrue();
        }

        @Test
        @DisplayName("isLeaf should return false for node with children")
        void isLeafShouldReturnFalseForNodeWithChildren() {
            TreeNode<String> parent = new TreeNode<>("parent");
            parent.addChild("child");

            assertThat(parent.isLeaf()).isFalse();
        }

        @Test
        @DisplayName("getDepth should return 0 for root")
        void getDepthShouldReturn0ForRoot() {
            TreeNode<String> root = new TreeNode<>("root");

            assertThat(root.getDepth()).isZero();
        }

        @Test
        @DisplayName("getDepth should return correct depth for nested nodes")
        void getDepthShouldReturnCorrectDepthForNestedNodes() {
            TreeNode<String> root = new TreeNode<>("root");
            TreeNode<String> child = root.addChild("child");
            TreeNode<String> grandchild = child.addChild("grandchild");

            assertThat(child.getDepth()).isEqualTo(1);
            assertThat(grandchild.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("getHeight should return 0 for leaf")
        void getHeightShouldReturn0ForLeaf() {
            TreeNode<String> leaf = new TreeNode<>("leaf");

            assertThat(leaf.getHeight()).isZero();
        }

        @Test
        @DisplayName("getHeight should return correct height for tree")
        void getHeightShouldReturnCorrectHeightForTree() {
            TreeNode<String> root = new TreeNode<>("root");
            TreeNode<String> child = root.addChild("child");
            child.addChild("grandchild");

            assertThat(root.getHeight()).isEqualTo(2);
            assertThat(child.getHeight()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Navigation Tests")
    class NavigationTests {

        @Test
        @DisplayName("getRoot should return self for root node")
        void getRootShouldReturnSelfForRootNode() {
            TreeNode<String> root = new TreeNode<>("root");

            assertThat(root.getRoot()).isEqualTo(root);
        }

        @Test
        @DisplayName("getRoot should return root for nested node")
        void getRootShouldReturnRootForNestedNode() {
            TreeNode<String> root = new TreeNode<>("root");
            TreeNode<String> child = root.addChild("child");
            TreeNode<String> grandchild = child.addChild("grandchild");

            assertThat(grandchild.getRoot()).isEqualTo(root);
        }

        @Test
        @DisplayName("getSiblings should return empty for root")
        void getSiblingsShouldReturnEmptyForRoot() {
            TreeNode<String> root = new TreeNode<>("root");

            assertThat(root.getSiblings()).isEmpty();
        }

        @Test
        @DisplayName("getSiblings should return sibling nodes")
        void getSiblingsShouldReturnSiblingNodes() {
            TreeNode<String> parent = new TreeNode<>("parent");
            TreeNode<String> child1 = parent.addChild("child1");
            TreeNode<String> child2 = parent.addChild("child2");
            TreeNode<String> child3 = parent.addChild("child3");

            assertThat(child1.getSiblings()).containsExactly(child2, child3);
            assertThat(child2.getSiblings()).containsExactly(child1, child3);
        }

        @Test
        @DisplayName("getAncestors should return empty for root")
        void getAncestorsShouldReturnEmptyForRoot() {
            TreeNode<String> root = new TreeNode<>("root");

            assertThat(root.getAncestors()).isEmpty();
        }

        @Test
        @DisplayName("getAncestors should return ancestors for nested node")
        void getAncestorsShouldReturnAncestorsForNestedNode() {
            TreeNode<String> root = new TreeNode<>("root");
            TreeNode<String> child = root.addChild("child");
            TreeNode<String> grandchild = child.addChild("grandchild");

            assertThat(grandchild.getAncestors()).containsExactly(child, root);
        }

        @Test
        @DisplayName("getDescendants should return empty for leaf")
        void getDescendantsShouldReturnEmptyForLeaf() {
            TreeNode<String> leaf = new TreeNode<>("leaf");

            assertThat(leaf.getDescendants()).isEmpty();
        }

        @Test
        @DisplayName("getDescendants should return all descendants")
        void getDescendantsShouldReturnAllDescendants() {
            TreeNode<String> root = new TreeNode<>("root");
            TreeNode<String> child1 = root.addChild("child1");
            TreeNode<String> child2 = root.addChild("child2");
            TreeNode<String> grandchild = child1.addChild("grandchild");

            assertThat(root.getDescendants()).containsExactlyInAnyOrder(child1, child2, grandchild);
        }

        @Test
        @DisplayName("getLeaves should return self for leaf node")
        void getLeavesShouldReturnSelfForLeafNode() {
            TreeNode<String> leaf = new TreeNode<>("leaf");

            assertThat(leaf.getLeaves()).containsExactly(leaf);
        }

        @Test
        @DisplayName("getLeaves should return all leaf nodes")
        void getLeavesShouldReturnAllLeafNodes() {
            TreeNode<String> root = new TreeNode<>("root");
            TreeNode<String> child1 = root.addChild("child1");
            TreeNode<String> leaf1 = root.addChild("leaf1");
            TreeNode<String> leaf2 = child1.addChild("leaf2");
            TreeNode<String> leaf3 = child1.addChild("leaf3");

            assertThat(root.getLeaves()).containsExactlyInAnyOrder(leaf1, leaf2, leaf3);
        }
    }

    @Nested
    @DisplayName("Find Operations Tests")
    class FindOperationsTests {

        @Test
        @DisplayName("find should return matching node")
        void findShouldReturnMatchingNode() {
            TreeNode<String> root = new TreeNode<>("root");
            TreeNode<String> child = root.addChild("child");
            TreeNode<String> target = child.addChild("target");

            Optional<TreeNode<String>> result = root.find("target"::equals);

            assertThat(result).contains(target);
        }

        @Test
        @DisplayName("find should return empty when not found")
        void findShouldReturnEmptyWhenNotFound() {
            TreeNode<String> root = new TreeNode<>("root");
            root.addChild("child");

            Optional<TreeNode<String>> result = root.find("notfound"::equals);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findAll should return all matching nodes")
        void findAllShouldReturnAllMatchingNodes() {
            TreeNode<Integer> root = new TreeNode<>(1);
            root.addChild(2);
            TreeNode<Integer> child = root.addChild(3);
            child.addChild(4);
            child.addChild(5);

            List<TreeNode<Integer>> result = root.findAll(n -> n > 2);

            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Traversal Tests")
    class TraversalTests {

        @Test
        @DisplayName("forEachPreOrder should visit nodes in pre-order")
        void forEachPreOrderShouldVisitNodesInPreOrder() {
            TreeNode<String> root = new TreeNode<>("A");
            TreeNode<String> b = root.addChild("B");
            root.addChild("C");
            b.addChild("D");
            b.addChild("E");

            StringBuilder sb = new StringBuilder();
            root.forEachPreOrder(n -> sb.append(n.getData()));

            assertThat(sb.toString()).isEqualTo("ABDEC");
        }

        @Test
        @DisplayName("forEachPostOrder should visit nodes in post-order")
        void forEachPostOrderShouldVisitNodesInPostOrder() {
            TreeNode<String> root = new TreeNode<>("A");
            TreeNode<String> b = root.addChild("B");
            root.addChild("C");
            b.addChild("D");
            b.addChild("E");

            StringBuilder sb = new StringBuilder();
            root.forEachPostOrder(n -> sb.append(n.getData()));

            assertThat(sb.toString()).isEqualTo("DEBCA");
        }

        @Test
        @DisplayName("forEachBreadthFirst should visit nodes level by level")
        void forEachBreadthFirstShouldVisitNodesLevelByLevel() {
            TreeNode<String> root = new TreeNode<>("A");
            TreeNode<String> b = root.addChild("B");
            root.addChild("C");
            b.addChild("D");
            b.addChild("E");

            StringBuilder sb = new StringBuilder();
            root.forEachBreadthFirst(n -> sb.append(n.getData()));

            assertThat(sb.toString()).isEqualTo("ABCDE");
        }
    }

    @Nested
    @DisplayName("Transformation Tests")
    class TransformationTests {

        @Test
        @DisplayName("map should transform all node data")
        void mapShouldTransformAllNodeData() {
            TreeNode<Integer> root = new TreeNode<>(1);
            root.addChild(2);
            root.addChild(3);

            TreeNode<String> mapped = root.map(String::valueOf);

            assertThat(mapped.getData()).isEqualTo("1");
            assertThat(mapped.getChildren()).hasSize(2);
            assertThat(mapped.getChildren().get(0).getData()).isEqualTo("2");
        }

        @Test
        @DisplayName("filter should keep matching nodes with ancestors")
        void filterShouldKeepMatchingNodesWithAncestors() {
            TreeNode<Integer> root = new TreeNode<>(1);
            TreeNode<Integer> child1 = root.addChild(2);
            root.addChild(3);
            child1.addChild(4);
            child1.addChild(5);

            TreeNode<Integer> filtered = root.filter(n -> n == 4 || n == 1);

            assertThat(filtered).isNotNull();
            assertThat(filtered.getData()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Size Tests")
    class SizeTests {

        @Test
        @DisplayName("size should return 1 for single node")
        void sizeShouldReturn1ForSingleNode() {
            TreeNode<String> node = new TreeNode<>("single");

            assertThat(node.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("size should return total node count")
        void sizeShouldReturnTotalNodeCount() {
            TreeNode<String> root = new TreeNode<>("root");
            TreeNode<String> child = root.addChild("child");
            child.addChild("grandchild1");
            child.addChild("grandchild2");
            root.addChild("child2");

            assertThat(root.size()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Data Operations Tests")
    class DataOperationsTests {

        @Test
        @DisplayName("setData should update node data")
        void setDataShouldUpdateNodeData() {
            TreeNode<String> node = new TreeNode<>("original");

            node.setData("updated");

            assertThat(node.getData()).isEqualTo("updated");
        }
    }
}
