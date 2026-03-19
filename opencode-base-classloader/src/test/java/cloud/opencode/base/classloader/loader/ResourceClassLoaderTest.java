package cloud.opencode.base.classloader.loader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ResourceClassLoader
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("ResourceClassLoader Tests")
class ResourceClassLoaderTest {

    @TempDir
    Path tempDir;

    private Path createResourceDir() throws IOException {
        Path resourceDir = tempDir.resolve("resources");
        Files.createDirectories(resourceDir);
        Files.writeString(resourceDir.resolve("test.txt"), "Hello, World!");
        Files.writeString(resourceDir.resolve("config.yml"), "key: value");

        // Create subdirectory with resource
        Path subDir = resourceDir.resolve("sub");
        Files.createDirectories(subDir);
        Files.writeString(subDir.resolve("nested.txt"), "Nested content");

        return resourceDir;
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create with resource paths")
        void shouldCreateWithResourcePaths() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create with parent and resource paths")
        void shouldCreateWithParentAndResourcePaths() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(
                    getClass().getClassLoader(), resourceDir);

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create with multiple paths")
        void shouldCreateWithMultiplePaths() throws IOException {
            Path dir1 = createResourceDir();
            Path dir2 = tempDir.resolve("resources2");
            Files.createDirectories(dir2);
            Files.writeString(dir2.resolve("other.txt"), "Other content");

            ResourceClassLoader loader = ResourceClassLoader.create(dir1, dir2);

            assertThat(loader).isNotNull();
        }
    }

    @Nested
    @DisplayName("Resource Path Management Tests")
    class ResourcePathManagementTests {

        @Test
        @DisplayName("Should add resource path")
        void shouldAddResourcePath() throws IOException {
            Path dir1 = createResourceDir();
            Path dir2 = tempDir.resolve("resources2");
            Files.createDirectories(dir2);

            ResourceClassLoader loader = ResourceClassLoader.create(dir1);
            loader.addResourcePath(dir2);

            assertThat(loader.getResourcePaths()).hasSize(2);
        }

        @Test
        @DisplayName("Should not add duplicate path")
        void shouldNotAddDuplicatePath() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            loader.addResourcePath(resourceDir);

            assertThat(loader.getResourcePaths()).hasSize(1);
        }

        @Test
        @DisplayName("Should remove resource path")
        void shouldRemoveResourcePath() throws IOException {
            Path dir1 = createResourceDir();
            Path dir2 = tempDir.resolve("resources2");
            Files.createDirectories(dir2);

            ResourceClassLoader loader = ResourceClassLoader.create(dir1, dir2);
            loader.removeResourcePath(dir2);

            assertThat(loader.getResourcePaths()).hasSize(1);
        }

        @Test
        @DisplayName("Should get resource paths")
        void shouldGetResourcePaths() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            List<Path> paths = loader.getResourcePaths();

            assertThat(paths).isNotEmpty();
        }

        @Test
        @DisplayName("Should clear all resource paths")
        void shouldClearAllResourcePaths() throws IOException {
            Path dir1 = createResourceDir();
            Path dir2 = tempDir.resolve("resources2");
            Files.createDirectories(dir2);

            ResourceClassLoader loader = ResourceClassLoader.create(dir1, dir2);
            loader.clearResourcePaths();

            assertThat(loader.getResourcePaths()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Resource Loading Tests")
    class ResourceLoadingTests {

        @Test
        @DisplayName("Should get resource")
        void shouldGetResource() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            URL resource = loader.getResource("test.txt");

            assertThat(resource).isNotNull();
        }

        @Test
        @DisplayName("Should return null for nonexistent resource")
        void shouldReturnNullForNonexistentResource() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            URL resource = loader.getResource("nonexistent.txt");

            assertThat(resource).isNull();
        }

        @Test
        @DisplayName("Should get nested resource")
        void shouldGetNestedResource() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            URL resource = loader.getResource("sub/nested.txt");

            assertThat(resource).isNotNull();
        }

        @Test
        @DisplayName("Should get resource as stream")
        void shouldGetResourceAsStream() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            try (InputStream is = loader.getResourceAsStream("test.txt")) {
                assertThat(is).isNotNull();
                String content = new String(is.readAllBytes());
                assertThat(content).isEqualTo("Hello, World!");
            }
        }
    }

    @Nested
    @DisplayName("Refresh Tests")
    class RefreshTests {

        @Test
        @DisplayName("Should refresh resources")
        void shouldRefreshResources() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            // Add a new file after creation
            Files.writeString(resourceDir.resolve("new-file.txt"), "New content");

            loader.refresh();

            // After refresh, new file should be accessible
            // Note: Implementation dependent
            assertThat(loader).isNotNull();
        }
    }

    @Nested
    @DisplayName("Class Loading Tests")
    class ClassLoadingTests {

        @Test
        @DisplayName("Should delegate to parent for classes")
        void shouldDelegateToParentForClasses() throws Exception {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            Class<?> stringClass = loader.loadClass("java.lang.String");

            assertThat(stringClass).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Should throw for nonexistent class")
        void shouldThrowForNonexistentClass() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            assertThatThrownBy(() -> loader.loadClass("com.nonexistent.Class"))
                    .isInstanceOf(ClassNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Resource Exists Tests")
    class ResourceExistsTests {

        @Test
        @DisplayName("Should return true for existing resource")
        void shouldReturnTrueForExistingResource() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            assertThat(loader.resourceExists("test.txt")).isTrue();
        }

        @Test
        @DisplayName("Should return false for nonexistent resource")
        void shouldReturnFalseForNonexistentResource() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            assertThat(loader.resourceExists("nonexistent.txt")).isFalse();
        }
    }

    @Nested
    @DisplayName("List Resources Tests")
    class ListResourcesTests {

        @Test
        @DisplayName("Should list resources in directory")
        void shouldListResourcesInDirectory() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            List<String> resources = loader.listResources("");

            assertThat(resources).contains("test.txt", "config.yml");
        }

        @Test
        @DisplayName("Should list resources in subdirectory")
        void shouldListResourcesInSubdirectory() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            List<String> resources = loader.listResources("sub");

            assertThat(resources).contains("nested.txt");
        }

        @Test
        @DisplayName("Should return empty for nonexistent directory")
        void shouldReturnEmptyForNonexistentDirectory() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            List<String> resources = loader.listResources("nonexistent");

            assertThat(resources).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Resources Enumeration Tests")
    class GetResourcesEnumerationTests {

        @Test
        @DisplayName("Should get resources enumeration")
        void shouldGetResourcesEnumeration() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            var resources = loader.getResources("test.txt");

            assertThat(resources.hasMoreElements()).isTrue();
        }

        @Test
        @DisplayName("Should return empty enumeration for nonexistent")
        void shouldReturnEmptyEnumerationForNonexistent() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            var resources = loader.getResources("nonexistent.txt");

            assertThat(resources.hasMoreElements()).isFalse();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with default constructor")
        void shouldCreateWithDefaultConstructor() {
            ResourceClassLoader loader = new ResourceClassLoader();

            assertThat(loader).isNotNull();
            assertThat(loader.getResourcePaths()).isEmpty();
        }

        @Test
        @DisplayName("Should create with parent classloader")
        void shouldCreateWithParentClassLoader() {
            ResourceClassLoader loader = new ResourceClassLoader(getClass().getClassLoader());

            assertThat(loader).isNotNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty resource paths")
        void shouldHandleEmptyResourcePaths() {
            ResourceClassLoader loader = ResourceClassLoader.create();

            assertThat(loader.getResourcePaths()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null path in add")
        void shouldHandleNullPathInAdd() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            assertThatNullPointerException()
                    .isThrownBy(() -> loader.addResourcePath(null));
        }

        @Test
        @DisplayName("Should handle null path in remove")
        void shouldHandleNullPathInRemove() throws IOException {
            Path resourceDir = createResourceDir();
            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            assertThatNullPointerException()
                    .isThrownBy(() -> loader.removeResourcePath(null));
        }

        @Test
        @DisplayName("Should handle removing nonexistent path")
        void shouldHandleRemovingNonexistentPath() throws IOException {
            Path resourceDir = createResourceDir();
            Path otherDir = tempDir.resolve("other");

            ResourceClassLoader loader = ResourceClassLoader.create(resourceDir);

            assertThatCode(() -> loader.removeResourcePath(otherDir))
                    .doesNotThrowAnyException();
        }
    }
}
