package cloud.opencode.base.io.resource;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * UrlResource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("UrlResource 测试")
class UrlResourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用URL创建")
        void testConstructorWithURL() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            URL url = file.toUri().toURL();
            UrlResource resource = new UrlResource(url);

            assertThat(resource.getURL()).isEqualTo(url);
        }

        @Test
        @DisplayName("使用URI创建")
        void testConstructorWithURI() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            URI uri = file.toUri();
            UrlResource resource = new UrlResource(uri);

            assertThat(resource.getURI()).isEqualTo(uri);
        }

        @Test
        @DisplayName("使用字符串创建")
        void testConstructorWithString() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            String urlString = file.toUri().toString();
            UrlResource resource = new UrlResource(urlString);

            assertThat(resource.getURL()).isNotNull();
        }

        @Test
        @DisplayName("null URL抛出异常")
        void testConstructorWithNullURL() {
            assertThatThrownBy(() -> new UrlResource((URL) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null URI抛出异常")
        void testConstructorWithNullURI() {
            assertThatThrownBy(() -> new UrlResource((URI) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null字符串抛出异常")
        void testConstructorWithNullString() {
            assertThatThrownBy(() -> new UrlResource((String) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("无效URL字符串抛出异常")
        void testConstructorWithInvalidString() {
            // URI.create throws IllegalArgumentException for invalid URIs
            assertThatThrownBy(() -> new UrlResource("not a valid url"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("存在的文件URL返回true")
        void testExistsTrue() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThat(resource.exists()).isTrue();
        }

        @Test
        @DisplayName("不存在的文件URL返回false")
        void testExistsFalse() throws Exception {
            Path file = tempDir.resolve("non-existent.txt");
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThat(resource.exists()).isFalse();
        }
    }

    @Nested
    @DisplayName("isReadable方法测试")
    class IsReadableTests {

        @Test
        @DisplayName("可读性与存在性相同")
        void testIsReadable() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThat(resource.isReadable()).isEqualTo(resource.exists());
        }
    }

    @Nested
    @DisplayName("getInputStream方法测试")
    class GetInputStreamTests {

        @Test
        @DisplayName("打开文件URL的输入流")
        void testGetInputStream() throws Exception {
            Path file = tempDir.resolve("test.txt");
            byte[] data = {1, 2, 3, 4, 5};
            Files.write(file, data);
            UrlResource resource = new UrlResource(file.toUri().toURL());

            try (InputStream is = resource.getInputStream()) {
                assertThat(is.readAllBytes()).isEqualTo(data);
            }
        }

        @Test
        @DisplayName("不存在的URL抛出异常")
        void testGetInputStreamNotFound() throws Exception {
            Path file = tempDir.resolve("non-existent.txt");
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThatThrownBy(resource::getInputStream)
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("getURL方法测试")
    class GetURLTests {

        @Test
        @DisplayName("返回URL")
        void testGetURL() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            URL url = file.toUri().toURL();
            UrlResource resource = new UrlResource(url);

            assertThat(resource.getURL()).isEqualTo(url);
        }
    }

    @Nested
    @DisplayName("getPath方法测试")
    class GetPathTests {

        @Test
        @DisplayName("文件协议返回Path")
        void testGetPathFile() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            UrlResource resource = new UrlResource(file.toUri().toURL());

            Path result = resource.getPath();

            assertThat(result).isEqualTo(file.toAbsolutePath());
        }

        @Test
        @DisplayName("非文件协议返回null")
        void testGetPathNonFile() throws Exception {
            UrlResource resource = new UrlResource(new URL("http://example.com/test.txt"));

            assertThat(resource.getPath()).isNull();
        }
    }

    @Nested
    @DisplayName("getDescription方法测试")
    class GetDescriptionTests {

        @Test
        @DisplayName("返回URL:前缀描述")
        void testGetDescription() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThat(resource.getDescription()).startsWith("URL:");
        }
    }

    @Nested
    @DisplayName("getFilename方法测试")
    class GetFilenameTests {

        @Test
        @DisplayName("返回文件名")
        void testGetFilename() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThat(resource.getFilename()).isEqualTo("test.txt");
        }

        @Test
        @DisplayName("空路径返回空字符串")
        void testGetFilenameEmpty() throws Exception {
            UrlResource resource = new UrlResource(new URL("http://example.com/"));

            // The filename should be empty or just "/"
            assertThat(resource.getFilename()).isEmpty();
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
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThat(resource.contentLength()).isEqualTo(100);
        }

        @Test
        @DisplayName("不存在的文件返回0或-1")
        void testContentLengthNotFound() throws Exception {
            Path file = tempDir.resolve("non-existent.txt");
            UrlResource resource = new UrlResource(file.toUri().toURL());

            // For file:// URLs of non-existent files, contentLength returns 0 (not -1)
            assertThat(resource.contentLength()).isIn(-1L, 0L);
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
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThat(resource.lastModified()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("createRelative方法测试")
    class CreateRelativeTests {

        @Test
        @DisplayName("创建相对资源")
        void testCreateRelative() throws Exception {
            Path dir = tempDir.resolve("config");
            Files.createDirectories(dir);
            Path file = dir.resolve("app.properties");
            Files.createFile(file);
            UrlResource resource = new UrlResource(file.toUri().toURL());

            Resource relative = resource.createRelative("other.properties");

            assertThat(relative).isInstanceOf(UrlResource.class);
            assertThat(relative.getFilename()).isEqualTo("other.properties");
        }
    }

    @Nested
    @DisplayName("getURI方法测试")
    class GetURITests {

        @Test
        @DisplayName("返回URI")
        void testGetURI() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            URI uri = file.toUri();
            UrlResource resource = new UrlResource(uri);

            assertThat(resource.getURI()).isEqualTo(uri);
        }
    }

    @Nested
    @DisplayName("equals和hashCode方法测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同URL相等")
        void testEquals() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            URL url = file.toUri().toURL();
            UrlResource r1 = new UrlResource(url);
            UrlResource r2 = new UrlResource(url);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("不同URL不相等")
        void testNotEquals() throws Exception {
            Path file1 = tempDir.resolve("test1.txt");
            Path file2 = tempDir.resolve("test2.txt");
            Files.createFile(file1);
            Files.createFile(file2);
            UrlResource r1 = new UrlResource(file1.toUri().toURL());
            UrlResource r2 = new UrlResource(file2.toUri().toURL());

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            UrlResource r1 = new UrlResource(file.toUri().toURL());

            assertThat(r1).isEqualTo(r1);
        }

        @Test
        @DisplayName("与null不相等")
        void testNotEqualsNull() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            UrlResource r1 = new UrlResource(file.toUri().toURL());

            assertThat(r1).isNotEqualTo(null);
        }

        @Test
        @DisplayName("与其他类型不相等")
        void testNotEqualsOtherType() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            UrlResource r1 = new UrlResource(file.toUri().toURL());

            assertThat(r1).isNotEqualTo("test.txt");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("返回描述")
        void testToString() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThat(resource.toString()).startsWith("URL:");
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
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThat(resource.readBytes()).isEqualTo(data);
        }

        @Test
        @DisplayName("读取字符串")
        void testReadString() throws Exception {
            Path file = tempDir.resolve("text.txt");
            String content = "Hello, World!";
            Files.writeString(file, content);
            UrlResource resource = new UrlResource(file.toUri().toURL());

            assertThat(resource.readString()).isEqualTo(content);
        }
    }
}
