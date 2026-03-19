package cloud.opencode.base.captcha.validator;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaRateLimiter Test - Unit tests for rate limiting
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaRateLimiterTest {

    @Nested
    @DisplayName("Default Constructor Tests")
    class DefaultConstructorTests {

        @Test
        @DisplayName("should allow requests within default limit")
        void shouldAllowRequestsWithinDefaultLimit() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter();

            for (int i = 0; i < 10; i++) {
                assertThat(limiter.isAllowed("client-1")).isTrue();
            }
        }

        @Test
        @DisplayName("should block after default limit exceeded")
        void shouldBlockAfterDefaultLimitExceeded() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter();

            // Use up all 10 default requests
            for (int i = 0; i < 11; i++) {
                limiter.isAllowed("client-1");
            }

            assertThat(limiter.isAllowed("client-1")).isFalse();
        }
    }

    @Nested
    @DisplayName("Custom Limit Tests")
    class CustomLimitTests {

        @Test
        @DisplayName("should respect custom max requests")
        void shouldRespectCustomMaxRequests() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(3, Duration.ofMinutes(1));

            assertThat(limiter.isAllowed("client-1")).isTrue();
            assertThat(limiter.isAllowed("client-1")).isTrue();
            assertThat(limiter.isAllowed("client-1")).isTrue();
            assertThat(limiter.isAllowed("client-1")).isFalse();
        }

        @Test
        @DisplayName("should allow single request with limit of 1")
        void shouldAllowSingleRequestWithLimitOfOne() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(1, Duration.ofMinutes(1));

            assertThat(limiter.isAllowed("client-1")).isTrue();
            assertThat(limiter.isAllowed("client-1")).isFalse();
        }
    }

    @Nested
    @DisplayName("Per-Client Tracking Tests")
    class PerClientTrackingTests {

        @Test
        @DisplayName("should track clients independently")
        void shouldTrackClientsIndependently() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(2, Duration.ofMinutes(1));

            assertThat(limiter.isAllowed("client-A")).isTrue();
            assertThat(limiter.isAllowed("client-A")).isTrue();
            assertThat(limiter.isAllowed("client-A")).isFalse();

            // Client B should still be allowed
            assertThat(limiter.isAllowed("client-B")).isTrue();
            assertThat(limiter.isAllowed("client-B")).isTrue();
        }

        @Test
        @DisplayName("should not cross-contaminate client limits")
        void shouldNotCrossContaminateClientLimits() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(1, Duration.ofMinutes(1));

            // Exhaust client-A
            limiter.isAllowed("client-A");
            limiter.isAllowed("client-A");

            // client-B should still be fine
            assertThat(limiter.isAllowed("client-B")).isTrue();
        }
    }

    @Nested
    @DisplayName("Remaining Requests Tests")
    class RemainingRequestsTests {

        @Test
        @DisplayName("should return max requests for unknown client")
        void shouldReturnMaxRequestsForUnknownClient() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(5, Duration.ofMinutes(1));

            assertThat(limiter.getRemainingRequests("unknown")).isEqualTo(5);
        }

        @Test
        @DisplayName("should decrease remaining after each request")
        void shouldDecreaseRemainingAfterEachRequest() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(5, Duration.ofMinutes(1));

            limiter.isAllowed("client-1");  // count=1
            assertThat(limiter.getRemainingRequests("client-1")).isEqualTo(4);

            limiter.isAllowed("client-1");  // count=2
            assertThat(limiter.getRemainingRequests("client-1")).isEqualTo(3);
        }

        @Test
        @DisplayName("should not return negative remaining")
        void shouldNotReturnNegativeRemaining() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(1, Duration.ofMinutes(1));

            limiter.isAllowed("client-1");
            limiter.isAllowed("client-1");
            limiter.isAllowed("client-1");

            assertThat(limiter.getRemainingRequests("client-1")).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Time Until Reset Tests")
    class TimeUntilResetTests {

        @Test
        @DisplayName("should return zero for unknown client")
        void shouldReturnZeroForUnknownClient() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter();

            assertThat(limiter.getTimeUntilReset("unknown")).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("should return positive duration for active client")
        void shouldReturnPositiveDurationForActiveClient() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(10, Duration.ofMinutes(5));
            limiter.isAllowed("client-1");

            Duration timeUntilReset = limiter.getTimeUntilReset("client-1");

            assertThat(timeUntilReset).isPositive();
            assertThat(timeUntilReset).isLessThanOrEqualTo(Duration.ofMinutes(5));
        }
    }

    @Nested
    @DisplayName("Sliding Window Tests")
    class SlidingWindowTests {

        @Test
        @DisplayName("should reset after window expires")
        void shouldResetAfterWindowExpires() throws InterruptedException {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(2, Duration.ofMillis(200));

            limiter.isAllowed("client-1");
            limiter.isAllowed("client-1");
            assertThat(limiter.isAllowed("client-1")).isFalse();

            // Wait for window to expire
            Thread.sleep(300);

            // Should be allowed again
            assertThat(limiter.isAllowed("client-1")).isTrue();
        }

        @Test
        @DisplayName("should return max remaining after window expires")
        void shouldReturnMaxRemainingAfterWindowExpires() throws InterruptedException {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(5, Duration.ofMillis(200));

            limiter.isAllowed("client-1");
            limiter.isAllowed("client-1");

            Thread.sleep(300);

            assertThat(limiter.getRemainingRequests("client-1")).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Clear Tests")
    class ClearTests {

        @Test
        @DisplayName("should clear specific client limit")
        void shouldClearSpecificClientLimit() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(1, Duration.ofMinutes(1));

            limiter.isAllowed("client-1");
            limiter.isAllowed("client-1");
            assertThat(limiter.isAllowed("client-1")).isFalse();

            limiter.clear("client-1");

            assertThat(limiter.isAllowed("client-1")).isTrue();
        }

        @Test
        @DisplayName("should not affect other clients when clearing one")
        void shouldNotAffectOtherClientsWhenClearingOne() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(2, Duration.ofMinutes(1));

            limiter.isAllowed("client-A");
            limiter.isAllowed("client-B");
            limiter.isAllowed("client-B");

            limiter.clear("client-A");

            // client-B should still have its count
            assertThat(limiter.getRemainingRequests("client-B")).isEqualTo(0);
        }

        @Test
        @DisplayName("clearing nonexistent client should not throw")
        void clearingNonexistentClientShouldNotThrow() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter();

            assertThatCode(() -> limiter.clear("nonexistent"))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Clear Expired Tests")
    class ClearExpiredTests {

        @Test
        @DisplayName("should clear expired entries")
        void shouldClearExpiredEntries() throws InterruptedException {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(5, Duration.ofMillis(200));

            limiter.isAllowed("client-1");
            limiter.isAllowed("client-2");

            Thread.sleep(300);

            limiter.clearExpired();

            // After clearing expired, both clients should have full allowance
            assertThat(limiter.getRemainingRequests("client-1")).isEqualTo(5);
            assertThat(limiter.getRemainingRequests("client-2")).isEqualTo(5);
        }

        @Test
        @DisplayName("should not clear non-expired entries")
        void shouldNotClearNonExpiredEntries() {
            CaptchaRateLimiter limiter = new CaptchaRateLimiter(5, Duration.ofMinutes(10));

            limiter.isAllowed("client-1");
            limiter.isAllowed("client-1");

            limiter.clearExpired();

            assertThat(limiter.getRemainingRequests("client-1")).isEqualTo(3);
        }
    }
}
