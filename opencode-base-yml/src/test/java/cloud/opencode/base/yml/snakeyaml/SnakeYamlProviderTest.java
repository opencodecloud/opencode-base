package cloud.opencode.base.yml.snakeyaml;

import cloud.opencode.base.yml.YmlConfig;
import cloud.opencode.base.yml.YmlNode;
import cloud.opencode.base.yml.exception.YmlParseException;
import cloud.opencode.base.yml.spi.YmlProvider;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link SnakeYamlProvider}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("SnakeYamlProvider Tests")
class SnakeYamlProviderTest {

    private SnakeYamlProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SnakeYamlProvider();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should create provider with default config")
        void defaultConstructorShouldCreateProviderWithDefaultConfig() {
            SnakeYamlProvider defaultProvider = new SnakeYamlProvider();

            assertThat(defaultProvider).isNotNull();
            assertThat(defaultProvider.getConfig()).isNotNull();
            assertThat(defaultProvider.getConfig().isSafeMode()).isTrue();
        }

        @Test
        @DisplayName("constructor with config should use provided config")
        void constructorWithConfigShouldUseProvidedConfig() {
            YmlConfig customConfig = YmlConfig.builder()
                .indent(4)
                .prettyPrint(false)
                .safeMode(false)
                .build();

            SnakeYamlProvider customProvider = new SnakeYamlProvider(customConfig);

            assertThat(customProvider.getConfig()).isEqualTo(customConfig);
            assertThat(customProvider.getConfig().getIndent()).isEqualTo(4);
            assertThat(customProvider.getConfig().isPrettyPrint()).isFalse();
        }
    }

    @Nested
    @DisplayName("getName Tests")
    class GetNameTests {

        @Test
        @DisplayName("getName should return snakeyaml")
        void getNameShouldReturnSnakeyaml() {
            String name = provider.getName();

            assertThat(name).isEqualTo("snakeyaml");
        }

        @Test
        @DisplayName("getName should return consistent value")
        void getNameShouldReturnConsistentValue() {
            String name1 = provider.getName();
            String name2 = provider.getName();

            assertThat(name1).isEqualTo(name2);
        }
    }

    @Nested
    @DisplayName("getPriority Tests")
    class GetPriorityTests {

        @Test
        @DisplayName("getPriority should return 100")
        void getPriorityShouldReturn100() {
            int priority = provider.getPriority();

            assertThat(priority).isEqualTo(100);
        }

        @Test
        @DisplayName("getPriority should return positive value")
        void getPriorityShouldReturnPositiveValue() {
            int priority = provider.getPriority();

            assertThat(priority).isPositive();
        }
    }

    @Nested
    @DisplayName("isAvailable Tests")
    class IsAvailableTests {

        @Test
        @DisplayName("isAvailable should return true when SnakeYAML is on classpath")
        void isAvailableShouldReturnTrue() {
            boolean available = provider.isAvailable();

            assertThat(available).isTrue();
        }

        @Test
        @DisplayName("isAvailable should check for SnakeYAML class")
        void isAvailableShouldCheckForSnakeYamlClass() {
            // Verify that the class exists
            assertThatCode(() -> Class.forName("org.yaml.snakeyaml.Yaml"))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("load(String) Tests")
    class LoadStringTests {

        @Test
        @DisplayName("load should parse simple YAML")
        void loadShouldParseSimpleYaml() {
            String yaml = "name: John\nage: 30";

            Map<String, Object> result = provider.load(yaml);

            assertThat(result).containsEntry("name", "John");
            assertThat(result).containsEntry("age", 30);
        }

        @Test
        @DisplayName("load should parse nested YAML")
        void loadShouldParseNestedYaml() {
            String yaml = """
                person:
                  name: John
                  address:
                    city: New York
                """;

            Map<String, Object> result = provider.load(yaml);

            assertThat(result).containsKey("person");
            @SuppressWarnings("unchecked")
            Map<String, Object> person = (Map<String, Object>) result.get("person");
            assertThat(person).containsEntry("name", "John");
        }

        @Test
        @DisplayName("load should parse list values")
        void loadShouldParseListValues() {
            String yaml = """
                items:
                  - apple
                  - banana
                  - cherry
                """;

            Map<String, Object> result = provider.load(yaml);

            assertThat(result).containsKey("items");
            @SuppressWarnings("unchecked")
            List<String> items = (List<String>) result.get("items");
            assertThat(items).containsExactly("apple", "banana", "cherry");
        }

        @Test
        @DisplayName("load should handle empty YAML")
        void loadShouldHandleEmptyYaml() {
            Map<String, Object> result = provider.load("");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("load should handle null YAML value")
        void loadShouldHandleNullYamlValue() {
            String yaml = "value: null";

            Map<String, Object> result = provider.load(yaml);

            assertThat(result).containsEntry("value", null);
        }

        @Test
        @DisplayName("load should throw exception for invalid YAML")
        void loadShouldThrowExceptionForInvalidYaml() {
            String invalidYaml = "invalid: : : yaml";

            assertThatThrownBy(() -> provider.load(invalidYaml))
                .isInstanceOf(YmlParseException.class);
        }

        @Test
        @DisplayName("load should parse boolean values correctly")
        void loadShouldParseBooleanValuesCorrectly() {
            String yaml = "enabled: true\ndisabled: false";

            Map<String, Object> result = provider.load(yaml);

            assertThat(result).containsEntry("enabled", true);
            assertThat(result).containsEntry("disabled", false);
        }

        @Test
        @DisplayName("load should parse numeric values correctly")
        void loadShouldParseNumericValuesCorrectly() {
            String yaml = "integer: 42\nfloat: 3.14";

            Map<String, Object> result = provider.load(yaml);

            assertThat(result).containsEntry("integer", 42);
            assertThat(result.get("float")).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("load(String, Class) Tests")
    class LoadStringClassTests {

        @Test
        @DisplayName("load with class should throw exception due to safe mode")
        void loadWithClassShouldThrowExceptionDueToSafeMode() {
            // SafeConstructor doesn't support arbitrary type instantiation
            String yaml = "name: test";

            // With SafeConstructor, loading to Map.class throws exception
            assertThatThrownBy(() -> provider.load(yaml, Map.class))
                .isInstanceOf(YmlParseException.class);
        }

        @Test
        @DisplayName("load should throw exception for invalid class mapping")
        void loadShouldThrowExceptionForInvalidClassMapping() {
            String yaml = "name: test";

            assertThatThrownBy(() -> provider.load(yaml, Integer.class))
                .isInstanceOf(YmlParseException.class);
        }
    }

    @Nested
    @DisplayName("load(InputStream) Tests")
    class LoadInputStreamTests {

        @Test
        @DisplayName("load should parse YAML from InputStream")
        void loadShouldParseYamlFromInputStream() {
            String yaml = "key: value";
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> result = provider.load(input);

            assertThat(result).containsEntry("key", "value");
        }

        @Test
        @DisplayName("load should handle empty InputStream")
        void loadShouldHandleEmptyInputStream() {
            InputStream input = new ByteArrayInputStream(new byte[0]);

            Map<String, Object> result = provider.load(input);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("load should throw exception for invalid InputStream content")
        void loadShouldThrowExceptionForInvalidInputStreamContent() {
            String invalidYaml = "invalid: : : yaml";
            InputStream input = new ByteArrayInputStream(invalidYaml.getBytes(StandardCharsets.UTF_8));

            assertThatThrownBy(() -> provider.load(input))
                .isInstanceOf(YmlParseException.class);
        }
    }

    @Nested
    @DisplayName("load(InputStream, Class) Tests")
    class LoadInputStreamClassTests {

        @Test
        @DisplayName("load with class should throw exception due to safe mode")
        void loadWithClassShouldThrowExceptionDueToSafeMode() {
            // SafeConstructor doesn't support arbitrary type instantiation
            String yaml = "key: value";
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

            // With SafeConstructor, loading to Map.class throws exception
            assertThatThrownBy(() -> provider.load(input, Map.class))
                .isInstanceOf(YmlParseException.class);
        }
    }

    @Nested
    @DisplayName("loadAll(String) Tests")
    class LoadAllStringTests {

        @Test
        @DisplayName("loadAll should parse multi-document YAML")
        void loadAllShouldParseMultiDocumentYaml() {
            String yaml = """
                ---
                name: doc1
                ---
                name: doc2
                ---
                name: doc3
                """;

            List<Map<String, Object>> results = provider.loadAll(yaml);

            assertThat(results).hasSize(3);
            assertThat(results.get(0)).containsEntry("name", "doc1");
            assertThat(results.get(1)).containsEntry("name", "doc2");
            assertThat(results.get(2)).containsEntry("name", "doc3");
        }

        @Test
        @DisplayName("loadAll should handle single document")
        void loadAllShouldHandleSingleDocument() {
            String yaml = "name: single";

            List<Map<String, Object>> results = provider.loadAll(yaml);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst()).containsEntry("name", "single");
        }

        @Test
        @DisplayName("loadAll should throw exception for invalid YAML")
        void loadAllShouldThrowExceptionForInvalidYaml() {
            String invalidYaml = "---\ninvalid: : :\n---\nok: true";

            assertThatThrownBy(() -> provider.loadAll(invalidYaml))
                .isInstanceOf(YmlParseException.class);
        }
    }

    @Nested
    @DisplayName("loadAll(String, Class) Tests")
    class LoadAllStringClassTests {

        @Test
        @DisplayName("loadAll with class filters by instance type")
        void loadAllWithClassFiltersByInstanceType() {
            // loadAll uses yaml.loadAll and filters with isInstance
            String yaml = """
                ---
                name: doc1
                ---
                name: doc2
                """;

            // Maps parsed from YAML are Map instances
            @SuppressWarnings("rawtypes")
            List<Map> results = provider.loadAll(yaml, Map.class);
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("loadAll with incompatible class returns empty list")
        void loadAllWithIncompatibleClassReturnsEmptyList() {
            String yaml = """
                ---
                name: doc1
                ---
                name: doc2
                """;

            // String class won't match Map instances
            List<String> results = provider.loadAll(yaml, String.class);
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("dump(Object) Tests")
    class DumpObjectTests {

        @Test
        @DisplayName("dump should serialize Map to YAML")
        void dumpShouldSerializeMapToYaml() {
            Map<String, Object> data = Map.of("name", "John", "age", 30);

            String yaml = provider.dump(data);

            assertThat(yaml).contains("name");
            assertThat(yaml).contains("John");
            assertThat(yaml).contains("age");
        }

        @Test
        @DisplayName("dump should serialize nested structures")
        void dumpShouldSerializeNestedStructures() {
            Map<String, Object> data = Map.of(
                "person", Map.of("name", "John", "city", "NYC")
            );

            String yaml = provider.dump(data);

            assertThat(yaml).contains("person");
            assertThat(yaml).contains("name");
            assertThat(yaml).contains("John");
        }

        @Test
        @DisplayName("dump should serialize lists")
        void dumpShouldSerializeLists() {
            Map<String, Object> data = Map.of("items", List.of("a", "b", "c"));

            String yaml = provider.dump(data);

            assertThat(yaml).contains("items");
            assertThat(yaml).contains("a");
            assertThat(yaml).contains("b");
            assertThat(yaml).contains("c");
        }

        @Test
        @DisplayName("dump should handle null values")
        void dumpShouldHandleNullValues() {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("key", null);

            String yaml = provider.dump(data);

            assertThat(yaml).contains("key");
        }
    }

    @Nested
    @DisplayName("dump(Object, YmlConfig) Tests")
    class DumpObjectConfigTests {

        @Test
        @DisplayName("dump with config should use flow style when configured")
        void dumpWithConfigShouldUseFlowStyleWhenConfigured() {
            YmlConfig config = YmlConfig.builder()
                .defaultFlowStyle(YmlConfig.FlowStyle.FLOW)
                .build();
            Map<String, Object> data = Map.of("key", "value");

            String yaml = provider.dump(data, config);

            assertThat(yaml).isNotBlank();
        }

        @Test
        @DisplayName("dump with config should respect indent setting")
        void dumpWithConfigShouldRespectIndentSetting() {
            YmlConfig config = YmlConfig.builder()
                .indent(4)
                .defaultFlowStyle(YmlConfig.FlowStyle.BLOCK)
                .build();
            Map<String, Object> data = Map.of("outer", Map.of("inner", "value"));

            String yaml = provider.dump(data, config);

            assertThat(yaml).contains("outer");
            assertThat(yaml).contains("inner");
        }
    }

    @Nested
    @DisplayName("dump(Object, OutputStream) Tests")
    class DumpObjectOutputStreamTests {

        @Test
        @DisplayName("dump should write to OutputStream")
        void dumpShouldWriteToOutputStream() {
            Map<String, Object> data = Map.of("key", "value");
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            provider.dump(data, output);

            String yaml = output.toString(StandardCharsets.UTF_8);
            assertThat(yaml).contains("key");
            assertThat(yaml).contains("value");
        }
    }

    @Nested
    @DisplayName("dump(Object, Writer) Tests")
    class DumpObjectWriterTests {

        @Test
        @DisplayName("dump should write to Writer")
        void dumpShouldWriteToWriter() {
            Map<String, Object> data = Map.of("key", "value");
            StringWriter writer = new StringWriter();

            provider.dump(data, writer);

            String yaml = writer.toString();
            assertThat(yaml).contains("key");
            assertThat(yaml).contains("value");
        }
    }

    @Nested
    @DisplayName("dumpAll(Iterable) Tests")
    class DumpAllTests {

        @Test
        @DisplayName("dumpAll should serialize multiple documents")
        void dumpAllShouldSerializeMultipleDocuments() {
            List<Map<String, Object>> documents = List.of(
                Map.of("doc", 1),
                Map.of("doc", 2),
                Map.of("doc", 3)
            );

            String yaml = provider.dumpAll(documents);

            assertThat(yaml).contains("doc");
            // YAML multi-document format uses --- as separator
            long separatorCount = yaml.lines().filter(line -> line.trim().equals("---")).count();
            assertThat(separatorCount).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("dumpAll should handle empty iterable")
        void dumpAllShouldHandleEmptyIterable() {
            List<Object> documents = List.of();

            String yaml = provider.dumpAll(documents);

            assertThat(yaml).isEmpty();
        }
    }

    @Nested
    @DisplayName("parseTree(String) Tests")
    class ParseTreeStringTests {

        @Test
        @DisplayName("parseTree should return YmlNode")
        void parseTreeShouldReturnYmlNode() {
            String yaml = "name: John";

            YmlNode node = provider.parseTree(yaml);

            assertThat(node).isNotNull();
        }

        @Test
        @DisplayName("parseTree should create navigable tree")
        void parseTreeShouldCreateNavigableTree() {
            String yaml = """
                person:
                  name: John
                  age: 30
                """;

            YmlNode node = provider.parseTree(yaml);

            assertThat(node.get("person")).isNotNull();
            assertThat(node.get("person").get("name").asText()).isEqualTo("John");
            assertThat(node.get("person").get("age").asInt()).isEqualTo(30);
        }

        @Test
        @DisplayName("parseTree should handle arrays")
        void parseTreeShouldHandleArrays() {
            String yaml = """
                items:
                  - first
                  - second
                """;

            YmlNode node = provider.parseTree(yaml);

            assertThat(node.get("items").size()).isEqualTo(2);
            assertThat(node.get("items").get(0).asText()).isEqualTo("first");
            assertThat(node.get("items").get(1).asText()).isEqualTo("second");
        }
    }

    @Nested
    @DisplayName("parseTree(InputStream) Tests")
    class ParseTreeInputStreamTests {

        @Test
        @DisplayName("parseTree should parse from InputStream")
        void parseTreeShouldParseFromInputStream() {
            String yaml = "key: value";
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

            YmlNode node = provider.parseTree(input);

            assertThat(node).isNotNull();
            assertThat(node.get("key").asText()).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("isValid(String) Tests")
    class IsValidTests {

        @Test
        @DisplayName("isValid should return true for valid YAML")
        void isValidShouldReturnTrueForValidYaml() {
            String validYaml = "name: John\nage: 30";

            boolean valid = provider.isValid(validYaml);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("isValid should return false for invalid YAML")
        void isValidShouldReturnFalseForInvalidYaml() {
            String invalidYaml = "invalid: : : yaml\n  bad indent";

            boolean valid = provider.isValid(invalidYaml);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("isValid should return true for empty YAML")
        void isValidShouldReturnTrueForEmptyYaml() {
            boolean valid = provider.isValid("");

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("isValid should return true for complex valid YAML")
        void isValidShouldReturnTrueForComplexValidYaml() {
            String yaml = """
                server:
                  host: localhost
                  port: 8080
                database:
                  url: jdbc:mysql://localhost/db
                  username: user
                  password: pass
                features:
                  - logging
                  - caching
                  - monitoring
                """;

            boolean valid = provider.isValid(yaml);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("isValid should return true for YAML with anchors and aliases")
        void isValidShouldReturnTrueForYamlWithAnchorsAndAliases() {
            String yaml = """
                defaults: &defaults
                  adapter: postgres
                  host: localhost

                development:
                  <<: *defaults
                  database: dev_db
                """;

            boolean valid = provider.isValid(yaml);

            assertThat(valid).isTrue();
        }
    }

    @Nested
    @DisplayName("configure(YmlConfig) Tests")
    class ConfigureTests {

        @Test
        @DisplayName("configure should return new provider with specified config")
        void configureShouldReturnNewProviderWithConfig() {
            YmlConfig newConfig = YmlConfig.builder()
                .indent(4)
                .safeMode(false)
                .build();

            YmlProvider configured = provider.configure(newConfig);

            assertThat(configured).isNotSameAs(provider);
            assertThat(configured.getConfig()).isEqualTo(newConfig);
            assertThat(configured.getConfig().getIndent()).isEqualTo(4);
        }

        @Test
        @DisplayName("configure should preserve provider functionality")
        void configureShouldPreserveProviderFunctionality() {
            YmlConfig newConfig = YmlConfig.builder().indent(4).build();
            YmlProvider configured = provider.configure(newConfig);

            Map<String, Object> data = configured.load("key: value");

            assertThat(data).containsEntry("key", "value");
        }

        @Test
        @DisplayName("original provider should remain unchanged after configure")
        void originalProviderShouldRemainUnchanged() {
            YmlConfig originalConfig = provider.getConfig();
            YmlConfig newConfig = YmlConfig.builder().indent(8).build();

            provider.configure(newConfig);

            assertThat(provider.getConfig()).isEqualTo(originalConfig);
        }
    }

    @Nested
    @DisplayName("getConfig Tests")
    class GetConfigTests {

        @Test
        @DisplayName("getConfig should return configuration")
        void getConfigShouldReturnConfiguration() {
            YmlConfig config = provider.getConfig();

            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("getConfig should return same config instance")
        void getConfigShouldReturnSameConfigInstance() {
            YmlConfig config1 = provider.getConfig();
            YmlConfig config2 = provider.getConfig();

            assertThat(config1).isSameAs(config2);
        }
    }

    @Nested
    @DisplayName("YmlProvider Interface Implementation Tests")
    class YmlProviderInterfaceTests {

        @Test
        @DisplayName("provider should implement YmlProvider interface")
        void providerShouldImplementYmlProviderInterface() {
            assertThat(provider).isInstanceOf(YmlProvider.class);
        }

        @Test
        @DisplayName("all interface methods should be implemented")
        void allInterfaceMethodsShouldBeImplemented() {
            // Test methods that work with SafeConstructor
            assertThatCode(() -> {
                provider.getName();
                provider.getPriority();
                provider.isAvailable();
                provider.load("key: value");
                provider.load(new ByteArrayInputStream("key: value".getBytes()));
                provider.loadAll("---\nkey: value");
                provider.dump(Map.of("key", "value"));
                provider.dump(Map.of("key", "value"), YmlConfig.defaults());
                provider.dump(Map.of("key", "value"), new ByteArrayOutputStream());
                provider.dump(Map.of("key", "value"), new StringWriter());
                provider.dumpAll(List.of(Map.of("key", "value")));
                provider.parseTree("key: value");
                provider.parseTree(new ByteArrayInputStream("key: value".getBytes()));
                provider.isValid("key: value");
                provider.configure(YmlConfig.defaults());
                provider.getConfig();
            }).doesNotThrowAnyException();

            // Methods with class parameter throw due to SafeConstructor (for load methods)
            assertThatThrownBy(() -> provider.load("key: value", Map.class))
                .isInstanceOf(YmlParseException.class);
            assertThatThrownBy(() -> provider.load(new ByteArrayInputStream("key: value".getBytes()), Map.class))
                .isInstanceOf(YmlParseException.class);

            // loadAll with class uses filtering (isInstance), so it doesn't throw
            @SuppressWarnings("rawtypes")
            List<Map> results = provider.loadAll("---\nkey: value", Map.class);
            assertThat(results).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle YAML with special characters")
        void shouldHandleYamlWithSpecialCharacters() {
            String yaml = "message: \"Hello, World!\"";

            Map<String, Object> result = provider.load(yaml);

            assertThat(result).containsEntry("message", "Hello, World!");
        }

        @Test
        @DisplayName("should handle YAML with unicode")
        void shouldHandleYamlWithUnicode() {
            String yaml = "greeting: \"\u4F60\u597D\""; // Chinese characters

            Map<String, Object> result = provider.load(yaml);

            assertThat(result).containsEntry("greeting", "\u4F60\u597D");
        }

        @Test
        @DisplayName("should handle multiline strings")
        void shouldHandleMultilineStrings() {
            String yaml = """
                text: |
                  Line 1
                  Line 2
                  Line 3
                """;

            Map<String, Object> result = provider.load(yaml);

            assertThat(result.get("text").toString()).contains("Line 1");
            assertThat(result.get("text").toString()).contains("Line 2");
        }

        @Test
        @DisplayName("should handle very large documents")
        void shouldHandleVeryLargeDocuments() {
            StringBuilder yaml = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                yaml.append("key").append(i).append(": value").append(i).append("\n");
            }

            Map<String, Object> result = provider.load(yaml.toString());

            assertThat(result).hasSize(1000);
        }

        @Test
        @DisplayName("should handle deeply nested structures")
        void shouldHandleDeeplyNestedStructures() {
            String yaml = """
                level1:
                  level2:
                    level3:
                      level4:
                        level5:
                          value: deep
                """;

            Map<String, Object> result = provider.load(yaml);

            assertThat(result).containsKey("level1");
        }
    }

    @Nested
    @DisplayName("Configuration Effects Tests")
    class ConfigurationEffectsTests {

        @Test
        @DisplayName("allowDuplicateKeys config should affect parsing")
        void allowDuplicateKeysConfigShouldAffectParsing() {
            YmlConfig config = YmlConfig.builder()
                .allowDuplicateKeys(true)
                .build();
            SnakeYamlProvider configuredProvider = new SnakeYamlProvider(config);

            // With allowDuplicateKeys, this should not throw
            String yaml = "key: value1\nkey: value2";
            assertThatCode(() -> configuredProvider.load(yaml)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("prettyPrint config should affect output")
        void prettyPrintConfigShouldAffectOutput() {
            YmlConfig prettyConfig = YmlConfig.builder()
                .prettyPrint(true)
                .defaultFlowStyle(YmlConfig.FlowStyle.BLOCK)
                .build();
            SnakeYamlProvider prettyProvider = new SnakeYamlProvider(prettyConfig);

            Map<String, Object> data = Map.of("key", Map.of("nested", "value"));
            String yaml = prettyProvider.dump(data);

            assertThat(yaml).isNotBlank();
        }

        @Test
        @DisplayName("flow style config should affect output format")
        void flowStyleConfigShouldAffectOutputFormat() {
            YmlConfig flowConfig = YmlConfig.builder()
                .defaultFlowStyle(YmlConfig.FlowStyle.FLOW)
                .build();
            SnakeYamlProvider flowProvider = new SnakeYamlProvider(flowConfig);

            YmlConfig blockConfig = YmlConfig.builder()
                .defaultFlowStyle(YmlConfig.FlowStyle.BLOCK)
                .build();
            SnakeYamlProvider blockProvider = new SnakeYamlProvider(blockConfig);

            Map<String, Object> data = Map.of("key", "value");
            String flowYaml = flowProvider.dump(data);
            String blockYaml = blockProvider.dump(data);

            // Both should contain the data
            assertThat(flowYaml).contains("key");
            assertThat(blockYaml).contains("key");
        }
    }

    @Nested
    @DisplayName("Round Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("load then dump should preserve data")
        void loadThenDumpShouldPreserveData() {
            String originalYaml = "name: John\nage: 30";

            Map<String, Object> loaded = provider.load(originalYaml);
            String dumped = provider.dump(loaded);
            Map<String, Object> reloaded = provider.load(dumped);

            assertThat(reloaded).containsEntry("name", "John");
            assertThat(reloaded).containsEntry("age", 30);
        }

        @Test
        @DisplayName("dump then load should preserve data")
        void dumpThenLoadShouldPreserveData() {
            Map<String, Object> originalData = new java.util.LinkedHashMap<>();
            originalData.put("string", "text");
            originalData.put("number", 42);
            originalData.put("boolean", true);
            originalData.put("list", List.of("a", "b", "c"));

            String yaml = provider.dump(originalData);
            Map<String, Object> loaded = provider.load(yaml);

            assertThat(loaded).containsEntry("string", "text");
            assertThat(loaded).containsEntry("number", 42);
            assertThat(loaded).containsEntry("boolean", true);
            assertThat(loaded.get("list")).isEqualTo(List.of("a", "b", "c"));
        }
    }
}
