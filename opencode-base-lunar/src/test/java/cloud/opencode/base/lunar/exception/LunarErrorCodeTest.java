package cloud.opencode.base.lunar.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LunarErrorCode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("LunarErrorCode 测试")
class LunarErrorCodeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含所有错误码")
        void testAllErrorCodes() {
            assertThat(LunarErrorCode.values())
                .contains(
                    LunarErrorCode.UNKNOWN,
                    LunarErrorCode.CONVERSION_FAILED,
                    LunarErrorCode.SOLAR_TO_LUNAR_FAILED,
                    LunarErrorCode.LUNAR_TO_SOLAR_FAILED,
                    LunarErrorCode.YEAR_OUT_OF_RANGE,
                    LunarErrorCode.INVALID_LUNAR_DATE,
                    LunarErrorCode.INVALID_LEAP_MONTH,
                    LunarErrorCode.INVALID_DAY
                );
        }
    }

    @Nested
    @DisplayName("getCode方法测试")
    class GetCodeTests {

        @Test
        @DisplayName("UNKNOWN返回正确代码")
        void testUnknownCode() {
            assertThat(LunarErrorCode.UNKNOWN.getCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("CONVERSION_FAILED返回正确代码")
        void testConversionFailedCode() {
            assertThat(LunarErrorCode.CONVERSION_FAILED.getCode()).isEqualTo(1001);
        }

        @Test
        @DisplayName("SOLAR_TO_LUNAR_FAILED返回正确代码")
        void testSolarToLunarFailedCode() {
            assertThat(LunarErrorCode.SOLAR_TO_LUNAR_FAILED.getCode()).isEqualTo(1002);
        }

        @Test
        @DisplayName("LUNAR_TO_SOLAR_FAILED返回正确代码")
        void testLunarToSolarFailedCode() {
            assertThat(LunarErrorCode.LUNAR_TO_SOLAR_FAILED.getCode()).isEqualTo(1003);
        }

        @Test
        @DisplayName("YEAR_OUT_OF_RANGE返回正确代码")
        void testYearOutOfRangeCode() {
            assertThat(LunarErrorCode.YEAR_OUT_OF_RANGE.getCode()).isEqualTo(2001);
        }

        @Test
        @DisplayName("INVALID_LUNAR_DATE返回正确代码")
        void testInvalidLunarDateCode() {
            assertThat(LunarErrorCode.INVALID_LUNAR_DATE.getCode()).isEqualTo(3001);
        }

        @Test
        @DisplayName("INVALID_LEAP_MONTH返回正确代码")
        void testInvalidLeapMonthCode() {
            assertThat(LunarErrorCode.INVALID_LEAP_MONTH.getCode()).isEqualTo(3002);
        }

        @Test
        @DisplayName("INVALID_DAY返回正确代码")
        void testInvalidDayCode() {
            assertThat(LunarErrorCode.INVALID_DAY.getCode()).isEqualTo(3003);
        }
    }

    @Nested
    @DisplayName("getDescription方法测试")
    class GetDescriptionTests {

        @Test
        @DisplayName("UNKNOWN返回正确描述")
        void testUnknownDescription() {
            assertThat(LunarErrorCode.UNKNOWN.getDescription()).isNotEmpty();
        }

        @Test
        @DisplayName("CONVERSION_FAILED返回正确描述")
        void testConversionFailedDescription() {
            assertThat(LunarErrorCode.CONVERSION_FAILED.getDescription()).isNotEmpty();
        }

        @Test
        @DisplayName("YEAR_OUT_OF_RANGE返回正确描述")
        void testYearOutOfRangeDescription() {
            assertThat(LunarErrorCode.YEAR_OUT_OF_RANGE.getDescription()).isNotEmpty();
        }

        @Test
        @DisplayName("所有描述都不为空")
        void testAllDescriptionsNotEmpty() {
            for (LunarErrorCode code : LunarErrorCode.values()) {
                assertThat(code.getDescription()).isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("toStringCode方法测试")
    class ToStringCodeTests {

        @Test
        @DisplayName("UNKNOWN返回LUNAR_0")
        void testUnknownStringCode() {
            assertThat(LunarErrorCode.UNKNOWN.toStringCode()).isEqualTo("LUNAR_0");
        }

        @Test
        @DisplayName("CONVERSION_FAILED返回LUNAR_1001")
        void testConversionFailedStringCode() {
            assertThat(LunarErrorCode.CONVERSION_FAILED.toStringCode()).isEqualTo("LUNAR_1001");
        }

        @Test
        @DisplayName("YEAR_OUT_OF_RANGE返回LUNAR_2001")
        void testYearOutOfRangeStringCode() {
            assertThat(LunarErrorCode.YEAR_OUT_OF_RANGE.toStringCode()).isEqualTo("LUNAR_2001");
        }

        @Test
        @DisplayName("所有错误码都以LUNAR_开头")
        void testAllStringCodesPrefix() {
            for (LunarErrorCode code : LunarErrorCode.values()) {
                assertThat(code.toStringCode()).startsWith("LUNAR_");
            }
        }
    }

    @Nested
    @DisplayName("valueOf方法测试")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(LunarErrorCode.valueOf("UNKNOWN")).isEqualTo(LunarErrorCode.UNKNOWN);
            assertThat(LunarErrorCode.valueOf("CONVERSION_FAILED")).isEqualTo(LunarErrorCode.CONVERSION_FAILED);
            assertThat(LunarErrorCode.valueOf("YEAR_OUT_OF_RANGE")).isEqualTo(LunarErrorCode.YEAR_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("无效名称抛出异常")
        void testValueOfInvalid() {
            assertThatThrownBy(() -> LunarErrorCode.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString返回枚举名称")
        void testToString() {
            assertThat(LunarErrorCode.UNKNOWN.toString()).isEqualTo("UNKNOWN");
            assertThat(LunarErrorCode.CONVERSION_FAILED.toString()).isEqualTo("CONVERSION_FAILED");
        }
    }
}
