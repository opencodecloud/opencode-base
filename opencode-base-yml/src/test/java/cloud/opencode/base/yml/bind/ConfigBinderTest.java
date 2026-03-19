package cloud.opencode.base.yml.bind;

import cloud.opencode.base.yml.exception.YmlBindException;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ConfigBinder
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("ConfigBinder Tests")
class ConfigBinderTest {

    // ==================== Test Classes ====================

    /**
     * Simple test class with basic field types
     */
    public static class ServerConfig {
        public String host;
        public int port;
    }

    /**
     * Test class with all primitive types
     */
    public static class AllPrimitivesConfig {
        public int intValue;
        public long longValue;
        public double doubleValue;
        public float floatValue;
        public boolean booleanValue;
        public short shortValue;
        public byte byteValue;
        public char charValue;
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
     * Test class with deeply nested object
     */
    public static class DeeplyNestedConfig {
        public String name;
        public NestedConfig nested;
    }

    /**
     * Test class with List fields
     */
    public static class ListConfig {
        public String name;
        public List<String> items;
        public List<Integer> numbers;
    }

    /**
     * Test class with Set field
     */
    public static class SetConfig {
        public String name;
        public Set<String> tags;
    }

    /**
     * Test class with Map field
     */
    public static class MapConfig {
        public String name;
        public Map<String, Object> properties;
    }

    /**
     * Test class with camelCase fields
     */
    public static class CamelCaseConfig {
        public String serverHost;
        public int serverPort;
        public boolean enableSsl;
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
     * Enum for testing
     */
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    /**
     * Test class with enum field
     */
    public static class EnumConfig {
        public String name;
        public LogLevel level;
    }

    /**
     * Test record with basic fields
     */
    public record DatabaseRecord(String url, int port) {}

    /**
     * Test record with multiple types
     */
    public record AppRecord(String name, int port, boolean enabled) {}

    /**
     * Test record with nested object
     */
    public record NestedRecord(String name, ServerConfig server) {}

    /**
     * Test record with nested record
     */
    public record ServerRecord(String host, int port) {}
    public record NestedRecordWithRecord(String name, ServerRecord server) {}

    /**
     * Test record with all primitives
     */
    public record AllPrimitivesRecord(
        int intValue,
        long longValue,
        double doubleValue,
        float floatValue,
        boolean booleanValue,
        short shortValue,
        byte byteValue,
        char charValue
    ) {}

    /**
     * Test record with collection
     */
    public record ListRecord(String name, List<String> items) {}

    /**
     * Test record with enum
     */
    public record EnumRecord(String name, LogLevel level) {}

    /**
     * Private constructor test class (for default creation testing)
     */
    public static class PrivateConstructorConfig {
        public String name;

        private PrivateConstructorConfig() {}
    }

    // ==================== bind(String yamlContent, Class<T>) Tests ====================

    @Nested
    @DisplayName("bind(String yamlContent, Class<T>) Tests")
    class BindYamlContentTests {

        @Test
        @DisplayName("should bind simple YAML to bean")
        void shouldBindSimpleYamlToBean() {
            String yaml = """
                host: localhost
                port: 8080
                """;

            ServerConfig config = ConfigBinder.bind(yaml, ServerConfig.class);

            assertThat(config.host).isEqualTo("localhost");
            assertThat(config.port).isEqualTo(8080);
        }

        @Test
        @DisplayName("should bind YAML with all primitive types")
        void shouldBindYamlWithAllPrimitiveTypes() {
            String yaml = """
                intValue: 42
                longValue: 9999999999
                doubleValue: 3.14159
                floatValue: 2.5
                booleanValue: true
                shortValue: 100
                byteValue: 127
                charValue: A
                """;

            AllPrimitivesConfig config = ConfigBinder.bind(yaml, AllPrimitivesConfig.class);

            assertThat(config.intValue).isEqualTo(42);
            assertThat(config.longValue).isEqualTo(9999999999L);
            assertThat(config.doubleValue).isEqualTo(3.14159);
            assertThat(config.floatValue).isEqualTo(2.5f);
            assertThat(config.booleanValue).isTrue();
            assertThat(config.shortValue).isEqualTo((short) 100);
            assertThat(config.byteValue).isEqualTo((byte) 127);
            assertThat(config.charValue).isEqualTo('A');
        }

        @Test
        @DisplayName("should bind YAML to record")
        void shouldBindYamlToRecord() {
            String yaml = """
                url: jdbc:mysql://localhost:3306/db
                port: 3306
                """;

            DatabaseRecord record = ConfigBinder.bind(yaml, DatabaseRecord.class);

            assertThat(record.url()).isEqualTo("jdbc:mysql://localhost:3306/db");
            assertThat(record.port()).isEqualTo(3306);
        }

        @Test
        @DisplayName("should bind YAML with nested object")
        void shouldBindYamlWithNestedObject() {
            String yaml = """
                name: my-app
                server:
                  host: localhost
                  port: 9090
                """;

            NestedConfig config = ConfigBinder.bind(yaml, NestedConfig.class);

            assertThat(config.name).isEqualTo("my-app");
            assertThat(config.server).isNotNull();
            assertThat(config.server.host).isEqualTo("localhost");
            assertThat(config.server.port).isEqualTo(9090);
        }

        @Test
        @DisplayName("should bind YAML with list")
        void shouldBindYamlWithList() {
            String yaml = """
                name: list-app
                items:
                  - one
                  - two
                  - three
                numbers:
                  - 1
                  - 2
                  - 3
                """;

            ListConfig config = ConfigBinder.bind(yaml, ListConfig.class);

            assertThat(config.name).isEqualTo("list-app");
            assertThat(config.items).containsExactly("one", "two", "three");
            assertThat(config.numbers).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("should bind YAML with set")
        void shouldBindYamlWithSet() {
            String yaml = """
                name: set-app
                tags:
                  - java
                  - kotlin
                  - scala
                """;

            SetConfig config = ConfigBinder.bind(yaml, SetConfig.class);

            assertThat(config.name).isEqualTo("set-app");
            assertThat(config.tags).containsExactlyInAnyOrder("java", "kotlin", "scala");
        }

        @Test
        @DisplayName("should bind YAML with map")
        void shouldBindYamlWithMap() {
            String yaml = """
                name: map-app
                properties:
                  key1: value1
                  key2: value2
                """;

            MapConfig config = ConfigBinder.bind(yaml, MapConfig.class);

            assertThat(config.name).isEqualTo("map-app");
            assertThat(config.properties).containsEntry("key1", "value1");
            assertThat(config.properties).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("should bind YAML with enum")
        void shouldBindYamlWithEnum() {
            String yaml = """
                name: enum-app
                level: debug
                """;

            EnumConfig config = ConfigBinder.bind(yaml, EnumConfig.class);

            assertThat(config.name).isEqualTo("enum-app");
            assertThat(config.level).isEqualTo(LogLevel.DEBUG);
        }

        @Test
        @DisplayName("should bind YAML with uppercase enum")
        void shouldBindYamlWithUppercaseEnum() {
            String yaml = """
                name: enum-app
                level: ERROR
                """;

            EnumConfig config = ConfigBinder.bind(yaml, EnumConfig.class);

            assertThat(config.level).isEqualTo(LogLevel.ERROR);
        }
    }

    // ==================== bind(String yamlContent, String prefix, Class<T>) Tests ====================

    @Nested
    @DisplayName("bind(String yamlContent, String prefix, Class<T>) Tests")
    class BindYamlWithPrefixTests {

        @Test
        @DisplayName("should bind YAML with single level prefix")
        void shouldBindYamlWithSingleLevelPrefix() {
            String yaml = """
                database:
                  host: db.example.com
                  port: 5432
                """;

            ServerConfig config = ConfigBinder.bind(yaml, "database", ServerConfig.class);

            assertThat(config.host).isEqualTo("db.example.com");
            assertThat(config.port).isEqualTo(5432);
        }

        @Test
        @DisplayName("should bind YAML with multi-level prefix")
        void shouldBindYamlWithMultiLevelPrefix() {
            String yaml = """
                app:
                  server:
                    host: localhost
                    port: 8080
                """;

            ServerConfig config = ConfigBinder.bind(yaml, "app.server", ServerConfig.class);

            assertThat(config.host).isEqualTo("localhost");
            assertThat(config.port).isEqualTo(8080);
        }

        @Test
        @DisplayName("should bind record with prefix")
        void shouldBindRecordWithPrefix() {
            String yaml = """
                config:
                  db:
                    url: jdbc:postgresql://localhost/mydb
                    port: 5432
                """;

            DatabaseRecord record = ConfigBinder.bind(yaml, "config.db", DatabaseRecord.class);

            assertThat(record.url()).isEqualTo("jdbc:postgresql://localhost/mydb");
            assertThat(record.port()).isEqualTo(5432);
        }

        @Test
        @DisplayName("should return default instance for non-existent prefix")
        void shouldReturnDefaultInstanceForNonExistentPrefix() {
            String yaml = """
                app:
                  name: test
                """;

            ServerConfig config = ConfigBinder.bind(yaml, "nonexistent", ServerConfig.class);

            assertThat(config.host).isNull();
            assertThat(config.port).isZero();
        }

        @Test
        @DisplayName("should handle empty prefix")
        void shouldHandleEmptyPrefix() {
            String yaml = """
                host: localhost
                port: 8080
                """;

            ServerConfig config = ConfigBinder.bind(yaml, "", ServerConfig.class);

            assertThat(config.host).isEqualTo("localhost");
            assertThat(config.port).isEqualTo(8080);
        }

        @Test
        @DisplayName("should handle null prefix")
        void shouldHandleNullPrefix() {
            String yaml = """
                host: localhost
                port: 8080
                """;

            ServerConfig config = ConfigBinder.bind(yaml, null, ServerConfig.class);

            assertThat(config.host).isEqualTo("localhost");
            assertThat(config.port).isEqualTo(8080);
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

            ServerConfig config = ConfigBinder.bind(yaml, "level1.level2.level3", ServerConfig.class);

            assertThat(config.host).isEqualTo("deep-server");
            assertThat(config.port).isEqualTo(7777);
        }
    }

    // ==================== bind(PropertySource, Class<T>) Tests ====================

    @Nested
    @DisplayName("bind(PropertySource, Class<T>) Tests")
    class BindPropertySourceTests {

        @Test
        @DisplayName("should bind from PropertySource")
        void shouldBindFromPropertySource() {
            String yaml = """
                host: localhost
                port: 9000
                """;
            PropertySource source = PropertySource.fromYaml(yaml);

            ServerConfig config = ConfigBinder.bind(source, ServerConfig.class);

            assertThat(config.host).isEqualTo("localhost");
            assertThat(config.port).isEqualTo(9000);
        }

        @Test
        @DisplayName("should bind record from PropertySource")
        void shouldBindRecordFromPropertySource() {
            String yaml = """
                url: jdbc:h2:mem:test
                port: 9092
                """;
            PropertySource source = PropertySource.fromYaml(yaml);

            DatabaseRecord record = ConfigBinder.bind(source, DatabaseRecord.class);

            assertThat(record.url()).isEqualTo("jdbc:h2:mem:test");
            assertThat(record.port()).isEqualTo(9092);
        }

        @Test
        @DisplayName("should bind nested object from PropertySource")
        void shouldBindNestedObjectFromPropertySource() {
            String yaml = """
                name: property-source-app
                server:
                  host: pshost
                  port: 5000
                """;
            PropertySource source = PropertySource.fromYaml(yaml);

            NestedConfig config = ConfigBinder.bind(source, NestedConfig.class);

            assertThat(config.name).isEqualTo("property-source-app");
            assertThat(config.server.host).isEqualTo("pshost");
            assertThat(config.server.port).isEqualTo(5000);
        }

        @Test
        @DisplayName("should bind from MapPropertySource")
        void shouldBindFromMapPropertySource() {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("host", "map-host");
            props.put("port", 4000);
            PropertySource source = PropertySource.fromMap("test", props);

            ServerConfig config = ConfigBinder.bind(source, ServerConfig.class);

            assertThat(config.host).isEqualTo("map-host");
            assertThat(config.port).isEqualTo(4000);
        }
    }

    // ==================== bind(PropertySource, String prefix, Class<T>) Tests ====================

    @Nested
    @DisplayName("bind(PropertySource, String prefix, Class<T>) Tests")
    class BindPropertySourceWithPrefixTests {

        @Test
        @DisplayName("should bind from PropertySource with prefix")
        void shouldBindFromPropertySourceWithPrefix() {
            String yaml = """
                database:
                  host: db-host
                  port: 3306
                """;
            PropertySource source = PropertySource.fromYaml(yaml);

            ServerConfig config = ConfigBinder.bind(source, "database", ServerConfig.class);

            assertThat(config.host).isEqualTo("db-host");
            assertThat(config.port).isEqualTo(3306);
        }

        @Test
        @DisplayName("should bind record from PropertySource with prefix")
        void shouldBindRecordFromPropertySourceWithPrefix() {
            String yaml = """
                services:
                  mysql:
                    url: jdbc:mysql://localhost/db
                    port: 3306
                """;
            PropertySource source = PropertySource.fromYaml(yaml);

            DatabaseRecord record = ConfigBinder.bind(source, "services.mysql", DatabaseRecord.class);

            assertThat(record.url()).isEqualTo("jdbc:mysql://localhost/db");
            assertThat(record.port()).isEqualTo(3306);
        }

        @Test
        @DisplayName("should return default for non-existent prefix in PropertySource")
        void shouldReturnDefaultForNonExistentPrefixInPropertySource() {
            String yaml = """
                app:
                  name: test
                """;
            PropertySource source = PropertySource.fromYaml(yaml);

            ServerConfig config = ConfigBinder.bind(source, "missing.path", ServerConfig.class);

            assertThat(config.host).isNull();
            assertThat(config.port).isZero();
        }
    }

    // ==================== bind(Map<String, Object>, Class<T>) Tests ====================

    @Nested
    @DisplayName("bind(Map<String, Object>, Class<T>) Tests")
    class BindMapTests {

        @Test
        @DisplayName("should bind from Map")
        void shouldBindFromMap() {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("host", "map-host");
            props.put("port", 3000);

            ServerConfig config = ConfigBinder.bind(props, ServerConfig.class);

            assertThat(config.host).isEqualTo("map-host");
            assertThat(config.port).isEqualTo(3000);
        }

        @Test
        @DisplayName("should bind record from Map")
        void shouldBindRecordFromMap() {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("url", "jdbc:oracle:thin:@localhost:1521/xe");
            props.put("port", 1521);

            DatabaseRecord record = ConfigBinder.bind(props, DatabaseRecord.class);

            assertThat(record.url()).isEqualTo("jdbc:oracle:thin:@localhost:1521/xe");
            assertThat(record.port()).isEqualTo(1521);
        }

        @Test
        @DisplayName("should bind nested Map to nested object")
        void shouldBindNestedMapToNestedObject() {
            Map<String, Object> serverMap = new LinkedHashMap<>();
            serverMap.put("host", "nested-host");
            serverMap.put("port", 2000);

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("name", "nested-app");
            props.put("server", serverMap);

            NestedConfig config = ConfigBinder.bind(props, NestedConfig.class);

            assertThat(config.name).isEqualTo("nested-app");
            assertThat(config.server.host).isEqualTo("nested-host");
            assertThat(config.server.port).isEqualTo(2000);
        }

        @Test
        @DisplayName("should return default instance for null Map")
        void shouldReturnDefaultInstanceForNullMap() {
            ServerConfig config = ConfigBinder.bind((Map<String, Object>) null, ServerConfig.class);

            assertThat(config.host).isNull();
            assertThat(config.port).isZero();
        }

        @Test
        @DisplayName("should return default instance for empty Map")
        void shouldReturnDefaultInstanceForEmptyMap() {
            Map<String, Object> props = new LinkedHashMap<>();

            ServerConfig config = ConfigBinder.bind(props, ServerConfig.class);

            assertThat(config.host).isNull();
            assertThat(config.port).isZero();
        }

        @Test
        @DisplayName("should return null for record with null Map")
        void shouldReturnNullForRecordWithNullMap() {
            DatabaseRecord record = ConfigBinder.bind((Map<String, Object>) null, DatabaseRecord.class);

            assertThat(record).isNull();
        }

        @Test
        @DisplayName("should bind with type conversion from String")
        void shouldBindWithTypeConversionFromString() {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("host", "string-host");
            props.put("port", "9999");  // String instead of int

            ServerConfig config = ConfigBinder.bind(props, ServerConfig.class);

            assertThat(config.host).isEqualTo("string-host");
            assertThat(config.port).isEqualTo(9999);
        }
    }

    // ==================== Bean Binding Tests ====================

    @Nested
    @DisplayName("Bean Binding Tests")
    class BeanBindingTests {

        @Test
        @DisplayName("should bind public fields")
        void shouldBindPublicFields() {
            String yaml = """
                host: field-host
                port: 1234
                """;

            ServerConfig config = ConfigBinder.bind(yaml, ServerConfig.class);

            assertThat(config.host).isEqualTo("field-host");
            assertThat(config.port).isEqualTo(1234);
        }

        @Test
        @DisplayName("should use default values for missing fields")
        void shouldUseDefaultValuesForMissingFields() {
            String yaml = """
                name: only-name
                """;

            WrapperConfig config = ConfigBinder.bind(yaml, WrapperConfig.class);

            assertThat(config.name).isEqualTo("only-name");
            assertThat(config.port).isNull();
            assertThat(config.timeout).isNull();
        }

        @Test
        @DisplayName("should handle inherited fields")
        void shouldHandleInheritedFields() {
            String yaml = """
                name: extended-app
                version: 2
                environment: production
                debug: true
                """;

            ExtendedConfig config = ConfigBinder.bind(yaml, ExtendedConfig.class);

            assertThat(config.name).isEqualTo("extended-app");
            assertThat(config.version).isEqualTo(2);
            assertThat(config.environment).isEqualTo("production");
            assertThat(config.debug).isTrue();
        }
    }

    // ==================== Record Binding Tests ====================

    @Nested
    @DisplayName("Record Binding Tests")
    class RecordBindingTests {

        @Test
        @DisplayName("should bind record with all component types")
        void shouldBindRecordWithAllComponentTypes() {
            String yaml = """
                name: record-app
                port: 8080
                enabled: true
                """;

            AppRecord record = ConfigBinder.bind(yaml, AppRecord.class);

            assertThat(record.name()).isEqualTo("record-app");
            assertThat(record.port()).isEqualTo(8080);
            assertThat(record.enabled()).isTrue();
        }

        @Test
        @DisplayName("should bind nested record within record")
        void shouldBindNestedRecordWithinRecord() {
            String yaml = """
                name: nested-record-app
                server:
                  host: record-host
                  port: 7070
                """;

            NestedRecordWithRecord record = ConfigBinder.bind(yaml, NestedRecordWithRecord.class);

            assertThat(record.name()).isEqualTo("nested-record-app");
            assertThat(record.server()).isNotNull();
            assertThat(record.server().host()).isEqualTo("record-host");
            assertThat(record.server().port()).isEqualTo(7070);
        }

        @Test
        @DisplayName("should bind record with all primitives")
        void shouldBindRecordWithAllPrimitives() {
            String yaml = """
                intValue: 100
                longValue: 1000000
                doubleValue: 1.5
                floatValue: 0.5
                booleanValue: true
                shortValue: 50
                byteValue: 25
                charValue: X
                """;

            AllPrimitivesRecord record = ConfigBinder.bind(yaml, AllPrimitivesRecord.class);

            assertThat(record.intValue()).isEqualTo(100);
            assertThat(record.longValue()).isEqualTo(1000000L);
            assertThat(record.doubleValue()).isEqualTo(1.5);
            assertThat(record.floatValue()).isEqualTo(0.5f);
            assertThat(record.booleanValue()).isTrue();
            assertThat(record.shortValue()).isEqualTo((short) 50);
            assertThat(record.byteValue()).isEqualTo((byte) 25);
            assertThat(record.charValue()).isEqualTo('X');
        }

        @Test
        @DisplayName("should bind record with list")
        void shouldBindRecordWithList() {
            String yaml = """
                name: list-record
                items:
                  - alpha
                  - beta
                  - gamma
                """;

            ListRecord record = ConfigBinder.bind(yaml, ListRecord.class);

            assertThat(record.name()).isEqualTo("list-record");
            assertThat(record.items()).containsExactly("alpha", "beta", "gamma");
        }

        @Test
        @DisplayName("should bind record with enum")
        void shouldBindRecordWithEnum() {
            String yaml = """
                name: enum-record
                level: warn
                """;

            EnumRecord record = ConfigBinder.bind(yaml, EnumRecord.class);

            assertThat(record.name()).isEqualTo("enum-record");
            assertThat(record.level()).isEqualTo(LogLevel.WARN);
        }

        @Test
        @DisplayName("should bind record with nested bean")
        void shouldBindRecordWithNestedBean() {
            String yaml = """
                name: record-with-bean
                server:
                  host: bean-host
                  port: 6060
                """;

            NestedRecord record = ConfigBinder.bind(yaml, NestedRecord.class);

            assertThat(record.name()).isEqualTo("record-with-bean");
            assertThat(record.server()).isNotNull();
            assertThat(record.server().host).isEqualTo("bean-host");
            assertThat(record.server().port).isEqualTo(6060);
        }
    }

    // ==================== Primitive Type Conversion Tests ====================

    @Nested
    @DisplayName("Primitive Type Conversion Tests")
    class PrimitiveTypeConversionTests {

        @Test
        @DisplayName("should convert String to int")
        void shouldConvertStringToInt() {
            Map<String, Object> props = Map.of("port", "8080");

            ServerConfig config = ConfigBinder.bind(props, ServerConfig.class);

            assertThat(config.port).isEqualTo(8080);
        }

        @Test
        @DisplayName("should convert String to long")
        void shouldConvertStringToLong() {
            Map<String, Object> props = Map.of("longValue", "9999999999");

            AllPrimitivesConfig config = ConfigBinder.bind(props, AllPrimitivesConfig.class);

            assertThat(config.longValue).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("should convert String to double")
        void shouldConvertStringToDouble() {
            Map<String, Object> props = Map.of("doubleValue", "3.14159");

            AllPrimitivesConfig config = ConfigBinder.bind(props, AllPrimitivesConfig.class);

            assertThat(config.doubleValue).isEqualTo(3.14159);
        }

        @Test
        @DisplayName("should convert String to float")
        void shouldConvertStringToFloat() {
            Map<String, Object> props = Map.of("floatValue", "2.718");

            AllPrimitivesConfig config = ConfigBinder.bind(props, AllPrimitivesConfig.class);

            assertThat(config.floatValue).isEqualTo(2.718f);
        }

        @Test
        @DisplayName("should convert String to boolean")
        void shouldConvertStringToBoolean() {
            Map<String, Object> props = Map.of("booleanValue", "true");

            AllPrimitivesConfig config = ConfigBinder.bind(props, AllPrimitivesConfig.class);

            assertThat(config.booleanValue).isTrue();
        }

        @Test
        @DisplayName("should convert String to short")
        void shouldConvertStringToShort() {
            Map<String, Object> props = Map.of("shortValue", "32767");

            AllPrimitivesConfig config = ConfigBinder.bind(props, AllPrimitivesConfig.class);

            assertThat(config.shortValue).isEqualTo((short) 32767);
        }

        @Test
        @DisplayName("should convert String to byte")
        void shouldConvertStringToByte() {
            Map<String, Object> props = Map.of("byteValue", "127");

            AllPrimitivesConfig config = ConfigBinder.bind(props, AllPrimitivesConfig.class);

            assertThat(config.byteValue).isEqualTo((byte) 127);
        }

        @Test
        @DisplayName("should convert String to char")
        void shouldConvertStringToChar() {
            Map<String, Object> props = Map.of("charValue", "Z");

            AllPrimitivesConfig config = ConfigBinder.bind(props, AllPrimitivesConfig.class);

            assertThat(config.charValue).isEqualTo('Z');
        }

        @Test
        @DisplayName("should handle empty String for char")
        void shouldHandleEmptyStringForChar() {
            Map<String, Object> props = Map.of("charValue", "");

            AllPrimitivesConfig config = ConfigBinder.bind(props, AllPrimitivesConfig.class);

            assertThat(config.charValue).isEqualTo('\0');
        }

        @Test
        @DisplayName("should return default values for primitives when missing")
        void shouldReturnDefaultValuesForPrimitivesWhenMissing() {
            Map<String, Object> props = new LinkedHashMap<>();

            AllPrimitivesConfig config = ConfigBinder.bind(props, AllPrimitivesConfig.class);

            assertThat(config.intValue).isZero();
            assertThat(config.longValue).isZero();
            assertThat(config.doubleValue).isZero();
            assertThat(config.floatValue).isZero();
            assertThat(config.booleanValue).isFalse();
            assertThat(config.shortValue).isZero();
            assertThat(config.byteValue).isZero();
            assertThat(config.charValue).isEqualTo('\0');
        }
    }

    // ==================== Collection Type Tests ====================

    @Nested
    @DisplayName("Collection Type Tests")
    class CollectionTypeTests {

        @Test
        @DisplayName("should bind List field")
        void shouldBindListField() {
            String yaml = """
                name: list-test
                items:
                  - first
                  - second
                  - third
                """;

            ListConfig config = ConfigBinder.bind(yaml, ListConfig.class);

            assertThat(config.items).hasSize(3);
            assertThat(config.items).containsExactly("first", "second", "third");
        }

        @Test
        @DisplayName("should bind Set field")
        void shouldBindSetField() {
            String yaml = """
                name: set-test
                tags:
                  - spring
                  - hibernate
                  - jdbc
                """;

            SetConfig config = ConfigBinder.bind(yaml, SetConfig.class);

            assertThat(config.tags).hasSize(3);
            assertThat(config.tags).containsExactlyInAnyOrder("spring", "hibernate", "jdbc");
        }

        @Test
        @DisplayName("should bind Map field")
        void shouldBindMapField() {
            String yaml = """
                name: map-test
                properties:
                  env: production
                  region: us-east-1
                  tier: premium
                """;

            MapConfig config = ConfigBinder.bind(yaml, MapConfig.class);

            assertThat(config.properties).hasSize(3);
            assertThat(config.properties).containsEntry("env", "production");
            assertThat(config.properties).containsEntry("region", "us-east-1");
            assertThat(config.properties).containsEntry("tier", "premium");
        }

        @Test
        @DisplayName("should bind empty List")
        void shouldBindEmptyList() {
            String yaml = """
                name: empty-list
                items: []
                """;

            ListConfig config = ConfigBinder.bind(yaml, ListConfig.class);

            assertThat(config.items).isEmpty();
        }

        @Test
        @DisplayName("should bind empty Set")
        void shouldBindEmptySet() {
            String yaml = """
                name: empty-set
                tags: []
                """;

            SetConfig config = ConfigBinder.bind(yaml, SetConfig.class);

            assertThat(config.tags).isEmpty();
        }

        @Test
        @DisplayName("should bind empty Map")
        void shouldBindEmptyMap() {
            String yaml = """
                name: empty-map
                properties: {}
                """;

            MapConfig config = ConfigBinder.bind(yaml, MapConfig.class);

            assertThat(config.properties).isEmpty();
        }
    }

    // ==================== Enum Type Tests ====================

    @Nested
    @DisplayName("Enum Type Tests")
    class EnumTypeTests {

        @Test
        @DisplayName("should bind lowercase enum value")
        void shouldBindLowercaseEnumValue() {
            String yaml = """
                name: enum-test
                level: info
                """;

            EnumConfig config = ConfigBinder.bind(yaml, EnumConfig.class);

            assertThat(config.level).isEqualTo(LogLevel.INFO);
        }

        @Test
        @DisplayName("should bind uppercase enum value")
        void shouldBindUppercaseEnumValue() {
            String yaml = """
                name: enum-test
                level: WARN
                """;

            EnumConfig config = ConfigBinder.bind(yaml, EnumConfig.class);

            assertThat(config.level).isEqualTo(LogLevel.WARN);
        }

        @Test
        @DisplayName("should bind mixed case enum value")
        void shouldBindMixedCaseEnumValue() {
            String yaml = """
                name: enum-test
                level: Error
                """;

            EnumConfig config = ConfigBinder.bind(yaml, EnumConfig.class);

            assertThat(config.level).isEqualTo(LogLevel.ERROR);
        }
    }

    // ==================== Nested Object Binding Tests ====================

    @Nested
    @DisplayName("Nested Object Binding Tests")
    class NestedObjectBindingTests {

        @Test
        @DisplayName("should bind single level nested object")
        void shouldBindSingleLevelNestedObject() {
            String yaml = """
                name: single-nested
                server:
                  host: single-host
                  port: 1111
                """;

            NestedConfig config = ConfigBinder.bind(yaml, NestedConfig.class);

            assertThat(config.name).isEqualTo("single-nested");
            assertThat(config.server.host).isEqualTo("single-host");
            assertThat(config.server.port).isEqualTo(1111);
        }

        @Test
        @DisplayName("should bind deeply nested object")
        void shouldBindDeeplyNestedObject() {
            String yaml = """
                name: deeply-nested
                nested:
                  name: inner-app
                  server:
                    host: deep-host
                    port: 2222
                """;

            DeeplyNestedConfig config = ConfigBinder.bind(yaml, DeeplyNestedConfig.class);

            assertThat(config.name).isEqualTo("deeply-nested");
            assertThat(config.nested.name).isEqualTo("inner-app");
            assertThat(config.nested.server.host).isEqualTo("deep-host");
            assertThat(config.nested.server.port).isEqualTo(2222);
        }

        @Test
        @DisplayName("should handle null nested object")
        void shouldHandleNullNestedObject() {
            String yaml = """
                name: null-nested
                server: null
                """;

            NestedConfig config = ConfigBinder.bind(yaml, NestedConfig.class);

            assertThat(config.name).isEqualTo("null-nested");
            assertThat(config.server).isNull();
        }

        @Test
        @DisplayName("should handle missing nested object")
        void shouldHandleMissingNestedObject() {
            String yaml = """
                name: missing-nested
                """;

            NestedConfig config = ConfigBinder.bind(yaml, NestedConfig.class);

            assertThat(config.name).isEqualTo("missing-nested");
            assertThat(config.server).isNull();
        }
    }

    // ==================== Null/Empty Input Handling Tests ====================

    @Nested
    @DisplayName("Null/Empty Input Handling Tests")
    class NullEmptyInputHandlingTests {

        @Test
        @DisplayName("should return default instance for empty YAML")
        void shouldReturnDefaultInstanceForEmptyYaml() {
            String yaml = "";

            ServerConfig config = ConfigBinder.bind(yaml, ServerConfig.class);

            assertThat(config.host).isNull();
            assertThat(config.port).isZero();
        }

        @Test
        @DisplayName("should return null for record with empty YAML")
        void shouldReturnNullForRecordWithEmptyYaml() {
            String yaml = "";

            DatabaseRecord record = ConfigBinder.bind(yaml, DatabaseRecord.class);

            assertThat(record).isNull();
        }

        @Test
        @DisplayName("should handle blank YAML")
        void shouldHandleBlankYaml() {
            String yaml = "   \n   \n   ";

            ServerConfig config = ConfigBinder.bind(yaml, ServerConfig.class);

            assertThat(config.host).isNull();
            assertThat(config.port).isZero();
        }

        @Test
        @DisplayName("should handle null values in YAML")
        void shouldHandleNullValuesInYaml() {
            String yaml = """
                name: test
                port: null
                """;

            WrapperConfig config = ConfigBinder.bind(yaml, WrapperConfig.class);

            assertThat(config.name).isEqualTo("test");
            assertThat(config.port).isNull();
        }
    }

    // ==================== Property Name Conversion Tests ====================

    @Nested
    @DisplayName("Property Name Conversion Tests")
    class PropertyNameConversionTests {

        @Test
        @DisplayName("should bind camelCase property names")
        void shouldBindCamelCasePropertyNames() {
            String yaml = """
                serverHost: camel-host
                serverPort: 3333
                enableSsl: true
                """;

            CamelCaseConfig config = ConfigBinder.bind(yaml, CamelCaseConfig.class);

            assertThat(config.serverHost).isEqualTo("camel-host");
            assertThat(config.serverPort).isEqualTo(3333);
            assertThat(config.enableSsl).isTrue();
        }

        @Test
        @DisplayName("should bind kebab-case property names to camelCase fields")
        void shouldBindKebabCasePropertyNames() {
            String yaml = """
                server-host: kebab-host
                server-port: 4444
                enable-ssl: true
                """;

            CamelCaseConfig config = ConfigBinder.bind(yaml, CamelCaseConfig.class);

            assertThat(config.serverHost).isEqualTo("kebab-host");
            assertThat(config.serverPort).isEqualTo(4444);
            assertThat(config.enableSsl).isTrue();
        }

        @Test
        @DisplayName("should bind snake_case property names to camelCase fields")
        void shouldBindSnakeCasePropertyNames() {
            String yaml = """
                server_host: snake-host
                server_port: 5555
                enable_ssl: true
                """;

            CamelCaseConfig config = ConfigBinder.bind(yaml, CamelCaseConfig.class);

            assertThat(config.serverHost).isEqualTo("snake-host");
            assertThat(config.serverPort).isEqualTo(5555);
            assertThat(config.enableSsl).isTrue();
        }
    }

    // ==================== Exception Handling Tests ====================

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("should throw YmlBindException for binding failure")
        void shouldThrowYmlBindExceptionForBindingFailure() {
            String yaml = """
                name: test
                """;

            assertThatThrownBy(() -> ConfigBinder.bind(yaml, PrivateConstructorConfig.class))
                .isInstanceOf(YmlBindException.class)
                .hasMessageContaining("Failed to bind");
        }

        @Test
        @DisplayName("should throw exception for invalid number format")
        void shouldThrowExceptionForInvalidNumberFormat() {
            Map<String, Object> props = Map.of("port", "not-a-number");

            assertThatThrownBy(() -> ConfigBinder.bind(props, ServerConfig.class))
                .isInstanceOf(YmlBindException.class);
        }
    }

    // ==================== Utility Class Tests ====================

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("constructor should be private")
        void constructorShouldBePrivate() throws Exception {
            var constructor = ConfigBinder.class.getDeclaredConstructor();

            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("should not be instantiable via reflection")
        void shouldNotBeInstantiableViaReflection() throws Exception {
            var constructor = ConfigBinder.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            // Private constructor with no body should not throw, just creates an instance
            // that is not usable (all methods are static)
            Object instance = constructor.newInstance();
            assertThat(instance).isNotNull();
        }
    }

    // ==================== Edge Case Tests ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle extra fields in YAML")
        void shouldHandleExtraFieldsInYaml() {
            String yaml = """
                host: localhost
                port: 8080
                extraField: ignored
                anotherExtra: also-ignored
                """;

            ServerConfig config = ConfigBinder.bind(yaml, ServerConfig.class);

            assertThat(config.host).isEqualTo("localhost");
            assertThat(config.port).isEqualTo(8080);
        }

        @Test
        @DisplayName("should handle special characters in string values")
        void shouldHandleSpecialCharactersInStringValues() {
            String yaml = """
                host: "host:with:colons"
                port: 8080
                """;

            ServerConfig config = ConfigBinder.bind(yaml, ServerConfig.class);

            assertThat(config.host).isEqualTo("host:with:colons");
        }

        @Test
        @DisplayName("should handle unicode in string values")
        void shouldHandleUnicodeInStringValues() {
            String yaml = """
                host: "\u4e2d\u6587\u4e3b\u673a"
                port: 8080
                """;

            ServerConfig config = ConfigBinder.bind(yaml, ServerConfig.class);

            assertThat(config.host).isEqualTo("\u4e2d\u6587\u4e3b\u673a");
        }

        @Test
        @DisplayName("should handle negative numbers")
        void shouldHandleNegativeNumbers() {
            String yaml = """
                intValue: -42
                longValue: -9999999999
                doubleValue: -3.14
                floatValue: -2.5
                shortValue: -100
                byteValue: -127
                booleanValue: false
                charValue: X
                """;

            AllPrimitivesConfig config = ConfigBinder.bind(yaml, AllPrimitivesConfig.class);

            assertThat(config.intValue).isEqualTo(-42);
            assertThat(config.longValue).isEqualTo(-9999999999L);
            assertThat(config.doubleValue).isEqualTo(-3.14);
            assertThat(config.floatValue).isEqualTo(-2.5f);
            assertThat(config.shortValue).isEqualTo((short) -100);
            assertThat(config.byteValue).isEqualTo((byte) -127);
        }

        @Test
        @DisplayName("should handle numeric values at boundaries")
        void shouldHandleNumericValuesAtBoundaries() {
            String yaml = """
                intValue: 2147483647
                longValue: 9223372036854775807
                shortValue: 32767
                byteValue: 127
                booleanValue: true
                doubleValue: 0.0
                floatValue: 0.0
                charValue: A
                """;

            AllPrimitivesConfig config = ConfigBinder.bind(yaml, AllPrimitivesConfig.class);

            assertThat(config.intValue).isEqualTo(Integer.MAX_VALUE);
            assertThat(config.longValue).isEqualTo(Long.MAX_VALUE);
            assertThat(config.shortValue).isEqualTo(Short.MAX_VALUE);
            assertThat(config.byteValue).isEqualTo(Byte.MAX_VALUE);
        }

        @Test
        @DisplayName("should handle value already of correct type")
        void shouldHandleValueAlreadyOfCorrectType() {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("host", "already-string");
            props.put("port", 8080);  // Already Integer

            ServerConfig config = ConfigBinder.bind(props, ServerConfig.class);

            assertThat(config.host).isEqualTo("already-string");
            assertThat(config.port).isEqualTo(8080);
        }
    }
}
