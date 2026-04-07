package cloud.opencode.base.classloader.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link IndexAwareScanner}.
 */
class IndexAwareScannerTest {

    private ClassIndex index;

    @BeforeEach
    void setUp() {
        List<ClassIndexEntry> entries = List.of(
                new ClassIndexEntry("com.example.service.UserService", "java.lang.Object",
                        List.of("com.example.api.Service"), List.of("java.lang.Deprecated"),
                        1, false, false, false, false, false),
                new ClassIndexEntry("com.example.service.OrderService", null,
                        List.of(), List.of(),
                        1, false, false, false, false, false),
                new ClassIndexEntry("com.example.model.User", null,
                        List.of("java.io.Serializable"), List.of(),
                        1, false, false, false, true, false),
                new ClassIndexEntry("com.example.api.Service", null,
                        List.of(), List.of(),
                        1537, true, true, false, false, false),
                new ClassIndexEntry("com.other.Unrelated", null,
                        List.of(), List.of(),
                        1, false, false, false, false, false)
        );
        index = new ClassIndex(1, "ts", "hash", entries);
    }

    @Nested
    @DisplayName("scan(index, basePackage) | 按包扫描")
    class ScanByPackageTests {

        @Test
        @DisplayName("should return classes under base package")
        void shouldReturnClassesUnderBasePackage() {
            Set<String> result = IndexAwareScanner.scan(index, "com.example.service");

            assertThat(result).containsExactlyInAnyOrder(
                    "com.example.service.UserService",
                    "com.example.service.OrderService"
            );
        }

        @Test
        @DisplayName("should return classes from multiple sub-packages")
        void shouldReturnClassesFromMultipleSubPackages() {
            Set<String> result = IndexAwareScanner.scan(index, "com.example");

            assertThat(result).containsExactlyInAnyOrder(
                    "com.example.service.UserService",
                    "com.example.service.OrderService",
                    "com.example.model.User",
                    "com.example.api.Service"
            );
        }

        @Test
        @DisplayName("should return empty set for non-existent package")
        void shouldReturnEmptyForNonExistentPackage() {
            Set<String> result = IndexAwareScanner.scan(index, "com.nonexistent");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should not include classes from unrelated packages")
        void shouldNotIncludeUnrelated() {
            Set<String> result = IndexAwareScanner.scan(index, "com.example");
            assertThat(result).doesNotContain("com.other.Unrelated");
        }

        @Test
        @DisplayName("should not match partial package names")
        void shouldNotMatchPartialPackageNames() {
            // "com.exam" should not match "com.example.service.UserService"
            Set<String> result = IndexAwareScanner.scan(index, "com.exam");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("scan(index, basePackage, filter) | 按包和过滤器扫描")
    class ScanWithFilterTests {

        @Test
        @DisplayName("should filter by interface flag")
        void shouldFilterByInterfaceFlag() {
            Set<String> result = IndexAwareScanner.scan(index, "com.example",
                    ClassIndexEntry::isInterface);

            assertThat(result).containsExactly("com.example.api.Service");
        }

        @Test
        @DisplayName("should filter by record flag")
        void shouldFilterByRecordFlag() {
            Set<String> result = IndexAwareScanner.scan(index, "com.example",
                    ClassIndexEntry::isRecord);

            assertThat(result).containsExactly("com.example.model.User");
        }

        @Test
        @DisplayName("should filter by annotation presence")
        void shouldFilterByAnnotation() {
            Set<String> result = IndexAwareScanner.scan(index, "com.example",
                    entry -> entry.annotationNames().contains("java.lang.Deprecated"));

            assertThat(result).containsExactly("com.example.service.UserService");
        }

        @Test
        @DisplayName("should filter non-interface classes")
        void shouldFilterNonInterfaces() {
            Set<String> result = IndexAwareScanner.scan(index, "com.example",
                    entry -> !entry.isInterface());

            assertThat(result).containsExactlyInAnyOrder(
                    "com.example.service.UserService",
                    "com.example.service.OrderService",
                    "com.example.model.User"
            );
        }

        @Test
        @DisplayName("should return empty when filter matches nothing")
        void shouldReturnEmptyWhenFilterMatchesNothing() {
            Set<String> result = IndexAwareScanner.scan(index, "com.example",
                    ClassIndexEntry::isEnum);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("null safety | 空值安全")
    class NullSafetyTests {

        @Test
        @DisplayName("should reject null index")
        void shouldRejectNullIndex() {
            assertThatNullPointerException()
                    .isThrownBy(() -> IndexAwareScanner.scan(null, "com.example"));
        }

        @Test
        @DisplayName("should reject null basePackage")
        void shouldRejectNullBasePackage() {
            assertThatNullPointerException()
                    .isThrownBy(() -> IndexAwareScanner.scan(index, null));
        }

        @Test
        @DisplayName("should reject null filter")
        void shouldRejectNullFilter() {
            assertThatNullPointerException()
                    .isThrownBy(() -> IndexAwareScanner.scan(index, "com.example", null));
        }
    }

    @Nested
    @DisplayName("result immutability | 结果不可变性")
    class ImmutabilityTests {

        @Test
        @DisplayName("should return unmodifiable set")
        void shouldReturnUnmodifiableSet() {
            Set<String> result = IndexAwareScanner.scan(index, "com.example");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> result.add("com.example.Fake"));
        }
    }
}
