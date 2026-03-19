package cloud.opencode.base.yml.bind;

import cloud.opencode.base.yml.OpenYml;
import cloud.opencode.base.yml.YmlDocument;
import cloud.opencode.base.yml.exception.YmlBindException;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for YmlBinder
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlBinder Tests")
class YmlBinderTest {

    // ==================== Test Classes ====================

    /**
     * Simple test class with basic field types
     */
    public static class SimpleConfig {
        public String name;
        public int port;
        public long timeout;
        public boolean enabled;
        public double rate;
        public float factor;
        public short count;
        public byte level;
        public char code;
    }

    /**
     * Test class with wrapper types
     */
    public static class WrapperConfig {
        public String name;
        public Integer port;
        public Long timeout;
        public Boolean enabled;
        public Double rate;
        public Float factor;
        public Short count;
        public Byte level;
        public Character code;
    }

    /**
     * Test class with nested object
     */
    public static class NestedConfig {
        public String name;
        public ServerConfig server;
    }

    /**
     * Nested server configuration
     */
    public static class ServerConfig {
        public String host;
        public int port;
    }

    /**
     * Test class with List field
     */
    public static class ListConfig {
        public String name;
        public List<String> items;
        public List<Integer> numbers;
    }

    /**
     * Test class with List of nested objects
     */
    public static class ListNestedConfig {
        public List<ServerConfig> servers;
    }

    /**
     * Test class with Map field
     */
    public static class MapConfig {
        public String name;
        public Map<String, Object> properties;
    }

    /**
     * Test class with YmlProperty annotation
     */
    public static class AnnotatedConfig {
        @YmlProperty("app.name")
        public String appName;

        @YmlProperty(value = "app.port", defaultValue = "8080")
        public int port;

        @YmlProperty(value = "app.required", required = true)
        public String requiredField;
    }

    /**
     * Test class with YmlProperty and default value
     */
    public static class DefaultValueConfig {
        @YmlProperty(value = "missing.field", defaultValue = "default-value")
        public String fieldWithDefault;

        @YmlProperty(value = "missing.port", defaultValue = "9999")
        public int portWithDefault;
    }

    /**
     * Test class with YmlAlias annotation
     */
    public static class AliasConfig {
        @YmlAlias({"db.url", "database.url", "jdbc.url"})
        public String url;

        @YmlAlias({"server.host", "host", "hostname"})
        public String host;
    }

    /**
     * Test class with YmlIgnore annotation
     */
    public static class IgnoreConfig {
        public String name;

        @YmlIgnore
        public String password;

        public int port;
    }

    /**
     * Test class with mixed annotations
     */
    public static class MixedAnnotationsConfig {
        @YmlProperty("app.name")
        public String name;

        @YmlAlias({"app.port", "server.port"})
        public int port;

        @YmlIgnore
        public String secret;

        public String description;
    }

    /**
     * Test class with inheritance
     */
    public static class BaseConfig {
        public String name;
        public int version;
    }

    /**
     * Extended configuration class
     */
    public static class ExtendedConfig extends BaseConfig {
        public String environment;
        public boolean debug;
    }

    /**
     * Test record with basic fields
     */
    public record SimpleRecord(String name, int port, boolean enabled) {}

    /**
     * Test record with nested record
     */
    public record NestedRecord(String name, ServerRecord server) {}

    /**
     * Nested server record
     */
    public record ServerRecord(String host, int port) {}

    /**
     * Test record with List
     */
    public record ListRecord(String name, List<String> items) {}

    /**
     * Private constructor test class (for exception testing)
     */
    public static class PrivateConstructorConfig {
        public String name;

        private PrivateConstructorConfig() {}
    }

    // ==================== Bind from YmlDocument Tests ====================

    @Nested
    @DisplayName("bind(YmlDocument, Class) Tests")
    class BindDocumentTests {

        @Test
        @DisplayName("should bind simple types from document")
        void shouldBindSimpleTypesFromDocument() {
            String yaml = """
                name: test-app
                port: 8080
                timeout: 30000
                enabled: true
                rate: 1.5
                factor: 0.5
                count: 10
                level: 5
                code: A
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.name).isEqualTo("test-app");
            assertThat(config.port).isEqualTo(8080);
            assertThat(config.timeout).isEqualTo(30000L);
            assertThat(config.enabled).isTrue();
            assertThat(config.rate).isEqualTo(1.5);
            assertThat(config.factor).isEqualTo(0.5f);
            assertThat(config.count).isEqualTo((short) 10);
            assertThat(config.level).isEqualTo((byte) 5);
            assertThat(config.code).isEqualTo('A');
        }

        @Test
        @DisplayName("should bind wrapper types from document")
        void shouldBindWrapperTypesFromDocument() {
            String yaml = """
                name: test-app
                port: 8080
                timeout: 30000
                enabled: true
                rate: 1.5
                factor: 0.5
                count: 10
                level: 5
                code: B
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            WrapperConfig config = YmlBinder.bind(doc, WrapperConfig.class);

            assertThat(config.name).isEqualTo("test-app");
            assertThat(config.port).isEqualTo(8080);
            assertThat(config.timeout).isEqualTo(30000L);
            assertThat(config.enabled).isTrue();
            assertThat(config.rate).isEqualTo(1.5);
            assertThat(config.factor).isEqualTo(0.5f);
            assertThat(config.count).isEqualTo((short) 10);
            assertThat(config.level).isEqualTo((byte) 5);
            assertThat(config.code).isEqualTo('B');
        }

        @Test
        @DisplayName("should bind nested objects from document")
        void shouldBindNestedObjectsFromDocument() {
            String yaml = """
                name: my-app
                server:
                  host: localhost
                  port: 8080
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            NestedConfig config = YmlBinder.bind(doc, NestedConfig.class);

            assertThat(config.name).isEqualTo("my-app");
            assertThat(config.server).isNotNull();
            assertThat(config.server.host).isEqualTo("localhost");
            assertThat(config.server.port).isEqualTo(8080);
        }

        @Test
        @DisplayName("should bind list of strings from document")
        void shouldBindListOfStringsFromDocument() {
            String yaml = """
                name: my-app
                items:
                  - one
                  - two
                  - three
                numbers:
                  - 1
                  - 2
                  - 3
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ListConfig config = YmlBinder.bind(doc, ListConfig.class);

            assertThat(config.name).isEqualTo("my-app");
            assertThat(config.items).containsExactly("one", "two", "three");
            assertThat(config.numbers).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("should bind list of nested objects from document")
        void shouldBindListOfNestedObjectsFromDocument() {
            String yaml = """
                servers:
                  - host: server1
                    port: 8080
                  - host: server2
                    port: 8081
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ListNestedConfig config = YmlBinder.bind(doc, ListNestedConfig.class);

            // Note: Generic type information is erased at runtime,
            // so nested objects in lists remain as Maps
            assertThat(config.servers).hasSize(2);
            @SuppressWarnings("unchecked")
            Map<String, Object> server0 = (Map<String, Object>) (Object) config.servers.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> server1 = (Map<String, Object>) (Object) config.servers.get(1);
            assertThat(server0.get("host")).isEqualTo("server1");
            assertThat(server0.get("port")).isEqualTo(8080);
            assertThat(server1.get("host")).isEqualTo("server2");
            assertThat(server1.get("port")).isEqualTo(8081);
        }

        @Test
        @DisplayName("should bind map from document")
        void shouldBindMapFromDocument() {
            String yaml = """
                name: my-app
                properties:
                  key1: value1
                  key2: value2
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            MapConfig config = YmlBinder.bind(doc, MapConfig.class);

            assertThat(config.name).isEqualTo("my-app");
            assertThat(config.properties).containsEntry("key1", "value1");
            assertThat(config.properties).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("should return null for null document root")
        void shouldReturnNullForNullDocumentRoot() {
            YmlDocument doc = YmlDocument.of(null);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config).isNull();
        }

        @Test
        @DisplayName("should bind record from document")
        void shouldBindRecordFromDocument() {
            String yaml = """
                name: test-app
                port: 8080
                enabled: true
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            SimpleRecord record = YmlBinder.bind(doc, SimpleRecord.class);

            assertThat(record.name()).isEqualTo("test-app");
            assertThat(record.port()).isEqualTo(8080);
            assertThat(record.enabled()).isTrue();
        }

        @Test
        @DisplayName("should bind nested record from document")
        void shouldBindNestedRecordFromDocument() {
            String yaml = """
                name: my-app
                server:
                  host: localhost
                  port: 9090
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            NestedRecord record = YmlBinder.bind(doc, NestedRecord.class);

            assertThat(record.name()).isEqualTo("my-app");
            assertThat(record.server()).isNotNull();
            assertThat(record.server().host()).isEqualTo("localhost");
            assertThat(record.server().port()).isEqualTo(9090);
        }

        @Test
        @DisplayName("should bind record with list from document")
        void shouldBindRecordWithListFromDocument() {
            String yaml = """
                name: my-app
                items:
                  - alpha
                  - beta
                  - gamma
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ListRecord record = YmlBinder.bind(doc, ListRecord.class);

            assertThat(record.name()).isEqualTo("my-app");
            assertThat(record.items()).containsExactly("alpha", "beta", "gamma");
        }
    }

    // ==================== Bind with Path Tests ====================

    @Nested
    @DisplayName("bind(YmlDocument, String, Class) Tests")
    class BindWithPathTests {

        @Test
        @DisplayName("should bind from nested path")
        void shouldBindFromNestedPath() {
            String yaml = """
                app:
                  server:
                    host: localhost
                    port: 8080
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ServerConfig config = YmlBinder.bind(doc, "app.server", ServerConfig.class);

            assertThat(config.host).isEqualTo("localhost");
            assertThat(config.port).isEqualTo(8080);
        }

        @Test
        @DisplayName("should bind from single level path")
        void shouldBindFromSingleLevelPath() {
            String yaml = """
                database:
                  host: db.example.com
                  port: 5432
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ServerConfig config = YmlBinder.bind(doc, "database", ServerConfig.class);

            assertThat(config.host).isEqualTo("db.example.com");
            assertThat(config.port).isEqualTo(5432);
        }

        @Test
        @DisplayName("should throw exception for non-existent path")
        void shouldThrowExceptionForNonExistentPath() {
            String yaml = """
                app:
                  name: test
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            // PathResolver.get throws YmlPathException for non-existent paths
            assertThatThrownBy(() -> YmlBinder.bind(doc, "nonexistent.path", ServerConfig.class))
                .isInstanceOf(cloud.opencode.base.yml.exception.YmlPathException.class)
                .hasMessageContaining("Path not found");
        }

        @Test
        @DisplayName("should bind deeply nested path")
        void shouldBindDeeplyNestedPath() {
            String yaml = """
                level1:
                  level2:
                    level3:
                      host: deep-server
                      port: 7777
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ServerConfig config = YmlBinder.bind(doc, "level1.level2.level3", ServerConfig.class);

            assertThat(config.host).isEqualTo("deep-server");
            assertThat(config.port).isEqualTo(7777);
        }

        @Test
        @DisplayName("should bind record from path")
        void shouldBindRecordFromPath() {
            String yaml = """
                config:
                  server:
                    host: record-host
                    port: 1234
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ServerRecord record = YmlBinder.bind(doc, "config.server", ServerRecord.class);

            assertThat(record.host()).isEqualTo("record-host");
            assertThat(record.port()).isEqualTo(1234);
        }
    }

    // ==================== Bind from Raw Object Tests ====================

    @Nested
    @DisplayName("bind(Object, Class) Tests")
    class BindFromObjectTests {

        @Test
        @DisplayName("should bind from Map")
        void shouldBindFromMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "test-app");
            data.put("port", 8080);
            data.put("enabled", true);

            SimpleRecord record = YmlBinder.bind(data, SimpleRecord.class);

            assertThat(record.name()).isEqualTo("test-app");
            assertThat(record.port()).isEqualTo(8080);
            assertThat(record.enabled()).isTrue();
        }

        @Test
        @DisplayName("should bind nested map to nested object")
        void shouldBindNestedMapToNestedObject() {
            Map<String, Object> serverData = new LinkedHashMap<>();
            serverData.put("host", "localhost");
            serverData.put("port", 9090);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "my-app");
            data.put("server", serverData);

            NestedConfig config = YmlBinder.bind(data, NestedConfig.class);

            assertThat(config.name).isEqualTo("my-app");
            assertThat(config.server.host).isEqualTo("localhost");
            assertThat(config.server.port).isEqualTo(9090);
        }

        @Test
        @DisplayName("should return null for null data input")
        void shouldReturnNullForNullDataInput() {
            // bind(Object, Class) returns null when data is null
            SimpleConfig config = YmlBinder.bind((Object) null, SimpleConfig.class);

            assertThat(config).isNull();
        }

        @Test
        @DisplayName("should convert simple type when target is simple")
        void shouldConvertSimpleTypeWhenTargetIsSimple() {
            String result = YmlBinder.bind("hello", String.class);

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("should convert integer string to Integer")
        void shouldConvertIntegerStringToInteger() {
            Integer result = YmlBinder.bind("42", Integer.class);

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("should convert boolean string to Boolean")
        void shouldConvertBooleanStringToBoolean() {
            Boolean result = YmlBinder.bind("true", Boolean.class);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should throw for type mismatch")
        void shouldThrowForTypeMismatch() {
            assertThatThrownBy(() -> YmlBinder.bind("not-a-map", NestedConfig.class))
                .isInstanceOf(YmlBindException.class)
                .hasMessageContaining("Type mismatch");
        }

        @Test
        @DisplayName("should bind with type conversion")
        void shouldBindWithTypeConversion() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("port", "8080");  // String instead of int
            data.put("enabled", "true");  // String instead of boolean

            SimpleRecord record = YmlBinder.bind(data, SimpleRecord.class);

            assertThat(record.port()).isEqualTo(8080);
            assertThat(record.enabled()).isTrue();
        }
    }

    // ==================== toMap Tests ====================

    @Nested
    @DisplayName("toMap Tests")
    class ToMapTests {

        @Test
        @DisplayName("should convert simple object to map")
        void shouldConvertSimpleObjectToMap() {
            ServerConfig config = new ServerConfig();
            config.host = "localhost";
            config.port = 8080;

            Map<String, Object> map = YmlBinder.toMap(config);

            assertThat(map).containsEntry("host", "localhost");
            assertThat(map).containsEntry("port", 8080);
        }

        @Test
        @DisplayName("should convert nested object to map")
        void shouldConvertNestedObjectToMap() {
            ServerConfig server = new ServerConfig();
            server.host = "localhost";
            server.port = 8080;

            NestedConfig config = new NestedConfig();
            config.name = "my-app";
            config.server = server;

            Map<String, Object> map = YmlBinder.toMap(config);

            assertThat(map).containsEntry("name", "my-app");
            assertThat(map).containsKey("server");
            @SuppressWarnings("unchecked")
            Map<String, Object> serverMap = (Map<String, Object>) map.get("server");
            assertThat(serverMap).containsEntry("host", "localhost");
            assertThat(serverMap).containsEntry("port", 8080);
        }

        @Test
        @DisplayName("should convert object with list to map")
        void shouldConvertObjectWithListToMap() {
            ListConfig config = new ListConfig();
            config.name = "list-app";
            config.items = List.of("one", "two", "three");

            Map<String, Object> map = YmlBinder.toMap(config);

            assertThat(map).containsEntry("name", "list-app");
            assertThat(map).containsKey("items");
            @SuppressWarnings("unchecked")
            List<String> items = (List<String>) map.get("items");
            assertThat(items).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("should return null for null input")
        void shouldReturnNullForNullInput() {
            Map<String, Object> map = YmlBinder.toMap(null);

            assertThat(map).isNull();
        }

        @Test
        @DisplayName("should skip null fields")
        void shouldSkipNullFields() {
            ServerConfig config = new ServerConfig();
            config.host = null;
            config.port = 8080;

            Map<String, Object> map = YmlBinder.toMap(config);

            assertThat(map).doesNotContainKey("host");
            assertThat(map).containsEntry("port", 8080);
        }

        @Test
        @DisplayName("should skip fields with YmlIgnore annotation")
        void shouldSkipFieldsWithYmlIgnoreAnnotation() {
            IgnoreConfig config = new IgnoreConfig();
            config.name = "test";
            config.password = "secret123";
            config.port = 8080;

            Map<String, Object> map = YmlBinder.toMap(config);

            assertThat(map).containsEntry("name", "test");
            assertThat(map).containsEntry("port", 8080);
            assertThat(map).doesNotContainKey("password");
        }

        @Test
        @DisplayName("should convert record to map")
        void shouldConvertRecordToMap() {
            SimpleRecord record = new SimpleRecord("test-app", 8080, true);

            Map<String, Object> map = YmlBinder.toMap(record);

            assertThat(map).containsEntry("name", "test-app");
            assertThat(map).containsEntry("port", 8080);
            assertThat(map).containsEntry("enabled", true);
        }

        @Test
        @DisplayName("should convert nested record to map")
        void shouldConvertNestedRecordToMap() {
            ServerRecord server = new ServerRecord("localhost", 9090);
            NestedRecord record = new NestedRecord("my-app", server);

            Map<String, Object> map = YmlBinder.toMap(record);

            assertThat(map).containsEntry("name", "my-app");
            assertThat(map).containsKey("server");
            @SuppressWarnings("unchecked")
            Map<String, Object> serverMap = (Map<String, Object>) map.get("server");
            assertThat(serverMap).containsEntry("host", "localhost");
            assertThat(serverMap).containsEntry("port", 9090);
        }

        @Test
        @DisplayName("should convert Map input to Map output")
        void shouldConvertMapInputToMapOutput() {
            Map<String, Object> input = new LinkedHashMap<>();
            input.put("key1", "value1");
            input.put("nested", Map.of("key2", "value2"));

            Map<String, Object> result = YmlBinder.toMap(input);

            assertThat(result).containsEntry("key1", "value1");
            assertThat(result).containsKey("nested");
        }

        @Test
        @DisplayName("should use YmlProperty name in map")
        void shouldUseYmlPropertyNameInMap() {
            DefaultValueConfig config = new DefaultValueConfig();
            config.fieldWithDefault = "test-value";
            config.portWithDefault = 9999;

            Map<String, Object> map = YmlBinder.toMap(config);

            // YmlProperty annotation specifies "missing.field" as the name
            assertThat(map).containsEntry("missing.field", "test-value");
            assertThat(map).containsEntry("missing.port", 9999);
        }
    }

    // ==================== Annotation Tests ====================

    @Nested
    @DisplayName("YmlProperty Annotation Tests")
    class YmlPropertyAnnotationTests {

        @Test
        @DisplayName("should bind using YmlProperty path")
        void shouldBindUsingYmlPropertyPath() {
            String yaml = """
                app:
                  name: annotated-app
                  port: 9090
                  required: required-value
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            AnnotatedConfig config = YmlBinder.bind(doc, AnnotatedConfig.class);

            assertThat(config.appName).isEqualTo("annotated-app");
            assertThat(config.port).isEqualTo(9090);
            assertThat(config.requiredField).isEqualTo("required-value");
        }

        @Test
        @DisplayName("should use default value when property missing")
        void shouldUseDefaultValueWhenPropertyMissing() {
            String yaml = """
                other: value
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            DefaultValueConfig config = YmlBinder.bind(doc, DefaultValueConfig.class);

            assertThat(config.fieldWithDefault).isEqualTo("default-value");
            assertThat(config.portWithDefault).isEqualTo(9999);
        }

        @Test
        @DisplayName("should throw when required property is missing")
        void shouldThrowWhenRequiredPropertyIsMissing() {
            String yaml = """
                app:
                  name: test
                  port: 8080
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            assertThatThrownBy(() -> YmlBinder.bind(doc, AnnotatedConfig.class))
                .isInstanceOf(YmlBindException.class)
                .hasMessageContaining("Required property")
                .hasMessageContaining("app.required");
        }
    }

    @Nested
    @DisplayName("YmlAlias Annotation Tests")
    class YmlAliasAnnotationTests {

        @Test
        @DisplayName("should bind using first matching alias")
        void shouldBindUsingFirstMatchingAlias() {
            String yaml = """
                db:
                  url: jdbc:mysql://localhost/db
                server:
                  host: localhost
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            AliasConfig config = YmlBinder.bind(doc, AliasConfig.class);

            assertThat(config.url).isEqualTo("jdbc:mysql://localhost/db");
            assertThat(config.host).isEqualTo("localhost");
        }

        @Test
        @DisplayName("should try alternative alias when first not found")
        void shouldTryAlternativeAliasWhenFirstNotFound() {
            String yaml = """
                database:
                  url: jdbc:postgres://localhost/db
                hostname: remote-host
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            AliasConfig config = YmlBinder.bind(doc, AliasConfig.class);

            assertThat(config.url).isEqualTo("jdbc:postgres://localhost/db");
            assertThat(config.host).isEqualTo("remote-host");
        }

        @Test
        @DisplayName("should use last resort alias")
        void shouldUseLastResortAlias() {
            String yaml = """
                jdbc:
                  url: jdbc:h2:mem:test
                host: simple-host
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            AliasConfig config = YmlBinder.bind(doc, AliasConfig.class);

            assertThat(config.url).isEqualTo("jdbc:h2:mem:test");
            assertThat(config.host).isEqualTo("simple-host");
        }
    }

    @Nested
    @DisplayName("YmlIgnore Annotation Tests")
    class YmlIgnoreAnnotationTests {

        @Test
        @DisplayName("should ignore fields with YmlIgnore during binding")
        void shouldIgnoreFieldsWithYmlIgnoreDuringBinding() {
            String yaml = """
                name: test-app
                password: secret123
                port: 8080
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            IgnoreConfig config = YmlBinder.bind(doc, IgnoreConfig.class);

            assertThat(config.name).isEqualTo("test-app");
            assertThat(config.password).isNull();  // Should be ignored
            assertThat(config.port).isEqualTo(8080);
        }
    }

    // ==================== Null Handling Tests ====================

    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {

        @Test
        @DisplayName("should handle null values in yaml")
        void shouldHandleNullValuesInYaml() {
            String yaml = """
                name: test
                port: null
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            WrapperConfig config = YmlBinder.bind(doc, WrapperConfig.class);

            assertThat(config.name).isEqualTo("test");
            assertThat(config.port).isNull();
        }

        @Test
        @DisplayName("should use primitive default for missing primitive fields")
        void shouldUsePrimitiveDefaultForMissingPrimitiveFields() {
            String yaml = """
                name: test
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.name).isEqualTo("test");
            assertThat(config.port).isZero();
            assertThat(config.timeout).isZero();
            assertThat(config.enabled).isFalse();
            assertThat(config.rate).isZero();
            assertThat(config.factor).isZero();
            assertThat(config.count).isZero();
            assertThat(config.level).isZero();
            assertThat(config.code).isEqualTo('\0');
        }

        @Test
        @DisplayName("should leave wrapper fields null when missing")
        void shouldLeaveWrapperFieldsNullWhenMissing() {
            String yaml = """
                name: test
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            WrapperConfig config = YmlBinder.bind(doc, WrapperConfig.class);

            assertThat(config.name).isEqualTo("test");
            assertThat(config.port).isNull();
            assertThat(config.timeout).isNull();
            assertThat(config.enabled).isNull();
            assertThat(config.rate).isNull();
        }

        @Test
        @DisplayName("should handle null nested objects")
        void shouldHandleNullNestedObjects() {
            String yaml = """
                name: test
                server: null
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            NestedConfig config = YmlBinder.bind(doc, NestedConfig.class);

            assertThat(config.name).isEqualTo("test");
            assertThat(config.server).isNull();
        }
    }

    // ==================== Type Conversion Tests ====================

    @Nested
    @DisplayName("Type Conversion Tests")
    class TypeConversionTests {

        @Test
        @DisplayName("should convert string to int")
        void shouldConvertStringToInt() {
            Map<String, Object> data = Map.of("port", "8080");
            YmlDocument doc = YmlDocument.of(data);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.port).isEqualTo(8080);
        }

        @Test
        @DisplayName("should convert string to long")
        void shouldConvertStringToLong() {
            Map<String, Object> data = Map.of("timeout", "9999999999");
            YmlDocument doc = YmlDocument.of(data);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.timeout).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("should convert string to boolean")
        void shouldConvertStringToBoolean() {
            Map<String, Object> data = Map.of("enabled", "true");
            YmlDocument doc = YmlDocument.of(data);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.enabled).isTrue();
        }

        @Test
        @DisplayName("should convert string to double")
        void shouldConvertStringToDouble() {
            Map<String, Object> data = Map.of("rate", "3.14159");
            YmlDocument doc = YmlDocument.of(data);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.rate).isEqualTo(3.14159);
        }

        @Test
        @DisplayName("should convert string to float")
        void shouldConvertStringToFloat() {
            Map<String, Object> data = Map.of("factor", "2.5");
            YmlDocument doc = YmlDocument.of(data);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.factor).isEqualTo(2.5f);
        }

        @Test
        @DisplayName("should convert string to short")
        void shouldConvertStringToShort() {
            Map<String, Object> data = Map.of("count", "100");
            YmlDocument doc = YmlDocument.of(data);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.count).isEqualTo((short) 100);
        }

        @Test
        @DisplayName("should convert string to byte")
        void shouldConvertStringToByte() {
            Map<String, Object> data = Map.of("level", "127");
            YmlDocument doc = YmlDocument.of(data);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.level).isEqualTo((byte) 127);
        }

        @Test
        @DisplayName("should convert string to char")
        void shouldConvertStringToChar() {
            Map<String, Object> data = Map.of("code", "X");
            YmlDocument doc = YmlDocument.of(data);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.code).isEqualTo('X');
        }

        @Test
        @DisplayName("should handle empty string for char")
        void shouldHandleEmptyStringForChar() {
            Map<String, Object> data = Map.of("code", "");
            YmlDocument doc = YmlDocument.of(data);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.code).isEqualTo('\0');
        }

        @Test
        @DisplayName("should convert int to Integer wrapper")
        void shouldConvertIntToIntegerWrapper() {
            Map<String, Object> data = Map.of("port", 8080);
            YmlDocument doc = YmlDocument.of(data);

            WrapperConfig config = YmlBinder.bind(doc, WrapperConfig.class);

            assertThat(config.port).isEqualTo(8080);
        }
    }

    // ==================== Inheritance Tests ====================

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should bind inherited fields")
        void shouldBindInheritedFields() {
            String yaml = """
                name: extended-app
                version: 2
                environment: production
                debug: true
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ExtendedConfig config = YmlBinder.bind(doc, ExtendedConfig.class);

            // Inherited fields
            assertThat(config.name).isEqualTo("extended-app");
            assertThat(config.version).isEqualTo(2);

            // Own fields
            assertThat(config.environment).isEqualTo("production");
            assertThat(config.debug).isTrue();
        }

        @Test
        @DisplayName("should convert extended object to map with inherited fields")
        void shouldConvertExtendedObjectToMapWithInheritedFields() {
            ExtendedConfig config = new ExtendedConfig();
            config.name = "extended-app";
            config.version = 2;
            config.environment = "staging";
            config.debug = false;

            Map<String, Object> map = YmlBinder.toMap(config);

            assertThat(map).containsEntry("name", "extended-app");
            assertThat(map).containsEntry("version", 2);
            assertThat(map).containsEntry("environment", "staging");
            assertThat(map).containsEntry("debug", false);
        }
    }

    // ==================== Exception Tests ====================

    @Nested
    @DisplayName("Exception Tests")
    class ExceptionTests {

        @Test
        @DisplayName("should throw when binding to class without default constructor")
        void shouldThrowWhenBindingToClassWithoutDefaultConstructor() {
            String yaml = """
                name: test
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            assertThatThrownBy(() -> YmlBinder.bind(doc, PrivateConstructorConfig.class))
                .isInstanceOf(YmlBindException.class)
                .hasMessageContaining("Failed to bind");
        }

        @Test
        @DisplayName("should throw for type mismatch with record")
        void shouldThrowForTypeMismatchWithRecord() {
            assertThatThrownBy(() -> YmlBinder.bind("not-a-map", SimpleRecord.class))
                .isInstanceOf(YmlBindException.class)
                .hasMessageContaining("Type mismatch");
        }
    }

    // ==================== Edge Case Tests ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty yaml")
        void shouldHandleEmptyYaml() {
            String yaml = "";
            YmlDocument doc = OpenYml.parse(yaml);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            // Empty document creates an instance with default values
            assertThat(config.name).isNull();
            assertThat(config.port).isZero();
        }

        @Test
        @DisplayName("should handle empty map in yaml")
        void shouldHandleEmptyMapInYaml() {
            Map<String, Object> data = new LinkedHashMap<>();
            YmlDocument doc = YmlDocument.of(data);

            SimpleConfig config = YmlBinder.bind(doc, SimpleConfig.class);

            assertThat(config.name).isNull();
            assertThat(config.port).isZero();
        }

        @Test
        @DisplayName("should handle extra fields in yaml")
        void shouldHandleExtraFieldsInYaml() {
            String yaml = """
                name: test
                port: 8080
                extraField: ignored
                anotherExtra: also-ignored
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ServerConfig config = YmlBinder.bind(doc, ServerConfig.class);

            assertThat(config.host).isNull();  // Not in yaml for ServerConfig
            assertThat(config.port).isEqualTo(8080);
        }

        @Test
        @DisplayName("should handle special characters in string values")
        void shouldHandleSpecialCharactersInStringValues() {
            String yaml = """
                host: "host:with:colons"
                port: 8080
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ServerConfig config = YmlBinder.bind(doc, ServerConfig.class);

            assertThat(config.host).isEqualTo("host:with:colons");
        }

        @Test
        @DisplayName("should handle unicode in string values")
        void shouldHandleUnicodeInStringValues() {
            String yaml = """
                name: "\u4e2d\u6587\u6d4b\u8bd5"
                port: 8080
                enabled: true
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            SimpleRecord record = YmlBinder.bind(doc, SimpleRecord.class);

            assertThat(record.name()).isEqualTo("\u4e2d\u6587\u6d4b\u8bd5");
        }

        @Test
        @DisplayName("should bind empty list")
        void shouldBindEmptyList() {
            String yaml = """
                name: empty-list
                items: []
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ListConfig config = YmlBinder.bind(doc, ListConfig.class);

            assertThat(config.items).isEmpty();
        }

        @Test
        @DisplayName("should bind empty map")
        void shouldBindEmptyMap() {
            String yaml = """
                name: empty-map
                properties: {}
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            MapConfig config = YmlBinder.bind(doc, MapConfig.class);

            assertThat(config.properties).isEmpty();
        }
    }

    // ==================== Round Trip Tests ====================

    @Nested
    @DisplayName("Round Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("should preserve data through bind and toMap")
        void shouldPreserveDataThroughBindAndToMap() {
            String yaml = """
                host: localhost
                port: 8080
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            ServerConfig config = YmlBinder.bind(doc, ServerConfig.class);
            Map<String, Object> map = YmlBinder.toMap(config);
            ServerConfig config2 = YmlBinder.bind(map, ServerConfig.class);

            assertThat(config2.host).isEqualTo(config.host);
            assertThat(config2.port).isEqualTo(config.port);
        }

        @Test
        @DisplayName("should preserve record data through bind and toMap")
        void shouldPreserveRecordDataThroughBindAndToMap() {
            SimpleRecord original = new SimpleRecord("test", 9090, true);

            Map<String, Object> map = YmlBinder.toMap(original);
            SimpleRecord restored = YmlBinder.bind(map, SimpleRecord.class);

            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("should preserve nested data through bind and toMap")
        void shouldPreserveNestedDataThroughBindAndToMap() {
            String yaml = """
                name: nested-app
                server:
                  host: localhost
                  port: 7070
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            NestedConfig config = YmlBinder.bind(doc, NestedConfig.class);
            Map<String, Object> map = YmlBinder.toMap(config);
            NestedConfig config2 = YmlBinder.bind(map, NestedConfig.class);

            assertThat(config2.name).isEqualTo(config.name);
            assertThat(config2.server.host).isEqualTo(config.server.host);
            assertThat(config2.server.port).isEqualTo(config.server.port);
        }
    }

    // ==================== Utility Class Tests ====================

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("constructor should throw AssertionError")
        void constructorShouldThrowAssertionError() throws Exception {
            var constructor = YmlBinder.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                .hasCauseInstanceOf(AssertionError.class);
        }
    }
}
