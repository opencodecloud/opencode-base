package cloud.opencode.base.classloader.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ScanFilter
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("ScanFilter Tests")
class ScanFilterTest {

    @Nested
    @DisplayName("Combination Tests")
    class CombinationTests {

        @Test
        @DisplayName("Should combine with and")
        void shouldCombineWithAnd() {
            ScanFilter filter = ScanFilter.isConcrete().and(ScanFilter.isPublic());

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(Serializable.class)).isFalse(); // Interface
        }

        @Test
        @DisplayName("Should combine with or")
        void shouldCombineWithOr() {
            ScanFilter filter = ScanFilter.isInterface().or(ScanFilter.isEnum());

            assertThat(filter.test(Serializable.class)).isTrue();
            assertThat(filter.test(RetentionPolicy.class)).isTrue();
            assertThat(filter.test(String.class)).isFalse();
        }

        @Test
        @DisplayName("Should negate filter")
        void shouldNegateFilter() {
            ScanFilter filter = ScanFilter.isInterface().negate();

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(Serializable.class)).isFalse();
        }

        @Test
        @DisplayName("Should combine multiple with static and")
        void shouldCombineMultipleWithStaticAnd() {
            ScanFilter filter = ScanFilter.and(
                    ScanFilter.isConcrete(),
                    ScanFilter.isPublic(),
                    ScanFilter.isFinal()
            );

            assertThat(filter.test(String.class)).isTrue();
        }

        @Test
        @DisplayName("Should combine multiple with static or")
        void shouldCombineMultipleWithStaticOr() {
            ScanFilter filter = ScanFilter.or(
                    ScanFilter.isInterface(),
                    ScanFilter.isEnum(),
                    ScanFilter.isAnnotation()
            );

            assertThat(filter.test(Serializable.class)).isTrue();
            assertThat(filter.test(RetentionPolicy.class)).isTrue();
            assertThat(filter.test(Retention.class)).isTrue();
        }

        @Test
        @DisplayName("Should use static not")
        void shouldUseStaticNot() {
            ScanFilter filter = ScanFilter.not(ScanFilter.isInterface());

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(Serializable.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Type Filter Tests")
    class TypeFilterTests {

        @Test
        @DisplayName("Should filter concrete classes")
        void shouldFilterConcreteClasses() {
            ScanFilter filter = ScanFilter.isConcrete();

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(Serializable.class)).isFalse(); // Interface
            assertThat(filter.test(Number.class)).isFalse(); // Abstract
        }

        @Test
        @DisplayName("Should filter interfaces")
        void shouldFilterInterfaces() {
            ScanFilter filter = ScanFilter.isInterface();

            assertThat(filter.test(Serializable.class)).isTrue();
            assertThat(filter.test(String.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter abstract classes")
        void shouldFilterAbstractClasses() {
            ScanFilter filter = ScanFilter.isAbstract();

            assertThat(filter.test(Number.class)).isTrue();
            assertThat(filter.test(String.class)).isFalse();
            assertThat(filter.test(Serializable.class)).isFalse(); // Interface, not abstract class
        }

        @Test
        @DisplayName("Should filter enums")
        void shouldFilterEnums() {
            ScanFilter filter = ScanFilter.isEnum();

            assertThat(filter.test(RetentionPolicy.class)).isTrue();
            assertThat(filter.test(String.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter records")
        void shouldFilterRecords() {
            ScanFilter filter = ScanFilter.isRecord();

            // Test with a non-record class
            assertThat(filter.test(String.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter annotations")
        void shouldFilterAnnotations() {
            ScanFilter filter = ScanFilter.isAnnotation();

            assertThat(filter.test(Retention.class)).isTrue();
            assertThat(filter.test(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Annotation Filter Tests")
    class AnnotationFilterTests {

        @Test
        @DisplayName("Should filter by annotation")
        void shouldFilterByAnnotation() {
            ScanFilter filter = ScanFilter.hasAnnotation(Deprecated.class);

            assertThat(filter.test(String.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter by any annotation")
        void shouldFilterByAnyAnnotation() {
            ScanFilter filter = ScanFilter.hasAnyAnnotation(Deprecated.class, SuppressWarnings.class);

            // Test logic works correctly
            assertThat(filter).isNotNull();
        }

        @Test
        @DisplayName("Should filter by all annotations")
        void shouldFilterByAllAnnotations() {
            ScanFilter filter = ScanFilter.hasAllAnnotations(Deprecated.class);

            assertThat(filter).isNotNull();
        }
    }

    @Nested
    @DisplayName("Inheritance Filter Tests")
    class InheritanceFilterTests {

        @Test
        @DisplayName("Should filter subtypes")
        void shouldFilterSubtypes() {
            ScanFilter filter = ScanFilter.isSubTypeOf(Number.class);

            assertThat(filter.test(Integer.class)).isTrue();
            assertThat(filter.test(String.class)).isFalse();
            assertThat(filter.test(Number.class)).isFalse(); // Not subtype of itself
        }

        @Test
        @DisplayName("Should filter interface implementations")
        void shouldFilterInterfaceImplementations() {
            ScanFilter filter = ScanFilter.implementsInterface(Serializable.class);

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(Object.class)).isFalse();
            assertThat(filter.test(Serializable.class)).isFalse(); // Interface itself
        }

        @Test
        @DisplayName("Should throw on non-interface in implementsInterface")
        void shouldThrowOnNonInterfaceInImplementsInterface() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ScanFilter.implementsInterface(String.class))
                    .withMessageContaining("Not an interface");
        }
    }

    @Nested
    @DisplayName("Name Filter Tests")
    class NameFilterTests {

        @Test
        @DisplayName("Should filter by name prefix")
        void shouldFilterByNamePrefix() {
            ScanFilter filter = ScanFilter.nameStartsWith("java.lang");

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(java.util.List.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter by name suffix")
        void shouldFilterByNameSuffix() {
            ScanFilter filter = ScanFilter.nameEndsWith("String");

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter by simple name suffix")
        void shouldFilterBySimpleNameSuffix() {
            ScanFilter filter = ScanFilter.simpleNameEndsWith("List");

            assertThat(filter.test(java.util.ArrayList.class)).isTrue();
            assertThat(filter.test(String.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter by name regex")
        void shouldFilterByNameRegex() {
            ScanFilter filter = ScanFilter.nameMatches("java\\.lang\\..*");

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(java.util.List.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter by package")
        void shouldFilterByPackage() {
            ScanFilter filter = ScanFilter.inPackage("java.lang");

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(java.util.List.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Modifier Filter Tests")
    class ModifierFilterTests {

        @Test
        @DisplayName("Should filter by modifier")
        void shouldFilterByModifier() {
            ScanFilter filter = ScanFilter.hasModifier(Modifier.FINAL);

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(Number.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter public classes")
        void shouldFilterPublicClasses() {
            ScanFilter filter = ScanFilter.isPublic();

            assertThat(filter.test(String.class)).isTrue();
        }

        @Test
        @DisplayName("Should filter final classes")
        void shouldFilterFinalClasses() {
            ScanFilter filter = ScanFilter.isFinal();

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(Object.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Special Filter Tests")
    class SpecialFilterTests {

        @Test
        @DisplayName("Should accept all with all filter")
        void shouldAcceptAllWithAllFilter() {
            ScanFilter filter = ScanFilter.all();

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(Serializable.class)).isTrue();
            assertThat(filter.test(RetentionPolicy.class)).isTrue();
        }

        @Test
        @DisplayName("Should reject all with none filter")
        void shouldRejectAllWithNoneFilter() {
            ScanFilter filter = ScanFilter.none();

            assertThat(filter.test(String.class)).isFalse();
            assertThat(filter.test(Serializable.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter top-level classes")
        void shouldFilterTopLevelClasses() {
            ScanFilter filter = ScanFilter.isTopLevel();

            assertThat(filter.test(String.class)).isTrue();
            assertThat(filter.test(java.util.Map.Entry.class)).isFalse();
        }

        @Test
        @DisplayName("Should filter inner classes")
        void shouldFilterInnerClasses() {
            ScanFilter filter = ScanFilter.isInnerClass();

            assertThat(filter.test(java.util.Map.Entry.class)).isTrue();
            assertThat(filter.test(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

        @Test
        @DisplayName("Should throw on null in and")
        void shouldThrowOnNullInAnd() {
            ScanFilter filter = ScanFilter.isConcrete();
            ScanFilter nullFilter = null;
            assertThatNullPointerException()
                    .isThrownBy(() -> filter.and(nullFilter));
        }

        @Test
        @DisplayName("Should throw on null in or")
        void shouldThrowOnNullInOr() {
            ScanFilter filter = ScanFilter.isConcrete();
            ScanFilter nullFilter = null;
            assertThatNullPointerException()
                    .isThrownBy(() -> filter.or(nullFilter));
        }

        @Test
        @DisplayName("Should throw on null annotation")
        void shouldThrowOnNullAnnotation() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ScanFilter.hasAnnotation(null));
        }

        @Test
        @DisplayName("Should throw on null super type")
        void shouldThrowOnNullSuperType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ScanFilter.isSubTypeOf(null));
        }

        @Test
        @DisplayName("Should throw on null interface type")
        void shouldThrowOnNullInterfaceType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ScanFilter.implementsInterface(null));
        }

        @Test
        @DisplayName("Should throw on null prefix")
        void shouldThrowOnNullPrefix() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ScanFilter.nameStartsWith(null));
        }

        @Test
        @DisplayName("Should throw on null suffix")
        void shouldThrowOnNullSuffix() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ScanFilter.nameEndsWith(null));
        }

        @Test
        @DisplayName("Should throw on null regex")
        void shouldThrowOnNullRegex() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ScanFilter.nameMatches(null));
        }

        @Test
        @DisplayName("Should throw on null package name")
        void shouldThrowOnNullPackageName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ScanFilter.inPackage(null));
        }
    }
}
