package cloud.opencode.base.yml.path;

import cloud.opencode.base.yml.exception.YmlPathException;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PathResolverTest Tests
 * PathResolverTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("PathResolver Tests")
class PathResolverTest {

    private Map<String, Object> testData;

    @BeforeEach
    void setUp() {
        testData = new HashMap<>();

        // Simple values
        testData.put("name", "test");
        testData.put("version", "1.0");
        testData.put("count", 42);
        testData.put("rate", 3.14);
        testData.put("enabled", true);
        testData.put("disabled", false);
        testData.put("nullValue", null);

        // Nested map
        Map<String, Object> server = new HashMap<>();
        server.put("host", "localhost");
        server.put("port", 8080);
        server.put("timeout", 30000L);
        server.put("ssl", true);
        testData.put("server", server);

        // Deeply nested map
        Map<String, Object> database = new HashMap<>();
        Map<String, Object> connection = new HashMap<>();
        connection.put("url", "jdbc:mysql://localhost/db");
        connection.put("maxPoolSize", 10);
        database.put("connection", connection);
        testData.put("database", database);

        // List of strings
        testData.put("tags", List.of("tag1", "tag2", "tag3"));

        // List of maps
        List<Map<String, Object>> users = new ArrayList<>();
        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", "Alice");
        user1.put("age", 30);
        users.add(user1);
        Map<String, Object> user2 = new HashMap<>();
        user2.put("name", "Bob");
        user2.put("age", 25);
        users.add(user2);
        testData.put("users", users);

        // Nested list
        List<List<Integer>> matrix = List.of(
            List.of(1, 2, 3),
            List.of(4, 5, 6),
            List.of(7, 8, 9)
        );
        testData.put("matrix", matrix);

        // Complex nested structure
        Map<String, Object> config = new HashMap<>();
        List<Map<String, Object>> servers = new ArrayList<>();
        Map<String, Object> server1 = new HashMap<>();
        server1.put("name", "server1");
        List<String> hosts = List.of("host1.example.com", "host2.example.com");
        server1.put("hosts", hosts);
        servers.add(server1);
        config.put("servers", servers);
        testData.put("config", config);
    }

    @Nested
    @DisplayName("get() Tests")
    class GetTests {

        @Nested
        @DisplayName("get(Object, String) Tests")
        class GetWithStringPathTests {

            @Test
            @DisplayName("get should return value for simple path")
            void getShouldReturnValueForSimplePath() {
                String result = PathResolver.get(testData, "name");

                assertThat(result).isEqualTo("test");
            }

            @Test
            @DisplayName("get should return value for nested path")
            void getShouldReturnValueForNestedPath() {
                String result = PathResolver.get(testData, "server.host");

                assertThat(result).isEqualTo("localhost");
            }

            @Test
            @DisplayName("get should return value for deeply nested path")
            void getShouldReturnValueForDeeplyNestedPath() {
                String result = PathResolver.get(testData, "database.connection.url");

                assertThat(result).isEqualTo("jdbc:mysql://localhost/db");
            }

            @Test
            @DisplayName("get should return value for array access")
            void getShouldReturnValueForArrayAccess() {
                String result = PathResolver.get(testData, "tags[0]");

                assertThat(result).isEqualTo("tag1");
            }

            @Test
            @DisplayName("get should return value for array with nested property")
            void getShouldReturnValueForArrayWithNestedProperty() {
                String result = PathResolver.get(testData, "users[0].name");

                assertThat(result).isEqualTo("Alice");
            }

            @Test
            @DisplayName("get should return integer value")
            void getShouldReturnIntegerValue() {
                Integer result = PathResolver.get(testData, "server.port");

                assertThat(result).isEqualTo(8080);
            }

            @Test
            @DisplayName("get should return boolean value")
            void getShouldReturnBooleanValue() {
                Boolean result = PathResolver.get(testData, "server.ssl");

                assertThat(result).isTrue();
            }

            @Test
            @DisplayName("get should throw for non-existent path")
            void getShouldThrowForNonExistentPath() {
                assertThatThrownBy(() -> PathResolver.get(testData, "nonexistent"))
                    .isInstanceOf(YmlPathException.class)
                    .hasMessageContaining("Path not found");
            }

            @Test
            @DisplayName("get should throw for null value at path")
            void getShouldThrowForNullValueAtPath() {
                assertThatThrownBy(() -> PathResolver.get(testData, "nullValue"))
                    .isInstanceOf(YmlPathException.class);
            }

            @Test
            @DisplayName("get should return map value")
            void getShouldReturnMapValue() {
                Map<String, Object> result = PathResolver.get(testData, "server");

                assertThat(result).containsEntry("host", "localhost");
            }

            @Test
            @DisplayName("get should return list value")
            void getShouldReturnListValue() {
                List<String> result = PathResolver.get(testData, "tags");

                assertThat(result).containsExactly("tag1", "tag2", "tag3");
            }

            @Test
            @DisplayName("get should handle nested array access")
            void getShouldHandleNestedArrayAccess() {
                Integer result = PathResolver.get(testData, "matrix[1][2]");

                assertThat(result).isEqualTo(6);
            }

            @Test
            @DisplayName("get should handle complex nested path")
            void getShouldHandleComplexNestedPath() {
                String result = PathResolver.get(testData, "config.servers[0].hosts[0]");

                assertThat(result).isEqualTo("host1.example.com");
            }
        }

        @Nested
        @DisplayName("get(Object, String, T) With Default Tests")
        class GetWithDefaultTests {

            @Test
            @DisplayName("get with default should return value when exists")
            void getWithDefaultShouldReturnValueWhenExists() {
                String result = PathResolver.get(testData, "name", "default");

                assertThat(result).isEqualTo("test");
            }

            @Test
            @DisplayName("get with default should return default for non-existent path")
            void getWithDefaultShouldReturnDefaultForNonExistentPath() {
                String result = PathResolver.get(testData, "nonexistent", "default");

                assertThat(result).isEqualTo("default");
            }

            @Test
            @DisplayName("get with default should return default for null value")
            void getWithDefaultShouldReturnDefaultForNullValue() {
                String result = PathResolver.get(testData, "nullValue", "default");

                assertThat(result).isEqualTo("default");
            }

            @Test
            @DisplayName("get with default should work with integer default")
            void getWithDefaultShouldWorkWithIntegerDefault() {
                Integer result = PathResolver.get(testData, "nonexistent", 999);

                assertThat(result).isEqualTo(999);
            }

            @Test
            @DisplayName("get with default should work with boolean default")
            void getWithDefaultShouldWorkWithBooleanDefault() {
                Boolean result = PathResolver.get(testData, "nonexistent", false);

                assertThat(result).isFalse();
            }

            @Test
            @DisplayName("get with default should work with null default")
            void getWithDefaultShouldWorkWithNullDefault() {
                String result = PathResolver.get(testData, "nonexistent", null);

                assertThat(result).isNull();
            }
        }

        @Nested
        @DisplayName("get(Object, YmlPath) Tests")
        class GetWithYmlPathTests {

            @Test
            @DisplayName("get with YmlPath should return value")
            void getWithYmlPathShouldReturnValue() {
                YmlPath path = YmlPath.of("server.host");
                String result = PathResolver.get(testData, path);

                assertThat(result).isEqualTo("localhost");
            }

            @Test
            @DisplayName("get with YmlPath should throw for non-existent path")
            void getWithYmlPathShouldThrowForNonExistentPath() {
                YmlPath path = YmlPath.of("nonexistent");

                assertThatThrownBy(() -> PathResolver.get(testData, path))
                    .isInstanceOf(YmlPathException.class);
            }

            @Test
            @DisplayName("get with root YmlPath should return root object")
            void getWithRootYmlPathShouldReturnRootObject() {
                YmlPath path = YmlPath.root();
                Object result = PathResolver.get(testData, path);

                assertThat(result).isSameAs(testData);
            }

            @Test
            @DisplayName("get with programmatic YmlPath should work")
            void getWithProgrammaticYmlPathShouldWork() {
                YmlPath path = YmlPath.root().child("users").index(1).child("name");
                String result = PathResolver.get(testData, path);

                assertThat(result).isEqualTo("Bob");
            }
        }
    }

    @Nested
    @DisplayName("getString() Tests")
    class GetStringTests {

        @Test
        @DisplayName("getString should return string value")
        void getStringShouldReturnStringValue() {
            String result = PathResolver.getString(testData, "name");

            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("getString should convert integer to string")
        void getStringShouldConvertIntegerToString() {
            String result = PathResolver.getString(testData, "server.port");

            assertThat(result).isEqualTo("8080");
        }

        @Test
        @DisplayName("getString should convert boolean to string")
        void getStringShouldConvertBooleanToString() {
            String result = PathResolver.getString(testData, "server.ssl");

            assertThat(result).isEqualTo("true");
        }

        @Test
        @DisplayName("getString should throw for non-existent path")
        void getStringShouldThrowForNonExistentPath() {
            assertThatThrownBy(() -> PathResolver.getString(testData, "nonexistent"))
                .isInstanceOf(YmlPathException.class);
        }

        @Test
        @DisplayName("getString with default should return value when exists")
        void getStringWithDefaultShouldReturnValueWhenExists() {
            String result = PathResolver.getString(testData, "name", "default");

            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("getString with default should return default for non-existent")
        void getStringWithDefaultShouldReturnDefaultForNonExistent() {
            String result = PathResolver.getString(testData, "nonexistent", "default");

            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("getString with default should convert values to string")
        void getStringWithDefaultShouldConvertValuesToString() {
            String result = PathResolver.getString(testData, "count", "0");

            assertThat(result).isEqualTo("42");
        }
    }

    @Nested
    @DisplayName("getInt() Tests")
    class GetIntTests {

        @Test
        @DisplayName("getInt should return integer value")
        void getIntShouldReturnIntegerValue() {
            Integer result = PathResolver.getInt(testData, "server.port");

            assertThat(result).isEqualTo(8080);
        }

        @Test
        @DisplayName("getInt should return null for non-existent path")
        void getIntShouldReturnNullForNonExistentPath() {
            Integer result = PathResolver.getInt(testData, "nonexistent");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getInt should parse string to integer")
        void getIntShouldParseStringToInteger() {
            Map<String, Object> data = Map.of("value", "123");

            Integer result = PathResolver.getInt(data, "value");

            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("getInt should convert long to integer")
        void getIntShouldConvertLongToInteger() {
            Integer result = PathResolver.getInt(testData, "server.timeout");

            assertThat(result).isEqualTo(30000);
        }

        @Test
        @DisplayName("getInt with default should return value when exists")
        void getIntWithDefaultShouldReturnValueWhenExists() {
            int result = PathResolver.getInt(testData, "server.port", 9999);

            assertThat(result).isEqualTo(8080);
        }

        @Test
        @DisplayName("getInt with default should return default for non-existent")
        void getIntWithDefaultShouldReturnDefaultForNonExistent() {
            int result = PathResolver.getInt(testData, "nonexistent", 9999);

            assertThat(result).isEqualTo(9999);
        }

        @Test
        @DisplayName("getInt should throw for invalid string")
        void getIntShouldThrowForInvalidString() {
            Map<String, Object> data = Map.of("value", "not-a-number");

            assertThatThrownBy(() -> PathResolver.getInt(data, "value"))
                .isInstanceOf(NumberFormatException.class);
        }
    }

    @Nested
    @DisplayName("getLong() Tests")
    class GetLongTests {

        @Test
        @DisplayName("getLong should return long value")
        void getLongShouldReturnLongValue() {
            Long result = PathResolver.getLong(testData, "server.timeout");

            assertThat(result).isEqualTo(30000L);
        }

        @Test
        @DisplayName("getLong should return null for non-existent path")
        void getLongShouldReturnNullForNonExistentPath() {
            Long result = PathResolver.getLong(testData, "nonexistent");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getLong should convert integer to long")
        void getLongShouldConvertIntegerToLong() {
            Long result = PathResolver.getLong(testData, "server.port");

            assertThat(result).isEqualTo(8080L);
        }

        @Test
        @DisplayName("getLong should parse string to long")
        void getLongShouldParseStringToLong() {
            Map<String, Object> data = Map.of("value", "9999999999");

            Long result = PathResolver.getLong(data, "value");

            assertThat(result).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("getLong should throw for invalid string")
        void getLongShouldThrowForInvalidString() {
            Map<String, Object> data = Map.of("value", "not-a-number");

            assertThatThrownBy(() -> PathResolver.getLong(data, "value"))
                .isInstanceOf(NumberFormatException.class);
        }
    }

    @Nested
    @DisplayName("getBoolean() Tests")
    class GetBooleanTests {

        @Test
        @DisplayName("getBoolean should return true value")
        void getBooleanShouldReturnTrueValue() {
            Boolean result = PathResolver.getBoolean(testData, "enabled");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("getBoolean should return false value")
        void getBooleanShouldReturnFalseValue() {
            Boolean result = PathResolver.getBoolean(testData, "disabled");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("getBoolean should return null for non-existent path")
        void getBooleanShouldReturnNullForNonExistentPath() {
            Boolean result = PathResolver.getBoolean(testData, "nonexistent");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getBoolean should parse string true")
        void getBooleanShouldParseStringTrue() {
            Map<String, Object> data = Map.of("value", "true");

            Boolean result = PathResolver.getBoolean(data, "value");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("getBoolean should parse string false")
        void getBooleanShouldParseStringFalse() {
            Map<String, Object> data = Map.of("value", "false");

            Boolean result = PathResolver.getBoolean(data, "value");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("getBoolean should return false for non-boolean string")
        void getBooleanShouldReturnFalseForNonBooleanString() {
            Map<String, Object> data = Map.of("value", "not-a-boolean");

            Boolean result = PathResolver.getBoolean(data, "value");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("getBoolean with default should return value when exists")
        void getBooleanWithDefaultShouldReturnValueWhenExists() {
            boolean result = PathResolver.getBoolean(testData, "enabled", false);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("getBoolean with default should return default for non-existent")
        void getBooleanWithDefaultShouldReturnDefaultForNonExistent() {
            boolean result = PathResolver.getBoolean(testData, "nonexistent", true);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("getList() Tests")
    class GetListTests {

        @Test
        @DisplayName("getList should return list of strings")
        void getListShouldReturnListOfStrings() {
            List<String> result = PathResolver.getList(testData, "tags");

            assertThat(result).containsExactly("tag1", "tag2", "tag3");
        }

        @Test
        @DisplayName("getList should return list of maps")
        void getListShouldReturnListOfMaps() {
            List<Map<String, Object>> result = PathResolver.getList(testData, "users");

            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsEntry("name", "Alice");
        }

        @Test
        @DisplayName("getList should return null for non-existent path")
        void getListShouldReturnNullForNonExistentPath() {
            List<String> result = PathResolver.getList(testData, "nonexistent");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getList should return null for non-list value")
        void getListShouldReturnNullForNonListValue() {
            List<String> result = PathResolver.getList(testData, "name");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getList should return nested list")
        void getListShouldReturnNestedList() {
            List<Integer> result = PathResolver.getList(testData, "matrix[0]");

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("getList should return list at complex path")
        void getListShouldReturnListAtComplexPath() {
            List<String> result = PathResolver.getList(testData, "config.servers[0].hosts");

            assertThat(result).containsExactly("host1.example.com", "host2.example.com");
        }
    }

    @Nested
    @DisplayName("getMap() Tests")
    class GetMapTests {

        @Test
        @DisplayName("getMap should return map value")
        void getMapShouldReturnMapValue() {
            Map<String, Object> result = PathResolver.getMap(testData, "server");

            assertThat(result).containsEntry("host", "localhost");
            assertThat(result).containsEntry("port", 8080);
        }

        @Test
        @DisplayName("getMap should return nested map")
        void getMapShouldReturnNestedMap() {
            Map<String, Object> result = PathResolver.getMap(testData, "database.connection");

            assertThat(result).containsEntry("url", "jdbc:mysql://localhost/db");
        }

        @Test
        @DisplayName("getMap should return null for non-existent path")
        void getMapShouldReturnNullForNonExistentPath() {
            Map<String, Object> result = PathResolver.getMap(testData, "nonexistent");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getMap should return null for non-map value")
        void getMapShouldReturnNullForNonMapValue() {
            Map<String, Object> result = PathResolver.getMap(testData, "name");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getMap should return map from list")
        void getMapShouldReturnMapFromList() {
            Map<String, Object> result = PathResolver.getMap(testData, "users[1]");

            assertThat(result).containsEntry("name", "Bob");
            assertThat(result).containsEntry("age", 25);
        }
    }

    @Nested
    @DisplayName("getOptional() Tests")
    class GetOptionalTests {

        @Test
        @DisplayName("getOptional should return present optional for existing path")
        void getOptionalShouldReturnPresentOptionalForExistingPath() {
            Optional<String> result = PathResolver.getOptional(testData, "name");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("test");
        }

        @Test
        @DisplayName("getOptional should return empty optional for non-existent path")
        void getOptionalShouldReturnEmptyOptionalForNonExistentPath() {
            Optional<String> result = PathResolver.getOptional(testData, "nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getOptional should return empty optional for null value")
        void getOptionalShouldReturnEmptyOptionalForNullValue() {
            Optional<String> result = PathResolver.getOptional(testData, "nullValue");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getOptional should work with nested paths")
        void getOptionalShouldWorkWithNestedPaths() {
            Optional<Integer> result = PathResolver.getOptional(testData, "server.port");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(8080);
        }

        @Test
        @DisplayName("getOptional should work with array paths")
        void getOptionalShouldWorkWithArrayPaths() {
            Optional<String> result = PathResolver.getOptional(testData, "users[0].name");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("getOptional should integrate well with Optional methods")
        void getOptionalShouldIntegrateWellWithOptionalMethods() {
            String result = PathResolver.<String>getOptional(testData, "nonexistent")
                .orElse("fallback");

            assertThat(result).isEqualTo("fallback");
        }
    }

    @Nested
    @DisplayName("has() Tests")
    class HasTests {

        @Test
        @DisplayName("has should return true for existing path")
        void hasShouldReturnTrueForExistingPath() {
            boolean result = PathResolver.has(testData, "name");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("has should return false for non-existent path")
        void hasShouldReturnFalseForNonExistentPath() {
            boolean result = PathResolver.has(testData, "nonexistent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("has should return true for nested path")
        void hasShouldReturnTrueForNestedPath() {
            boolean result = PathResolver.has(testData, "server.host");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("has should return false for null value")
        void hasShouldReturnFalseForNullValue() {
            boolean result = PathResolver.has(testData, "nullValue");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("has should return true for array element")
        void hasShouldReturnTrueForArrayElement() {
            boolean result = PathResolver.has(testData, "tags[0]");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("has should return false for out of bounds index")
        void hasShouldReturnFalseForOutOfBoundsIndex() {
            boolean result = PathResolver.has(testData, "tags[100]");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("has should return false for negative index")
        void hasShouldReturnFalseForNegativeIndex() {
            boolean result = PathResolver.has(testData, "tags[-1]");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("has should return true for map value")
        void hasShouldReturnTrueForMapValue() {
            boolean result = PathResolver.has(testData, "server");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("has should return true for list value")
        void hasShouldReturnTrueForListValue() {
            boolean result = PathResolver.has(testData, "tags");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("has should handle deep paths")
        void hasShouldHandleDeepPaths() {
            assertThat(PathResolver.has(testData, "config.servers[0].hosts[0]")).isTrue();
            assertThat(PathResolver.has(testData, "config.servers[0].hosts[10]")).isFalse();
        }
    }

    @Nested
    @DisplayName("resolve() Tests")
    class ResolveTests {

        @Test
        @DisplayName("resolve should return root for root path")
        void resolveShouldReturnRootForRootPath() {
            Object result = PathResolver.resolve(testData, YmlPath.root());

            assertThat(result).isSameAs(testData);
        }

        @Test
        @DisplayName("resolve should return null for null root")
        void resolveShouldReturnNullForNullRoot() {
            Object result = PathResolver.resolve(null, YmlPath.of("name"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("resolve should return null when property not in map")
        void resolveShouldReturnNullWhenPropertyNotInMap() {
            Object result = PathResolver.resolve(testData, YmlPath.of("nonexistent"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("resolve should return null when property segment on non-map")
        void resolveShouldReturnNullWhenPropertySegmentOnNonMap() {
            Object result = PathResolver.resolve(testData, YmlPath.of("name.child"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("resolve should return null when index segment on non-list")
        void resolveShouldReturnNullWhenIndexSegmentOnNonList() {
            Object result = PathResolver.resolve(testData, YmlPath.of("name[0]"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("resolve should return null when index out of bounds")
        void resolveShouldReturnNullWhenIndexOutOfBounds() {
            Object result = PathResolver.resolve(testData, YmlPath.of("tags[999]"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("resolve should return null when negative index")
        void resolveShouldReturnNullWhenNegativeIndex() {
            Object result = PathResolver.resolve(testData, YmlPath.of("tags[-1]"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("resolve should handle intermediate null values")
        void resolveShouldHandleIntermediateNullValues() {
            Map<String, Object> data = new HashMap<>();
            data.put("parent", null);

            Object result = PathResolver.resolve(data, YmlPath.of("parent.child"));

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Default Value Handling Tests")
    class DefaultValueHandlingTests {

        @Test
        @DisplayName("should use default when path not found")
        void shouldUseDefaultWhenPathNotFound() {
            String result = PathResolver.get(testData, "missing.path", "default-value");

            assertThat(result).isEqualTo("default-value");
        }

        @Test
        @DisplayName("should use default when intermediate path not found")
        void shouldUseDefaultWhenIntermediatePathNotFound() {
            Integer result = PathResolver.get(testData, "missing.nested.value", 42);

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("should use default when array index out of bounds")
        void shouldUseDefaultWhenArrayIndexOutOfBounds() {
            String result = PathResolver.get(testData, "tags[100]", "not-found");

            assertThat(result).isEqualTo("not-found");
        }

        @Test
        @DisplayName("should use default for null value")
        void shouldUseDefaultForNullValue() {
            String result = PathResolver.get(testData, "nullValue", "default");

            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("getInt should use default correctly")
        void getIntShouldUseDefaultCorrectly() {
            int result = PathResolver.getInt(testData, "missing", -1);

            assertThat(result).isEqualTo(-1);
        }

        @Test
        @DisplayName("getBoolean should use default correctly")
        void getBooleanShouldUseDefaultCorrectly() {
            boolean result = PathResolver.getBoolean(testData, "missing", true);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("getString should use default correctly")
        void getStringShouldUseDefaultCorrectly() {
            String result = PathResolver.getString(testData, "missing", "fallback");

            assertThat(result).isEqualTo("fallback");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty map")
        void shouldHandleEmptyMap() {
            Map<String, Object> emptyMap = new HashMap<>();

            assertThat(PathResolver.has(emptyMap, "anything")).isFalse();
            assertThat(PathResolver.getOptional(emptyMap, "anything")).isEmpty();
        }

        @Test
        @DisplayName("should handle empty list")
        void shouldHandleEmptyList() {
            Map<String, Object> data = Map.of("items", List.of());

            assertThat(PathResolver.has(data, "items[0]")).isFalse();
        }

        @Test
        @DisplayName("should handle empty path")
        void shouldHandleEmptyPath() {
            Object result = PathResolver.resolve(testData, YmlPath.of(""));

            assertThat(result).isSameAs(testData);
        }

        @Test
        @DisplayName("should handle list as root")
        void shouldHandleListAsRoot() {
            List<Map<String, Object>> listRoot = List.of(
                Map.of("name", "first"),
                Map.of("name", "second")
            );

            String result = PathResolver.get(listRoot, "[0].name");

            assertThat(result).isEqualTo("first");
        }

        @Test
        @DisplayName("should handle deeply nested structure")
        void shouldHandleDeeplyNestedStructure() {
            Map<String, Object> deep = new HashMap<>();
            Map<String, Object> current = deep;
            for (int i = 0; i < 10; i++) {
                Map<String, Object> next = new HashMap<>();
                current.put("level" + i, next);
                current = next;
            }
            current.put("value", "found");

            String result = PathResolver.get(deep, "level0.level1.level2.level3.level4.level5.level6.level7.level8.level9.value");

            assertThat(result).isEqualTo("found");
        }

        @Test
        @DisplayName("should handle special characters in property names")
        void shouldHandleSpecialCharactersInPropertyNames() {
            Map<String, Object> data = Map.of("my-property_123", "value");

            String result = PathResolver.get(data, "my-property_123");

            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("should handle numeric string as map key")
        void shouldHandleNumericStringAsMapKey() {
            Map<String, Object> data = Map.of("123", "value");

            String result = PathResolver.get(data, "123");

            assertThat(result).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("Type Conversion Tests")
    class TypeConversionTests {

        @Test
        @DisplayName("should handle implicit type conversion")
        void shouldHandleImplicitTypeConversion() {
            Map<String, Object> data = new HashMap<>();
            data.put("intValue", 42);
            data.put("doubleValue", 3.14);
            data.put("boolValue", true);
            data.put("stringValue", "hello");

            // These should work due to generic type erasure
            Object intResult = PathResolver.get(data, "intValue");
            Object doubleResult = PathResolver.get(data, "doubleValue");
            Object boolResult = PathResolver.get(data, "boolValue");
            Object stringResult = PathResolver.get(data, "stringValue");

            assertThat(intResult).isInstanceOf(Integer.class);
            assertThat(doubleResult).isInstanceOf(Double.class);
            assertThat(boolResult).isInstanceOf(Boolean.class);
            assertThat(stringResult).isInstanceOf(String.class);
        }

        @Test
        @DisplayName("getInt should handle double values")
        void getIntShouldHandleDoubleValues() {
            Map<String, Object> data = Map.of("value", 3.7);

            Integer result = PathResolver.getInt(data, "value");

            assertThat(result).isEqualTo(3); // truncates decimal
        }

        @Test
        @DisplayName("getLong should handle double values")
        void getLongShouldHandleDoubleValues() {
            Map<String, Object> data = Map.of("value", 3.7);

            Long result = PathResolver.getLong(data, "value");

            assertThat(result).isEqualTo(3L); // truncates decimal
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("PathResolver should not be instantiable")
        void pathResolverShouldNotBeInstantiable() throws Exception {
            var constructor = PathResolver.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                .hasCauseInstanceOf(AssertionError.class);
        }
    }
}
