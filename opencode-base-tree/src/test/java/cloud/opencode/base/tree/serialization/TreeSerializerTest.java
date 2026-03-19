package cloud.opencode.base.tree.serialization;

import cloud.opencode.base.tree.DefaultTreeNode;
import cloud.opencode.base.tree.TreeNode;
import cloud.opencode.base.tree.OpenTree;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeSerializer Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
class TreeSerializerTest {

    private List<DefaultTreeNode<Long>> createTestTree() {
        DefaultTreeNode<Long> root = new DefaultTreeNode<>();
        root.setId(1L);
        root.setParentId(null);
        root.setName("Root");

        DefaultTreeNode<Long> child1 = new DefaultTreeNode<>();
        child1.setId(2L);
        child1.setParentId(1L);
        child1.setName("Child1");

        DefaultTreeNode<Long> child2 = new DefaultTreeNode<>();
        child2.setId(3L);
        child2.setParentId(1L);
        child2.setName("Child2");

        DefaultTreeNode<Long> grandchild = new DefaultTreeNode<>();
        grandchild.setId(4L);
        grandchild.setParentId(2L);
        grandchild.setName("Grandchild");

        List<DefaultTreeNode<Long>> flatList = List.of(root, child1, child2, grandchild);
        return OpenTree.buildTree(new ArrayList<>(flatList), null);
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        void shouldSerializeToJson() {
            List<DefaultTreeNode<Long>> tree = createTestTree();

            String json = TreeSerializer.toJson(tree);

            assertThat(json).isNotEmpty();
            assertThat(json).contains("\"id\":");
            assertThat(json).contains("\"children\":");
        }

        @Test
        void shouldSerializeSingleNodeToJson() {
            List<DefaultTreeNode<Long>> tree = createTestTree();

            String json = TreeSerializer.toJsonSingle(tree.getFirst());

            assertThat(json).isNotEmpty();
            assertThat(json).startsWith("{");
            assertThat(json).endsWith("}");
        }

        @Test
        void shouldPrettyPrintJson() {
            List<DefaultTreeNode<Long>> tree = createTestTree();
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .prettyPrint(true)
                    .indentSize(4)
                    .build();

            String json = TreeSerializer.toJson(tree, config);

            assertThat(json).contains("\n");
            assertThat(json).contains("    ");
        }

        @Test
        void shouldCompactJson() {
            List<DefaultTreeNode<Long>> tree = createTestTree();
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .prettyPrint(false)
                    .build();

            String json = TreeSerializer.toJson(tree, config);

            assertThat(json).doesNotContain("\n");
        }

        @Test
        void shouldIncludeParentId() {
            List<DefaultTreeNode<Long>> tree = createTestTree();
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .includeParentId(true)
                    .build();

            String json = TreeSerializer.toJson(tree, config);

            assertThat(json).contains("\"parentId\":");
        }

        @Test
        void shouldExcludeParentId() {
            List<DefaultTreeNode<Long>> tree = createTestTree();
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .includeParentId(false)
                    .build();

            String json = TreeSerializer.toJson(tree, config);

            // Children nodes should not have parentId
            assertThat(json.split("\"parentId\":").length).isEqualTo(1);
        }

        @Test
        void shouldUseCustomFieldNames() {
            List<DefaultTreeNode<Long>> tree = createTestTree();
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .idField("nodeId")
                    .childrenField("items")
                    .build();

            String json = TreeSerializer.toJson(tree, config);

            assertThat(json).contains("\"nodeId\":");
            assertThat(json).contains("\"items\":");
        }

        @Test
        void shouldIncludeCustomFields() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.setId(1L);
            node.setName("Test");
            node.put("customField", "customValue");

            List<DefaultTreeNode<Long>> tree = List.of(node);
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .fieldExtractor((DefaultTreeNode<Long> n) -> Map.of("name", n.getName()))
                    .build();

            String json = TreeSerializer.toJson(tree, config);

            assertThat(json).contains("\"name\":");
            assertThat(json).contains("\"Test\"");
        }

        @Test
        void shouldEscapeSpecialCharacters() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.setId(1L);
            node.setName("Test \"quoted\" value\nwith newline");

            List<DefaultTreeNode<Long>> tree = List.of(node);
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .fieldExtractor((DefaultTreeNode<Long> n) -> Map.of("name", n.getName()))
                    .build();

            String json = TreeSerializer.toJson(tree, config);

            assertThat(json).contains("\\\"quoted\\\"");
            assertThat(json).contains("\\n");
        }

        @Test
        void shouldHandleEmptyTree() {
            List<DefaultTreeNode<Long>> tree = List.of();

            String json = TreeSerializer.toJson(tree);

            assertThat(json).isEqualTo("[]");
        }
    }

    @Nested
    @DisplayName("XML Serialization Tests")
    class XmlSerializationTests {

        @Test
        void shouldSerializeToXml() {
            List<DefaultTreeNode<Long>> tree = createTestTree();

            String xml = TreeSerializer.toXml(tree);

            assertThat(xml).isNotEmpty();
            assertThat(xml).contains("<?xml version=\"1.0\"");
            assertThat(xml).contains("<tree>");
            assertThat(xml).contains("</tree>");
            assertThat(xml).contains("<node>");
            assertThat(xml).contains("<id>");
        }

        @Test
        void shouldSerializeSingleNodeToXml() {
            List<DefaultTreeNode<Long>> tree = createTestTree();

            String xml = TreeSerializer.toXmlSingle(tree.getFirst());

            assertThat(xml).isNotEmpty();
            assertThat(xml).contains("<node>");
            assertThat(xml).contains("</node>");
        }

        @Test
        void shouldExcludeXmlDeclaration() {
            List<DefaultTreeNode<Long>> tree = createTestTree();
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .includeXmlDeclaration(false)
                    .build();

            String xml = TreeSerializer.toXml(tree, config);

            assertThat(xml).doesNotContain("<?xml");
        }

        @Test
        void shouldUseCustomElementNames() {
            List<DefaultTreeNode<Long>> tree = createTestTree();
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .rootElement("document")
                    .nodeElement("item")
                    .idField("identifier")
                    .build();

            String xml = TreeSerializer.toXml(tree, config);

            assertThat(xml).contains("<document>");
            assertThat(xml).contains("<item>");
            assertThat(xml).contains("<identifier>");
        }

        @Test
        void shouldEscapeXmlSpecialCharacters() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.setId(1L);
            node.setName("Test <tag> & \"value\"");

            List<DefaultTreeNode<Long>> tree = List.of(node);
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .fieldExtractor((DefaultTreeNode<Long> n) -> Map.of("name", n.getName()))
                    .build();

            String xml = TreeSerializer.toXml(tree, config);

            assertThat(xml).contains("&lt;tag&gt;");
            assertThat(xml).contains("&amp;");
            assertThat(xml).contains("&quot;");
        }

        @Test
        void shouldPrettyPrintXml() {
            List<DefaultTreeNode<Long>> tree = createTestTree();
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .prettyPrint(true)
                    .indentSize(2)
                    .build();

            String xml = TreeSerializer.toXml(tree, config);

            assertThat(xml).contains("\n");
            assertThat(xml).contains("  ");
        }
    }

    @Nested
    @DisplayName("Map Conversion Tests")
    class MapConversionTests {

        @Test
        void shouldConvertToMaps() {
            List<DefaultTreeNode<Long>> tree = createTestTree();

            List<Map<String, Object>> maps = TreeSerializer.toMaps(tree);

            assertThat(maps).hasSize(1);
            assertThat(maps.getFirst()).containsKey("id");
            assertThat(maps.getFirst()).containsKey("children");
        }

        @Test
        void shouldConvertSingleToMap() {
            List<DefaultTreeNode<Long>> tree = createTestTree();

            Map<String, Object> map = TreeSerializer.toMap(tree.getFirst());

            assertThat(map).containsKey("id");
            assertThat(map.get("id")).isEqualTo(1L);
        }

        @Test
        void shouldIncludeChildrenInMap() {
            List<DefaultTreeNode<Long>> tree = createTestTree();

            Map<String, Object> map = TreeSerializer.toMap(tree.getFirst());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) map.get("children");
            assertThat(children).hasSize(2);
        }

        @Test
        void shouldConvertToFlatMaps() {
            List<DefaultTreeNode<Long>> tree = createTestTree();

            List<Map<String, Object>> flatMaps = TreeSerializer.toFlatMaps(tree);

            assertThat(flatMaps).hasSize(4);
            assertThat(flatMaps).allMatch(m -> m.containsKey("id"));
            assertThat(flatMaps).allMatch(m -> m.containsKey("parentId"));
            assertThat(flatMaps).noneMatch(m -> m.containsKey("children"));
        }

        @Test
        void shouldIncludeCustomFieldsInMap() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.setId(1L);
            node.setName("Test");

            List<DefaultTreeNode<Long>> tree = List.of(node);
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .fieldExtractor((DefaultTreeNode<Long> n) -> Map.of("name", n.getName(), "level", 0))
                    .build();

            List<Map<String, Object>> maps = TreeSerializer.toMaps(tree, config);

            assertThat(maps.getFirst()).containsEntry("name", "Test");
            assertThat(maps.getFirst()).containsEntry("level", 0);
        }
    }

    @Nested
    @DisplayName("TreeNode Serialization Tests")
    class TreeNodeSerializationTests {

        @Test
        void shouldSerializeTreeNodeToJson() {
            TreeNode<String> root = new TreeNode<>("Root");
            root.addChild("Child1");
            root.addChild("Child2");

            String json = TreeSerializer.treeNodeToJson(root,
                    data -> Map.of("value", data));

            assertThat(json).contains("\"value\":");
            assertThat(json).contains("\"Root\"");
            assertThat(json).contains("\"children\":");
        }

        @Test
        void shouldSerializeTreeNodeToXml() {
            TreeNode<String> root = new TreeNode<>("Root");
            root.addChild("Child1");
            root.addChild("Child2");

            String xml = TreeSerializer.treeNodeToXml(root,
                    data -> Map.of("value", data));

            assertThat(xml).contains("<node>");
            assertThat(xml).contains("<value>");
            assertThat(xml).contains("Root");
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        void shouldCreateDefaultConfig() {
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.defaultConfig();

            assertThat(config.idField()).isEqualTo("id");
            assertThat(config.parentIdField()).isEqualTo("parentId");
            assertThat(config.childrenField()).isEqualTo("children");
            assertThat(config.prettyPrint()).isTrue();
            assertThat(config.indentSize()).isEqualTo(2);
        }

        @Test
        void shouldBuildCustomConfig() {
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .idField("nodeId")
                    .parentIdField("pid")
                    .childrenField("items")
                    .nodeElement("entry")
                    .rootElement("entries")
                    .prettyPrint(false)
                    .indentSize(4)
                    .includeParentId(false)
                    .includeEmptyChildren(true)
                    .includeXmlDeclaration(false)
                    .build();

            assertThat(config.idField()).isEqualTo("nodeId");
            assertThat(config.parentIdField()).isEqualTo("pid");
            assertThat(config.childrenField()).isEqualTo("items");
            assertThat(config.nodeElement()).isEqualTo("entry");
            assertThat(config.rootElement()).isEqualTo("entries");
            assertThat(config.prettyPrint()).isFalse();
            assertThat(config.indentSize()).isEqualTo(4);
            assertThat(config.includeParentId()).isFalse();
            assertThat(config.includeEmptyChildren()).isTrue();
            assertThat(config.includeXmlDeclaration()).isFalse();
        }
    }

    @Nested
    @DisplayName("OpenTree Integration Tests")
    class OpenTreeIntegrationTests {

        @Test
        void shouldSerializeViaOpenTree() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.setId(1L);
            node.setName("Test");

            List<DefaultTreeNode<Long>> tree = List.of(node);

            String json = OpenTree.toJson(tree);
            String xml = OpenTree.toXml(tree);

            assertThat(json).contains("\"id\":");
            assertThat(xml).contains("<id>");
        }

        @Test
        void shouldSerializeWithFieldExtractor() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.setId(1L);
            node.setName("Test");

            List<DefaultTreeNode<Long>> tree = List.of(node);

            String json = OpenTree.toJson(tree, n -> Map.of("name", n.getName()));

            assertThat(json).contains("\"name\":");
            assertThat(json).contains("\"Test\"");
        }

        @Test
        void shouldConvertToMapsViaOpenTree() {
            List<DefaultTreeNode<Long>> tree = createTestTree();

            List<Map<String, Object>> maps = OpenTree.toMaps(tree);
            List<Map<String, Object>> flatMaps = OpenTree.toFlatMaps(tree);

            assertThat(maps).hasSize(1);
            assertThat(flatMaps).hasSize(4);
        }

        @Test
        void shouldSerializeTreeNodeViaOpenTree() {
            TreeNode<String> root = new TreeNode<>("Root");
            root.addChild("Child");

            String json = OpenTree.treeNodeToJson(root, data -> Map.of("text", data));
            String xml = OpenTree.treeNodeToXml(root, data -> Map.of("text", data));

            assertThat(json).contains("\"text\":");
            assertThat(xml).contains("<text>");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        void shouldHandleNullValues() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.setId(1L);
            node.setName(null);

            List<DefaultTreeNode<Long>> tree = List.of(node);
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .fieldExtractor((DefaultTreeNode<Long> n) -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", n.getName());
                        return map;
                    })
                    .build();

            String json = TreeSerializer.toJson(tree, config);

            assertThat(json).contains("\"name\": null");
        }

        @Test
        void shouldHandleNestedMaps() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.setId(1L);

            List<DefaultTreeNode<Long>> tree = List.of(node);
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .fieldExtractor((DefaultTreeNode<Long> n) ->
                            Map.of("nested", Map.of("key", "value")))
                    .prettyPrint(false)
                    .build();

            String json = TreeSerializer.toJson(tree, config);

            assertThat(json).contains("\"nested\":");
            assertThat(json).contains("\"key\":\"value\"");
        }

        @Test
        void shouldHandleListValues() {
            DefaultTreeNode<Long> node = new DefaultTreeNode<>();
            node.setId(1L);

            List<DefaultTreeNode<Long>> tree = List.of(node);
            TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                    .fieldExtractor((DefaultTreeNode<Long> n) ->
                            Map.of("tags", List.of("a", "b", "c")))
                    .prettyPrint(false)
                    .build();

            String json = TreeSerializer.toJson(tree, config);

            assertThat(json).contains("\"tags\":");
            assertThat(json).contains("[\"a\",\"b\",\"c\"]");
        }
    }
}
