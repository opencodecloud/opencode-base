package cloud.opencode.base.classloader.diagnostic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ClassLoaderDiagnostics
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("ClassLoaderDiagnostics Tests")
class ClassLoaderDiagnosticsTest {

    @Nested
    @DisplayName("findDuplicateClasses Tests")
    class FindDuplicateClassesTests {

        @Test
        @DisplayName("should return empty list when fewer than 2 loaders")
        void shouldReturnEmptyForSingleLoader() {
            URLClassLoader loader = new URLClassLoader(new URL[0], null);
            try {
                List<DuplicateClassReport> result =
                        ClassLoaderDiagnostics.findDuplicateClasses(loader);
                assertThat(result).isEmpty();
            } finally {
                closeQuietly(loader);
            }
        }

        @Test
        @DisplayName("should return empty list for empty classloaders")
        void shouldReturnEmptyForEmptyLoaders() {
            URLClassLoader loader1 = new URLClassLoader(new URL[0], null);
            URLClassLoader loader2 = new URLClassLoader(new URL[0], null);
            try {
                List<DuplicateClassReport> result =
                        ClassLoaderDiagnostics.findDuplicateClasses(loader1, loader2);
                assertThat(result).isEmpty();
            } finally {
                closeQuietly(loader1);
                closeQuietly(loader2);
            }
        }

        @Test
        @DisplayName("should throw NPE for null array")
        void shouldThrowNpeForNullArray() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassLoaderDiagnostics.findDuplicateClasses((ClassLoader[]) null));
        }

        @Test
        @DisplayName("should throw NPE for null element")
        void shouldThrowNpeForNullElement() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassLoaderDiagnostics.findDuplicateClasses(
                            ClassLoader.getSystemClassLoader(), null));
        }
    }

    @Nested
    @DisplayName("detectPackageSplits Tests")
    class DetectPackageSplitsTests {

        @Test
        @DisplayName("should return empty list when fewer than 2 loaders")
        void shouldReturnEmptyForSingleLoader() {
            List<PackageSplitReport> result =
                    ClassLoaderDiagnostics.detectPackageSplits(ClassLoader.getSystemClassLoader());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty list for empty classloaders with no packages")
        void shouldReturnEmptyForEmptyLoaders() {
            URLClassLoader loader1 = new URLClassLoader(new URL[0], null);
            URLClassLoader loader2 = new URLClassLoader(new URL[0], null);
            try {
                List<PackageSplitReport> result =
                        ClassLoaderDiagnostics.detectPackageSplits(loader1, loader2);
                assertThat(result).isEmpty();
            } finally {
                closeQuietly(loader1);
                closeQuietly(loader2);
            }
        }

        @Test
        @DisplayName("should throw NPE for null array")
        void shouldThrowNpeForNullArray() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassLoaderDiagnostics.detectPackageSplits((ClassLoader[]) null));
        }

        @Test
        @DisplayName("should throw NPE for null element")
        void shouldThrowNpeForNullElement() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassLoaderDiagnostics.detectPackageSplits(
                            ClassLoader.getSystemClassLoader(), null));
        }

        @Test
        @DisplayName("should detect split packages when loaders share same parent with loaded classes")
        void shouldDetectSplitPackages() {
            // Both the system classloader and its parent (platform) may share packages
            ClassLoader system = ClassLoader.getSystemClassLoader();
            ClassLoader platform = ClassLoader.getPlatformClassLoader();

            List<PackageSplitReport> result =
                    ClassLoaderDiagnostics.detectPackageSplits(system, platform);
            // The result depends on what packages are defined; at minimum we verify
            // the method runs without error and returns a valid list
            assertThat(result).isNotNull();
            for (PackageSplitReport report : result) {
                assertThat(report.packageName()).isNotNull();
                assertThat(report.classLoaderNames()).hasSizeGreaterThanOrEqualTo(2);
            }
        }
    }

    @Nested
    @DisplayName("traceClassLoading Tests")
    class TraceClassLoadingTests {

        @Test
        @DisplayName("should trace JDK class through delegation chain")
        void shouldTraceJdkClass() {
            ClassLoadTrace trace = ClassLoaderDiagnostics.traceClassLoading(
                    "java.lang.String", ClassLoader.getSystemClassLoader());

            assertThat(trace.className()).isEqualTo("java.lang.String");
            assertThat(trace.delegationChain()).isNotEmpty();
            assertThat(trace.delegationChain()).last().isEqualTo("bootstrap");
            assertThat(trace.definingLoader()).isEqualTo("bootstrap");
            assertThat(trace.location()).isNotNull();
        }

        @Test
        @DisplayName("should trace with custom URLClassLoader")
        void shouldTraceWithCustomLoader() {
            URLClassLoader child = new URLClassLoader(
                    new URL[0], ClassLoader.getSystemClassLoader());
            try {
                ClassLoadTrace trace = ClassLoaderDiagnostics.traceClassLoading(
                        "java.lang.String", child);

                assertThat(trace.className()).isEqualTo("java.lang.String");
                assertThat(trace.delegationChain()).hasSizeGreaterThanOrEqualTo(2);
                // First entry should be the custom loader
                assertThat(trace.delegationChain().getFirst()).contains("URLClassLoader");
                // bootstrap should be at the end
                assertThat(trace.delegationChain().getLast()).isEqualTo("bootstrap");
                assertThat(trace.definingLoader()).isEqualTo("bootstrap");
            } finally {
                closeQuietly(child);
            }
        }

        @Test
        @DisplayName("should return null location for non-existent class")
        void shouldReturnNullLocationForNonExistentClass() {
            ClassLoadTrace trace = ClassLoaderDiagnostics.traceClassLoading(
                    "com.nonexistent.NoSuchClass", ClassLoader.getSystemClassLoader());

            assertThat(trace.className()).isEqualTo("com.nonexistent.NoSuchClass");
            assertThat(trace.delegationChain()).isNotEmpty();
            assertThat(trace.location()).isNull();
        }

        @Test
        @DisplayName("should throw NPE for null className")
        void shouldThrowNpeForNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassLoaderDiagnostics.traceClassLoading(
                            null, ClassLoader.getSystemClassLoader()));
        }

        @Test
        @DisplayName("should throw NPE for null classLoader")
        void shouldThrowNpeForNullClassLoader() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassLoaderDiagnostics.traceClassLoading(
                            "java.lang.String", null));
        }
    }

    @Nested
    @DisplayName("findClassLocations Tests")
    class FindClassLocationsTests {

        @Test
        @DisplayName("should find JDK class locations")
        void shouldFindJdkClassLocations() {
            List<String> locations = ClassLoaderDiagnostics.findClassLocations(
                    "java.lang.String", ClassLoader.getSystemClassLoader());

            assertThat(locations).isNotEmpty();
            assertThat(locations.getFirst()).contains("java.base");
        }

        @Test
        @DisplayName("should return empty list for non-existent class")
        void shouldReturnEmptyForNonExistentClass() {
            List<String> locations = ClassLoaderDiagnostics.findClassLocations(
                    "com.nonexistent.NoSuchClass", ClassLoader.getSystemClassLoader());

            assertThat(locations).isEmpty();
        }

        @Test
        @DisplayName("should find same class in multiple loaders sharing parent")
        void shouldFindClassInMultipleLoaders() {
            URLClassLoader loader1 = new URLClassLoader(
                    new URL[0], ClassLoader.getSystemClassLoader());
            URLClassLoader loader2 = new URLClassLoader(
                    new URL[0], ClassLoader.getSystemClassLoader());
            try {
                List<String> locations = ClassLoaderDiagnostics.findClassLocations(
                        "java.lang.String", loader1, loader2);

                // Both loaders delegate to system/bootstrap, so the same location should appear
                assertThat(locations).isNotEmpty();
            } finally {
                closeQuietly(loader1);
                closeQuietly(loader2);
            }
        }

        @Test
        @DisplayName("should throw NPE for null className")
        void shouldThrowNpeForNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassLoaderDiagnostics.findClassLocations(
                            null, ClassLoader.getSystemClassLoader()));
        }

        @Test
        @DisplayName("should throw NPE for null classLoaders array")
        void shouldThrowNpeForNullArray() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassLoaderDiagnostics.findClassLocations(
                            "java.lang.String", (ClassLoader[]) null));
        }

        @Test
        @DisplayName("should handle empty classloaders")
        void shouldHandleEmptyLoaders() {
            URLClassLoader emptyLoader = new URLClassLoader(new URL[0], null);
            try {
                List<String> locations = ClassLoaderDiagnostics.findClassLocations(
                        "com.nonexistent.Foo", emptyLoader);
                assertThat(locations).isEmpty();
            } finally {
                closeQuietly(emptyLoader);
            }
        }
    }

    @Nested
    @DisplayName("classNameToResourcePath Tests")
    class ClassNameToResourcePathTests {

        @Test
        @DisplayName("should convert dotted name to slash-separated path with .class")
        void shouldConvertClassName() {
            assertThat(ClassLoaderDiagnostics.classNameToResourcePath("com.example.Foo"))
                    .isEqualTo("com/example/Foo.class");
        }

        @Test
        @DisplayName("should handle default package class")
        void shouldHandleDefaultPackage() {
            assertThat(ClassLoaderDiagnostics.classNameToResourcePath("Foo"))
                    .isEqualTo("Foo.class");
        }
    }

    @Nested
    @DisplayName("classLoaderName Tests")
    class ClassLoaderNameTests {

        @Test
        @DisplayName("should return 'bootstrap' for null classloader")
        void shouldReturnBootstrapForNull() {
            assertThat(ClassLoaderDiagnostics.classLoaderName(null))
                    .isEqualTo("bootstrap");
        }

        @Test
        @DisplayName("should return named classloader name")
        void shouldReturnNamedLoaderName() {
            // The system classloader has a name in modern JDKs
            ClassLoader system = ClassLoader.getSystemClassLoader();
            String name = ClassLoaderDiagnostics.classLoaderName(system);
            assertThat(name).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("should return class@hash for unnamed loader")
        void shouldReturnClassHashForUnnamedLoader() {
            URLClassLoader unnamed = new URLClassLoader(new URL[0], null);
            try {
                String name = ClassLoaderDiagnostics.classLoaderName(unnamed);
                assertThat(name).contains("URLClassLoader@");
            } finally {
                closeQuietly(unnamed);
            }
        }
    }

    @Nested
    @DisplayName("DuplicateClassReport Record Tests")
    class DuplicateClassReportTests {

        @Test
        @DisplayName("should create valid report")
        void shouldCreateValidReport() {
            DuplicateClassReport report = new DuplicateClassReport(
                    "com.example.Foo",
                    List.of("loader1", "loader2"),
                    List.of("file:/a.jar", "file:/b.jar"));

            assertThat(report.className()).isEqualTo("com.example.Foo");
            assertThat(report.classLoaderNames()).containsExactly("loader1", "loader2");
            assertThat(report.locations()).containsExactly("file:/a.jar", "file:/b.jar");
        }

        @Test
        @DisplayName("should reject fewer than 2 loader names")
        void shouldRejectSingleLoaderName() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new DuplicateClassReport(
                            "com.example.Foo", List.of("loader1"), List.of()));
        }

        @Test
        @DisplayName("should make defensive copy of lists")
        void shouldMakeDefensiveCopy() {
            var loaders = new java.util.ArrayList<>(List.of("l1", "l2"));
            var locations = new java.util.ArrayList<>(List.of("loc1"));
            DuplicateClassReport report = new DuplicateClassReport("Foo", loaders, locations);

            loaders.add("l3");
            locations.add("loc2");

            assertThat(report.classLoaderNames()).hasSize(2);
            assertThat(report.locations()).hasSize(1);
        }

        @Test
        @DisplayName("should throw NPE for null className")
        void shouldThrowNpeForNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DuplicateClassReport(null, List.of("a", "b"), List.of()));
        }
    }

    @Nested
    @DisplayName("PackageSplitReport Record Tests")
    class PackageSplitReportTests {

        @Test
        @DisplayName("should create valid report")
        void shouldCreateValidReport() {
            PackageSplitReport report = new PackageSplitReport(
                    "com.example", List.of("loader1", "loader2"));

            assertThat(report.packageName()).isEqualTo("com.example");
            assertThat(report.classLoaderNames()).containsExactly("loader1", "loader2");
        }

        @Test
        @DisplayName("should reject fewer than 2 loader names")
        void shouldRejectSingleLoaderName() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new PackageSplitReport("com.example", List.of("loader1")));
        }

        @Test
        @DisplayName("should throw NPE for null packageName")
        void shouldThrowNpeForNullPackageName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new PackageSplitReport(null, List.of("a", "b")));
        }
    }

    @Nested
    @DisplayName("ClassLoadTrace Record Tests")
    class ClassLoadTraceTests {

        @Test
        @DisplayName("should create valid trace with null location")
        void shouldCreateValidTraceWithNullLocation() {
            ClassLoadTrace trace = new ClassLoadTrace(
                    "com.example.Foo",
                    List.of("child", "parent", "bootstrap"),
                    "bootstrap",
                    null);

            assertThat(trace.className()).isEqualTo("com.example.Foo");
            assertThat(trace.delegationChain()).hasSize(3);
            assertThat(trace.definingLoader()).isEqualTo("bootstrap");
            assertThat(trace.location()).isNull();
        }

        @Test
        @DisplayName("should create valid trace with location")
        void shouldCreateValidTraceWithLocation() {
            ClassLoadTrace trace = new ClassLoadTrace(
                    "com.example.Foo",
                    List.of("myLoader"),
                    "myLoader",
                    "file:/path/to/Foo.class");

            assertThat(trace.location()).isEqualTo("file:/path/to/Foo.class");
        }

        @Test
        @DisplayName("should throw NPE for null className")
        void shouldThrowNpeForNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ClassLoadTrace(null, List.of("a"), "a", null));
        }

        @Test
        @DisplayName("should throw NPE for null definingLoader")
        void shouldThrowNpeForNullDefiningLoader() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ClassLoadTrace("Foo", List.of("a"), null, null));
        }

        @Test
        @DisplayName("should make defensive copy of delegation chain")
        void shouldMakeDefensiveCopy() {
            var chain = new java.util.ArrayList<>(List.of("a", "b"));
            ClassLoadTrace trace = new ClassLoadTrace("Foo", chain, "a", null);

            chain.add("c");
            assertThat(trace.delegationChain()).hasSize(2);
        }
    }

    private static void closeQuietly(URLClassLoader loader) {
        try {
            loader.close();
        } catch (Exception ignored) {
            // best-effort cleanup
        }
    }
}
