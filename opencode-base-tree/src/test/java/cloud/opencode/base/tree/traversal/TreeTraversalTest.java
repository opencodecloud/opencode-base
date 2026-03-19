package cloud.opencode.base.tree.traversal;

import cloud.opencode.base.tree.Treeable;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeTraversalTest Tests
 * TreeTraversalTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeTraversal 接口测试")
class TreeTraversalTest {

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("traverse(roots, Consumer)使用Consumer遍历")
        void testTraverseWithConsumer() {
            List<TestNode> roots = buildTree();
            List<String> visited = new ArrayList<>();

            PreOrderTraversal.getInstance().traverse(roots, (java.util.function.Consumer<TestNode>) node -> visited.add(node.name));

            assertThat(visited).contains("root", "child1", "child2");
        }

        @Test
        @DisplayName("collect收集所有节点")
        void testCollect() {
            List<TestNode> roots = buildTree();

            List<TestNode> all = PreOrderTraversal.getInstance().collect(roots);

            assertThat(all).hasSize(3);
        }
    }

    @Nested
    @DisplayName("IterativeTraversal测试")
    class IterativeTraversalTests {

        @Test
        @DisplayName("getInstance返回单例")
        void testSingleton() {
            IterativeTraversal t1 = IterativeTraversal.getInstance();
            IterativeTraversal t2 = IterativeTraversal.getInstance();
            assertThat(t1).isSameAs(t2);
        }

        @Test
        @DisplayName("遍历顺序为先序")
        void testPreOrderTraversal() {
            List<TestNode> roots = buildTree();
            List<String> visited = new ArrayList<>();

            IterativeTraversal.getInstance().traverse(roots, (node, depth) -> {
                visited.add(node.name);
                return true;
            });

            assertThat(visited).containsExactly("root", "child1", "child2");
        }

        @Test
        @DisplayName("空列表不抛出异常")
        void testEmptyRoots() {
            List<TestNode> empty = List.of();
            TreeVisitor<TestNode> visitor = (node, depth) -> true;
            assertThatNoException().isThrownBy(() ->
                    IterativeTraversal.getInstance().traverse(empty, visitor));
        }

        @Test
        @DisplayName("null列表不抛出异常")
        void testNullRoots() {
            List<TestNode> nullList = null;
            TreeVisitor<TestNode> visitor = (node, depth) -> true;
            assertThatNoException().isThrownBy(() ->
                    IterativeTraversal.getInstance().traverse(nullList, visitor));
        }

        @Test
        @DisplayName("visitor返回false时停止遍历")
        void testStopTraversal() {
            List<TestNode> roots = buildTree();
            List<String> visited = new ArrayList<>();

            IterativeTraversal.getInstance().traverse(roots, (node, depth) -> {
                visited.add(node.name);
                return !"root".equals(node.name); // stop after root
            });

            assertThat(visited).containsExactly("root");
        }
    }

    @Nested
    @DisplayName("TreeVisitor测试")
    class TreeVisitorTests {

        @Test
        @DisplayName("of(Consumer)创建始终继续的访问者")
        void testOfConsumer() {
            List<String> names = new ArrayList<>();
            TreeVisitor<TestNode> visitor = TreeVisitor.of(n -> names.add(n.name));

            boolean result = visitor.visit(new TestNode(1L, null, "test"), 0);

            assertThat(result).isTrue();
            assertThat(names).containsExactly("test");
        }

        @Test
        @DisplayName("withDepth创建带深度的访问者")
        void testWithDepth() {
            List<String> results = new ArrayList<>();
            TreeVisitor<TestNode> visitor = TreeVisitor.withDepth((node, depth) ->
                    results.add(node.name + ":" + depth));

            visitor.visit(new TestNode(1L, null, "root"), 0);
            visitor.visit(new TestNode(2L, 1L, "child"), 1);

            assertThat(results).containsExactly("root:0", "child:1");
        }
    }

    private static List<TestNode> buildTree() {
        TestNode root = new TestNode(1L, null, "root");
        TestNode child1 = new TestNode(2L, 1L, "child1");
        TestNode child2 = new TestNode(3L, 1L, "child2");
        root.setChildren(List.of(child1, child2));
        return List.of(root);
    }

    static class TestNode implements Treeable<TestNode, Long> {
        final Long id;
        final Long parentId;
        final String name;
        private List<TestNode> children = new ArrayList<>();

        TestNode(Long id, Long parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        TestNode(Long id, Long parentId) {
            this(id, parentId, "node-" + id);
        }

        @Override public Long getId() { return id; }
        @Override public Long getParentId() { return parentId; }
        @Override public List<TestNode> getChildren() { return children; }
        @Override public void setChildren(List<TestNode> children) { this.children = children; }
    }
}
