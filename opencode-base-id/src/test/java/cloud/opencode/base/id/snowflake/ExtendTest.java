package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ExtendTest Tests
 * ExtendTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("Extend 时钟回拨扩展策略测试")
class ExtendTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("有效位数创建成功")
        void testValidBits() {
            Extend extend = new Extend(2);
            assertThat(extend.extensionBits()).isEqualTo(2);
            assertThat(extend.maxExtension()).isEqualTo(3);
            assertThat(extend.extensionValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("1位扩展最大值为1")
        void testOneBit() {
            Extend extend = new Extend(1);
            assertThat(extend.maxExtension()).isEqualTo(1);
        }

        @Test
        @DisplayName("10位扩展最大值为1023")
        void testMaxBits() {
            Extend extend = new Extend(10);
            assertThat(extend.maxExtension()).isEqualTo(1023);
        }

        @Test
        @DisplayName("0位抛出异常")
        void testZeroBits() {
            assertThatThrownBy(() -> new Extend(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负数位抛出异常")
        void testNegativeBits() {
            assertThatThrownBy(() -> new Extend(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("超过10位抛出异常")
        void testExceedMaxBits() {
            assertThatThrownBy(() -> new Extend(11))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("handle方法测试")
    class HandleTests {

        @Test
        @DisplayName("handle返回lastTimestamp")
        void testHandleReturnsLastTimestamp() {
            Extend extend = new Extend(3);
            long result = extend.handle(1000L, 900L);
            assertThat(result).isEqualTo(1000L);
        }

        @Test
        @DisplayName("handle递增扩展值")
        void testHandleIncrementsExtension() {
            Extend extend = new Extend(3);
            extend.handle(1000L, 900L);
            assertThat(extend.extensionValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("扩展值耗尽时抛出异常")
        void testExtensionExhausted() {
            Extend extend = new Extend(1); // max = 1
            extend.handle(1000L, 900L); // extensionValue = 1
            assertThatThrownBy(() -> extend.handle(1000L, 900L))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("reset重置扩展值")
        void testReset() {
            Extend extend = new Extend(3);
            extend.handle(1000L, 900L);
            extend.handle(1000L, 900L);
            assertThat(extend.extensionValue()).isEqualTo(2);
            extend.reset();
            assertThat(extend.extensionValue()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            Extend extend = new Extend(2);
            String str = extend.toString();
            assertThat(str).contains("bits=2").contains("max=3");
        }
    }

    @Nested
    @DisplayName("ClockBackwardStrategy接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现ClockBackwardStrategy接口")
        void testImplementsInterface() {
            Extend extend = new Extend(2);
            assertThat(extend).isInstanceOf(ClockBackwardStrategy.class);
        }
    }
}
