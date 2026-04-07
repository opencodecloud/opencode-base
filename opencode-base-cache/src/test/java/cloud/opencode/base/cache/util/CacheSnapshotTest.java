package cloud.opencode.base.cache.util;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheManager;
import cloud.opencode.base.cache.OpenCache;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * CacheSnapshot tests
 * CacheSnapshot 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.3
 */
class CacheSnapshotTest {

    private Path tempDir;

    @BeforeEach
    void setup() throws IOException {
        CacheManager.getInstance().reset();
        tempDir = Files.createTempDirectory("cache-snapshot-test");
    }

    @AfterEach
    void cleanup() throws IOException {
        // Clean up temp files
        try (var walk = Files.walk(tempDir)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    // ==================== saveStringCache / restoreStringCache ====================

    @Nested
    @DisplayName("String cache roundtrip")
    class StringCacheRoundtripTests {

        @Test
        @DisplayName("should save and restore string cache entries")
        void shouldSaveAndRestoreStringCache() throws IOException {
            Cache<String, String> cache = OpenCache.getOrCreate("snap-str");
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.put("key3", "value3");

            Path file = tempDir.resolve("string.snapshot");
            CacheSnapshot.saveStringCache(cache, file);

            // Restore to a fresh cache
            Cache<String, String> restored = OpenCache.getOrCreate("snap-str-restored");
            int count = CacheSnapshot.restoreStringCache(file, restored);

            assertThat(count).isEqualTo(3);
            assertThat(restored.get("key1")).isEqualTo("value1");
            assertThat(restored.get("key2")).isEqualTo("value2");
            assertThat(restored.get("key3")).isEqualTo("value3");
        }

        @Test
        @DisplayName("should handle special characters in keys and values")
        void shouldHandleSpecialCharacters() throws IOException {
            Cache<String, String> cache = OpenCache.getOrCreate("snap-special");
            cache.put("key with spaces", "value\twith\ttabs");
            cache.put("key\nwith\nnewlines", "value=with=equals");
            cache.put("unicode:你好", "世界:world");

            Path file = tempDir.resolve("special.snapshot");
            CacheSnapshot.saveStringCache(cache, file);

            Cache<String, String> restored = OpenCache.getOrCreate("snap-special-restored");
            int count = CacheSnapshot.restoreStringCache(file, restored);

            assertThat(count).isEqualTo(3);
            assertThat(restored.get("key with spaces")).isEqualTo("value\twith\ttabs");
            assertThat(restored.get("key\nwith\nnewlines")).isEqualTo("value=with=equals");
            assertThat(restored.get("unicode:你好")).isEqualTo("世界:world");
        }
    }

    // ==================== save / restore with custom serializers ====================

    @Nested
    @DisplayName("Custom serializer roundtrip")
    class CustomSerializerRoundtripTests {

        @Test
        @DisplayName("should save and restore with custom serializers")
        void shouldSaveAndRestoreWithCustomSerializers() throws IOException {
            Cache<String, Integer> cache = OpenCache.getOrCreate("snap-int");
            cache.put("a", 100);
            cache.put("b", 200);

            Path file = tempDir.resolve("custom.snapshot");
            CacheSnapshot.save(cache, file, Function.identity(), Object::toString);

            Cache<String, Integer> restored = OpenCache.getOrCreate("snap-int-restored");
            int count = CacheSnapshot.restore(file, restored, Function.identity(), Integer::valueOf);

            assertThat(count).isEqualTo(2);
            assertThat(restored.get("a")).isEqualTo(100);
            assertThat(restored.get("b")).isEqualTo(200);
        }
    }

    // ==================== Empty cache ====================

    @Nested
    @DisplayName("Empty cache")
    class EmptyCacheTests {

        @Test
        @DisplayName("should save empty cache without errors")
        void shouldSaveEmptyCache() throws IOException {
            Cache<String, String> cache = OpenCache.getOrCreate("snap-empty");
            Path file = tempDir.resolve("empty.snapshot");

            CacheSnapshot.saveStringCache(cache, file);

            assertThat(Files.exists(file)).isTrue();
            // File should contain only header lines
            long lineCount = Files.lines(file).count();
            assertThat(lineCount).isEqualTo(3); // 3 header lines
        }

        @Test
        @DisplayName("should restore from empty snapshot returning zero")
        void shouldRestoreFromEmptySnapshot() throws IOException {
            Cache<String, String> cache = OpenCache.getOrCreate("snap-empty-src");
            Path file = tempDir.resolve("empty-restore.snapshot");
            CacheSnapshot.saveStringCache(cache, file);

            Cache<String, String> restored = OpenCache.getOrCreate("snap-empty-dst");
            int count = CacheSnapshot.restoreStringCache(file, restored);

            assertThat(count).isZero();
            assertThat(restored.estimatedSize()).isZero();
        }
    }

    // ==================== Error handling ====================

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should throw IOException for nonexistent file on restore")
        void shouldThrowForNonexistentFile() {
            Cache<String, String> cache = OpenCache.getOrCreate("snap-err");
            Path missing = tempDir.resolve("nonexistent.snapshot");

            assertThatThrownBy(() -> CacheSnapshot.restoreStringCache(missing, cache))
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException for null cache on save")
        void shouldThrowForNullCacheOnSave() {
            Path file = tempDir.resolve("null.snapshot");

            assertThatThrownBy(() -> CacheSnapshot.saveStringCache(null, file))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException for null path on save")
        void shouldThrowForNullPathOnSave() {
            Cache<String, String> cache = OpenCache.getOrCreate("snap-null-path");

            assertThatThrownBy(() -> CacheSnapshot.saveStringCache(cache, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should skip malformed lines during restore")
        void shouldSkipMalformedLines() throws IOException {
            Path file = tempDir.resolve("malformed.snapshot");
            Files.writeString(file,
                    "# OpenCache Snapshot v1\n" +
                    "# Created: 2026-04-03T10:00:00Z\n" +
                    "# Entries: 1\n" +
                    "bad-line-no-tabs\n" +
                    java.util.Base64.getEncoder().encodeToString("key1".getBytes()) + "\t" +
                    java.util.Base64.getEncoder().encodeToString("val1".getBytes()) + "\t-1\n"
            );

            Cache<String, String> cache = OpenCache.getOrCreate("snap-malformed");
            int count = CacheSnapshot.restoreStringCache(file, cache);

            assertThat(count).isEqualTo(1);
            assertThat(cache.get("key1")).isEqualTo("val1");
        }
    }

    // ==================== File format ====================

    @Nested
    @DisplayName("File format")
    class FileFormatTests {

        @Test
        @DisplayName("snapshot file should contain proper header")
        void shouldContainProperHeader() throws IOException {
            Cache<String, String> cache = OpenCache.getOrCreate("snap-header");
            cache.put("k", "v");

            Path file = tempDir.resolve("header.snapshot");
            CacheSnapshot.saveStringCache(cache, file);

            var lines = Files.readAllLines(file);
            assertThat(lines.get(0)).startsWith("# OpenCache Snapshot v1");
            assertThat(lines.get(1)).startsWith("# Created:");
            assertThat(lines.get(2)).startsWith("# Entries:");
            // Data line
            assertThat(lines.get(3)).contains("\t");
        }
    }
}
