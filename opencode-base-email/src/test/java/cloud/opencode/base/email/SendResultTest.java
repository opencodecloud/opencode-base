package cloud.opencode.base.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SendResult
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("SendResult")
class SendResultTest {

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("should create success result with message ID")
        void shouldCreateSuccessWithMessageId() {
            SendResult result = SendResult.success("<123@example.com>");

            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isEqualTo("<123@example.com>");
            assertThat(result.sentAt()).isNotNull();
            assertThat(result.hasMessageId()).isTrue();
        }

        @Test
        @DisplayName("should create failure result")
        void shouldCreateFailureResult() {
            SendResult result = SendResult.failure();

            assertThat(result.success()).isFalse();
            assertThat(result.messageId()).isNull();
            assertThat(result.sentAt()).isNotNull();
            assertThat(result.hasMessageId()).isFalse();
        }
    }

    @Nested
    @DisplayName("Message ID")
    class MessageId {

        @Test
        @DisplayName("should detect blank message ID")
        void shouldDetectBlankMessageId() {
            SendResult result = new SendResult("", Instant.now(), true);

            assertThat(result.hasMessageId()).isFalse();
        }

        @Test
        @DisplayName("should detect null message ID")
        void shouldDetectNullMessageId() {
            SendResult result = new SendResult(null, Instant.now(), true);

            assertThat(result.hasMessageId()).isFalse();
        }
    }
}
