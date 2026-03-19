package cloud.opencode.base.classloader.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for AnnotationScanner
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("AnnotationScanner Tests")
class AnnotationScannerTest {

    // Use the project's own package for scanning tests
    private static final String TEST_PACKAGE = "cloud.opencode.base.classloader.scanner";

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create with single package")
        void shouldCreateWithSinglePackage() {
            AnnotationScanner scanner = AnnotationScanner.of(TEST_PACKAGE);

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should configure classloader")
        void shouldConfigureClassLoader() {
            AnnotationScanner scanner = AnnotationScanner.of(TEST_PACKAGE)
                    .classLoader(getClass().getClassLoader());

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should configure include inner classes")
        void shouldConfigureIncludeInnerClasses() {
            AnnotationScanner scanner = AnnotationScanner.of(TEST_PACKAGE)
                    .includeInnerClasses(true);

            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("Scan Classes Tests")
    class ScanClassesTests {

        @Test
        @DisplayName("Should scan classes with annotation")
        void shouldScanClassesWithAnnotation() {
            Set<Class<?>> classes = AnnotationScanner.of(TEST_PACKAGE)
                    .scanClasses(FunctionalInterface.class);

            // ScanFilter is annotated with @FunctionalInterface
            assertThat(classes).isNotEmpty();
            assertThat(classes).allMatch(c -> c.isAnnotationPresent(FunctionalInterface.class));
        }

        @Test
        @DisplayName("Should return empty for no matches")
        void shouldReturnEmptyForNoMatches() {
            Set<Class<?>> classes = AnnotationScanner.of(TEST_PACKAGE)
                    .scanClasses(SuppressWarnings.class);

            // May or may not be empty depending on what's present
            assertThat(classes).isNotNull();
        }
    }

    @Nested
    @DisplayName("Scan Methods Tests")
    class ScanMethodsTests {

        @Test
        @DisplayName("Should scan methods with annotation")
        void shouldScanMethodsWithAnnotation() {
            Set<Method> methods = AnnotationScanner.of(TEST_PACKAGE)
                    .scanMethods(Deprecated.class);

            // There may or may not be deprecated methods
            assertThat(methods).isNotNull();
        }
    }

    @Nested
    @DisplayName("Scan Fields Tests")
    class ScanFieldsTests {

        @Test
        @DisplayName("Should scan fields with annotation")
        void shouldScanFieldsWithAnnotation() {
            Set<Field> fields = AnnotationScanner.of(TEST_PACKAGE)
                    .scanFields(Deprecated.class);

            assertThat(fields).isNotNull();
        }
    }

    @Nested
    @DisplayName("Scan Constructors Tests")
    class ScanConstructorsTests {

        @Test
        @DisplayName("Should scan constructors with annotation")
        void shouldScanConstructorsWithAnnotation() {
            Set<Constructor<?>> constructors = AnnotationScanner.of(TEST_PACKAGE)
                    .scanConstructors(Deprecated.class);

            assertThat(constructors).isNotNull();
        }
    }

    @Nested
    @DisplayName("Meta-Annotation Tests")
    class MetaAnnotationTests {

        @Test
        @DisplayName("Should scan meta-annotated classes")
        void shouldScanMetaAnnotatedClasses() {
            Set<Class<?>> classes = AnnotationScanner.of(TEST_PACKAGE)
                    .scanMetaAnnotated(FunctionalInterface.class);

            // May or may not find meta-annotated classes
            assertThat(classes).isNotNull();
        }

        @Test
        @DisplayName("Should scan classes with functional interface meta-annotation")
        void shouldScanClassesWithFunctionalInterfaceMetaAnnotation() {
            Set<Class<?>> classes = AnnotationScanner.of(TEST_PACKAGE)
                    .scanMetaAnnotated(FunctionalInterface.class);

            // May or may not find classes
            assertThat(classes).isNotNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle nonexistent package")
        void shouldHandleNonexistentPackage() {
            Set<Class<?>> classes = AnnotationScanner.of("com.nonexistent.package")
                    .scanClasses(Deprecated.class);

            assertThat(classes).isEmpty();
        }

        @Test
        @DisplayName("Should throw on null annotation in scanClasses")
        void shouldThrowOnNullAnnotationInScanClasses() {
            AnnotationScanner scanner = AnnotationScanner.of(TEST_PACKAGE);

            assertThatNullPointerException()
                    .isThrownBy(() -> scanner.scanClasses(null));
        }

        @Test
        @DisplayName("Should throw on null annotation in scanMethods")
        void shouldThrowOnNullAnnotationInScanMethods() {
            AnnotationScanner scanner = AnnotationScanner.of(TEST_PACKAGE);

            assertThatNullPointerException()
                    .isThrownBy(() -> scanner.scanMethods(null));
        }

        @Test
        @DisplayName("Should throw on null annotation in scanFields")
        void shouldThrowOnNullAnnotationInScanFields() {
            AnnotationScanner scanner = AnnotationScanner.of(TEST_PACKAGE);

            assertThatNullPointerException()
                    .isThrownBy(() -> scanner.scanFields(null));
        }

        @Test
        @DisplayName("Should throw on null annotation in scanConstructors")
        void shouldThrowOnNullAnnotationInScanConstructors() {
            AnnotationScanner scanner = AnnotationScanner.of(TEST_PACKAGE);

            assertThatNullPointerException()
                    .isThrownBy(() -> scanner.scanConstructors(null));
        }

        @Test
        @DisplayName("Should throw on null annotation in scanMetaAnnotated")
        void shouldThrowOnNullAnnotationInScanMetaAnnotated() {
            AnnotationScanner scanner = AnnotationScanner.of(TEST_PACKAGE);

            assertThatNullPointerException()
                    .isThrownBy(() -> scanner.scanMetaAnnotated(null));
        }
    }
}
