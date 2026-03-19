package cloud.opencode.base.io.resource;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * FileSystemResource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("FileSystemResource 测试")
class FileSystemResourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用Path创建")
        void testConstructorWithPath() {
            Path file = tempDir.resolve("test.txt");
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.getFilePath()).isNotNull();
            assertThat(resource.getFilePath().isAbsolute()).isTrue();
        }

        @Test
        @DisplayName("使用字符串路径创建")
        void testConstructorWithString() {
            String path = tempDir.resolve("test.txt").toString();
            FileSystemResource resource = new FileSystemResource(path);

            assertThat(resource.getFilePath()).isNotNull();
        }

        @Test
        @DisplayName("null Path抛出异常")
        void testConstructorWithNullPath() {
            assertThatThrownBy(() -> new FileSystemResource((Path) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("存在的文件返回true")
        void testExistsTrue() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("不存在的文件返回false")
        void testExistsFalse() {
            Path file = tempDir.resolve("non-existent.txt");
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.exists()).isFalse();
        }
    }

    @Nested
    @DisplayName("isReadable方法测试")
    class IsReadableTests {

        @Test
        @DisplayName("可读文件返回true")
        void testIsReadableTrue() throws Exception {
            Path file = tempDir.resolve("readable.txt");
            Files.createFile(file);
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.isReadable()).isTrue();
        }

        @Test
        @DisplayName("不存在的文件返回false")
        void testIsReadableFalse() {
            Path file = tempDir.resolve("non-existent.txt");
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.isReadable()).isFalse();
        }
    }

    @Nested
    @DisplayName("getInputStream方法测试")
    class GetInputStreamTests {

        @Test
        @DisplayName("打开输入流")
        void testGetInputStream() throws Exception {
            Path file = tempDir.resolve("test.txt");
            byte[] data = {1, 2, 3, 4, 5};
            Files.write(file, data);
            FileSystemResource resource = new FileSystemResource(file);

            try (InputStream is = resource.getInputStream()) {
                assertThat(is.readAllBytes()).isEqualTo(data);
            }
        }

        @Test
        @DisplayName("不存在的文件抛出异常")
        void testGetInputStreamNotFound() {
            Path file = tempDir.resolve("non-existent.txt");
            FileSystemResource resource = new FileSystemResource(file);

            assertThatThrownBy(resource::getInputStream)
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("getURL方法测试")
    class GetURLTests {

        @Test
        @DisplayName("返回文件URL")
        void testGetURL() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            FileSystemResource resource = new FileSystemResource(file);

            URL url = resource.getURL();

            assertThat(url.getProtocol()).isEqualTo("file");
        }
    }

    @Nested
    @DisplayName("getPath方法测试")
    class GetPathTests {

        @Test
        @DisplayName("返回绝对路径")
        void testGetPath() {
            Path file = tempDir.resolve("test.txt");
            FileSystemResource resource = new FileSystemResource(file);

            Path result = resource.getPath();

            assertThat(result.isAbsolute()).isTrue();
        }
    }

    @Nested
    @DisplayName("getDescription方法测试")
    class GetDescriptionTests {

        @Test
        @DisplayName("返回file:前缀描述")
        void testGetDescription() {
            Path file = tempDir.resolve("test.txt");
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.getDescription()).startsWith("file:");
        }
    }

    @Nested
    @DisplayName("getFilename方法测试")
    class GetFilenameTests {

        @Test
        @DisplayName("返回文件名")
        void testGetFilename() {
            Path file = tempDir.resolve("test.txt");
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.getFilename()).isEqualTo("test.txt");
        }

        @Test
        @DisplayName("目录时返回目录名")
        void testGetFilenameDirectory() {
            FileSystemResource resource = new FileSystemResource(tempDir);

            assertThat(resource.getFilename()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("contentLength方法测试")
    class ContentLengthTests {

        @Test
        @DisplayName("返回文件大小")
        void testContentLength() throws Exception {
            Path file = tempDir.resolve("test.txt");
            byte[] data = new byte[100];
            Files.write(file, data);
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.contentLength()).isEqualTo(100);
        }

        @Test
        @DisplayName("不存在的文件返回-1")
        void testContentLengthNotFound() {
            Path file = tempDir.resolve("non-existent.txt");
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.contentLength()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("lastModified方法测试")
    class LastModifiedTests {

        @Test
        @DisplayName("返回最后修改时间")
        void testLastModified() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.lastModified()).isGreaterThan(0);
        }

        @Test
        @DisplayName("不存在的文件返回-1")
        void testLastModifiedNotFound() {
            Path file = tempDir.resolve("non-existent.txt");
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.lastModified()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("createRelative方法测试")
    class CreateRelativeTests {

        @Test
        @DisplayName("创建相对资源")
        void testCreateRelative() throws Exception {
            Path file = tempDir.resolve("config/app.properties");
            Files.createDirectories(file.getParent());
            Files.createFile(file);
            FileSystemResource resource = new FileSystemResource(file);

            Resource relative = resource.createRelative("other.properties");

            assertThat(relative).isInstanceOf(FileSystemResource.class);
            assertThat(relative.getFilename()).isEqualTo("other.properties");
        }

        @Test
        @DisplayName("根目录时直接使用相对路径")
        void testCreateRelativeFromRoot() {
            // Create resource from path without parent (edge case)
            FileSystemResource resource = new FileSystemResource(Path.of("/test.txt"));
            Resource relative = resource.createRelative("other.txt");

            assertThat(relative).isInstanceOf(FileSystemResource.class);
        }
    }

    @Nested
    @DisplayName("getFilePath方法测试")
    class GetFilePathTests {

        @Test
        @DisplayName("返回标准化绝对路径")
        void testGetFilePath() {
            Path file = tempDir.resolve("test.txt");
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.getFilePath()).isEqualTo(resource.getPath());
        }
    }

    @Nested
    @DisplayName("equals和hashCode方法测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同路径相等")
        void testEquals() {
            Path file = tempDir.resolve("test.txt");
            FileSystemResource r1 = new FileSystemResource(file);
            FileSystemResource r2 = new FileSystemResource(file);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("不同路径不相等")
        void testNotEquals() {
            FileSystemResource r1 = new FileSystemResource(tempDir.resolve("test1.txt"));
            FileSystemResource r2 = new FileSystemResource(tempDir.resolve("test2.txt"));

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            FileSystemResource r1 = new FileSystemResource(tempDir.resolve("test.txt"));

            assertThat(r1).isEqualTo(r1);
        }

        @Test
        @DisplayName("与null不相等")
        void testNotEqualsNull() {
            FileSystemResource r1 = new FileSystemResource(tempDir.resolve("test.txt"));

            assertThat(r1).isNotEqualTo(null);
        }

        @Test
        @DisplayName("与其他类型不相等")
        void testNotEqualsOtherType() {
            FileSystemResource r1 = new FileSystemResource(tempDir.resolve("test.txt"));

            assertThat(r1).isNotEqualTo("test.txt");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("返回描述")
        void testToString() {
            Path file = tempDir.resolve("test.txt");
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.toString()).startsWith("file:");
        }
    }

    @Nested
    @DisplayName("readBytes和readString测试")
    class ReadTests {

        @Test
        @DisplayName("读取字节")
        void testReadBytes() throws Exception {
            Path file = tempDir.resolve("data.bin");
            byte[] data = {10, 20, 30, 40, 50};
            Files.write(file, data);
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.readBytes()).isEqualTo(data);
        }

        @Test
        @DisplayName("读取字符串")
        void testReadString() throws Exception {
            Path file = tempDir.resolve("text.txt");
            String content = "Hello, World!";
            Files.writeString(file, content);
            FileSystemResource resource = new FileSystemResource(file);

            assertThat(resource.readString()).isEqualTo(content);
        }
    }
}
