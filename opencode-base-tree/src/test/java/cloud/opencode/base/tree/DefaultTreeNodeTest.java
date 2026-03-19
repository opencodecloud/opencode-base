package cloud.opencode.base.tree;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultTreeNodeTest Tests
 * DefaultTreeNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("DefaultTreeNode Tests")
class DefaultTreeNodeTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty node")
        void noArgConstructorShouldCreateEmptyNode() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();

            assertThat(node.getId()).isNull();
            assertThat(node.getParentId()).isNull();
            assertThat(node.getName()).isNull();
            assertThat(node.getSort()).isZero();
        }

        @Test
        @DisplayName("constructor with id should set id")
        void constructorWithIdShouldSetId() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>(1L);

            assertThat(node.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("constructor with id, parentId, and name should set all")
        void constructorWithIdParentIdAndNameShouldSetAll() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>(1L, 0L, "Test");

            assertThat(node.getId()).isEqualTo(1L);
            assertThat(node.getParentId()).isEqualTo(0L);
            assertThat(node.getName()).isEqualTo("Test");
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId should update id")
        void setIdShouldUpdateId() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();

            node.setId(5L);

            assertThat(node.getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("setParentId should update parentId")
        void setParentIdShouldUpdateParentId() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();

            node.setParentId(10L);

            assertThat(node.getParentId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("setName should update name")
        void setNameShouldUpdateName() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();

            node.setName("Updated");

            assertThat(node.getName()).isEqualTo("Updated");
        }

        @Test
        @DisplayName("setSort should update sort")
        void setSortShouldUpdateSort() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();

            node.setSort(99);

            assertThat(node.getSort()).isEqualTo(99);
        }

        @Test
        @DisplayName("setChildren should update children list")
        void setChildrenShouldUpdateChildrenList() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>(1L);
            List<DefaultTreeNode<Long>> children = new ArrayList<>();
            children.add(new DefaultTreeNode<>(2L, 1L, "Child"));

            node.setChildren(children);

            assertThat(node.getChildren()).hasSize(1);
            assertThat(node.getChildren().get(0).getId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("Extra Data Tests")
    class ExtraDataTests {

        @Test
        @DisplayName("put should add extra data")
        void putShouldAddExtraData() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();

            node.put("key", "value");

            assertThat(node.getExtra()).containsEntry("key", "value");
        }

        @Test
        @DisplayName("get should return extra data")
        void getShouldReturnExtraData() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.put("key", "value");

            Object result = node.get("key");

            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("get should return null for missing key")
        void getShouldReturnNullForMissingKey() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();

            Object result = node.get("missing");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("setExtra should replace extra map")
        void setExtraShouldReplaceExtraMap() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.put("old", "value");

            node.setExtra(java.util.Map.of("new", "data"));

            assertThat((Object) node.get("old")).isNull();
            assertThat((Object) node.get("new")).isEqualTo("data");
        }
    }

    @Nested
    @DisplayName("Node Properties Tests")
    class NodePropertiesTests {

        @Test
        @DisplayName("isRoot should return true when parentId is null")
        void isRootShouldReturnTrueWhenParentIdIsNull() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>(1L, null, "Root");

            assertThat(node.isRoot()).isTrue();
        }

        @Test
        @DisplayName("isRoot should return false when parentId is 0L (not null)")
        void isRootShouldReturnFalseWhenParentIdIsZero() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>(1L, 0L, "Root");

            // isRoot() only checks parentId == null
            assertThat(node.isRoot()).isFalse();
        }

        @Test
        @DisplayName("isRoot should return false when parentId is non-zero")
        void isRootShouldReturnFalseWhenParentIdIsNonZero() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>(2L, 1L, "Child");

            assertThat(node.isRoot()).isFalse();
        }

        @Test
        @DisplayName("isLeaf should return true when children is empty")
        void isLeafShouldReturnTrueWhenChildrenIsEmpty() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>(1L, 0L, "Leaf");
            node.setChildren(new ArrayList<>());

            assertThat(node.isLeaf()).isTrue();
        }

        @Test
        @DisplayName("isLeaf should return false when has children")
        void isLeafShouldReturnFalseWhenHasChildren() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>(1L, 0L, "Parent");
            List<DefaultTreeNode<Long>> children = new ArrayList<>();
            children.add(new DefaultTreeNode<>(2L, 1L, "Child"));
            node.setChildren(children);

            assertThat(node.isLeaf()).isFalse();
        }
    }

    @Nested
    @DisplayName("AddChild Tests")
    class AddChildTests {

        @Test
        @DisplayName("addChild should add child to list")
        void addChildShouldAddChildToList() {
            DefaultTreeNode<Long> parent = new DefaultTreeNode<>(1L, 0L, "Parent");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");

            parent.addChild(child);

            assertThat(parent.getChildren()).contains(child);
        }

        @Test
        @DisplayName("addChild should add multiple children")
        void addChildShouldAddMultipleChildren() {
            DefaultTreeNode<Long> parent = new DefaultTreeNode<>(1L, 0L, "Parent");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");

            parent.addChild(child1);
            parent.addChild(child2);

            assertThat(parent.getChildren()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("String ID Tests")
    class StringIdTests {

        @Test
        @DisplayName("should work with String IDs")
        void shouldWorkWithStringIds() {
            DefaultTreeNode<String> node = new DefaultTreeNode<>("node1", "root", "Node 1");

            assertThat(node.getId()).isEqualTo("node1");
            assertThat(node.getParentId()).isEqualTo("root");
            assertThat(node.getName()).isEqualTo("Node 1");
        }

        @Test
        @DisplayName("isRoot should return false for empty string parentId (not null)")
        void isRootShouldReturnFalseForEmptyStringParentId() {
            DefaultTreeNode<String> node = new DefaultTreeNode<>("node1", "", "Root");

            // isRoot() only checks parentId == null
            assertThat(node.isRoot()).isFalse();
        }

        @Test
        @DisplayName("isRoot should return false for '0' string parentId (not null)")
        void isRootShouldReturnFalseForZeroStringParentId() {
            DefaultTreeNode<String> node = new DefaultTreeNode<>("node1", "0", "Root");

            // isRoot() only checks parentId == null
            assertThat(node.isRoot()).isFalse();
        }
    }
}
