package cloud.opencode.base.captcha.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaRateLimitException Test - Unit tests for CAPTCHA rate limit exception
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaRateLimitExceptionTest {

    @Nested
    @DisplayName("ClientId Only Constructor Tests")
    class ClientIdOnlyConstructorTests {

        @Test
        @DisplayName("should create exception with client ID")
        void shouldCreateExceptionWithClientId() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client-123");

            assertThat(ex.getClientId()).isEqualTo("client-123");
            assertThat(ex.getRetryAfter()).isNull();
            assertThat(ex.getRawMessage()).isEqualTo("Rate limit exceeded for client: client-123");
        }

        @Test
        @DisplayName("should create exception with IP address as client ID")
        void shouldCreateExceptionWithIpAddressAsClientId() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("192.168.1.100");

            assertThat(ex.getClientId()).isEqualTo("192.168.1.100");
            assertThat(ex.getRetryAfter()).isNull();
            assertThat(ex.getMessage()).contains("192.168.1.100");
        }

        @Test
        @DisplayName("should create exception with null client ID")
        void shouldCreateExceptionWithNullClientId() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException((String) null);

            assertThat(ex.getClientId()).isNull();
            assertThat(ex.getRetryAfter()).isNull();
            assertThat(ex.getRawMessage()).isEqualTo("Rate limit exceeded for client: null");
        }

        @Test
        @DisplayName("should create exception with empty client ID")
        void shouldCreateExceptionWithEmptyClientId() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("");

            assertThat(ex.getClientId()).isEmpty();
            assertThat(ex.getRetryAfter()).isNull();
            assertThat(ex.getRawMessage()).isEqualTo("Rate limit exceeded for client: ");
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client");

            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create exception with whitespace client ID")
        void shouldCreateExceptionWithWhitespaceClientId() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("  ");

            assertThat(ex.getClientId()).isEqualTo("  ");
            assertThat(ex.getRetryAfter()).isNull();
        }
    }

    @Nested
    @DisplayName("ClientId and RetryAfter Constructor Tests")
    class ClientIdAndRetryAfterConstructorTests {

        @Test
        @DisplayName("should create exception with client ID and retry duration")
        void shouldCreateExceptionWithClientIdAndRetryDuration() {
            Duration retryAfter = Duration.ofSeconds(30);
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client-456", retryAfter);

            assertThat(ex.getClientId()).isEqualTo("client-456");
            assertThat(ex.getRetryAfter()).isEqualTo(retryAfter);
            assertThat(ex.getRawMessage()).isEqualTo(
                "Rate limit exceeded for client: client-456. Retry after: 30 seconds");
        }

        @Test
        @DisplayName("should create exception with zero duration")
        void shouldCreateExceptionWithZeroDuration() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client-zero", Duration.ZERO);

            assertThat(ex.getClientId()).isEqualTo("client-zero");
            assertThat(ex.getRetryAfter()).isEqualTo(Duration.ZERO);
            assertThat(ex.getMessage()).contains("0 seconds");
        }

        @Test
        @DisplayName("should create exception with one minute duration")
        void shouldCreateExceptionWithOneMinuteDuration() {
            Duration retryAfter = Duration.ofMinutes(1);
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client-min", retryAfter);

            assertThat(ex.getRetryAfter()).isEqualTo(retryAfter);
            assertThat(ex.getMessage()).contains("60 seconds");
        }

        @Test
        @DisplayName("should create exception with one hour duration")
        void shouldCreateExceptionWithOneHourDuration() {
            Duration retryAfter = Duration.ofHours(1);
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client-long", retryAfter);

            assertThat(ex.getRetryAfter()).isEqualTo(retryAfter);
            assertThat(ex.getMessage()).contains("3600 seconds");
        }

        @Test
        @DisplayName("should create exception with one second duration")
        void shouldCreateExceptionWithOneSecondDuration() {
            Duration retryAfter = Duration.ofSeconds(1);
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client-1s", retryAfter);

            assertThat(ex.getRetryAfter()).isEqualTo(retryAfter);
            assertThat(ex.getMessage()).contains("1 seconds");
        }

        @Test
        @DisplayName("should create exception with large duration")
        void shouldCreateExceptionWithLargeDuration() {
            Duration retryAfter = Duration.ofDays(1);
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client-day", retryAfter);

            assertThat(ex.getRetryAfter()).isEqualTo(retryAfter);
            assertThat(ex.getMessage()).contains("86400 seconds");
        }

        @Test
        @DisplayName("should throw NullPointerException with null retry duration")
        void shouldThrowNpeWithNullRetryDuration() {
            assertThatThrownBy(() -> new CaptchaRateLimitException("client-null", null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should create exception with null client ID and valid duration")
        void shouldCreateExceptionWithNullClientIdAndValidDuration() {
            Duration retryAfter = Duration.ofSeconds(15);
            CaptchaRateLimitException ex = new CaptchaRateLimitException(null, retryAfter);

            assertThat(ex.getClientId()).isNull();
            assertThat(ex.getRetryAfter()).isEqualTo(retryAfter);
            assertThat(ex.getMessage()).contains("15 seconds");
        }

        @Test
        @DisplayName("should have no cause")
        void shouldHaveNoCause() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException(
                "client", Duration.ofSeconds(10));

            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should format message with 'Retry after' segment")
        void shouldFormatMessageWithRetryAfterSegment() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException(
                "test-client", Duration.ofSeconds(120));

            assertThat(ex.getRawMessage())
                .isEqualTo("Rate limit exceeded for client: test-client. Retry after: 120 seconds");
        }
    }

    @Nested
    @DisplayName("getClientId Method Tests")
    class GetClientIdMethodTests {

        @Test
        @DisplayName("should return exact client ID from single-arg constructor")
        void shouldReturnExactClientIdFromSingleArgConstructor() {
            String clientId = "test-client-id-12345";
            CaptchaRateLimitException ex = new CaptchaRateLimitException(clientId);

            assertThat(ex.getClientId()).isEqualTo(clientId);
            assertThat(ex.getClientId()).isSameAs(clientId);
        }

        @Test
        @DisplayName("should return exact client ID from two-arg constructor")
        void shouldReturnExactClientIdFromTwoArgConstructor() {
            String clientId = "two-arg-client-id";
            CaptchaRateLimitException ex = new CaptchaRateLimitException(
                clientId, Duration.ofSeconds(5));

            assertThat(ex.getClientId()).isEqualTo(clientId);
            assertThat(ex.getClientId()).isSameAs(clientId);
        }

        @Test
        @DisplayName("should return client ID with special format")
        void shouldReturnClientIdWithSpecialFormat() {
            String clientId = "user:session:abc123:device:mobile";
            CaptchaRateLimitException ex = new CaptchaRateLimitException(clientId);

            assertThat(ex.getClientId()).isEqualTo(clientId);
        }

        @Test
        @DisplayName("should return IPv6 address as client ID")
        void shouldReturnIpv6AddressAsClientId() {
            String clientId = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
            CaptchaRateLimitException ex = new CaptchaRateLimitException(clientId);

            assertThat(ex.getClientId()).isEqualTo(clientId);
        }

        @Test
        @DisplayName("should return consistent client ID on multiple calls")
        void shouldReturnConsistentClientIdOnMultipleCalls() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("stable-client");

            assertThat(ex.getClientId()).isEqualTo(ex.getClientId());
            assertThat(ex.getClientId()).isSameAs(ex.getClientId());
        }
    }

    @Nested
    @DisplayName("getRetryAfter Method Tests")
    class GetRetryAfterMethodTests {

        @Test
        @DisplayName("should return null when not provided")
        void shouldReturnNullWhenNotProvided() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client");

            assertThat(ex.getRetryAfter()).isNull();
        }

        @Test
        @DisplayName("should return exact duration")
        void shouldReturnExactDuration() {
            Duration retryAfter = Duration.ofSeconds(45);
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client", retryAfter);

            assertThat(ex.getRetryAfter()).isEqualTo(retryAfter);
        }

        @Test
        @DisplayName("should return duration with nanoseconds")
        void shouldReturnDurationWithNanoseconds() {
            Duration retryAfter = Duration.ofSeconds(10).plusNanos(500_000_000);
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client", retryAfter);

            assertThat(ex.getRetryAfter()).isEqualTo(retryAfter);
            assertThat(ex.getRetryAfter().toMillis()).isEqualTo(10_500);
        }

        @Test
        @DisplayName("should preserve duration immutability")
        void shouldPreserveDurationImmutability() {
            Duration retryAfter = Duration.ofSeconds(30);
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client", retryAfter);

            Duration retrieved = ex.getRetryAfter();
            assertThat(retrieved).isEqualTo(retryAfter);
            // Duration is immutable, so plusSeconds returns a new instance
            assertThat(retrieved.plusSeconds(10)).isNotEqualTo(retryAfter);
        }

        @Test
        @DisplayName("should return consistent retry after on multiple calls")
        void shouldReturnConsistentRetryAfterOnMultipleCalls() {
            Duration retryAfter = Duration.ofMinutes(2);
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client", retryAfter);

            assertThat(ex.getRetryAfter()).isEqualTo(ex.getRetryAfter());
        }

        @Test
        @DisplayName("should return Duration.ZERO for zero duration")
        void shouldReturnDurationZero() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException(
                "client", Duration.ZERO);

            assertThat(ex.getRetryAfter()).isEqualTo(Duration.ZERO);
            assertThat(ex.getRetryAfter().isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("Message Format Tests")
    class MessageFormatTests {

        @Test
        @DisplayName("should format message without retry duration")
        void shouldFormatMessageWithoutRetryDuration() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("test-client");

            assertThat(ex.getRawMessage()).isEqualTo("Rate limit exceeded for client: test-client");
        }

        @Test
        @DisplayName("should format message with retry duration")
        void shouldFormatMessageWithRetryDuration() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException(
                "test-client", Duration.ofSeconds(120));

            assertThat(ex.getRawMessage())
                .isEqualTo("Rate limit exceeded for client: test-client. Retry after: 120 seconds");
        }

        @Test
        @DisplayName("single-arg message should not contain 'Retry after'")
        void singleArgMessageShouldNotContainRetryAfter() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client-abc");

            assertThat(ex.getMessage()).doesNotContain("Retry after");
        }

        @Test
        @DisplayName("two-arg message should contain 'Retry after'")
        void twoArgMessageShouldContainRetryAfter() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException(
                "client-abc", Duration.ofSeconds(5));

            assertThat(ex.getMessage()).contains("Retry after");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenException")
        void shouldExtendOpenException() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("should extend CaptchaException")
        void shouldExtendCaptchaException() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client");

            assertThat(ex).isInstanceOf(CaptchaException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should extend Exception")
        void shouldExtendException() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client");

            assertThat(ex).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should extend Throwable")
        void shouldExtendThrowable() {
            CaptchaRateLimitException ex = new CaptchaRateLimitException("client");

            assertThat(ex).isInstanceOf(Throwable.class);
        }

        @Test
        @DisplayName("should be catchable as CaptchaException")
        void shouldBeCatchableAsCaptchaException() {
            assertThatCode(() -> {
                try {
                    throw new CaptchaRateLimitException("test-client", Duration.ofSeconds(60));
                } catch (CaptchaException e) {
                    assertThat(e).isInstanceOf(CaptchaRateLimitException.class);
                    CaptchaRateLimitException rateLimitEx = (CaptchaRateLimitException) e;
                    assertThat(rateLimitEx.getRetryAfter()).isEqualTo(Duration.ofSeconds(60));
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should be throwable with assertThatThrownBy")
        void shouldBeThrowableWithAssertThatThrownBy() {
            assertThatThrownBy(() -> {
                throw new CaptchaRateLimitException("rate-limited-client");
            }).isInstanceOf(CaptchaRateLimitException.class)
              .isInstanceOf(CaptchaException.class)
              .hasMessageContaining("Rate limit exceeded")
              .hasMessageContaining("rate-limited-client");
        }

        @Test
        @DisplayName("should preserve all fields when caught as CaptchaException")
        void shouldPreserveAllFieldsWhenCaughtAsCaptchaException() {
            Duration retryAfter = Duration.ofSeconds(90);

            assertThatCode(() -> {
                try {
                    throw new CaptchaRateLimitException("preserved-client", retryAfter);
                } catch (CaptchaException e) {
                    CaptchaRateLimitException rateLimitEx = (CaptchaRateLimitException) e;
                    assertThat(rateLimitEx.getClientId()).isEqualTo("preserved-client");
                    assertThat(rateLimitEx.getRetryAfter()).isEqualTo(retryAfter);
                }
            }).doesNotThrowAnyException();
        }
    }
}
