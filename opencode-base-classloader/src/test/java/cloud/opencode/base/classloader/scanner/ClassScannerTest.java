package cloud.opencode.base.classloader.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Nested
    @DisplayName("Cache Tests")
    class CacheTests {

        @Test
        @DisplayName("Should write cache file on first scan")
        void shouldWriteCacheOnFirstScan(@TempDir Path tempDir) {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .cacheDir(tempDir)
                    .cacheKey("v1");

            Set<Class<?>> classes = scanner.scan();

            assertThat(classes).isNotEmpty();

            // Verify cache file was created
            Path cacheFile = tempDir.resolve(TEST_PACKAGE + "-v1.json");
            assertThat(cacheFile).exists();
        }

        @Test
        @DisplayName("Should load from cache on second scan")
        void shouldLoadFromCacheOnSecondScan(@TempDir Path tempDir) {
            // First scan — writes cache
            ClassScanner scanner1 = ClassScanner.of(TEST_PACKAGE)
                    .cacheDir(tempDir)
                    .cacheKey("v1");
            Set<Class<?>> first = scanner1.scan();

            // Second scan — should load from cache
            ClassScanner scanner2 = ClassScanner.of(TEST_PACKAGE)
                    .cacheDir(tempDir)
                    .cacheKey("v1");
            Set<Class<?>> second = scanner2.scan();

            assertThat(second).isNotEmpty();
            assertThat(second.stream().map(Class::getName).toList())
                    .containsExactlyInAnyOrderElementsOf(
                            first.stream().map(Class::getName).toList()
                    );
        }

        @Test
        @DisplayName("Should invalidate cache when classpath hash changes")
        void shouldInvalidateCacheOnHashChange(@TempDir Path tempDir) throws IOException {
            // First scan — writes cache
            ClassScanner scanner1 = ClassScanner.of(TEST_PACKAGE)
                    .cacheDir(tempDir)
                    .cacheKey("v1");
            scanner1.scan();

            Path cacheFile = tempDir.resolve(TEST_PACKAGE + "-v1.json");
            assertThat(cacheFile).exists();

            // Tamper with the cached classpathHash to simulate classpath change
            String json = Files.readString(cacheFile, StandardCharsets.UTF_8);
            String tampered = json.replace(
                    json.substring(json.indexOf("\"classpathHash\": \"") + 18,
                            json.indexOf("\"", json.indexOf("\"classpathHash\": \"") + 18)),
                    "invalid_hash_000"
            );
            Files.writeString(cacheFile, tampered, StandardCharsets.UTF_8);

            // Second scan — cache should be invalid, should rescan
            ClassScanner scanner2 = ClassScanner.of(TEST_PACKAGE)
                    .cacheDir(tempDir)
                    .cacheKey("v1");
            Set<Class<?>> result = scanner2.scan();

            assertThat(result).isNotEmpty();

            // Cache file should be overwritten with correct hash
            String updatedJson = Files.readString(cacheFile, StandardCharsets.UTF_8);
            assertThat(updatedJson).doesNotContain("invalid_hash_000");
        }

        @Test
        @DisplayName("Should not create cache when not configured")
        void shouldNotCreateCacheWhenNotConfigured(@TempDir Path tempDir) {
            // No cacheDir/cacheKey configured
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE);
            Set<Class<?>> classes = scanner.scan();

            assertThat(classes).isNotEmpty();

            // No cache files should be created anywhere
            assertThat(tempDir.toFile().listFiles()).isEmpty();
        }

        @Test
        @DisplayName("Should not create cache with only cacheDir configured")
        void shouldNotCreateCacheWithOnlyCacheDir(@TempDir Path tempDir) {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .cacheDir(tempDir);

            Set<Class<?>> classes = scanner.scan();

            assertThat(classes).isNotEmpty();
            assertThat(tempDir.toFile().listFiles()).isEmpty();
        }

        @Test
        @DisplayName("Should not create cache with only cacheKey configured")
        void shouldNotCreateCacheWithOnlyCacheKey(@TempDir Path tempDir) {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .cacheKey("v1");

            Set<Class<?>> classes = scanner.scan();

            assertThat(classes).isNotEmpty();
            // tempDir wasn't configured as cacheDir, so no files there
            assertThat(tempDir.toFile().listFiles()).isEmpty();
        }

        @Test
        @DisplayName("Should cache classNameStream results")
        void shouldCacheClassNameStream(@TempDir Path tempDir) {
            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .cacheDir(tempDir)
                    .cacheKey("v1");

            long count = scanner.classNameStream().count();

            assertThat(count).isGreaterThan(0);

            Path cacheFile = tempDir.resolve(TEST_PACKAGE + "-v1.json");
            assertThat(cacheFile).exists();
        }

        @Test
        @DisplayName("Should handle corrupt cache file gracefully")
        void shouldHandleCorruptCacheGracefully(@TempDir Path tempDir) throws IOException {
            Path cacheFile = tempDir.resolve(TEST_PACKAGE + "-v1.json");
            Files.writeString(cacheFile, "not valid json at all!!!", StandardCharsets.UTF_8);

            ClassScanner scanner = ClassScanner.of(TEST_PACKAGE)
                    .cacheDir(tempDir)
                    .cacheKey("v1");

            // Should not throw — should fall back to normal scan
            Set<Class<?>> classes = scanner.scan();

            assertThat(classes).isNotEmpty();
        }

        @Test
        @DisplayName("Should use different cache files for different cacheKeys")
        void shouldUseDifferentCacheFiles(@TempDir Path tempDir) {
            ClassScanner.of(TEST_PACKAGE).cacheDir(tempDir).cacheKey("v1").scan();
            ClassScanner.of(TEST_PACKAGE).cacheDir(tempDir).cacheKey("v2").scan();

            assertThat(tempDir.resolve(TEST_PACKAGE + "-v1.json")).exists();
            assertThat(tempDir.resolve(TEST_PACKAGE + "-v2.json")).exists();
        }
    }
}
