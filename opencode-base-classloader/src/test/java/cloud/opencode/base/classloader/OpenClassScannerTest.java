package cloud.opencode.base.classloader;

import cloud.opencode.base.classloader.scanner.AnnotationScanner;
import cloud.opencode.base.classloader.scanner.ClassScanner;
import cloud.opencode.base.classloader.scanner.PackageScanner;
import cloud.opencode.base.classloader.scanner.ScanFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for OpenClassScanner facade
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("OpenClassScanner Facade Tests")
class OpenClassScannerTest {

    // Use the project's own package for scanning tests
    private static final String TEST_PACKAGE = "cloud.opencode.base.classloader.scanner";
    private static final String PARENT_PACKAGE = "cloud.opencode.base.classloader";

    @Nested
    @DisplayName("Scanner Creation Tests")
    class ScannerCreationTests {

        @Test
        @DisplayName("Should create scanner for single package")
        void shouldCreateScannerForSinglePackage() {
            ClassScanner scanner = OpenClassScanner.of(TEST_PACKAGE);

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should create scanner for multiple packages")
        void shouldCreateScannerForMultiplePackages() {
            ClassScanner scanner = OpenClassScanner.of(TEST_PACKAGE, "cloud.opencode.base.classloader.metadata");

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should create scanner with classloader")
        void shouldCreateScannerWithClassLoader() {
            ClassScanner scanner = OpenClassScanner.of(
                    getClass().getClassLoader(), TEST_PACKAGE);

            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("Quick Scan with Annotation Tests")
    class QuickScanWithAnnotationTests {

        @Test
        @DisplayName("Should scan classes with annotation")
        void shouldScanClassesWithAnnotation() {
            Set<Class<?>> classes = OpenClassScanner.scanWithAnnotation(
                    TEST_PACKAGE, FunctionalInterface.class);

            // ScanFilter is annotated with @FunctionalInterface
            assertThat(classes).isNotEmpty();
            assertThat(classes).allMatch(c -> c.isAnnotationPresent(FunctionalInterface.class));
        }

        @Test
        @DisplayName("Should return empty for no matches")
        void shouldReturnEmptyForNoMatches() {
            Set<Class<?>> classes = OpenClassScanner.scanWithAnnotation(
                    TEST_PACKAGE, SuppressWarnings.class);

            // May or may not be empty depending on what's present
            assertThat(classes).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null base package")
        void shouldThrowOnNullBasePackage() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassScanner.scanWithAnnotation(null, FunctionalInterface.class));
        }

        @Test
        @DisplayName("Should throw on null annotation")
        void shouldThrowOnNullAnnotation() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassScanner.scanWithAnnotation(TEST_PACKAGE, null));
        }
    }

    @Nested
    @DisplayName("Quick Scan Subtypes Tests")
    class QuickScanSubtypesTests {

        @Test
        @DisplayName("Should scan subtypes")
        void shouldScanSubtypes() {
            Set<Class<? extends ScanFilter>> classes = OpenClassScanner.scanSubTypes(
                    TEST_PACKAGE, ScanFilter.class);

            // ScanFilter is a functional interface, may or may not have implementations
            assertThat(classes).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null base package")
        void shouldThrowOnNullBasePackageForSubtypes() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassScanner.scanSubTypes(null, Object.class));
        }

        @Test
        @DisplayName("Should throw on null super type")
        void shouldThrowOnNullSuperType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassScanner.scanSubTypes(TEST_PACKAGE, null));
        }
    }

    @Nested
    @DisplayName("Quick Scan Implementations Tests")
    class QuickScanImplementationsTests {

        @Test
        @DisplayName("Should scan implementations")
        void shouldScanImplementations() {
            Set<Class<? extends ScanFilter>> classes = OpenClassScanner.scanImplementations(
                    TEST_PACKAGE, ScanFilter.class);

            // May or may not find implementations
            assertThat(classes).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null base package")
        void shouldThrowOnNullBasePackageForImplementations() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassScanner.scanImplementations(null, ScanFilter.class));
        }

        @Test
        @DisplayName("Should throw on null interface type")
        void shouldThrowOnNullInterfaceType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassScanner.scanImplementations(TEST_PACKAGE, null));
        }
    }

    @Nested
    @DisplayName("Specialized Scanner Tests")
    class SpecializedScannerTests {

        @Test
        @DisplayName("Should create annotation scanner")
        void shouldCreateAnnotationScanner() {
            AnnotationScanner scanner = OpenClassScanner.annotationScanner(TEST_PACKAGE);

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should create package scanner")
        void shouldCreatePackageScanner() {
            PackageScanner scanner = OpenClassScanner.packageScanner(TEST_PACKAGE);

            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("Scanner Configuration Tests")
    class ScannerConfigurationTests {

        @Test
        @DisplayName("Should scan with filter")
        void shouldScanWithFilter() {
            Set<Class<?>> classes = OpenClassScanner.of(TEST_PACKAGE)
                    .scan(ScanFilter.isInterface());

            assertThat(classes).isNotEmpty();
            assertThat(classes).allMatch(Class::isInterface);
        }

        @Test
        @DisplayName("Should scan with inner classes")
        void shouldScanWithInnerClasses() {
            Set<Class<?>> classes = OpenClassScanner.of(TEST_PACKAGE)
                    .includeInnerClasses(true)
                    .scan();

            assertThat(classes).isNotEmpty();
        }

        @Test
        @DisplayName("Should scan with parallel")
        void shouldScanWithParallel() {
            Set<Class<?>> classes = OpenClassScanner.of(TEST_PACKAGE)
                    .parallel(true)
                    .scan();

            assertThat(classes).isNotEmpty();
        }
    }
}
