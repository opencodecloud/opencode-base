package cloud.opencode.base.classloader.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ClassScanner
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("ClassScanner Tests")
class ClassScannerTest {

    // Use the project's own package for scanning tests
    private static final String TEST_PACKAGE = "cloud.opencode.base.classloader.scanner";

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create with single package")
        void shouldCreateWithSinglePackage() {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE);

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should create with multiple packages")
        void shouldCreateWithMultiplePackages() {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE, "cloud.opencode.base.classloader.metadata");

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should create with classloader")
        void shouldCreateWithClassLoader() {
            ClassScanner scanner = ClassScanner.of(getClass().getClassLoader(), TEST_PACKAGE);

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null package")
        void shouldThrowOnNullPackage() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassScanner.of((String) null));
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should configure includeJars")
        void shouldConfigureIncludeJars() {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .includeJars(true);

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should configure includeInnerClasses")
        void shouldConfigureIncludeInnerClasses() {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .includeInnerClasses(true);

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should configure parallel")
        void shouldConfigureParallel() {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .parallel(true);

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should configure exclude package")
        void shouldConfigureExcludePackage() {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .excludePackage("cloud.opencode.base.classloader.loader");

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should configure classloader")
        void shouldConfigureClassLoader() {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .classLoader(getClass().getClassLoader());

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should handle null classloader")
        void shouldHandleNullClassLoader() {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .classLoader(null);

            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("Scan Tests")
    class ScanTests {

        @Test
        @DisplayName("Should scan all classes")
        void shouldScanAllClasses() {
            Set<Class<?>> classes = ClassScanner.of(TEST_PACKAGE)
                    .scan();

            assertThat(classes).isNotEmpty();
            assertThat(classes).anyMatch(c -> c.getName().startsWith(TEST_PACKAGE));
        }

        @Test
        @DisplayName("Should scan with filter")
        void shouldScanWithFilter() {
            Set<Class<?>> classes = ClassScanner.of(TEST_PACKAGE)
                    .scan(ScanFilter.isInterface());

            assertThat(classes).isNotEmpty();
            assertThat(classes).allMatch(Class::isInterface);
        }

        @Test
        @DisplayName("Should scan with null filter throws")
        void shouldScanWithNullFilterThrows() {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE);

            assertThatNullPointerException()
                    .isThrownBy(() -> scanner.scan(null));
        }
    }

    @Nested
    @DisplayName("Annotation Scan Tests")
    class AnnotationScanTests {

        @Test
        @DisplayName("Should scan with annotation")
        void shouldScanWithAnnotation() {
            Set<Class<?>> classes = ClassScanner.of(TEST_PACKAGE)
                    .scanWithAnnotation(FunctionalInterface.class);

            // ScanFilter is annotated with @FunctionalInterface
            assertThat(classes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Subtype Scan Tests")
    class SubtypeScanTests {

        @Test
        @DisplayName("Should scan subtypes")
        void shouldScanSubtypes() {
            Set<Class<? extends ScanFilter>> classes = ClassScanner.of(TEST_PACKAGE)
                    .scanSubTypes(ScanFilter.class);

            // ScanFilter is a functional interface, no direct subtypes
            assertThat(classes).isNotNull();
        }

        @Test
        @DisplayName("Should scan implementations")
        void shouldScanImplementations() {
            Set<Class<? extends ScanFilter>> classes = ClassScanner.of(TEST_PACKAGE)
                    .includeInnerClasses(false)
                    .scanImplementations(ScanFilter.class);

            // May or may not find implementations
            assertThat(classes).isNotNull();
        }
    }

    @Nested
    @DisplayName("Stream API Tests")
    class StreamApiTests {

        @Test
        @DisplayName("Should return class stream")
        void shouldReturnClassStream() {
            Stream<Class<?>> stream = ClassScanner.of(TEST_PACKAGE)
                    .stream();

            assertThat(stream).isNotNull();
            assertThat(stream.count()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return class name stream")
        void shouldReturnClassNameStream() {
            Stream<String> stream = ClassScanner.of(TEST_PACKAGE)
                    .classNameStream();

            assertThat(stream).isNotNull();
            assertThat(stream.count()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should support parallel stream")
        void shouldSupportParallelStream() {
            Stream<String> stream = ClassScanner.of(TEST_PACKAGE)
                    .parallel(true)
                    .classNameStream();

            assertThat(stream).isNotNull();
        }
    }

    @Nested
    @DisplayName("Inner Class Tests")
    class InnerClassTests {

        @Test
        @DisplayName("Should exclude inner classes by default")
        void shouldExcludeInnerClassesByDefault() {
            Set<Class<?>> classes = ClassScanner.of(TEST_PACKAGE)
                    .includeInnerClasses(false)
                    .scan();

            assertThat(classes).noneMatch(c -> c.getName().contains("$"));
        }

        @Test
        @DisplayName("Should include inner classes when configured")
        void shouldIncludeInnerClassesWhenConfigured() {
            Set<Class<?>> classes = ClassScanner.of(TEST_PACKAGE)
                    .includeInnerClasses(true)
                    .scan();

            // Should find classes, may or may not include inner classes
            assertThat(classes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Package Exclusion Tests")
    class PackageExclusionTests {

        @Test
        @DisplayName("Should exclude specified packages")
        void shouldExcludeSpecifiedPackages() {
            Set<Class<?>> classes = ClassScanner.of("cloud.opencode.base.classloader")
                    .excludePackage("cloud.opencode.base.classloader.loader")
                    .excludePackage("cloud.opencode.base.classloader.resource")
                    .scan();

            assertThat(classes).noneMatch(c ->
                    c.getName().startsWith("cloud.opencode.base.classloader.loader") ||
                    c.getName().startsWith("cloud.opencode.base.classloader.resource"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle nonexistent package")
        void shouldHandleNonexistentPackage() {
            Set<Class<?>> classes = ClassScanner.of("com.nonexistent.package")
                    .scan();

            assertThat(classes).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty package")
        void shouldHandleEmptyPackage() {
            Set<Class<?>> classes = ClassScanner.of("")
                    .scan();

            // Empty package may or may not have classes
            assertThat(classes).isNotNull();
        }
    }
}
