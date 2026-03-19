package cloud.opencode.base.classloader;

import cloud.opencode.base.classloader.loader.HotSwapClassLoader;
import cloud.opencode.base.classloader.loader.IsoClassLoader;
import cloud.opencode.base.classloader.loader.ResourceClassLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for OpenClassLoader facade
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("OpenClassLoader Facade Tests")
class OpenClassLoaderTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("ClassLoader Getter Tests")
    class ClassLoaderGetterTests {

        @Test
        @DisplayName("Should get default classloader")
        void shouldGetDefaultClassLoader() {
            ClassLoader cl = OpenClassLoader.getDefaultClassLoader();

            assertThat(cl).isNotNull();
        }

        @Test
        @DisplayName("Should get context classloader")
        void shouldGetContextClassLoader() {
            ClassLoader cl = OpenClassLoader.getContextClassLoader();

            // May be null in some contexts
            assertThat(cl).isNotNull();
        }

        @Test
        @DisplayName("Should get classloader for class")
        void shouldGetClassLoaderForClass() {
            ClassLoader cl = OpenClassLoader.getClassLoader(String.class);

            // Bootstrap classloader may return system classloader
            assertThat(cl).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null class")
        void shouldThrowOnNullClass() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassLoader.getClassLoader(null));
        }
    }

    @Nested
    @DisplayName("ClassLoader Creation Tests")
    class ClassLoaderCreationTests {

        @Test
        @DisplayName("Should create isolated loader builder")
        void shouldCreateIsolatedLoaderBuilder() {
            IsoClassLoader.Builder builder = OpenClassLoader.isolatedLoader();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("Should create hot-swap loader")
        void shouldCreateHotSwapLoader() {
            HotSwapClassLoader loader = OpenClassLoader.hotSwapLoader();

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create hot-swap loader with parent")
        void shouldCreateHotSwapLoaderWithParent() {
            HotSwapClassLoader loader = OpenClassLoader.hotSwapLoader(getClass().getClassLoader());

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create resource loader")
        void shouldCreateResourceLoader() throws IOException {
            Path resourceDir = tempDir.resolve("resources");
            Files.createDirectories(resourceDir);

            ResourceClassLoader loader = OpenClassLoader.resourceLoader(resourceDir);

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create resource loader with multiple paths")
        void shouldCreateResourceLoaderWithMultiplePaths() throws IOException {
            Path dir1 = tempDir.resolve("resources1");
            Path dir2 = tempDir.resolve("resources2");
            Files.createDirectories(dir1);
            Files.createDirectories(dir2);

            ResourceClassLoader loader = OpenClassLoader.resourceLoader(dir1, dir2);

            assertThat(loader).isNotNull();
        }
    }

    @Nested
    @DisplayName("WithClassLoader Tests")
    class WithClassLoaderTests {

        @Test
        @DisplayName("Should execute supplier with classloader")
        void shouldExecuteSupplierWithClassLoader() {
            ClassLoader customLoader = getClass().getClassLoader();
            AtomicReference<ClassLoader> capturedLoader = new AtomicReference<>();

            String result = OpenClassLoader.withClassLoader(customLoader, () -> {
                capturedLoader.set(Thread.currentThread().getContextClassLoader());
                return "result";
            });

            assertThat(result).isEqualTo("result");
            assertThat(capturedLoader.get()).isSameAs(customLoader);
        }

        @Test
        @DisplayName("Should restore original classloader after supplier")
        void shouldRestoreOriginalClassLoaderAfterSupplier() {
            ClassLoader original = Thread.currentThread().getContextClassLoader();
            ClassLoader customLoader = ClassLoader.getSystemClassLoader();

            OpenClassLoader.withClassLoader(customLoader, () -> "ignored");

            assertThat(Thread.currentThread().getContextClassLoader()).isSameAs(original);
        }

        @Test
        @DisplayName("Should execute runnable with classloader")
        void shouldExecuteRunnableWithClassLoader() {
            ClassLoader customLoader = getClass().getClassLoader();
            AtomicReference<ClassLoader> capturedLoader = new AtomicReference<>();

            OpenClassLoader.withClassLoader(customLoader, () -> {
                capturedLoader.set(Thread.currentThread().getContextClassLoader());
            });

            assertThat(capturedLoader.get()).isSameAs(customLoader);
        }

        @Test
        @DisplayName("Should restore original classloader after runnable")
        void shouldRestoreOriginalClassLoaderAfterRunnable() {
            ClassLoader original = Thread.currentThread().getContextClassLoader();
            ClassLoader customLoader = ClassLoader.getSystemClassLoader();

            OpenClassLoader.withClassLoader(customLoader, () -> {});

            assertThat(Thread.currentThread().getContextClassLoader()).isSameAs(original);
        }

        @Test
        @DisplayName("Should throw on null classloader in supplier")
        void shouldThrowOnNullClassLoaderInSupplier() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassLoader.withClassLoader(null, () -> "result"));
        }

        @Test
        @DisplayName("Should throw on null supplier")
        void shouldThrowOnNullSupplier() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassLoader.withClassLoader(
                            getClass().getClassLoader(),
                            (java.util.function.Supplier<Object>) null));
        }

        @Test
        @DisplayName("Should throw on null classloader in runnable")
        void shouldThrowOnNullClassLoaderInRunnable() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassLoader.withClassLoader(null, () -> {}));
        }

        @Test
        @DisplayName("Should throw on null runnable")
        void shouldThrowOnNullRunnable() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassLoader.withClassLoader(
                            getClass().getClassLoader(),
                            (Runnable) null));
        }

        @Test
        @DisplayName("Should propagate exception from supplier and restore classloader")
        void shouldPropagateExceptionFromSupplierAndRestoreClassLoader() {
            ClassLoader original = Thread.currentThread().getContextClassLoader();
            ClassLoader customLoader = ClassLoader.getSystemClassLoader();

            assertThatThrownBy(() ->
                OpenClassLoader.withClassLoader(customLoader, () -> {
                    throw new RuntimeException("Test exception");
                })
            ).isInstanceOf(RuntimeException.class).hasMessage("Test exception");

            // Verify classloader is restored even after exception
            assertThat(Thread.currentThread().getContextClassLoader()).isSameAs(original);
        }

        @Test
        @DisplayName("Should propagate exception from runnable and restore classloader")
        void shouldPropagateExceptionFromRunnableAndRestoreClassLoader() {
            ClassLoader original = Thread.currentThread().getContextClassLoader();
            ClassLoader customLoader = ClassLoader.getSystemClassLoader();

            assertThatThrownBy(() ->
                OpenClassLoader.withClassLoader(customLoader, (Runnable) () -> {
                    throw new RuntimeException("Test exception");
                })
            ).isInstanceOf(RuntimeException.class).hasMessage("Test exception");

            // Verify classloader is restored even after exception
            assertThat(Thread.currentThread().getContextClassLoader()).isSameAs(original);
        }
    }

    @Nested
    @DisplayName("Visibility Tests")
    class VisibilityTests {

        @Test
        @DisplayName("Should check class visibility")
        void shouldCheckClassVisibility() {
            boolean visible = OpenClassLoader.isVisible(String.class, getClass().getClassLoader());

            assertThat(visible).isTrue();
        }

        @Test
        @DisplayName("Should return true for null classloader")
        void shouldReturnTrueForNullClassLoader() {
            boolean visible = OpenClassLoader.isVisible(String.class, null);

            assertThat(visible).isTrue();
        }

        @Test
        @DisplayName("Should throw on null class in isVisible")
        void shouldThrowOnNullClassInIsVisible() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassLoader.isVisible(null, getClass().getClassLoader()));
        }
    }

    @Nested
    @DisplayName("ClassLoader Hierarchy Tests")
    class ClassLoaderHierarchyTests {

        @Test
        @DisplayName("Should get classloader hierarchy")
        void shouldGetClassLoaderHierarchy() {
            ClassLoader cl = getClass().getClassLoader();
            List<ClassLoader> hierarchy = OpenClassLoader.getClassLoaderHierarchy(cl);

            assertThat(hierarchy).isNotEmpty();
            assertThat(hierarchy.get(0)).isSameAs(cl);
        }

        @Test
        @DisplayName("Should return empty list for null classloader")
        void shouldReturnEmptyListForNullClassLoader() {
            List<ClassLoader> hierarchy = OpenClassLoader.getClassLoaderHierarchy(null);

            assertThat(hierarchy).isEmpty();
        }
    }

    @Nested
    @DisplayName("FindDefiningClassLoader Tests")
    class FindDefiningClassLoaderTests {

        @Test
        @DisplayName("Should find defining classloader")
        void shouldFindDefiningClassLoader() {
            // Use a class from this project that has a non-null classloader
            Optional<ClassLoader> cl = OpenClassLoader.findDefiningClassLoader(
                    OpenClassLoader.class.getName());

            // Should find the classloader for our own class
            assertThat(cl).isPresent();
        }

        @Test
        @DisplayName("Should return empty for nonexistent class")
        void shouldReturnEmptyForNonexistentClass() {
            Optional<ClassLoader> cl = OpenClassLoader.findDefiningClassLoader("com.nonexistent.Class");

            assertThat(cl).isEmpty();
        }

        @Test
        @DisplayName("Should throw on null class name")
        void shouldThrowOnNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassLoader.findDefiningClassLoader(null));
        }
    }

    @Nested
    @DisplayName("LoadClass Tests")
    class LoadClassTests {

        @Test
        @DisplayName("Should load class by name")
        void shouldLoadClassByName() {
            Optional<Class<?>> clazz = OpenClassLoader.loadClass("java.lang.String");

            assertThat(clazz).isPresent();
            assertThat(clazz.get()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Should load class with classloader")
        void shouldLoadClassWithClassLoader() {
            Optional<Class<?>> clazz = OpenClassLoader.loadClass(
                    "java.lang.Integer", getClass().getClassLoader());

            assertThat(clazz).isPresent();
            assertThat(clazz.get()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("Should return empty for nonexistent class")
        void shouldReturnEmptyForNonexistentClass() {
            Optional<Class<?>> clazz = OpenClassLoader.loadClass("com.nonexistent.Class");

            assertThat(clazz).isEmpty();
        }

        @Test
        @DisplayName("Should throw on null class name")
        void shouldThrowOnNullClassNameInLoadClass() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassLoader.loadClass(null));
        }

        @Test
        @DisplayName("Should throw on null class name with classloader")
        void shouldThrowOnNullClassNameWithClassLoader() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassLoader.loadClass(null, getClass().getClassLoader()));
        }
    }
}
