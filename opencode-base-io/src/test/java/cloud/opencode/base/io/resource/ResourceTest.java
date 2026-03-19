package cloud.opencode.base.io.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Resource 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("Resource 接口测试")
class ResourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("readBytes默认方法测试")
    class ReadBytesTests {

        @Test
        @DisplayName("读取资源为字节数组")
        void testReadBytes() throws Exception {
            Path file = tempDir.resolve("test.txt");
            byte[] data = {1, 2, 3, 4, 5};
            Files.write(file, data);
            Resource resource = new FileSystemResource(file);

            byte[] result = resource.readBytes();

            assertThat(result).isEqualTo(data);
        }

        @Test
        @DisplayName("读取空文件")
        void testReadBytesEmpty() throws Exception {
            Path file = tempDir.resolve("empty.txt");
            Files.createFile(file);
            Resource resource = new FileSystemResource(file);

            byte[] result = resource.readBytes();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("readString默认方法测试")
    class ReadStringTests {

        @Test
        @DisplayName("使用指定字符集读取")
        void testReadStringWithCharset() throws Exception {
            Path file = tempDir.resolve("test.txt");
            String content = "你好，世界！";
            Files.writeString(file, content, StandardCharsets.UTF_8);
            Resource resource = new FileSystemResource(file);

            String result = resource.readString(StandardCharsets.UTF_8);

            assertThat(result).isEqualTo(content);
        }

        @Test
        @DisplayName("使用默认UTF-8读取")
        void testReadStringDefault() throws Exception {
            Path file = tempDir.resolve("test.txt");
            String content = "Hello, World!";
            Files.writeString(file, content, StandardCharsets.UTF_8);
            Resource resource = new FileSystemResource(file);

            String result = resource.readString();

            assertThat(result).isEqualTo(content);
        }

        @Test
        @DisplayName("读取空字符串")
        void testReadStringEmpty() throws Exception {
            Path file = tempDir.resolve("empty.txt");
            Files.createFile(file);
            Resource resource = new FileSystemResource(file);

            String result = resource.readString();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Resource接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("自定义实现测试")
        void testCustomImplementation() {
            Resource resource = new Resource() {
                @Override
                public boolean exists() {
                    return true;
                }

                @Override
                public boolean isReadable() {
                    return true;
                }

                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8));
                }

                @Override
                public URL getURL() {
                    return null;
                }

                @Override
                public Path getPath() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return "custom resource";
                }

                @Override
                public String getFilename() {
                    return "test.txt";
                }

                @Override
                public long contentLength() {
                    return 12;
                }

                @Override
                public long lastModified() {
                    return -1;
                }

                @Override
                public Resource createRelative(String relativePath) {
                    return this;
                }
            };

            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();
            assertThat(resource.readString()).isEqualTo("test content");
            assertThat(resource.readBytes()).isEqualTo("test content".getBytes(StandardCharsets.UTF_8));
            assertThat(resource.getDescription()).isEqualTo("custom resource");
            assertThat(resource.getFilename()).isEqualTo("test.txt");
            assertThat(resource.contentLength()).isEqualTo(12);
        }
    }
}
