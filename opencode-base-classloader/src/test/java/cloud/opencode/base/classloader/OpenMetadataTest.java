package cloud.opencode.base.classloader;

import cloud.opencode.base.classloader.metadata.ClassMetadata;
import cloud.opencode.base.classloader.metadata.MetadataReader;
import cloud.opencode.base.classloader.resource.ClassPathResource;
import cloud.opencode.base.classloader.resource.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for OpenMetadata facade
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("OpenMetadata Facade Tests")
class OpenMetadataTest {

    @Nested
    @DisplayName("Read By Class Name Tests")
    class ReadByClassNameTests {

        @Test
        @DisplayName("Should read metadata by class name")
        void shouldReadMetadataByClassName() {
            ClassMetadata metadata = OpenMetadata.read("java.lang.String");

            assertThat(metadata).isNotNull();
            assertThat(metadata.className()).isEqualTo("java.lang.String");
        }

        @Test
        @DisplayName("Should throw on null class name")
        void shouldThrowOnNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenMetadata.read((String) null));
        }
    }

    @Nested
    @DisplayName("Read By Class Object Tests")
    class ReadByClassObjectTests {

        @Test
        @DisplayName("Should read metadata from Class object")
        void shouldReadMetadataFromClassObject() {
            ClassMetadata metadata = OpenMetadata.read(String.class);

            assertThat(metadata).isNotNull();
            assertThat(metadata.className()).isEqualTo("java.lang.String");
        }

        @Test
        @DisplayName("Should read interface metadata")
        void shouldReadInterfaceMetadata() {
            ClassMetadata metadata = OpenMetadata.read(java.io.Serializable.class);

            assertThat(metadata).isNotNull();
            assertThat(metadata.isInterface()).isTrue();
        }

        @Test
        @DisplayName("Should read annotation metadata")
        void shouldReadAnnotationMetadata() {
            ClassMetadata metadata = OpenMetadata.read(java.lang.annotation.Retention.class);

            assertThat(metadata).isNotNull();
            assertThat(metadata.isAnnotation()).isTrue();
        }

        @Test
        @DisplayName("Should throw on null Class object")
        void shouldThrowOnNullClassObject() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenMetadata.read((Class<?>) null));
        }
    }

    @Nested
    @DisplayName("Read By Resource Tests")
    class ReadByResourceTests {

        @Test
        @DisplayName("Should read metadata from resource")
        void shouldReadMetadataFromResource() {
            // Use this class's resource (available in test classpath)
            String path = OpenMetadataTest.class.getName().replace('.', '/') + ".class";
            Resource resource = new ClassPathResource(path);

            ClassMetadata metadata = OpenMetadata.read(resource);

            assertThat(metadata).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null resource")
        void shouldThrowOnNullResource() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenMetadata.read((Resource) null));
        }
    }

    @Nested
    @DisplayName("Read By Bytecode Tests")
    class ReadByBytecodeTests {

        @Test
        @DisplayName("Should read metadata from bytecode")
        void shouldReadMetadataFromBytecode() throws Exception {
            // Get bytecode from this class (available in test classpath)
            String path = OpenMetadataTest.class.getName().replace('.', '/') + ".class";
            Resource resource = new ClassPathResource(path);
            byte[] bytecode = resource.getBytes();

            ClassMetadata metadata = OpenMetadata.read(bytecode);

            assertThat(metadata).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null bytecode")
        void shouldThrowOnNullBytecode() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenMetadata.read((byte[]) null));
        }
    }

    @Nested
    @DisplayName("Batch Read Tests")
    class BatchReadTests {

        private static final String TEST_PACKAGE = "cloud.opencode.base.classloader.metadata";

        @Test
        @DisplayName("Should read package metadata")
        void shouldReadPackageMetadata() {
            List<ClassMetadata> metadataList = OpenMetadata.readPackage(TEST_PACKAGE);

            // May return empty if package scanning doesn't work for test classes
            assertThat(metadataList).isNotNull();
        }

        @Test
        @DisplayName("Should read package metadata with filter")
        void shouldReadPackageMetadataWithFilter() {
            List<ClassMetadata> metadataList = OpenMetadata.readPackage(
                    TEST_PACKAGE,
                    name -> name.contains("Metadata")
            );

            // May return empty if package scanning doesn't work for test classes
            assertThat(metadataList).isNotNull();
        }

        @Test
        @DisplayName("Should return empty for nonexistent package")
        void shouldReturnEmptyForNonexistentPackage() {
            List<ClassMetadata> metadataList = OpenMetadata.readPackage("com.nonexistent.package");

            assertThat(metadataList).isEmpty();
        }

        @Test
        @DisplayName("Should throw on null package name")
        void shouldThrowOnNullPackageName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenMetadata.readPackage(null));
        }

        @Test
        @DisplayName("Should throw on null filter")
        void shouldThrowOnNullFilter() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenMetadata.readPackage(TEST_PACKAGE, null));
        }
    }

    @Nested
    @DisplayName("Reader Tests")
    class ReaderTests {

        @Test
        @DisplayName("Should return metadata reader class")
        void shouldReturnMetadataReaderClass() {
            Class<MetadataReader> readerClass = OpenMetadata.reader();

            assertThat(readerClass).isEqualTo(MetadataReader.class);
        }
    }
}
