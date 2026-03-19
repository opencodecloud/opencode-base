package cloud.opencode.base.tree;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * LightTreeNodeTest Tests
 * LightTreeNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("LightTreeNode Tests")
class LightTreeNodeTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create node with all parameters")
        void ofShouldCreateNodeWithAllParameters() {
            LightTreeNode<Long> node = LightTreeNode.of(1L, 0L, "Test");

            assertThat(node.id()).isEqualTo(1L);
            assertThat(node.parentId()).isEqualTo(0L);
            assertThat(node.name()).isEqualTo("Test");
            assertThat(node.children()).isEmpty();
        }

        @Test
        @DisplayName("root should create root node")
        void rootShouldCreateRootNode() {
            LightTreeNode<Long> node = LightTreeNode.root(1L, "Root");

            assertThat(node.id()).isEqualTo(1L);
            assertThat(node.parentId()).isNull();
            assertThat(node.name()).isEqualTo("Root");
            assertThat(node.isRoot()).isTrue();
        }
    }

    @Nested
    @DisplayName("Record Constructor Tests")
    class RecordConstructorTests {

        @Test
        @DisplayName("constructor should set all fields")
        void constructorShouldSetAllFields() {
            LightTreeNode<String> child = new LightTreeNode<>("c1", "root", "Child", List.of());
            LightTreeNode<String> node = new LightTreeNode<>("root", null, "Root", List.of(child));

            assertThat(node.id()).isEqualTo("root");
            assertThat(node.parentId()).isNull();
            assertThat(node.name()).isEqualTo("Root");
            assertThat(node.children()).containsExactly(child);
        }

        @Test
        @DisplayName("constructor should handle null children as empty list")
        void constructorShouldHandleNullChildrenAsEmptyList() {
            LightTreeNode<Long> node = new LightTreeNode<>(1L, null, "Test", null);

            assertThat(node.children()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Node Properties Tests")
    class NodePropertiesTests {

        @Test
        @DisplayName("isRoot should return true when parentId is null")
        void isRootShouldReturnTrueWhenParentIdIsNull() {
            LightTreeNode<Long> node = LightTreeNode.root(1L, "Root");

            assertThat(node.isRoot()).isTrue();
        }

        @Test
        @DisplayName("isRoot should return false when parentId is not null")
        void isRootShouldReturnFalseWhenParentIdIsNotNull() {
            LightTreeNode<Long> node = LightTreeNode.of(2L, 1L, "Child");

            assertThat(node.isRoot()).isFalse();
        }

        @Test
        @DisplayName("isLeaf should return true when children is empty")
        void isLeafShouldReturnTrueWhenChildrenIsEmpty() {
            LightTreeNode<Long> node = LightTreeNode.of(1L, null, "Leaf");

            assertThat(node.isLeaf()).isTrue();
        }

        @Test
        @DisplayName("isLeaf should return false when has children")
        void isLeafShouldReturnFalseWhenHasChildren() {
            LightTreeNode<Long> child = LightTreeNode.of(2L, 1L, "Child");
            LightTreeNode<Long> parent = new LightTreeNode<>(1L, null, "Parent", List.of(child));

            assertThat(parent.isLeaf()).isFalse();
        }
    }

    @Nested
    @DisplayName("WithChild Tests")
    class WithChildTests {

        @Test
        @DisplayName("withChild should return new node with added child")
        void withChildShouldReturnNewNodeWithAddedChild() {
            LightTreeNode<Long> parent = LightTreeNode.root(1L, "Parent");
            LightTreeNode<Long> child = LightTreeNode.of(2L, 1L, "Child");

            LightTreeNode<Long> newParent = parent.withChild(child);

            assertThat(newParent).isNotSameAs(parent);
            assertThat(newParent.children()).containsExactly(child);
            assertThat(parent.children()).isEmpty(); // Original unchanged
        }

        @Test
        @DisplayName("withChild should preserve existing children")
        void withChildShouldPreserveExistingChildren() {
            LightTreeNode<Long> child1 = LightTreeNode.of(2L, 1L, "Child1");
            LightTreeNode<Long> parent = new LightTreeNode<>(1L, null, "Parent", List.of(child1));
            LightTreeNode<Long> child2 = LightTreeNode.of(3L, 1L, "Child2");

            LightTreeNode<Long> newParent = parent.withChild(child2);

            assertThat(newParent.children()).containsExactly(child1, child2);
        }
    }

    @Nested
    @DisplayName("Record Accessor Tests")
    class RecordAccessorTests {

        @Test
        @DisplayName("id() accessor should return id")
        void idAccessorShouldReturnId() {
            LightTreeNode<Long> node = LightTreeNode.of(42L, null, "Test");

            assertThat(node.id()).isEqualTo(42L);
        }

        @Test
        @DisplayName("parentId() accessor should return parentId")
        void parentIdAccessorShouldReturnParentId() {
            LightTreeNode<Long> node = LightTreeNode.of(2L, 1L, "Test");

            assertThat(node.parentId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("children() accessor should return children list")
        void childrenAccessorShouldReturnChildrenList() {
            LightTreeNode<Long> child = LightTreeNode.of(2L, 1L, "Child");
            LightTreeNode<Long> node = new LightTreeNode<>(1L, null, "Parent", List.of(child));

            assertThat(node.children()).containsExactly(child);
        }

        @Test
        @DisplayName("name() accessor should return name")
        void nameAccessorShouldReturnName() {
            LightTreeNode<Long> node = LightTreeNode.of(1L, null, "TestName");

            assertThat(node.name()).isEqualTo("TestName");
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class RecordEqualityTests {

        @Test
        @DisplayName("equals should return true for same values")
        void equalsShouldReturnTrueForSameValues() {
            LightTreeNode<Long> node1 = LightTreeNode.of(1L, null, "Test");
            LightTreeNode<Long> node2 = LightTreeNode.of(1L, null, "Test");

            assertThat(node1).isEqualTo(node2);
        }

        @Test
        @DisplayName("equals should return false for different values")
        void equalsShouldReturnFalseForDifferentValues() {
            LightTreeNode<Long> node1 = LightTreeNode.of(1L, null, "Test1");
            LightTreeNode<Long> node2 = LightTreeNode.of(1L, null, "Test2");

            assertThat(node1).isNotEqualTo(node2);
        }

        @Test
        @DisplayName("hashCode should be same for equal nodes")
        void hashCodeShouldBeSameForEqualNodes() {
            LightTreeNode<Long> node1 = LightTreeNode.of(1L, null, "Test");
            LightTreeNode<Long> node2 = LightTreeNode.of(1L, null, "Test");

            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }
    }

    @Nested
    @DisplayName("String ID Tests")
    class StringIdTests {

        @Test
        @DisplayName("should work with String IDs")
        void shouldWorkWithStringIds() {
            LightTreeNode<String> node = LightTreeNode.of("node-1", "parent-1", "Node 1");

            assertThat(node.id()).isEqualTo("node-1");
            assertThat(node.parentId()).isEqualTo("parent-1");
        }

        @Test
        @DisplayName("should build tree with String IDs")
        void shouldBuildTreeWithStringIds() {
            LightTreeNode<String> grandchild = LightTreeNode.of("gc-1", "c-1", "Grandchild");
            LightTreeNode<String> child = new LightTreeNode<>("c-1", "root", "Child", List.of(grandchild));
            LightTreeNode<String> root = LightTreeNode.root("root", "Root").withChild(child);

            assertThat(root.children()).hasSize(1);
            assertThat(root.children().get(0).id()).isEqualTo("c-1");
        }
    }
}
