package cloud.opencode.base.tree;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeableTest Tests
 * TreeableTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("Treeable 接口测试")
class TreeableTest {

    @Nested
    @DisplayName("接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("getId返回节点ID")
        void testGetId() {
            TestNode node = new TestNode(1L, null);
            assertThat(node.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getParentId返回父节点ID")
        void testGetParentId() {
            TestNode node = new TestNode(2L, 1L);
            assertThat(node.getParentId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("setChildren和getChildren正确工作")
        void testSetAndGetChildren() {
            TestNode parent = new TestNode(1L, null);
            TestNode child1 = new TestNode(2L, 1L);
            TestNode child2 = new TestNode(3L, 1L);

            parent.setChildren(List.of(child1, child2));

            assertThat(parent.getChildren()).containsExactly(child1, child2);
        }

        @Test
        @DisplayName("根节点的parentId为null")
        void testRootNodeParentIdIsNull() {
            TestNode root = new TestNode(1L, null);
            assertThat(root.getParentId()).isNull();
        }
    }

    static class TestNode implements Treeable<TestNode, Long> {
        private final Long id;
        private final Long parentId;
        private List<TestNode> children = new ArrayList<>();

        TestNode(Long id, Long parentId) {
            this.id = id;
            this.parentId = parentId;
        }

        @Override
        public Long getId() { return id; }

        @Override
        public Long getParentId() { return parentId; }

        @Override
        public List<TestNode> getChildren() { return children; }

        @Override
        public void setChildren(List<TestNode> children) { this.children = children; }
    }
}
