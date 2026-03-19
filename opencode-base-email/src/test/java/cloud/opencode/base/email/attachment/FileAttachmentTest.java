package cloud.opencode.base.email.attachment;

import cloud.opencode.base.email.exception.EmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * FileAttachment 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("FileAttachment 测试")
class FileAttachmentTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("从Path创建附件")
        void testConstructorFromPath() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello World");

            FileAttachment attachment = new FileAttachment(file);

            assertThat(attachment.getFileName()).isEqualTo("test.txt");
            assertThat(attachment.getPath()).isEqualTo(file);
            assertThat(attachment.getSize()).isEqualTo(11);
        }

        @Test
        @DisplayName("使用自定义文件名")
        void testConstructorWithCustomFileName() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            FileAttachment attachment = new FileAttachment(file, "custom-name.txt");

            assertThat(attachment.getFileName()).isEqualTo("custom-name.txt");
        }

        @Test
        @DisplayName("使用自定义文件名和内容类型")
        void testConstructorWithCustomFileNameAndContentType() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            FileAttachment attachment = new FileAttachment(file, "custom.html", "text/html");

            assertThat(attachment.getFileName()).isEqualTo("custom.html");
            assertThat(attachment.getContentType()).isEqualTo("text/html");
        }

        @Test
        @DisplayName("空路径抛出异常")
        void testNullPathThrowsException() {
            assertThatThrownBy(() -> new FileAttachment(null))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("path cannot be null");
        }

        @Test
        @DisplayName("文件不存在抛出异常")
        void testNonExistentFileThrowsException() {
            Path file = tempDir.resolve("nonexistent.txt");

            assertThatThrownBy(() -> new FileAttachment(file))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("File not found");
        }

        @Test
        @DisplayName("目录路径抛出异常")
        void testDirectoryPathThrowsException() {
            assertThatThrownBy(() -> new FileAttachment(tempDir))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("not a regular file");
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of(Path) 创建附件")
        void testOfFromPath() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            FileAttachment attachment = FileAttachment.of(file);

            assertThat(attachment.getFileName()).isEqualTo("test.txt");
        }

        @Test
        @DisplayName("of(String) 创建附件")
        void testOfFromString() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            FileAttachment attachment = FileAttachment.of(file.toString());

            assertThat(attachment.getFileName()).isEqualTo("test.txt");
        }
    }

    @Nested
    @DisplayName("数据访问测试")
    class DataAccessTests {

        @Test
        @DisplayName("getInputStream() 返回文件内容")
        void testGetInputStream() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello World");

            FileAttachment attachment = new FileAttachment(file);

            try (InputStream is = attachment.getInputStream()) {
                String content = new String(is.readAllBytes());
                assertThat(content).isEqualTo("Hello World");
            }
        }

        @Test
        @DisplayName("getSize() 返回文件大小")
        void testGetSize() throws Exception {
            Path file = tempDir.resolve("test.txt");
            byte[] data = new byte[1024];
            Files.write(file, data);

            FileAttachment attachment = new FileAttachment(file);

            assertThat(attachment.getSize()).isEqualTo(1024);
        }

        @Test
        @DisplayName("getContentType() 自动检测内容类型")
        void testContentTypeDetection() throws Exception {
            Path htmlFile = tempDir.resolve("test.html");
            Files.writeString(htmlFile, "<html></html>");

            FileAttachment attachment = new FileAttachment(htmlFile);

            // 内容类型检测可能因系统而异，至少不应该是null
            assertThat(attachment.getContentType()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Attachment接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("isInline() 返回false")
        void testIsInline() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            FileAttachment attachment = new FileAttachment(file);

            assertThat(attachment.isInline()).isFalse();
        }

        @Test
        @DisplayName("getContentId() 返回null")
        void testGetContentId() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            FileAttachment attachment = new FileAttachment(file);

            assertThat(attachment.getContentId()).isNull();
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString() 包含关键信息")
        void testToString() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "content");

            FileAttachment attachment = new FileAttachment(file);

            String str = attachment.toString();
            assertThat(str).contains("test.txt");
            assertThat(str).contains("path=");
        }
    }
}
