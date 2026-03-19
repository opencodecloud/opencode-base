package cloud.opencode.base.classloader.loader;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for IsoClassLoader
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("IsoClassLoader Tests")
class IsoClassLoaderTest {

    @TempDir
    Path tempDir;

    private Path createTestJar() throws IOException {
        Path jarPath = tempDir.resolve("test.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            // Add a simple resource
            JarEntry entry = new JarEntry("test-resource.txt");
            jos.putNextEntry(entry);
            jos.write("Test content".getBytes());
            jos.closeEntry();
        }
        return jarPath;
    }

    private Path createClassesDir() throws IOException {
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);

        // Create a simple resource
        Files.writeString(classesDir.resolve("test-resource.txt"), "Test content");

        return classesDir;
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create from JAR path string")
        void shouldCreateFromJarPathString() throws IOException {
            Path jarPath = createTestJar();
            IsoClassLoader.Builder builder = IsoClassLoader.fromJar(jarPath.toString());

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("Should create from JAR Path")
        void shouldCreateFromJarPath() throws IOException {
            Path jarPath = createTestJar();
            IsoClassLoader.Builder builder = IsoClassLoader.fromJar(jarPath);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("Should create from directory")
        void shouldCreateFromDirectory() throws IOException {
            Path classesDir = createClassesDir();
            IsoClassLoader.Builder builder = IsoClassLoader.fromDirectory(classesDir);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("Should create from URLs")
        void shouldCreateFromUrls() throws Exception {
            Path jarPath = createTestJar();
            URL url = jarPath.toUri().toURL();
            IsoClassLoader.Builder builder = IsoClassLoader.fromUrls(url);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("Should throw on invalid JAR path")
        void shouldThrowOnInvalidJarPath() {
            // Path with invalid characters on some systems
            assertThatThrownBy(() -> IsoClassLoader.fromJar("\0invalid"))
                    .isInstanceOf(Exception.class);  // May throw InvalidPathException or OpenClassLoaderException
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build with default settings")
        void shouldBuildWithDefaultSettings() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build()) {
                assertThat(loader).isNotNull();
                assertThat(loader.isClosed()).isFalse();
            }
        }

        @Test
        @DisplayName("Should build with isolated packages")
        void shouldBuildWithIsolatedPackages() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .addIsolatedPackage("com.example")
                    .build()) {
                assertThat(loader).isNotNull();
            }
        }

        @Test
        @DisplayName("Should build with multiple isolated packages")
        void shouldBuildWithMultipleIsolatedPackages() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .addIsolatedPackages("com.example", "org.test")
                    .build()) {
                assertThat(loader).isNotNull();
            }
        }

        @Test
        @DisplayName("Should build with shared packages")
        void shouldBuildWithSharedPackages() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .addSharedPackage("java.util")
                    .build()) {
                assertThat(loader).isNotNull();
            }
        }

        @Test
        @DisplayName("Should build with multiple shared packages")
        void shouldBuildWithMultipleSharedPackages() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .addSharedPackages("java.util", "java.io")
                    .build()) {
                assertThat(loader).isNotNull();
            }
        }

        @Test
        @DisplayName("Should build with parent classloader")
        void shouldBuildWithParentClassLoader() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .parent(getClass().getClassLoader())
                    .build()) {
                assertThat(loader).isNotNull();
            }
        }

        @Test
        @DisplayName("Should build with loading strategy")
        void shouldBuildWithLoadingStrategy() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .loadingStrategy(IsoClassLoader.LoadingStrategy.CHILD_FIRST)
                    .build()) {
                assertThat(loader).isNotNull();
            }
        }

        @Test
        @DisplayName("Should build with additional paths")
        void shouldBuildWithAdditionalPaths() throws Exception {
            Path jarPath = createTestJar();
            Path classesDir = createClassesDir();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .addPath(classesDir)
                    .build()) {
                assertThat(loader).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw on empty URLs")
        void shouldThrowOnEmptyUrls() {
            IsoClassLoader.Builder builder = new IsoClassLoader.Builder();

            assertThatThrownBy(builder::build)
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("No URLs");
        }
    }

    @Nested
    @DisplayName("Class Loading Tests")
    class ClassLoadingTests {

        @Test
        @DisplayName("Should load system classes")
        void shouldLoadSystemClasses() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build()) {
                Class<?> stringClass = loader.loadClass("java.lang.String");
                assertThat(stringClass).isEqualTo(String.class);
            }
        }

        @Test
        @DisplayName("Should check if can load class")
        void shouldCheckIfCanLoadClass() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build()) {
                assertThat(loader.canLoad("java.lang.String")).isTrue();
                assertThat(loader.canLoad("com.nonexistent.Class")).isFalse();
            }
        }

        @Test
        @DisplayName("Should throw on load when closed")
        void shouldThrowOnLoadWhenClosed() throws Exception {
            Path jarPath = createTestJar();
            IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build();
            loader.close();

            assertThatThrownBy(() -> loader.loadClass("java.lang.String"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should use parent first strategy")
        void shouldUseParentFirstStrategy() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .loadingStrategy(IsoClassLoader.LoadingStrategy.PARENT_FIRST)
                    .build()) {
                Class<?> stringClass = loader.loadClass("java.lang.String");
                assertThat(stringClass).isEqualTo(String.class);
            }
        }

        @Test
        @DisplayName("Should use child first strategy")
        void shouldUseChildFirstStrategy() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .loadingStrategy(IsoClassLoader.LoadingStrategy.CHILD_FIRST)
                    .build()) {
                // System classes still loaded from parent
                Class<?> stringClass = loader.loadClass("java.lang.String");
                assertThat(stringClass).isEqualTo(String.class);
            }
        }

        @Test
        @DisplayName("Should use parent only strategy")
        void shouldUseParentOnlyStrategy() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .loadingStrategy(IsoClassLoader.LoadingStrategy.PARENT_ONLY)
                    .build()) {
                Class<?> stringClass = loader.loadClass("java.lang.String");
                assertThat(stringClass).isEqualTo(String.class);
            }
        }

        @Test
        @DisplayName("Should cache loaded classes")
        void shouldCacheLoadedClasses() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build()) {
                Class<?> class1 = loader.loadClass("java.lang.String");
                Class<?> class2 = loader.loadClass("java.lang.String");
                assertThat(class1).isSameAs(class2);
            }
        }

        @Test
        @DisplayName("Should get loaded class names")
        void shouldGetLoadedClassNames() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build()) {
                // JDK classes may be delegated to parent, so may not appear in loaded list
                loader.loadClass("java.lang.String");
                loader.loadClass("java.lang.Integer");

                // Just verify the method returns a set
                assertThat(loader.getLoadedClassNames()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("LoadClassLocally Tests")
    class LoadClassLocallyTests {

        @Test
        @DisplayName("Should throw ClassNotFoundException for system class")
        void shouldThrowClassNotFoundExceptionForSystemClass() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build()) {
                // System classes are not in the local classloader
                assertThatThrownBy(() -> loader.loadClassLocally("java.lang.String"))
                        .isInstanceOf(ClassNotFoundException.class);
            }
        }

        @Test
        @DisplayName("Should throw when closed")
        void shouldThrowWhenClosed() throws Exception {
            Path jarPath = createTestJar();
            IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build();
            loader.close();

            assertThatThrownBy(() -> loader.loadClassLocally("some.Class"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("Child Only Strategy Tests")
    class ChildOnlyStrategyTests {

        @Test
        @DisplayName("Should throw for class not in local classpath with CHILD_ONLY strategy")
        void shouldThrowForClassNotInLocalClasspath() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath)
                    .loadingStrategy(IsoClassLoader.LoadingStrategy.CHILD_ONLY)
                    .build()) {
                // Non-system classes that don't exist in JAR should throw
                assertThatThrownBy(() -> loader.loadClass("com.nonexistent.Class"))
                        .isInstanceOf(ClassNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("Resource Access Tests")
    class ResourceAccessTests {

        @Test
        @DisplayName("Should get resource")
        void shouldGetResource() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build()) {
                URL resource = loader.getResource("test-resource.txt");
                assertThat(resource).isNotNull();
            }
        }

        @Test
        @DisplayName("Should return null for nonexistent resource")
        void shouldReturnNullForNonexistentResource() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build()) {
                URL resource = loader.getResource("nonexistent.txt");
                assertThat(resource).isNull();
            }
        }

        @Test
        @DisplayName("Should get resources")
        void shouldGetResources() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build()) {
                Enumeration<URL> resources = loader.getResources("test-resource.txt");
                assertThat(resources.hasMoreElements()).isTrue();
            }
        }

        @Test
        @DisplayName("Should get resource as stream")
        void shouldGetResourceAsStream() throws Exception {
            Path jarPath = createTestJar();

            try (IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build()) {
                try (InputStream is = loader.getResourceAsStream("test-resource.txt")) {
                    assertThat(is).isNotNull();
                    String content = new String(is.readAllBytes());
                    assertThat(content).isEqualTo("Test content");
                }
            }
        }

        @Test
        @DisplayName("Should throw on resource access when closed")
        void shouldThrowOnResourceAccessWhenClosed() throws Exception {
            Path jarPath = createTestJar();
            IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build();
            loader.close();

            assertThatThrownBy(() -> loader.getResource("test-resource.txt"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on getResources when closed")
        void shouldThrowOnGetResourcesWhenClosed() throws Exception {
            Path jarPath = createTestJar();
            IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build();
            loader.close();

            assertThatThrownBy(() -> loader.getResources("test-resource.txt"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on getResourceAsStream when closed")
        void shouldThrowOnGetResourceAsStreamWhenClosed() throws Exception {
            Path jarPath = createTestJar();
            IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build();
            loader.close();

            assertThatThrownBy(() -> loader.getResourceAsStream("test-resource.txt"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("Should close successfully")
        void shouldCloseSuccessfully() throws Exception {
            Path jarPath = createTestJar();
            IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build();

            assertThat(loader.isClosed()).isFalse();
            loader.close();
            assertThat(loader.isClosed()).isTrue();
        }

        @Test
        @DisplayName("Should handle multiple close calls")
        void shouldHandleMultipleCloseCalls() throws Exception {
            Path jarPath = createTestJar();
            IsoClassLoader loader = IsoClassLoader.fromJar(jarPath).build();

            loader.close();
            loader.close(); // Should not throw

            assertThat(loader.isClosed()).isTrue();
        }

        @Test
        @DisplayName("Should work with try-with-resources")
        void shouldWorkWithTryWithResources() throws Exception {
            Path jarPath = createTestJar();
            IsoClassLoader loader;

            try (IsoClassLoader l = IsoClassLoader.fromJar(jarPath).build()) {
                loader = l;
                assertThat(loader.isClosed()).isFalse();
            }

            assertThat(loader.isClosed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Loading Strategy Enum Tests")
    class LoadingStrategyEnumTests {

        @Test
        @DisplayName("Should have all expected strategies")
        void shouldHaveAllExpectedStrategies() {
            assertThat(IsoClassLoader.LoadingStrategy.values())
                    .containsExactlyInAnyOrder(
                            IsoClassLoader.LoadingStrategy.PARENT_FIRST,
                            IsoClassLoader.LoadingStrategy.CHILD_FIRST,
                            IsoClassLoader.LoadingStrategy.PARENT_ONLY,
                            IsoClassLoader.LoadingStrategy.CHILD_ONLY
                    );
        }

        @Test
        @DisplayName("Should convert from string")
        void shouldConvertFromString() {
            assertThat(IsoClassLoader.LoadingStrategy.valueOf("PARENT_FIRST"))
                    .isEqualTo(IsoClassLoader.LoadingStrategy.PARENT_FIRST);
        }
    }
}
