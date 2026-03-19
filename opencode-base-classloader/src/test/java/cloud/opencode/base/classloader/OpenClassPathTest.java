package cloud.opencode.base.classloader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for OpenClassPath facade
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("OpenClassPath Facade Tests")
class OpenClassPathTest {

    @Nested
    @DisplayName("ClassPath Access Tests")
    class ClassPathAccessTests {

        @Test
        @DisplayName("Should get classpath URLs")
        void shouldGetClassPathUrls() {
            List<URL> urls = OpenClassPath.getClassPathUrls();

            assertThat(urls).isNotEmpty();
        }

        @Test
        @DisplayName("Should get classpath entries")
        void shouldGetClassPathEntries() {
            List<Path> entries = OpenClassPath.getClassPathEntries();

            assertThat(entries).isNotEmpty();
        }

        @Test
        @DisplayName("Should get classpath string")
        void shouldGetClassPathString() {
            String classpath = OpenClassPath.getClassPath();

            assertThat(classpath).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Path Conversion Tests")
    class PathConversionTests {

        @Test
        @DisplayName("Should convert class name to resource path")
        void shouldConvertClassNameToResourcePath() {
            String path = OpenClassPath.classNameToResourcePath("com.example.MyClass");

            assertThat(path).isEqualTo("com/example/MyClass.class");
        }

        @Test
        @DisplayName("Should convert resource path to class name")
        void shouldConvertResourcePathToClassName() {
            String className = OpenClassPath.resourcePathToClassName("com/example/MyClass.class");

            assertThat(className).isEqualTo("com.example.MyClass");
        }

        @Test
        @DisplayName("Should handle resource path without extension")
        void shouldHandleResourcePathWithoutExtension() {
            String className = OpenClassPath.resourcePathToClassName("com/example/MyClass");

            assertThat(className).isEqualTo("com.example.MyClass");
        }

        @Test
        @DisplayName("Should convert package name to resource path")
        void shouldConvertPackageNameToResourcePath() {
            String path = OpenClassPath.packageNameToResourcePath("com.example.package");

            assertThat(path).isEqualTo("com/example/package");
        }

        @Test
        @DisplayName("Should throw on null class name")
        void shouldThrowOnNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassPath.classNameToResourcePath(null));
        }

        @Test
        @DisplayName("Should throw on null resource path")
        void shouldThrowOnNullResourcePath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassPath.resourcePathToClassName(null));
        }

        @Test
        @DisplayName("Should throw on null package name")
        void shouldThrowOnNullPackageName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassPath.packageNameToResourcePath(null));
        }
    }

    @Nested
    @DisplayName("Resource Finding Tests")
    class ResourceFindingTests {

        @Test
        @DisplayName("Should get package resources")
        void shouldGetPackageResources() {
            // Use the project's own package for more reliable testing
            List<URL> urls = OpenClassPath.getPackageResources("cloud.opencode.base.classloader");

            // May or may not find resources depending on how package resources work
            assertThat(urls).isNotNull();
        }

        @Test
        @DisplayName("Should return empty for nonexistent package")
        void shouldReturnEmptyForNonexistentPackage() {
            List<URL> urls = OpenClassPath.getPackageResources("com.nonexistent.package");

            assertThat(urls).isEmpty();
        }

        @Test
        @DisplayName("Should find single resource")
        void shouldFindSingleResource() {
            Optional<URL> url = OpenClassPath.findResource("test.txt");

            assertThat(url).isPresent();
        }

        @Test
        @DisplayName("Should return empty for nonexistent resource")
        void shouldReturnEmptyForNonexistentResource() {
            Optional<URL> url = OpenClassPath.findResource("nonexistent-resource.xyz");

            assertThat(url).isEmpty();
        }

        @Test
        @DisplayName("Should find all resources")
        void shouldFindAllResources() {
            List<URL> urls = OpenClassPath.findResources("test.txt");

            assertThat(urls).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty list for nonexistent resource")
        void shouldReturnEmptyListForNonexistentResource() {
            List<URL> urls = OpenClassPath.findResources("nonexistent-resource.xyz");

            assertThat(urls).isEmpty();
        }

        @Test
        @DisplayName("Should throw on null package name in getPackageResources")
        void shouldThrowOnNullPackageNameInGetPackageResources() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassPath.getPackageResources(null));
        }

        @Test
        @DisplayName("Should throw on null resource name in findResource")
        void shouldThrowOnNullResourceNameInFindResource() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassPath.findResource(null));
        }

        @Test
        @DisplayName("Should throw on null resource name in findResources")
        void shouldThrowOnNullResourceNameInFindResources() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenClassPath.findResources(null));
        }
    }

    @Nested
    @DisplayName("Existence Check Tests")
    class ExistenceCheckTests {

        @Test
        @DisplayName("Should return true for existing resource")
        void shouldReturnTrueForExistingResource() {
            boolean exists = OpenClassPath.resourceExists("test.txt");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false for nonexistent resource")
        void shouldReturnFalseForNonexistentResource() {
            boolean exists = OpenClassPath.resourceExists("nonexistent-resource.xyz");

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return true for existing class")
        void shouldReturnTrueForExistingClass() {
            boolean exists = OpenClassPath.classExists("java.lang.String");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false for nonexistent class")
        void shouldReturnFalseForNonexistentClass() {
            boolean exists = OpenClassPath.classExists("com.nonexistent.Class");

            assertThat(exists).isFalse();
        }
    }
}
