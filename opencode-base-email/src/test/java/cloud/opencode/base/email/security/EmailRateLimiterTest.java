package cloud.opencode.base.email.security;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailRateLimiterTest Tests
 * EmailRateLimiterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailRateLimiter Tests")
class EmailRateLimiterTest {

    private EmailRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new EmailRateLimiter(5, 50, 500);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should use default limits")
        void defaultConstructorShouldUseDefaultLimits() {
            EmailRateLimiter defaultLimiter = new EmailRateLimiter();

            assertThat(defaultLimiter.getMaxPerMinute()).isEqualTo(10);
            assertThat(defaultLimiter.getMaxPerHour()).isEqualTo(100);
            assertThat(defaultLimiter.getMaxPerDay()).isEqualTo(1000);
        }

        @Test
        @DisplayName("custom constructor should set custom limits")
        void customConstructorShouldSetCustomLimits() {
            EmailRateLimiter customLimiter = new EmailRateLimiter(20, 200, 2000);

            assertThat(customLimiter.getMaxPerMinute()).isEqualTo(20);
            assertThat(customLimiter.getMaxPerHour()).isEqualTo(200);
            assertThat(customLimiter.getMaxPerDay()).isEqualTo(2000);
        }
    }

    @Nested
    @DisplayName("allowSend Tests")
    class AllowSendTests {

        @Test
        @DisplayName("allowSend should return true within limit")
        void allowSendShouldReturnTrueWithinLimit() {
            boolean result = limiter.allowSend("test@example.com");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("allowSend should return false when minute limit exceeded")
        void allowSendShouldReturnFalseWhenMinuteLimitExceeded() {
            String recipient = "test@example.com";

            // Use up all minute quota
            for (int i = 0; i < 5; i++) {
                assertThat(limiter.allowSend(recipient)).isTrue();
            }

            // Next should be rejected
            assertThat(limiter.allowSend(recipient)).isFalse();
        }

        @Test
        @DisplayName("allowSend without recipient should use global limit")
        void allowSendWithoutRecipientShouldUseGlobalLimit() {
            boolean result = limiter.allowSend();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("allowSend should track different recipients separately")
        void allowSendShouldTrackDifferentRecipientsSeparately() {
            // Use up quota for one recipient
            for (int i = 0; i < 5; i++) {
                limiter.allowSend("recipient1@example.com");
            }

            // Different recipient should still be allowed
            assertThat(limiter.allowSend("recipient2@example.com")).isTrue();
        }
    }

    @Nested
    @DisplayName("getQuota Tests")
    class GetQuotaTests {

        @Test
        @DisplayName("getQuota should return full quota for new recipient")
        void getQuotaShouldReturnFullQuotaForNewRecipient() {
            EmailRateLimiter.RateLimitQuota quota = limiter.getQuota("new@example.com");

            assertThat(quota.minuteRemaining()).isEqualTo(5);
            assertThat(quota.hourRemaining()).isEqualTo(50);
            assertThat(quota.dayRemaining()).isEqualTo(500);
        }

        @Test
        @DisplayName("getQuota should return reduced quota after sends")
        void getQuotaShouldReturnReducedQuotaAfterSends() {
            String recipient = "test@example.com";
            limiter.allowSend(recipient);
            limiter.allowSend(recipient);

            EmailRateLimiter.RateLimitQuota quota = limiter.getQuota(recipient);

            assertThat(quota.minuteRemaining()).isEqualTo(3);
            assertThat(quota.hourRemaining()).isEqualTo(48);
            assertThat(quota.dayRemaining()).isEqualTo(498);
        }
    }

    @Nested
    @DisplayName("reset Tests")
    class ResetTests {

        @Test
        @DisplayName("reset should clear limit for specific recipient")
        void resetShouldClearLimitForSpecificRecipient() {
            String recipient = "test@example.com";

            // Use up quota
            for (int i = 0; i < 5; i++) {
                limiter.allowSend(recipient);
            }
            assertThat(limiter.allowSend(recipient)).isFalse();

            // Reset
            limiter.reset(recipient);

            // Should be allowed again
            assertThat(limiter.allowSend(recipient)).isTrue();
        }

        @Test
        @DisplayName("reset should not affect other recipients")
        void resetShouldNotAffectOtherRecipients() {
            String recipient1 = "r1@example.com";
            String recipient2 = "r2@example.com";

            // Use up quota for both
            for (int i = 0; i < 5; i++) {
                limiter.allowSend(recipient1);
                limiter.allowSend(recipient2);
            }

            // Reset only one
            limiter.reset(recipient1);

            // First should be allowed, second still blocked
            assertThat(limiter.allowSend(recipient1)).isTrue();
            assertThat(limiter.allowSend(recipient2)).isFalse();
        }
    }

    @Nested
    @DisplayName("resetAll Tests")
    class ResetAllTests {

        @Test
        @DisplayName("resetAll should clear all limits")
        void resetAllShouldClearAllLimits() {
            String recipient1 = "r1@example.com";
            String recipient2 = "r2@example.com";

            // Use up quota for both
            for (int i = 0; i < 5; i++) {
                limiter.allowSend(recipient1);
                limiter.allowSend(recipient2);
            }
            assertThat(limiter.allowSend(recipient1)).isFalse();
            assertThat(limiter.allowSend(recipient2)).isFalse();

            // Reset all
            limiter.resetAll();

            // Both should be allowed
            assertThat(limiter.allowSend(recipient1)).isTrue();
            assertThat(limiter.allowSend(recipient2)).isTrue();
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getMaxPerMinute should return configured value")
        void getMaxPerMinuteShouldReturnConfiguredValue() {
            assertThat(limiter.getMaxPerMinute()).isEqualTo(5);
        }

        @Test
        @DisplayName("getMaxPerHour should return configured value")
        void getMaxPerHourShouldReturnConfiguredValue() {
            assertThat(limiter.getMaxPerHour()).isEqualTo(50);
        }

        @Test
        @DisplayName("getMaxPerDay should return configured value")
        void getMaxPerDayShouldReturnConfiguredValue() {
            assertThat(limiter.getMaxPerDay()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("RateLimitQuota Record Tests")
    class RateLimitQuotaRecordTests {

        @Test
        @DisplayName("record components should be accessible")
        void recordComponentsShouldBeAccessible() {
            EmailRateLimiter.RateLimitQuota quota = new EmailRateLimiter.RateLimitQuota(10, 100, 1000);

            assertThat(quota.minuteRemaining()).isEqualTo(10);
            assertThat(quota.hourRemaining()).isEqualTo(100);
            assertThat(quota.dayRemaining()).isEqualTo(1000);
        }

        @Test
        @DisplayName("record should implement equals")
        void recordShouldImplementEquals() {
            EmailRateLimiter.RateLimitQuota quota1 = new EmailRateLimiter.RateLimitQuota(10, 100, 1000);
            EmailRateLimiter.RateLimitQuota quota2 = new EmailRateLimiter.RateLimitQuota(10, 100, 1000);

            assertThat(quota1).isEqualTo(quota2);
        }

        @Test
        @DisplayName("record should implement hashCode")
        void recordShouldImplementHashCode() {
            EmailRateLimiter.RateLimitQuota quota1 = new EmailRateLimiter.RateLimitQuota(10, 100, 1000);
            EmailRateLimiter.RateLimitQuota quota2 = new EmailRateLimiter.RateLimitQuota(10, 100, 1000);

            assertThat(quota1.hashCode()).isEqualTo(quota2.hashCode());
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("should handle concurrent access")
        void shouldHandleConcurrentAccess() throws InterruptedException {
            EmailRateLimiter concurrentLimiter = new EmailRateLimiter(100, 1000, 10000);
            String recipient = "concurrent@example.com";

            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 10; j++) {
                        concurrentLimiter.allowSend(recipient);
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }

            // Should have tracked all sends
            EmailRateLimiter.RateLimitQuota quota = concurrentLimiter.getQuota(recipient);
            // 100 sends total, minute remaining should be 0
            assertThat(quota.minuteRemaining()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero limits gracefully")
        void shouldHandleZeroLimitsGracefully() {
            EmailRateLimiter zeroLimiter = new EmailRateLimiter(0, 0, 0);

            assertThat(zeroLimiter.allowSend("test@example.com")).isFalse();
        }

        @Test
        @DisplayName("should handle empty recipient")
        void shouldHandleEmptyRecipient() {
            assertThat(limiter.allowSend("")).isTrue();
        }

        @Test
        @DisplayName("should handle special characters in recipient")
        void shouldHandleSpecialCharactersInRecipient() {
            assertThat(limiter.allowSend("test+special@example.com")).isTrue();
        }
    }
}
