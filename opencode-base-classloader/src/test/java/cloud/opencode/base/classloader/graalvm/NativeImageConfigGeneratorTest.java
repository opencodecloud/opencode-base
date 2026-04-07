package cloud.opencode.base.classloader.graalvm;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for NativeImageConfigGenerator
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("NativeImageConfigGenerator Tests")
class NativeImageConfigGeneratorTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create builder")
        void shouldCreateBuilder() {
            NativeImageConfigGenerator.Builder builder = NativeImageConfigGenerator.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("Should reject null package name")
        void shouldRejectNullPackage() {
            assertThatNullPointerException()
                    .isThrownBy(() -> NativeImageConfigGenerator.builder().addPackage(null))
                    .withMessageContaining("Package name must not be null");
        }

        @Test
        @DisplayName("Should reject null resource pattern")
        void shouldRejectNullResourcePattern() {
            assertThatNullPointerException()
                    .isThrownBy(() -> NativeImageConfigGenerator.builder().addResourcePattern(null))
                    .withMessageContaining("Resource pattern must not be null");
        }

        @Test
        @DisplayName("Should reject null output directory")
        void shouldRejectNullOutputDir() {
            assertThatNullPointerException()
                    .isThrownBy(() -> NativeImageConfigGenerator.builder().outputDir(null))
                    .withMessageContaining("Output directory must not be null");
        }

        @Test
        @DisplayName("Should build generator without executing")
        void shouldBuildWithoutExecuting() {
            NativeImageConfigGenerator generator = NativeImageConfigGenerator.builder()
                    .addPackage("com.example")
                    .outputDir(tempDir)
                    .build();

            assertThat(generator).isNotNull();
        }
    }

    @Nested
    @DisplayName("Generation Tests")
    class GenerationTests {

        @Test
        @DisplayName("Should throw if output directory not set")
        void shouldThrowIfNoOutputDir() {
            // build() now validates eagerly — NullPointerException from Objects.requireNonNull
            assertThatNullPointerException()
                    .isThrownBy(() -> NativeImageConfigGenerator.builder()
                            .addPackage("com.example")
                            .build())
                    .withMessageContaining("Output directory must be set");
        }

        @Test
        @DisplayName("Should generate reflect-config.json and resource-config.json")
        void shouldGenerateConfigFiles() throws IOException {
            Path outputDir = tempDir.resolve("native-image");

            NativeImageConfigGenerator.builder()
                    .addPackage("cloud.opencode.base.classloader.graalvm")
                    .addResourcePattern("config/.*")
                    .outputDir(outputDir)
                    .generate();

            assertThat(outputDir.resolve("reflect-config.json")).exists();
            assertThat(outputDir.resolve("resource-config.json")).exists();
        }

        @Test
        @DisplayName("Should generate valid reflect-config.json format")
        void shouldGenerateValidReflectConfig() throws IOException {
            Path outputDir = tempDir.resolve("reflect-test");

            NativeImageConfigGenerator.builder()
                    .addPackage("cloud.opencode.base.classloader.graalvm")
                    .outputDir(outputDir)
                    .generate();

            String content = Files.readString(
                    outputDir.resolve("reflect-config.json"), StandardCharsets.UTF_8
            );

            // Should be a JSON array
            assertThat(content).startsWith("[");
            assertThat(content).endsWith("]");

            // Should contain known classes from this package
            assertThat(content).contains("\"name\":\"cloud.opencode.base.classloader.graalvm.NativeImageSupport\"");
            assertThat(content).contains("\"name\":\"cloud.opencode.base.classloader.graalvm.ReflectConfig\"");
            assertThat(content).contains("\"name\":\"cloud.opencode.base.classloader.graalvm.ResourceConfig\"");

            // Should contain reflection flags
            assertThat(content).contains("\"allDeclaredConstructors\":true");
            assertThat(content).contains("\"allDeclaredMethods\":true");
            assertThat(content).contains("\"allDeclaredFields\":true");
            assertThat(content).contains("\"allPublicMethods\":true");
        }

        @Test
        @DisplayName("Should generate valid resource-config.json format")
        void shouldGenerateValidResourceConfig() throws IOException {
            Path outputDir = tempDir.resolve("resource-test");

            NativeImageConfigGenerator.builder()
                    .addPackage("cloud.opencode.base.classloader.graalvm")
                    .addResourcePattern("config/.*")
                    .addResourcePattern("templates/.*")
                    .outputDir(outputDir)
                    .generate();

            String content = Files.readString(
                    outputDir.resolve("resource-config.json"), StandardCharsets.UTF_8
            );

            assertThat(content).isEqualTo(
                    "{\"resources\":{\"includes\":[" +
                            "{\"pattern\":\"config/.*\"}," +
                            "{\"pattern\":\"templates/.*\"}" +
                            "]}}"
            );
        }

        @Test
        @DisplayName("Should generate empty resource config when no patterns")
        void shouldGenerateEmptyResourceConfig() throws IOException {
            Path outputDir = tempDir.resolve("empty-resource-test");

            NativeImageConfigGenerator.builder()
                    .addPackage("cloud.opencode.base.classloader.graalvm")
                    .outputDir(outputDir)
                    .generate();

            String content = Files.readString(
                    outputDir.resolve("resource-config.json"), StandardCharsets.UTF_8
            );

            assertThat(content).isEqualTo("{\"resources\":{\"includes\":[]}}");
        }

        @Test
        @DisplayName("Should create output directory if not exists")
        void shouldCreateOutputDirectory() throws IOException {
            Path outputDir = tempDir.resolve("nested/deep/output");

            NativeImageConfigGenerator.builder()
                    .addPackage("cloud.opencode.base.classloader.graalvm")
                    .outputDir(outputDir)
                    .generate();

            assertThat(outputDir).isDirectory();
            assertThat(outputDir.resolve("reflect-config.json")).exists();
        }

        @Test
        @DisplayName("Should generate with custom classloader")
        void shouldGenerateWithCustomClassLoader() throws IOException {
            Path outputDir = tempDir.resolve("classloader-test");

            NativeImageConfigGenerator.builder()
                    .addPackage("cloud.opencode.base.classloader.graalvm")
                    .classLoader(getClass().getClassLoader())
                    .outputDir(outputDir)
                    .generate();

            assertThat(outputDir.resolve("reflect-config.json")).exists();
            String content = Files.readString(
                    outputDir.resolve("reflect-config.json"), StandardCharsets.UTF_8
            );
            assertThat(content).startsWith("[");
        }

        @Test
        @DisplayName("Should generate sorted class names in reflect-config.json")
        void shouldGenerateSortedClassNames() throws IOException {
            Path outputDir = tempDir.resolve("sorted-test");

            NativeImageConfigGenerator.builder()
                    .addPackage("cloud.opencode.base.classloader.graalvm")
                    .outputDir(outputDir)
                    .generate();

            String content = Files.readString(
                    outputDir.resolve("reflect-config.json"), StandardCharsets.UTF_8
            );

            // NativeImageConfigGenerator should come before NativeImageSupport alphabetically
            int generatorIdx = content.indexOf("NativeImageConfigGenerator");
            int supportIdx = content.indexOf("NativeImageSupport");
            if (generatorIdx >= 0 && supportIdx >= 0) {
                assertThat(generatorIdx).isLessThan(supportIdx);
            }
        }

        @Test
        @DisplayName("Should handle empty package scan gracefully")
        void shouldHandleEmptyPackageScan() throws IOException {
            Path outputDir = tempDir.resolve("empty-scan-test");

            NativeImageConfigGenerator.builder()
                    .addPackage("com.nonexistent.package.that.does.not.exist")
                    .outputDir(outputDir)
                    .generate();

            String content = Files.readString(
                    outputDir.resolve("reflect-config.json"), StandardCharsets.UTF_8
            );
            assertThat(content).isEqualTo("[]");
        }

        @Test
        @DisplayName("Builder generate() should be a shortcut for build().generate()")
        void shouldSupportBuilderGenerateShortcut() throws IOException {
            Path outputDir = tempDir.resolve("shortcut-test");

            NativeImageConfigGenerator.builder()
                    .addPackage("cloud.opencode.base.classloader.graalvm")
                    .outputDir(outputDir)
                    .generate();

            assertThat(outputDir.resolve("reflect-config.json")).exists();
            assertThat(outputDir.resolve("resource-config.json")).exists();
        }
    }
}
