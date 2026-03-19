package cloud.opencode.base.classloader.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PackageScanner
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("PackageScanner Tests")
class PackageScannerTest {

    // Use the project's own package for scanning tests
    private static final String TEST_PACKAGE = "cloud.opencode.base.classloader.scanner";
    private static final String PARENT_PACKAGE = "cloud.opencode.base.classloader";

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create with single package")
        void shouldCreateWithSinglePackage() {
            PackageScanner scanner = PackageScanner.of(TEST_PACKAGE);

            assertThat(scanner).isNotNull();
            assertThat(scanner.getBasePackage()).isEqualTo(TEST_PACKAGE);
        }

        @Test
        @DisplayName("Should throw on null package")
        void shouldThrowOnNullPackage() {
            assertThatNullPointerException()
                    .isThrownBy(() -> PackageScanner.of(null));
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should configure classloader")
        void shouldConfigureClassLoader() {
            PackageScanner scanner = PackageScanner.of(TEST_PACKAGE)
                    .classLoader(getClass().getClassLoader());

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should configure virtual threads")
        void shouldConfigureVirtualThreads() {
            PackageScanner scanner = PackageScanner.of(TEST_PACKAGE)
                    .useVirtualThreads(true);

            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("Should handle null classloader")
        void shouldHandleNullClassLoader() {
            PackageScanner scanner = PackageScanner.of(TEST_PACKAGE)
                    .classLoader(null);

            assertThat(scanner).isNotNull();
        }
    }

    @Nested
    @DisplayName("FindSubPackages Tests")
    class FindSubPackagesTests {

        @Test
        @DisplayName("Should find sub packages")
        void shouldFindSubPackages() {
            Set<String> subPackages = PackageScanner.of(PARENT_PACKAGE)
                    .findSubPackages();

            assertThat(subPackages).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty for nonexistent package")
        void shouldReturnEmptyForNonexistentPackage() {
            Set<String> subPackages = PackageScanner.of("com.nonexistent.package")
                    .findSubPackages();

            assertThat(subPackages).isEmpty();
        }
    }

    @Nested
    @DisplayName("FindClasses Tests")
    class FindClassesTests {

        @Test
        @DisplayName("Should find classes non recursively")
        void shouldFindClassesNonRecursively() {
            List<Class<?>> classes = PackageScanner.of(TEST_PACKAGE)
                    .findClasses();

            assertThat(classes).isNotEmpty();
        }

        @Test
        @DisplayName("Should find classes recursively")
        void shouldFindClassesRecursively() {
            List<Class<?>> classes = PackageScanner.of(PARENT_PACKAGE)
                    .findClasses(true);

            assertThat(classes).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty for nonexistent package")
        void shouldReturnEmptyForNonexistentPackage() {
            List<Class<?>> classes = PackageScanner.of("com.nonexistent.package")
                    .findClasses();

            assertThat(classes).isEmpty();
        }
    }

    @Nested
    @DisplayName("FindClassNames Tests")
    class FindClassNamesTests {

        @Test
        @DisplayName("Should find class names non recursively")
        void shouldFindClassNamesNonRecursively() {
            List<String> classNames = PackageScanner.of(TEST_PACKAGE)
                    .findClassNames(false);

            assertThat(classNames).isNotEmpty();
            assertThat(classNames).allMatch(name -> name.startsWith(TEST_PACKAGE));
        }

        @Test
        @DisplayName("Should find class names recursively")
        void shouldFindClassNamesRecursively() {
            List<String> classNames = PackageScanner.of(PARENT_PACKAGE)
                    .findClassNames(true);

            assertThat(classNames).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Exists Tests")
    class ExistsTests {

        @Test
        @DisplayName("Should return true for existing package")
        void shouldReturnTrueForExistingPackage() {
            boolean exists = PackageScanner.of(TEST_PACKAGE).exists();

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false for nonexistent package")
        void shouldReturnFalseForNonexistentPackage() {
            boolean exists = PackageScanner.of("com.nonexistent.package").exists();

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Virtual Threads Tests")
    class VirtualThreadsTests {

        @Test
        @DisplayName("Should find classes with virtual threads")
        void shouldFindClassesWithVirtualThreads() {
            List<Class<?>> classes = PackageScanner.of(TEST_PACKAGE)
                    .useVirtualThreads(true)
                    .findClasses();

            assertThat(classes).isNotEmpty();
        }
    }
}
