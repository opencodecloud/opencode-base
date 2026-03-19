package cloud.opencode.base.tree;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeTraverserTest Tests
 * TreeTraverserTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeTraverser Tests")
class TreeTraverserTest {

    private TreeNode<String> root;

    @BeforeEach
    void setUp() {
        // Build tree:
        //       A
        //      / \
        //     B   C
        //    / \
        //   D   E
        root = new TreeNode<>("A");
        TreeNode<String> b = root.addChild("B");
        root.addChild("C");
        b.addChild("D");
        b.addChild("E");
    }

    @Nested
    @DisplayName("TraversalControl Enum Tests")
    class TraversalControlEnumTests {

        @Test
        @DisplayName("should have all control values")
        void shouldHaveAllControlValues() {
            TreeTraverser.TraversalControl[] controls = TreeTraverser.TraversalControl.values();

            assertThat(controls).contains(
                TreeTraverser.TraversalControl.CONTINUE,
                TreeTraverser.TraversalControl.SKIP_SUBTREE,
                TreeTraverser.TraversalControl.STOP
            );
        }
    }

    @Nested
    @DisplayName("Controlled Traversal Tests")
    class ControlledTraversalTests {

        @Test
        @DisplayName("traverse with CONTINUE should visit all nodes")
        void traverseWithContinueShouldVisitAllNodes() {
            List<String> visited = new ArrayList<>();

            boolean completed = TreeTraverser.traverse(root, node -> {
                visited.add(node.getData());
                return TreeTraverser.TraversalControl.CONTINUE;
            });

            assertThat(completed).isTrue();
            assertThat(visited).containsExactly("A", "B", "D", "E", "C");
        }

        @Test
        @DisplayName("traverse with STOP should terminate early")
        void traverseWithStopShouldTerminateEarly() {
            List<String> visited = new ArrayList<>();

            boolean completed = TreeTraverser.traverse(root, node -> {
                visited.add(node.getData());
                return "B".equals(node.getData())
                    ? TreeTraverser.TraversalControl.STOP
                    : TreeTraverser.TraversalControl.CONTINUE;
            });

            assertThat(completed).isFalse();
            assertThat(visited).containsExactly("A", "B");
        }

        @Test
        @DisplayName("traverse with SKIP_SUBTREE should skip children")
        void traverseWithSkipSubtreeShouldSkipChildren() {
            List<String> visited = new ArrayList<>();

            TreeTraverser.traverse(root, node -> {
                visited.add(node.getData());
                return "B".equals(node.getData())
                    ? TreeTraverser.TraversalControl.SKIP_SUBTREE
                    : TreeTraverser.TraversalControl.CONTINUE;
            });

            assertThat(visited).containsExactly("A", "B", "C");
        }
    }

    @Nested
    @DisplayName("Stream Tests")
    class StreamTests {

        @Test
        @DisplayName("stream should return pre-order stream")
        void streamShouldReturnPreOrderStream() {
            List<String> visited = TreeTraverser.stream(root)
                .map(TreeNode::getData)
                .toList();

            assertThat(visited).containsExactly("A", "B", "D", "E", "C");
        }

        @Test
        @DisplayName("parallelStream should support parallel operations")
        void parallelStreamShouldSupportParallelOperations() {
            long count = TreeTraverser.parallelStream(root).count();

            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("breadthFirstStream should visit level by level")
        void breadthFirstStreamShouldVisitLevelByLevel() {
            List<String> visited = TreeTraverser.breadthFirstStream(root)
                .map(TreeNode::getData)
                .toList();

            assertThat(visited).containsExactly("A", "B", "C", "D", "E");
        }

        @Test
        @DisplayName("postOrderStream should visit children before parent")
        void postOrderStreamShouldVisitChildrenBeforeParent() {
            List<String> visited = TreeTraverser.postOrderStream(root)
                .map(TreeNode::getData)
                .toList();

            assertThat(visited).containsExactly("D", "E", "B", "C", "A");
        }
    }

    @Nested
    @DisplayName("Iterator Tests")
    class IteratorTests {

        @Test
        @DisplayName("preOrderIterator should iterate in pre-order")
        void preOrderIteratorShouldIterateInPreOrder() {
            Iterator<TreeNode<String>> iter = TreeTraverser.preOrderIterator(root);
            List<String> visited = new ArrayList<>();

            while (iter.hasNext()) {
                visited.add(iter.next().getData());
            }

            assertThat(visited).containsExactly("A", "B", "D", "E", "C");
        }

        @Test
        @DisplayName("breadthFirstIterator should iterate level by level")
        void breadthFirstIteratorShouldIterateLevelByLevel() {
            Iterator<TreeNode<String>> iter = TreeTraverser.breadthFirstIterator(root);
            List<String> visited = new ArrayList<>();

            while (iter.hasNext()) {
                visited.add(iter.next().getData());
            }

            assertThat(visited).containsExactly("A", "B", "C", "D", "E");
        }

        @Test
        @DisplayName("postOrderIterator should iterate in post-order")
        void postOrderIteratorShouldIterateInPostOrder() {
            Iterator<TreeNode<String>> iter = TreeTraverser.postOrderIterator(root);
            List<String> visited = new ArrayList<>();

            while (iter.hasNext()) {
                visited.add(iter.next().getData());
            }

            assertThat(visited).containsExactly("D", "E", "B", "C", "A");
        }
    }

    @Nested
    @DisplayName("Map Tests")
    class MapTests {

        @Test
        @DisplayName("map should transform node data")
        void mapShouldTransformNodeData() {
            TreeNode<Integer> mapped = TreeTraverser.map(root, String::length);

            assertThat(mapped.getData()).isEqualTo(1);
            assertThat(mapped.getChildren()).hasSize(2);
        }

        @Test
        @DisplayName("mapNode should transform with node context")
        void mapNodeShouldTransformWithNodeContext() {
            TreeNode<String> mapped = TreeTraverser.mapNode(root,
                node -> node.getData() + ":" + node.getChildren().size());

            assertThat(mapped.getData()).isEqualTo("A:2");
        }

        @Test
        @DisplayName("flatMap should flatten mapped streams")
        void flatMapShouldFlattenMappedStreams() {
            List<String> result = TreeTraverser.flatMap(root,
                node -> Stream.of(node.getData(), node.getData().toLowerCase()))
                .toList();

            assertThat(result).hasSize(10);
        }
    }

    @Nested
    @DisplayName("Reduce Tests")
    class ReduceTests {

        @Test
        @DisplayName("reduce should accumulate values")
        void reduceShouldAccumulateValues() {
            int total = TreeTraverser.reduce(root, 0, (acc, data) -> acc + 1);

            assertThat(total).isEqualTo(5);
        }

        @Test
        @DisplayName("reduce with combiner should work")
        void reduceWithCombinerShouldWork() {
            String result = TreeTraverser.reduce(root, "",
                (acc, data) -> acc + data,
                (a, b) -> a + b);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("foldBottomUp should fold from leaves")
        void foldBottomUpShouldFoldFromLeaves() {
            int result = TreeTraverser.foldBottomUp(root,
                data -> 1,
                (data, childResults) -> 1 + childResults.stream().mapToInt(i -> i).sum());

            assertThat(result).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Ancestor/Descendant Tests")
    class AncestorDescendantTests {

        @Test
        @DisplayName("getDescendants should return all descendants")
        void getDescendantsShouldReturnAllDescendants() {
            List<TreeNode<String>> descendants = TreeTraverser.getDescendants(root);

            assertThat(descendants).hasSize(4);
        }

        @Test
        @DisplayName("getSiblings should return sibling nodes")
        void getSiblingsShouldReturnSiblingNodes() {
            TreeNode<String> b = root.getChildren().get(0);
            TreeNode<String> d = b.getChildren().get(0);

            List<TreeNode<String>> siblings = TreeTraverser.getSiblings(d);

            assertThat(siblings).hasSize(1);
            assertThat(siblings.get(0).getData()).isEqualTo("E");
        }

        @Test
        @DisplayName("getAncestors should return ancestors from parent to root")
        void getAncestorsShouldReturnAncestorsFromParentToRoot() {
            TreeNode<String> b = root.getChildren().get(0);
            TreeNode<String> d = b.getChildren().get(0);

            List<TreeNode<String>> ancestors = TreeTraverser.getAncestors(d);

            assertThat(ancestors).hasSize(2);
            assertThat(ancestors.get(0).getData()).isEqualTo("B");
            assertThat(ancestors.get(1).getData()).isEqualTo("A");
        }

        @Test
        @DisplayName("getDepth should return node depth")
        void getDepthShouldReturnNodeDepth() {
            TreeNode<String> b = root.getChildren().get(0);
            TreeNode<String> d = b.getChildren().get(0);

            assertThat(TreeTraverser.getDepth(root)).isZero();
            assertThat(TreeTraverser.getDepth(b)).isEqualTo(1);
            assertThat(TreeTraverser.getDepth(d)).isEqualTo(2);
        }

        @Test
        @DisplayName("findLowestCommonAncestor should find LCA")
        void findLowestCommonAncestorShouldFindLca() {
            TreeNode<String> b = root.getChildren().get(0);
            TreeNode<String> c = root.getChildren().get(1);
            TreeNode<String> d = b.getChildren().get(0);

            TreeNode<String> lca = TreeTraverser.findLowestCommonAncestor(d, c);

            assertThat(lca).isEqualTo(root);
        }
    }
}
