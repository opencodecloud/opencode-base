package cloud.opencode.base.collections.tree;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeTraversalUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("TreeTraversalUtil 测试")
class TreeTraversalUtilTest {

    // 测试用树节点
    static class TreeNode {
        String value;
        List<TreeNode> children;

        TreeNode(String value) {
            this.value = value;
            this.children = new ArrayList<>();
        }

        void addChild(TreeNode child) {
            children.add(child);
        }

        List<TreeNode> getChildren() {
            return children;
        }
    }

    // 创建测试树:
    //       A
    //      /|\
    //     B C D
    //    /|   |
    //   E F   G
    TreeNode createTestTree() {
        TreeNode a = new TreeNode("A");
        TreeNode b = new TreeNode("B");
        TreeNode c = new TreeNode("C");
        TreeNode d = new TreeNode("D");
        TreeNode e = new TreeNode("E");
        TreeNode f = new TreeNode("F");
        TreeNode g = new TreeNode("G");

        a.addChild(b);
        a.addChild(c);
        a.addChild(d);
        b.addChild(e);
        b.addChild(f);
        d.addChild(g);

        return a;
    }

    @Nested
    @DisplayName("前序遍历测试")
    class PreOrderTests {

        @Test
        @DisplayName("preOrder - 前序遍历")
        void testPreOrder() {
            TreeNode root = createTestTree();
            List<String> result = new ArrayList<>();

            TreeTraversalUtil.preOrder(root, TreeNode::getChildren, node -> result.add(node.value));

            assertThat(result).containsExactly("A", "B", "E", "F", "C", "D", "G");
        }

        @Test
        @DisplayName("preOrder - null 根节点")
        void testPreOrderNull() {
            List<String> result = new ArrayList<>();

            TreeTraversalUtil.<String>preOrder(null, n -> null, result::add);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("preOrderIterative - 迭代式前序遍历")
        void testPreOrderIterative() {
            TreeNode root = createTestTree();
            List<String> result = new ArrayList<>();

            TreeTraversalUtil.preOrderIterative(root, TreeNode::getChildren, node -> result.add(node.value));

            assertThat(result).containsExactly("A", "B", "E", "F", "C", "D", "G");
        }

        @Test
        @DisplayName("preOrderIterative - null 根节点")
        void testPreOrderIterativeNull() {
            List<String> result = new ArrayList<>();

            TreeTraversalUtil.<String>preOrderIterative(null, n -> null, result::add);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("collectPreOrder - 收集前序节点")
        void testCollectPreOrder() {
            TreeNode root = createTestTree();

            List<TreeNode> result = TreeTraversalUtil.collectPreOrder(root, TreeNode::getChildren);

            List<String> values = result.stream().map(n -> n.value).toList();
            assertThat(values).containsExactly("A", "B", "E", "F", "C", "D", "G");
        }
    }

    @Nested
    @DisplayName("后序遍历测试")
    class PostOrderTests {

        @Test
        @DisplayName("postOrder - 后序遍历")
        void testPostOrder() {
            TreeNode root = createTestTree();
            List<String> result = new ArrayList<>();

            TreeTraversalUtil.postOrder(root, TreeNode::getChildren, node -> result.add(node.value));

            assertThat(result).containsExactly("E", "F", "B", "C", "G", "D", "A");
        }

        @Test
        @DisplayName("postOrder - null 根节点")
        void testPostOrderNull() {
            List<String> result = new ArrayList<>();

            TreeTraversalUtil.<String>postOrder(null, n -> null, result::add);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("collectPostOrder - 收集后序节点")
        void testCollectPostOrder() {
            TreeNode root = createTestTree();

            List<TreeNode> result = TreeTraversalUtil.collectPostOrder(root, TreeNode::getChildren);

            List<String> values = result.stream().map(n -> n.value).toList();
            assertThat(values).containsExactly("E", "F", "B", "C", "G", "D", "A");
        }
    }

    @Nested
    @DisplayName("层序遍历测试")
    class LevelOrderTests {

        @Test
        @DisplayName("levelOrder - 层序遍历")
        void testLevelOrder() {
            TreeNode root = createTestTree();
            List<String> result = new ArrayList<>();

            TreeTraversalUtil.levelOrder(root, TreeNode::getChildren, node -> result.add(node.value));

            assertThat(result).containsExactly("A", "B", "C", "D", "E", "F", "G");
        }

        @Test
        @DisplayName("levelOrder - null 根节点")
        void testLevelOrderNull() {
            List<String> result = new ArrayList<>();

            TreeTraversalUtil.<String>levelOrder(null, n -> null, result::add);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("collectLevelOrder - 收集层序节点")
        void testCollectLevelOrder() {
            TreeNode root = createTestTree();

            List<TreeNode> result = TreeTraversalUtil.collectLevelOrder(root, TreeNode::getChildren);

            List<String> values = result.stream().map(n -> n.value).toList();
            assertThat(values).containsExactly("A", "B", "C", "D", "E", "F", "G");
        }

        @Test
        @DisplayName("collectByLevel - 按层收集")
        void testCollectByLevel() {
            TreeNode root = createTestTree();

            List<List<TreeNode>> levels = TreeTraversalUtil.collectByLevel(root, TreeNode::getChildren);

            assertThat(levels).hasSize(3);
            assertThat(levels.get(0).stream().map(n -> n.value).toList()).containsExactly("A");
            assertThat(levels.get(1).stream().map(n -> n.value).toList()).containsExactly("B", "C", "D");
            assertThat(levels.get(2).stream().map(n -> n.value).toList()).containsExactly("E", "F", "G");
        }

        @Test
        @DisplayName("collectByLevel - null 根节点")
        void testCollectByLevelNull() {
            List<List<String>> levels = TreeTraversalUtil.collectByLevel(null, n -> null);

            assertThat(levels).isEmpty();
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityMethodTests {

        @Test
        @DisplayName("depth - 计算深度")
        void testDepth() {
            TreeNode root = createTestTree();

            int depth = TreeTraversalUtil.depth(root, TreeNode::getChildren);

            assertThat(depth).isEqualTo(3);
        }

        @Test
        @DisplayName("depth - null 根节点")
        void testDepthNull() {
            int depth = TreeTraversalUtil.depth(null, n -> null);

            assertThat(depth).isZero();
        }

        @Test
        @DisplayName("depth - 单节点")
        void testDepthSingleNode() {
            TreeNode root = new TreeNode("A");

            int depth = TreeTraversalUtil.depth(root, TreeNode::getChildren);

            assertThat(depth).isEqualTo(1);
        }

        @Test
        @DisplayName("count - 计算节点数")
        void testCount() {
            TreeNode root = createTestTree();

            int count = TreeTraversalUtil.count(root, TreeNode::getChildren);

            assertThat(count).isEqualTo(7);
        }

        @Test
        @DisplayName("count - null 根节点")
        void testCountNull() {
            int count = TreeTraversalUtil.count(null, n -> null);

            assertThat(count).isZero();
        }

        @Test
        @DisplayName("find - 查找节点")
        void testFind() {
            TreeNode root = createTestTree();

            TreeNode found = TreeTraversalUtil.find(root, TreeNode::getChildren, node -> "E".equals(node.value));

            assertThat(found).isNotNull();
            assertThat(found.value).isEqualTo("E");
        }

        @Test
        @DisplayName("find - 未找到节点")
        void testFindNotFound() {
            TreeNode root = createTestTree();

            TreeNode found = TreeTraversalUtil.find(root, TreeNode::getChildren, node -> "Z".equals(node.value));

            assertThat(found).isNull();
        }

        @Test
        @DisplayName("find - null 根节点")
        void testFindNull() {
            TreeNode found = TreeTraversalUtil.find(null, TreeNode::getChildren, node -> true);

            assertThat(found).isNull();
        }

        @Test
        @DisplayName("findAll - 查找所有匹配节点")
        void testFindAll() {
            TreeNode root = createTestTree();

            List<TreeNode> found = TreeTraversalUtil.findAll(root, TreeNode::getChildren,
                    node -> node.value.compareTo("C") >= 0);

            List<String> values = found.stream().map(n -> n.value).toList();
            assertThat(values).containsExactlyInAnyOrder("C", "D", "E", "F", "G");
        }

        @Test
        @DisplayName("findAll - 无匹配")
        void testFindAllNoMatch() {
            TreeNode root = createTestTree();

            List<TreeNode> found = TreeTraversalUtil.findAll(root, TreeNode::getChildren,
                    node -> "Z".equals(node.value));

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("pathTo - 获取路径")
        void testPathTo() {
            TreeNode root = createTestTree();
            TreeNode target = TreeTraversalUtil.find(root, TreeNode::getChildren, node -> "E".equals(node.value));

            List<TreeNode> path = TreeTraversalUtil.pathTo(root, target, TreeNode::getChildren);

            List<String> values = path.stream().map(n -> n.value).toList();
            assertThat(values).containsExactly("A", "B", "E");
        }

        @Test
        @DisplayName("pathTo - 根节点")
        void testPathToRoot() {
            TreeNode root = createTestTree();

            List<TreeNode> path = TreeTraversalUtil.pathTo(root, root, TreeNode::getChildren);

            List<String> values = path.stream().map(n -> n.value).toList();
            assertThat(values).containsExactly("A");
        }

        @Test
        @DisplayName("pathTo - 未找到目标")
        void testPathToNotFound() {
            TreeNode root = createTestTree();
            TreeNode other = new TreeNode("Z");

            List<TreeNode> path = TreeTraversalUtil.pathTo(root, other, TreeNode::getChildren);

            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("pathTo - null 根节点")
        void testPathToNull() {
            TreeNode target = new TreeNode("A");

            List<TreeNode> path = TreeTraversalUtil.pathTo(null, target, TreeNode::getChildren);

            assertThat(path).isEmpty();
        }
    }

    @Nested
    @DisplayName("特殊树结构测试")
    class SpecialTreeTests {

        @Test
        @DisplayName("单节点树")
        void testSingleNode() {
            TreeNode root = new TreeNode("A");

            List<String> preOrder = new ArrayList<>();
            TreeTraversalUtil.preOrder(root, TreeNode::getChildren, node -> preOrder.add(node.value));

            assertThat(preOrder).containsExactly("A");
        }

        @Test
        @DisplayName("线性树 (退化为链表)")
        void testLinearTree() {
            TreeNode a = new TreeNode("A");
            TreeNode b = new TreeNode("B");
            TreeNode c = new TreeNode("C");
            a.addChild(b);
            b.addChild(c);

            int depth = TreeTraversalUtil.depth(a, TreeNode::getChildren);
            int count = TreeTraversalUtil.count(a, TreeNode::getChildren);

            assertThat(depth).isEqualTo(3);
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("宽树 (多子节点)")
        void testWideTree() {
            TreeNode root = new TreeNode("A");
            for (int i = 0; i < 10; i++) {
                root.addChild(new TreeNode("Child" + i));
            }

            int depth = TreeTraversalUtil.depth(root, TreeNode::getChildren);
            int count = TreeTraversalUtil.count(root, TreeNode::getChildren);

            assertThat(depth).isEqualTo(2);
            assertThat(count).isEqualTo(11);
        }
    }
}
