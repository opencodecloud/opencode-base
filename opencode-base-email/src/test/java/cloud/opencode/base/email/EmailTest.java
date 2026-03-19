package cloud.opencode.base.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Email
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("Email")
class EmailTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("should build simple text email")
        void shouldBuildSimpleTextEmail() {
            Email email = Email.builder()
                    .from("sender@example.com")
                    .to("recipient@example.com")
                    .subject("Test Subject")
                    .text("Hello World")
                    .build();

            assertThat(email.from()).isEqualTo("sender@example.com");
            assertThat(email.to()).containsExactly("recipient@example.com");
            assertThat(email.subject()).isEqualTo("Test Subject");
            assertThat(email.content()).isEqualTo("Hello World");
            assertThat(email.html()).isFalse();
        }

        @Test
        @DisplayName("should build HTML email")
        void shouldBuildHtmlEmail() {
            Email email = Email.builder()
                    .from("sender@example.com")
                    .to("recipient@example.com")
                    .subject("Test")
                    .html("<h1>Hello</h1>")
                    .build();

            assertThat(email.content()).isEqualTo("<h1>Hello</h1>");
            assertThat(email.html()).isTrue();
        }

        @Test
        @DisplayName("should build email with multiple recipients")
        void shouldBuildEmailWithMultipleRecipients() {
            Email email = Email.builder()
                    .from("sender@example.com")
                    .to("user1@example.com", "user2@example.com")
                    .cc("cc@example.com")
                    .bcc("bcc@example.com")
                    .subject("Test")
                    .text("Hello")
                    .build();

            assertThat(email.to()).hasSize(2);
            assertThat(email.cc()).containsExactly("cc@example.com");
            assertThat(email.bcc()).containsExactly("bcc@example.com");
        }

        @Test
        @DisplayName("should build email with from name")
        void shouldBuildEmailWithFromName() {
            Email email = Email.builder()
                    .from("sender@example.com", "Sender Name")
                    .to("recipient@example.com")
                    .subject("Test")
                    .text("Hello")
                    .build();

            assertThat(email.from()).isEqualTo("sender@example.com");
            assertThat(email.fromName()).isEqualTo("Sender Name");
        }

        @Test
        @DisplayName("should build email with reply-to")
        void shouldBuildEmailWithReplyTo() {
            Email email = Email.builder()
                    .from("sender@example.com")
                    .to("recipient@example.com")
                    .replyTo("reply@example.com")
                    .subject("Test")
                    .text("Hello")
                    .build();

            assertThat(email.replyTo()).isEqualTo("reply@example.com");
        }

        @Test
        @DisplayName("should build email with custom headers")
        void shouldBuildEmailWithCustomHeaders() {
            Email email = Email.builder()
                    .from("sender@example.com")
                    .to("recipient@example.com")
                    .subject("Test")
                    .text("Hello")
                    .header("X-Custom", "value")
                    .build();

            assertThat(email.headers()).containsEntry("X-Custom", "value");
        }

        @Test
        @DisplayName("should build email with priority")
        void shouldBuildEmailWithPriority() {
            Email email = Email.builder()
                    .from("sender@example.com")
                    .to("recipient@example.com")
                    .subject("Urgent")
                    .text("Hello")
                    .priority(Email.Priority.HIGH)
                    .build();

            assertThat(email.priority()).isEqualTo(Email.Priority.HIGH);
        }
    }

    @Nested
    @DisplayName("Priority")
    class PriorityTest {

        @Test
        @DisplayName("should have correct priority values")
        void shouldHaveCorrectPriorityValues() {
            assertThat(Email.Priority.HIGH.getValue()).isEqualTo(1);
            assertThat(Email.Priority.NORMAL.getValue()).isEqualTo(3);
            assertThat(Email.Priority.LOW.getValue()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Attachments")
    class Attachments {

        @Test
        @DisplayName("should not have attachments by default")
        void shouldNotHaveAttachmentsByDefault() {
            Email email = Email.builder()
                    .from("sender@example.com")
                    .to("recipient@example.com")
                    .subject("Test")
                    .text("Hello")
                    .build();

            assertThat(email.hasAttachments()).isFalse();
            assertThat(email.hasInlineAttachments()).isFalse();
        }
    }
}
