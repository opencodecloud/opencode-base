package cloud.opencode.base.lunar.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DateConversionException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("DateConversionException 测试")
class DateConversionExceptionTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("继承LunarException")
        void testExtendsLunarException() {
            DateConversionException exception = new DateConversionException("test");
            assertThat(exception).isInstanceOf(LunarException.class);
        }

        @Test
        @DisplayName("继承OpenException")
        void testExtendsOpenException() {
            DateConversionException exception = new DateConversionException("test");
            assertThat(exception).isInstanceOf(OpenException.class);
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息构造函数")
        void testMessageConstructor() {
            DateConversionException exception = new DateConversionException("Conversion failed");

            assertThat(exception.getMessage()).contains("Conversion failed");
            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.CONVERSION_FAILED);
            assertThat(exception.getErrorCode()).isEqualTo("LUNAR_1001");
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("root cause");
            DateConversionException exception = new DateConversionException("Conversion failed", cause);

            assertThat(exception.getMessage()).contains("Conversion failed");
            assertThat(exception.getCause()).isSameAs(cause);
            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.CONVERSION_FAILED);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("solarToLunar创建异常")
        void testSolarToLunar() {
            DateConversionException exception = DateConversionException.solarToLunar(2024, 1, 15);

            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.CONVERSION_FAILED);
            assertThat(exception.getMessage()).contains("2024");
        }

        @Test
        @DisplayName("lunarToSolar创建异常")
        void testLunarToSolar() {
            DateConversionException exception = DateConversionException.lunarToSolar(2024, 6, 15, false);

            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.CONVERSION_FAILED);
            assertThat(exception.getMessage()).contains("2024");
        }

        @Test
        @DisplayName("lunarToSolar闰月创建异常")
        void testLunarToSolarLeapMonth() {
            DateConversionException exception = DateConversionException.lunarToSolar(2024, 6, 15, true);

            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.CONVERSION_FAILED);
            assertThat(exception.getMessage()).contains("闰");
        }
    }
}
