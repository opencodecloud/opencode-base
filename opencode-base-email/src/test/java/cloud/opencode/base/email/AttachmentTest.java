package cloud.opencode.base.email;

import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * AttachmentTest Tests
 * AttachmentTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("Attachment 接口测试")
class AttachmentTest {

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("isInline默认返回false")
        void testDefaultIsInline() {
            Attachment attachment = createAttachment("test.txt", "text/plain", new byte[]{1, 2, 3});
            assertThat(attachment.isInline()).isFalse();
        }

        @Test
        @DisplayName("getContentId默认返回null")
        void testDefaultGetContentId() {
            Attachment attachment = createAttachment("test.txt", "text/plain", new byte[]{1, 2, 3});
            assertThat(attachment.getContentId()).isNull();
        }
    }

    @Nested
    @DisplayName("实现测试")
    class ImplementationTests {

        @Test
        @DisplayName("getFileName返回文件名")
        void testGetFileName() {
            Attachment attachment = createAttachment("document.pdf", "application/pdf", new byte[10]);
            assertThat(attachment.getFileName()).isEqualTo("document.pdf");
        }

        @Test
        @DisplayName("getContentType返回MIME类型")
        void testGetContentType() {
            Attachment attachment = createAttachment("image.png", "image/png", new byte[5]);
            assertThat(attachment.getContentType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("getSize返回附件大小")
        void testGetSize() {
            byte[] data = new byte[]{1, 2, 3, 4, 5};
            Attachment attachment = createAttachment("test.txt", "text/plain", data);
            assertThat(attachment.getSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("getInputStream返回输入流")
        void testGetInputStream() {
            byte[] data = {10, 20, 30};
            Attachment attachment = createAttachment("data.bin", "application/octet-stream", data);
            assertThat(attachment.getInputStream()).isNotNull();
        }
    }

    @Nested
    @DisplayName("内嵌附件测试")
    class InlineAttachmentTests {

        @Test
        @DisplayName("内嵌附件isInline返回true")
        void testInlineIsTrue() {
            Attachment inline = createInlineAttachment("logo.png", "image/png", new byte[10], "logo-cid");
            assertThat(inline.isInline()).isTrue();
        }

        @Test
        @DisplayName("内嵌附件getContentId返回CID")
        void testInlineContentId() {
            Attachment inline = createInlineAttachment("logo.png", "image/png", new byte[10], "logo-cid");
            assertThat(inline.getContentId()).isEqualTo("logo-cid");
        }
    }

    private Attachment createAttachment(String fileName, String contentType, byte[] data) {
        return new Attachment() {
            @Override public String getFileName() { return fileName; }
            @Override public String getContentType() { return contentType; }
            @Override public InputStream getInputStream() { return new ByteArrayInputStream(data); }
            @Override public long getSize() { return data.length; }
        };
    }

    private Attachment createInlineAttachment(String fileName, String contentType, byte[] data, String contentId) {
        return new Attachment() {
            @Override public String getFileName() { return fileName; }
            @Override public String getContentType() { return contentType; }
            @Override public InputStream getInputStream() { return new ByteArrayInputStream(data); }
            @Override public long getSize() { return data.length; }
            @Override public boolean isInline() { return true; }
            @Override public String getContentId() { return contentId; }
        };
    }
}
