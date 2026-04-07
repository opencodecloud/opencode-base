package cloud.opencode.base.classloader.index;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ClassIndexEntry}.
 */
class ClassIndexEntryTest {

    @Nested
    @DisplayName("Constructor | 构造方法")
    class ConstructorTests {

        @Test
        @DisplayName("should create entry with all fields")
        void shouldCreateEntryWithAllFields() {
            ClassIndexEntry entry = new ClassIndexEntry(
                    "com.example.MyClass",
                    "com.example.BaseClass",
                    List.of("java.io.Serializable"),
                    List.of("java.lang.Deprecated"),
                    1,
                    false, false, false, false, false
            );

            assertThat(entry.className()).isEqualTo("com.example.MyClass");
            assertThat(entry.superClassName()).isEqualTo("com.example.BaseClass");
            assertThat(entry.interfaceNames()).containsExactly("java.io.Serializable");
            assertThat(entry.annotationNames()).containsExactly("java.lang.Deprecated");
            assertThat(entry.modifiers()).isEqualTo(1);
            assertThat(entry.isInterface()).isFalse();
            assertThat(entry.isAbstract()).isFalse();
            assertThat(entry.isEnum()).isFalse();
            assertThat(entry.isRecord()).isFalse();
            assertThat(entry.isSealed()).isFalse();
        }

        @Test
        @DisplayName("should reject null className")
        void shouldRejectNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ClassIndexEntry(
                            null, null, List.of(), List.of(),
                            0, false, false, false, false, false
                    ));
        }

        @Test
        @DisplayName("should allow null superClassName")
        void shouldAllowNullSuperClassName() {
            ClassIndexEntry entry = new ClassIndexEntry(
                    "com.example.MyClass", null,
                    List.of(), List.of(),
                    0, false, false, false, false, false
            );
            assertThat(entry.superClassName()).isNull();
        }

        @Test
        @DisplayName("should default null interfaceNames to empty list")
        void shouldDefaultNullInterfaceNames() {
            ClassIndexEntry entry = new ClassIndexEntry(
                    "com.example.MyClass", null,
                    null, null,
                    0, false, false, false, false, false
            );
            assertThat(entry.interfaceNames()).isEmpty();
            assertThat(entry.annotationNames()).isEmpty();
        }

        @Test
        @DisplayName("should make defensive copies of lists")
        void shouldMakeDefensiveCopies() {
            List<String> interfaces = new ArrayList<>(List.of("java.io.Serializable"));
            List<String> annotations = new ArrayList<>(List.of("java.lang.Deprecated"));

            ClassIndexEntry entry = new ClassIndexEntry(
                    "com.example.MyClass", null,
                    interfaces, annotations,
                    0, false, false, false, false, false
            );

            interfaces.add("java.lang.Cloneable");
            annotations.add("java.lang.Override");

            assertThat(entry.interfaceNames()).hasSize(1);
            assertThat(entry.annotationNames()).hasSize(1);
        }

        @Test
        @DisplayName("should return unmodifiable interfaceNames")
        void shouldReturnUnmodifiableInterfaceNames() {
            ClassIndexEntry entry = new ClassIndexEntry(
                    "com.example.MyClass", null,
                    List.of("java.io.Serializable"), List.of(),
                    0, false, false, false, false, false
            );
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> entry.interfaceNames().add("x"));
        }

        @Test
        @DisplayName("should return unmodifiable annotationNames")
        void shouldReturnUnmodifiableAnnotationNames() {
            ClassIndexEntry entry = new ClassIndexEntry(
                    "com.example.MyClass", null,
                    List.of(), List.of("java.lang.Deprecated"),
                    0, false, false, false, false, false
            );
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> entry.annotationNames().add("x"));
        }

        @Test
        @DisplayName("should correctly report type flags")
        void shouldCorrectlyReportTypeFlags() {
            ClassIndexEntry iface = new ClassIndexEntry(
                    "com.example.MyIface", null,
                    List.of(), List.of(),
                    0, true, true, false, false, false
            );
            assertThat(iface.isInterface()).isTrue();
            assertThat(iface.isAbstract()).isTrue();

            ClassIndexEntry enumEntry = new ClassIndexEntry(
                    "com.example.MyEnum", null,
                    List.of(), List.of(),
                    0, false, false, true, false, false
            );
            assertThat(enumEntry.isEnum()).isTrue();

            ClassIndexEntry recordEntry = new ClassIndexEntry(
                    "com.example.MyRecord", null,
                    List.of(), List.of(),
                    0, false, false, false, true, false
            );
            assertThat(recordEntry.isRecord()).isTrue();

            ClassIndexEntry sealedEntry = new ClassIndexEntry(
                    "com.example.MySealed", null,
                    List.of(), List.of(),
                    0, false, false, false, false, true
            );
            assertThat(sealedEntry.isSealed()).isTrue();
        }
    }

    @Nested
    @DisplayName("equals and hashCode | 相等性与哈希码")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal when all fields match")
        void shouldBeEqualWhenFieldsMatch() {
            ClassIndexEntry a = new ClassIndexEntry(
                    "com.example.A", null, List.of(), List.of(),
                    0, false, false, false, false, false
            );
            ClassIndexEntry b = new ClassIndexEntry(
                    "com.example.A", null, List.of(), List.of(),
                    0, false, false, false, false, false
            );
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("should not be equal when className differs")
        void shouldNotBeEqualWhenClassNameDiffers() {
            ClassIndexEntry a = new ClassIndexEntry(
                    "com.example.A", null, List.of(), List.of(),
                    0, false, false, false, false, false
            );
            ClassIndexEntry b = new ClassIndexEntry(
                    "com.example.B", null, List.of(), List.of(),
                    0, false, false, false, false, false
            );
            assertThat(a).isNotEqualTo(b);
        }
    }
}
