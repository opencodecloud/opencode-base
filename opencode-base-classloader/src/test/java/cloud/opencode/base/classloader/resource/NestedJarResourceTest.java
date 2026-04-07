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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for NestedJarResource
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("NestedJarResource Tests")
class NestedJarResourceTest {

    @TempDir
    Path tempDir;

    private NestedJarHandler handler;
    private Path fatJar;

    @BeforeEach
    void setUp() throws IOException {
        handler = NestedJarHandler.builder()
                .tempDirectory(tempDir.resolve("nested-work"))
                .build();
        fatJar = createFatJar();
    }

    @AfterEach
    void tearDown() {
        if (handler != null && !handler.isClosed()) {
            handler.close();
        }
    }

    // ==================== Helper ====================

    private Path createFatJar() throws IOException {
        byte[] innerJarBytes = buildInnerJar();
        Path jar = tempDir.resolve("fat.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar.toFile()))) {
            // directory entry
            jos.putNextEntry(new JarEntry("BOOT-INF/lib/"));
            jos.closeEntry();
            // nested JAR
            JarEntry entry = new JarEntry("BOOT-INF/lib/inner.jar");
            jos.putNextEntry(entry);
            jos.write(innerJarBytes);
            jos.closeEntry();
        }
        return jar;
    }

    /**
     * Builds an inner JAR containing:
     *   com/example/App.class  → "fake-class-bytes"
     *   config.properties      → "app.name=test"
     */
    private byte[] buildInnerJar() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (JarOutputStream jos = new JarOutputStream(baos)) {
            jos.putNextEntry(new JarEntry("com/example/App.class"));
            jos.write("fake-class-bytes".getBytes());
            jos.closeEntry();

            jos.putNextEntry(new JarEntry("config.properties"));
            jos.write("app.name=test".getBytes());
            jos.closeEntry();
        }
        return baos.toByteArray();
    }

    // ==================== Tests ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should throw on null handler")
        void shouldThrowOnNullHandler() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new NestedJarResource(null, fatJar, "BOOT-INF/lib/inner.jar", "test.txt"));
        }

        @Test
        @DisplayName("Should throw on null outerJarPath")
        void shouldThrowOnNullOuterJarPath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new NestedJarResource(handler, null, "BOOT-INF/lib/inner.jar", "test.txt"));
        }

        @Test
        @DisplayName("Should throw on null nestedJarEntry")
        void shouldThrowOnNullNestedJarEntry() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new NestedJarResource(handler, fatJar, null, "test.txt"));
        }

        @Test
        @DisplayName("Should throw on null resourceEntry")
        void shouldThrowOnNullResourceEntry() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new NestedJarResource(handler, fatJar, "BOOT-INF/lib/inner.jar", null));
        }
    }

    @Nested
    @DisplayName("Exists Tests")
    class ExistsTests {

        @Test
        @DisplayName("Should exist for valid resource entry")
        void shouldExistForValidResourceEntry() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "com/example/App.class");

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should exist for properties entry")
        void shouldExistForPropertiesEntry() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should not exist for missing entry")
        void shouldNotExistForMissingEntry() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "nonexistent.txt");

            assertThat(resource.exists()).isFalse();
        }

        @Test
        @DisplayName("Should not exist for missing nested JAR")
        void shouldNotExistForMissingNestedJar() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/missing.jar", "test.txt");

            assertThat(resource.exists()).isFalse();
        }

        @Test
        @DisplayName("Should be readable if exists")
        void shouldBeReadableIfExists() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(resource.isReadable()).isTrue();
        }

        @Test
        @DisplayName("Should not be file")
        void shouldNotBeFile() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(resource.isFile()).isFalse();
        }
    }

    @Nested
    @DisplayName("GetInputStream Tests")
    class GetInputStreamTests {

        @Test
        @DisplayName("Should read class bytes from nested JAR")
        void shouldReadClassBytesFromNestedJar() throws IOException {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "com/example/App.class");

            try (InputStream is = resource.getInputStream()) {
                String content = new String(is.readAllBytes());
                assertThat(content).isEqualTo("fake-class-bytes");
            }
        }

        @Test
        @DisplayName("Should read properties from nested JAR")
        void shouldReadPropertiesFromNestedJar() throws IOException {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(resource.getString()).isEqualTo("app.name=test");
        }

        @Test
        @DisplayName("Should read bytes from nested JAR")
        void shouldReadBytesFromNestedJar() throws IOException {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(resource.getBytes()).isEqualTo("app.name=test".getBytes());
        }

        @Test
        @DisplayName("Should throw on missing resource entry")
        void shouldThrowOnMissingResourceEntry() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "nonexistent.txt");

            assertThatThrownBy(resource::getInputStream)
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should throw on missing nested JAR entry")
        void shouldThrowOnMissingNestedJarEntry() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/missing.jar", "test.txt");

            assertThatThrownBy(resource::getInputStream)
                    .isInstanceOf(OpenClassLoaderException.class);
        }
    }

    @Nested
    @DisplayName("URL Tests")
    class UrlTests {

        @Test
        @DisplayName("Should return URL with nested jar protocol")
        void shouldReturnUrlWithNestedJarProtocol() throws IOException {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "com/example/App.class");

            URL url = resource.getUrl();
            assertThat(url).isNotNull();
            assertThat(url.toString()).contains("BOOT-INF/lib/inner.jar");
            assertThat(url.toString()).contains("com/example/App.class");
        }

        @Test
        @DisplayName("Should return URI")
        void shouldReturnUri() throws IOException {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(resource.getUri()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("Should return filename from resource entry")
        void shouldReturnFilenameFromResourceEntry() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "com/example/App.class");

            assertThat(resource.getFilename()).isEqualTo("App.class");
        }

        @Test
        @DisplayName("Should return filename for top-level entry")
        void shouldReturnFilenameForTopLevelEntry() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(resource.getFilename()).isEqualTo("config.properties");
        }

        @Test
        @DisplayName("Should return description with nested notation")
        void shouldReturnDescriptionWithNestedNotation() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "com/example/App.class");

            String desc = resource.getDescription();
            assertThat(desc).contains("nested jar resource");
            assertThat(desc).contains("BOOT-INF/lib/inner.jar");
            assertThat(desc).contains("com/example/App.class");
        }

        @Test
        @DisplayName("Should return empty file and path")
        void shouldReturnEmptyFileAndPath() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(resource.getFile()).isEmpty();
            assertThat(resource.getPath()).isEmpty();
        }

        @Test
        @DisplayName("Should return content length")
        void shouldReturnContentLength() throws IOException {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(resource.contentLength()).isEqualTo("app.name=test".length());
        }

        @Test
        @DisplayName("toString should equal description")
        void toStringShouldEqualDescription() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(resource.toString()).isEqualTo(resource.getDescription());
        }
    }

    @Nested
    @DisplayName("Accessor Tests")
    class AccessorTests {

        @Test
        @DisplayName("Should return outer JAR path")
        void shouldReturnOuterJarPath() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "test.txt");

            assertThat(resource.getOuterJarPath()).isEqualTo(fatJar.toAbsolutePath());
        }

        @Test
        @DisplayName("Should return nested JAR entry")
        void shouldReturnNestedJarEntry() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "test.txt");

            assertThat(resource.getNestedJarEntry()).isEqualTo("BOOT-INF/lib/inner.jar");
        }

        @Test
        @DisplayName("Should return resource entry")
        void shouldReturnResourceEntry() {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "com/example/App.class");

            assertThat(resource.getResourceEntry()).isEqualTo("com/example/App.class");
        }
    }

    @Nested
    @DisplayName("CreateRelative Tests")
    class CreateRelativeTests {

        @Test
        @DisplayName("Should create relative resource in same directory")
        void shouldCreateRelativeResourceInSameDirectory() throws IOException {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "com/example/App.class");

            Resource relative = resource.createRelative("Other.class");
            assertThat(relative).isInstanceOf(NestedJarResource.class);

            NestedJarResource nestedRelative = (NestedJarResource) relative;
            assertThat(nestedRelative.getResourceEntry()).isEqualTo("com/example/Other.class");
        }

        @Test
        @DisplayName("Should create relative resource from root entry")
        void shouldCreateRelativeResourceFromRootEntry() throws IOException {
            NestedJarResource resource = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            Resource relative = resource.createRelative("other.properties");
            assertThat(relative).isInstanceOf(NestedJarResource.class);

            NestedJarResource nestedRelative = (NestedJarResource) relative;
            assertThat(nestedRelative.getResourceEntry()).isEqualTo("other.properties");
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal for same description")
        void shouldBeEqualForSameDescription() {
            NestedJarResource r1 = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");
            NestedJarResource r2 = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different resource entries")
        void shouldNotBeEqualForDifferentResourceEntries() {
            NestedJarResource r1 = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "config.properties");
            NestedJarResource r2 = new NestedJarResource(
                    handler, fatJar, "BOOT-INF/lib/inner.jar", "com/example/App.class");

            assertThat(r1).isNotEqualTo(r2);
        }
    }
}
