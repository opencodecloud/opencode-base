package cloud.opencode.base.email;

import cloud.opencode.base.email.security.EmailRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EmailRateLimiter
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailRateLimiter")
class EmailRateLimiterTest {

    @Nested
    @DisplayName("Default rate limiter")
    class DefaultRateLimiter {

        private EmailRateLimiter limiter;

        @BeforeEach
        void setUp() {
            limiter = new EmailRateLimiter();
        }

        @Test
        @DisplayName("should have default limits")
        void shouldHaveDefaultLimits() {
            assertThat(limiter.getMaxPerMinute()).isEqualTo(10);
            assertThat(limiter.getMaxPerHour()).isEqualTo(100);
            assertThat(limiter.getMaxPerDay()).isEqualTo(1000);
        }

        @Test
        @DisplayName("should allow sending within limits")
        void shouldAllowSendingWithinLimits() {
            assertThat(limiter.allowSend()).isTrue();
            assertThat(limiter.allowSend("test@example.com")).isTrue();
        }
    }

    @Nested
    @DisplayName("Custom rate limiter")
    class CustomRateLimiter {

        @Test
        @DisplayName("should respect custom per-minute limit")
        void shouldRespectCustomPerMinuteLimit() {
            EmailRateLimiter limiter = new EmailRateLimiter(2, 100, 1000);

            assertThat(limiter.allowSend()).isTrue();
            assertThat(limiter.allowSend()).isTrue();
            assertThat(limiter.allowSend()).isFalse(); // Exceeded limit
        }

        @Test
        @DisplayName("should track recipients separately")
        void shouldTrackRecipientsSeparately() {
            EmailRateLimiter limiter = new EmailRateLimiter(2, 100, 1000);

            // Send to recipient 1
            assertThat(limiter.allowSend("user1@example.com")).isTrue();
            assertThat(limiter.allowSend("user1@example.com")).isTrue();
            assertThat(limiter.allowSend("user1@example.com")).isFalse();

            // Recipient 2 should still have quota
            assertThat(limiter.allowSend("user2@example.com")).isTrue();
        }
    }

    @Nested
    @DisplayName("Quota tracking")
    class QuotaTracking {

        @Test
        @DisplayName("should return correct quota for new recipient")
        void shouldReturnCorrectQuotaForNewRecipient() {
            EmailRateLimiter limiter = new EmailRateLimiter(10, 100, 1000);

            EmailRateLimiter.RateLimitQuota quota = limiter.getQuota("new@example.com");

            assertThat(quota.minuteRemaining()).isEqualTo(10);
            assertThat(quota.hourRemaining()).isEqualTo(100);
            assertThat(quota.dayRemaining()).isEqualTo(1000);
        }

        @Test
        @DisplayName("should decrement quota after sending")
        void shouldDecrementQuotaAfterSending() {
            EmailRateLimiter limiter = new EmailRateLimiter(10, 100, 1000);

            limiter.allowSend("test@example.com");
            limiter.allowSend("test@example.com");
            limiter.allowSend("test@example.com");

            EmailRateLimiter.RateLimitQuota quota = limiter.getQuota("test@example.com");

            assertThat(quota.minuteRemaining()).isEqualTo(7);
            assertThat(quota.hourRemaining()).isEqualTo(97);
            assertThat(quota.dayRemaining()).isEqualTo(997);
        }
    }

    @Nested
    @DisplayName("Reset operations")
    class ResetOperations {

        @Test
        @DisplayName("should reset single recipient")
        void shouldResetSingleRecipient() {
            EmailRateLimiter limiter = new EmailRateLimiter(2, 100, 1000);

            limiter.allowSend("user@example.com");
            limiter.allowSend("user@example.com");
            assertThat(limiter.allowSend("user@example.com")).isFalse();

            limiter.reset("user@example.com");

            assertThat(limiter.allowSend("user@example.com")).isTrue();
        }

        @Test
        @DisplayName("should reset all recipients")
        void shouldResetAllRecipients() {
            EmailRateLimiter limiter = new EmailRateLimiter(1, 100, 1000);

            limiter.allowSend("user1@example.com");
            limiter.allowSend("user2@example.com");

            assertThat(limiter.allowSend("user1@example.com")).isFalse();
            assertThat(limiter.allowSend("user2@example.com")).isFalse();

            limiter.resetAll();

            assertThat(limiter.allowSend("user1@example.com")).isTrue();
            assertThat(limiter.allowSend("user2@example.com")).isTrue();
        }
    }
}
