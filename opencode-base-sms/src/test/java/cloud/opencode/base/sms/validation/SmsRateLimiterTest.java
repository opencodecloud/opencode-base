package cloud.opencode.base.sms.validation;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsRateLimiterTest Tests
 * SmsRateLimiterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsRateLimiter 测试")
class SmsRateLimiterTest {

    @Nested
    @DisplayName("tryAcquire方法测试")
    class TryAcquireTests {

        @Test
        @DisplayName("首次请求返回true")
        void testFirstRequest() {
            SmsRateLimiter limiter = new SmsRateLimiter(10, 100, 1000);

            boolean result = limiter.tryAcquire("13800138000");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("超过每分钟限制返回false")
        void testExceedMinuteLimit() {
            SmsRateLimiter limiter = new SmsRateLimiter(2, 100, 1000);

            limiter.tryAcquire("13800138000");
            limiter.tryAcquire("13800138000");
            boolean result = limiter.tryAcquire("13800138000");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("不同号码独立计数")
        void testDifferentPhones() {
            SmsRateLimiter limiter = new SmsRateLimiter(1, 100, 1000);

            limiter.tryAcquire("13800138001");
            boolean result = limiter.tryAcquire("13800138002");

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("acquire方法测试")
    class AcquireTests {

        @Test
        @DisplayName("首次请求不抛出异常")
        void testFirstAcquire() {
            SmsRateLimiter limiter = new SmsRateLimiter(10, 100, 1000);

            assertThatNoException().isThrownBy(() -> limiter.acquire("13800138000"));
        }
    }

    @Nested
    @DisplayName("getRetryAfter方法测试")
    class GetRetryAfterTests {

        @Test
        @DisplayName("未限制返回空")
        void testNotLimited() {
            SmsRateLimiter limiter = new SmsRateLimiter(10, 100, 1000);

            Duration retryAfter = limiter.getRetryAfter("13800138000");

            assertThat(retryAfter).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("被限制返回等待时间")
        void testLimited() {
            SmsRateLimiter limiter = new SmsRateLimiter(1, 100, 1000);

            limiter.tryAcquire("13800138000");
            limiter.tryAcquire("13800138000"); // 超过限制

            Duration retryAfter = limiter.getRetryAfter("13800138000");

            assertThat(retryAfter).isNotNull();
        }
    }

    @Nested
    @DisplayName("getCurrentCount方法测试")
    class GetCurrentCountTests {

        @Test
        @DisplayName("返回当前计数")
        void testGetCurrentCount() {
            SmsRateLimiter limiter = new SmsRateLimiter(10, 100, 1000);

            limiter.tryAcquire("13800138000");
            limiter.tryAcquire("13800138000");
            limiter.tryAcquire("13800138000");

            int count = limiter.getCurrentCount("13800138000");

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("新号码返回0")
        void testGetCurrentCountNew() {
            SmsRateLimiter limiter = new SmsRateLimiter(10, 100, 1000);

            int count = limiter.getCurrentCount("13800138000");

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("重置特定号码")
        void testReset() {
            SmsRateLimiter limiter = new SmsRateLimiter(1, 100, 1000);

            limiter.tryAcquire("13800138000");

            limiter.reset("13800138000");

            assertThat(limiter.getCurrentCount("13800138000")).isZero();
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除所有计数")
        void testClear() {
            SmsRateLimiter limiter = new SmsRateLimiter(10, 100, 1000);

            limiter.tryAcquire("13800138001");
            limiter.tryAcquire("13800138002");

            limiter.clear();

            assertThat(limiter.getCurrentCount("13800138001")).isZero();
            assertThat(limiter.getCurrentCount("13800138002")).isZero();
        }
    }

    @Nested
    @DisplayName("配置getter方法测试")
    class ConfigGettersTests {

        @Test
        @DisplayName("返回每分钟限制")
        void testGetLimitPerMinute() {
            SmsRateLimiter limiter = new SmsRateLimiter(5, 100, 1000);

            assertThat(limiter.getLimitPerMinute()).isEqualTo(5);
        }

        @Test
        @DisplayName("返回每小时限制")
        void testGetLimitPerHour() {
            SmsRateLimiter limiter = new SmsRateLimiter(5, 100, 1000);

            assertThat(limiter.getLimitPerHour()).isEqualTo(100);
        }

        @Test
        @DisplayName("返回每天限制")
        void testGetLimitPerDay() {
            SmsRateLimiter limiter = new SmsRateLimiter(5, 100, 1000);

            assertThat(limiter.getLimitPerDay()).isEqualTo(1000);
        }
    }
}
