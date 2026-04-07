package cloud.opencode.base.yml.include;

import cloud.opencode.base.yml.exception.OpenYmlException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for YmlIncludeResolver
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
@DisplayName("YmlIncludeResolver Tests")
class YmlIncludeResolverTest {

    @TempDir
    Path tempDir;

    private void writeFile(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    @Nested
    @DisplayName("Basic Include Tests")
    class BasicInclude {

        @Test
        @DisplayName("should resolve a single file include")
        void shouldResolveSingleFileInclude() throws IOException {
            writeFile(tempDir.resolve("db.yml"), "host: localhost\nport: 3306\n");
            writeFile(tempDir.resolve("app.yml"), "app:\n  name: test\ndatabase: \"!include db.yml\"\n");

            YmlIncludeResolver resolver = YmlIncludeResolver.builder()
                    .basePath(tempDir)
                    .build();

            Map<String, Object> result = resolver.resolve(tempDir.resolve("app.yml"));

            assertThat(result).containsKey("app");
            assertThat(result).containsKey("database");

            @SuppressWarnings("unchecked")
            Map<String, Object> db = (Map<String, Object>) result.get("database");
            assertThat(db).containsEntry("host", "localhost");
            assertThat(db).containsEntry("port", 3306);
        }

        @Test
        @DisplayName("should resolve nested includes")
        void shouldResolveNestedIncludes() throws IOException {
            writeFile(tempDir.resolve("inner.yml"), "value: inner-data\n");
            writeFile(tempDir.resolve("middle.yml"), "nested: \"!include inner.yml\"\n");
            writeFile(tempDir.resolve("outer.yml"), "top: \"!include middle.yml\"\n");

            YmlIncludeResolver resolver = YmlIncludeResolver.builder()
                    .basePath(tempDir)
                    .build();

            Map<String, Object> result = resolver.resolve(tempDir.resolve("outer.yml"));

            @SuppressWarnings("unchecked")
            Map<String, Object> top = (Map<String, Object>) result.get("top");
            assertThat(top).containsKey("nested");

            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) top.get("nested");
            assertThat(nested).containsEntry("value", "inner-data");
        }

        @Test
        @DisplayName("should load with static convenience method")
        void shouldLoadWithStaticMethod() throws IOException {
            writeFile(tempDir.resolve("simple.yml"), "key: value\n");

            Map<String, Object> result = YmlIncludeResolver.load(tempDir.resolve("simple.yml"));

            assertThat(result).containsEntry("key", "value");
        }

        @Test
        @DisplayName("should resolve YAML string with basePath")
        void shouldResolveYamlString() throws IOException {
            writeFile(tempDir.resolve("ref.yml"), "refKey: refValue\n");

            YmlIncludeResolver resolver = YmlIncludeResolver.builder()
                    .basePath(tempDir)
                    .build();

            Map<String, Object> result = resolver.resolve(
                    "data: \"!include ref.yml\"\n", tempDir);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            assertThat(data).containsEntry("refKey", "refValue");
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class Security {

        @Test
        @DisplayName("should block path traversal")
        void shouldBlockPathTraversal() throws IOException {
            writeFile(tempDir.resolve("app.yml"), "config: \"!include ../../../etc/passwd\"\n");

            YmlIncludeResolver resolver = YmlIncludeResolver.builder()
                    .basePath(tempDir)
                    .build();

            assertThatThrownBy(() -> resolver.resolve(tempDir.resolve("app.yml")))
                    .isInstanceOf(OpenYmlException.class)
                    .hasMessageContaining("Path traversal detected");
        }

        @Test
        @DisplayName("should detect circular reference")
        void shouldDetectCircularReference() throws IOException {
            writeFile(tempDir.resolve("a.yml"), "ref: \"!include b.yml\"\n");
            writeFile(tempDir.resolve("b.yml"), "ref: \"!include a.yml\"\n");

            YmlIncludeResolver resolver = YmlIncludeResolver.builder()
                    .basePath(tempDir)
                    .build();

            assertThatThrownBy(() -> resolver.resolve(tempDir.resolve("a.yml")))
                    .isInstanceOf(OpenYmlException.class)
                    .hasMessageContaining("Circular include detected");
        }

        @Test
        @DisplayName("should enforce max depth")
        void shouldEnforceMaxDepth() throws IOException {
            // Create a chain of 5 files that include each other
            for (int i = 0; i < 5; i++) {
                writeFile(tempDir.resolve("level" + i + ".yml"),
                        "data: \"!include level" + (i + 1) + ".yml\"\n");
            }
            writeFile(tempDir.resolve("level5.yml"), "leaf: true\n");

            YmlIncludeResolver resolver = YmlIncludeResolver.builder()
                    .basePath(tempDir)
                    .maxDepth(2)
                    .build();

            assertThatThrownBy(() -> resolver.resolve(tempDir.resolve("level0.yml")))
                    .isInstanceOf(OpenYmlException.class)
                    .hasMessageContaining("Include depth exceeded");
        }

        @Test
        @DisplayName("should enforce extension whitelist")
        void shouldEnforceExtensionWhitelist() throws IOException {
            writeFile(tempDir.resolve("secret.txt"), "password: secret\n");
            writeFile(tempDir.resolve("app.yml"), "config: \"!include secret.txt\"\n");

            YmlIncludeResolver resolver = YmlIncludeResolver.builder()
                    .basePath(tempDir)
                    .build();

            assertThatThrownBy(() -> resolver.resolve(tempDir.resolve("app.yml")))
                    .isInstanceOf(OpenYmlException.class)
                    .hasMessageContaining("File extension not allowed");
        }

        @Test
        @DisplayName("should allow custom extensions")
        void shouldAllowCustomExtensions() throws IOException {
            writeFile(tempDir.resolve("data.conf"), "key: value\n");
            writeFile(tempDir.resolve("app.conf"), "config: \"!include data.conf\"\n");

            YmlIncludeResolver resolver = YmlIncludeResolver.builder()
                    .basePath(tempDir)
                    .allowedExtensions(Set.of(".conf"))
                    .build();

            Map<String, Object> result = resolver.resolve(tempDir.resolve("app.conf"));

            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) result.get("config");
            assertThat(config).containsEntry("key", "value");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCases {

        @Test
        @DisplayName("should throw on missing included file")
        void shouldThrowOnMissingFile() throws IOException {
            writeFile(tempDir.resolve("app.yml"), "config: \"!include nonexistent.yml\"\n");

            YmlIncludeResolver resolver = YmlIncludeResolver.builder()
                    .basePath(tempDir)
                    .build();

            assertThatThrownBy(() -> resolver.resolve(tempDir.resolve("app.yml")))
                    .isInstanceOf(OpenYmlException.class)
                    .hasMessageContaining("Included file not found");
        }

        @Test
        @DisplayName("should handle empty include file")
        void shouldHandleEmptyIncludeFile() throws IOException {
            writeFile(tempDir.resolve("empty.yml"), "");
            writeFile(tempDir.resolve("app.yml"), "config: \"!include empty.yml\"\n");

            YmlIncludeResolver resolver = YmlIncludeResolver.builder()
                    .basePath(tempDir)
                    .build();

            Map<String, Object> result = resolver.resolve(tempDir.resolve("app.yml"));

            // Empty YAML file yields empty map for include
            assertThat(result).containsKey("config");
        }

        @Test
        @DisplayName("should preserve non-include string values")
        void shouldPreserveNonIncludeValues() throws IOException {
            writeFile(tempDir.resolve("app.yml"), "name: hello world\nport: 8080\n");

            Map<String, Object> result = YmlIncludeResolver.load(tempDir.resolve("app.yml"));

            assertThat(result).containsEntry("name", "hello world");
            assertThat(result).containsEntry("port", 8080);
        }

        @Test
        @DisplayName("should throw on null file")
        void shouldThrowOnNullFile() {
            assertThatThrownBy(() -> YmlIncludeResolver.load(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder should reject non-positive maxDepth")
        void builderShouldRejectNonPositiveMaxDepth() {
            assertThatThrownBy(() -> YmlIncludeResolver.builder().maxDepth(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxDepth must be positive");

            assertThatThrownBy(() -> YmlIncludeResolver.builder().maxDepth(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("builder should reject empty extensions")
        void builderShouldRejectEmptyExtensions() {
            assertThatThrownBy(() -> YmlIncludeResolver.builder().allowedExtensions(Set.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("extensions must not be empty");
        }

        @Test
        @DisplayName("builder should reject null extensions")
        void builderShouldRejectNullExtensions() {
            assertThatThrownBy(() -> YmlIncludeResolver.builder().allowedExtensions(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
