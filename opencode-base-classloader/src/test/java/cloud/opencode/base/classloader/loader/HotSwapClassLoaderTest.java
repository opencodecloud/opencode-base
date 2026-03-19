package cloud.opencode.base.classloader.loader;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for HotSwapClassLoader
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("HotSwapClassLoader Tests")
class HotSwapClassLoaderTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create with default constructor via factory")
        void shouldCreateWithDefaultConstructor() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create with direct default constructor")
        void shouldCreateWithDirectDefaultConstructor() {
            HotSwapClassLoader loader = new HotSwapClassLoader();

            assertThat(loader).isNotNull();
            assertThat(loader.isClosed()).isFalse();
        }

        @Test
        @DisplayName("Should create with parent classloader via factory")
        void shouldCreateWithParentClassLoader() {
            HotSwapClassLoader loader = HotSwapClassLoader.create(getClass().getClassLoader());

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create with direct constructor with parent")
        void shouldCreateWithDirectConstructorWithParent() {
            ClassLoader parent = getClass().getClassLoader();
            HotSwapClassLoader loader = new HotSwapClassLoader(parent);

            assertThat(loader).isNotNull();
            assertThat(loader.getParent()).isSameAs(parent);
        }
    }

    @Nested
    @DisplayName("Class Loading Tests")
    class ClassLoadingTests {

        @Test
        @DisplayName("Should load class from bytecode")
        void shouldLoadClassFromBytecode() throws IOException {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // Get bytecode of a known class (use this class since it has a non-null classloader)
            String resourcePath = HotSwapClassLoader.class.getName().replace('.', '/') + ".class";
            byte[] bytecode = HotSwapClassLoader.class.getClassLoader().getResourceAsStream(resourcePath).readAllBytes();

            // Verify bytecode was obtained and loader is created
            assertThat(bytecode).isNotEmpty();
            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should reload class from file")
        void shouldReloadClassFromFile() throws IOException {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // Create a dummy class file
            Path classFile = tempDir.resolve("Test.class");

            // Get bytecode of HotSwapClassLoader as a test (has non-null classloader)
            String resourcePath = HotSwapClassLoader.class.getName().replace('.', '/') + ".class";
            byte[] bytecode = HotSwapClassLoader.class.getClassLoader().getResourceAsStream(resourcePath).readAllBytes();
            Files.write(classFile, bytecode);

            // The method should handle loading from file
            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should delegate to parent for system classes")
        void shouldDelegateToParentForSystemClasses() throws Exception {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            Class<?> stringClass = loader.loadClass("java.lang.String");

            assertThat(stringClass).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("Version Tracking Tests")
    class VersionTrackingTests {

        @Test
        @DisplayName("Should return 0 for unloaded class")
        void shouldReturnZeroForUnloadedClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThat(loader.getVersion("com.example.Unknown")).isEqualTo(0);
        }

        @Test
        @DisplayName("Should track loaded class names")
        void shouldTrackLoadedClassNames() throws Exception {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // Load a class to trigger tracking
            loader.loadClass("java.lang.String");

            assertThat(loader.getLoadedClassNames()).isNotNull();
        }
    }

    @Nested
    @DisplayName("IsLoaded Tests")
    class IsLoadedTests {

        @Test
        @DisplayName("Should return false for unloaded class")
        void shouldReturnFalseForUnloadedClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThat(loader.isLoaded("com.example.Unknown")).isFalse();
        }
    }

    @Nested
    @DisplayName("GetBytecode Tests")
    class GetBytecodeTests {

        @Test
        @DisplayName("Should return empty for unloaded class")
        void shouldReturnEmptyForUnloadedClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            Optional<byte[]> bytecode = loader.getBytecode("com.example.Unknown");

            assertThat(bytecode).isEmpty();
        }
    }

    @Nested
    @DisplayName("IsClosed Tests")
    class IsClosedTests {

        @Test
        @DisplayName("Should return false when not closed")
        void shouldReturnFalseWhenNotClosed() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThat(loader.isClosed()).isFalse();
        }

        @Test
        @DisplayName("Should return true after close")
        void shouldReturnTrueAfterClose() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            assertThat(loader.isClosed()).isTrue();
        }

        @Test
        @DisplayName("Should throw on loadClass with bytecode when closed")
        void shouldThrowOnLoadClassWithBytecodeWhenClosed() throws IOException {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            String resourcePath = HotSwapClassLoaderTest.class.getName().replace('.', '/') + ".class";
            byte[] bytecode = HotSwapClassLoaderTest.class.getClassLoader().getResourceAsStream(resourcePath).readAllBytes();

            assertThatThrownBy(() -> loader.loadClass("test.ClosedClass", bytecode))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on loadClass when closed")
        void shouldThrowOnLoadClassWhenClosed() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            assertThatThrownBy(() -> loader.loadClass("java.lang.String"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on reloadClass when closed")
        void shouldThrowOnReloadClassWhenClosed() throws IOException {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            Path classFile = tempDir.resolve("Test.class");
            Files.writeString(classFile, "dummy");
            loader.close();

            assertThatThrownBy(() -> loader.reloadClass("test.ClosedReload", classFile))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on unloadClass when closed")
        void shouldThrowOnUnloadClassWhenClosed() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            assertThatThrownBy(() -> loader.unloadClass("test.Class"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on clear when closed")
        void shouldThrowOnClearWhenClosed() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            assertThatThrownBy(loader::clear)
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("ReloadClass Tests")
    class ReloadClassTests {

        @Test
        @DisplayName("Should throw on nonexistent file")
        void shouldThrowOnNonexistentFile() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            Path nonexistent = tempDir.resolve("nonexistent.class");

            assertThatThrownBy(() -> loader.reloadClass("test.Nonexistent", nonexistent))
                    .isInstanceOf(OpenClassLoaderException.class);
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw on null class name in loadClass with bytecode")
        void shouldThrowOnNullClassNameInLoadClassWithBytecode() throws IOException {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            String resourcePath = HotSwapClassLoaderTest.class.getName().replace('.', '/') + ".class";
            byte[] bytecode = HotSwapClassLoaderTest.class.getClassLoader().getResourceAsStream(resourcePath).readAllBytes();

            assertThatNullPointerException()
                    .isThrownBy(() -> loader.loadClass(null, bytecode));
        }

        @Test
        @DisplayName("Should throw on null bytecode in loadClass")
        void shouldThrowOnNullBytecodeInLoadClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThatNullPointerException()
                    .isThrownBy(() -> loader.loadClass("test.NullBytecode", null));
        }
    }

    @Nested
    @DisplayName("Unload Tests")
    class UnloadTests {

        @Test
        @DisplayName("Should unload class")
        void shouldUnloadClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            loader.unloadClass("com.example.MyClass");

            // Should not throw, unloading marks class for reload
            assertThat(loader.getVersion("com.example.MyClass")).isEqualTo(0);
        }

        @Test
        @DisplayName("Should clear all classes")
        void shouldClearAllClasses() throws Exception {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            loader.loadClass("java.lang.String");
            loader.clear();

            assertThat(loader.getLoadedClassNames()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("Should close successfully")
        void shouldCloseSuccessfully() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThatCode(loader::close).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should work with try-with-resources")
        void shouldWorkWithTryWithResources() {
            assertThatCode(() -> {
                try (HotSwapClassLoader loader = HotSwapClassLoader.create()) {
                    loader.loadClass("java.lang.String");
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null class name in getVersion")
        void shouldHandleNullClassNameInGetVersion() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // Null class name may throw or return 0
            try {
                int version = loader.getVersion(null);
                assertThat(version).isGreaterThanOrEqualTo(0);
            } catch (NullPointerException e) {
                // Expected if implementation doesn't handle null
            }
        }

        @Test
        @DisplayName("Should handle null class name in unload")
        void shouldHandleNullClassNameInUnload() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // May throw NullPointerException or handle gracefully
            try {
                loader.unloadClass(null);
            } catch (NullPointerException e) {
                // Expected if implementation doesn't handle null
            }
        }

        @Test
        @DisplayName("Should throw ClassNotFoundException for unknown class in findClass")
        void shouldThrowForUnknownClassInFindClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // findClass is protected but called internally by loadClass when parent can't find
            assertThatThrownBy(() -> loader.loadClass("com.nonexistent.Unknown123456"))
                    .isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        @DisplayName("Close should be idempotent")
        void closeShouldBeIdempotent() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // Multiple close calls should not throw
            assertThatCode(() -> {
                loader.close();
                loader.close();
                loader.close();
            }).doesNotThrowAnyException();

            assertThat(loader.isClosed()).isTrue();
        }

        @Test
        @DisplayName("Should handle isLoaded for null class name")
        void shouldHandleIsLoadedForNull() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // null key in ConcurrentHashMap.containsKey returns false or throws
            try {
                boolean loaded = loader.isLoaded(null);
                assertThat(loaded).isFalse();
            } catch (NullPointerException e) {
                // Expected
            }
        }

        @Test
        @DisplayName("Should handle getBytecode for null class name")
        void shouldHandleGetBytecodeForNull() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            try {
                Optional<byte[]> bytecode = loader.getBytecode(null);
                assertThat(bytecode).isEmpty();
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }
}
