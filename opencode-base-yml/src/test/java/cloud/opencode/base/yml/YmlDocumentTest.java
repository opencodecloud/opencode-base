package cloud.opencode.base.yml;

import cloud.opencode.base.yml.exception.YmlPathException;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlDocumentTest Tests
 * YmlDocumentTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlDocument Tests")
class YmlDocumentTest {

    private static final String SIMPLE_YAML = """
        server:
          port: 8080
          host: localhost
        items:
          - one
          - two
        """;

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create document from Map")
        void ofShouldCreateDocumentFromMap() {
            Map<String, Object> data = Map.of("key", "value");

            YmlDocument doc = YmlDocument.of(data);

            assertThat(doc).isNotNull();
            assertThat(doc.getString("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("of should create document from List")
        void ofShouldCreateDocumentFromList() {
            List<String> data = List.of("one", "two", "three");

            YmlDocument doc = YmlDocument.of(data);

            assertThat(doc).isNotNull();
            List<String> list = doc.asList();
            assertThat(list).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("of should create document from null")
        void ofShouldCreateDocumentFromNull() {
            YmlDocument doc = YmlDocument.of(null);

            assertThat(doc).isNotNull();
            assertThat(doc.getRoot()).isNull();
        }

        @Test
        @DisplayName("of should create document from scalar")
        void ofShouldCreateDocumentFromScalar() {
            YmlDocument doc = YmlDocument.of("scalar value");

            assertThat(doc).isNotNull();
            assertThat(doc.getRoot()).isEqualTo("scalar value");
        }

        @Test
        @DisplayName("empty should create empty document")
        void emptyShouldCreateEmptyDocument() {
            YmlDocument doc = YmlDocument.empty();

            assertThat(doc).isNotNull();
            assertThat(doc.isEmpty()).isTrue();
            assertThat(doc.asMap()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GetRoot Tests")
    class GetRootTests {

        @Test
        @DisplayName("getRoot should return underlying data")
        void getRootShouldReturnUnderlyingData() {
            Map<String, Object> data = Map.of("key", "value");
            YmlDocument doc = YmlDocument.of(data);

            Object root = doc.getRoot();

            assertThat(root).isEqualTo(data);
        }

        @Test
        @DisplayName("getRoot should return null for null document")
        void getRootShouldReturnNullForNullDocument() {
            YmlDocument doc = YmlDocument.of(null);

            assertThat(doc.getRoot()).isNull();
        }
    }

    @Nested
    @DisplayName("AsMap Tests")
    class AsMapTests {

        @Test
        @DisplayName("asMap should return Map for Map root")
        void asMapShouldReturnMapForMapRoot() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Map<String, Object> map = doc.asMap();

            assertThat(map).containsKey("server");
            assertThat(map).containsKey("items");
        }

        @Test
        @DisplayName("asMap should return empty Map for List root")
        void asMapShouldReturnEmptyMapForListRoot() {
            YmlDocument doc = YmlDocument.of(List.of("one", "two"));

            Map<String, Object> map = doc.asMap();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("asMap should return empty Map for scalar root")
        void asMapShouldReturnEmptyMapForScalarRoot() {
            YmlDocument doc = YmlDocument.of("scalar");

            Map<String, Object> map = doc.asMap();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("asMap should return empty Map for null root")
        void asMapShouldReturnEmptyMapForNullRoot() {
            YmlDocument doc = YmlDocument.of(null);

            Map<String, Object> map = doc.asMap();

            assertThat(map).isEmpty();
        }
    }

    @Nested
    @DisplayName("AsList Tests")
    class AsListTests {

        @Test
        @DisplayName("asList should return List for List root")
        void asListShouldReturnListForListRoot() {
            YmlDocument doc = YmlDocument.of(List.of("one", "two", "three"));

            List<String> list = doc.asList();

            assertThat(list).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("asList should return empty List for Map root")
        void asListShouldReturnEmptyListForMapRoot() {
            YmlDocument doc = YmlDocument.of(Map.of("key", "value"));

            List<Object> list = doc.asList();

            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("asList should return empty List for scalar root")
        void asListShouldReturnEmptyListForScalarRoot() {
            YmlDocument doc = YmlDocument.of("scalar");

            List<Object> list = doc.asList();

            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("asList should return empty List for null root")
        void asListShouldReturnEmptyListForNullRoot() {
            YmlDocument doc = YmlDocument.of(null);

            List<Object> list = doc.asList();

            assertThat(list).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Tests")
    class GetTests {

        @Test
        @DisplayName("get should retrieve value at path")
        void getShouldRetrieveValueAtPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Integer port = doc.get("server.port");

            assertThat(port).isEqualTo(8080);
        }

        @Test
        @DisplayName("get should throw exception for missing path")
        void getShouldThrowExceptionForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            assertThatThrownBy(() -> doc.get("nonexistent"))
                .isInstanceOf(YmlPathException.class);
        }

        @Test
        @DisplayName("get with default should return default for missing path")
        void getWithDefaultShouldReturnDefaultForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            String result = doc.get("nonexistent", "default");

            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("get with default should return value when present")
        void getWithDefaultShouldReturnValueWhenPresent() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            String host = doc.get("server.host", "fallback");

            assertThat(host).isEqualTo("localhost");
        }
    }

    @Nested
    @DisplayName("GetDocument Tests")
    class GetDocumentTests {

        @Test
        @DisplayName("getDocument should return sub-document")
        void getDocumentShouldReturnSubDocument() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            YmlDocument server = doc.getDocument("server");

            assertThat(server).isNotNull();
            assertThat(server.getString("host")).isEqualTo("localhost");
            assertThat(server.getInt("port")).isEqualTo(8080);
        }

        @Test
        @DisplayName("getDocument should return empty document for missing path")
        void getDocumentShouldReturnEmptyDocumentForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            YmlDocument missing = doc.getDocument("nonexistent");

            assertThat(missing).isNotNull();
            assertThat(missing.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("GetString Tests")
    class GetStringTests {

        @Test
        @DisplayName("getString should return string value")
        void getStringShouldReturnStringValue() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            String host = doc.getString("server.host");

            assertThat(host).isEqualTo("localhost");
        }

        @Test
        @DisplayName("getString should throw exception for missing path")
        void getStringShouldThrowExceptionForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            assertThatThrownBy(() -> doc.getString("nonexistent"))
                .isInstanceOf(YmlPathException.class);
        }

        @Test
        @DisplayName("getString with default should return default for missing")
        void getStringWithDefaultShouldReturnDefaultForMissing() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            String result = doc.getString("nonexistent", "default");

            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("getString with default should return value when present")
        void getStringWithDefaultShouldReturnValueWhenPresent() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            String host = doc.getString("server.host", "default");

            assertThat(host).isEqualTo("localhost");
        }

        @Test
        @DisplayName("getString should convert number to string")
        void getStringShouldConvertNumberToString() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            String port = doc.getString("server.port");

            assertThat(port).isEqualTo("8080");
        }
    }

    @Nested
    @DisplayName("GetInt Tests")
    class GetIntTests {

        @Test
        @DisplayName("getInt should return integer value")
        void getIntShouldReturnIntegerValue() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Integer port = doc.getInt("server.port");

            assertThat(port).isEqualTo(8080);
        }

        @Test
        @DisplayName("getInt should return null for missing path")
        void getIntShouldReturnNullForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Integer result = doc.getInt("nonexistent");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getInt with default should return default for missing")
        void getIntWithDefaultShouldReturnDefaultForMissing() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            int result = doc.getInt("nonexistent", 9999);

            assertThat(result).isEqualTo(9999);
        }

        @Test
        @DisplayName("getInt with default should return value when present")
        void getIntWithDefaultShouldReturnValueWhenPresent() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            int port = doc.getInt("server.port", 9999);

            assertThat(port).isEqualTo(8080);
        }
    }

    @Nested
    @DisplayName("GetLong Tests")
    class GetLongTests {

        @Test
        @DisplayName("getLong should return long value")
        void getLongShouldReturnLongValue() {
            String yaml = "bigNumber: 9999999999";
            YmlDocument doc = OpenYml.parse(yaml);

            Long bigNumber = doc.getLong("bigNumber");

            assertThat(bigNumber).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("getLong should return null for missing path")
        void getLongShouldReturnNullForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Long result = doc.getLong("nonexistent");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("GetBoolean Tests")
    class GetBooleanTests {

        @Test
        @DisplayName("getBoolean should return boolean value")
        void getBooleanShouldReturnBooleanValue() {
            String yaml = """
                enabled: true
                disabled: false
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            assertThat(doc.getBoolean("enabled")).isTrue();
            assertThat(doc.getBoolean("disabled")).isFalse();
        }

        @Test
        @DisplayName("getBoolean should return null for missing path")
        void getBooleanShouldReturnNullForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Boolean result = doc.getBoolean("nonexistent");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getBoolean with default should return default for missing")
        void getBooleanWithDefaultShouldReturnDefaultForMissing() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            boolean result = doc.getBoolean("nonexistent", true);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("getBoolean with default should return value when present")
        void getBooleanWithDefaultShouldReturnValueWhenPresent() {
            String yaml = "enabled: false";
            YmlDocument doc = OpenYml.parse(yaml);

            boolean result = doc.getBoolean("enabled", true);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("GetList Tests")
    class GetListTests {

        @Test
        @DisplayName("getList should return list value")
        void getListShouldReturnListValue() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            List<String> items = doc.getList("items");

            assertThat(items).containsExactly("one", "two");
        }

        @Test
        @DisplayName("getList should return null for missing path")
        void getListShouldReturnNullForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            List<String> result = doc.getList("nonexistent");

            // Note: PathResolver.getList returns null for missing paths
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("GetMap Tests")
    class GetMapTests {

        @Test
        @DisplayName("getMap should return map value")
        void getMapShouldReturnMapValue() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Map<String, Object> server = doc.getMap("server");

            assertThat(server).containsEntry("port", 8080);
            assertThat(server).containsEntry("host", "localhost");
        }

        @Test
        @DisplayName("getMap should return null for missing path")
        void getMapShouldReturnNullForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Map<String, Object> result = doc.getMap("nonexistent");

            // Note: PathResolver.getMap returns null for missing paths
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Has Tests")
    class HasTests {

        @Test
        @DisplayName("has should return true for existing path")
        void hasShouldReturnTrueForExistingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            assertThat(doc.has("server")).isTrue();
            assertThat(doc.has("server.port")).isTrue();
            assertThat(doc.has("items")).isTrue();
        }

        @Test
        @DisplayName("has should return false for missing path")
        void hasShouldReturnFalseForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            assertThat(doc.has("nonexistent")).isFalse();
            assertThat(doc.has("server.nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("GetOptional Tests")
    class GetOptionalTests {

        @Test
        @DisplayName("getOptional should return present for existing path")
        void getOptionalShouldReturnPresentForExistingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Optional<String> host = doc.getOptional("server.host");

            assertThat(host).isPresent().hasValue("localhost");
        }

        @Test
        @DisplayName("getOptional should return empty for missing path")
        void getOptionalShouldReturnEmptyForMissingPath() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Optional<String> result = doc.getOptional("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Keys Tests")
    class KeysTests {

        @Test
        @DisplayName("keys should return all root keys")
        void keysShouldReturnAllRootKeys() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            Set<String> keys = doc.keys();

            assertThat(keys).containsExactlyInAnyOrder("server", "items");
        }

        @Test
        @DisplayName("keys should return empty set for non-map root")
        void keysShouldReturnEmptySetForNonMapRoot() {
            YmlDocument doc = YmlDocument.of(List.of("one", "two"));

            Set<String> keys = doc.keys();

            assertThat(keys).isEmpty();
        }

        @Test
        @DisplayName("keys should return empty set for null root")
        void keysShouldReturnEmptySetForNullRoot() {
            YmlDocument doc = YmlDocument.of(null);

            Set<String> keys = doc.keys();

            assertThat(keys).isEmpty();
        }
    }

    @Nested
    @DisplayName("Size Tests")
    class SizeTests {

        @Test
        @DisplayName("size should return map size for map root")
        void sizeShouldReturnMapSizeForMapRoot() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            assertThat(doc.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("size should return list size for list root")
        void sizeShouldReturnListSizeForListRoot() {
            YmlDocument doc = YmlDocument.of(List.of("one", "two", "three"));

            assertThat(doc.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("size should return 0 for scalar root")
        void sizeShouldReturnZeroForScalarRoot() {
            YmlDocument doc = YmlDocument.of("scalar");

            assertThat(doc.size()).isZero();
        }

        @Test
        @DisplayName("size should return 0 for null root")
        void sizeShouldReturnZeroForNullRoot() {
            YmlDocument doc = YmlDocument.of(null);

            assertThat(doc.size()).isZero();
        }
    }

    @Nested
    @DisplayName("IsEmpty Tests")
    class IsEmptyTests {

        @Test
        @DisplayName("isEmpty should return true for empty map")
        void isEmptyShouldReturnTrueForEmptyMap() {
            YmlDocument doc = YmlDocument.empty();

            assertThat(doc.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty should return false for non-empty document")
        void isEmptyShouldReturnFalseForNonEmptyDocument() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            assertThat(doc.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("isEmpty should return true for empty list")
        void isEmptyShouldReturnTrueForEmptyList() {
            YmlDocument doc = YmlDocument.of(List.of());

            assertThat(doc.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should include root data")
        void toStringShouldIncludeRootData() {
            YmlDocument doc = YmlDocument.of(Map.of("key", "value"));

            String result = doc.toString();

            assertThat(result).contains("YmlDocument");
            assertThat(result).contains("key");
            assertThat(result).contains("value");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("equals should return true for same data")
        void equalsShouldReturnTrueForSameData() {
            Map<String, Object> data = Map.of("key", "value");
            YmlDocument doc1 = YmlDocument.of(data);
            YmlDocument doc2 = YmlDocument.of(data);

            assertThat(doc1).isEqualTo(doc2);
        }

        @Test
        @DisplayName("equals should return true for equal data")
        void equalsShouldReturnTrueForEqualData() {
            YmlDocument doc1 = YmlDocument.of(Map.of("key", "value"));
            YmlDocument doc2 = YmlDocument.of(Map.of("key", "value"));

            assertThat(doc1).isEqualTo(doc2);
        }

        @Test
        @DisplayName("equals should return false for different data")
        void equalsShouldReturnFalseForDifferentData() {
            YmlDocument doc1 = YmlDocument.of(Map.of("key1", "value1"));
            YmlDocument doc2 = YmlDocument.of(Map.of("key2", "value2"));

            assertThat(doc1).isNotEqualTo(doc2);
        }

        @Test
        @DisplayName("equals should return false for null")
        void equalsShouldReturnFalseForNull() {
            YmlDocument doc = YmlDocument.of(Map.of("key", "value"));

            assertThat(doc).isNotEqualTo(null);
        }

        @Test
        @DisplayName("equals should return true for same instance")
        void equalsShouldReturnTrueForSameInstance() {
            YmlDocument doc = YmlDocument.of(Map.of("key", "value"));

            assertThat(doc).isEqualTo(doc);
        }

        @Test
        @DisplayName("hashCode should be same for equal documents")
        void hashCodeShouldBeSameForEqualDocuments() {
            YmlDocument doc1 = YmlDocument.of(Map.of("key", "value"));
            YmlDocument doc2 = YmlDocument.of(Map.of("key", "value"));

            assertThat(doc1.hashCode()).isEqualTo(doc2.hashCode());
        }
    }

    @Nested
    @DisplayName("Array Access Tests")
    class ArrayAccessTests {

        @Test
        @DisplayName("should access array elements by index")
        void shouldAccessArrayElementsByIndex() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            String firstItem = doc.get("items[0]");
            String secondItem = doc.get("items[1]");

            assertThat(firstItem).isEqualTo("one");
            assertThat(secondItem).isEqualTo("two");
        }

        @Test
        @DisplayName("should handle nested path with array")
        void shouldHandleNestedPathWithArray() {
            String yaml = """
                users:
                  - name: Alice
                    age: 30
                  - name: Bob
                    age: 25
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            String firstName = doc.getString("users[0].name");
            Integer secondAge = doc.getInt("users[1].age");

            assertThat(firstName).isEqualTo("Alice");
            assertThat(secondAge).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("Deep Nesting Tests")
    class DeepNestingTests {

        @Test
        @DisplayName("should handle deeply nested paths")
        void shouldHandleDeeplyNestedPaths() {
            String yaml = """
                level1:
                  level2:
                    level3:
                      level4:
                        value: deep
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            String value = doc.getString("level1.level2.level3.level4.value");

            assertThat(value).isEqualTo("deep");
        }
    }
}
