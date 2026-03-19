package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ClockBackwardStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("ClockBackwardStrategy 测试")
class ClockBackwardStrategyTest {

    @Nested
    @DisplayName("ThrowException策略测试")
    class ThrowExceptionTests {

        @Test
        @DisplayName("获取单例实例")
        void testGetInstance() {
            ThrowException strategy = ThrowException.getInstance();

            assertThat(strategy).isNotNull();
            assertThat(strategy).isSameAs(ThrowException.getInstance());
        }

        @Test
        @DisplayName("处理时抛出异常")
        void testHandle() {
            ThrowException strategy = ThrowException.getInstance();

            assertThatThrownBy(() -> strategy.handle(1000L, 900L))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        @DisplayName("toString方法")
        void testToString() {
            ThrowException strategy = ThrowException.getInstance();

            assertThat(strategy.toString()).isEqualTo("ThrowException{}");
        }
    }

    @Nested
    @DisplayName("Wait策略测试")
    class WaitTests {

        @Test
        @DisplayName("使用秒数创建")
        void testOfSeconds() {
            Wait wait = Wait.ofSeconds(5);

            assertThat(wait).isNotNull();
            assertThat(wait.maxWaitMillis()).isEqualTo(5000);
        }

        @Test
        @DisplayName("使用毫秒创建")
        void testOfMillis() {
            Wait wait = Wait.ofMillis(100);

            assertThat(wait).isNotNull();
            assertThat(wait.maxWaitMillis()).isEqualTo(100);
        }

        @Test
        @DisplayName("构造无效参数抛出异常")
        void testInvalidConstructor() {
            assertThatThrownBy(() -> new Wait(0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> new Wait(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("超过最大等待时间抛出异常")
        void testHandleExceedsMax() {
            Wait wait = Wait.ofMillis(10);

            assertThatThrownBy(() -> wait.handle(1000L, 500L))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        @DisplayName("toString方法")
        void testToString() {
            Wait wait = Wait.ofMillis(100);

            assertThat(wait.toString()).contains("100");
        }
    }

    @Nested
    @DisplayName("Extend策略测试")
    class ExtendTests {

        @Test
        @DisplayName("创建扩展策略")
        void testConstructor() {
            Extend extend = new Extend(2);

            assertThat(extend).isNotNull();
            assertThat(extend.extensionBits()).isEqualTo(2);
            assertThat(extend.maxExtension()).isEqualTo(3); // 2^2 - 1
        }

        @Test
        @DisplayName("无效位数抛出异常")
        void testInvalidBits() {
            assertThatThrownBy(() -> new Extend(0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> new Extend(11))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("处理返回上次时间戳")
        void testHandle() {
            Extend extend = new Extend(2);

            long result = extend.handle(1000L, 900L);

            assertThat(result).isEqualTo(1000L);
            assertThat(extend.extensionValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("扩展值耗尽抛出异常")
        void testExtensionExhausted() {
            Extend extend = new Extend(1); // max = 1

            extend.handle(1000L, 900L);

            assertThatThrownBy(() -> extend.handle(1000L, 900L))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        @DisplayName("重置扩展值")
        void testReset() {
            Extend extend = new Extend(2);
            extend.handle(1000L, 900L);
            assertThat(extend.extensionValue()).isEqualTo(1);

            extend.reset();

            assertThat(extend.extensionValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("toString方法")
        void testToString() {
            Extend extend = new Extend(2);

            assertThat(extend.toString()).contains("bits=2");
        }
    }

    @Nested
    @DisplayName("Custom策略测试")
    class CustomTests {

        @Test
        @DisplayName("自定义策略实现")
        void testCustomImplementation() {
            ClockBackwardStrategy.Custom custom = (last, current) -> last;

            long result = custom.handle(1000L, 900L);

            assertThat(result).isEqualTo(1000L);
        }
    }
}
