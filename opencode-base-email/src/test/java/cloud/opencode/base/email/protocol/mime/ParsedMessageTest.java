package cloud.opencode.base.email.protocol.mime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ParsedMessage and ParsedAttachment records
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("ParsedMessage")
class ParsedMessageTest {

    @Nested
    @DisplayName("ParsedMessage record")
    class ParsedMessageRecordTests {

        @Test
        @DisplayName("should store and retrieve all fields")
        void shouldStoreAllFields() {
            Instant sentDate = Instant.parse("2026-01-15T10:30:00Z");
            Instant receivedDate = Instant.parse("2026-01-15T10:30:05Z");
            byte[] attData = "attachment data".getBytes(StandardCharsets.UTF_8);
            ParsedMessage.ParsedAttachment attachment =
                    new ParsedMessage.ParsedAttachment("file.pdf", "application/pdf", attData, false, null);

            ParsedMessage msg = new ParsedMessage(
                    "abc123@example.com",
                    "sender@example.com",
                    "John Doe",
                    List.of("recipient@example.com"),
                    List.of("cc@example.com"),
                    List.of("bcc@example.com"),
                    "reply@example.com",
                    "Test Subject",
                    "Plain text body",
                    "<h1>HTML body</h1>",
                    sentDate,
                    receivedDate,
                    4096,
                    Map.of("From", "sender@example.com", "Subject", "Test Subject"),
                    List.of(attachment)
            );

            assertThat(msg.messageId()).isEqualTo("abc123@example.com");
            assertThat(msg.from()).isEqualTo("sender@example.com");
            assertThat(msg.fromName()).isEqualTo("John Doe");
            assertThat(msg.to()).containsExactly("recipient@example.com");
            assertThat(msg.cc()).containsExactly("cc@example.com");
            assertThat(msg.bcc()).containsExactly("bcc@example.com");
            assertThat(msg.replyTo()).isEqualTo("reply@example.com");
            assertThat(msg.subject()).isEqualTo("Test Subject");
            assertThat(msg.textContent()).isEqualTo("Plain text body");
            assertThat(msg.htmlContent()).isEqualTo("<h1>HTML body</h1>");
            assertThat(msg.sentDate()).isEqualTo(sentDate);
            assertThat(msg.receivedDate()).isEqualTo(receivedDate);
            assertThat(msg.size()).isEqualTo(4096);
            assertThat(msg.headers()).containsEntry("From", "sender@example.com");
            assertThat(msg.attachments()).hasSize(1);
            assertThat(msg.attachments().getFirst()).isEqualTo(attachment);
        }

        @Test
        @DisplayName("should accept null for nullable fields")
        void shouldAcceptNullFields() {
            ParsedMessage msg = new ParsedMessage(
                    null, null, null,
                    List.of(), List.of(), List.of(),
                    null, null, null, null,
                    null, null,
                    0,
                    Map.of(),
                    List.of()
            );

            assertThat(msg.messageId()).isNull();
            assertThat(msg.from()).isNull();
            assertThat(msg.fromName()).isNull();
            assertThat(msg.replyTo()).isNull();
            assertThat(msg.subject()).isNull();
            assertThat(msg.textContent()).isNull();
            assertThat(msg.htmlContent()).isNull();
            assertThat(msg.sentDate()).isNull();
            assertThat(msg.receivedDate()).isNull();
            assertThat(msg.to()).isEmpty();
            assertThat(msg.cc()).isEmpty();
            assertThat(msg.bcc()).isEmpty();
            assertThat(msg.attachments()).isEmpty();
        }

        @Test
        @DisplayName("should implement equals for identical records")
        void shouldImplementEquals() {
            Instant now = Instant.now();
            ParsedMessage msg1 = new ParsedMessage(
                    "id1", "a@b.com", "A", List.of("c@d.com"), List.of(), List.of(),
                    null, "Subject", "text", null, now, null, 100,
                    Map.of(), List.of());
            ParsedMessage msg2 = new ParsedMessage(
                    "id1", "a@b.com", "A", List.of("c@d.com"), List.of(), List.of(),
                    null, "Subject", "text", null, now, null, 100,
                    Map.of(), List.of());

            assertThat(msg1).isEqualTo(msg2);
            assertThat(msg1.hashCode()).isEqualTo(msg2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when fields differ")
        void shouldNotBeEqualWhenDifferent() {
            ParsedMessage msg1 = new ParsedMessage(
                    "id1", "a@b.com", null, List.of(), List.of(), List.of(),
                    null, "Subject 1", null, null, null, null, 0,
                    Map.of(), List.of());
            ParsedMessage msg2 = new ParsedMessage(
                    "id2", "a@b.com", null, List.of(), List.of(), List.of(),
                    null, "Subject 2", null, null, null, null, 0,
                    Map.of(), List.of());

            assertThat(msg1).isNotEqualTo(msg2);
        }

        @Test
        @DisplayName("should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            ParsedMessage msg = new ParsedMessage(
                    "id@example.com", "sender@example.com", "Sender Name",
                    List.of("to@example.com"), List.of(), List.of(),
                    null, "Hello", "body", null, null, null, 42,
                    Map.of(), List.of());

            String str = msg.toString();
            assertThat(str).contains("id@example.com");
            assertThat(str).contains("sender@example.com");
            assertThat(str).contains("Hello");
        }

        @Test
        @DisplayName("should handle multiple To and CC recipients")
        void shouldHandleMultipleRecipients() {
            ParsedMessage msg = new ParsedMessage(
                    "id1", "from@example.com", null,
                    List.of("to1@example.com", "to2@example.com", "to3@example.com"),
                    List.of("cc1@example.com", "cc2@example.com"),
                    List.of("bcc1@example.com"),
                    null, "Subject", null, null, null, null, 0,
                    Map.of(), List.of());

            assertThat(msg.to()).hasSize(3);
            assertThat(msg.cc()).hasSize(2);
            assertThat(msg.bcc()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("ParsedAttachment record")
    class ParsedAttachmentRecordTests {

        @Test
        @DisplayName("should store all fields for regular attachment")
        void shouldStoreFieldsForRegularAttachment() {
            byte[] data = "file content".getBytes(StandardCharsets.UTF_8);
            ParsedMessage.ParsedAttachment att =
                    new ParsedMessage.ParsedAttachment("report.pdf", "application/pdf", data, false, null);

            assertThat(att.fileName()).isEqualTo("report.pdf");
            assertThat(att.contentType()).isEqualTo("application/pdf");
            assertThat(att.data()).isEqualTo(data);
            assertThat(att.inline()).isFalse();
            assertThat(att.contentId()).isNull();
        }

        @Test
        @DisplayName("should store all fields for inline attachment")
        void shouldStoreFieldsForInlineAttachment() {
            byte[] data = "image data".getBytes(StandardCharsets.UTF_8);
            ParsedMessage.ParsedAttachment att =
                    new ParsedMessage.ParsedAttachment("logo.png", "image/png", data, true, "cid123");

            assertThat(att.fileName()).isEqualTo("logo.png");
            assertThat(att.contentType()).isEqualTo("image/png");
            assertThat(att.data()).isEqualTo(data);
            assertThat(att.inline()).isTrue();
            assertThat(att.contentId()).isEqualTo("cid123");
        }

        @Test
        @DisplayName("should allow null fileName")
        void shouldAllowNullFileName() {
            byte[] data = "data".getBytes(StandardCharsets.UTF_8);
            ParsedMessage.ParsedAttachment att =
                    new ParsedMessage.ParsedAttachment(null, "application/octet-stream", data, false, null);

            assertThat(att.fileName()).isNull();
        }

        @Test
        @DisplayName("should allow null contentId for non-inline attachment")
        void shouldAllowNullContentIdForNonInline() {
            byte[] data = "data".getBytes(StandardCharsets.UTF_8);
            ParsedMessage.ParsedAttachment att =
                    new ParsedMessage.ParsedAttachment("file.zip", "application/zip", data, false, null);

            assertThat(att.contentId()).isNull();
            assertThat(att.inline()).isFalse();
        }

        @Test
        @DisplayName("should implement equals for identical attachments")
        void shouldImplementEquals() {
            byte[] data = "same data".getBytes(StandardCharsets.UTF_8);
            ParsedMessage.ParsedAttachment att1 =
                    new ParsedMessage.ParsedAttachment("f.txt", "text/plain", data, false, null);
            ParsedMessage.ParsedAttachment att2 =
                    new ParsedMessage.ParsedAttachment("f.txt", "text/plain", data, false, null);

            assertThat(att1).isEqualTo(att2);
            assertThat(att1.hashCode()).isEqualTo(att2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when inline flag differs")
        void shouldNotBeEqualWhenInlineDiffers() {
            byte[] data = "data".getBytes(StandardCharsets.UTF_8);
            ParsedMessage.ParsedAttachment att1 =
                    new ParsedMessage.ParsedAttachment("f.png", "image/png", data, true, "cid1");
            ParsedMessage.ParsedAttachment att2 =
                    new ParsedMessage.ParsedAttachment("f.png", "image/png", data, false, "cid1");

            assertThat(att1).isNotEqualTo(att2);
        }

        @Test
        @DisplayName("should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            byte[] data = "data".getBytes(StandardCharsets.UTF_8);
            ParsedMessage.ParsedAttachment att =
                    new ParsedMessage.ParsedAttachment("photo.jpg", "image/jpeg", data, true, "photo1");

            String str = att.toString();
            assertThat(str).contains("photo.jpg");
            assertThat(str).contains("image/jpeg");
        }

        @Test
        @DisplayName("should handle empty byte array data")
        void shouldHandleEmptyData() {
            byte[] data = new byte[0];
            ParsedMessage.ParsedAttachment att =
                    new ParsedMessage.ParsedAttachment("empty.bin", "application/octet-stream", data, false, null);

            assertThat(att.data()).isEmpty();
        }

        @Test
        @DisplayName("should handle various content types")
        void shouldHandleVariousContentTypes() {
            byte[] data = "x".getBytes(StandardCharsets.UTF_8);

            ParsedMessage.ParsedAttachment pdf =
                    new ParsedMessage.ParsedAttachment("f.pdf", "application/pdf", data, false, null);
            ParsedMessage.ParsedAttachment xls =
                    new ParsedMessage.ParsedAttachment("f.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", data, false, null);
            ParsedMessage.ParsedAttachment mp3 =
                    new ParsedMessage.ParsedAttachment("f.mp3", "audio/mpeg", data, false, null);

            assertThat(pdf.contentType()).isEqualTo("application/pdf");
            assertThat(xls.contentType()).startsWith("application/vnd.");
            assertThat(mp3.contentType()).isEqualTo("audio/mpeg");
        }
    }
}
