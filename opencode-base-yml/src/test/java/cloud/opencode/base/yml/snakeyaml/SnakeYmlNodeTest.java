package cloud.opencode.base.yml.snakeyaml;

import cloud.opencode.base.yml.YmlNode;
import cloud.opencode.base.yml.YmlNode.NodeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.StringReader;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for {@link SnakeYmlNode}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("SnakeYmlNode Tests")
class SnakeYmlNodeTest {

    private Yaml yaml;

    @BeforeEach
    void setUp() {
        LoaderOptions loaderOptions = new LoaderOptions();
        DumperOptions dumperOptions = new DumperOptions();
        yaml = new Yaml(new SafeConstructor(loaderOptions), new Representer(dumperOptions), dumperOptions, loaderOptions);
    }

    private Node composeYaml(String yamlStr) {
        return yaml.compose(new StringReader(yamlStr));
    }

    // ==================== Factory Method Tests ====================

    @Nested
    @DisplayName("of(Node) Factory Method Tests")
    class OfNodeFactoryTests {

        @Test
        @DisplayName("of should create node from ScalarNode")
        void ofShouldCreateNodeFromScalarNode() {
            Node snakeNode = composeYaml("hello");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
            assertThat(node.asText()).isEqualTo("hello");
        }

        @Test
        @DisplayName("of should create node from MappingNode")
        void ofShouldCreateNodeFromMappingNode() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.MAPPING);
            assertThat(node.get("key").asText()).isEqualTo("value");
        }

        @Test
        @DisplayName("of should create node from SequenceNode")
        void ofShouldCreateNodeFromSequenceNode() {
            Node snakeNode = composeYaml("- one\n- two\n- three");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SEQUENCE);
            assertThat(node.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("of should return null node for null input")
        void ofShouldReturnNullNodeForNullInput() {
            SnakeYmlNode node = SnakeYmlNode.of(null);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.NULL);
            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("of should preserve SnakeYAML node reference")
        void ofShouldPreserveSnakeYamlNodeReference() {
            Node snakeNode = composeYaml("test: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getSnakeNode()).isSameAs(snakeNode);
        }

        @Test
        @DisplayName("of should handle nested structures")
        void ofShouldHandleNestedStructures() {
            String yamlStr = """
                server:
                  host: localhost
                  port: 8080
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.get("server").get("host").asText()).isEqualTo("localhost");
            assertThat(node.get("server").get("port").asInt()).isEqualTo(8080);
        }
    }

    @Nested
    @DisplayName("ofValue(Object) Factory Method Tests")
    class OfValueFactoryTests {

        @Test
        @DisplayName("ofValue should create node from String")
        void ofValueShouldCreateNodeFromString() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("hello");

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
            assertThat(node.asText()).isEqualTo("hello");
        }

        @Test
        @DisplayName("ofValue should create node from Integer")
        void ofValueShouldCreateNodeFromInteger() {
            SnakeYmlNode node = SnakeYmlNode.ofValue(42);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
            assertThat(node.asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("ofValue should create node from Boolean")
        void ofValueShouldCreateNodeFromBoolean() {
            SnakeYmlNode node = SnakeYmlNode.ofValue(true);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
            assertThat(node.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("ofValue should create node from Double")
        void ofValueShouldCreateNodeFromDouble() {
            SnakeYmlNode node = SnakeYmlNode.ofValue(3.14);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
            assertThat(node.asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("ofValue should create node from Map")
        void ofValueShouldCreateNodeFromMap() {
            Map<String, Object> data = Map.of("key", "value");

            SnakeYmlNode node = SnakeYmlNode.ofValue(data);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.MAPPING);
            assertThat(node.get("key").asText()).isEqualTo("value");
        }

        @Test
        @DisplayName("ofValue should create node from List")
        void ofValueShouldCreateNodeFromList() {
            List<String> data = List.of("one", "two", "three");

            SnakeYmlNode node = SnakeYmlNode.ofValue(data);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.SEQUENCE);
            assertThat(node.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("ofValue should return null node for null input")
        void ofValueShouldReturnNullNodeForNullInput() {
            SnakeYmlNode node = SnakeYmlNode.ofValue(null);

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.NULL);
            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("ofValue node should have null SnakeYAML node reference")
        void ofValueNodeShouldHaveNullSnakeYamlNodeReference() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("test");

            assertThat(node.getSnakeNode()).isNull();
        }
    }

    @Nested
    @DisplayName("nullNode() Factory Method Tests")
    class NullNodeFactoryTests {

        @Test
        @DisplayName("nullNode should return null node")
        void nullNodeShouldReturnNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node).isNotNull();
            assertThat(node.getType()).isEqualTo(NodeType.NULL);
            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("nullNode should return same instance")
        void nullNodeShouldReturnSameInstance() {
            SnakeYmlNode node1 = SnakeYmlNode.nullNode();
            SnakeYmlNode node2 = SnakeYmlNode.nullNode();

            assertThat(node1).isSameAs(node2);
        }

        @Test
        @DisplayName("nullNode should be same as of(null)")
        void nullNodeShouldBeSameAsOfNull() {
            SnakeYmlNode node1 = SnakeYmlNode.nullNode();
            SnakeYmlNode node2 = SnakeYmlNode.of(null);
            SnakeYmlNode node3 = SnakeYmlNode.ofValue(null);

            assertThat(node1).isSameAs(node2);
            assertThat(node2).isSameAs(node3);
        }
    }

    // ==================== SnakeYAML-Specific Method Tests ====================

    @Nested
    @DisplayName("getSnakeNode() Tests")
    class GetSnakeNodeTests {

        @Test
        @DisplayName("getSnakeNode should return underlying node")
        void getSnakeNodeShouldReturnUnderlyingNode() {
            Node snakeNode = composeYaml("test: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getSnakeNode()).isSameAs(snakeNode);
        }

        @Test
        @DisplayName("getSnakeNode should return null for value-created node")
        void getSnakeNodeShouldReturnNullForValueCreatedNode() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("test");

            assertThat(node.getSnakeNode()).isNull();
        }

        @Test
        @DisplayName("getSnakeNode should return null for null node")
        void getSnakeNodeShouldReturnNullForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.getSnakeNode()).isNull();
        }
    }

    @Nested
    @DisplayName("getTag() Tests")
    class GetTagTests {

        @Test
        @DisplayName("getTag should return string tag for string scalar")
        void getTagShouldReturnStringTagForStringScalar() {
            Node snakeNode = composeYaml("hello");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getTag()).isEqualTo(Tag.STR);
        }

        @Test
        @DisplayName("getTag should return int tag for integer scalar")
        void getTagShouldReturnIntTagForIntegerScalar() {
            Node snakeNode = composeYaml("42");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getTag()).isEqualTo(Tag.INT);
        }

        @Test
        @DisplayName("getTag should return bool tag for boolean scalar")
        void getTagShouldReturnBoolTagForBooleanScalar() {
            Node snakeNode = composeYaml("true");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getTag()).isEqualTo(Tag.BOOL);
        }

        @Test
        @DisplayName("getTag should return float tag for float scalar")
        void getTagShouldReturnFloatTagForFloatScalar() {
            Node snakeNode = composeYaml("3.14");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getTag()).isEqualTo(Tag.FLOAT);
        }

        @Test
        @DisplayName("getTag should return map tag for mapping node")
        void getTagShouldReturnMapTagForMappingNode() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getTag()).isEqualTo(Tag.MAP);
        }

        @Test
        @DisplayName("getTag should return seq tag for sequence node")
        void getTagShouldReturnSeqTagForSequenceNode() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getTag()).isEqualTo(Tag.SEQ);
        }

        @Test
        @DisplayName("getTag should return null for value-created node")
        void getTagShouldReturnNullForValueCreatedNode() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("test");

            assertThat(node.getTag()).isNull();
        }

        @Test
        @DisplayName("getTag should return null for null node")
        void getTagShouldReturnNullForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.getTag()).isNull();
        }
    }

    @Nested
    @DisplayName("getTagString() Tests")
    class GetTagStringTests {

        @Test
        @DisplayName("getTagString should return tag value as string")
        void getTagStringShouldReturnTagValueAsString() {
            Node snakeNode = composeYaml("hello");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getTagString()).isEqualTo("tag:yaml.org,2002:str");
        }

        @Test
        @DisplayName("getTagString should return null for value-created node")
        void getTagStringShouldReturnNullForValueCreatedNode() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("test");

            assertThat(node.getTagString()).isNull();
        }

        @Test
        @DisplayName("getTagString should return null for null node")
        void getTagStringShouldReturnNullForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.getTagString()).isNull();
        }
    }

    @Nested
    @DisplayName("getAnchor() Tests")
    class GetAnchorTests {

        @Test
        @DisplayName("getAnchor should return anchor name when present")
        void getAnchorShouldReturnAnchorNameWhenPresent() {
            String yamlStr = """
                defaults: &defaults
                  host: localhost
                production:
                  <<: *defaults
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);
            SnakeYmlNode defaults = (SnakeYmlNode) node.get("defaults");

            assertThat(defaults.getAnchor()).isEqualTo("defaults");
        }

        @Test
        @DisplayName("getAnchor should return null when no anchor")
        void getAnchorShouldReturnNullWhenNoAnchor() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getAnchor()).isNull();
        }

        @Test
        @DisplayName("getAnchor should return null for value-created node")
        void getAnchorShouldReturnNullForValueCreatedNode() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("test");

            assertThat(node.getAnchor()).isNull();
        }

        @Test
        @DisplayName("getAnchor should return null for null node")
        void getAnchorShouldReturnNullForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.getAnchor()).isNull();
        }
    }

    @Nested
    @DisplayName("hasAnchor() Tests")
    class HasAnchorTests {

        @Test
        @DisplayName("hasAnchor should return true when anchor present")
        void hasAnchorShouldReturnTrueWhenAnchorPresent() {
            String yamlStr = """
                defaults: &defaults
                  host: localhost
                production:
                  <<: *defaults
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);
            SnakeYmlNode defaults = (SnakeYmlNode) node.get("defaults");

            assertThat(defaults.hasAnchor()).isTrue();
        }

        @Test
        @DisplayName("hasAnchor should return false when no anchor")
        void hasAnchorShouldReturnFalseWhenNoAnchor() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.hasAnchor()).isFalse();
        }

        @Test
        @DisplayName("hasAnchor should return false for null node")
        void hasAnchorShouldReturnFalseForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.hasAnchor()).isFalse();
        }
    }

    @Nested
    @DisplayName("getStartLine() Tests")
    class GetStartLineTests {

        @Test
        @DisplayName("getStartLine should return 1-based line number")
        void getStartLineShouldReturn1BasedLineNumber() {
            String yamlStr = """
                first: 1
                second: 2
                third: 3
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getStartLine()).isEqualTo(1);
        }

        @Test
        @DisplayName("getStartLine should return -1 for value-created node")
        void getStartLineShouldReturnMinusOneForValueCreatedNode() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("test");

            assertThat(node.getStartLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("getStartLine should return -1 for null node")
        void getStartLineShouldReturnMinusOneForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.getStartLine()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("getStartColumn() Tests")
    class GetStartColumnTests {

        @Test
        @DisplayName("getStartColumn should return 1-based column number")
        void getStartColumnShouldReturn1BasedColumnNumber() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getStartColumn()).isEqualTo(1);
        }

        @Test
        @DisplayName("getStartColumn should return -1 for value-created node")
        void getStartColumnShouldReturnMinusOneForValueCreatedNode() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("test");

            assertThat(node.getStartColumn()).isEqualTo(-1);
        }

        @Test
        @DisplayName("getStartColumn should return -1 for null node")
        void getStartColumnShouldReturnMinusOneForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.getStartColumn()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("getEndLine() Tests")
    class GetEndLineTests {

        @Test
        @DisplayName("getEndLine should return 1-based end line number")
        void getEndLineShouldReturn1BasedEndLineNumber() {
            String yamlStr = """
                first: 1
                second: 2
                third: 3
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getEndLine()).isGreaterThan(0);
        }

        @Test
        @DisplayName("getEndLine should return -1 for value-created node")
        void getEndLineShouldReturnMinusOneForValueCreatedNode() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("test");

            assertThat(node.getEndLine()).isEqualTo(-1);
        }

        @Test
        @DisplayName("getEndLine should return -1 for null node")
        void getEndLineShouldReturnMinusOneForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.getEndLine()).isEqualTo(-1);
        }
    }

    // ==================== YmlNode Implementation Tests ====================

    @Nested
    @DisplayName("getType() Tests")
    class GetTypeTests {

        @Test
        @DisplayName("getType should return SCALAR for string value")
        void getTypeShouldReturnScalarForStringValue() {
            Node snakeNode = composeYaml("hello");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
        }

        @Test
        @DisplayName("getType should return SCALAR for integer value")
        void getTypeShouldReturnScalarForIntegerValue() {
            Node snakeNode = composeYaml("42");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
        }

        @Test
        @DisplayName("getType should return MAPPING for map")
        void getTypeShouldReturnMappingForMap() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.MAPPING);
        }

        @Test
        @DisplayName("getType should return SEQUENCE for list")
        void getTypeShouldReturnSequenceForList() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SEQUENCE);
        }

        @Test
        @DisplayName("getType should return NULL for null node")
        void getTypeShouldReturnNullForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.getType()).isEqualTo(NodeType.NULL);
        }
    }

    @Nested
    @DisplayName("asText() Tests")
    class AsTextTests {

        @Test
        @DisplayName("asText should return string value")
        void asTextShouldReturnStringValue() {
            Node snakeNode = composeYaml("hello world");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asText()).isEqualTo("hello world");
        }

        @Test
        @DisplayName("asText should convert integer to string")
        void asTextShouldConvertIntegerToString() {
            Node snakeNode = composeYaml("42");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asText()).isEqualTo("42");
        }

        @Test
        @DisplayName("asText should convert boolean to string")
        void asTextShouldConvertBooleanToString() {
            Node snakeNode = composeYaml("true");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asText()).isEqualTo("true");
        }

        @Test
        @DisplayName("asText should return null for null node")
        void asTextShouldReturnNullForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.asText()).isNull();
        }

        @Test
        @DisplayName("asText with default should return default for null node")
        void asTextWithDefaultShouldReturnDefaultForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.asText("default")).isEqualTo("default");
        }

        @Test
        @DisplayName("asText with default should return value for non-null node")
        void asTextWithDefaultShouldReturnValueForNonNullNode() {
            Node snakeNode = composeYaml("actual");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asText("default")).isEqualTo("actual");
        }
    }

    @Nested
    @DisplayName("asInt() Tests")
    class AsIntTests {

        @Test
        @DisplayName("asInt should return integer value")
        void asIntShouldReturnIntegerValue() {
            Node snakeNode = composeYaml("42");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("asInt should parse string to integer")
        void asIntShouldParseStringToInteger() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("123");

            assertThat(node.asInt()).isEqualTo(123);
        }

        @Test
        @DisplayName("asInt should return 0 for null node")
        void asIntShouldReturnZeroForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.asInt()).isZero();
        }

        @Test
        @DisplayName("asInt should truncate long value")
        void asIntShouldTruncateLongValue() {
            Node snakeNode = composeYaml("100");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asInt()).isEqualTo(100);
        }

        @Test
        @DisplayName("asInt with default should return default on parse error")
        void asIntWithDefaultShouldReturnDefaultOnParseError() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("not a number");

            assertThat(node.asInt(99)).isEqualTo(99);
        }

        @Test
        @DisplayName("asInt with default should return value on success")
        void asIntWithDefaultShouldReturnValueOnSuccess() {
            Node snakeNode = composeYaml("42");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asInt(99)).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("asLong() Tests")
    class AsLongTests {

        @Test
        @DisplayName("asLong should return long value")
        void asLongShouldReturnLongValue() {
            Node snakeNode = composeYaml("9999999999");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asLong()).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("asLong should parse string to long")
        void asLongShouldParseStringToLong() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("9999999999");

            assertThat(node.asLong()).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("asLong should return 0 for null node")
        void asLongShouldReturnZeroForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.asLong()).isZero();
        }

        @Test
        @DisplayName("asLong with default should return default on parse error")
        void asLongWithDefaultShouldReturnDefaultOnParseError() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("not a number");

            assertThat(node.asLong(999L)).isEqualTo(999L);
        }

        @Test
        @DisplayName("asLong with default should return value on success")
        void asLongWithDefaultShouldReturnValueOnSuccess() {
            Node snakeNode = composeYaml("12345");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asLong(999L)).isEqualTo(12345L);
        }
    }

    @Nested
    @DisplayName("asBoolean() Tests")
    class AsBooleanTests {

        @Test
        @DisplayName("asBoolean should return true for true value")
        void asBooleanShouldReturnTrueForTrueValue() {
            Node snakeNode = composeYaml("true");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("asBoolean should return false for false value")
        void asBooleanShouldReturnFalseForFalseValue() {
            Node snakeNode = composeYaml("false");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("asBoolean should parse string to boolean")
        void asBooleanShouldParseStringToBoolean() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("true");

            assertThat(node.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("asBoolean should return false for null node")
        void asBooleanShouldReturnFalseForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("asBoolean with default should return default for null node")
        void asBooleanWithDefaultShouldReturnDefaultForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.asBoolean(true)).isTrue();
        }

        @Test
        @DisplayName("asBoolean with default should return value for non-null node")
        void asBooleanWithDefaultShouldReturnValueForNonNullNode() {
            Node snakeNode = composeYaml("false");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asBoolean(true)).isFalse();
        }
    }

    @Nested
    @DisplayName("asDouble() Tests")
    class AsDoubleTests {

        @Test
        @DisplayName("asDouble should return double value")
        void asDoubleShouldReturnDoubleValue() {
            Node snakeNode = composeYaml("3.14");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("asDouble should parse string to double")
        void asDoubleShouldParseStringToDouble() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("3.14");

            assertThat(node.asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("asDouble should return 0.0 for null node")
        void asDoubleShouldReturnZeroForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.asDouble()).isZero();
        }

        @Test
        @DisplayName("asDouble should convert integer to double")
        void asDoubleShouldConvertIntegerToDouble() {
            Node snakeNode = composeYaml("42");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asDouble()).isEqualTo(42.0);
        }

        @Test
        @DisplayName("asDouble with default should return default on parse error")
        void asDoubleWithDefaultShouldReturnDefaultOnParseError() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("not a number");

            assertThat(node.asDouble(9.99)).isEqualTo(9.99);
        }

        @Test
        @DisplayName("asDouble with default should return value on success")
        void asDoubleWithDefaultShouldReturnValueOnSuccess() {
            Node snakeNode = composeYaml("3.14");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.asDouble(9.99)).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("get(String key) Tests")
    class GetByKeyTests {

        @Test
        @DisplayName("get should return child node by key")
        void getShouldReturnChildNodeByKey() {
            Node snakeNode = composeYaml("name: John\nage: 30");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.get("name").asText()).isEqualTo("John");
            assertThat(node.get("age").asInt()).isEqualTo(30);
        }

        @Test
        @DisplayName("get should return null node for missing key")
        void getShouldReturnNullNodeForMissingKey() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            YmlNode missing = node.get("nonexistent");

            assertThat(missing.isNull()).isTrue();
        }

        @Test
        @DisplayName("get should return null node for non-mapping node")
        void getShouldReturnNullNodeForNonMappingNode() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            YmlNode result = node.get("key");

            assertThat(result.isNull()).isTrue();
        }

        @Test
        @DisplayName("get should preserve SnakeYAML node for child")
        void getShouldPreserveSnakeYamlNodeForChild() {
            Node snakeNode = composeYaml("outer:\n  inner: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);
            SnakeYmlNode inner = (SnakeYmlNode) node.get("outer");

            assertThat(inner.getSnakeNode()).isNotNull();
        }
    }

    @Nested
    @DisplayName("get(int index) Tests")
    class GetByIndexTests {

        @Test
        @DisplayName("get should return child node by index")
        void getShouldReturnChildNodeByIndex() {
            Node snakeNode = composeYaml("- one\n- two\n- three");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.get(0).asText()).isEqualTo("one");
            assertThat(node.get(1).asText()).isEqualTo("two");
            assertThat(node.get(2).asText()).isEqualTo("three");
        }

        @Test
        @DisplayName("get should return null node for out of bounds index")
        void getShouldReturnNullNodeForOutOfBoundsIndex() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            YmlNode result = node.get(10);

            assertThat(result.isNull()).isTrue();
        }

        @Test
        @DisplayName("get should return null node for negative index")
        void getShouldReturnNullNodeForNegativeIndex() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            YmlNode result = node.get(-1);

            assertThat(result.isNull()).isTrue();
        }

        @Test
        @DisplayName("get should return null node for non-sequence node")
        void getShouldReturnNullNodeForNonSequenceNode() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            YmlNode result = node.get(0);

            assertThat(result.isNull()).isTrue();
        }

        @Test
        @DisplayName("get should preserve SnakeYAML node for sequence item")
        void getShouldPreserveSnakeYamlNodeForSequenceItem() {
            Node snakeNode = composeYaml("- first\n- second");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);
            SnakeYmlNode item = (SnakeYmlNode) node.get(0);

            assertThat(item.getSnakeNode()).isNotNull();
        }
    }

    @Nested
    @DisplayName("at(String path) Tests")
    class AtPathTests {

        @Test
        @DisplayName("at should navigate using dot notation")
        void atShouldNavigateUsingDotNotation() {
            String yamlStr = """
                server:
                  host: localhost
                  port: 8080
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.at("server.host").asText()).isEqualTo("localhost");
            assertThat(node.at("server.port").asInt()).isEqualTo(8080);
        }

        @Test
        @DisplayName("at should handle array index notation")
        void atShouldHandleArrayIndexNotation() {
            String yamlStr = """
                items:
                  - first
                  - second
                  - third
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.at("items[0]").asText()).isEqualTo("first");
            assertThat(node.at("items[1]").asText()).isEqualTo("second");
        }

        @Test
        @DisplayName("at should return null node for invalid path")
        void atShouldReturnNullNodeForInvalidPath() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            YmlNode result = node.at("nonexistent.path");

            assertThat(result.isNull()).isTrue();
        }

        @Test
        @DisplayName("at should handle complex nested paths")
        void atShouldHandleComplexNestedPaths() {
            String yamlStr = """
                database:
                  servers:
                    - name: primary
                      port: 5432
                    - name: replica
                      port: 5433
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.at("database.servers[0].name").asText()).isEqualTo("primary");
            assertThat(node.at("database.servers[1].port").asInt()).isEqualTo(5433);
        }
    }

    @Nested
    @DisplayName("has(String key) Tests")
    class HasTests {

        @Test
        @DisplayName("has should return true for existing key")
        void hasShouldReturnTrueForExistingKey() {
            Node snakeNode = composeYaml("name: John\nage: 30");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.has("name")).isTrue();
            assertThat(node.has("age")).isTrue();
        }

        @Test
        @DisplayName("has should return false for missing key")
        void hasShouldReturnFalseForMissingKey() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.has("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("has should return false for non-mapping node")
        void hasShouldReturnFalseForNonMappingNode() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.has("key")).isFalse();
        }

        @Test
        @DisplayName("has should return false for null node")
        void hasShouldReturnFalseForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.has("key")).isFalse();
        }
    }

    @Nested
    @DisplayName("size() Tests")
    class SizeTests {

        @Test
        @DisplayName("size should return map size for mapping node")
        void sizeShouldReturnMapSizeForMappingNode() {
            Node snakeNode = composeYaml("a: 1\nb: 2\nc: 3");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("size should return list size for sequence node")
        void sizeShouldReturnListSizeForSequenceNode() {
            Node snakeNode = composeYaml("- one\n- two\n- three");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("size should return 0 for scalar node")
        void sizeShouldReturnZeroForScalarNode() {
            Node snakeNode = composeYaml("scalar");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.size()).isZero();
        }

        @Test
        @DisplayName("size should return 0 for null node")
        void sizeShouldReturnZeroForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.size()).isZero();
        }
    }

    @Nested
    @DisplayName("keys() Tests")
    class KeysTests {

        @Test
        @DisplayName("keys should return all keys for mapping node")
        void keysShouldReturnAllKeysForMappingNode() {
            Node snakeNode = composeYaml("name: John\nage: 30\ncity: NYC");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            Set<String> keys = node.keys();

            assertThat(keys).containsExactlyInAnyOrder("name", "age", "city");
        }

        @Test
        @DisplayName("keys should return empty set for sequence node")
        void keysShouldReturnEmptySetForSequenceNode() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.keys()).isEmpty();
        }

        @Test
        @DisplayName("keys should return empty set for scalar node")
        void keysShouldReturnEmptySetForScalarNode() {
            Node snakeNode = composeYaml("scalar");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.keys()).isEmpty();
        }

        @Test
        @DisplayName("keys should return empty set for null node")
        void keysShouldReturnEmptySetForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.keys()).isEmpty();
        }
    }

    @Nested
    @DisplayName("values() Tests")
    class ValuesTests {

        @Test
        @DisplayName("values should return all child nodes for mapping")
        void valuesShouldReturnAllChildNodesForMapping() {
            Node snakeNode = composeYaml("a: 1\nb: 2\nc: 3");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            List<YmlNode> values = node.values();

            assertThat(values).hasSize(3);
        }

        @Test
        @DisplayName("values should return all items for sequence")
        void valuesShouldReturnAllItemsForSequence() {
            Node snakeNode = composeYaml("- one\n- two\n- three");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            List<YmlNode> values = node.values();

            assertThat(values).hasSize(3);
            assertThat(values.get(0).asText()).isEqualTo("one");
            assertThat(values.get(1).asText()).isEqualTo("two");
            assertThat(values.get(2).asText()).isEqualTo("three");
        }

        @Test
        @DisplayName("values should return empty list for scalar")
        void valuesShouldReturnEmptyListForScalar() {
            Node snakeNode = composeYaml("scalar");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.values()).isEmpty();
        }

        @Test
        @DisplayName("values should return empty list for null node")
        void valuesShouldReturnEmptyListForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.values()).isEmpty();
        }

        @Test
        @DisplayName("values from mapping should preserve SnakeYAML nodes")
        void valuesFromMappingShouldPreserveSnakeYamlNodes() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);
            List<YmlNode> values = node.values();

            assertThat(values.get(0)).isInstanceOf(SnakeYmlNode.class);
            assertThat(((SnakeYmlNode) values.get(0)).getSnakeNode()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toMap() Tests")
    class ToMapTests {

        @Test
        @DisplayName("toMap should return map for mapping node")
        void toMapShouldReturnMapForMappingNode() {
            Node snakeNode = composeYaml("name: John\nage: 30");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            Map<String, Object> map = node.toMap();

            assertThat(map).containsEntry("name", "John");
            assertThat(map).containsEntry("age", 30L);
        }

        @Test
        @DisplayName("toMap should return empty map for sequence node")
        void toMapShouldReturnEmptyMapForSequenceNode() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.toMap()).isEmpty();
        }

        @Test
        @DisplayName("toMap should return empty map for null node")
        void toMapShouldReturnEmptyMapForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.toMap()).isEmpty();
        }

        @Test
        @DisplayName("toMap should return new map instance")
        void toMapShouldReturnNewMapInstance() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            Map<String, Object> map1 = node.toMap();
            Map<String, Object> map2 = node.toMap();

            assertThat(map1).isNotSameAs(map2);
        }
    }

    @Nested
    @DisplayName("toList() Tests")
    class ToListTests {

        @Test
        @DisplayName("toList should return list for sequence node")
        void toListShouldReturnListForSequenceNode() {
            Node snakeNode = composeYaml("- one\n- two\n- three");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            List<Object> list = node.toList();

            assertThat(list).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("toList should return empty list for mapping node")
        void toListShouldReturnEmptyListForMappingNode() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.toList()).isEmpty();
        }

        @Test
        @DisplayName("toList should return empty list for null node")
        void toListShouldReturnEmptyListForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.toList()).isEmpty();
        }

        @Test
        @DisplayName("toList should return new list instance")
        void toListShouldReturnNewListInstance() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            List<Object> list1 = node.toList();
            List<Object> list2 = node.toList();

            assertThat(list1).isNotSameAs(list2);
        }
    }

    @Nested
    @DisplayName("toYaml() Tests")
    class ToYamlTests {

        @Test
        @DisplayName("toYaml should serialize node to YAML string")
        void toYamlShouldSerializeNodeToYamlString() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            String yaml = node.toYaml();

            assertThat(yaml).contains("key");
            assertThat(yaml).contains("value");
        }

        @Test
        @DisplayName("toYaml should serialize sequence")
        void toYamlShouldSerializeSequence() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            String yaml = node.toYaml();

            assertThat(yaml).contains("one");
            assertThat(yaml).contains("two");
        }
    }

    @Nested
    @DisplayName("getRawValue() Tests")
    class GetRawValueTests {

        @Test
        @DisplayName("getRawValue should return underlying value for mapping")
        void getRawValueShouldReturnUnderlyingValueForMapping() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            Object rawValue = node.getRawValue();

            assertThat(rawValue).isInstanceOf(Map.class);
        }

        @Test
        @DisplayName("getRawValue should return underlying value for sequence")
        void getRawValueShouldReturnUnderlyingValueForSequence() {
            Node snakeNode = composeYaml("- one\n- two");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            Object rawValue = node.getRawValue();

            assertThat(rawValue).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("getRawValue should return null for null node")
        void getRawValueShouldReturnNullForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.getRawValue()).isNull();
        }
    }

    @Nested
    @DisplayName("iterator() Tests")
    class IteratorTests {

        @Test
        @DisplayName("iterator should iterate over sequence items")
        void iteratorShouldIterateOverSequenceItems() {
            Node snakeNode = composeYaml("- one\n- two\n- three");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            List<String> values = new ArrayList<>();
            for (YmlNode child : node) {
                values.add(child.asText());
            }

            assertThat(values).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("iterator should iterate over mapping values")
        void iteratorShouldIterateOverMappingValues() {
            Node snakeNode = composeYaml("a: 1\nb: 2");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            int count = 0;
            for (YmlNode child : node) {
                count++;
            }

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("iterator should be empty for scalar node")
        void iteratorShouldBeEmptyForScalarNode() {
            Node snakeNode = composeYaml("scalar");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            List<YmlNode> values = new ArrayList<>();
            for (YmlNode child : node) {
                values.add(child);
            }

            assertThat(values).isEmpty();
        }

        @Test
        @DisplayName("iterator should be empty for null node")
        void iteratorShouldBeEmptyForNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            List<YmlNode> values = new ArrayList<>();
            for (YmlNode child : node) {
                values.add(child);
            }

            assertThat(values).isEmpty();
        }
    }

    @Nested
    @DisplayName("toString() Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should include type information")
        void toStringShouldIncludeTypeInformation() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            String result = node.toString();

            assertThat(result).contains("SnakeYmlNode");
            assertThat(result).contains("MAPPING");
        }

        @Test
        @DisplayName("toString should include value")
        void toStringShouldIncludeValue() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            String result = node.toString();

            assertThat(result).contains("key");
            assertThat(result).contains("value");
        }

        @Test
        @DisplayName("toString should include line number for node from YAML")
        void toStringShouldIncludeLineNumberForNodeFromYaml() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            String result = node.toString();

            assertThat(result).contains("line=");
        }

        @Test
        @DisplayName("toString should not include line number for value-created node")
        void toStringShouldNotIncludeLineNumberForValueCreatedNode() {
            SnakeYmlNode node = SnakeYmlNode.ofValue("test");

            String result = node.toString();

            assertThat(result).doesNotContain("line=");
        }
    }

    @Nested
    @DisplayName("equals() and hashCode() Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("equals should return true for same value")
        void equalsShouldReturnTrueForSameValue() {
            Node snakeNode1 = composeYaml("key: value");
            Node snakeNode2 = composeYaml("key: value");

            SnakeYmlNode node1 = SnakeYmlNode.of(snakeNode1);
            SnakeYmlNode node2 = SnakeYmlNode.of(snakeNode2);

            assertThat(node1).isEqualTo(node2);
        }

        @Test
        @DisplayName("equals should return false for different value")
        void equalsShouldReturnFalseForDifferentValue() {
            Node snakeNode1 = composeYaml("key1: value1");
            Node snakeNode2 = composeYaml("key2: value2");

            SnakeYmlNode node1 = SnakeYmlNode.of(snakeNode1);
            SnakeYmlNode node2 = SnakeYmlNode.of(snakeNode2);

            assertThat(node1).isNotEqualTo(node2);
        }

        @Test
        @DisplayName("equals should return true for same instance")
        void equalsShouldReturnTrueForSameInstance() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node).isEqualTo(node);
        }

        @Test
        @DisplayName("equals should return false for null")
        void equalsShouldReturnFalseForNull() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node).isNotEqualTo(null);
        }

        @Test
        @DisplayName("equals should return false for different type")
        void equalsShouldReturnFalseForDifferentType() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node).isNotEqualTo("string");
        }

        @Test
        @DisplayName("hashCode should be same for equal nodes")
        void hashCodeShouldBeSameForEqualNodes() {
            Node snakeNode1 = composeYaml("key: value");
            Node snakeNode2 = composeYaml("key: value");

            SnakeYmlNode node1 = SnakeYmlNode.of(snakeNode1);
            SnakeYmlNode node2 = SnakeYmlNode.of(snakeNode2);

            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            Node snakeNode = composeYaml("key: value");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            int hash1 = node.hashCode();
            int hash2 = node.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    // ==================== Different Node Type Tests ====================

    @Nested
    @DisplayName("ScalarNode Type Tests")
    class ScalarNodeTypeTests {

        @Test
        @DisplayName("should handle string scalar")
        void shouldHandleStringScalar() {
            Node snakeNode = composeYaml("hello world");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
            assertThat(node.asText()).isEqualTo("hello world");
            assertThat(node.getTag()).isEqualTo(Tag.STR);
        }

        @Test
        @DisplayName("should handle integer scalar")
        void shouldHandleIntegerScalar() {
            Node snakeNode = composeYaml("42");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
            assertThat(node.asInt()).isEqualTo(42);
            assertThat(node.getTag()).isEqualTo(Tag.INT);
        }

        @Test
        @DisplayName("should handle boolean scalar")
        void shouldHandleBooleanScalar() {
            Node snakeNode = composeYaml("true");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
            assertThat(node.asBoolean()).isTrue();
            assertThat(node.getTag()).isEqualTo(Tag.BOOL);
        }

        @Test
        @DisplayName("should handle float scalar")
        void shouldHandleFloatScalar() {
            Node snakeNode = composeYaml("3.14159");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SCALAR);
            assertThat(node.asDouble()).isEqualTo(3.14159);
            assertThat(node.getTag()).isEqualTo(Tag.FLOAT);
        }

        @Test
        @DisplayName("should handle null scalar")
        void shouldHandleNullScalar() {
            Node snakeNode = composeYaml("null");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.NULL);
            assertThat(node.asText()).isNull();
        }
    }

    @Nested
    @DisplayName("SequenceNode Type Tests")
    class SequenceNodeTypeTests {

        @Test
        @DisplayName("should handle simple sequence")
        void shouldHandleSimpleSequence() {
            Node snakeNode = composeYaml("- apple\n- banana\n- cherry");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SEQUENCE);
            assertThat(node.size()).isEqualTo(3);
            assertThat(node.get(0).asText()).isEqualTo("apple");
            assertThat(node.get(1).asText()).isEqualTo("banana");
            assertThat(node.get(2).asText()).isEqualTo("cherry");
        }

        @Test
        @DisplayName("should handle nested sequence")
        void shouldHandleNestedSequence() {
            String yamlStr = """
                - - a
                  - b
                - - c
                  - d
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SEQUENCE);
            assertThat(node.size()).isEqualTo(2);
            assertThat(node.get(0).get(0).asText()).isEqualTo("a");
            assertThat(node.get(1).get(1).asText()).isEqualTo("d");
        }

        @Test
        @DisplayName("should handle sequence of mappings")
        void shouldHandleSequenceOfMappings() {
            String yamlStr = """
                - name: John
                  age: 30
                - name: Jane
                  age: 25
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SEQUENCE);
            assertThat(node.get(0).get("name").asText()).isEqualTo("John");
            assertThat(node.get(1).get("age").asInt()).isEqualTo(25);
        }

        @Test
        @DisplayName("should handle empty sequence")
        void shouldHandleEmptySequence() {
            Node snakeNode = composeYaml("[]");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.SEQUENCE);
            assertThat(node.size()).isZero();
        }
    }

    @Nested
    @DisplayName("MappingNode Type Tests")
    class MappingNodeTypeTests {

        @Test
        @DisplayName("should handle simple mapping")
        void shouldHandleSimpleMapping() {
            Node snakeNode = composeYaml("name: John\nage: 30\ncity: NYC");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.MAPPING);
            assertThat(node.get("name").asText()).isEqualTo("John");
            assertThat(node.get("age").asInt()).isEqualTo(30);
            assertThat(node.get("city").asText()).isEqualTo("NYC");
        }

        @Test
        @DisplayName("should handle nested mapping")
        void shouldHandleNestedMapping() {
            String yamlStr = """
                person:
                  name: John
                  address:
                    city: NYC
                    zip: "10001"
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.MAPPING);
            assertThat(node.get("person").get("name").asText()).isEqualTo("John");
            assertThat(node.get("person").get("address").get("city").asText()).isEqualTo("NYC");
        }

        @Test
        @DisplayName("should handle mapping with sequence values")
        void shouldHandleMappingWithSequenceValues() {
            String yamlStr = """
                fruits:
                  - apple
                  - banana
                vegetables:
                  - carrot
                  - potato
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.MAPPING);
            assertThat(node.get("fruits").size()).isEqualTo(2);
            assertThat(node.get("vegetables").get(0).asText()).isEqualTo("carrot");
        }

        @Test
        @DisplayName("should handle empty mapping")
        void shouldHandleEmptyMapping() {
            Node snakeNode = composeYaml("{}");

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.getType()).isEqualTo(NodeType.MAPPING);
            assertThat(node.size()).isZero();
        }
    }

    @Nested
    @DisplayName("Null Node Tests")
    class NullNodeTests {

        @Test
        @DisplayName("null node should have NULL type")
        void nullNodeShouldHaveNullType() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.getType()).isEqualTo(NodeType.NULL);
            assertThat(node.isNull()).isTrue();
        }

        @Test
        @DisplayName("null node should return safe defaults")
        void nullNodeShouldReturnSafeDefaults() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.asText()).isNull();
            assertThat(node.asInt()).isZero();
            assertThat(node.asLong()).isZero();
            assertThat(node.asDouble()).isZero();
            assertThat(node.asBoolean()).isFalse();
            assertThat(node.size()).isZero();
            assertThat(node.keys()).isEmpty();
            assertThat(node.values()).isEmpty();
            assertThat(node.toMap()).isEmpty();
            assertThat(node.toList()).isEmpty();
        }

        @Test
        @DisplayName("null node get operations should return null node")
        void nullNodeGetOperationsShouldReturnNullNode() {
            SnakeYmlNode node = SnakeYmlNode.nullNode();

            assertThat(node.get("key").isNull()).isTrue();
            assertThat(node.get(0).isNull()).isTrue();
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should navigate complex structure")
        void shouldNavigateComplexStructure() {
            String yamlStr = """
                database:
                  servers:
                    - name: primary
                      port: 5432
                      replicas:
                        - host: replica1.example.com
                        - host: replica2.example.com
                    - name: secondary
                      port: 5433
                  credentials:
                    username: admin
                    password: secret
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.at("database.servers[0].name").asText()).isEqualTo("primary");
            assertThat(node.at("database.servers[0].replicas[1].host").asText())
                .isEqualTo("replica2.example.com");
            assertThat(node.at("database.credentials.username").asText()).isEqualTo("admin");
        }

        @Test
        @DisplayName("should chain get calls")
        void shouldChainGetCalls() {
            String yamlStr = """
                level1:
                  level2:
                    level3:
                      value: deep
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            String value = node.get("level1").get("level2").get("level3").get("value").asText();

            assertThat(value).isEqualTo("deep");
        }

        @Test
        @DisplayName("should handle mixed types in sequence")
        void shouldHandleMixedTypesInSequence() {
            String yamlStr = """
                items:
                  - string value
                  - 42
                  - true
                  - 3.14
                  - key: value
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            assertThat(node.get("items").get(0).asText()).isEqualTo("string value");
            assertThat(node.get("items").get(1).asInt()).isEqualTo(42);
            assertThat(node.get("items").get(2).asBoolean()).isTrue();
            assertThat(node.get("items").get(3).asDouble()).isEqualTo(3.14);
            assertThat(node.get("items").get(4).get("key").asText()).isEqualTo("value");
        }

        @Test
        @DisplayName("should handle YAML with anchors")
        void shouldHandleYamlWithAnchors() {
            String yamlStr = """
                defaults: &defaults
                  adapter: postgres
                  host: localhost

                development:
                  database: dev_db
                  config: *defaults
                """;
            Node snakeNode = composeYaml(yamlStr);

            SnakeYmlNode node = SnakeYmlNode.of(snakeNode);

            // Test that the anchor is properly set on defaults
            SnakeYmlNode defaults = (SnakeYmlNode) node.get("defaults");
            assertThat(defaults.hasAnchor()).isTrue();
            assertThat(defaults.getAnchor()).isEqualTo("defaults");

            // Test that the anchor content is accessible
            assertThat(defaults.get("adapter").asText()).isEqualTo("postgres");
            assertThat(defaults.get("host").asText()).isEqualTo("localhost");

            // Test that the alias reference resolves to the same content
            assertThat(node.get("development").get("config").get("adapter").asText()).isEqualTo("postgres");
            assertThat(node.get("development").get("database").asText()).isEqualTo("dev_db");
        }

        @Test
        @DisplayName("node type check methods should work correctly")
        void nodeTypeCheckMethodsShouldWorkCorrectly() {
            Node mappingNode = composeYaml("key: value");
            Node sequenceNode = composeYaml("- one\n- two");
            Node scalarNode = composeYaml("scalar");

            SnakeYmlNode mapping = SnakeYmlNode.of(mappingNode);
            SnakeYmlNode sequence = SnakeYmlNode.of(sequenceNode);
            SnakeYmlNode scalar = SnakeYmlNode.of(scalarNode);
            SnakeYmlNode nullNode = SnakeYmlNode.nullNode();

            assertThat(mapping.isMapping()).isTrue();
            assertThat(mapping.isSequence()).isFalse();
            assertThat(mapping.isScalar()).isFalse();
            assertThat(mapping.isNull()).isFalse();

            assertThat(sequence.isSequence()).isTrue();
            assertThat(sequence.isMapping()).isFalse();
            assertThat(sequence.isScalar()).isFalse();
            assertThat(sequence.isNull()).isFalse();

            assertThat(scalar.isScalar()).isTrue();
            assertThat(scalar.isMapping()).isFalse();
            assertThat(scalar.isSequence()).isFalse();
            assertThat(scalar.isNull()).isFalse();

            assertThat(nullNode.isNull()).isTrue();
            assertThat(nullNode.isMapping()).isFalse();
            assertThat(nullNode.isSequence()).isFalse();
            assertThat(nullNode.isScalar()).isFalse();
        }
    }
}
