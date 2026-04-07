package cloud.opencode.base.email.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.*;

/**
 * ProtocolException unit tests
 * ProtocolException 单元测试
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("ProtocolException Tests")
class ProtocolExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("message-only constructor sets message, replyCode = -1, no cause")
        void messageOnlyConstructor() {
            var ex = new ProtocolException("test error");

            assertThat(ex.getMessage()).isEqualTo("test error");
            assertThat(ex.getReplyCode()).isEqualTo(-1);
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("message + cause constructor sets both, replyCode = -1")
        void messageAndCauseConstructor() {
            var cause = new RuntimeException("root");
            var ex = new ProtocolException("test error", cause);

            assertThat(ex.getMessage()).isEqualTo("test error");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getReplyCode()).isEqualTo(-1);
        }

        @Test
        @DisplayName("message + replyCode constructor sets both, no cause")
        void messageAndReplyCodeConstructor() {
            var ex = new ProtocolException("auth failed", 535);

            assertThat(ex.getMessage()).isEqualTo("auth failed");
            assertThat(ex.getReplyCode()).isEqualTo(535);
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("message + replyCode + cause constructor sets all three")
        void allArgsConstructor() {
            var cause = new RuntimeException("root");
            var ex = new ProtocolException("auth failed", 535, cause);

            assertThat(ex.getMessage()).isEqualTo("auth failed");
            assertThat(ex.getReplyCode()).isEqualTo(535);
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("ProtocolException extends Exception")
        void extendsException() {
            var ex = new ProtocolException("test");
            assertThat(ex).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("isAuthenticationFailure Tests")
    class IsAuthenticationFailureTests {

        @Test
        @DisplayName("reply code 535 is authentication failure")
        void replyCode535() {
            var ex = new ProtocolException("denied", 535);
            assertThat(ex.isAuthenticationFailure()).isTrue();
        }

        @Test
        @DisplayName("reply code 534 is authentication failure")
        void replyCode534() {
            var ex = new ProtocolException("need stronger auth", 534);
            assertThat(ex.isAuthenticationFailure()).isTrue();
        }

        @Test
        @DisplayName("message containing 'auth' is authentication failure")
        void messageContainingAuth() {
            var ex = new ProtocolException("Authentication required");
            assertThat(ex.isAuthenticationFailure()).isTrue();
        }

        @Test
        @DisplayName("message containing 'AUTH' (uppercase) is authentication failure")
        void messageContainingAuthUpperCase() {
            var ex = new ProtocolException("AUTH PLAIN failed");
            assertThat(ex.isAuthenticationFailure()).isTrue();
        }

        @Test
        @DisplayName("non-auth reply code with unrelated message is not authentication failure")
        void notAuthFailure() {
            var ex = new ProtocolException("mailbox full", 452);
            assertThat(ex.isAuthenticationFailure()).isFalse();
        }

        @Test
        @DisplayName("null message with non-auth code returns false")
        void nullMessageNonAuthCode() {
            // Use the message+replyCode constructor with null message
            var ex = new ProtocolException(null, 250);
            assertThat(ex.isAuthenticationFailure()).isFalse();
        }
    }

    @Nested
    @DisplayName("isTimeout Tests")
    class IsTimeoutTests {

        @Test
        @DisplayName("SocketTimeoutException cause returns true")
        void socketTimeoutCause() {
            var cause = new SocketTimeoutException("read timed out");
            var ex = new ProtocolException("timeout", cause);
            assertThat(ex.isTimeout()).isTrue();
        }

        @Test
        @DisplayName("other cause returns false")
        void otherCause() {
            var cause = new RuntimeException("something else");
            var ex = new ProtocolException("error", cause);
            assertThat(ex.isTimeout()).isFalse();
        }

        @Test
        @DisplayName("no cause returns false")
        void noCause() {
            var ex = new ProtocolException("error");
            assertThat(ex.isTimeout()).isFalse();
        }
    }

    @Nested
    @DisplayName("isConnectionFailure Tests")
    class IsConnectionFailureTests {

        @Test
        @DisplayName("ConnectException cause returns true")
        void connectExceptionCause() {
            var cause = new ConnectException("Connection refused");
            var ex = new ProtocolException("connection failed", cause);
            assertThat(ex.isConnectionFailure()).isTrue();
        }

        @Test
        @DisplayName("UnknownHostException cause returns true")
        void unknownHostCause() {
            var cause = new UnknownHostException("no.such.host");
            var ex = new ProtocolException("connection failed", cause);
            assertThat(ex.isConnectionFailure()).isTrue();
        }

        @Test
        @DisplayName("other cause returns false")
        void otherCause() {
            var cause = new RuntimeException("something else");
            var ex = new ProtocolException("error", cause);
            assertThat(ex.isConnectionFailure()).isFalse();
        }

        @Test
        @DisplayName("no cause returns false")
        void noCause() {
            var ex = new ProtocolException("error");
            assertThat(ex.isConnectionFailure()).isFalse();
        }
    }

    @Nested
    @DisplayName("getReplyCode Tests")
    class GetReplyCodeTests {

        @Test
        @DisplayName("returns the set reply code")
        void returnsSetCode() {
            var ex = new ProtocolException("ok", 250);
            assertThat(ex.getReplyCode()).isEqualTo(250);
        }

        @Test
        @DisplayName("returns -1 when no reply code specified")
        void returnsMinusOneWhenNotSet() {
            var ex = new ProtocolException("error");
            assertThat(ex.getReplyCode()).isEqualTo(-1);
        }

        @Test
        @DisplayName("returns -1 for message+cause constructor")
        void returnsMinusOneForCauseConstructor() {
            var ex = new ProtocolException("error", new RuntimeException());
            assertThat(ex.getReplyCode()).isEqualTo(-1);
        }
    }
}
