package cloud.opencode.base.yml;

import cloud.opencode.base.yml.YmlNode.NodeType;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultYmlNodeTest Tests
 * DefaultYmlNodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("DefaultYmlNode Tests")
class DefaultYmlNodeTest {

    private static final String SIMPLE_YAML = """
        server:
          port: 8080
          host: localhost
        items:
          - one
          - two
        enabled: true
        ratio: 3.14
        """;

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create node from Map")
        void ofShouldCreateNodeFromMap() {
            Map<String, Object> data = Map.of("key", "value");

            YmlNode node = DefaultYmlNode.of(data);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.MAPPING);
        }

        @Test
        @DisplayName("of should create node from List")
        void ofShouldCreateNodeFromList() {
            List<String> data = List.of("one", "two");

            YmlNode node = DefaultYmlNode.of(data);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SEQUENCE);
        }

        @Test
        @DisplayName("of should create node from String scalar")
        void ofShouldCreateNodeFromStringScalar() {
            YmlNode node = DefaultYmlNode.of("scalar value");

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
        }

        @Test
        @DisplayName("of should create node from Integer scalar")
        void ofShouldCreateNodeFromIntegerScalar() {
            YmlNode node = DefaultYmlNode.of(42);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
        }

        @Test
        @DisplayName("of should create node from Boolean scalar")
        void ofShouldCreateNodeFromBooleanScalar() {
            YmlNode node = DefaultYmlNode.of(true);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
        }

        @Test
        @DisplayName("of should return null node for null value")
        void ofShouldReturnNullNodeForNullValue() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.NULL);
            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("nullNode should return null node")
        void nullNodeShouldReturnNullNode() {
            YmlNode node = DefaultYmlNode.nullNode();

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.NULL);
            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("nullNode should return same instance")
        void nullNodeShouldReturnSameInstance() {
            YmlNode node1 = DefaultYmlNode.nullNode();
            YmlNode node2 = DefaultYmlNode.nullNode();
            YmlNode node3 = DefaultYmlNode.of(null);

            assertThat(node1).isSameAs(node2);
            assertThat(node2).isSameAs(node3);
        }
    }

    @Nested
    @DisplayName("YmlNode.of Static Method Tests")
    class YmlNodeOfTests {

        @Test
        @DisplayName("YmlNode.of should delegate to DefaultYmlNode.of")
        void ymlNodeOfShouldDelegateToDefaultYmlNodeOf() {
            YmlNode node = YmlNode.of(Map.of("key", "value"));

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.MAPPING);
        }
    }

    @Nested
    @DisplayName("GetType Tests")
    class GetTypeTests {

        @Test
        @DisplayName("getType should return MAPPING for Map")
        void getTypeShouldReturnMappingForMap() {
            YmlNode node = DefaultYmlNode.of(Map.of("key", "value"));

            assertThat(node.getType()).isEqualTo(NodeType.MAPPING);
        }

        @Test
        @DisplayName("getType should return SEQUENCE for List")
        void getTypeShouldReturnSequenceForList() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two"));

            assertThat(node.getType()).isEqualTo(NodeType.SEQUENCE);
        }

        @Test
        @DisplayName("getType should return SCALAR for primitive values")
        void getTypeShouldReturnScalarForPrimitiveValues() {
            assertThat(DefaultYmlNode.of("string").getType()).isEqualTo(NodeType.SCALAR);
            assertThat(DefaultYmlNode.of(42).getType()).isEqualTo(NodeType.SCALAR);
            assertThat(DefaultYmlNode.of(3.14).getType()).isEqualTo(NodeType.SCALAR);
            assertThat(DefaultYmlNode.of(true).getType()).isEqualTo(NodeType.SCALAR);
        }

        @Test
        @DisplayName("getType should return NULL for null value")
        void getTypeShouldReturnNullForNullValue() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.getType()).isEqualTo(NodeType.NULL);
        }
    }

    @Nested
    @DisplayName("Node Type Check Tests")
    class NodeTypeCheckTests {

        @Test
        @DisplayName("isScalar should return true for scalar nodes")
        void isScalarShouldReturnTrueForScalarNodes() {
            assertThat(DefaultYmlNode.of("string").isScalar()).isTrue();
            assertThat(DefaultYmlNode.of(42).isScalar()).isTrue();
            assertThat(DefaultYmlNode.of(3.14).isScalar()).isTrue();
            assertThat(DefaultYmlNode.of(true).isScalar()).isTrue();
        }

        @Test
        @DisplayName("isScalar should return false for non-scalar nodes")
        void isScalarShouldReturnFalseForNonScalarNodes() {
            assertThat(DefaultYmlNode.of(Map.of()).isScalar()).isFalse();
            assertThat(DefaultYmlNode.of(List.of()).isScalar()).isFalse();
            assertThat(DefaultYmlNode.of(null).isScalar()).isFalse();
        }

        @Test
        @DisplayName("isSequence should return true for list nodes")
        void isSequenceShouldReturnTrueForListNodes() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two"));

            assertThat(node.isSequence()).isTrue();
        }

        @Test
        @DisplayName("isSequence should return false for non-list nodes")
        void isSequenceShouldReturnFalseForNonListNodes() {
            assertThat(DefaultYmlNode.of(Map.of()).isSequence()).isFalse();
            assertThat(DefaultYmlNode.of("string").isSequence()).isFalse();
            assertThat(DefaultYmlNode.of(null).isSequence()).isFalse();
        }

        @Test
        @DisplayName("isMapping should return true for map nodes")
        void isMappingShouldReturnTrueForMapNodes() {
            YmlNode node = DefaultYmlNode.of(Map.of("key", "value"));

            assertThat(node.isMapping()).isTrue();
        }

        @Test
        @DisplayName("isMapping should return false for non-map nodes")
        void isMappingShouldReturnFalseForNonMapNodes() {
            assertThat(DefaultYmlNode.of(List.of()).isMapping()).isFalse();
            assertThat(DefaultYmlNode.of("string").isMapping()).isFalse();
            assertThat(DefaultYmlNode.of(null).isMapping()).isFalse();
        }

        @Test
        @DisplayName("isNull should return true for null nodes")
        void isNullShouldReturnTrueForNullNodes() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("isNull should return false for non-null nodes")
        void isNullShouldReturnFalseForNonNullNodes() {
            assertThat(DefaultYmlNode.of(Map.of()).isNull()).isFalse();
            assertThat(DefaultYmlNode.of(List.of()).isNull()).isFalse();
            assertThat(DefaultYmlNode.of("string").isNull()).isFalse();
        }
    }

    @Nested
    @DisplayName("AsText Tests")
    class AsTextTests {

        @Test
        @DisplayName("asText should return string value")
        void asTextShouldReturnStringValue() {
            YmlNode node = DefaultYmlNode.of("hello");

            assertThat(node.asText()).isEqualTo("hello");
        }

        @Test
        @DisplayName("asText should convert number to string")
        void asTextShouldConvertNumberToString() {
            YmlNode node = DefaultYmlNode.of(42);

            assertThat(node.asText()).isEqualTo("42");
        }

        @Test
        @DisplayName("asText should convert boolean to string")
        void asTextShouldConvertBooleanToString() {
            assertThat(DefaultYmlNode.of(true).asText()).isEqualTo("true");
            assertThat(DefaultYmlNode.of(false).asText()).isEqualTo("false");
        }

        @Test
        @DisplayName("asText should return null for null node")
        void asTextShouldReturnNullForNullNode() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.asText()).isNull();
        }

        @Test
        @DisplayName("asText with default should return default for null node")
        void asTextWithDefaultShouldReturnDefaultForNullNode() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.asText("default")).isEqualTo("default");
        }

        @Test
        @DisplayName("asText with default should return value for non-null node")
        void asTextWithDefaultShouldReturnValueForNonNullNode() {
            YmlNode node = DefaultYmlNode.of("value");

            assertThat(node.asText("default")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("AsInt Tests")
    class AsIntTests {

        @Test
        @DisplayName("asInt should return integer value")
        void asIntShouldReturnIntegerValue() {
            YmlNode node = DefaultYmlNode.of(42);

            assertThat(node.asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("asInt should parse string to integer")
        void asIntShouldParseStringToInteger() {
            YmlNode node = DefaultYmlNode.of("123");

            assertThat(node.asInt()).isEqualTo(123);
        }

        @Test
        @DisplayName("asInt should return 0 for null node")
        void asIntShouldReturnZeroForNullNode() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.asInt()).isZero();
        }

        @Test
        @DisplayName("asInt should truncate double value")
        void asIntShouldTruncateDoubleValue() {
            YmlNode node = DefaultYmlNode.of(3.9);

            assertThat(node.asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("asInt with default should return default on parse error")
        void asIntWithDefaultShouldReturnDefaultOnParseError() {
            YmlNode node = DefaultYmlNode.of("not a number");

            assertThat(node.asInt(99)).isEqualTo(99);
        }

        @Test
        @DisplayName("asInt with default should return value on success")
        void asIntWithDefaultShouldReturnValueOnSuccess() {
            YmlNode node = DefaultYmlNode.of(42);

            assertThat(node.asInt(99)).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("AsLong Tests")
    class AsLongTests {

        @Test
        @DisplayName("asLong should return long value")
        void asLongShouldReturnLongValue() {
            YmlNode node = DefaultYmlNode.of(9999999999L);

            assertThat(node.asLong()).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("asLong should parse string to long")
        void asLongShouldParseStringToLong() {
            YmlNode node = DefaultYmlNode.of("9999999999");

            assertThat(node.asLong()).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("asLong should return 0 for null node")
        void asLongShouldReturnZeroForNullNode() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.asLong()).isZero();
        }

        @Test
        @DisplayName("asLong with default should return default on parse error")
        void asLongWithDefaultShouldReturnDefaultOnParseError() {
            YmlNode node = DefaultYmlNode.of("not a number");

            assertThat(node.asLong(999L)).isEqualTo(999L);
        }

        @Test
        @DisplayName("asLong with default should return value on success")
        void asLongWithDefaultShouldReturnValueOnSuccess() {
            YmlNode node = DefaultYmlNode.of(12345L);

            assertThat(node.asLong(999L)).isEqualTo(12345L);
        }
    }

    @Nested
    @DisplayName("AsBoolean Tests")
    class AsBooleanTests {

        @Test
        @DisplayName("asBoolean should return boolean value")
        void asBooleanShouldReturnBooleanValue() {
            assertThat(DefaultYmlNode.of(true).asBoolean()).isTrue();
            assertThat(DefaultYmlNode.of(false).asBoolean()).isFalse();
        }

        @Test
        @DisplayName("asBoolean should parse string to boolean")
        void asBooleanShouldParseStringToBoolean() {
            assertThat(DefaultYmlNode.of("true").asBoolean()).isTrue();
            assertThat(DefaultYmlNode.of("false").asBoolean()).isFalse();
        }

        @Test
        @DisplayName("asBoolean should return false for null node")
        void asBooleanShouldReturnFalseForNullNode() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("asBoolean with default should return default for null node")
        void asBooleanWithDefaultShouldReturnDefaultForNullNode() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.asBoolean(true)).isTrue();
        }

        @Test
        @DisplayName("asBoolean with default should return value for non-null node")
        void asBooleanWithDefaultShouldReturnValueForNonNullNode() {
            YmlNode node = DefaultYmlNode.of(false);

            assertThat(node.asBoolean(true)).isFalse();
        }
    }

    @Nested
    @DisplayName("AsDouble Tests")
    class AsDoubleTests {

        @Test
        @DisplayName("asDouble should return double value")
        void asDoubleShouldReturnDoubleValue() {
            YmlNode node = DefaultYmlNode.of(3.14);

            assertThat(node.asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("asDouble should parse string to double")
        void asDoubleShouldParseStringToDouble() {
            YmlNode node = DefaultYmlNode.of("3.14");

            assertThat(node.asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("asDouble should return 0.0 for null node")
        void asDoubleShouldReturnZeroForNullNode() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.asDouble()).isZero();
        }

        @Test
        @DisplayName("asDouble should convert integer to double")
        void asDoubleShouldConvertIntegerToDouble() {
            YmlNode node = DefaultYmlNode.of(42);

            assertThat(node.asDouble()).isEqualTo(42.0);
        }

        @Test
        @DisplayName("asDouble with default should return default on parse error")
        void asDoubleWithDefaultShouldReturnDefaultOnParseError() {
            YmlNode node = DefaultYmlNode.of("not a number");

            assertThat(node.asDouble(9.99)).isEqualTo(9.99);
        }

        @Test
        @DisplayName("asDouble with default should return value on success")
        void asDoubleWithDefaultShouldReturnValueOnSuccess() {
            YmlNode node = DefaultYmlNode.of(3.14);

            assertThat(node.asDouble(9.99)).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("Get By Key Tests")
    class GetByKeyTests {

        @Test
        @DisplayName("get should return child node by key")
        void getShouldReturnChildNodeByKey() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            YmlNode server = node.get("server");

            assertThat(server).isNotNull();
            assertThat(server.isMapping()).isTrue();
        }

        @Test
        @DisplayName("get should return null node for missing key")
        void getShouldReturnNullNodeForMissingKey() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            YmlNode missing = node.get("nonexistent");

            assertThat(missing).isNotNull();
            assertThat(missing.isNull()).isTrue();
        }

        @Test
        @DisplayName("get should return null node for non-mapping node")
        void getShouldReturnNullNodeForNonMappingNode() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two"));

            YmlNode result = node.get("key");

            assertThat(result).isNotNull();
            assertThat(result.isNull()).isTrue();
        }
    }

    @Nested
    @DisplayName("Get By Index Tests")
    class GetByIndexTests {

        @Test
        @DisplayName("get should return child node by index")
        void getShouldReturnChildNodeByIndex() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two", "three"));

            assertThat(node.get(0).asText()).isEqualTo("one");
            assertThat(node.get(1).asText()).isEqualTo("two");
            assertThat(node.get(2).asText()).isEqualTo("three");
        }

        @Test
        @DisplayName("get should return null node for out of bounds index")
        void getShouldReturnNullNodeForOutOfBoundsIndex() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two"));

            YmlNode result = node.get(10);

            assertThat(result).isNotNull();
            assertThat(result.isNull()).isTrue();
        }

        @Test
        @DisplayName("get should return null node for negative index")
        void getShouldReturnNullNodeForNegativeIndex() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two"));

            YmlNode result = node.get(-1);

            assertThat(result).isNotNull();
            assertThat(result.isNull()).isTrue();
        }

        @Test
        @DisplayName("get should return null node for non-sequence node")
        void getShouldReturnNullNodeForNonSequenceNode() {
            YmlNode node = DefaultYmlNode.of(Map.of("key", "value"));

            YmlNode result = node.get(0);

            assertThat(result).isNotNull();
            assertThat(result.isNull()).isTrue();
        }
    }

    @Nested
    @DisplayName("At Path Tests")
    class AtPathTests {

        @Test
        @DisplayName("at should navigate using dot notation")
        void atShouldNavigateUsingDotNotation() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            YmlNode port = node.at("server.port");

            assertThat(port.asInt()).isEqualTo(8080);
        }

        @Test
        @DisplayName("at should handle array index notation")
        void atShouldHandleArrayIndexNotation() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            YmlNode item = node.at("items[0]");

            assertThat(item.asText()).isEqualTo("one");
        }

        @Test
        @DisplayName("at should return null node for invalid path")
        void atShouldReturnNullNodeForInvalidPath() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            YmlNode result = node.at("nonexistent.path");

            assertThat(result).isNotNull();
            assertThat(result.isNull()).isTrue();
        }
    }

    @Nested
    @DisplayName("Has Tests")
    class HasTests {

        @Test
        @DisplayName("has should return true for existing key")
        void hasShouldReturnTrueForExistingKey() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            assertThat(node.has("server")).isTrue();
            assertThat(node.has("items")).isTrue();
        }

        @Test
        @DisplayName("has should return false for missing key")
        void hasShouldReturnFalseForMissingKey() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            assertThat(node.has("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("has should return false for non-mapping node")
        void hasShouldReturnFalseForNonMappingNode() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two"));

            assertThat(node.has("key")).isFalse();
        }
    }

    @Nested
    @DisplayName("Size Tests")
    class SizeTests {

        @Test
        @DisplayName("size should return map size")
        void sizeShouldReturnMapSize() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            assertThat(node.size()).isEqualTo(4); // server, items, enabled, ratio
        }

        @Test
        @DisplayName("size should return list size")
        void sizeShouldReturnListSize() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two", "three"));

            assertThat(node.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("size should return 0 for scalar node")
        void sizeShouldReturnZeroForScalarNode() {
            YmlNode node = DefaultYmlNode.of("scalar");

            assertThat(node.size()).isZero();
        }

        @Test
        @DisplayName("size should return 0 for null node")
        void sizeShouldReturnZeroForNullNode() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.size()).isZero();
        }
    }

    @Nested
    @DisplayName("Keys Tests")
    class KeysTests {

        @Test
        @DisplayName("keys should return all keys for mapping node")
        void keysShouldReturnAllKeysForMappingNode() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            Set<String> keys = node.keys();

            assertThat(keys).containsExactlyInAnyOrder("server", "items", "enabled", "ratio");
        }

        @Test
        @DisplayName("keys should return empty set for sequence node")
        void keysShouldReturnEmptySetForSequenceNode() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two"));

            Set<String> keys = node.keys();

            assertThat(keys).isEmpty();
        }

        @Test
        @DisplayName("keys should return empty set for scalar node")
        void keysShouldReturnEmptySetForScalarNode() {
            YmlNode node = DefaultYmlNode.of("scalar");

            Set<String> keys = node.keys();

            assertThat(keys).isEmpty();
        }
    }

    @Nested
    @DisplayName("Values Tests")
    class ValuesTests {

        @Test
        @DisplayName("values should return all child nodes for mapping")
        void valuesShouldReturnAllChildNodesForMapping() {
            YmlNode node = DefaultYmlNode.of(Map.of("a", 1, "b", 2));

            List<YmlNode> values = node.values();

            assertThat(values).hasSize(2);
        }

        @Test
        @DisplayName("values should return all items for sequence")
        void valuesShouldReturnAllItemsForSequence() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two", "three"));

            List<YmlNode> values = node.values();

            assertThat(values).hasSize(3);
            assertThat(values.get(0).asText()).isEqualTo("one");
            assertThat(values.get(1).asText()).isEqualTo("two");
            assertThat(values.get(2).asText()).isEqualTo("three");
        }

        @Test
        @DisplayName("values should return empty list for scalar")
        void valuesShouldReturnEmptyListForScalar() {
            YmlNode node = DefaultYmlNode.of("scalar");

            List<YmlNode> values = node.values();

            assertThat(values).isEmpty();
        }
    }

    @Nested
    @DisplayName("ToMap Tests")
    class ToMapTests {

        @Test
        @DisplayName("toMap should return map for mapping node")
        void toMapShouldReturnMapForMappingNode() {
            YmlNode node = DefaultYmlNode.of(Map.of("key", "value"));

            Map<String, Object> map = node.toMap();

            assertThat(map).containsEntry("key", "value");
        }

        @Test
        @DisplayName("toMap should return empty map for sequence node")
        void toMapShouldReturnEmptyMapForSequenceNode() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two"));

            Map<String, Object> map = node.toMap();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("toMap should return new map instance")
        void toMapShouldReturnNewMapInstance() {
            Map<String, Object> original = new HashMap<>();
            original.put("key", "value");
            YmlNode node = DefaultYmlNode.of(original);

            Map<String, Object> result = node.toMap();
            result.put("newKey", "newValue");

            assertThat(original).doesNotContainKey("newKey");
        }
    }

    @Nested
    @DisplayName("ToList Tests")
    class ToListTests {

        @Test
        @DisplayName("toList should return list for sequence node")
        void toListShouldReturnListForSequenceNode() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two", "three"));

            List<Object> list = node.toList();

            assertThat(list).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("toList should return empty list for mapping node")
        void toListShouldReturnEmptyListForMappingNode() {
            YmlNode node = DefaultYmlNode.of(Map.of("key", "value"));

            List<Object> list = node.toList();

            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("toList should return new list instance")
        void toListShouldReturnNewListInstance() {
            List<String> original = new ArrayList<>();
            original.add("one");
            YmlNode node = DefaultYmlNode.of(original);

            List<Object> result = node.toList();
            result.add("two");

            assertThat(original).hasSize(1);
        }
    }

    @Nested
    @DisplayName("ToYaml Tests")
    class ToYamlTests {

        @Test
        @DisplayName("toYaml should serialize node to YAML string")
        void toYamlShouldSerializeNodeToYamlString() {
            YmlNode node = DefaultYmlNode.of(Map.of("key", "value"));

            String yaml = node.toYaml();

            assertThat(yaml).contains("key:");
            assertThat(yaml).contains("value");
        }
    }

    @Nested
    @DisplayName("GetRawValue Tests")
    class GetRawValueTests {

        @Test
        @DisplayName("getRawValue should return underlying value")
        void getRawValueShouldReturnUnderlyingValue() {
            Map<String, Object> data = Map.of("key", "value");
            YmlNode node = DefaultYmlNode.of(data);

            Object rawValue = node.getRawValue();

            assertThat(rawValue).isEqualTo(data);
        }

        @Test
        @DisplayName("getRawValue should return null for null node")
        void getRawValueShouldReturnNullForNullNode() {
            YmlNode node = DefaultYmlNode.of(null);

            assertThat(node.getRawValue()).isNull();
        }
    }

    @Nested
    @DisplayName("Iterator Tests")
    class IteratorTests {

        @Test
        @DisplayName("iterator should iterate over child nodes")
        void iteratorShouldIterateOverChildNodes() {
            YmlNode node = DefaultYmlNode.of(List.of("one", "two", "three"));

            List<String> values = new ArrayList<>();
            for (YmlNode child : node) {
                values.add(child.asText());
            }

            assertThat(values).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("iterator should be empty for scalar node")
        void iteratorShouldBeEmptyForScalarNode() {
            YmlNode node = DefaultYmlNode.of("scalar");

            List<YmlNode> values = new ArrayList<>();
            for (YmlNode child : node) {
                values.add(child);
            }

            assertThat(values).isEmpty();
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should include value")
        void toStringShouldIncludeValue() {
            YmlNode node = DefaultYmlNode.of(Map.of("key", "value"));

            String result = node.toString();

            assertThat(result).contains("YmlNode");
            assertThat(result).contains("key");
            assertThat(result).contains("value");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("equals should return true for same value")
        void equalsShouldReturnTrueForSameValue() {
            YmlNode node1 = DefaultYmlNode.of(Map.of("key", "value"));
            YmlNode node2 = DefaultYmlNode.of(Map.of("key", "value"));

            assertThat(node1).isEqualTo(node2);
        }

        @Test
        @DisplayName("equals should return false for different value")
        void equalsShouldReturnFalseForDifferentValue() {
            YmlNode node1 = DefaultYmlNode.of(Map.of("key1", "value1"));
            YmlNode node2 = DefaultYmlNode.of(Map.of("key2", "value2"));

            assertThat(node1).isNotEqualTo(node2);
        }

        @Test
        @DisplayName("equals should return true for same instance")
        void equalsShouldReturnTrueForSameInstance() {
            YmlNode node = DefaultYmlNode.of(Map.of("key", "value"));

            assertThat(node).isEqualTo(node);
        }

        @Test
        @DisplayName("equals should return false for null")
        void equalsShouldReturnFalseForNull() {
            YmlNode node = DefaultYmlNode.of(Map.of("key", "value"));

            assertThat(node).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode should be same for equal nodes")
        void hashCodeShouldBeSameForEqualNodes() {
            YmlNode node1 = DefaultYmlNode.of(Map.of("key", "value"));
            YmlNode node2 = DefaultYmlNode.of(Map.of("key", "value"));

            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }
    }

    @Nested
    @DisplayName("NodeType Enum Tests")
    class NodeTypeEnumTests {

        @Test
        @DisplayName("NodeType should have all expected values")
        void nodeTypeShouldHaveAllExpectedValues() {
            NodeType[] values = NodeType.values();

            assertThat(values).hasSize(4);
            assertThat(values).containsExactly(
                NodeType.SCALAR,
                NodeType.SEQUENCE,
                NodeType.MAPPING,
                NodeType.NULL
            );
        }

        @Test
        @DisplayName("NodeType.valueOf should return correct enum")
        void nodeTypeValueOfShouldReturnCorrectEnum() {
            assertThat(NodeType.valueOf("SCALAR")).isEqualTo(NodeType.SCALAR);
            assertThat(NodeType.valueOf("SEQUENCE")).isEqualTo(NodeType.SEQUENCE);
            assertThat(NodeType.valueOf("MAPPING")).isEqualTo(NodeType.MAPPING);
            assertThat(NodeType.valueOf("NULL")).isEqualTo(NodeType.NULL);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should navigate complex structure")
        void shouldNavigateComplexStructure() {
            String yaml = """
                database:
                  servers:
                    - name: primary
                      port: 5432
                    - name: replica
                      port: 5433
                  credentials:
                    username: admin
                    password: secret
                """;
            YmlNode node = OpenYml.parseTree(yaml);

            assertThat(node.at("database.servers[0].name").asText()).isEqualTo("primary");
            assertThat(node.at("database.servers[0].port").asInt()).isEqualTo(5432);
            assertThat(node.at("database.servers[1].name").asText()).isEqualTo("replica");
            assertThat(node.at("database.credentials.username").asText()).isEqualTo("admin");
        }

        @Test
        @DisplayName("should chain get calls")
        void shouldChainGetCalls() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            String host = node.get("server").get("host").asText();
            int port = node.get("server").get("port").asInt();

            assertThat(host).isEqualTo("localhost");
            assertThat(port).isEqualTo(8080);
        }
    }
}
