package cloud.opencode.base.classloader.resource;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for NestedJarHandler
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("NestedJarHandler Tests")
class NestedJarHandlerTest {

    @TempDir
    Path tempDir;

    private NestedJarHandler handler;

    @BeforeEach
    void setUp() {
        handler = NestedJarHandler.builder()
                .tempDirectory(tempDir.resolve("nested-work"))
                .build();
    }

    @AfterEach
    void tearDown() {
        if (handler != null && !handler.isClosed()) {
            handler.close();
        }
    }

    // ==================== Helper: build a fat JAR with nested JARs ====================

    /**
     * Creates a fat JAR containing:
     *   BOOT-INF/lib/inner.jar  (contains hello.txt = "hello-inner")
     *   WEB-INF/lib/web.jar     (contains web.properties = "k=v")
     *   lib/plain.jar           (contains plain.txt = "plain-content")
     *   README.txt              (top-level, not a nested JAR)
     */
    private Path createFatJar() throws IOException {
        byte[] innerJarBytes = buildInnerJar("hello.txt", "hello-inner");
        byte[] webJarBytes = buildInnerJar("web.properties", "k=v");
        byte[] plainJarBytes = buildInnerJar("plain.txt", "plain-content");

        Path fatJar = tempDir.resolve("fat.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(fatJar.toFile()))) {
            addEntry(jos, "BOOT-INF/lib/inner.jar", innerJarBytes);
            addEntry(jos, "WEB-INF/lib/web.jar", webJarBytes);
            addEntry(jos, "lib/plain.jar", plainJarBytes);
            addEntry(jos, "README.txt", "top-level readme".getBytes());
        }
        return fatJar;
    }

    /**
     * Builds a minimal JAR in memory with one entry.
     */
    private byte[] buildInnerJar(String entryName, String content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (JarOutputStream jos = new JarOutputStream(baos)) {
            jos.putNextEntry(new JarEntry(entryName));
            jos.write(content.getBytes());
            jos.closeEntry();
        }
        return baos.toByteArray();
    }

    private void addEntry(JarOutputStream jos, String name, byte[] data) throws IOException {
        // Ensure parent directory entries exist
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash > 0) {
            String dir = name.substring(0, lastSlash + 1);
            JarEntry dirEntry = new JarEntry(dir);
            try {
                jos.putNextEntry(dirEntry);
                jos.closeEntry();
            } catch (java.util.zip.ZipException ignored) {
                // duplicate entry — already added
            }
        }
        JarEntry entry = new JarEntry(name);
        jos.putNextEntry(entry);
        jos.write(data);
        jos.closeEntry();
    }

    // ==================== Tests ====================

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build with default temp directory")
        void shouldBuildWithDefaultTempDir() {
            try (NestedJarHandler h = NestedJarHandler.builder().build()) {
                assertThat(h.getTempDirectory()).isNotNull();
                assertThat(Files.exists(h.getTempDirectory())).isTrue();
            }
        }

        @Test
        @DisplayName("Should build with custom temp directory")
        void shouldBuildWithCustomTempDir() {
            assertThat(handler.getTempDirectory()).isEqualTo(tempDir.resolve("nested-work"));
        }
    }

    @Nested
    @DisplayName("FindNestedJars Tests")
    class FindNestedJarsTests {

        @Test
        @DisplayName("Should discover BOOT-INF/lib JARs")
        void shouldDiscoverBootInfLibJars() throws IOException {
            Path fatJar = createFatJar();
            List<String> nested = handler.findNestedJars(fatJar);

            assertThat(nested).contains("BOOT-INF/lib/inner.jar");
        }

        @Test
        @DisplayName("Should discover WEB-INF/lib JARs")
        void shouldDiscoverWebInfLibJars() throws IOException {
            Path fatJar = createFatJar();
            List<String> nested = handler.findNestedJars(fatJar);

            assertThat(nested).contains("WEB-INF/lib/web.jar");
        }

        @Test
        @DisplayName("Should discover lib/ JARs")
        void shouldDiscoverLibJars() throws IOException {
            Path fatJar = createFatJar();
            List<String> nested = handler.findNestedJars(fatJar);

            assertThat(nested).contains("lib/plain.jar");
        }

        @Test
        @DisplayName("Should not include non-JAR entries")
        void shouldNotIncludeNonJarEntries() throws IOException {
            Path fatJar = createFatJar();
            List<String> nested = handler.findNestedJars(fatJar);

            assertThat(nested).noneMatch(name -> name.equals("README.txt"));
        }

        @Test
        @DisplayName("Should return all three nested JARs")
        void shouldReturnAllThreeNestedJars() throws IOException {
            Path fatJar = createFatJar();
            List<String> nested = handler.findNestedJars(fatJar);

            assertThat(nested).hasSize(3);
        }

        @Test
        @DisplayName("Should return empty list for JAR without nested JARs")
        void shouldReturnEmptyForJarWithoutNestedJars() throws IOException {
            Path simpleJar = tempDir.resolve("simple.jar");
            try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(simpleJar.toFile()))) {
                addEntry(jos, "readme.txt", "hello".getBytes());
            }

            List<String> nested = handler.findNestedJars(simpleJar);
            assertThat(nested).isEmpty();
        }

        @Test
        @DisplayName("Should throw on null jarPath")
        void shouldThrowOnNullJarPath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> handler.findNestedJars(null));
        }

        @Test
        @DisplayName("Should throw on nonexistent JAR file")
        void shouldThrowOnNonexistentJar() {
            Path missing = tempDir.resolve("missing.jar");
            assertThatThrownBy(() -> handler.findNestedJars(missing))
                    .isInstanceOf(OpenClassLoaderException.class);
        }
    }

    @Nested
    @DisplayName("ExtractNestedJar Tests")
    class ExtractNestedJarTests {

        @Test
        @DisplayName("Should extract nested JAR to temp file")
        void shouldExtractNestedJarToTempFile() throws IOException {
            Path fatJar = createFatJar();
            Path extracted = handler.extractNestedJar(fatJar, "BOOT-INF/lib/inner.jar");

            assertThat(extracted).exists();
            assertThat(Files.size(extracted)).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should read content from extracted JAR")
        void shouldReadContentFromExtractedJar() throws IOException {
            Path fatJar = createFatJar();
            Path extracted = handler.extractNestedJar(fatJar, "BOOT-INF/lib/inner.jar");

            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(extracted.toFile())) {
                JarEntry entry = jar.getJarEntry("hello.txt");
                assertThat(entry).isNotNull();
                String content = new String(jar.getInputStream(entry).readAllBytes());
                assertThat(content).isEqualTo("hello-inner");
            }
        }

        @Test
        @DisplayName("Should return same path for duplicate extract (ref count)")
        void shouldReturnSamePathForDuplicateExtract() throws IOException {
            Path fatJar = createFatJar();
            Path first = handler.extractNestedJar(fatJar, "BOOT-INF/lib/inner.jar");
            Path second = handler.extractNestedJar(fatJar, "BOOT-INF/lib/inner.jar");

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("Should throw on nonexistent nested entry")
        void shouldThrowOnNonexistentNestedEntry() throws IOException {
            Path fatJar = createFatJar();

            assertThatThrownBy(() -> handler.extractNestedJar(fatJar, "BOOT-INF/lib/nonexistent.jar"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should throw on null parameters")
        void shouldThrowOnNullParameters() {
            assertThatNullPointerException()
                    .isThrownBy(() -> handler.extractNestedJar(null, "foo.jar"));

            assertThatNullPointerException()
                    .isThrownBy(() -> handler.extractNestedJar(Path.of("x.jar"), null));
        }
    }

    @Nested
    @DisplayName("Release Tests")
    class ReleaseTests {

        @Test
        @DisplayName("Should delete temp file when refCount reaches zero")
        void shouldDeleteWhenRefCountReachesZero() throws IOException {
            Path fatJar = createFatJar();
            Path extracted = handler.extractNestedJar(fatJar, "BOOT-INF/lib/inner.jar");

            assertThat(extracted).exists();
            handler.release(fatJar, "BOOT-INF/lib/inner.jar");
            assertThat(extracted).doesNotExist();
        }

        @Test
        @DisplayName("Should not delete temp file when refCount is still positive")
        void shouldNotDeleteWhenRefCountPositive() throws IOException {
            Path fatJar = createFatJar();
            Path extracted = handler.extractNestedJar(fatJar, "BOOT-INF/lib/inner.jar");
            // Extract again → refCount = 2
            handler.extractNestedJar(fatJar, "BOOT-INF/lib/inner.jar");

            handler.release(fatJar, "BOOT-INF/lib/inner.jar");
            assertThat(extracted).exists(); // refCount = 1, still alive

            handler.release(fatJar, "BOOT-INF/lib/inner.jar");
            assertThat(extracted).doesNotExist(); // refCount = 0, deleted
        }

        @Test
        @DisplayName("Should be no-op for unknown entry")
        void shouldBeNoOpForUnknownEntry() throws IOException {
            Path fatJar = createFatJar();
            // Should not throw
            handler.release(fatJar, "BOOT-INF/lib/unknown.jar");
        }
    }

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

        @Test
        @DisplayName("Should clean up all temp files on close")
        void shouldCleanUpAllTempFilesOnClose() throws IOException {
            Path fatJar = createFatJar();
            Path inner = handler.extractNestedJar(fatJar, "BOOT-INF/lib/inner.jar");
            Path web = handler.extractNestedJar(fatJar, "WEB-INF/lib/web.jar");

            assertThat(inner).exists();
            assertThat(web).exists();

            handler.close();

            assertThat(inner).doesNotExist();
            assertThat(web).doesNotExist();
        }

        @Test
        @DisplayName("Should mark handler as closed")
        void shouldMarkHandlerAsClosed() {
            assertThat(handler.isClosed()).isFalse();
            handler.close();
            assertThat(handler.isClosed()).isTrue();
        }

        @Test
        @DisplayName("Double close should be safe")
        void doubleCloseShouldBeSafe() {
            handler.close();
            assertThatNoException().isThrownBy(() -> handler.close());
        }

        @Test
        @DisplayName("Should throw on operations after close")
        void shouldThrowOnOperationsAfterClose() throws IOException {
            Path fatJar = createFatJar();
            handler.close();

            assertThatThrownBy(() -> handler.findNestedJars(fatJar))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");

            assertThatThrownBy(() -> handler.extractNestedJar(fatJar, "BOOT-INF/lib/inner.jar"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("Concurrency Tests")
    class ConcurrencyTests {

        @Test
        @DisplayName("Should handle concurrent extract safely")
        void shouldHandleConcurrentExtractSafely() throws Exception {
            Path fatJar = createFatJar();
            int threadCount = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            CopyOnWriteArrayList<Path> results = new CopyOnWriteArrayList<>();
            CopyOnWriteArrayList<Throwable> errors = new CopyOnWriteArrayList<>();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            try {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        try {
                            startLatch.await();
                            Path extracted = handler.extractNestedJar(fatJar, "BOOT-INF/lib/inner.jar");
                            results.add(extracted);
                        } catch (Throwable t) {
                            errors.add(t);
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }
                startLatch.countDown();
                assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
            } finally {
                executor.shutdownNow();
            }

            assertThat(errors).isEmpty();
            // All threads should get the same path
            assertThat(results).hasSize(threadCount);
            assertThat(results.stream().distinct().count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Path Traversal Tests")
    class PathTraversalTests {

        @Test
        @DisplayName("Should reject entry with path traversal")
        void shouldRejectEntryWithPathTraversal() throws IOException {
            Path fatJar = createFatJar();

            assertThatThrownBy(() -> handler.extractNestedJar(fatJar, "../../../etc/passwd"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("Path traversal");
        }

        @Test
        @DisplayName("Should reject entry with absolute path")
        void shouldRejectEntryWithAbsolutePath() throws IOException {
            Path fatJar = createFatJar();

            assertThatThrownBy(() -> handler.extractNestedJar(fatJar, "/etc/passwd"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("Path traversal");
        }
    }
}
