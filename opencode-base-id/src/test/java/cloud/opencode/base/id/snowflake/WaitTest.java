package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * WaitTest Tests
 * WaitTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("Wait 等待策略测试")
class WaitTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("有效毫秒数创建成功")
        void testValidMillis() {
            Wait wait = new Wait(100);
            assertThat(wait.maxWaitMillis()).isEqualTo(100);
        }

        @Test
        @DisplayName("0毫秒抛出异常")
        void testZeroMillis() {
            assertThatThrownBy(() -> new Wait(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负数毫秒抛出异常")
        void testNegativeMillis() {
            assertThatThrownBy(() -> new Wait(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("ofSeconds创建秒级等待策略")
        void testOfSeconds() {
            Wait wait = Wait.ofSeconds(5);
            assertThat(wait.maxWaitMillis()).isEqualTo(5000);
        }

        @Test
        @DisplayName("ofMillis创建毫秒级等待策略")
        void testOfMillis() {
            Wait wait = Wait.ofMillis(200);
            assertThat(wait.maxWaitMillis()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("handle方法测试")
    class HandleTests {

        @Test
        @DisplayName("回拨超过最大等待时间抛出异常")
        void testExceedMaxWaitThrows() {
            Wait wait = Wait.ofMillis(10);
            // diff = 1000 - 900 = 100 > 10
            assertThatThrownBy(() -> wait.handle(1000L, 900L))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        @DisplayName("回拨在等待范围内返回当前时间")
        void testWithinWaitRange() {
            Wait wait = Wait.ofMillis(100);
            long now = System.currentTimeMillis();
            // diff = now+1 - now = 1 < 100
            long result = wait.handle(now + 1, now);
            assertThat(result).isGreaterThanOrEqualTo(now);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含maxWaitMillis")
        void testToString() {
            Wait wait = Wait.ofMillis(500);
            assertThat(wait.toString()).contains("maxWaitMillis=500");
        }
    }

    @Nested
    @DisplayName("接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现ClockBackwardStrategy接口")
        void testImplementsInterface() {
            Wait wait = Wait.ofMillis(100);
            assertThat(wait).isInstanceOf(ClockBackwardStrategy.class);
        }
    }
}
