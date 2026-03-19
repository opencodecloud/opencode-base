package cloud.opencode.base.classloader.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ResourceLoader
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("ResourceLoader Tests")
class ResourceLoaderTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create with default classloader")
        void shouldCreateWithDefaultClassLoader() {
            ResourceLoader loader = ResourceLoader.create();

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create with custom classloader")
        void shouldCreateWithCustomClassLoader() {
            ResourceLoader loader = ResourceLoader.create(getClass().getClassLoader());

            assertThat(loader).isNotNull();
        }
    }

    @Nested
    @DisplayName("ClassPath Loading Tests")
    class ClassPathLoadingTests {

        @Test
        @DisplayName("Should load classpath resource")
        void shouldLoadClassPathResource() {
            ResourceLoader loader = ResourceLoader.create();
            Resource resource = loader.load("classpath:test.txt");

            assertThat(resource).isInstanceOf(ClassPathResource.class);
            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should load classpath resource without prefix")
        void shouldLoadClassPathResourceWithoutPrefix() {
            ResourceLoader loader = ResourceLoader.create();
            Resource resource = loader.load("test.txt");

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should load multiple classpath resources")
        void shouldLoadMultipleClassPathResources() {
            ResourceLoader loader = ResourceLoader.create();
            List<Resource> resources = loader.loadAll("classpath*:*.txt");

            assertThat(resources).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("File Loading Tests")
    class FileLoadingTests {

        @Test
        @DisplayName("Should load file resource")
        void shouldLoadFileResource() throws IOException {
            Path file = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            ResourceLoader loader = ResourceLoader.create();

            Resource resource = loader.load("file:" + file);

            assertThat(resource).isInstanceOf(FileResource.class);
            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should load nonexistent file resource")
        void shouldLoadNonexistentFileResource() {
            ResourceLoader loader = ResourceLoader.create();
            Resource resource = loader.load("file:" + tempDir.resolve("nonexistent.txt"));

            assertThat(resource).isInstanceOf(FileResource.class);
            assertThat(resource.exists()).isFalse();
        }
    }

    @Nested
    @DisplayName("URL Loading Tests")
    class UrlLoadingTests {

        @Test
        @DisplayName("Should load URL resource")
        void shouldLoadUrlResource() throws IOException {
            Path file = Files.writeString(tempDir.resolve("test.txt"), "Hello");
            ResourceLoader loader = ResourceLoader.create();

            Resource resource = loader.load("url:" + file.toUri());

            assertThat(resource).isInstanceOf(UrlResource.class);
            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("Should load HTTP URL resource")
        void shouldLoadHttpUrlResource() {
            ResourceLoader loader = ResourceLoader.create();
            Resource resource = loader.load("url:https://example.com");

            assertThat(resource).isInstanceOf(UrlResource.class);
        }
    }

    @Nested
    @DisplayName("Pattern Loading Tests")
    class PatternLoadingTests {

        @Test
        @DisplayName("Should load all matching resources")
        void shouldLoadAllMatchingResources() {
            ResourceLoader loader = ResourceLoader.create();
            List<Resource> resources = loader.loadAll("classpath*:*.txt");

            assertThat(resources).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty list for no matches")
        void shouldReturnEmptyListForNoMatches() {
            ResourceLoader loader = ResourceLoader.create();
            List<Resource> resources = loader.loadAll("classpath*:nonexistent-pattern-*.xyz");

            assertThat(resources).isEmpty();
        }
    }

    @Nested
    @DisplayName("ClassLoader Configuration Tests")
    class ClassLoaderConfigurationTests {

        @Test
        @DisplayName("Should use custom classloader")
        void shouldUseCustomClassLoader() {
            ClassLoader customLoader = new ClassLoader() {};
            ResourceLoader loader = ResourceLoader.create(customLoader);

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should chain classloader configuration")
        void shouldChainClassLoaderConfiguration() {
            ResourceLoader loader = ResourceLoader.create()
                    .classLoader(getClass().getClassLoader());

            assertThat(loader).isNotNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null location")
        void shouldHandleNullLocation() {
            ResourceLoader loader = ResourceLoader.create();

            assertThatNullPointerException()
                    .isThrownBy(() -> loader.load(null));
        }

        @Test
        @DisplayName("Should handle empty location")
        void shouldHandleEmptyLocation() {
            ResourceLoader loader = ResourceLoader.create();
            Resource resource = loader.load("");

            // Empty location may or may not exist depending on implementation
            assertThat(resource).isNotNull();
        }

        @Test
        @DisplayName("Should handle location with only protocol")
        void shouldHandleLocationWithOnlyProtocol() {
            ResourceLoader loader = ResourceLoader.create();
            Resource resource = loader.load("classpath:");

            // Protocol-only location may or may not exist depending on implementation
            assertThat(resource).isNotNull();
        }
    }
}
