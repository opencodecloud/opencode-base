package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;

import static org.assertj.core.api.Assertions.*;

/**
 * YearQuarter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("YearQuarter 测试")
class YearQuarterTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(int, int) 从年份和季度值创建")
        void testOfIntInt() {
            YearQuarter yq = YearQuarter.of(2024, 1);
            assertThat(yq.getYear()).isEqualTo(2024);
            assertThat(yq.getQuarterValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("of(int, Quarter) 从年份和Quarter枚举创建")
        void testOfIntQuarter() {
            YearQuarter yq = YearQuarter.of(2024, Quarter.Q2);
            assertThat(yq.getYear()).isEqualTo(2024);
            assertThat(yq.getQuarter()).isEqualTo(Quarter.Q2);
        }

        @Test
        @DisplayName("of() null quarter抛出异常")
        void testOfNullQuarter() {
            assertThatThrownBy(() -> YearQuarter.of(2024, (Quarter) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("from() 从LocalDate创建")
        void testFrom() {
            LocalDate date = LocalDate.of(2024, 5, 15);
            YearQuarter yq = YearQuarter.from(date);
            assertThat(yq.getYear()).isEqualTo(2024);
            assertThat(yq.getQuarterValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("from() 从YearQuarter返回自身")
        void testFromYearQuarter() {
            YearQuarter original = YearQuarter.of(2024, 3);
            assertThat(YearQuarter.from(original)).isSameAs(original);
        }

        @Test
        @DisplayName("now() 获取当前YearQuarter")
        void testNow() {
            YearQuarter yq = YearQuarter.now();
            assertThat(yq).isNotNull();
            assertThat(yq.getYear()).isGreaterThanOrEqualTo(2024);
        }

        @Test
        @DisplayName("parse() 解析字符串 2024Q1 格式")
        void testParse() {
            YearQuarter yq = YearQuarter.parse("2024Q1");
            assertThat(yq.getYear()).isEqualTo(2024);
            assertThat(yq.getQuarterValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("parse() 解析字符串 2024Q2")
        void testParseWithoutDash() {
            YearQuarter yq = YearQuarter.parse("2024Q2");
            assertThat(yq.getYear()).isEqualTo(2024);
            assertThat(yq.getQuarterValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("parse() 小写字符串")
        void testParseLowerCase() {
            YearQuarter yq = YearQuarter.parse("2024q3");
            assertThat(yq.getQuarterValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("parse() 无效字符串抛出异常")
        void testParseInvalid() {
            assertThatThrownBy(() -> YearQuarter.parse("invalid"))
                    .isInstanceOf(OpenDateException.class);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("isLeapYear() 检查闰年")
        void testIsLeapYear() {
            assertThat(YearQuarter.of(2024, 1).isLeapYear()).isTrue();
            assertThat(YearQuarter.of(2023, 1).isLeapYear()).isFalse();
        }

        @Test
        @DisplayName("lengthOfQuarter() 获取季度天数")
        void testLengthOfQuarter() {
            assertThat(YearQuarter.of(2024, 1).lengthOfQuarter()).isEqualTo(91); // leap year
            assertThat(YearQuarter.of(2023, 1).lengthOfQuarter()).isEqualTo(90); // non-leap
            assertThat(YearQuarter.of(2024, 2).lengthOfQuarter()).isEqualTo(91);
        }

        @Test
        @DisplayName("isFirstQuarter() 检查是否为第一季度")
        void testIsFirstQuarter() {
            assertThat(YearQuarter.of(2024, 1).isFirstQuarter()).isTrue();
            assertThat(YearQuarter.of(2024, 2).isFirstQuarter()).isFalse();
        }

        @Test
        @DisplayName("isLastQuarter() 检查是否为最后季度")
        void testIsLastQuarter() {
            assertThat(YearQuarter.of(2024, 4).isLastQuarter()).isTrue();
            assertThat(YearQuarter.of(2024, 3).isLastQuarter()).isFalse();
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("plusYears() 加年数")
        void testPlusYears() {
            YearQuarter yq = YearQuarter.of(2024, 2);
            assertThat(yq.plusYears(1)).isEqualTo(YearQuarter.of(2025, 2));
        }

        @Test
        @DisplayName("plusQuarters() 加季度数")
        void testPlusQuarters() {
            YearQuarter yq = YearQuarter.of(2024, 2);
            assertThat(yq.plusQuarters(1)).isEqualTo(YearQuarter.of(2024, 3));
            assertThat(yq.plusQuarters(3)).isEqualTo(YearQuarter.of(2025, 1));
        }

        @Test
        @DisplayName("plusQuarters() 加0返回自身")
        void testPlusQuartersZero() {
            YearQuarter yq = YearQuarter.of(2024, 2);
            assertThat(yq.plusQuarters(0)).isSameAs(yq);
        }

        @Test
        @DisplayName("minusYears() 减年数")
        void testMinusYears() {
            YearQuarter yq = YearQuarter.of(2024, 2);
            assertThat(yq.minusYears(1)).isEqualTo(YearQuarter.of(2023, 2));
        }

        @Test
        @DisplayName("minusQuarters() 减季度数")
        void testMinusQuarters() {
            YearQuarter yq = YearQuarter.of(2024, 2);
            assertThat(yq.minusQuarters(1)).isEqualTo(YearQuarter.of(2024, 1));
            assertThat(yq.minusQuarters(2)).isEqualTo(YearQuarter.of(2023, 4));
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("atStartOfQuarter() 获取季度开始日期")
        void testAtStartOfQuarter() {
            assertThat(YearQuarter.of(2024, 1).atStartOfQuarter())
                    .isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(YearQuarter.of(2024, 2).atStartOfQuarter())
                    .isEqualTo(LocalDate.of(2024, 4, 1));
        }

        @Test
        @DisplayName("atEndOfQuarter() 获取季度结束日期")
        void testAtEndOfQuarter() {
            assertThat(YearQuarter.of(2024, 1).atEndOfQuarter())
                    .isEqualTo(LocalDate.of(2024, 3, 31));
            assertThat(YearQuarter.of(2024, 4).atEndOfQuarter())
                    .isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        @DisplayName("atDay() 获取季度内特定天")
        void testAtDay() {
            assertThat(YearQuarter.of(2024, 1).atDay(1))
                    .isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(YearQuarter.of(2024, 1).atDay(32))
                    .isEqualTo(LocalDate.of(2024, 2, 1));
        }

        @Test
        @DisplayName("atDay() 无效天数抛出异常")
        void testAtDayInvalid() {
            assertThatThrownBy(() -> YearQuarter.of(2024, 1).atDay(0))
                    .isInstanceOf(OpenDateException.class);
            assertThatThrownBy(() -> YearQuarter.of(2024, 1).atDay(100))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("atMonth() 获取季度内特定月份")
        void testAtMonth() {
            assertThat(YearQuarter.of(2024, 2).atMonth(1))
                    .isEqualTo(YearMonth.of(2024, 4));
            assertThat(YearQuarter.of(2024, 2).atMonth(3))
                    .isEqualTo(YearMonth.of(2024, 6));
        }

        @Test
        @DisplayName("atMonth() 无效月份抛出异常")
        void testAtMonthInvalid() {
            assertThatThrownBy(() -> YearQuarter.of(2024, 1).atMonth(0))
                    .isInstanceOf(OpenDateException.class);
            assertThatThrownBy(() -> YearQuarter.of(2024, 1).atMonth(4))
                    .isInstanceOf(OpenDateException.class);
        }
    }

    @Nested
    @DisplayName("比较方法测试")
    class ComparisonTests {

        @Test
        @DisplayName("isBefore() 在之前")
        void testIsBefore() {
            YearQuarter yq1 = YearQuarter.of(2024, 1);
            YearQuarter yq2 = YearQuarter.of(2024, 2);
            assertThat(yq1.isBefore(yq2)).isTrue();
            assertThat(yq2.isBefore(yq1)).isFalse();
        }

        @Test
        @DisplayName("isAfter() 在之后")
        void testIsAfter() {
            YearQuarter yq1 = YearQuarter.of(2024, 1);
            YearQuarter yq2 = YearQuarter.of(2024, 2);
            assertThat(yq2.isAfter(yq1)).isTrue();
            assertThat(yq1.isAfter(yq2)).isFalse();
        }

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            YearQuarter yq1 = YearQuarter.of(2024, 1);
            YearQuarter yq2 = YearQuarter.of(2024, 2);
            YearQuarter yq3 = YearQuarter.of(2025, 1);

            assertThat(yq1.compareTo(yq2)).isLessThan(0);
            assertThat(yq2.compareTo(yq1)).isGreaterThan(0);
            assertThat(yq1.compareTo(yq3)).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("格式化方法测试")
    class FormattingTests {

        @Test
        @DisplayName("format() 默认格式化")
        void testFormat() {
            assertThat(YearQuarter.of(2024, 1).format()).isEqualTo("2024-Q1");
            assertThat(YearQuarter.of(2024, 4).format()).isEqualTo("2024-Q4");
        }

        @Test
        @DisplayName("toString() 返回format()")
        void testToString() {
            YearQuarter yq = YearQuarter.of(2024, 2);
            assertThat(yq.toString()).isEqualTo(yq.format());
        }
    }

    @Nested
    @DisplayName("Temporal实现测试")
    class TemporalTests {

        @Test
        @DisplayName("isSupported(TemporalField) 支持的字段")
        void testIsSupportedField() {
            YearQuarter yq = YearQuarter.of(2024, 1);
            // Only test fields that are explicitly supported to avoid infinite recursion
            // in ChronoField.isSupportedBy() which calls back to isSupported()
            assertThat(yq.isSupported(IsoFields.QUARTER_OF_YEAR)).isTrue();
            assertThat(yq.isSupported((java.time.temporal.TemporalField) null)).isFalse();
        }

        @Test
        @DisplayName("isSupported(TemporalUnit) 支持的单位")
        void testIsSupportedUnit() {
            YearQuarter yq = YearQuarter.of(2024, 1);
            // Only test explicitly supported units to avoid infinite recursion
            assertThat(yq.isSupported(IsoFields.QUARTER_YEARS)).isTrue();
            assertThat(yq.isSupported((java.time.temporal.TemporalUnit) null)).isFalse();
        }

        @Test
        @DisplayName("getLong() 获取字段值")
        void testGetLong() {
            YearQuarter yq = YearQuarter.of(2024, 3);
            assertThat(yq.getLong(ChronoField.YEAR)).isEqualTo(2024);
            assertThat(yq.getLong(IsoFields.QUARTER_OF_YEAR)).isEqualTo(3);
        }

        @Test
        @DisplayName("with() 设置字段值")
        void testWith() {
            YearQuarter yq = YearQuarter.of(2024, 1);
            assertThat(yq.with(ChronoField.YEAR, 2025))
                    .isEqualTo(YearQuarter.of(2025, 1));
            assertThat(yq.with(IsoFields.QUARTER_OF_YEAR, 3))
                    .isEqualTo(YearQuarter.of(2024, 3));
        }

        @Test
        @DisplayName("plus() 加时间量")
        void testPlus() {
            YearQuarter yq = YearQuarter.of(2024, 1);
            assertThat(yq.plus(1, ChronoUnit.YEARS))
                    .isEqualTo(YearQuarter.of(2025, 1));
            assertThat(yq.plus(2, IsoFields.QUARTER_YEARS))
                    .isEqualTo(YearQuarter.of(2024, 3));
        }

        @Test
        @DisplayName("until() 计算时间差")
        void testUntil() {
            YearQuarter yq1 = YearQuarter.of(2024, 1);
            YearQuarter yq2 = YearQuarter.of(2025, 3);
            assertThat(yq1.until(yq2, IsoFields.QUARTER_YEARS)).isEqualTo(6);
            assertThat(yq1.until(yq2, ChronoUnit.YEARS)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("TemporalAdjuster实现测试")
    class TemporalAdjusterTests {

        @Test
        @DisplayName("adjustInto() 调整日期")
        void testAdjustInto() {
            YearQuarter yq = YearQuarter.of(2024, 2);
            LocalDate date = LocalDate.of(2023, 1, 15);
            LocalDate adjusted = (LocalDate) yq.adjustInto(date);
            assertThat(adjusted.getYear()).isEqualTo(2024);
            assertThat(adjusted.get(IsoFields.QUARTER_OF_YEAR)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("equals/hashCode测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            YearQuarter yq1 = YearQuarter.of(2024, 2);
            YearQuarter yq2 = YearQuarter.of(2024, 2);
            YearQuarter yq3 = YearQuarter.of(2024, 3);

            assertThat(yq1).isEqualTo(yq2);
            assertThat(yq1).isNotEqualTo(yq3);
            assertThat(yq1).isEqualTo(yq1);
            assertThat(yq1).isNotEqualTo(null);
            assertThat(yq1).isNotEqualTo("2024-Q2");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            YearQuarter yq1 = YearQuarter.of(2024, 2);
            YearQuarter yq2 = YearQuarter.of(2024, 2);
            assertThat(yq1.hashCode()).isEqualTo(yq2.hashCode());
        }
    }
}
