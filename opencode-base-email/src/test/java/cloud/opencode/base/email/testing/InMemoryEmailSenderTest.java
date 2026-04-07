package cloud.opencode.base.email.testing;

import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.SendResult;
import cloud.opencode.base.email.exception.EmailSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InMemoryEmailSender")
class InMemoryEmailSenderTest {

    private InMemoryEmailSender sender;

    @BeforeEach
    void setUp() {
        sender = new InMemoryEmailSender();
    }

    private Email createEmail(String to, String subject) {
        return Email.builder()
                .from("sender@test.com")
                .to(to)
                .subject(subject)
                .text("content")
                .build();
    }

    @Nested
    @DisplayName("send()")
    class Send {

        @Test
        @DisplayName("should capture sent email")
        void captureSent() {
            Email email = createEmail("user@test.com", "Test");
            sender.send(email);
            assertThat(sender.getSentCount()).isEqualTo(1);
            assertThat(sender.getLastEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("should reject null email")
        void rejectNull() {
            assertThatThrownBy(() -> sender.send(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("sendWithResult()")
    class SendWithResult {

        @Test
        @DisplayName("should return success result with message ID")
        void successResult() {
            Email email = createEmail("user@test.com", "Test");
            SendResult result = sender.sendWithResult(email);
            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("query methods")
    class Query {

        @Test
        @DisplayName("findByRecipient should match")
        void findByRecipient() {
            sender.send(createEmail("a@test.com", "A"));
            sender.send(createEmail("b@test.com", "B"));
            assertThat(sender.findByRecipient("a@test.com")).hasSize(1);
            assertThat(sender.findByRecipient("c@test.com")).isEmpty();
        }

        @Test
        @DisplayName("findBySubject should match contains")
        void findBySubject() {
            sender.send(createEmail("a@test.com", "Hello World"));
            sender.send(createEmail("b@test.com", "Goodbye"));
            assertThat(sender.findBySubject("Hello")).hasSize(1);
            assertThat(sender.findBySubject("World")).hasSize(1);
            assertThat(sender.findBySubject("Nothing")).isEmpty();
        }

        @Test
        @DisplayName("findBy predicate should work")
        void findByPredicate() {
            sender.send(createEmail("a@test.com", "HTML"));
            Email htmlEmail = Email.builder().from("x@test.com").to("y@test.com").subject("HTML2").html("<b>bold</b>").build();
            sender.send(htmlEmail);
            assertThat(sender.findBy(Email::html)).hasSize(1);
        }

        @Test
        @DisplayName("hasSentTo should check recipients")
        void hasSentTo() {
            sender.send(createEmail("user@test.com", "Test"));
            assertThat(sender.hasSentTo("user@test.com")).isTrue();
            assertThat(sender.hasSentTo("other@test.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("clear()")
    class Clear {

        @Test
        @DisplayName("should clear all sent emails")
        void clearAll() {
            sender.send(createEmail("a@test.com", "A"));
            sender.send(createEmail("b@test.com", "B"));
            sender.clear();
            assertThat(sender.getSentCount()).isZero();
            assertThat(sender.getLastEmail()).isNull();
        }
    }

    @Nested
    @DisplayName("failure simulation")
    class FailureSimulation {

        @Test
        @DisplayName("should throw when failure predicate matches")
        void simulateFailure() {
            sender.simulateFailure(e -> e.subject() != null && e.subject().contains("FAIL"));
            assertThatThrownBy(() -> sender.send(createEmail("a@test.com", "FAIL THIS")))
                    .isInstanceOf(EmailSendException.class);
            // Non-matching should succeed
            sender.send(createEmail("a@test.com", "OK"));
            assertThat(sender.getSentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should stop simulating after clear")
        void clearSimulator() {
            sender.simulateFailure(e -> true);
            sender.clearFailureSimulator();
            sender.send(createEmail("a@test.com", "Should work"));
            assertThat(sender.getSentCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getLastEmail()")
    class GetLastEmail {

        @Test
        @DisplayName("should return null when no emails sent")
        void emptyReturnsNull() {
            assertThat(sender.getLastEmail()).isNull();
        }

        @Test
        @DisplayName("should return most recent email")
        void returnsLast() {
            sender.send(createEmail("a@test.com", "First"));
            sender.send(createEmail("b@test.com", "Second"));
            assertThat(sender.getLastEmail().subject()).isEqualTo("Second");
        }
    }
}
