package cloud.opencode.base.classloader.index;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ClassIndexReader}.
 */
class ClassIndexReaderTest {

    @AfterEach
    void tearDown() {
        ClassIndexReader.invalidateCache();
    }

    @Nested
    @DisplayName("load | 加载")
    class LoadTests {

        @Test
        @DisplayName("should return empty when index not on classpath")
        void shouldReturnEmptyWhenNotPresent() {
            ClassIndexReader.invalidateCache();
            Optional<ClassIndex> index = ClassIndexReader.load();
            // The index file is not on the test classpath by default
            assertThat(index).isEmpty();
        }

        @Test
        @DisplayName("should return empty for classloader without index")
        void shouldReturnEmptyForClassloaderWithoutIndex() {
            ClassIndexReader.invalidateCache();
            // A fresh class loader that has no resources
            ClassLoader emptyLoader = new ClassLoader(null) {};
            Optional<ClassIndex> index = ClassIndexReader.load(emptyLoader);
            assertThat(index).isEmpty();
        }

        @Test
        @DisplayName("should reject null classloader")
        void shouldRejectNullClassloader() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassIndexReader.load(null));
        }
    }

    @Nested
    @DisplayName("isValid | 验证")
    class IsValidTests {

        @Test
        @DisplayName("should reject null index")
        void shouldRejectNullIndex() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassIndexReader.isValid(null));
        }

        @Test
        @DisplayName("should return true when classpath hash matches")
        void shouldReturnTrueWhenHashMatches() {
            String currentHash = ClassIndexWriter.computeClasspathHash();
            ClassIndex index = new ClassIndex(1, "ts", currentHash, List.of());
            assertThat(ClassIndexReader.isValid(index)).isTrue();
        }

        @Test
        @DisplayName("should return false when classpath hash does not match")
        void shouldReturnFalseWhenHashMismatches() {
            ClassIndex index = new ClassIndex(1, "ts", "invalid-hash", List.of());
            assertThat(ClassIndexReader.isValid(index)).isFalse();
        }
    }

    @Nested
    @DisplayName("invalidateCache | 缓存失效")
    class InvalidateCacheTests {

        @Test
        @DisplayName("should allow reload after clearing cache")
        void shouldAllowReloadAfterClear() {
            ClassIndexReader.invalidateCache();
            // After clear, load should re-attempt reading from classpath
            Optional<ClassIndex> first = ClassIndexReader.load();
            ClassIndexReader.invalidateCache();
            Optional<ClassIndex> second = ClassIndexReader.load();
            assertThat(first).isEqualTo(second);
        }
    }

    @Nested
    @DisplayName("fromJson | JSON 解析")
    class FromJsonTests {

        @Test
        @DisplayName("should parse full index JSON")
        void shouldParseFullIndexJson() {
            String json = """
                    {
                      "version": 1,
                      "timestamp": "2026-01-01T00:00:00Z",
                      "classpathHash": "abc123def456",
                      "entries": [
                        {
                          "className": "com.example.MyClass",
                          "superClassName": "com.example.Base",
                          "interfaceNames": ["java.io.Serializable", "java.lang.Cloneable"],
                          "annotationNames": ["java.lang.Deprecated"],
                          "modifiers": 1,
                          "isInterface": false,
                          "isAbstract": false,
                          "isEnum": false,
                          "isRecord": false,
                          "isSealed": false
                        }
                      ]
                    }
                    """;

            ClassIndex index = ClassIndexReader.fromJson(json);

            assertThat(index.version()).isEqualTo(1);
            assertThat(index.timestamp()).isEqualTo("2026-01-01T00:00:00Z");
            assertThat(index.classpathHash()).isEqualTo("abc123def456");
            assertThat(index.entries()).hasSize(1);

            ClassIndexEntry entry = index.entries().getFirst();
            assertThat(entry.className()).isEqualTo("com.example.MyClass");
            assertThat(entry.superClassName()).isEqualTo("com.example.Base");
            assertThat(entry.interfaceNames()).containsExactly("java.io.Serializable", "java.lang.Cloneable");
            assertThat(entry.annotationNames()).containsExactly("java.lang.Deprecated");
            assertThat(entry.modifiers()).isEqualTo(1);
            assertThat(entry.isInterface()).isFalse();
        }

        @Test
        @DisplayName("should parse null superClassName")
        void shouldParseNullSuperClassName() {
            String json = """
                    {
                      "version": 1,
                      "timestamp": "ts",
                      "classpathHash": "hash",
                      "entries": [
                        {
                          "className": "com.example.A",
                          "superClassName": null,
                          "interfaceNames": [],
                          "annotationNames": [],
                          "modifiers": 0,
                          "isInterface": true,
                          "isAbstract": true,
                          "isEnum": false,
                          "isRecord": false,
                          "isSealed": false
                        }
                      ]
                    }
                    """;

            ClassIndex index = ClassIndexReader.fromJson(json);
            ClassIndexEntry entry = index.entries().getFirst();
            assertThat(entry.superClassName()).isNull();
            assertThat(entry.isInterface()).isTrue();
            assertThat(entry.isAbstract()).isTrue();
        }

        @Test
        @DisplayName("should parse empty entries array")
        void shouldParseEmptyEntries() {
            String json = """
                    {
                      "version": 1,
                      "timestamp": "ts",
                      "classpathHash": "hash",
                      "entries": []
                    }
                    """;

            ClassIndex index = ClassIndexReader.fromJson(json);
            assertThat(index.entries()).isEmpty();
        }

        @Test
        @DisplayName("should parse multiple entries")
        void shouldParseMultipleEntries() {
            String json = """
                    {
                      "version": 1,
                      "timestamp": "ts",
                      "classpathHash": "hash",
                      "entries": [
                        {
                          "className": "com.example.A",
                          "superClassName": null,
                          "interfaceNames": [],
                          "annotationNames": [],
                          "modifiers": 0,
                          "isInterface": false,
                          "isAbstract": false,
                          "isEnum": false,
                          "isRecord": false,
                          "isSealed": false
                        },
                        {
                          "className": "com.example.B",
                          "superClassName": "com.example.A",
                          "interfaceNames": ["java.io.Serializable"],
                          "annotationNames": [],
                          "modifiers": 1,
                          "isInterface": false,
                          "isAbstract": false,
                          "isEnum": true,
                          "isRecord": false,
                          "isSealed": false
                        }
                      ]
                    }
                    """;

            ClassIndex index = ClassIndexReader.fromJson(json);
            assertThat(index.entries()).hasSize(2);
            assertThat(index.entries().get(0).className()).isEqualTo("com.example.A");
            assertThat(index.entries().get(1).className()).isEqualTo("com.example.B");
            assertThat(index.entries().get(1).superClassName()).isEqualTo("com.example.A");
            assertThat(index.entries().get(1).isEnum()).isTrue();
        }

        @Test
        @DisplayName("should handle escaped strings")
        void shouldHandleEscapedStrings() {
            String json = """
                    {
                      "version": 1,
                      "timestamp": "ts\\ntest",
                      "classpathHash": "hash",
                      "entries": []
                    }
                    """;

            ClassIndex index = ClassIndexReader.fromJson(json);
            assertThat(index.timestamp()).isEqualTo("ts\ntest");
        }

        @Test
        @DisplayName("should return version mismatch as detected by caller")
        void shouldReturnVersionForCallerCheck() {
            String json = """
                    {
                      "version": 999,
                      "timestamp": "ts",
                      "classpathHash": "hash",
                      "entries": []
                    }
                    """;

            ClassIndex index = ClassIndexReader.fromJson(json);
            assertThat(index.version()).isEqualTo(999);
        }
    }
}
