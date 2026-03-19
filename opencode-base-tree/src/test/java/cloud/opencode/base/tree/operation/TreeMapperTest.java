package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeMapperTest Tests
 * TreeMapperTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeMapper Tests")
class TreeMapperTest {

    @Nested
    @DisplayName("Map Tests")
    class MapTests {

        @Test
        @DisplayName("map should transform tree nodes")
        void mapShouldTransformTreeNodes() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            List<DefaultTreeNode<Long>> result = TreeMapper.map(
                List.of(root),
                node -> {
                    DefaultTreeNode<Long> mapped = new DefaultTreeNode<>(
                        node.getId() * 10, node.getParentId(), node.getName().toUpperCase());
                    return mapped;
                }
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(10L);
            assertThat(result.get(0).getName()).isEqualTo("ROOT");
            assertThat(result.get(0).getChildren()).hasSize(1);
            assertThat(result.get(0).getChildren().get(0).getId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("map should handle empty list")
        void mapShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();

            List<DefaultTreeNode<Long>> result = TreeMapper.map(emptyList, node -> node);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("map should handle multiple roots")
        void mapShouldHandleMultipleRoots() {
            DefaultTreeNode<Long> root1 = new DefaultTreeNode<>(1L, null, "Root1");
            DefaultTreeNode<Long> root2 = new DefaultTreeNode<>(2L, null, "Root2");

            List<DefaultTreeNode<Long>> result = TreeMapper.map(
                List.of(root1, root2),
                node -> new DefaultTreeNode<>(node.getId(), node.getParentId(), node.getName() + "_mapped")
            );

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Root1_mapped");
            assertThat(result.get(1).getName()).isEqualTo("Root2_mapped");
        }
    }

    @Nested
    @DisplayName("MapToAny Tests")
    class MapToAnyTests {

        record SimpleNode(Long id, String name, List<SimpleNode> children) {}

        @Test
        @DisplayName("mapToAny should transform to different type")
        void mapToAnyShouldTransformToDifferentType() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            List<SimpleNode> result = TreeMapper.mapToAny(
                List.of(root),
                node -> new SimpleNode(node.getId(), node.getName(), new ArrayList<>()),
                (parent, children) -> {
                    // Need mutable list
                    parent.children().addAll(children);
                }
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(0).name()).isEqualTo("Root");
            assertThat(result.get(0).children()).hasSize(1);
        }

        @Test
        @DisplayName("mapToAny should handle empty list")
        void mapToAnyShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();

            List<SimpleNode> result = TreeMapper.mapToAny(
                emptyList,
                node -> new SimpleNode(node.getId(), node.getName(), new ArrayList<>()),
                (parent, children) -> parent.children().addAll(children)
            );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("ExtractAll Tests")
    class ExtractAllTests {

        @Test
        @DisplayName("extractAll should extract values from all nodes")
        void extractAllShouldExtractValuesFromAllNodes() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            List<Long> ids = TreeMapper.extractAll(List.of(root), DefaultTreeNode::getId);

            assertThat(ids).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("extractAll should handle deep hierarchy")
        void extractAllShouldHandleDeepHierarchy() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            DefaultTreeNode<Long> grandchild = new DefaultTreeNode<>(3L, 2L, "Grandchild");
            child.setChildren(new ArrayList<>(List.of(grandchild)));
            root.setChildren(new ArrayList<>(List.of(child)));

            List<String> names = TreeMapper.extractAll(List.of(root), DefaultTreeNode::getName);

            assertThat(names).containsExactly("Root", "Child", "Grandchild");
        }

        @Test
        @DisplayName("extractAll should handle empty list")
        void extractAllShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();

            List<Long> ids = TreeMapper.extractAll(emptyList, DefaultTreeNode::getId);

            assertThat(ids).isEmpty();
        }
    }
}
