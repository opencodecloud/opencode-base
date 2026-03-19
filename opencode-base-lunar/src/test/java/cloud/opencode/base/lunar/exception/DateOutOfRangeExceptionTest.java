package cloud.opencode.base.lunar.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DateOutOfRangeException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("DateOutOfRangeException 测试")
class DateOutOfRangeExceptionTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("继承LunarException")
        void testExtendsLunarException() {
            DateOutOfRangeException exception = new DateOutOfRangeException(1800);
            assertThat(exception).isInstanceOf(LunarException.class);
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("MIN_YEAR为1900")
        void testMinYear() {
            assertThat(DateOutOfRangeException.MIN_YEAR).isEqualTo(1900);
        }

        @Test
        @DisplayName("MAX_YEAR为2100")
        void testMaxYear() {
            assertThat(DateOutOfRangeException.MAX_YEAR).isEqualTo(2100);
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("年份构造函数")
        void testYearConstructor() {
            DateOutOfRangeException exception = new DateOutOfRangeException(1800);

            assertThat(exception.getYear()).isEqualTo(1800);
            assertThat(exception.getMessage()).contains("1800");
            assertThat(exception.getErrorCode()).isEqualTo(LunarErrorCode.YEAR_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("消息构造函数")
        void testMessageConstructor() {
            DateOutOfRangeException exception = new DateOutOfRangeException("Custom message");

            assertThat(exception.getMessage()).contains("Custom message");
            assertThat(exception.getErrorCode()).isEqualTo(LunarErrorCode.DATE_OUT_OF_RANGE);
        }
    }

    @Nested
    @DisplayName("getYear方法测试")
    class GetYearTests {

        @Test
        @DisplayName("返回正确的年份")
        void testGetYear() {
            DateOutOfRangeException exception = new DateOutOfRangeException(1850);
            assertThat(exception.getYear()).isEqualTo(1850);
        }

        @Test
        @DisplayName("低于最小年份")
        void testBelowMinYear() {
            DateOutOfRangeException exception = new DateOutOfRangeException(1899);
            assertThat(exception.getYear()).isEqualTo(1899);
            assertThat(exception.getYear()).isLessThan(DateOutOfRangeException.MIN_YEAR);
        }

        @Test
        @DisplayName("高于最大年份")
        void testAboveMaxYear() {
            DateOutOfRangeException exception = new DateOutOfRangeException(2101);
            assertThat(exception.getYear()).isEqualTo(2101);
            assertThat(exception.getYear()).isGreaterThan(DateOutOfRangeException.MAX_YEAR);
        }
    }

    @Nested
    @DisplayName("getMinYear和getMaxYear方法测试")
    class MinMaxYearTests {

        @Test
        @DisplayName("getMinYear返回正确值")
        void testGetMinYear() {
            DateOutOfRangeException exception = new DateOutOfRangeException(1800);
            assertThat(exception.getMinYear()).isEqualTo(1900);
        }

        @Test
        @DisplayName("getMaxYear返回正确值")
        void testGetMaxYear() {
            DateOutOfRangeException exception = new DateOutOfRangeException(1800);
            assertThat(exception.getMaxYear()).isEqualTo(2100);
        }
    }
}
