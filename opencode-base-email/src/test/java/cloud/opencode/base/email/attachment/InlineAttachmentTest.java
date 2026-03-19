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
 * InlineAttachment 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("InlineAttachment 测试")
class InlineAttachmentTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建内嵌附件")
        void testConstructor() {
            byte[] data = "image data".getBytes();
            InlineAttachment attachment = new InlineAttachment("logo", "logo.png", data, "image/png");

            assertThat(attachment.getContentId()).isEqualTo("logo");
            assertThat(attachment.getFileName()).isEqualTo("logo.png");
            assertThat(attachment.getContentType()).isEqualTo("image/png");
            assertThat(attachment.getSize()).isEqualTo(data.length);
        }

        @Test
        @DisplayName("空ContentId抛出异常")
        void testNullContentIdThrowsException() {
            assertThatThrownBy(() -> new InlineAttachment(null, "file.png", new byte[10], "image/png"))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("Content-ID");
        }

        @Test
        @DisplayName("空白ContentId抛出异常")
        void testBlankContentIdThrowsException() {
            assertThatThrownBy(() -> new InlineAttachment("  ", "file.png", new byte[10], "image/png"))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("Content-ID");
        }

        @Test
        @DisplayName("空文件名抛出异常")
        void testNullFileNameThrowsException() {
            assertThatThrownBy(() -> new InlineAttachment("id", null, new byte[10], "image/png"))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("File name");
        }

        @Test
        @DisplayName("空数据抛出异常")
        void testNullDataThrowsException() {
            assertThatThrownBy(() -> new InlineAttachment("id", "file.png", null, "image/png"))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("Data");
        }

        @Test
        @DisplayName("空内容类型抛出异常")
        void testNullContentTypeThrowsException() {
            assertThatThrownBy(() -> new InlineAttachment("id", "file.png", new byte[10], null))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("Content type");
        }

        @Test
        @DisplayName("空白内容类型抛出异常")
        void testBlankContentTypeThrowsException() {
            assertThatThrownBy(() -> new InlineAttachment("id", "file.png", new byte[10], "  "))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("Content type");
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() 从字节数据创建")
        void testOfFromBytes() {
            byte[] data = "test".getBytes();
            InlineAttachment attachment = InlineAttachment.of("id", "file.txt", data, "text/plain");

            assertThat(attachment.getContentId()).isEqualTo("id");
            assertThat(attachment.getFileName()).isEqualTo("file.txt");
        }

        @Test
        @DisplayName("of() 从文件创建")
        void testOfFromFile(@TempDir Path tempDir) throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello World");

            InlineAttachment attachment = InlineAttachment.of("content-id", file, "text/plain");

            assertThat(attachment.getContentId()).isEqualTo("content-id");
            assertThat(attachment.getFileName()).isEqualTo("test.txt");
            assertThat(attachment.getSize()).isEqualTo(11);
        }

        @Test
        @DisplayName("of() 文件不存在抛出异常")
        void testOfFromNonExistentFile(@TempDir Path tempDir) {
            Path file = tempDir.resolve("nonexistent.txt");

            assertThatThrownBy(() -> InlineAttachment.of("id", file, "text/plain"))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("Failed to read file");
        }
    }

    @Nested
    @DisplayName("数据访问测试")
    class DataAccessTests {

        @Test
        @DisplayName("getInputStream() 返回数据流")
        void testGetInputStream() throws Exception {
            byte[] data = "Hello".getBytes();
            InlineAttachment attachment = new InlineAttachment("id", "file.txt", data, "text/plain");

            try (InputStream is = attachment.getInputStream()) {
                byte[] readData = is.readAllBytes();
                assertThat(readData).isEqualTo(data);
            }
        }

        @Test
        @DisplayName("getData() 返回数据副本")
        void testGetDataReturnsCopy() {
            byte[] originalData = "Hello".getBytes();
            InlineAttachment attachment = new InlineAttachment("id", "file.txt", originalData, "text/plain");

            byte[] data1 = attachment.getData();
            byte[] data2 = attachment.getData();

            assertThat(data1).isNotSameAs(data2);
        }
    }

    @Nested
    @DisplayName("Attachment接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("isInline() 返回true")
        void testIsInline() {
            InlineAttachment attachment = new InlineAttachment("id", "file.txt", new byte[10], "text/plain");
            assertThat(attachment.isInline()).isTrue();
        }

        @Test
        @DisplayName("getContentId() 返回ContentId")
        void testGetContentId() {
            InlineAttachment attachment = new InlineAttachment("my-content-id", "file.txt", new byte[10], "text/plain");
            assertThat(attachment.getContentId()).isEqualTo("my-content-id");
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString() 包含关键信息")
        void testToString() {
            InlineAttachment attachment = new InlineAttachment("logo", "logo.png", new byte[100], "image/png");

            String str = attachment.toString();
            assertThat(str).contains("logo");
            assertThat(str).contains("logo.png");
            assertThat(str).contains("image/png");
            assertThat(str).contains("100");
        }
    }
}
