package cloud.opencode.base.event.security;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EventRateLimiter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventRateLimiter 测试")
class EventRateLimiterTest {

    static class TestEvent extends Event {}
    static class OtherEvent extends Event {}

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("设置默认最大值")
        void testConstructorWithDefault() {
            EventRateLimiter limiter = new EventRateLimiter(100);

            assertThat(limiter.getDefaultMaxPerSecond()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("setLimit() 测试")
    class SetLimitTests {

        @Test
        @DisplayName("为特定事件类型设置限制")
        void testSetLimitForEventType() {
            EventRateLimiter limiter = new EventRateLimiter(10);

            limiter.setLimit(TestEvent.class, 100);

            // 验证可以发布超过默认限制的事件
            for (int i = 0; i < 50; i++) {
                assertThat(limiter.allowPublish(new TestEvent())).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("allowPublish() 测试")
    class AllowPublishTests {

        @Test
        @DisplayName("在限制内允许发布")
        void testAllowWithinLimit() {
            EventRateLimiter limiter = new EventRateLimiter(10);

            for (int i = 0; i < 10; i++) {
                assertThat(limiter.allowPublish(new TestEvent())).isTrue();
            }
        }

        @Test
        @DisplayName("超过限制拒绝发布")
        void testDenyOverLimit() {
            EventRateLimiter limiter = new EventRateLimiter(5);

            for (int i = 0; i < 5; i++) {
                limiter.allowPublish(new TestEvent());
            }

            // 第6个应该被拒绝
            assertThat(limiter.allowPublish(new TestEvent())).isFalse();
        }

        @Test
        @DisplayName("null事件返回false")
        void testNullEventReturnsFalse() {
            EventRateLimiter limiter = new EventRateLimiter(100);

            assertThat(limiter.allowPublish(null)).isFalse();
        }

        @Test
        @DisplayName("不同事件类型独立计数")
        void testIndependentCountPerType() {
            EventRateLimiter limiter = new EventRateLimiter(5);

            for (int i = 0; i < 5; i++) {
                limiter.allowPublish(new TestEvent());
            }

            // TestEvent已达限制，但OtherEvent应该还可以
            assertThat(limiter.allowPublish(new OtherEvent())).isTrue();
        }
    }

    @Nested
    @DisplayName("getCurrentCount() 测试")
    class GetCurrentCountTests {

        @Test
        @DisplayName("返回当前计数")
        void testReturnsCurrentCount() {
            EventRateLimiter limiter = new EventRateLimiter(100);

            limiter.allowPublish(new TestEvent());
            limiter.allowPublish(new TestEvent());
            limiter.allowPublish(new TestEvent());

            assertThat(limiter.getCurrentCount(TestEvent.class)).isEqualTo(3);
        }

        @Test
        @DisplayName("未使用的类型返回0")
        void testUnusedTypeReturnsZero() {
            EventRateLimiter limiter = new EventRateLimiter(100);

            assertThat(limiter.getCurrentCount(TestEvent.class)).isZero();
        }
    }

    @Nested
    @DisplayName("reset() 测试")
    class ResetTests {

        @Test
        @DisplayName("重置所有限制")
        void testResetAll() {
            EventRateLimiter limiter = new EventRateLimiter(5);

            // 消耗限制
            for (int i = 0; i < 5; i++) {
                limiter.allowPublish(new TestEvent());
            }
            assertThat(limiter.allowPublish(new TestEvent())).isFalse();

            // 重置
            limiter.reset();

            // 应该可以再发布
            assertThat(limiter.allowPublish(new TestEvent())).isTrue();
        }

        @Test
        @DisplayName("重置特定类型")
        void testResetSpecificType() {
            EventRateLimiter limiter = new EventRateLimiter(5);

            // 消耗TestEvent限制
            for (int i = 0; i < 5; i++) {
                limiter.allowPublish(new TestEvent());
            }

            // 消耗OtherEvent部分限制
            for (int i = 0; i < 3; i++) {
                limiter.allowPublish(new OtherEvent());
            }

            // 只重置TestEvent
            limiter.reset(TestEvent.class);

            // TestEvent应该可以发布
            assertThat(limiter.allowPublish(new TestEvent())).isTrue();

            // OtherEvent还有剩余
            assertThat(limiter.getCurrentCount(OtherEvent.class)).isEqualTo(3);
        }

        @Test
        @DisplayName("重置不存在的类型不抛异常")
        void testResetNonExistentType() {
            EventRateLimiter limiter = new EventRateLimiter(100);

            assertThatNoException().isThrownBy(() -> limiter.reset(TestEvent.class));
        }
    }

    @Nested
    @DisplayName("getDefaultMaxPerSecond() 测试")
    class GetDefaultMaxPerSecondTests {

        @Test
        @DisplayName("返回设置的默认值")
        void testReturnsDefault() {
            EventRateLimiter limiter = new EventRateLimiter(500);

            assertThat(limiter.getDefaultMaxPerSecond()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("滑动窗口测试")
    class SlidingWindowTests {

        @Test
        @DisplayName("窗口过期后计数重置")
        void testWindowExpiry() throws InterruptedException {
            EventRateLimiter limiter = new EventRateLimiter(3);

            // 消耗限制
            for (int i = 0; i < 3; i++) {
                limiter.allowPublish(new TestEvent());
            }
            assertThat(limiter.allowPublish(new TestEvent())).isFalse();

            // 等待窗口过期（超过1秒）
            Thread.sleep(1100);

            // 应该可以再发布
            assertThat(limiter.allowPublish(new TestEvent())).isTrue();
        }
    }
}
