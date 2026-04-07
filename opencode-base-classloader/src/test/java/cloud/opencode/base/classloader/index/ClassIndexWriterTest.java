package cloud.opencode.base.classloader.index;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ClassIndexWriter}.
 */
class ClassIndexWriterTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        ClassIndexReader.invalidateCache();
    }

    @Nested
    @DisplayName("generate | 生成")
    class GenerateTests {

        @Test
        @DisplayName("should generate index file in output directory")
        void shouldGenerateIndexFile() throws IOException {
            ClassIndex index = ClassIndexWriter.builder()
                    .addPackage("cloud.opencode.base.classloader.index")
                    .outputDir(tempDir)
                    .generate();

            Path indexFile = tempDir.resolve(ClassIndex.INDEX_LOCATION);
            assertThat(indexFile).exists();
            assertThat(index.entries()).isNotEmpty();
            assertThat(index.version()).isEqualTo(ClassIndex.CURRENT_VERSION);
            assertThat(index.timestamp()).isNotEmpty();
            assertThat(index.classpathHash()).isNotEmpty();
        }

        @Test
        @DisplayName("should generate valid JSON content")
        void shouldGenerateValidJsonContent() throws IOException {
            ClassIndexWriter.builder()
                    .addPackage("cloud.opencode.base.classloader.index")
                    .outputDir(tempDir)
                    .generate();

            Path indexFile = tempDir.resolve(ClassIndex.INDEX_LOCATION);
            String json = Files.readString(indexFile, StandardCharsets.UTF_8);

            assertThat(json).contains("\"version\"");
            assertThat(json).contains("\"timestamp\"");
            assertThat(json).contains("\"classpathHash\"");
            assertThat(json).contains("\"entries\"");
            assertThat(json).contains("\"className\"");
        }

        @Test
        @DisplayName("should include classes from scanned package")
        void shouldIncludeClassesFromPackage() throws IOException {
            ClassIndex index = ClassIndexWriter.builder()
                    .addPackage("cloud.opencode.base.classloader.index")
                    .outputDir(tempDir)
                    .generate();

            List<String> classNames = index.entries().stream()
                    .map(ClassIndexEntry::className)
                    .toList();

            assertThat(classNames).contains(
                    "cloud.opencode.base.classloader.index.ClassIndex",
                    "cloud.opencode.base.classloader.index.ClassIndexEntry",
                    "cloud.opencode.base.classloader.index.ClassIndexWriter",
                    "cloud.opencode.base.classloader.index.ClassIndexReader"
            );
        }

        @Test
        @DisplayName("should produce consistent classpathHash across invocations")
        void shouldProduceConsistentClasspathHash() throws IOException {
            ClassIndex first = ClassIndexWriter.builder()
                    .addPackage("cloud.opencode.base.classloader.index")
                    .outputDir(tempDir)
                    .generate();

            // Create second output dir
            Path tempDir2 = tempDir.resolve("second");
            Files.createDirectories(tempDir2);

            ClassIndex second = ClassIndexWriter.builder()
                    .addPackage("cloud.opencode.base.classloader.index")
                    .outputDir(tempDir2)
                    .generate();

            assertThat(first.classpathHash()).isEqualTo(second.classpathHash());
        }

        @Test
        @DisplayName("should correctly record type flags for records and interfaces")
        void shouldRecordTypeFlags() throws IOException {
            ClassIndex index = ClassIndexWriter.builder()
                    .addPackage("cloud.opencode.base.classloader.index")
                    .outputDir(tempDir)
                    .generate();

            // ClassIndexEntry is a record
            Optional<ClassIndexEntry> recordEntry = index.entries().stream()
                    .filter(e -> e.className().equals("cloud.opencode.base.classloader.index.ClassIndexEntry"))
                    .findFirst();

            assertThat(recordEntry).isPresent();
            assertThat(recordEntry.get().isRecord()).isTrue();
            assertThat(recordEntry.get().isInterface()).isFalse();

            // ClassIndex is also a record
            Optional<ClassIndexEntry> indexRecord = index.entries().stream()
                    .filter(e -> e.className().equals("cloud.opencode.base.classloader.index.ClassIndex"))
                    .findFirst();

            assertThat(indexRecord).isPresent();
            assertThat(indexRecord.get().isRecord()).isTrue();
        }

        @Test
        @DisplayName("should throw when no packages specified")
        void shouldThrowWhenNoPackages() {
            ClassIndexWriter writer = ClassIndexWriter.builder().outputDir(tempDir);

            assertThatIllegalStateException()
                    .isThrownBy(writer::generate)
                    .withMessageContaining("package");
        }

        @Test
        @DisplayName("should throw when no outputDir specified")
        void shouldThrowWhenNoOutputDir() {
            ClassIndexWriter writer = ClassIndexWriter.builder()
                    .addPackage("com.example");

            assertThatIllegalStateException()
                    .isThrownBy(writer::generate)
                    .withMessageContaining("Output directory");
        }

        @Test
        @DisplayName("ClassIndexReader should be able to read writer-generated index")
        void readerShouldReadWriterOutput() throws IOException {
            ClassIndex written = ClassIndexWriter.builder()
                    .addPackage("cloud.opencode.base.classloader.index")
                    .outputDir(tempDir)
                    .generate();

            // Read the JSON and parse it
            Path indexFile = tempDir.resolve(ClassIndex.INDEX_LOCATION);
            String json = Files.readString(indexFile, StandardCharsets.UTF_8);
            ClassIndex read = ClassIndexReader.fromJson(json);

            assertThat(read.version()).isEqualTo(written.version());
            assertThat(read.classpathHash()).isEqualTo(written.classpathHash());
            assertThat(read.entries()).hasSameSizeAs(written.entries());

            for (int i = 0; i < written.entries().size(); i++) {
                ClassIndexEntry we = written.entries().get(i);
                ClassIndexEntry re = read.entries().get(i);
                assertThat(re.className()).isEqualTo(we.className());
                assertThat(re.superClassName()).isEqualTo(we.superClassName());
                assertThat(re.interfaceNames()).isEqualTo(we.interfaceNames());
                assertThat(re.annotationNames()).isEqualTo(we.annotationNames());
                assertThat(re.modifiers()).isEqualTo(we.modifiers());
                assertThat(re.isInterface()).isEqualTo(we.isInterface());
                assertThat(re.isAbstract()).isEqualTo(we.isAbstract());
                assertThat(re.isEnum()).isEqualTo(we.isEnum());
                assertThat(re.isRecord()).isEqualTo(we.isRecord());
                assertThat(re.isSealed()).isEqualTo(we.isSealed());
            }
        }
    }

    @Nested
    @DisplayName("toJson | JSON 序列化")
    class ToJsonTests {

        @Test
        @DisplayName("should produce valid JSON for empty entries")
        void shouldProduceValidJsonForEmptyEntries() {
            ClassIndex index = new ClassIndex(1, "2026-01-01T00:00:00Z", "abc", List.of());
            String json = ClassIndexWriter.toJson(index);

            assertThat(json).contains("\"version\": 1");
            assertThat(json).contains("\"entries\": []");
        }

        @Test
        @DisplayName("should handle null superClassName")
        void shouldHandleNullSuperClassName() {
            ClassIndexEntry entry = new ClassIndexEntry(
                    "com.example.A", null,
                    List.of(), List.of(),
                    0, false, false, false, false, false
            );
            ClassIndex index = new ClassIndex(1, "ts", "hash", List.of(entry));
            String json = ClassIndexWriter.toJson(index);

            assertThat(json).contains("\"superClassName\": null");
        }

        @Test
        @DisplayName("should handle special characters in strings")
        void shouldHandleSpecialCharacters() {
            ClassIndexEntry entry = new ClassIndexEntry(
                    "com.example.A\"B", null,
                    List.of(), List.of(),
                    0, false, false, false, false, false
            );
            ClassIndex index = new ClassIndex(1, "ts", "hash", List.of(entry));
            String json = ClassIndexWriter.toJson(index);

            assertThat(json).contains("com.example.A\\\"B");
        }
    }

    @Nested
    @DisplayName("escapeJson | JSON 转义")
    class EscapeJsonTests {

        @Test
        @DisplayName("should escape special characters")
        void shouldEscapeSpecialCharacters() {
            assertThat(ClassIndexWriter.escapeJson("a\"b")).isEqualTo("a\\\"b");
            assertThat(ClassIndexWriter.escapeJson("a\\b")).isEqualTo("a\\\\b");
            assertThat(ClassIndexWriter.escapeJson("a\nb")).isEqualTo("a\\nb");
            assertThat(ClassIndexWriter.escapeJson("a\rb")).isEqualTo("a\\rb");
            assertThat(ClassIndexWriter.escapeJson("a\tb")).isEqualTo("a\\tb");
        }

        @Test
        @DisplayName("should return empty string for null")
        void shouldReturnEmptyForNull() {
            assertThat(ClassIndexWriter.escapeJson(null)).isEmpty();
        }

        @Test
        @DisplayName("should not modify plain strings")
        void shouldNotModifyPlainStrings() {
            assertThat(ClassIndexWriter.escapeJson("hello.world")).isEqualTo("hello.world");
        }

        @Test
        @DisplayName("should escape control characters as unicode")
        void shouldEscapeControlChars() {
            assertThat(ClassIndexWriter.escapeJson("\u0001")).isEqualTo("\\u0001");
        }
    }

    @Nested
    @DisplayName("computeClasspathHash | 计算 classpath 哈希")
    class ClasspathHashTests {

        @Test
        @DisplayName("should produce deterministic hash")
        void shouldProduceDeterministicHash() {
            String hash1 = ClassIndexWriter.computeClasspathHash();
            String hash2 = ClassIndexWriter.computeClasspathHash();
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("should produce non-empty hash")
        void shouldProduceNonEmptyHash() {
            String hash = ClassIndexWriter.computeClasspathHash();
            assertThat(hash).isNotEmpty();
        }

        @Test
        @DisplayName("should produce hex string")
        void shouldProduceHexString() {
            String hash = ClassIndexWriter.computeClasspathHash();
            if (!hash.isEmpty()) {
                assertThat(hash).matches("[0-9a-f]+");
                // SHA-256 produces 64 hex chars
                assertThat(hash).hasSize(64);
            }
        }
    }
}
