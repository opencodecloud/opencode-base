package cloud.opencode.base.yml;

import cloud.opencode.base.yml.exception.OpenYmlException;
import cloud.opencode.base.yml.merge.MergeStrategy;
import cloud.opencode.base.yml.placeholder.PlaceholderResolver;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenYmlTest Tests
 * OpenYmlTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("OpenYml Tests")
class OpenYmlTest {

    private static final String SIMPLE_YAML = """
        name: test
        version: 1.0
        """;

    private static final String NESTED_YAML = """
        server:
          host: localhost
          port: 8080
        database:
          url: jdbc:mysql://localhost/db
        """;

    private static final String ITEMS_YAML = """
        server:
          port: 8080
          host: localhost
        items:
          - one
          - two
        """;

    @Nested
    @DisplayName("Load Tests")
    class LoadTests {

        @Test
        @DisplayName("load should parse YAML to Map")
        void loadShouldParseYamlToMap() {
            Map<String, Object> data = OpenYml.load(SIMPLE_YAML);

            assertThat(data).containsEntry("name", "test");
            // Note: YAML parses 1.0 as a number, not string
            assertThat(data.get("version")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("load should handle nested YAML")
        void loadShouldHandleNestedYaml() {
            Map<String, Object> data = OpenYml.load(NESTED_YAML);

            assertThat(data).containsKey("server");
            assertThat(data).containsKey("database");
        }

        @Test
        @DisplayName("load should handle empty YAML")
        void loadShouldHandleEmptyYaml() {
            Map<String, Object> data = OpenYml.load("");

            assertThat(data).isNotNull();
        }

        @Test
        @DisplayName("load should handle YAML with arrays")
        void loadShouldHandleYamlWithArrays() {
            Map<String, Object> data = OpenYml.load(ITEMS_YAML);

            assertThat(data).containsKey("items");
            @SuppressWarnings("unchecked")
            List<String> items = (List<String>) data.get("items");
            assertThat(items).containsExactly("one", "two");
        }

        @Test
        @DisplayName("load should handle various data types")
        void loadShouldHandleVariousDataTypes() {
            String yaml = """
                stringValue: hello
                intValue: 42
                floatValue: 3.14
                boolValue: true
                nullValue: null
                """;

            Map<String, Object> data = OpenYml.load(yaml);

            assertThat(data.get("stringValue")).isEqualTo("hello");
            assertThat(data.get("intValue")).isEqualTo(42);
            assertThat(data.get("floatValue")).isEqualTo(3.14);
            assertThat(data.get("boolValue")).isEqualTo(true);
            assertThat(data.get("nullValue")).isNull();
        }
    }

    @Nested
    @DisplayName("Parse Tests")
    class ParseTests {

        @Test
        @DisplayName("parse should return YmlDocument")
        void parseShouldReturnYmlDocument() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            assertThat(doc).isNotNull();
            assertThat(doc.getString("name")).isEqualTo("test");
        }

        @Test
        @DisplayName("parse should handle empty YAML")
        void parseShouldHandleEmptyYaml() {
            YmlDocument doc = OpenYml.parse("");

            assertThat(doc).isNotNull();
        }

        @Test
        @DisplayName("parse should support path access")
        void parseShouldSupportPathAccess() {
            YmlDocument doc = OpenYml.parse(NESTED_YAML);

            assertThat(doc.getString("server.host")).isEqualTo("localhost");
            assertThat(doc.getInt("server.port")).isEqualTo(8080);
        }

        @Test
        @DisplayName("parse should support array index access")
        void parseShouldSupportArrayIndexAccess() {
            YmlDocument doc = OpenYml.parse(ITEMS_YAML);

            String firstItem = doc.get("items[0]");
            String secondItem = doc.get("items[1]");

            assertThat(firstItem).isEqualTo("one");
            assertThat(secondItem).isEqualTo("two");
        }
    }

    @Nested
    @DisplayName("LoadFile Tests")
    class LoadFileTests {

        @Test
        @DisplayName("loadFile should load from file")
        void loadFileShouldLoadFromFile() throws IOException {
            Path tempFile = Files.createTempFile("test", ".yml");
            try {
                Files.writeString(tempFile, SIMPLE_YAML);

                YmlDocument doc = OpenYml.loadFile(tempFile);

                assertThat(doc.getString("name")).isEqualTo("test");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("loadFile should load with charset")
        void loadFileShouldLoadWithCharset() throws IOException {
            Path tempFile = Files.createTempFile("test", ".yml");
            try {
                Files.writeString(tempFile, SIMPLE_YAML, StandardCharsets.UTF_8);

                YmlDocument doc = OpenYml.loadFile(tempFile, StandardCharsets.UTF_8);

                assertThat(doc.getString("name")).isEqualTo("test");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("loadFile should throw for non-existent file")
        void loadFileShouldThrowForNonExistentFile() {
            Path nonExistent = Path.of("/non/existent/file.yml");

            assertThatThrownBy(() -> OpenYml.loadFile(nonExistent))
                .isInstanceOf(OpenYmlException.class)
                .hasMessageContaining("Failed to read YAML file");
        }

        @Test
        @DisplayName("loadFile should handle UTF-8 BOM")
        void loadFileShouldHandleUtf8Bom() throws IOException {
            Path tempFile = Files.createTempFile("test", ".yml");
            try {
                // Write UTF-8 with BOM
                byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
                byte[] content = SIMPLE_YAML.getBytes(StandardCharsets.UTF_8);
                byte[] withBom = new byte[bom.length + content.length];
                System.arraycopy(bom, 0, withBom, 0, bom.length);
                System.arraycopy(content, 0, withBom, bom.length, content.length);
                Files.write(tempFile, withBom);

                YmlDocument doc = OpenYml.loadFile(tempFile, StandardCharsets.UTF_8);

                assertThat(doc).isNotNull();
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    @Nested
    @DisplayName("LoadStream Tests")
    class LoadStreamTests {

        @Test
        @DisplayName("loadStream should load from InputStream")
        void loadStreamShouldLoadFromInputStream() {
            InputStream input = new ByteArrayInputStream(SIMPLE_YAML.getBytes(StandardCharsets.UTF_8));

            YmlDocument doc = OpenYml.loadStream(input);

            assertThat(doc.getString("name")).isEqualTo("test");
        }

        @Test
        @DisplayName("loadStream should handle empty stream")
        void loadStreamShouldHandleEmptyStream() {
            InputStream input = new ByteArrayInputStream(new byte[0]);

            YmlDocument doc = OpenYml.loadStream(input);

            assertThat(doc).isNotNull();
        }
    }

    @Nested
    @DisplayName("LoadAll Tests")
    class LoadAllTests {

        @Test
        @DisplayName("loadAll should load multi-document YAML")
        void loadAllShouldLoadMultiDocumentYaml() {
            String multiDoc = """
                ---
                name: doc1
                ---
                name: doc2
                """;

            List<YmlDocument> docs = OpenYml.loadAll(multiDoc);

            assertThat(docs).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("loadAll should load single document")
        void loadAllShouldLoadSingleDocument() {
            List<YmlDocument> docs = OpenYml.loadAll(SIMPLE_YAML);

            assertThat(docs).hasSizeGreaterThanOrEqualTo(1);
            assertThat(docs.getFirst().getString("name")).isEqualTo("test");
        }

        @Test
        @DisplayName("loadAll should handle empty YAML")
        void loadAllShouldHandleEmptyYaml() {
            List<YmlDocument> docs = OpenYml.loadAll("");

            assertThat(docs).isNotNull();
        }
    }

    @Nested
    @DisplayName("Dump Tests")
    class DumpTests {

        @Test
        @DisplayName("dump should serialize Map to YAML")
        void dumpShouldSerializeMapToYaml() {
            Map<String, Object> data = Map.of("name", "test", "version", "1.0");

            String yaml = OpenYml.dump(data);

            assertThat(yaml).contains("name:");
            assertThat(yaml).contains("version:");
        }

        @Test
        @DisplayName("dump with config should use configuration")
        void dumpWithConfigShouldUseConfiguration() {
            Map<String, Object> data = Map.of("name", "test");
            YmlConfig config = YmlConfig.builder().indent(4).build();

            String yaml = OpenYml.dump(data, config);

            assertThat(yaml).contains("name:");
        }

        @Test
        @DisplayName("dump document should serialize document")
        void dumpDocumentShouldSerializeDocument() {
            YmlDocument doc = OpenYml.parse(SIMPLE_YAML);

            String yaml = OpenYml.dump(doc);

            assertThat(yaml).contains("name:");
        }

        @Test
        @DisplayName("dump should handle nested structures")
        void dumpShouldHandleNestedStructures() {
            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("server", Map.of("host", "localhost", "port", 8080));

            String yaml = OpenYml.dump(nested);

            assertThat(yaml).contains("server:");
            assertThat(yaml).contains("host:");
            assertThat(yaml).contains("port:");
        }

        @Test
        @DisplayName("dump should handle arrays")
        void dumpShouldHandleArrays() {
            Map<String, Object> data = Map.of("items", List.of("one", "two", "three"));

            String yaml = OpenYml.dump(data);

            assertThat(yaml).contains("items:");
            assertThat(yaml).contains("- one");
            assertThat(yaml).contains("- two");
            assertThat(yaml).contains("- three");
        }

        @Test
        @DisplayName("dump should handle null values")
        void dumpShouldHandleNullValues() {
            Map<String, Object> data = new HashMap<>();
            data.put("nullValue", null);
            data.put("name", "test");

            String yaml = OpenYml.dump(data);

            assertThat(yaml).isNotNull();
        }
    }

    @Nested
    @DisplayName("DumpObject Tests")
    class DumpObjectTests {

        @Test
        @DisplayName("dumpObject should serialize object to YAML")
        void dumpObjectShouldSerializeObjectToYaml() {
            TestConfig config = new TestConfig();
            config.name = "test";
            config.value = 42;

            String yaml = OpenYml.dumpObject(config);

            assertThat(yaml).contains("name:");
            assertThat(yaml).contains("test");
        }
    }

    @Nested
    @DisplayName("WriteFile Tests")
    class WriteFileTests {

        @Test
        @DisplayName("writeFile should write YAML to file")
        void writeFileShouldWriteYamlToFile() throws IOException {
            Path tempFile = Files.createTempFile("output", ".yml");
            try {
                Map<String, Object> data = Map.of("name", "test");

                OpenYml.writeFile(data, tempFile);

                String content = Files.readString(tempFile);
                assertThat(content).contains("name:");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("writeFile with charset should use charset")
        void writeFileWithCharsetShouldUseCharset() throws IOException {
            Path tempFile = Files.createTempFile("output", ".yml");
            try {
                Map<String, Object> data = Map.of("name", "test");

                OpenYml.writeFile(data, tempFile, StandardCharsets.UTF_8);

                String content = Files.readString(tempFile);
                assertThat(content).contains("name:");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("writeFile should throw for invalid path")
        void writeFileShouldThrowForInvalidPath() {
            Path invalidPath = Path.of("/invalid/directory/file.yml");
            Map<String, Object> data = Map.of("name", "test");

            assertThatThrownBy(() -> OpenYml.writeFile(data, invalidPath))
                .isInstanceOf(OpenYmlException.class)
                .hasMessageContaining("Failed to write YAML file");
        }
    }

    @Nested
    @DisplayName("Write Tests")
    class WriteTests {

        @Test
        @DisplayName("write should write to writer")
        void writeShouldWriteToWriter() {
            Map<String, Object> data = Map.of("name", "test");
            StringWriter writer = new StringWriter();

            OpenYml.write(data, writer);

            assertThat(writer.toString()).contains("name:");
        }

        @Test
        @DisplayName("write should handle nested data")
        void writeShouldHandleNestedData() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("server", Map.of("host", "localhost"));
            StringWriter writer = new StringWriter();

            OpenYml.write(data, writer);

            String result = writer.toString();
            assertThat(result).contains("server:");
            assertThat(result).contains("host:");
        }
    }

    @Nested
    @DisplayName("DumpAll Tests")
    class DumpAllTests {

        @Test
        @DisplayName("dumpAll should serialize multiple documents")
        void dumpAllShouldSerializeMultipleDocuments() {
            List<Map<String, Object>> docs = List.of(
                Map.of("name", "doc1"),
                Map.of("name", "doc2")
            );

            String yaml = OpenYml.dumpAll(docs);

            assertThat(yaml).contains("name:");
        }

        @Test
        @DisplayName("dumpAll should handle empty list")
        void dumpAllShouldHandleEmptyList() {
            List<Map<String, Object>> docs = List.of();

            String yaml = OpenYml.dumpAll(docs);

            assertThat(yaml).isNotNull();
        }
    }

    @Nested
    @DisplayName("Bind Tests")
    class BindTests {

        @Test
        @DisplayName("bind should bind document to object")
        void bindShouldBindDocumentToObject() {
            String yaml = """
                name: test
                value: 42
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            TestConfig config = OpenYml.bind(doc, TestConfig.class);

            assertThat(config.name).isEqualTo("test");
            assertThat(config.value).isEqualTo(42);
        }

        @Test
        @DisplayName("bind with path should bind from path")
        void bindWithPathShouldBindFromPath() {
            String yaml = """
                server:
                  name: test
                  value: 42
                """;
            YmlDocument doc = OpenYml.parse(yaml);

            TestConfig config = OpenYml.bind(doc, "server", TestConfig.class);

            assertThat(config.name).isEqualTo("test");
        }

        @Test
        @DisplayName("bind YAML string should bind directly")
        void bindYamlStringShouldBindDirectly() {
            String yaml = """
                name: test
                value: 42
                """;

            TestConfig config = OpenYml.bind(yaml, TestConfig.class);

            assertThat(config.name).isEqualTo("test");
        }

        @Test
        @DisplayName("bind should handle nested objects")
        void bindShouldHandleNestedObjects() {
            String yaml = """
                name: parent
                value: 100
                """;

            TestConfig config = OpenYml.bind(yaml, TestConfig.class);

            assertThat(config.name).isEqualTo("parent");
            assertThat(config.value).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("ToMap Tests")
    class ToMapTests {

        @Test
        @DisplayName("toMap should convert object to Map")
        void toMapShouldConvertObjectToMap() {
            TestConfig config = new TestConfig();
            config.name = "test";
            config.value = 42;

            Map<String, Object> map = OpenYml.toMap(config);

            assertThat(map).containsEntry("name", "test");
            assertThat(map).containsEntry("value", 42);
        }

        @Test
        @DisplayName("toMap should handle null fields")
        void toMapShouldHandleNullFields() {
            TestConfig config = new TestConfig();
            config.name = null;
            config.value = 0;

            Map<String, Object> map = OpenYml.toMap(config);

            assertThat(map).containsKey("value");
        }
    }

    @Nested
    @DisplayName("Merge Tests")
    class MergeTests {

        @Test
        @DisplayName("merge should merge two maps")
        void mergeShouldMergeTwoMaps() {
            Map<String, Object> base = new HashMap<>(Map.of("a", "1", "b", "2"));
            Map<String, Object> overlay = new HashMap<>(Map.of("b", "3", "c", "4"));

            Map<String, Object> result = OpenYml.merge(base, overlay);

            assertThat(result).containsEntry("a", "1");
            assertThat(result).containsEntry("b", "3");
            assertThat(result).containsEntry("c", "4");
        }

        @Test
        @DisplayName("merge with strategy should use strategy")
        void mergeWithStrategyShouldUseStrategy() {
            Map<String, Object> base = new HashMap<>(Map.of("a", "1"));
            Map<String, Object> overlay = new HashMap<>(Map.of("a", "2"));

            Map<String, Object> result = OpenYml.merge(base, overlay, MergeStrategy.KEEP_FIRST);

            assertThat(result).containsEntry("a", "1");
        }

        @Test
        @DisplayName("mergeAll should merge multiple maps")
        void mergeAllShouldMergeMultipleMaps() {
            Map<String, Object> map1 = new HashMap<>(Map.of("a", "1"));
            Map<String, Object> map2 = new HashMap<>(Map.of("b", "2"));
            Map<String, Object> map3 = new HashMap<>(Map.of("c", "3"));

            Map<String, Object> result = OpenYml.mergeAll(map1, map2, map3);

            assertThat(result).containsKeys("a", "b", "c");
        }

        @Test
        @DisplayName("merge documents should merge two documents")
        void mergeDocumentsShouldMergeTwoDocuments() {
            YmlDocument base = OpenYml.parse("a: 1");
            YmlDocument overlay = OpenYml.parse("b: 2");

            YmlDocument result = OpenYml.merge(base, overlay);

            assertThat(result.has("a")).isTrue();
            assertThat(result.has("b")).isTrue();
        }

        @Test
        @DisplayName("merge should deep merge nested maps")
        void mergeShouldDeepMergeNestedMaps() {
            Map<String, Object> base = new HashMap<>();
            base.put("server", new HashMap<>(Map.of("host", "localhost", "port", 8080)));

            Map<String, Object> overlay = new HashMap<>();
            overlay.put("server", new HashMap<>(Map.of("port", 9090)));

            Map<String, Object> result = OpenYml.merge(base, overlay);

            @SuppressWarnings("unchecked")
            Map<String, Object> server = (Map<String, Object>) result.get("server");
            assertThat(server.get("host")).isEqualTo("localhost");
            assertThat(server.get("port")).isEqualTo(9090);
        }
    }

    @Nested
    @DisplayName("Placeholder Tests")
    class PlaceholderTests {

        @Test
        @DisplayName("resolvePlaceholders should resolve placeholders")
        void resolvePlaceholdersShouldResolvePlaceholders() {
            System.setProperty("test.value", "resolved");
            String yaml = "value: ${test.value}";

            String result = OpenYml.resolvePlaceholders(yaml);

            assertThat(result).contains("resolved");
        }

        @Test
        @DisplayName("resolvePlaceholders with custom resolver should use resolver")
        void resolvePlaceholdersWithCustomResolverShouldUseResolver() {
            PlaceholderResolver resolver = PlaceholderResolver.create(Map.of("custom.key", "custom-value"));
            String yaml = "value: ${custom.key}";

            String result = OpenYml.resolvePlaceholders(yaml, resolver);

            assertThat(result).contains("custom-value");
        }

        @Test
        @DisplayName("resolvePlaceholders in map should resolve")
        void resolvePlaceholdersInMapShouldResolve() {
            System.setProperty("test.map.value", "resolved");
            Map<String, Object> data = new HashMap<>();
            data.put("key", "${test.map.value}");

            Map<String, Object> result = OpenYml.resolvePlaceholders(data);

            assertThat(result.get("key")).isEqualTo("resolved");
        }

        @Test
        @DisplayName("parseWithPlaceholders should parse and resolve")
        void parseWithPlaceholdersShouldParseAndResolve() {
            System.setProperty("test.parse.value", "resolved");
            String yaml = "value: ${test.parse.value}";

            YmlDocument doc = OpenYml.parseWithPlaceholders(yaml);

            assertThat(doc.getString("value")).isEqualTo("resolved");
        }

        @Test
        @DisplayName("parseWithPlaceholders with properties should use properties")
        void parseWithPlaceholdersWithPropertiesShouldUseProperties() {
            String yaml = "value: ${custom.key}";
            Map<String, String> properties = Map.of("custom.key", "custom-value");

            YmlDocument doc = OpenYml.parseWithPlaceholders(yaml, properties);

            assertThat(doc.getString("value")).isEqualTo("custom-value");
        }

        @Test
        @DisplayName("should handle missing placeholder with default")
        void shouldHandleMissingPlaceholderWithDefault() {
            String yaml = "value: ${missing.key:default-value}";

            YmlDocument doc = OpenYml.parseWithPlaceholders(yaml);

            assertThat(doc.getString("value")).isEqualTo("default-value");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("isValid should return true for valid YAML")
        void isValidShouldReturnTrueForValidYaml() {
            assertThat(OpenYml.isValid(SIMPLE_YAML)).isTrue();
        }

        @Test
        @DisplayName("isValid should return false for invalid YAML")
        void isValidShouldReturnFalseForInvalidYaml() {
            String invalid = "invalid: [unclosed";

            assertThat(OpenYml.isValid(invalid)).isFalse();
        }

        @Test
        @DisplayName("isValid should return true for empty YAML")
        void isValidShouldReturnTrueForEmptyYaml() {
            assertThat(OpenYml.isValid("")).isTrue();
        }

        @Test
        @DisplayName("isValid should return true for complex valid YAML")
        void isValidShouldReturnTrueForComplexValidYaml() {
            assertThat(OpenYml.isValid(NESTED_YAML)).isTrue();
        }

        @Test
        @DisplayName("isValid should return false for malformed YAML")
        void isValidShouldReturnFalseForMalformedYaml() {
            String malformed = """
                key: value
                  bad: indent
                """;

            assertThat(OpenYml.isValid(malformed)).isFalse();
        }
    }

    @Nested
    @DisplayName("ParseTree Tests")
    class ParseTreeTests {

        @Test
        @DisplayName("parseTree should return YmlNode")
        void parseTreeShouldReturnYmlNode() {
            YmlNode node = OpenYml.parseTree(SIMPLE_YAML);

            assertThat(node).isNotNull();
            assertThat(node.isMapping()).isTrue();
        }

        @Test
        @DisplayName("parseTree should allow node navigation")
        void parseTreeShouldAllowNodeNavigation() {
            YmlNode node = OpenYml.parseTree(NESTED_YAML);

            assertThat(node.get("server").get("host").asText()).isEqualTo("localhost");
            assertThat(node.get("server").get("port").asInt()).isEqualTo(8080);
        }

        @Test
        @DisplayName("parseTree should allow path navigation")
        void parseTreeShouldAllowPathNavigation() {
            YmlNode node = OpenYml.parseTree(NESTED_YAML);

            assertThat(node.at("server.host").asText()).isEqualTo("localhost");
            assertThat(node.at("server.port").asInt()).isEqualTo(8080);
        }

        @Test
        @DisplayName("parseTree should handle arrays")
        void parseTreeShouldHandleArrays() {
            YmlNode node = OpenYml.parseTree(ITEMS_YAML);

            assertThat(node.get("items").isSequence()).isTrue();
            assertThat(node.at("items[0]").asText()).isEqualTo("one");
            assertThat(node.at("items[1]").asText()).isEqualTo("two");
        }
    }

    @Nested
    @DisplayName("Round Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("parse and dump should preserve data")
        void parseAndDumpShouldPreserveData() {
            Map<String, Object> original = OpenYml.load(SIMPLE_YAML);

            String yaml = OpenYml.dump(original);
            Map<String, Object> reparsed = OpenYml.load(yaml);

            assertThat(reparsed).containsAllEntriesOf(original);
        }

        @Test
        @DisplayName("load and dump should be idempotent")
        void loadAndDumpShouldBeIdempotent() {
            String yaml = """
                name: test
                value: 123
                """;

            Map<String, Object> data = OpenYml.load(yaml);
            String dumped1 = OpenYml.dump(data);
            Map<String, Object> data2 = OpenYml.load(dumped1);
            String dumped2 = OpenYml.dump(data2);

            assertThat(dumped1).isEqualTo(dumped2);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle special characters in values")
        void shouldHandleSpecialCharactersInValues() {
            String yaml = """
                special: "value with: colon"
                quoted: 'single quoted'
                """;

            Map<String, Object> data = OpenYml.load(yaml);

            assertThat(data.get("special")).isEqualTo("value with: colon");
            assertThat(data.get("quoted")).isEqualTo("single quoted");
        }

        @Test
        @DisplayName("should handle multiline strings")
        void shouldHandleMultilineStrings() {
            String yaml = """
                multiline: |
                  line 1
                  line 2
                  line 3
                """;

            Map<String, Object> data = OpenYml.load(yaml);

            String multiline = (String) data.get("multiline");
            assertThat(multiline).contains("line 1");
            assertThat(multiline).contains("line 2");
            assertThat(multiline).contains("line 3");
        }

        @Test
        @DisplayName("should handle anchor and alias")
        void shouldHandleAnchorAndAlias() {
            String yaml = """
                defaults: &defaults
                  timeout: 30
                  retries: 3
                production:
                  <<: *defaults
                  timeout: 60
                """;

            Map<String, Object> data = OpenYml.load(yaml);

            @SuppressWarnings("unchecked")
            Map<String, Object> production = (Map<String, Object>) data.get("production");
            assertThat(production.get("timeout")).isEqualTo(60);
            assertThat(production.get("retries")).isEqualTo(3);
        }
    }

    // Test helper class
    public static class TestConfig {
        public String name;
        public int value;
    }
}
