package cloud.opencode.base.email;

import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ReceivedEmail
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("ReceivedEmail")
class ReceivedEmailTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("should build email with basic fields")
        void shouldBuildEmailWithBasicFields() {
            ReceivedEmail email = ReceivedEmail.builder()
                    .messageId("<123@example.com>")
                    .from("sender@example.com")
                    .fromName("Sender Name")
                    .to(List.of("recipient@example.com"))
                    .subject("Test Subject")
                    .textContent("Hello World")
                    .folder("INBOX")
                    .build();

            assertThat(email.messageId()).isEqualTo("<123@example.com>");
            assertThat(email.from()).isEqualTo("sender@example.com");
            assertThat(email.fromName()).isEqualTo("Sender Name");
            assertThat(email.to()).containsExactly("recipient@example.com");
            assertThat(email.subject()).isEqualTo("Test Subject");
            assertThat(email.textContent()).isEqualTo("Hello World");
            assertThat(email.folder()).isEqualTo("INBOX");
        }

        @Test
        @DisplayName("should build email with HTML content")
        void shouldBuildEmailWithHtmlContent() {
            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .htmlContent("<h1>Hello</h1>")
                    .build();

            assertThat(email.htmlContent()).isEqualTo("<h1>Hello</h1>");
            assertThat(email.hasHtmlContent()).isTrue();
        }

        @Test
        @DisplayName("should build email with dates")
        void shouldBuildEmailWithDates() {
            Instant sent = Instant.now().minusSeconds(3600);
            Instant received = Instant.now();

            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .sentDate(sent)
                    .receivedDate(received)
                    .build();

            assertThat(email.sentDate()).isEqualTo(sent);
            assertThat(email.receivedDate()).isEqualTo(received);
        }

        @Test
        @DisplayName("should build email with flags")
        void shouldBuildEmailWithFlags() {
            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .flags(EmailFlags.READ)
                    .build();

            assertThat(email.flags()).isEqualTo(EmailFlags.READ);
            assertThat(email.isUnread()).isFalse();
        }

        @Test
        @DisplayName("should build email with headers")
        void shouldBuildEmailWithHeaders() {
            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .headers(Map.of("X-Custom", "value", "X-Priority", "1"))
                    .build();

            assertThat(email.headers()).containsEntry("X-Custom", "value");
            assertThat(email.getHeader("X-Priority")).isEqualTo("1");
        }
    }

    @Nested
    @DisplayName("Content methods")
    class ContentMethods {

        @Test
        @DisplayName("should detect text content")
        void shouldDetectTextContent() {
            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .textContent("Hello")
                    .build();

            assertThat(email.hasTextContent()).isTrue();
            assertThat(email.hasHtmlContent()).isFalse();
        }

        @Test
        @DisplayName("should return HTML content from getContent when available")
        void shouldReturnHtmlContentFromGetContent() {
            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .textContent("Text version")
                    .htmlContent("<p>HTML version</p>")
                    .build();

            assertThat(email.getContent()).isEqualTo("<p>HTML version</p>");
        }

        @Test
        @DisplayName("should return text content from getTextOrHtmlContent when available")
        void shouldReturnTextContentFromGetTextOrHtmlContent() {
            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .textContent("Text version")
                    .htmlContent("<p>HTML version</p>")
                    .build();

            assertThat(email.getTextOrHtmlContent()).isEqualTo("Text version");
        }
    }

    @Nested
    @DisplayName("Attachments")
    class Attachments {

        @Test
        @DisplayName("should not have attachments by default")
        void shouldNotHaveAttachmentsByDefault() {
            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .build();

            assertThat(email.hasAttachments()).isFalse();
            assertThat(email.getAttachmentCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("should have attachments when added")
        void shouldHaveAttachmentsWhenAdded() {
            Attachment attachment = ByteArrayAttachment.of("file.txt", new byte[]{1, 2, 3}, "text/plain");

            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .attachments(List.of(attachment))
                    .build();

            assertThat(email.hasAttachments()).isTrue();
            assertThat(email.getAttachmentCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Flags")
    class Flags {

        @Test
        @DisplayName("should detect unread status")
        void shouldDetectUnreadStatus() {
            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .flags(EmailFlags.UNREAD)
                    .build();

            assertThat(email.isUnread()).isTrue();
        }

        @Test
        @DisplayName("should detect flagged status")
        void shouldDetectFlaggedStatus() {
            EmailFlags flagged = EmailFlags.READ.withFlagged(true);

            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .flags(flagged)
                    .build();

            assertThat(email.isFlagged()).isTrue();
        }

        @Test
        @DisplayName("should detect answered status")
        void shouldDetectAnsweredStatus() {
            EmailFlags answered = EmailFlags.READ.withAnswered(true);

            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .flags(answered)
                    .build();

            assertThat(email.isAnswered()).isTrue();
        }
    }

    @Nested
    @DisplayName("Recipients")
    class Recipients {

        @Test
        @DisplayName("should get all recipients")
        void shouldGetAllRecipients() {
            ReceivedEmail email = ReceivedEmail.builder()
                    .from("sender@example.com")
                    .to(List.of("to1@example.com", "to2@example.com"))
                    .cc(List.of("cc@example.com"))
                    .bcc(List.of("bcc@example.com"))
                    .build();

            assertThat(email.getAllRecipients())
                    .containsExactlyInAnyOrder(
                            "to1@example.com",
                            "to2@example.com",
                            "cc@example.com",
                            "bcc@example.com"
                    );
        }
    }
}
