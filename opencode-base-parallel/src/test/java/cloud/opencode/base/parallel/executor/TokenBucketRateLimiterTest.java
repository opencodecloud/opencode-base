package cloud.opencode.base.parallel.executor;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TokenBucketRateLimiterTest Tests
 * TokenBucketRateLimiterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("TokenBucketRateLimiter 测试")
class TokenBucketRateLimiterTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("创建有效的限流器")
        void shouldCreateWithValidPermitsPerSecond() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(100.0);

            assertThat(limiter).isNotNull();
            assertThat(limiter.getPermitsPerSecond()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("使用小数速率创建限流器")
        void shouldCreateWithFractionalRate() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(0.5);

            assertThat(limiter.getPermitsPerSecond()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("速率为零时抛出IllegalArgumentException")
        void shouldRejectZeroRate() {
            assertThatThrownBy(() -> TokenBucketRateLimiter.of(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("速率为负数时抛出IllegalArgumentException")
        void shouldRejectNegativeRate() {
            assertThatThrownBy(() -> TokenBucketRateLimiter.of(-5.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("单个许可获取测试")
    class TryAcquireSingleTests {

        @Test
        @DisplayName("有许可时获取成功")
        void shouldAcquireWhenPermitsAvailable() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(10.0);

            assertThat(limiter.tryAcquire()).isTrue();
        }

        @Test
        @DisplayName("许可耗尽后获取失败")
        void shouldFailWhenPermitsExhausted() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(3.0);
            long max = limiter.getMaxPermits();

            // Exhaust all permits
            for (long i = 0; i < max; i++) {
                assertThat(limiter.tryAcquire()).isTrue();
            }

            // Next acquire should fail
            assertThat(limiter.tryAcquire()).isFalse();
        }

        @Test
        @DisplayName("速率小于1时桶容量至少为1")
        void shouldHaveAtLeastOnePermitForFractionalRate() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(0.1);

            assertThat(limiter.getMaxPermits()).isEqualTo(1);
            assertThat(limiter.tryAcquire()).isTrue();
            assertThat(limiter.tryAcquire()).isFalse();
        }
    }

    @Nested
    @DisplayName("多个许可获取测试")
    class TryAcquireMultipleTests {

        @Test
        @DisplayName("批量获取许可成功")
        void shouldAcquireMultiplePermits() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(10.0);

            assertThat(limiter.tryAcquire(5)).isTrue();
        }

        @Test
        @DisplayName("许可不足时批量获取失败")
        void shouldFailWhenInsufficientPermits() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(5.0);

            // Request more than max
            assertThat(limiter.tryAcquire(6)).isFalse();
        }

        @Test
        @DisplayName("获取全部许可后再次获取失败")
        void shouldFailAfterExhaustingWithBatch() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(10.0);
            long max = limiter.getMaxPermits();

            assertThat(limiter.tryAcquire((int) max)).isTrue();
            assertThat(limiter.tryAcquire(1)).isFalse();
        }

        @Test
        @DisplayName("许可数为零时抛出IllegalArgumentException")
        void shouldRejectZeroPermits() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(10.0);

            assertThatThrownBy(() -> limiter.tryAcquire(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("许可数为负数时抛出IllegalArgumentException")
        void shouldRejectNegativePermits() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(10.0);

            assertThatThrownBy(() -> limiter.tryAcquire(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("令牌补充测试")
    class RefillTests {

        @Test
        @DisplayName("等待后许可自动补充")
        void shouldRefillPermitsAfterSleeping() throws InterruptedException {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(100.0);
            long max = limiter.getMaxPermits();

            // Exhaust all permits
            for (long i = 0; i < max; i++) {
                limiter.tryAcquire();
            }
            assertThat(limiter.availablePermits()).isEqualTo(0);

            // Wait for refill (100 permits/sec -> ~10ms per permit)
            Thread.sleep(150);

            // Permits should have been refilled
            assertThat(limiter.availablePermits()).isGreaterThan(0);
            assertThat(limiter.tryAcquire()).isTrue();
        }

        @Test
        @DisplayName("补充不超过最大容量")
        void shouldNotExceedMaxPermitsOnRefill() throws InterruptedException {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(50.0);
            long max = limiter.getMaxPermits();

            // Wait well beyond what would produce more tokens than max
            Thread.sleep(200);

            assertThat(limiter.availablePermits()).isLessThanOrEqualTo(max);
        }
    }

    @Nested
    @DisplayName("配置查询测试")
    class ConfigurationTests {

        @Test
        @DisplayName("getPermitsPerSecond返回配置速率")
        void shouldReturnConfiguredRate() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(42.5);

            assertThat(limiter.getPermitsPerSecond()).isEqualTo(42.5);
        }

        @Test
        @DisplayName("getMaxPermits返回向下取整的速率")
        void shouldReturnFlooredMaxPermits() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(99.9);

            assertThat(limiter.getMaxPermits()).isEqualTo(99L);
        }

        @Test
        @DisplayName("getMaxPermits对整数速率返回精确值")
        void shouldReturnExactMaxPermitsForIntegerRate() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(50.0);

            assertThat(limiter.getMaxPermits()).isEqualTo(50L);
        }

        @Test
        @DisplayName("getMaxPermits对小于1的速率返回1")
        void shouldReturnOneForSubOneRate() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(0.3);

            assertThat(limiter.getMaxPermits()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("可用许可数测试")
    class AvailablePermitsTests {

        @Test
        @DisplayName("初始可用许可数等于最大容量")
        void shouldStartWithMaxPermits() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(20.0);

            assertThat(limiter.availablePermits()).isEqualTo(limiter.getMaxPermits());
        }

        @Test
        @DisplayName("获取后可用许可数减少")
        void shouldDecreaseAfterAcquire() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(20.0);
            long initial = limiter.availablePermits();

            limiter.tryAcquire();

            assertThat(limiter.availablePermits()).isEqualTo(initial - 1);
        }

        @Test
        @DisplayName("批量获取后可用许可数按数量减少")
        void shouldDecreaseByAmountAfterBatchAcquire() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(20.0);
            long initial = limiter.availablePermits();

            limiter.tryAcquire(5);

            assertThat(limiter.availablePermits()).isEqualTo(initial - 5);
        }

        @Test
        @DisplayName("获取失败不减少可用许可数")
        void shouldNotDecreaseOnFailedAcquire() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(3.0);
            long max = limiter.getMaxPermits();

            // Exhaust
            for (long i = 0; i < max; i++) {
                limiter.tryAcquire();
            }

            long before = limiter.availablePermits();
            limiter.tryAcquire(); // should fail
            long after = limiter.availablePermits();

            assertThat(after).isEqualTo(before);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含关键信息")
        void shouldContainKeyInfo() {
            TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(50.0);
            String str = limiter.toString();

            assertThat(str).contains("TokenBucketRateLimiter");
            assertThat(str).contains("permitsPerSecond=50.0");
            assertThat(str).contains("max=50");
        }
    }
}
