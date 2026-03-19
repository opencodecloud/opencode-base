package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;

import static org.assertj.core.api.Assertions.*;

/**
 * YearWeek 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("YearWeek 测试")
class YearWeekTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of() 从年份和周创建")
        void testOf() {
            YearWeek yw = YearWeek.of(2024, 1);
            assertThat(yw.getYear()).isEqualTo(2024);
            assertThat(yw.getWeek()).isEqualTo(1);
        }

        @Test
        @DisplayName("of() 53周年份")
        void testOf53Weeks() {
            YearWeek yw = YearWeek.of(2020, 53);
            assertThat(yw.getWeek()).isEqualTo(53);
        }

        @Test
        @DisplayName("of() 无效周数抛出异常")
        void testOfInvalid() {
            assertThatThrownBy(() -> YearWeek.of(2024, 0))
                    .isInstanceOf(OpenDateException.class);
            assertThatThrownBy(() -> YearWeek.of(2024, 53)) // 2024 has only 52 weeks
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("from() 从LocalDate创建")
        void testFrom() {
            LocalDate date = LocalDate.of(2024, 1, 8);
            YearWeek yw = YearWeek.from(date);
            assertThat(yw.getYear()).isEqualTo(2024);
            assertThat(yw.getWeek()).isEqualTo(2);
        }

        @Test
        @DisplayName("from() 从YearWeek返回自身")
        void testFromYearWeek() {
            YearWeek original = YearWeek.of(2024, 10);
            assertThat(YearWeek.from(original)).isSameAs(original);
        }

        @Test
        @DisplayName("now() 获取当前YearWeek")
        void testNow() {
            YearWeek yw = YearWeek.now();
            assertThat(yw).isNotNull();
        }

        @Test
        @DisplayName("parse() 解析字符串 2024W01 格式")
        void testParse() {
            YearWeek yw = YearWeek.parse("2024W01");
            assertThat(yw.getYear()).isEqualTo(2024);
            assertThat(yw.getWeek()).isEqualTo(1);
        }

        @Test
        @DisplayName("parse() 解析字符串 2024W10")
        void testParseWithoutDash() {
            YearWeek yw = YearWeek.parse("2024W10");
            assertThat(yw.getYear()).isEqualTo(2024);
            assertThat(yw.getWeek()).isEqualTo(10);
        }

        @Test
        @DisplayName("parse() 小写字符串")
        void testParseLowerCase() {
            YearWeek yw = YearWeek.parse("2024w05");
            assertThat(yw.getWeek()).isEqualTo(5);
        }

        @Test
        @DisplayName("parse() 无效字符串抛出异常")
        void testParseInvalid() {
            assertThatThrownBy(() -> YearWeek.parse("invalid"))
                    .isInstanceOf(OpenDateException.class);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("lengthOfYear() 获取年度周数")
        void testLengthOfYear() {
            assertThat(YearWeek.of(2024, 1).lengthOfYear()).isEqualTo(52);
            assertThat(YearWeek.of(2020, 1).lengthOfYear()).isEqualTo(53);
        }

        @Test
        @DisplayName("isFirstWeek() 检查是否为第一周")
        void testIsFirstWeek() {
            assertThat(YearWeek.of(2024, 1).isFirstWeek()).isTrue();
            assertThat(YearWeek.of(2024, 2).isFirstWeek()).isFalse();
        }

        @Test
        @DisplayName("isLastWeek() 检查是否为最后一周")
        void testIsLastWeek() {
            assertThat(YearWeek.of(2024, 52).isLastWeek()).isTrue();
            assertThat(YearWeek.of(2020, 53).isLastWeek()).isTrue();
            assertThat(YearWeek.of(2024, 51).isLastWeek()).isFalse();
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("plusYears() 加年数")
        void testPlusYears() {
            YearWeek yw = YearWeek.of(2024, 10);
            assertThat(yw.plusYears(1)).isEqualTo(YearWeek.of(2025, 10));
        }

        @Test
        @DisplayName("plusYears() 跨年时周数调整")
        void testPlusYearsAdjusted() {
            // 2020 has 53 weeks, 2021 has 52 weeks
            YearWeek yw = YearWeek.of(2020, 53);
            YearWeek result = yw.plusYears(1);
            assertThat(result.getYear()).isEqualTo(2021);
            assertThat(result.getWeek()).isEqualTo(52);
        }

        @Test
        @DisplayName("plusWeeks() 加周数")
        void testPlusWeeks() {
            YearWeek yw = YearWeek.of(2024, 50);
            assertThat(yw.plusWeeks(1)).isEqualTo(YearWeek.of(2024, 51));
            assertThat(yw.plusWeeks(3)).isEqualTo(YearWeek.of(2025, 1));
        }

        @Test
        @DisplayName("plusWeeks() 加0返回自身")
        void testPlusWeeksZero() {
            YearWeek yw = YearWeek.of(2024, 10);
            assertThat(yw.plusWeeks(0)).isSameAs(yw);
        }

        @Test
        @DisplayName("minusYears() 减年数")
        void testMinusYears() {
            YearWeek yw = YearWeek.of(2024, 10);
            assertThat(yw.minusYears(1)).isEqualTo(YearWeek.of(2023, 10));
        }

        @Test
        @DisplayName("minusWeeks() 减周数")
        void testMinusWeeks() {
            YearWeek yw = YearWeek.of(2024, 2);
            assertThat(yw.minusWeeks(1)).isEqualTo(YearWeek.of(2024, 1));
            assertThat(yw.minusWeeks(2)).isEqualTo(YearWeek.of(2023, 52));
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("atDay() 获取特定星期几的日期")
        void testAtDay() {
            YearWeek yw = YearWeek.of(2024, 1);
            assertThat(yw.atDay(DayOfWeek.MONDAY)).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(yw.atDay(DayOfWeek.FRIDAY)).isEqualTo(LocalDate.of(2024, 1, 5));
        }

        @Test
        @DisplayName("atDay() null抛出异常")
        void testAtDayNull() {
            assertThatThrownBy(() -> YearWeek.of(2024, 1).atDay(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("atMonday() 获取周一")
        void testAtMonday() {
            assertThat(YearWeek.of(2024, 1).atMonday())
                    .isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("atTuesday() 获取周二")
        void testAtTuesday() {
            assertThat(YearWeek.of(2024, 1).atTuesday())
                    .isEqualTo(LocalDate.of(2024, 1, 2));
        }

        @Test
        @DisplayName("atWednesday() 获取周三")
        void testAtWednesday() {
            assertThat(YearWeek.of(2024, 1).atWednesday())
                    .isEqualTo(LocalDate.of(2024, 1, 3));
        }

        @Test
        @DisplayName("atThursday() 获取周四")
        void testAtThursday() {
            assertThat(YearWeek.of(2024, 1).atThursday())
                    .isEqualTo(LocalDate.of(2024, 1, 4));
        }

        @Test
        @DisplayName("atFriday() 获取周五")
        void testAtFriday() {
            assertThat(YearWeek.of(2024, 1).atFriday())
                    .isEqualTo(LocalDate.of(2024, 1, 5));
        }

        @Test
        @DisplayName("atSaturday() 获取周六")
        void testAtSaturday() {
            assertThat(YearWeek.of(2024, 1).atSaturday())
                    .isEqualTo(LocalDate.of(2024, 1, 6));
        }

        @Test
        @DisplayName("atSunday() 获取周日")
        void testAtSunday() {
            assertThat(YearWeek.of(2024, 1).atSunday())
                    .isEqualTo(LocalDate.of(2024, 1, 7));
        }
    }

    @Nested
    @DisplayName("比较方法测试")
    class ComparisonTests {

        @Test
        @DisplayName("isBefore() 在之前")
        void testIsBefore() {
            YearWeek yw1 = YearWeek.of(2024, 1);
            YearWeek yw2 = YearWeek.of(2024, 2);
            assertThat(yw1.isBefore(yw2)).isTrue();
            assertThat(yw2.isBefore(yw1)).isFalse();
        }

        @Test
        @DisplayName("isAfter() 在之后")
        void testIsAfter() {
            YearWeek yw1 = YearWeek.of(2024, 1);
            YearWeek yw2 = YearWeek.of(2024, 2);
            assertThat(yw2.isAfter(yw1)).isTrue();
            assertThat(yw1.isAfter(yw2)).isFalse();
        }

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            YearWeek yw1 = YearWeek.of(2024, 1);
            YearWeek yw2 = YearWeek.of(2024, 2);
            YearWeek yw3 = YearWeek.of(2025, 1);

            assertThat(yw1.compareTo(yw2)).isLessThan(0);
            assertThat(yw2.compareTo(yw1)).isGreaterThan(0);
            assertThat(yw1.compareTo(yw3)).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("格式化方法测试")
    class FormattingTests {

        @Test
        @DisplayName("format() 默认格式化")
        void testFormat() {
            assertThat(YearWeek.of(2024, 1).format()).isEqualTo("2024-W01");
            assertThat(YearWeek.of(2024, 10).format()).isEqualTo("2024-W10");
        }

        @Test
        @DisplayName("toString() 返回format()")
        void testToString() {
            YearWeek yw = YearWeek.of(2024, 5);
            assertThat(yw.toString()).isEqualTo(yw.format());
        }
    }

    @Nested
    @DisplayName("Temporal实现测试")
    class TemporalTests {

        @Test
        @DisplayName("isSupported(TemporalField) 支持的字段")
        void testIsSupportedField() {
            YearWeek yw = YearWeek.of(2024, 1);
            assertThat(yw.isSupported(IsoFields.WEEK_BASED_YEAR)).isTrue();
            assertThat(yw.isSupported(IsoFields.WEEK_OF_WEEK_BASED_YEAR)).isTrue();
        }

        @Test
        @DisplayName("isSupported(TemporalUnit) 支持的单位")
        void testIsSupportedUnit() {
            YearWeek yw = YearWeek.of(2024, 1);
            assertThat(yw.isSupported(ChronoUnit.WEEKS)).isTrue();
            assertThat(yw.isSupported(ChronoUnit.YEARS)).isTrue();
        }

        @Test
        @DisplayName("getLong() 获取字段值")
        void testGetLong() {
            YearWeek yw = YearWeek.of(2024, 10);
            assertThat(yw.getLong(IsoFields.WEEK_BASED_YEAR)).isEqualTo(2024);
            assertThat(yw.getLong(IsoFields.WEEK_OF_WEEK_BASED_YEAR)).isEqualTo(10);
        }

        @Test
        @DisplayName("with() 设置字段值")
        void testWith() {
            YearWeek yw = YearWeek.of(2024, 1);
            assertThat(yw.with(IsoFields.WEEK_BASED_YEAR, 2025))
                    .isEqualTo(YearWeek.of(2025, 1));
            assertThat(yw.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 10))
                    .isEqualTo(YearWeek.of(2024, 10));
        }

        @Test
        @DisplayName("plus() 加时间量")
        void testPlus() {
            YearWeek yw = YearWeek.of(2024, 1);
            assertThat(yw.plus(1, ChronoUnit.YEARS))
                    .isEqualTo(YearWeek.of(2025, 1));
            assertThat(yw.plus(2, ChronoUnit.WEEKS))
                    .isEqualTo(YearWeek.of(2024, 3));
        }

        @Test
        @DisplayName("until() 计算时间差")
        void testUntil() {
            YearWeek yw1 = YearWeek.of(2024, 1);
            YearWeek yw2 = YearWeek.of(2024, 11);
            assertThat(yw1.until(yw2, ChronoUnit.WEEKS)).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("TemporalAdjuster实现测试")
    class TemporalAdjusterTests {

        @Test
        @DisplayName("adjustInto() 调整日期")
        void testAdjustInto() {
            YearWeek yw = YearWeek.of(2024, 10);
            LocalDate date = LocalDate.of(2023, 1, 15);
            LocalDate adjusted = (LocalDate) yw.adjustInto(date);
            assertThat(YearWeek.from(adjusted)).isEqualTo(yw);
        }
    }

    @Nested
    @DisplayName("equals/hashCode测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            YearWeek yw1 = YearWeek.of(2024, 10);
            YearWeek yw2 = YearWeek.of(2024, 10);
            YearWeek yw3 = YearWeek.of(2024, 11);

            assertThat(yw1).isEqualTo(yw2);
            assertThat(yw1).isNotEqualTo(yw3);
            assertThat(yw1).isEqualTo(yw1);
            assertThat(yw1).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            YearWeek yw1 = YearWeek.of(2024, 10);
            YearWeek yw2 = YearWeek.of(2024, 10);
            assertThat(yw1.hashCode()).isEqualTo(yw2.hashCode());
        }
    }
}
