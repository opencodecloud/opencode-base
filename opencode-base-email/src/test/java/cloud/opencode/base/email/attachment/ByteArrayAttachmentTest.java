package cloud.opencode.base.email.attachment;

import cloud.opencode.base.email.exception.EmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * ByteArrayAttachment 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("ByteArrayAttachment 测试")
class ByteArrayAttachmentTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建简单附件")
        void testSimpleConstructor() {
            byte[] data = "Hello World".getBytes();
            ByteArrayAttachment attachment = new ByteArrayAttachment("test.txt", data);

            assertThat(attachment.getFileName()).isEqualTo("test.txt");
            assertThat(attachment.getContentType()).isEqualTo("application/octet-stream");
            assertThat(attachment.getSize()).isEqualTo(data.length);
        }

        @Test
        @DisplayName("创建带内容类型的附件")
        void testConstructorWithContentType() {
            byte[] data = "Hello".getBytes();
            ByteArrayAttachment attachment = new ByteArrayAttachment("test.txt", data, "text/plain");

            assertThat(attachment.getContentType()).isEqualTo("text/plain");
        }

        @Test
        @DisplayName("空文件名抛出异常")
        void testNullFileNameThrowsException() {
            assertThatThrownBy(() -> new ByteArrayAttachment(null, new byte[10]))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("File name");
        }

        @Test
        @DisplayName("空白文件名抛出异常")
        void testBlankFileNameThrowsException() {
            assertThatThrownBy(() -> new ByteArrayAttachment("  ", new byte[10]))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("File name");
        }

        @Test
        @DisplayName("空数据抛出异常")
        void testNullDataThrowsException() {
            assertThatThrownBy(() -> new ByteArrayAttachment("test.txt", null))
                    .isInstanceOf(EmailException.class)
                    .hasMessageContaining("Data");
        }

        @Test
        @DisplayName("空内容类型使用默认值")
        void testNullContentTypeUsesDefault() {
            ByteArrayAttachment attachment = new ByteArrayAttachment("test.txt", new byte[10], null);
            assertThat(attachment.getContentType()).isEqualTo("application/octet-stream");
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() 创建附件")
        void testOfWithContentType() {
            byte[] data = "test".getBytes();
            ByteArrayAttachment attachment = ByteArrayAttachment.of("file.txt", data, "text/plain");

            assertThat(attachment.getFileName()).isEqualTo("file.txt");
            assertThat(attachment.getContentType()).isEqualTo("text/plain");
        }

        @Test
        @DisplayName("of() 创建附件不带内容类型")
        void testOfWithoutContentType() {
            byte[] data = "test".getBytes();
            ByteArrayAttachment attachment = ByteArrayAttachment.of("file.txt", data);

            assertThat(attachment.getFileName()).isEqualTo("file.txt");
            assertThat(attachment.getContentType()).isEqualTo("application/octet-stream");
        }
    }

    @Nested
    @DisplayName("数据访问测试")
    class DataAccessTests {

        @Test
        @DisplayName("getInputStream() 返回数据流")
        void testGetInputStream() throws Exception {
            byte[] data = "Hello World".getBytes();
            ByteArrayAttachment attachment = new ByteArrayAttachment("test.txt", data);

            try (InputStream is = attachment.getInputStream()) {
                byte[] readData = is.readAllBytes();
                assertThat(readData).isEqualTo(data);
            }
        }

        @Test
        @DisplayName("getData() 返回数据副本")
        void testGetDataReturnsCopy() {
            byte[] originalData = "Hello".getBytes();
            ByteArrayAttachment attachment = new ByteArrayAttachment("test.txt", originalData);

            byte[] data1 = attachment.getData();
            byte[] data2 = attachment.getData();

            assertThat(data1).isEqualTo(originalData);
            assertThat(data1).isNotSameAs(data2); // 防御性拷贝
        }

        @Test
        @DisplayName("原始数据修改不影响附件")
        void testDefensiveCopy() {
            byte[] originalData = "Hello".getBytes();
            ByteArrayAttachment attachment = new ByteArrayAttachment("test.txt", originalData);

            originalData[0] = 'X'; // 修改原始数据

            assertThat(attachment.getData()[0]).isEqualTo((byte) 'H'); // 附件数据不变
        }
    }

    @Nested
    @DisplayName("Attachment接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("isInline() 返回false")
        void testIsInline() {
            ByteArrayAttachment attachment = new ByteArrayAttachment("test.txt", new byte[10]);
            assertThat(attachment.isInline()).isFalse();
        }

        @Test
        @DisplayName("getContentId() 返回null")
        void testGetContentId() {
            ByteArrayAttachment attachment = new ByteArrayAttachment("test.txt", new byte[10]);
            assertThat(attachment.getContentId()).isNull();
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString() 包含关键信息")
        void testToString() {
            ByteArrayAttachment attachment = new ByteArrayAttachment("test.txt", new byte[100], "text/plain");

            String str = attachment.toString();
            assertThat(str).contains("test.txt");
            assertThat(str).contains("text/plain");
            assertThat(str).contains("100");
        }
    }
}
