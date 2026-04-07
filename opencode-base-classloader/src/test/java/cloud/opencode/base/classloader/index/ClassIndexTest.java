package cloud.opencode.base.classloader.index;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ClassIndex}.
 */
class ClassIndexTest {

    private static ClassIndexEntry sampleEntry(String className) {
        return new ClassIndexEntry(
                className, null, List.of(), List.of(),
                1, false, false, false, false, false
        );
    }

    @Nested
    @DisplayName("Constructor | 构造方法")
    class ConstructorTests {

        @Test
        @DisplayName("should create index with all fields")
        void shouldCreateIndexWithAllFields() {
            ClassIndexEntry entry = sampleEntry("com.example.A");
            ClassIndex index = new ClassIndex(1, "2026-01-01T00:00:00Z", "abc123", List.of(entry));

            assertThat(index.version()).isEqualTo(1);
            assertThat(index.timestamp()).isEqualTo("2026-01-01T00:00:00Z");
            assertThat(index.classpathHash()).isEqualTo("abc123");
            assertThat(index.entries()).hasSize(1);
            assertThat(index.entries().getFirst().className()).isEqualTo("com.example.A");
        }

        @Test
        @DisplayName("should reject null timestamp")
        void shouldRejectNullTimestamp() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ClassIndex(1, null, "hash", List.of()));
        }

        @Test
        @DisplayName("should reject null classpathHash")
        void shouldRejectNullClasspathHash() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ClassIndex(1, "ts", null, List.of()));
        }

        @Test
        @DisplayName("should default null entries to empty list")
        void shouldDefaultNullEntries() {
            ClassIndex index = new ClassIndex(1, "ts", "hash", null);
            assertThat(index.entries()).isEmpty();
        }

        @Test
        @DisplayName("should make defensive copy of entries")
        void shouldMakeDefensiveCopy() {
            List<ClassIndexEntry> entries = new ArrayList<>();
            entries.add(sampleEntry("com.example.A"));

            ClassIndex index = new ClassIndex(1, "ts", "hash", entries);

            entries.add(sampleEntry("com.example.B"));
            assertThat(index.entries()).hasSize(1);
        }

        @Test
        @DisplayName("should return unmodifiable entries")
        void shouldReturnUnmodifiableEntries() {
            ClassIndex index = new ClassIndex(1, "ts", "hash",
                    List.of(sampleEntry("com.example.A")));

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> index.entries().add(sampleEntry("com.example.B")));
        }
    }

    @Nested
    @DisplayName("Constants | 常量")
    class ConstantsTests {

        @Test
        @DisplayName("CURRENT_VERSION should be 1")
        void currentVersionShouldBe1() {
            assertThat(ClassIndex.CURRENT_VERSION).isEqualTo(1);
        }

        @Test
        @DisplayName("INDEX_LOCATION should be correct path")
        void indexLocationShouldBeCorrectPath() {
            assertThat(ClassIndex.INDEX_LOCATION).isEqualTo("META-INF/opencode/class-index.json");
        }
    }

    @Nested
    @DisplayName("equals and hashCode | 相等性与哈希码")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal when all fields match")
        void shouldBeEqualWhenFieldsMatch() {
            ClassIndex a = new ClassIndex(1, "ts", "hash", List.of());
            ClassIndex b = new ClassIndex(1, "ts", "hash", List.of());
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("should not be equal when version differs")
        void shouldNotBeEqualWhenVersionDiffers() {
            ClassIndex a = new ClassIndex(1, "ts", "hash", List.of());
            ClassIndex b = new ClassIndex(2, "ts", "hash", List.of());
            assertThat(a).isNotEqualTo(b);
        }
    }
}
