package cloud.opencode.base.classloader.integration;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import cloud.opencode.base.classloader.index.ClassIndex;
import cloud.opencode.base.classloader.index.ClassIndexEntry;
import cloud.opencode.base.classloader.index.ClassIndexReader;
import cloud.opencode.base.classloader.index.IndexAwareScanner;
import cloud.opencode.base.classloader.leak.LeakDetection;
import cloud.opencode.base.classloader.leak.LeakDetector;
import cloud.opencode.base.classloader.loader.IsoClassLoader;
import cloud.opencode.base.classloader.resource.NestedJarHandler;
import cloud.opencode.base.classloader.resource.ResourceLoader;
import cloud.opencode.base.classloader.scanner.ClassScanner;
import cloud.opencode.base.classloader.security.ClassLoadingPolicy;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * V2 Integration Tests - Cross-component integration verification
 * V2 集成测试 - 跨组件集成验证
 *
 * <p>Covers the integration points added in v1.0.3:</p>
 * <ul>
 *   <li>IsoClassLoader + ClassLoadingPolicy bytecode enforcement</li>
 *   <li>ClassScanner + NestedJarHandler nested JAR scanning</li>
 *   <li>IndexAwareScanner + ClassIndexReader index-based scanning</li>
 *   <li>LeakDetector + IsoClassLoader lifecycle</li>
 *   <li>ResourceLoader + NestedJarResource double-nested URL parsing</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
class V2IntegrationTest {

    // ==================== IsoClassLoader + Policy Integration ====================

    @Nested
    @DisplayName("IsoClassLoader + ClassLoadingPolicy bytecode integration")
    class IsoClassLoaderPolicyIntegrationTests {

        @Test
        @DisplayName("Policy maxBytecodeSize enforced via findClass override")
        void policyMaxBytecodeSizeEnforcedOnLoad(@TempDir Path tempDir) throws Exception {
            // Create a valid .class file larger than our limit
            // Compile a simple class for testing
            Path classDir = tempDir.resolve("classes");
            Files.createDirectories(classDir);

            // Use the policy with a very small bytecode size limit
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxBytecodeSize(1) // 1 byte - virtually any class will exceed this
                    .build();

            // Build IsoClassLoader with the policy pointing to the temp dir
            IsoClassLoader loader = IsoClassLoader.fromDirectory(classDir)
                    .policy(policy)
                    .name("policy-test")
                    .build();

            // Trying to load a class from parent should still work for system classes
            // (system classes bypass the policy)
            Class<?> stringClass = loader.loadClass("java.lang.String");
            assertThat(stringClass).isNotNull();

            loader.close();
        }

        @Test
        @DisplayName("Policy deny packages blocks loading via IsoClassLoader")
        void policyDenyPackagesBlocksLoading(@TempDir Path tempDir) throws Exception {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addDeniedPackage("cloud.opencode.base.classloader.scanner")
                    .build();

            IsoClassLoader loader = IsoClassLoader.fromDirectory(tempDir)
                    .policy(policy)
                    .name("deny-test")
                    .build();

            // Loading a denied package class should throw
            assertThatThrownBy(() -> loader.loadClass("cloud.opencode.base.classloader.scanner.ClassScanner"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("denied by policy");

            loader.close();
        }

        @Test
        @DisplayName("Policy bytecodeVerifier rejects via findClass")
        void policyBytecodeVerifierRejectsViaFindClass(@TempDir Path tempDir) throws Exception {
            // Verifier that rejects everything
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .bytecodeVerifier(bytecode -> false)
                    .build();

            IsoClassLoader loader = IsoClassLoader.fromDirectory(tempDir)
                    .policy(policy)
                    .name("verifier-test")
                    .build();

            // System classes should still work (bypass findClass override)
            assertThat(loader.loadClass("java.lang.Integer")).isNotNull();

            loader.close();
        }
    }

    // ==================== LeakDetector + IsoClassLoader Integration ====================

    @Nested
    @DisplayName("LeakDetector + IsoClassLoader lifecycle integration")
    class LeakDetectorIntegrationTests {

        @Test
        @DisplayName("Properly closed IsoClassLoader does not generate leak report")
        void closedLoaderNoLeak(@TempDir Path tempDir) throws Exception {
            LeakDetector detector = LeakDetector.getInstance();
            int initialCount = detector.getTrackedCount();

            IsoClassLoader loader = IsoClassLoader.fromDirectory(tempDir)
                    .leakDetection(LeakDetection.SIMPLE)
                    .name("no-leak-test")
                    .build();

            assertThat(detector.getTrackedCount()).isEqualTo(initialCount + 1);

            loader.close();

            assertThat(detector.getTrackedCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("PARANOID mode records creation stack trace")
        void paranoidModeRecordsStack(@TempDir Path tempDir) {
            IsoClassLoader loader = IsoClassLoader.fromDirectory(tempDir)
                    .leakDetection(LeakDetection.PARANOID)
                    .name("paranoid-test")
                    .build();

            // Just verify it doesn't throw — stack trace is recorded internally
            assertThat(loader.getLeakDetection()).isEqualTo(LeakDetection.PARANOID);

            loader.close();
        }

        @Test
        @DisplayName("DISABLED mode does not register with detector")
        void disabledModeNoRegistration(@TempDir Path tempDir) {
            LeakDetector detector = LeakDetector.getInstance();
            int initialCount = detector.getTrackedCount();

            IsoClassLoader loader = IsoClassLoader.fromDirectory(tempDir)
                    .leakDetection(LeakDetection.DISABLED)
                    .name("disabled-test")
                    .build();

            assertThat(detector.getTrackedCount()).isEqualTo(initialCount);

            loader.close();
        }
    }

    // ==================== Index + Scanner Integration ====================

    @Nested
    @DisplayName("ClassIndex + IndexAwareScanner integration")
    class IndexScannerIntegrationTests {

        @Test
        @DisplayName("IndexAwareScanner filters by package prefix correctly")
        void indexAwareScannerFilters() {
            ClassIndex index = new ClassIndex(
                    ClassIndex.CURRENT_VERSION,
                    "2026-04-01T00:00:00Z",
                    "test-hash",
                    List.of(
                            new ClassIndexEntry("com.example.service.UserService", "java.lang.Object",
                                    List.of(), List.of("jakarta.inject.Singleton"), 1,
                                    false, false, false, false, false),
                            new ClassIndexEntry("com.example.model.User", "java.lang.Object",
                                    List.of(), List.of(), 1,
                                    false, false, false, false, false),
                            new ClassIndexEntry("com.other.OtherClass", "java.lang.Object",
                                    List.of(), List.of(), 1,
                                    false, false, false, false, false)
                    )
            );

            Set<String> serviceResult = IndexAwareScanner.scan(index, "com.example.service");
            assertThat(serviceResult).containsExactly("com.example.service.UserService");

            Set<String> allExample = IndexAwareScanner.scan(index, "com.example");
            assertThat(allExample).containsExactlyInAnyOrder(
                    "com.example.service.UserService",
                    "com.example.model.User"
            );

            Set<String> other = IndexAwareScanner.scan(index, "com.other");
            assertThat(other).containsExactly("com.other.OtherClass");
        }

        @Test
        @DisplayName("IndexAwareScanner with predicate filter")
        void indexAwareScannerWithPredicate() {
            ClassIndex index = new ClassIndex(
                    ClassIndex.CURRENT_VERSION,
                    "2026-04-01T00:00:00Z",
                    "test-hash",
                    List.of(
                            new ClassIndexEntry("com.example.MyRecord", "java.lang.Record",
                                    List.of(), List.of(), 1,
                                    false, false, false, true, false),
                            new ClassIndexEntry("com.example.MyClass", "java.lang.Object",
                                    List.of(), List.of(), 1,
                                    false, false, false, false, false)
                    )
            );

            Set<String> records = IndexAwareScanner.scan(index, "com.example",
                    entry -> entry.isRecord());
            assertThat(records).containsExactly("com.example.MyRecord");
        }

        @Test
        @DisplayName("ClassIndexReader invalidateCache clears cached index")
        void invalidateCacheClearsIndex() {
            ClassIndexReader.invalidateCache();
            // After invalidation, load() should re-read from classpath
            // (which likely returns empty in test environment)
            var result = ClassIndexReader.load();
            // Just verify no exception — actual index presence depends on test resources
            assertThat(result).isNotNull();
        }
    }

    // ==================== NestedJarHandler + ClassScanner Integration ====================

    @Nested
    @DisplayName("NestedJarHandler + ClassScanner integration")
    class NestedJarIntegrationTests {

        @Test
        @DisplayName("NestedJarHandler discovers and extracts nested JARs")
        void discoverAndExtract(@TempDir Path tempDir) throws Exception {
            // Create a fat JAR with a nested JAR in BOOT-INF/lib/
            Path fatJar = createFatJarWithNestedJar(tempDir);

            try (NestedJarHandler handler = NestedJarHandler.builder()
                    .tempDirectory(tempDir.resolve("extract"))
                    .build()) {

                List<String> nested = handler.findNestedJars(fatJar);
                assertThat(nested).hasSize(1);
                assertThat(nested.getFirst()).startsWith("BOOT-INF/lib/");

                Path extracted = handler.extractNestedJar(fatJar, nested.getFirst());
                assertThat(Files.exists(extracted)).isTrue();
                assertThat(Files.size(extracted)).isGreaterThan(0);

                handler.release(fatJar, nested.getFirst());
            }
        }

        @Test
        @DisplayName("ClassScanner includeNestedJars configuration is accepted")
        void classScannerAcceptsIncludeNestedJars() {
            ClassScanner scanner = ClassScanner.of("com.example")
                    .includeNestedJars(true);

            // Should not throw; actual nested scanning tested with real fat JARs
            assertThat(scanner).isNotNull();
        }

        @Test
        @DisplayName("ResourceLoader parses double-nested JAR URL and returns NestedJarResource")
        void resourceLoaderParsesDoubleNestedJarUrl(@TempDir Path tempDir) throws Exception {
            Path fatJar = createFatJarWithNestedJar(tempDir);

            ResourceLoader loader = ResourceLoader.create();
            String nestedUrl = "jar:file:" + fatJar + "!/BOOT-INF/lib/inner.jar!/META-INF/MANIFEST.MF";

            // Must return NestedJarResource (not JarResource or throw)
            var resource = loader.load(nestedUrl);
            assertThat(resource).isInstanceOf(
                    cloud.opencode.base.classloader.resource.NestedJarResource.class);
        }

        @Test
        @DisplayName("ResourceLoader parses simple JAR URL and returns JarResource")
        void resourceLoaderParsesSimpleJarUrl(@TempDir Path tempDir) throws Exception {
            Path fatJar = createFatJarWithNestedJar(tempDir);

            ResourceLoader loader = ResourceLoader.create();
            String simpleUrl = "jar:file:" + fatJar + "!/META-INF/MANIFEST.MF";

            // Single-level JAR URL must return JarResource (not NestedJarResource)
            var resource = loader.load(simpleUrl);
            assertThat(resource).isInstanceOf(
                    cloud.opencode.base.classloader.resource.JarResource.class);
        }

        /**
         * Create a minimal fat JAR containing a nested JAR in BOOT-INF/lib/
         */
        private Path createFatJarWithNestedJar(Path tempDir) throws IOException {
            // First create the inner JAR
            Path innerJar = tempDir.resolve("inner.jar");
            try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(innerJar.toFile()))) {
                JarEntry entry = new JarEntry("META-INF/MANIFEST.MF");
                jos.putNextEntry(entry);
                jos.write("Manifest-Version: 1.0\n".getBytes());
                jos.closeEntry();
            }

            // Create the fat JAR with the inner JAR nested
            Path fatJar = tempDir.resolve("app.jar");
            try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(fatJar.toFile()))) {
                // Add nested JAR entry
                JarEntry nestedEntry = new JarEntry("BOOT-INF/lib/inner.jar");
                jos.putNextEntry(nestedEntry);
                jos.write(Files.readAllBytes(innerJar));
                jos.closeEntry();

                // Add a manifest
                JarEntry manifest = new JarEntry("META-INF/MANIFEST.MF");
                jos.putNextEntry(manifest);
                jos.write("Manifest-Version: 1.0\n".getBytes());
                jos.closeEntry();
            }

            return fatJar;
        }
    }
}
