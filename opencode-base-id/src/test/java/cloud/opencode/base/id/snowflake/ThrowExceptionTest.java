package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ThrowExceptionTest Tests
 * ThrowExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("ThrowException 抛异常策略测试")
class ThrowExceptionTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("getInstance返回非空实例")
        void testGetInstance() {
            assertThat(ThrowException.getInstance()).isNotNull();
        }

        @Test
        @DisplayName("getInstance返回同一实例")
        void testSingletonIdentity() {
            ThrowException t1 = ThrowException.getInstance();
            ThrowException t2 = ThrowException.getInstance();
            assertThat(t1).isSameAs(t2);
        }
    }

    @Nested
    @DisplayName("handle方法测试")
    class HandleTests {

        @Test
        @DisplayName("handle始终抛出异常")
        void testHandleThrows() {
            assertThatThrownBy(() -> ThrowException.getInstance().handle(1000L, 900L))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        @DisplayName("异常消息包含时间戳信息")
        void testExceptionMessage() {
            assertThatThrownBy(() -> ThrowException.getInstance().handle(1000L, 900L))
                    .isInstanceOf(OpenIdGenerationException.class)
                    .hasMessageContaining("1000")
                    .hasMessageContaining("900");
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString返回ThrowException{}")
        void testToString() {
            assertThat(ThrowException.getInstance().toString()).isEqualTo("ThrowException{}");
        }
    }

    @Nested
    @DisplayName("接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现ClockBackwardStrategy接口")
        void testImplementsInterface() {
            assertThat(ThrowException.getInstance()).isInstanceOf(ClockBackwardStrategy.class);
        }
    }
}
