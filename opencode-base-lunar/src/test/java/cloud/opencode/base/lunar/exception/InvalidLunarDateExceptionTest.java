package cloud.opencode.base.lunar.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * InvalidLunarDateException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("InvalidLunarDateException 测试")
class InvalidLunarDateExceptionTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("继承LunarException")
        void testExtendsLunarException() {
            InvalidLunarDateException exception = new InvalidLunarDateException("test");
            assertThat(exception).isInstanceOf(LunarException.class);
        }

        @Test
        @DisplayName("继承OpenException")
        void testExtendsOpenException() {
            InvalidLunarDateException exception = new InvalidLunarDateException("test");
            assertThat(exception).isInstanceOf(OpenException.class);
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息构造函数")
        void testMessageConstructor() {
            InvalidLunarDateException exception = new InvalidLunarDateException("Custom message");

            assertThat(exception.getMessage()).contains("Custom message");
            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.INVALID_LUNAR_DATE);
        }

        @Test
        @DisplayName("带日期详情的构造函数")
        void testDateDetailsConstructor() {
            InvalidLunarDateException exception = new InvalidLunarDateException(2024, 6, 31, true, "Day out of range");

            assertThat(exception.getYear()).isEqualTo(2024);
            assertThat(exception.getMonth()).isEqualTo(6);
            assertThat(exception.getDay()).isEqualTo(31);
            assertThat(exception.isLeap()).isTrue();
            assertThat(exception.getLunarErrorCode()).isEqualTo(LunarErrorCode.INVALID_LUNAR_DATE);
        }

        @Test
        @DisplayName("非闰月日期详情")
        void testNonLeapConstructor() {
            InvalidLunarDateException exception = new InvalidLunarDateException(2024, 13, 1, false, "Invalid month");

            assertThat(exception.getYear()).isEqualTo(2024);
            assertThat(exception.getMonth()).isEqualTo(13);
            assertThat(exception.getDay()).isEqualTo(1);
            assertThat(exception.isLeap()).isFalse();
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getYear返回正确值")
        void testGetYear() {
            InvalidLunarDateException exception = new InvalidLunarDateException(2024, 6, 15, false, "test");
            assertThat(exception.getYear()).isEqualTo(2024);
        }

        @Test
        @DisplayName("getMonth返回正确值")
        void testGetMonth() {
            InvalidLunarDateException exception = new InvalidLunarDateException(2024, 8, 15, false, "test");
            assertThat(exception.getMonth()).isEqualTo(8);
        }

        @Test
        @DisplayName("getDay返回正确值")
        void testGetDay() {
            InvalidLunarDateException exception = new InvalidLunarDateException(2024, 6, 30, false, "test");
            assertThat(exception.getDay()).isEqualTo(30);
        }

        @Test
        @DisplayName("isLeap返回正确值")
        void testIsLeap() {
            InvalidLunarDateException exceptionLeap = new InvalidLunarDateException(2024, 6, 15, true, "test");
            InvalidLunarDateException exceptionNonLeap = new InvalidLunarDateException(2024, 6, 15, false, "test");

            assertThat(exceptionLeap.isLeap()).isTrue();
            assertThat(exceptionNonLeap.isLeap()).isFalse();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("invalidLeapMonth创建异常")
        void testInvalidLeapMonth() {
            InvalidLunarDateException exception = InvalidLunarDateException.invalidLeapMonth(2024, 5, 0);

            assertThat(exception.getYear()).isEqualTo(2024);
            assertThat(exception.getMonth()).isEqualTo(5);
            assertThat(exception.isLeap()).isTrue();
            assertThat(exception.getMessage()).containsIgnoringCase("leap");
        }

        @Test
        @DisplayName("invalidLeapMonth有实际闰月时显示正确信息")
        void testInvalidLeapMonthWithActual() {
            InvalidLunarDateException exception = InvalidLunarDateException.invalidLeapMonth(2020, 5, 4);

            assertThat(exception.getMessage()).contains("4");
        }

        @Test
        @DisplayName("invalidDay创建异常")
        void testInvalidDay() {
            InvalidLunarDateException exception = InvalidLunarDateException.invalidDay(2024, 6, 31, false, 30);

            assertThat(exception.getYear()).isEqualTo(2024);
            assertThat(exception.getMonth()).isEqualTo(6);
            assertThat(exception.getDay()).isEqualTo(31);
            assertThat(exception.isLeap()).isFalse();
            assertThat(exception.getMessage()).contains("30");
        }
    }

    @Nested
    @DisplayName("消息格式测试")
    class MessageFormatTests {

        @Test
        @DisplayName("消息包含年月日信息")
        void testMessageContainsDateInfo() {
            InvalidLunarDateException exception = new InvalidLunarDateException(2024, 6, 31, true, "test");

            String message = exception.getMessage();
            assertThat(message).contains("2024");
        }

        @Test
        @DisplayName("闰月消息包含闰月标识")
        void testLeapMonthMessage() {
            InvalidLunarDateException exception = InvalidLunarDateException.invalidLeapMonth(2024, 6, 0);

            String message = exception.getMessage();
            assertThat(message).containsIgnoringCase("leap");
        }
    }
}
