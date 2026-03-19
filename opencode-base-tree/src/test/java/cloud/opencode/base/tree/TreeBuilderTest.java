package cloud.opencode.base.tree;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeBuilderTest Tests
 * TreeBuilderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeBuilder Tests")
class TreeBuilderTest {

    record Item(Long id, Long parentId, String name) {}

    @Nested
    @DisplayName("Build Tests")
    class BuildTests {

        @Test
        @DisplayName("build should create tree from flat list")
        void buildShouldCreateTreeFromFlatList() {
            List<Item> items = List.of(
                new Item(1L, null, "Root"),
                new Item(2L, 1L, "Child1"),
                new Item(3L, 1L, "Child2")
            );

            List<TreeNode<Item>> roots = TreeBuilder.build(items, Item::id, Item::parentId);

            assertThat(roots).hasSize(1);
            assertThat(roots.get(0).getChildren()).hasSize(2);
        }

        @Test
        @DisplayName("build should handle empty list")
        void buildShouldHandleEmptyList() {
            List<Item> items = List.of();

            List<TreeNode<Item>> roots = TreeBuilder.build(items, Item::id, Item::parentId);

            assertThat(roots).isEmpty();
        }

        @Test
        @DisplayName("build should handle multiple roots")
        void buildShouldHandleMultipleRoots() {
            List<Item> items = List.of(
                new Item(1L, null, "Root1"),
                new Item(2L, null, "Root2")
            );

            List<TreeNode<Item>> roots = TreeBuilder.build(items, Item::id, Item::parentId);

            assertThat(roots).hasSize(2);
        }

        @Test
        @DisplayName("build should handle deep hierarchy")
        void buildShouldHandleDeepHierarchy() {
            List<Item> items = List.of(
                new Item(1L, null, "Root"),
                new Item(2L, 1L, "Child"),
                new Item(3L, 2L, "Grandchild"),
                new Item(4L, 3L, "GreatGrandchild")
            );

            List<TreeNode<Item>> roots = TreeBuilder.build(items, Item::id, Item::parentId);

            assertThat(roots).hasSize(1);
            assertThat(roots.get(0).getHeight()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("BuildSingle Tests")
    class BuildSingleTests {

        @Test
        @DisplayName("buildSingle should return single root")
        void buildSingleShouldReturnSingleRoot() {
            List<Item> items = List.of(
                new Item(1L, null, "Root"),
                new Item(2L, 1L, "Child")
            );

            TreeNode<Item> root = TreeBuilder.buildSingle(items, Item::id, Item::parentId);

            assertThat(root).isNotNull();
            assertThat(root.getData().id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("buildSingle should return null for empty list")
        void buildSingleShouldReturnNullForEmptyList() {
            List<Item> items = List.of();

            TreeNode<Item> root = TreeBuilder.buildSingle(items, Item::id, Item::parentId);

            assertThat(root).isNull();
        }

        @Test
        @DisplayName("buildSingle should return first root when multiple roots")
        void buildSingleShouldReturnFirstRootWhenMultipleRoots() {
            List<Item> items = List.of(
                new Item(1L, null, "Root1"),
                new Item(2L, null, "Root2")
            );

            TreeNode<Item> root = TreeBuilder.buildSingle(items, Item::id, Item::parentId);

            assertThat(root).isNotNull();
        }
    }

    @Nested
    @DisplayName("BuildFromMap Tests")
    class BuildFromMapTests {

        @Test
        @DisplayName("buildFromMap should create tree from nested map")
        void buildFromMapShouldCreateTreeFromNestedMap() {
            Map<String, Object> data = new HashMap<>();
            data.put("id", 1);
            data.put("name", "Root");
            data.put("children", List.of(
                Map.of("id", 2, "name", "Child1"),
                Map.of("id", 3, "name", "Child2")
            ));

            TreeNode<Map<String, Object>> root = TreeBuilder.buildFromMap(data, "children");

            assertThat(root.getData()).containsKey("id");
            assertThat(root.getData()).containsKey("name");
            assertThat(root.getData()).doesNotContainKey("children");
            assertThat(root.getChildren()).hasSize(2);
        }

        @Test
        @DisplayName("buildFromMap should handle map without children")
        void buildFromMapShouldHandleMapWithoutChildren() {
            Map<String, Object> data = Map.of("id", 1, "name", "Leaf");

            TreeNode<Map<String, Object>> root = TreeBuilder.buildFromMap(data, "children");

            assertThat(root.getChildren()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Flatten Tests")
    class FlattenTests {

        @Test
        @DisplayName("flatten should return all data in pre-order")
        void flattenShouldReturnAllDataInPreOrder() {
            TreeNode<String> root = new TreeNode<>("A");
            TreeNode<String> b = root.addChild("B");
            root.addChild("C");
            b.addChild("D");

            List<String> flat = TreeBuilder.flatten(root);

            assertThat(flat).containsExactly("A", "B", "D", "C");
        }

        @Test
        @DisplayName("flatten should return single element for leaf")
        void flattenShouldReturnSingleElementForLeaf() {
            TreeNode<String> leaf = new TreeNode<>("Single");

            List<String> flat = TreeBuilder.flatten(leaf);

            assertThat(flat).containsExactly("Single");
        }
    }

    @Nested
    @DisplayName("FlattenWithDepth Tests")
    class FlattenWithDepthTests {

        @Test
        @DisplayName("flattenWithDepth should include depth information")
        void flattenWithDepthShouldIncludeDepthInformation() {
            TreeNode<String> root = new TreeNode<>("Root");
            TreeNode<String> child = root.addChild("Child");
            child.addChild("Grandchild");

            List<TreeBuilder.NodeWithDepth<String>> result = TreeBuilder.flattenWithDepth(root);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).depth()).isEqualTo(0);
            assertThat(result.get(0).data()).isEqualTo("Root");
            assertThat(result.get(1).depth()).isEqualTo(1);
            assertThat(result.get(2).depth()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("NodeWithDepth Tests")
    class NodeWithDepthTests {

        @Test
        @DisplayName("NodeWithDepth should store data and depth")
        void nodeWithDepthShouldStoreDataAndDepth() {
            TreeBuilder.NodeWithDepth<String> nwd = new TreeBuilder.NodeWithDepth<>("data", 5);

            assertThat(nwd.data()).isEqualTo("data");
            assertThat(nwd.depth()).isEqualTo(5);
        }
    }
}
