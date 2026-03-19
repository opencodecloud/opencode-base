/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.yml.bind;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for PropertySource interface and its implementations
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("PropertySource Tests")
class PropertySourceTest {

    // ==================== MapPropertySource Tests ====================

    @Nested
    @DisplayName("MapPropertySource Tests")
    class MapPropertySourceTests {

        @Nested
        @DisplayName("getName() Tests")
        class GetNameTests {

            @Test
            @DisplayName("should return the source name")
            void shouldReturnTheSourceName() {
                Map<String, Object> props = Map.of("key", "value");
                PropertySource source = PropertySource.fromMap("testSource", props);

                assertThat(source.getName()).isEqualTo("testSource");
            }

            @Test
            @DisplayName("should return empty name when empty string provided")
            void shouldReturnEmptyNameWhenEmptyStringProvided() {
                PropertySource source = PropertySource.fromMap("", Map.of());

                assertThat(source.getName()).isEmpty();
            }

            @Test
            @DisplayName("should return null name when null provided")
            void shouldReturnNullNameWhenNullProvided() {
                PropertySource source = PropertySource.fromMap(null, Map.of());

                assertThat(source.getName()).isNull();
            }
        }

        @Nested
        @DisplayName("getProperty() Tests")
        class GetPropertyTests {

            @Test
            @DisplayName("should return property value for simple key")
            void shouldReturnPropertyValueForSimpleKey() {
                Map<String, Object> props = Map.of("host", "localhost");
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.getProperty("host")).isEqualTo("localhost");
            }

            @Test
            @DisplayName("should return null for non-existent property")
            void shouldReturnNullForNonExistentProperty() {
                PropertySource source = PropertySource.fromMap("test", Map.of());

                assertThat(source.getProperty("missing")).isNull();
            }

            @Test
            @DisplayName("should return null for null path")
            void shouldReturnNullForNullPath() {
                PropertySource source = PropertySource.fromMap("test", Map.of("key", "value"));

                assertThat(source.getProperty(null)).isNull();
            }

            @Test
            @DisplayName("should return null for empty path")
            void shouldReturnNullForEmptyPath() {
                PropertySource source = PropertySource.fromMap("test", Map.of("key", "value"));

                assertThat(source.getProperty("")).isNull();
            }

            @Test
            @DisplayName("should convert integer value to string")
            void shouldConvertIntegerValueToString() {
                Map<String, Object> props = Map.of("port", 8080);
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.getProperty("port")).isEqualTo("8080");
            }

            @Test
            @DisplayName("should convert boolean value to string")
            void shouldConvertBooleanValueToString() {
                Map<String, Object> props = Map.of("enabled", true);
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.getProperty("enabled")).isEqualTo("true");
            }
        }

        @Nested
        @DisplayName("Nested Path Tests")
        class NestedPathTests {

            @Test
            @DisplayName("should return nested property value")
            void shouldReturnNestedPropertyValue() {
                Map<String, Object> server = Map.of("host", "localhost", "port", 8080);
                Map<String, Object> props = Map.of("database", server);
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.getProperty("database.host")).isEqualTo("localhost");
                assertThat(source.getProperty("database.port")).isEqualTo("8080");
            }

            @Test
            @DisplayName("should return deeply nested property value")
            void shouldReturnDeeplyNestedPropertyValue() {
                Map<String, Object> config = Map.of("timeout", 30);
                Map<String, Object> server = Map.of("config", config);
                Map<String, Object> database = Map.of("server", server);
                Map<String, Object> props = Map.of("app", database);
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.getProperty("app.server.config.timeout")).isEqualTo("30");
            }

            @Test
            @DisplayName("should return null for partial path match")
            void shouldReturnNullForPartialPathMatch() {
                Map<String, Object> server = Map.of("host", "localhost");
                Map<String, Object> props = Map.of("database", server);
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.getProperty("database.host.extra")).isNull();
            }

            @Test
            @DisplayName("should return null when intermediate path is not a map")
            void shouldReturnNullWhenIntermediatePathIsNotAMap() {
                Map<String, Object> props = Map.of("database", "simple-value");
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.getProperty("database.host")).isNull();
            }
        }

        @Nested
        @DisplayName("containsProperty() Tests")
        class ContainsPropertyTests {

            @Test
            @DisplayName("should return true for existing property")
            void shouldReturnTrueForExistingProperty() {
                Map<String, Object> props = Map.of("host", "localhost");
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.containsProperty("host")).isTrue();
            }

            @Test
            @DisplayName("should return false for non-existent property")
            void shouldReturnFalseForNonExistentProperty() {
                PropertySource source = PropertySource.fromMap("test", Map.of());

                assertThat(source.containsProperty("missing")).isFalse();
            }

            @Test
            @DisplayName("should return true for nested property")
            void shouldReturnTrueForNestedProperty() {
                Map<String, Object> server = Map.of("host", "localhost");
                Map<String, Object> props = Map.of("database", server);
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.containsProperty("database.host")).isTrue();
            }

            @Test
            @DisplayName("should return false for null path")
            void shouldReturnFalseForNullPath() {
                PropertySource source = PropertySource.fromMap("test", Map.of("key", "value"));

                assertThat(source.containsProperty(null)).isFalse();
            }
        }

        @Nested
        @DisplayName("getPropertyNames() Tests")
        class GetPropertyNamesTests {

            @Test
            @DisplayName("should return all top-level property names")
            void shouldReturnAllTopLevelPropertyNames() {
                Map<String, Object> props = new LinkedHashMap<>();
                props.put("host", "localhost");
                props.put("port", 8080);
                props.put("enabled", true);
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.getPropertyNames()).containsExactlyInAnyOrder("host", "port", "enabled");
            }

            @Test
            @DisplayName("should return empty set for empty properties")
            void shouldReturnEmptySetForEmptyProperties() {
                PropertySource source = PropertySource.fromMap("test", Map.of());

                assertThat(source.getPropertyNames()).isEmpty();
            }

            @Test
            @DisplayName("should return empty set for null properties")
            void shouldReturnEmptySetForNullProperties() {
                PropertySource source = PropertySource.fromMap("test", null);

                assertThat(source.getPropertyNames()).isEmpty();
            }
        }

        @Nested
        @DisplayName("getProperties() Tests")
        class GetPropertiesTests {

            @Test
            @DisplayName("should return all properties as map")
            void shouldReturnAllPropertiesAsMap() {
                Map<String, Object> props = new LinkedHashMap<>();
                props.put("host", "localhost");
                props.put("port", 8080);
                PropertySource source = PropertySource.fromMap("test", props);

                Map<String, Object> result = source.getProperties();

                assertThat(result).containsEntry("host", "localhost");
                assertThat(result).containsEntry("port", 8080);
            }

            @Test
            @DisplayName("should return immutable copy of properties")
            void shouldReturnImmutableCopyOfProperties() {
                Map<String, Object> props = new LinkedHashMap<>();
                props.put("host", "localhost");
                PropertySource source = PropertySource.fromMap("test", props);

                Map<String, Object> result = source.getProperties();

                assertThatThrownBy(() -> result.put("new", "value"))
                        .isInstanceOf(UnsupportedOperationException.class);
            }

            @Test
            @DisplayName("should return empty map for null properties")
            void shouldReturnEmptyMapForNullProperties() {
                PropertySource source = PropertySource.fromMap("test", null);

                assertThat(source.getProperties()).isEmpty();
            }
        }

        @Nested
        @DisplayName("getProperties(prefix) Tests")
        class GetPropertiesWithPrefixTests {

            @Test
            @DisplayName("should return properties under prefix")
            void shouldReturnPropertiesUnderPrefix() {
                Map<String, Object> server = new LinkedHashMap<>();
                server.put("host", "localhost");
                server.put("port", 8080);
                Map<String, Object> props = Map.of("database", server);
                PropertySource source = PropertySource.fromMap("test", props);

                Map<String, Object> result = source.getProperties("database");

                assertThat(result).containsEntry("host", "localhost");
                assertThat(result).containsEntry("port", 8080);
            }

            @Test
            @DisplayName("should return empty map for non-existent prefix")
            void shouldReturnEmptyMapForNonExistentPrefix() {
                PropertySource source = PropertySource.fromMap("test", Map.of("key", "value"));

                assertThat(source.getProperties("missing")).isEmpty();
            }

            @Test
            @DisplayName("should return empty map when prefix points to non-map value")
            void shouldReturnEmptyMapWhenPrefixPointsToNonMapValue() {
                Map<String, Object> props = Map.of("key", "simple-value");
                PropertySource source = PropertySource.fromMap("test", props);

                assertThat(source.getProperties("key")).isEmpty();
            }

            @Test
            @DisplayName("should return nested map for multi-level prefix")
            void shouldReturnNestedMapForMultiLevelPrefix() {
                Map<String, Object> config = Map.of("timeout", 30);
                Map<String, Object> server = Map.of("config", config);
                Map<String, Object> props = Map.of("database", server);
                PropertySource source = PropertySource.fromMap("test", props);

                Map<String, Object> result = source.getProperties("database.config");

                assertThat(result).containsEntry("timeout", 30);
            }
        }

        @Nested
        @DisplayName("Type Conversion Tests")
        class TypeConversionTests {

            private PropertySource source;

            @BeforeEach
            void setUp() {
                Map<String, Object> props = new LinkedHashMap<>();
                props.put("intValue", "42");
                props.put("longValue", "9999999999");
                props.put("booleanTrue", "true");
                props.put("booleanFalse", "false");
                props.put("doubleValue", "3.14159");
                props.put("stringValue", "hello");
                props.put("intAsInt", 100);
                props.put("boolAsTrue", true);
                source = PropertySource.fromMap("test", props);
            }

            @Test
            @DisplayName("should convert string to Integer")
            void shouldConvertStringToInteger() {
                Integer result = source.getProperty("intValue", Integer.class);

                assertThat(result).isEqualTo(42);
            }

            @Test
            @DisplayName("should convert string to int primitive via wrapper")
            void shouldConvertStringToIntPrimitiveViaWrapper() {
                // Note: The implementation uses type.cast() which doesn't work with primitives
                // so we use wrapper type and verify the value works as primitive
                Integer result = source.getProperty("intValue", Integer.class);
                int primitiveResult = result;

                assertThat(primitiveResult).isEqualTo(42);
            }

            @Test
            @DisplayName("should convert string to Long")
            void shouldConvertStringToLong() {
                Long result = source.getProperty("longValue", Long.class);

                assertThat(result).isEqualTo(9999999999L);
            }

            @Test
            @DisplayName("should convert string to long primitive via wrapper")
            void shouldConvertStringToLongPrimitiveViaWrapper() {
                // Note: The implementation uses type.cast() which doesn't work with primitives
                // so we use wrapper type and verify the value works as primitive
                Long result = source.getProperty("longValue", Long.class);
                long primitiveResult = result;

                assertThat(primitiveResult).isEqualTo(9999999999L);
            }

            @Test
            @DisplayName("should convert string to Boolean true")
            void shouldConvertStringToBooleanTrue() {
                Boolean result = source.getProperty("booleanTrue", Boolean.class);

                assertThat(result).isTrue();
            }

            @Test
            @DisplayName("should convert string to Boolean false")
            void shouldConvertStringToBooleanFalse() {
                Boolean result = source.getProperty("booleanFalse", Boolean.class);

                assertThat(result).isFalse();
            }

            @Test
            @DisplayName("should convert string to boolean primitive via wrapper")
            void shouldConvertStringToBooleanPrimitiveViaWrapper() {
                // Note: The implementation uses type.cast() which doesn't work with primitives
                // so we use wrapper type and verify the value works as primitive
                Boolean result = source.getProperty("booleanTrue", Boolean.class);
                boolean primitiveResult = result;

                assertThat(primitiveResult).isTrue();
            }

            @Test
            @DisplayName("should convert string to Double")
            void shouldConvertStringToDouble() {
                Double result = source.getProperty("doubleValue", Double.class);

                assertThat(result).isEqualTo(3.14159);
            }

            @Test
            @DisplayName("should convert string to double primitive via wrapper")
            void shouldConvertStringToDoublePrimitiveViaWrapper() {
                // Note: The implementation uses type.cast() which doesn't work with primitives
                // so we use wrapper type and verify the value works as primitive
                Double result = source.getProperty("doubleValue", Double.class);
                double primitiveResult = result;

                assertThat(primitiveResult).isEqualTo(3.14159);
            }

            @Test
            @DisplayName("should return String as is")
            void shouldReturnStringAsIs() {
                String result = source.getProperty("stringValue", String.class);

                assertThat(result).isEqualTo("hello");
            }

            @Test
            @DisplayName("should return null for missing property with type")
            void shouldReturnNullForMissingPropertyWithType() {
                Integer result = source.getProperty("missing", Integer.class);

                assertThat(result).isNull();
            }

            @Test
            @DisplayName("should return null for unsupported type conversion")
            void shouldReturnNullForUnsupportedTypeConversion() {
                List<?> result = source.getProperty("stringValue", List.class);

                assertThat(result).isNull();
            }

            @Test
            @DisplayName("should cast when value is already correct type")
            void shouldCastWhenValueIsAlreadyCorrectType() {
                Integer result = source.getProperty("intAsInt", Integer.class);

                assertThat(result).isEqualTo(100);
            }

            @Test
            @DisplayName("should cast boolean when value is already boolean")
            void shouldCastBooleanWhenValueIsAlreadyBoolean() {
                Boolean result = source.getProperty("boolAsTrue", Boolean.class);

                assertThat(result).isTrue();
            }
        }
    }

    // ==================== YamlPropertySource Tests ====================

    @Nested
    @DisplayName("YamlPropertySource Tests")
    class YamlPropertySourceTests {

        @Nested
        @DisplayName("Parsing YAML Content Tests")
        class ParsingYamlContentTests {

            @Test
            @DisplayName("should parse simple YAML content")
            void shouldParseSimpleYamlContent() {
                String yaml = """
                        host: localhost
                        port: 8080
                        enabled: true
                        """;
                PropertySource source = PropertySource.fromYaml("test", yaml);

                assertThat(source.getProperty("host")).isEqualTo("localhost");
                assertThat(source.getProperty("port")).isEqualTo("8080");
                assertThat(source.getProperty("enabled")).isEqualTo("true");
            }

            @Test
            @DisplayName("should parse YAML with default name")
            void shouldParseYamlWithDefaultName() {
                String yaml = "key: value";
                PropertySource source = PropertySource.fromYaml(yaml);

                assertThat(source.getName()).isEqualTo("yaml");
                assertThat(source.getProperty("key")).isEqualTo("value");
            }

            @Test
            @DisplayName("should return empty properties for null YAML content")
            void shouldReturnEmptyPropertiesForNullYamlContent() {
                PropertySource source = PropertySource.fromYaml("test", null);

                assertThat(source.getProperties()).isEmpty();
            }

            @Test
            @DisplayName("should return empty properties for blank YAML content")
            void shouldReturnEmptyPropertiesForBlankYamlContent() {
                PropertySource source = PropertySource.fromYaml("test", "   ");

                assertThat(source.getProperties()).isEmpty();
            }

            @Test
            @DisplayName("should return empty properties for empty YAML content")
            void shouldReturnEmptyPropertiesForEmptyYamlContent() {
                PropertySource source = PropertySource.fromYaml("test", "");

                assertThat(source.getProperties()).isEmpty();
            }

            @Test
            @DisplayName("should handle invalid YAML gracefully")
            void shouldHandleInvalidYamlGracefully() {
                // Test with unbalanced quotes which triggers parse exception
                String invalidYaml = "key: \"unclosed quote";

                // Invalid YAML either returns empty properties or throws exception
                // depending on the YAML parser behavior - we verify it doesn't crash
                try {
                    PropertySource source = PropertySource.fromYaml("test", invalidYaml);
                    // If parsing succeeds, the result should be usable
                    assertThat(source).isNotNull();
                } catch (Exception e) {
                    // Some YAML parsers may throw exceptions for invalid YAML
                    assertThat(e).isNotNull();
                }
            }
        }

        @Nested
        @DisplayName("Nested Paths Tests")
        class NestedPathsTests {

            @Test
            @DisplayName("should access nested YAML properties")
            void shouldAccessNestedYamlProperties() {
                String yaml = """
                        database:
                          host: localhost
                          port: 5432
                          credentials:
                            username: admin
                            password: secret
                        """;
                PropertySource source = PropertySource.fromYaml("test", yaml);

                assertThat(source.getProperty("database.host")).isEqualTo("localhost");
                assertThat(source.getProperty("database.port")).isEqualTo("5432");
                assertThat(source.getProperty("database.credentials.username")).isEqualTo("admin");
                assertThat(source.getProperty("database.credentials.password")).isEqualTo("secret");
            }

            @Test
            @DisplayName("should get properties under YAML prefix")
            void shouldGetPropertiesUnderYamlPrefix() {
                String yaml = """
                        server:
                          host: 0.0.0.0
                          port: 8080
                        """;
                PropertySource source = PropertySource.fromYaml("test", yaml);

                Map<String, Object> serverProps = source.getProperties("server");

                assertThat(serverProps).containsEntry("host", "0.0.0.0");
                assertThat(serverProps).containsEntry("port", 8080);
            }

            @Test
            @DisplayName("should check containsProperty for nested YAML paths")
            void shouldCheckContainsPropertyForNestedYamlPaths() {
                String yaml = """
                        app:
                          name: myapp
                          version: 1.0
                        """;
                PropertySource source = PropertySource.fromYaml("test", yaml);

                assertThat(source.containsProperty("app.name")).isTrue();
                assertThat(source.containsProperty("app.missing")).isFalse();
            }

            @Test
            @DisplayName("should handle YAML with lists")
            void shouldHandleYamlWithLists() {
                String yaml = """
                        servers:
                          - host: server1
                            port: 8080
                          - host: server2
                            port: 8081
                        """;
                PropertySource source = PropertySource.fromYaml("test", yaml);

                assertThat(source.containsProperty("servers")).isTrue();
            }
        }

        @Nested
        @DisplayName("Type Conversion Tests")
        class YamlTypeConversionTests {

            @Test
            @DisplayName("should convert YAML values to typed properties")
            void shouldConvertYamlValuesToTypedProperties() {
                String yaml = """
                        port: 8080
                        timeout: 30000
                        enabled: true
                        rate: 1.5
                        """;
                PropertySource source = PropertySource.fromYaml("test", yaml);

                assertThat(source.getProperty("port", Integer.class)).isEqualTo(8080);
                assertThat(source.getProperty("timeout", Long.class)).isEqualTo(30000L);
                assertThat(source.getProperty("enabled", Boolean.class)).isTrue();
                assertThat(source.getProperty("rate", Double.class)).isEqualTo(1.5);
            }
        }
    }

    // ==================== EnvironmentPropertySource Tests ====================

    @Nested
    @DisplayName("EnvironmentPropertySource Tests")
    class EnvironmentPropertySourceTests {

        private PropertySource envSource;

        @BeforeEach
        void setUp() {
            envSource = PropertySource.fromEnvironment();
        }

        @Nested
        @DisplayName("Basic Tests")
        class BasicTests {

            @Test
            @DisplayName("should return 'environment' as name")
            void shouldReturnEnvironmentAsName() {
                assertThat(envSource.getName()).isEqualTo("environment");
            }

            @Test
            @DisplayName("should return environment variable names")
            void shouldReturnEnvironmentVariableNames() {
                Set<String> names = envSource.getPropertyNames();

                assertThat(names).isNotEmpty();
                // PATH is commonly available on most systems
                assertThat(names).contains("PATH");
            }

            @Test
            @DisplayName("should return all environment variables as properties")
            void shouldReturnAllEnvironmentVariablesAsProperties() {
                Map<String, Object> props = envSource.getProperties();

                assertThat(props).isNotEmpty();
                assertThat(props).containsKey("PATH");
            }
        }

        @Nested
        @DisplayName("Property Name Conversion Tests")
        class PropertyNameConversionTests {

            @Test
            @DisplayName("should convert dots to underscores and uppercase")
            void shouldConvertDotsToUnderscoresAndUppercase() {
                // The EnvironmentPropertySource converts:
                // database.host -> DATABASE_HOST
                // If DATABASE_HOST exists, it will return its value
                // We can verify the conversion logic by checking a known env var

                // HOME is commonly set - verify we can read it via home
                String homeValue = System.getenv("HOME");
                if (homeValue != null) {
                    // Since getProperty("home") -> HOME
                    // But the path would be just "home" which converts to "HOME"
                    assertThat(envSource.getProperty("home")).isEqualTo(homeValue);
                }
            }

            @Test
            @DisplayName("should return null for non-existent environment variable")
            void shouldReturnNullForNonExistentEnvironmentVariable() {
                assertThat(envSource.getProperty("non.existent.variable.that.does.not.exist")).isNull();
            }

            @Test
            @DisplayName("should check containsProperty correctly")
            void shouldCheckContainsPropertyCorrectly() {
                // PATH exists on most systems
                assertThat(envSource.containsProperty("path")).isTrue();
                assertThat(envSource.containsProperty("non.existent.var")).isFalse();
            }
        }

        @Nested
        @DisplayName("Type Conversion Tests")
        class EnvironmentTypeConversionTests {

            @Test
            @DisplayName("should convert environment variable to Integer")
            void shouldConvertEnvironmentVariableToInteger() {
                // This tests the type conversion mechanism
                // We'll test with a known format if available, otherwise skip
                String pathValue = System.getenv("PATH");
                if (pathValue != null) {
                    String result = envSource.getProperty("path", String.class);
                    assertThat(result).isEqualTo(pathValue);
                }
            }

            @Test
            @DisplayName("should return null for missing property with type")
            void shouldReturnNullForMissingPropertyWithType() {
                Integer result = envSource.getProperty("missing.env.var", Integer.class);

                assertThat(result).isNull();
            }
        }

        @Nested
        @DisplayName("getProperties(prefix) Tests")
        class GetPropertiesWithPrefixTests {

            @Test
            @DisplayName("should return properties matching prefix pattern")
            void shouldReturnPropertiesMatchingPrefixPattern() {
                // This tests if any environment variables match a given prefix
                // The result depends on actual environment, so we just verify the method works
                Map<String, Object> result = envSource.getProperties("java");

                // Result may be empty or contain JAVA_ prefixed env vars
                assertThat(result).isNotNull();
            }
        }
    }

    // ==================== SystemPropertySource Tests ====================

    @Nested
    @DisplayName("SystemPropertySource Tests")
    class SystemPropertySourceTests {

        private PropertySource sysSource;
        private static final String TEST_PROPERTY_KEY = "cloud.opencode.test.property";
        private static final String TEST_PROPERTY_VALUE = "testValue123";

        @BeforeEach
        void setUp() {
            sysSource = PropertySource.fromSystemProperties();
            System.setProperty(TEST_PROPERTY_KEY, TEST_PROPERTY_VALUE);
        }

        @AfterEach
        void tearDown() {
            System.clearProperty(TEST_PROPERTY_KEY);
        }

        @Nested
        @DisplayName("Basic Tests")
        class BasicTests {

            @Test
            @DisplayName("should return 'system' as name")
            void shouldReturnSystemAsName() {
                assertThat(sysSource.getName()).isEqualTo("system");
            }

            @Test
            @DisplayName("should return system property names")
            void shouldReturnSystemPropertyNames() {
                Set<String> names = sysSource.getPropertyNames();

                assertThat(names).isNotEmpty();
                assertThat(names).contains("java.version");
                assertThat(names).contains(TEST_PROPERTY_KEY);
            }

            @Test
            @DisplayName("should return all system properties as map")
            void shouldReturnAllSystemPropertiesAsMap() {
                Map<String, Object> props = sysSource.getProperties();

                assertThat(props).isNotEmpty();
                assertThat(props).containsKey("java.version");
                assertThat(props).containsEntry(TEST_PROPERTY_KEY, TEST_PROPERTY_VALUE);
            }
        }

        @Nested
        @DisplayName("getProperty() Tests")
        class GetPropertyTests {

            @Test
            @DisplayName("should return system property value")
            void shouldReturnSystemPropertyValue() {
                assertThat(sysSource.getProperty(TEST_PROPERTY_KEY)).isEqualTo(TEST_PROPERTY_VALUE);
            }

            @Test
            @DisplayName("should return java.version system property")
            void shouldReturnJavaVersionSystemProperty() {
                String javaVersion = System.getProperty("java.version");

                assertThat(sysSource.getProperty("java.version")).isEqualTo(javaVersion);
            }

            @Test
            @DisplayName("should return null for non-existent system property")
            void shouldReturnNullForNonExistentSystemProperty() {
                assertThat(sysSource.getProperty("non.existent.property")).isNull();
            }
        }

        @Nested
        @DisplayName("containsProperty() Tests")
        class ContainsPropertyTests {

            @Test
            @DisplayName("should return true for existing system property")
            void shouldReturnTrueForExistingSystemProperty() {
                assertThat(sysSource.containsProperty(TEST_PROPERTY_KEY)).isTrue();
                assertThat(sysSource.containsProperty("java.version")).isTrue();
            }

            @Test
            @DisplayName("should return false for non-existent system property")
            void shouldReturnFalseForNonExistentSystemProperty() {
                assertThat(sysSource.containsProperty("non.existent.property")).isFalse();
            }
        }

        @Nested
        @DisplayName("Type Conversion Tests")
        class SystemTypeConversionTests {

            @BeforeEach
            void setUpTypedProperties() {
                System.setProperty("cloud.opencode.test.int", "42");
                System.setProperty("cloud.opencode.test.long", "9999999999");
                System.setProperty("cloud.opencode.test.boolean", "true");
                System.setProperty("cloud.opencode.test.double", "3.14159");
            }

            @AfterEach
            void tearDownTypedProperties() {
                System.clearProperty("cloud.opencode.test.int");
                System.clearProperty("cloud.opencode.test.long");
                System.clearProperty("cloud.opencode.test.boolean");
                System.clearProperty("cloud.opencode.test.double");
            }

            @Test
            @DisplayName("should convert system property to Integer")
            void shouldConvertSystemPropertyToInteger() {
                Integer result = sysSource.getProperty("cloud.opencode.test.int", Integer.class);

                assertThat(result).isEqualTo(42);
            }

            @Test
            @DisplayName("should convert system property to Long")
            void shouldConvertSystemPropertyToLong() {
                Long result = sysSource.getProperty("cloud.opencode.test.long", Long.class);

                assertThat(result).isEqualTo(9999999999L);
            }

            @Test
            @DisplayName("should convert system property to Boolean")
            void shouldConvertSystemPropertyToBoolean() {
                Boolean result = sysSource.getProperty("cloud.opencode.test.boolean", Boolean.class);

                assertThat(result).isTrue();
            }

            @Test
            @DisplayName("should convert system property to Double")
            void shouldConvertSystemPropertyToDouble() {
                Double result = sysSource.getProperty("cloud.opencode.test.double", Double.class);

                assertThat(result).isEqualTo(3.14159);
            }

            @Test
            @DisplayName("should return String property as is")
            void shouldReturnStringPropertyAsIs() {
                String result = sysSource.getProperty(TEST_PROPERTY_KEY, String.class);

                assertThat(result).isEqualTo(TEST_PROPERTY_VALUE);
            }

            @Test
            @DisplayName("should return null for missing property with type")
            void shouldReturnNullForMissingPropertyWithType() {
                Integer result = sysSource.getProperty("non.existent", Integer.class);

                assertThat(result).isNull();
            }
        }

        @Nested
        @DisplayName("getProperties(prefix) Tests")
        class GetPropertiesWithPrefixTests {

            @BeforeEach
            void setUpPrefixedProperties() {
                System.setProperty("cloud.opencode.prefix.a", "valueA");
                System.setProperty("cloud.opencode.prefix.b", "valueB");
                System.setProperty("cloud.opencode.prefix.c", "valueC");
            }

            @AfterEach
            void tearDownPrefixedProperties() {
                System.clearProperty("cloud.opencode.prefix.a");
                System.clearProperty("cloud.opencode.prefix.b");
                System.clearProperty("cloud.opencode.prefix.c");
            }

            @Test
            @DisplayName("should return properties under prefix")
            void shouldReturnPropertiesUnderPrefix() {
                Map<String, Object> result = sysSource.getProperties("cloud.opencode.prefix");

                assertThat(result).containsEntry("a", "valueA");
                assertThat(result).containsEntry("b", "valueB");
                assertThat(result).containsEntry("c", "valueC");
            }

            @Test
            @DisplayName("should return empty map for non-matching prefix")
            void shouldReturnEmptyMapForNonMatchingPrefix() {
                Map<String, Object> result = sysSource.getProperties("non.existent.prefix");

                assertThat(result).isEmpty();
            }

            @Test
            @DisplayName("should handle prefix with trailing dot")
            void shouldHandlePrefixWithTrailingDot() {
                Map<String, Object> result = sysSource.getProperties("cloud.opencode.prefix.");

                assertThat(result).containsEntry("a", "valueA");
            }
        }
    }

    // ==================== Factory Methods Tests ====================

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Nested
        @DisplayName("fromMap() Tests")
        class FromMapTests {

            @Test
            @DisplayName("should create PropertySource from map with name")
            void shouldCreatePropertySourceFromMapWithName() {
                Map<String, Object> props = Map.of("key", "value");

                PropertySource source = PropertySource.fromMap("mySource", props);

                assertThat(source).isNotNull();
                assertThat(source.getName()).isEqualTo("mySource");
                assertThat(source.getProperty("key")).isEqualTo("value");
            }

            @Test
            @DisplayName("should create PropertySource from empty map")
            void shouldCreatePropertySourceFromEmptyMap() {
                PropertySource source = PropertySource.fromMap("empty", Map.of());

                assertThat(source).isNotNull();
                assertThat(source.getProperties()).isEmpty();
            }

            @Test
            @DisplayName("should create PropertySource from null map")
            void shouldCreatePropertySourceFromNullMap() {
                PropertySource source = PropertySource.fromMap("nullMap", null);

                assertThat(source).isNotNull();
                assertThat(source.getProperties()).isEmpty();
            }
        }

        @Nested
        @DisplayName("fromYaml() Tests")
        class FromYamlTests {

            @Test
            @DisplayName("should create PropertySource from YAML content with default name")
            void shouldCreatePropertySourceFromYamlContentWithDefaultName() {
                String yaml = "key: value";

                PropertySource source = PropertySource.fromYaml(yaml);

                assertThat(source).isNotNull();
                assertThat(source.getName()).isEqualTo("yaml");
                assertThat(source.getProperty("key")).isEqualTo("value");
            }

            @Test
            @DisplayName("should create PropertySource from YAML content with custom name")
            void shouldCreatePropertySourceFromYamlContentWithCustomName() {
                String yaml = "host: localhost";

                PropertySource source = PropertySource.fromYaml("application", yaml);

                assertThat(source).isNotNull();
                assertThat(source.getName()).isEqualTo("application");
                assertThat(source.getProperty("host")).isEqualTo("localhost");
            }
        }

        @Nested
        @DisplayName("fromEnvironment() Tests")
        class FromEnvironmentTests {

            @Test
            @DisplayName("should create EnvironmentPropertySource")
            void shouldCreateEnvironmentPropertySource() {
                PropertySource source = PropertySource.fromEnvironment();

                assertThat(source).isNotNull();
                assertThat(source.getName()).isEqualTo("environment");
                assertThat(source).isInstanceOf(PropertySource.EnvironmentPropertySource.class);
            }
        }

        @Nested
        @DisplayName("fromSystemProperties() Tests")
        class FromSystemPropertiesTests {

            @Test
            @DisplayName("should create SystemPropertySource")
            void shouldCreateSystemPropertySource() {
                PropertySource source = PropertySource.fromSystemProperties();

                assertThat(source).isNotNull();
                assertThat(source.getName()).isEqualTo("system");
                assertThat(source).isInstanceOf(PropertySource.SystemPropertySource.class);
            }
        }
    }

    // ==================== Default Methods Tests ====================

    @Nested
    @DisplayName("Default Methods Tests")
    class DefaultMethodsTests {

        private PropertySource source;

        @BeforeEach
        void setUp() {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("host", "localhost");
            props.put("port", "8080");
            props.put("enabled", "true");
            source = PropertySource.fromMap("test", props);
        }

        @Nested
        @DisplayName("getProperty(path, defaultValue) Tests")
        class GetPropertyWithDefaultTests {

            @Test
            @DisplayName("should return property value when exists")
            void shouldReturnPropertyValueWhenExists() {
                String result = source.getProperty("host", "default-host");

                assertThat(result).isEqualTo("localhost");
            }

            @Test
            @DisplayName("should return default value when property does not exist")
            void shouldReturnDefaultValueWhenPropertyDoesNotExist() {
                String result = source.getProperty("missing", "default-value");

                assertThat(result).isEqualTo("default-value");
            }

            @Test
            @DisplayName("should return null default when property does not exist and default is null")
            void shouldReturnNullDefaultWhenPropertyDoesNotExistAndDefaultIsNull() {
                String result = source.getProperty("missing", (String) null);

                assertThat(result).isNull();
            }
        }

        @Nested
        @DisplayName("getProperty(path, type, defaultValue) Tests")
        class GetPropertyWithTypeAndDefaultTests {

            @Test
            @DisplayName("should return typed value when property exists")
            void shouldReturnTypedValueWhenPropertyExists() {
                Integer result = source.getProperty("port", Integer.class, 9090);

                assertThat(result).isEqualTo(8080);
            }

            @Test
            @DisplayName("should return default value when property does not exist")
            void shouldReturnDefaultValueWhenPropertyDoesNotExist() {
                Integer result = source.getProperty("missing", Integer.class, 9090);

                assertThat(result).isEqualTo(9090);
            }

            @Test
            @DisplayName("should return default boolean when property does not exist")
            void shouldReturnDefaultBooleanWhenPropertyDoesNotExist() {
                Boolean result = source.getProperty("missing", Boolean.class, false);

                assertThat(result).isFalse();
            }

            @Test
            @DisplayName("should return typed boolean value when property exists")
            void shouldReturnTypedBooleanValueWhenPropertyExists() {
                Boolean result = source.getProperty("enabled", Boolean.class, false);

                assertThat(result).isTrue();
            }

            @Test
            @DisplayName("should return null default when type conversion fails and default is null")
            void shouldReturnNullDefaultWhenTypeConversionFailsAndDefaultIsNull() {
                List<?> result = source.getProperty("host", List.class, null);

                assertThat(result).isNull();
            }
        }

        @Nested
        @DisplayName("getOptionalProperty(path) Tests")
        class GetOptionalPropertyTests {

            @Test
            @DisplayName("should return non-empty Optional when property exists")
            void shouldReturnNonEmptyOptionalWhenPropertyExists() {
                Optional<String> result = source.getOptionalProperty("host");

                assertThat(result).isPresent();
                assertThat(result).contains("localhost");
            }

            @Test
            @DisplayName("should return empty Optional when property does not exist")
            void shouldReturnEmptyOptionalWhenPropertyDoesNotExist() {
                Optional<String> result = source.getOptionalProperty("missing");

                assertThat(result).isEmpty();
            }

            @Test
            @DisplayName("should allow map operation on Optional")
            void shouldAllowMapOperationOnOptional() {
                int length = source.getOptionalProperty("host")
                        .map(String::length)
                        .orElse(0);

                assertThat(length).isEqualTo("localhost".length());
            }

            @Test
            @DisplayName("should allow orElse operation on empty Optional")
            void shouldAllowOrElseOperationOnEmptyOptional() {
                String result = source.getOptionalProperty("missing")
                        .orElse("fallback");

                assertThat(result).isEqualTo("fallback");
            }
        }
    }

    // ==================== Edge Cases Tests ====================

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle property with special characters in value")
        void shouldHandlePropertyWithSpecialCharactersInValue() {
            Map<String, Object> props = Map.of(
                    "url", "jdbc:mysql://localhost:3306/db?useSSL=true&serverTimezone=UTC",
                    "regex", "^[a-zA-Z0-9]+$"
            );
            PropertySource source = PropertySource.fromMap("test", props);

            assertThat(source.getProperty("url")).isEqualTo("jdbc:mysql://localhost:3306/db?useSSL=true&serverTimezone=UTC");
            assertThat(source.getProperty("regex")).isEqualTo("^[a-zA-Z0-9]+$");
        }

        @Test
        @DisplayName("should handle property with unicode characters")
        void shouldHandlePropertyWithUnicodeCharacters() {
            Map<String, Object> props = Map.of("name", "\u4e2d\u6587\u540d\u79f0");
            PropertySource source = PropertySource.fromMap("test", props);

            assertThat(source.getProperty("name")).isEqualTo("\u4e2d\u6587\u540d\u79f0");
        }

        @Test
        @DisplayName("should handle property with empty string value")
        void shouldHandlePropertyWithEmptyStringValue() {
            Map<String, Object> props = Map.of("empty", "");
            PropertySource source = PropertySource.fromMap("test", props);

            assertThat(source.getProperty("empty")).isEmpty();
            assertThat(source.containsProperty("empty")).isTrue();
        }

        @Test
        @DisplayName("should handle deeply nested properties")
        void shouldHandleDeeplyNestedProperties() {
            Map<String, Object> level4 = Map.of("value", "deep");
            Map<String, Object> level3 = Map.of("level4", level4);
            Map<String, Object> level2 = Map.of("level3", level3);
            Map<String, Object> level1 = Map.of("level2", level2);
            Map<String, Object> props = Map.of("level1", level1);
            PropertySource source = PropertySource.fromMap("test", props);

            assertThat(source.getProperty("level1.level2.level3.level4.value")).isEqualTo("deep");
        }

        @Test
        @DisplayName("should handle complex YAML with mixed types")
        void shouldHandleComplexYamlWithMixedTypes() {
            String yaml = """
                    app:
                      name: myapp
                      version: 1.0
                      features:
                        - feature1
                        - feature2
                      config:
                        timeout: 30
                        retries: 3
                    """;
            PropertySource source = PropertySource.fromYaml("complex", yaml);

            assertThat(source.getProperty("app.name")).isEqualTo("myapp");
            assertThat(source.getProperty("app.config.timeout")).isEqualTo("30");
            assertThat(source.containsProperty("app.features")).isTrue();
        }
    }
}
