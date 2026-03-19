package cloud.opencode.base.test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

/**
 * ResourceLoaderTest Tests
 * ResourceLoaderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("ResourceLoader Tests")
class ResourceLoaderTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("loadString Tests")
    class LoadStringTests {

        @Test
        @DisplayName("Should throw for non-existent resource")
        void shouldThrowForNonExistentResource() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> ResourceLoader.loadString("non-existent.txt"))
                .withMessageContaining("Resource not found");
        }
    }

    @Nested
    @DisplayName("loadStringOptional Tests")
    class LoadStringOptionalTests {

        @Test
        @DisplayName("Should return empty for non-existent resource")
        void shouldReturnEmptyForNonExistentResource() {
            Optional<String> result = ResourceLoader.loadStringOptional("non-existent.txt");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("loadLines Tests")
    class LoadLinesTests {

        @Test
        @DisplayName("Should throw for non-existent resource")
        void shouldThrowForNonExistentResource() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> ResourceLoader.loadLines("non-existent.txt"))
                .withMessageContaining("Resource not found");
        }
    }

    @Nested
    @DisplayName("loadBytes Tests")
    class LoadBytesTests {

        @Test
        @DisplayName("Should throw for non-existent resource")
        void shouldThrowForNonExistentResource() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> ResourceLoader.loadBytes("non-existent.bin"))
                .withMessageContaining("Resource not found");
        }
    }

    @Nested
    @DisplayName("loadProperties Tests")
    class LoadPropertiesTests {

        @Test
        @DisplayName("Should throw for non-existent resource")
        void shouldThrowForNonExistentResource() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> ResourceLoader.loadProperties("non-existent.properties"))
                .withMessageContaining("Resource not found");
        }
    }

    @Nested
    @DisplayName("getResourceStream Tests")
    class GetResourceStreamTests {

        @Test
        @DisplayName("Should throw for non-existent resource")
        void shouldThrowForNonExistentResource() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> ResourceLoader.getResourceStream("non-existent.txt"))
                .withMessageContaining("Resource not found");
        }
    }

    @Nested
    @DisplayName("getResourceURL Tests")
    class GetResourceURLTests {

        @Test
        @DisplayName("Should return empty for non-existent resource")
        void shouldReturnEmptyForNonExistentResource() {
            Optional<URL> result = ResourceLoader.getResourceURL("non-existent.txt");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("exists Tests")
    class ExistsTests {

        @Test
        @DisplayName("Should return false for non-existent resource")
        void shouldReturnFalseForNonExistentResource() {
            assertThat(ResourceLoader.exists("non-existent.txt")).isFalse();
        }
    }

    @Nested
    @DisplayName("loadFile Tests")
    class LoadFileTests {

        @Test
        @DisplayName("Should load file content")
        void shouldLoadFileContent() throws Exception {
            Path testFile = tempDir.resolve("test.txt");
            Files.writeString(testFile, "Hello, World!");

            String content = ResourceLoader.loadFile(testFile);
            assertThat(content).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should load file with charset")
        void shouldLoadFileWithCharset() throws Exception {
            Path testFile = tempDir.resolve("test.txt");
            Files.writeString(testFile, "Hello, World!", StandardCharsets.UTF_8);

            String content = ResourceLoader.loadFile(testFile, StandardCharsets.UTF_8);
            assertThat(content).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should throw for non-existent file")
        void shouldThrowForNonExistentFile() {
            Path nonExistent = tempDir.resolve("non-existent.txt");

            assertThatIllegalArgumentException()
                .isThrownBy(() -> ResourceLoader.loadFile(nonExistent))
                .withMessageContaining("Failed to load file");
        }
    }

    @Nested
    @DisplayName("loadFileLines Tests")
    class LoadFileLinesTests {

        @Test
        @DisplayName("Should load file lines")
        void shouldLoadFileLines() throws Exception {
            Path testFile = tempDir.resolve("test.txt");
            Files.writeString(testFile, "line1\nline2\nline3");

            List<String> lines = ResourceLoader.loadFileLines(testFile);
            assertThat(lines).containsExactly("line1", "line2", "line3");
        }

        @Test
        @DisplayName("Should throw for non-existent file")
        void shouldThrowForNonExistentFile() {
            Path nonExistent = tempDir.resolve("non-existent.txt");

            assertThatIllegalArgumentException()
                .isThrownBy(() -> ResourceLoader.loadFileLines(nonExistent))
                .withMessageContaining("Failed to load file");
        }
    }

    @Nested
    @DisplayName("loadFileBytes Tests")
    class LoadFileBytesTests {

        @Test
        @DisplayName("Should load file bytes")
        void shouldLoadFileBytes() throws Exception {
            Path testFile = tempDir.resolve("test.bin");
            byte[] data = {0x01, 0x02, 0x03, 0x04};
            Files.write(testFile, data);

            byte[] bytes = ResourceLoader.loadFileBytes(testFile);
            assertThat(bytes).isEqualTo(data);
        }

        @Test
        @DisplayName("Should throw for non-existent file")
        void shouldThrowForNonExistentFile() {
            Path nonExistent = tempDir.resolve("non-existent.bin");

            assertThatIllegalArgumentException()
                .isThrownBy(() -> ResourceLoader.loadFileBytes(nonExistent))
                .withMessageContaining("Failed to load file");
        }
    }
}
