package cloud.opencode.base.date.extra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * YearHalf 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("YearHalf 测试")
class YearHalfTest {

    @Nested
    @DisplayName("Half枚举测试")
    class HalfEnumTests {

        @Test
        @DisplayName("getValue() 获取值")
        void testGetValue() {
            assertThat(YearHalf.Half.H1.getValue()).isEqualTo(1);
            assertThat(YearHalf.Half.H2.getValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("firstMonth() 获取第一个月")
        void testFirstMonth() {
            assertThat(YearHalf.Half.H1.firstMonth()).isEqualTo(Month.JANUARY);
            assertThat(YearHalf.Half.H2.firstMonth()).isEqualTo(Month.JULY);
        }

        @Test
        @DisplayName("lastMonth() 获取最后一个月")
        void testLastMonth() {
            assertThat(YearHalf.Half.H1.lastMonth()).isEqualTo(Month.JUNE);
            assertThat(YearHalf.Half.H2.lastMonth()).isEqualTo(Month.DECEMBER);
        }

        @Test
        @DisplayName("of() 从值获取Half")
        void testOf() {
            assertThat(YearHalf.Half.of(1)).isEqualTo(YearHalf.Half.H1);
            assertThat(YearHalf.Half.of(2)).isEqualTo(YearHalf.Half.H2);
        }

        @Test
        @DisplayName("of() 无效值抛出异常")
        void testOfInvalid() {
            assertThatThrownBy(() -> YearHalf.Half.of(0))
                    .isInstanceOf(DateTimeException.class);
            assertThatThrownBy(() -> YearHalf.Half.of(3))
                    .isInstanceOf(DateTimeException.class);
        }

        @Test
        @DisplayName("ofMonth(Month) 从月份获取Half")
        void testOfMonthMonth() {
            assertThat(YearHalf.Half.ofMonth(Month.MARCH)).isEqualTo(YearHalf.Half.H1);
            assertThat(YearHalf.Half.ofMonth(Month.AUGUST)).isEqualTo(YearHalf.Half.H2);
        }

        @Test
        @DisplayName("ofMonth(int) 从月份值获取Half")
        void testOfMonthInt() {
            assertThat(YearHalf.Half.ofMonth(6)).isEqualTo(YearHalf.Half.H1);
            assertThat(YearHalf.Half.ofMonth(7)).isEqualTo(YearHalf.Half.H2);
        }

        @Test
        @DisplayName("ofMonth(int) 无效值抛出异常")
        void testOfMonthIntInvalid() {
            assertThatThrownBy(() -> YearHalf.Half.ofMonth(0))
                    .isInstanceOf(DateTimeException.class);
            assertThatThrownBy(() -> YearHalf.Half.ofMonth(13))
                    .isInstanceOf(DateTimeException.class);
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(int, Half) 创建YearHalf")
        void testOfIntHalf() {
            YearHalf yh = YearHalf.of(2024, YearHalf.Half.H1);
            assertThat(yh.getYear()).isEqualTo(2024);
            assertThat(yh.getHalf()).isEqualTo(YearHalf.Half.H1);
        }

        @Test
        @DisplayName("of(int, int) 创建YearHalf")
        void testOfIntInt() {
            YearHalf yh = YearHalf.of(2024, 2);
            assertThat(yh.getYear()).isEqualTo(2024);
            assertThat(yh.getHalfValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("now() 获取当前YearHalf")
        void testNow() {
            YearHalf yh = YearHalf.now();
            assertThat(yh).isNotNull();
        }

        @Test
        @DisplayName("from() 从日期创建")
        void testFrom() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            YearHalf yh = YearHalf.from(date);
            assertThat(yh.getYear()).isEqualTo(2024);
            assertThat(yh.getHalf()).isEqualTo(YearHalf.Half.H1);

            date = LocalDate.of(2024, 8, 15);
            yh = YearHalf.from(date);
            assertThat(yh.getHalf()).isEqualTo(YearHalf.Half.H2);
        }

        @Test
        @DisplayName("from() 从YearHalf返回自身")
        void testFromYearHalf() {
            YearHalf original = YearHalf.of(2024, 1);
            assertThat(YearHalf.from(original)).isSameAs(original);
        }

        @Test
        @DisplayName("parse() 解析字符串")
        void testParse() {
            YearHalf yh = YearHalf.parse("2024-H1");
            assertThat(yh.getYear()).isEqualTo(2024);
            assertThat(yh.getHalfValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("parse() 小写字符串")
        void testParseLowerCase() {
            YearHalf yh = YearHalf.parse("2024-h2");
            assertThat(yh.getHalfValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("parse() 无效字符串抛出异常")
        void testParseInvalid() {
            assertThatThrownBy(() -> YearHalf.parse("invalid"))
                    .isInstanceOf(DateTimeException.class);
        }
    }

    @Nested
    @DisplayName("日期操作测试")
    class DateOperationTests {

        @Test
        @DisplayName("atStart() 获取开始日期")
        void testAtStart() {
            assertThat(YearHalf.of(2024, 1).atStart())
                    .isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(YearHalf.of(2024, 2).atStart())
                    .isEqualTo(LocalDate.of(2024, 7, 1));
        }

        @Test
        @DisplayName("atEnd() 获取结束日期")
        void testAtEnd() {
            assertThat(YearHalf.of(2024, 1).atEnd())
                    .isEqualTo(LocalDate.of(2024, 6, 30));
            assertThat(YearHalf.of(2024, 2).atEnd())
                    .isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        @DisplayName("atDay() 获取特定日期")
        void testAtDay() {
            assertThat(YearHalf.of(2024, 1).atDay(3, 15))
                    .isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("atDay() H1无效月份抛出异常")
        void testAtDayInvalidH1() {
            assertThatThrownBy(() -> YearHalf.of(2024, 1).atDay(7, 1))
                    .isInstanceOf(DateTimeException.class);
        }

        @Test
        @DisplayName("atDay() H2无效月份抛出异常")
        void testAtDayInvalidH2() {
            assertThatThrownBy(() -> YearHalf.of(2024, 2).atDay(1, 1))
                    .isInstanceOf(DateTimeException.class);
        }
    }

    @Nested
    @DisplayName("算术测试")
    class ArithmeticTests {

        @Test
        @DisplayName("plusHalves() 加半年")
        void testPlusHalves() {
            YearHalf yh = YearHalf.of(2024, 1);
            assertThat(yh.plusHalves(1)).isEqualTo(YearHalf.of(2024, 2));
            assertThat(yh.plusHalves(2)).isEqualTo(YearHalf.of(2025, 1));
            assertThat(yh.plusHalves(3)).isEqualTo(YearHalf.of(2025, 2));
        }

        @Test
        @DisplayName("plusHalves() 加0返回自身")
        void testPlusHalvesZero() {
            YearHalf yh = YearHalf.of(2024, 1);
            assertThat(yh.plusHalves(0)).isSameAs(yh);
        }

        @Test
        @DisplayName("minusHalves() 减半年")
        void testMinusHalves() {
            YearHalf yh = YearHalf.of(2024, 2);
            assertThat(yh.minusHalves(1)).isEqualTo(YearHalf.of(2024, 1));
            assertThat(yh.minusHalves(2)).isEqualTo(YearHalf.of(2023, 2));
        }

        @Test
        @DisplayName("plusYears() 加年数")
        void testPlusYears() {
            YearHalf yh = YearHalf.of(2024, 1);
            assertThat(yh.plusYears(1)).isEqualTo(YearHalf.of(2025, 1));
        }

        @Test
        @DisplayName("minusYears() 减年数")
        void testMinusYears() {
            YearHalf yh = YearHalf.of(2024, 1);
            assertThat(yh.minusYears(1)).isEqualTo(YearHalf.of(2023, 1));
        }
    }

    @Nested
    @DisplayName("长度测试")
    class LengthTests {

        @Test
        @DisplayName("lengthInDays() 获取天数")
        void testLengthInDays() {
            // H1 non-leap year
            assertThat(YearHalf.of(2023, 1).lengthInDays()).isEqualTo(181);
            // H1 leap year
            assertThat(YearHalf.of(2024, 1).lengthInDays()).isEqualTo(182);
            // H2 (always 184)
            assertThat(YearHalf.of(2024, 2).lengthInDays()).isEqualTo(184);
        }

        @Test
        @DisplayName("lengthInMonths() 获取月数")
        void testLengthInMonths() {
            assertThat(YearHalf.of(2024, 1).lengthInMonths()).isEqualTo(6);
            assertThat(YearHalf.of(2024, 2).lengthInMonths()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("Temporal实现测试")
    class TemporalTests {

        @Test
        @DisplayName("isSupported(TemporalField) 支持的字段")
        void testIsSupportedField() {
            YearHalf yh = YearHalf.of(2024, 1);
            assertThat(yh.isSupported(ChronoField.YEAR)).isTrue();
            assertThat(yh.isSupported(ChronoField.MONTH_OF_YEAR)).isTrue();
        }

        @Test
        @DisplayName("isSupported(TemporalUnit) 支持的单位")
        void testIsSupportedUnit() {
            YearHalf yh = YearHalf.of(2024, 1);
            assertThat(yh.isSupported(ChronoUnit.YEARS)).isTrue();
            assertThat(yh.isSupported(ChronoUnit.MONTHS)).isTrue();
        }

        @Test
        @DisplayName("getLong() 获取字段值")
        void testGetLong() {
            YearHalf yh = YearHalf.of(2024, 2);
            assertThat(yh.getLong(ChronoField.YEAR)).isEqualTo(2024);
            assertThat(yh.getLong(ChronoField.MONTH_OF_YEAR)).isEqualTo(7);
        }

        @Test
        @DisplayName("with() 设置字段值")
        void testWith() {
            YearHalf yh = YearHalf.of(2024, 1);
            assertThat(yh.with(ChronoField.YEAR, 2025))
                    .isEqualTo(YearHalf.of(2025, 1));
        }

        @Test
        @DisplayName("plus() 加时间量")
        void testPlus() {
            YearHalf yh = YearHalf.of(2024, 1);
            assertThat(yh.plus(1, ChronoUnit.YEARS))
                    .isEqualTo(YearHalf.of(2025, 1));
        }

        @Test
        @DisplayName("until() 计算时间差")
        void testUntil() {
            YearHalf yh1 = YearHalf.of(2024, 1);
            YearHalf yh2 = YearHalf.of(2025, 2);
            assertThat(yh1.until(yh2, ChronoUnit.YEARS)).isEqualTo(1);
        }

        @Test
        @DisplayName("range() 获取字段值范围")
        void testRange() {
            YearHalf yh = YearHalf.of(2024, 1);
            java.time.temporal.ValueRange yearRange = yh.range(ChronoField.YEAR);
            assertThat(yearRange).isNotNull();
            assertThat(yearRange.getMinimum()).isLessThan(yearRange.getMaximum());
        }

        @Test
        @DisplayName("get() 获取字段值")
        void testGet() {
            YearHalf yh = YearHalf.of(2024, 2);
            assertThat(yh.get(ChronoField.YEAR)).isEqualTo(2024);
        }

        @Test
        @DisplayName("query() 查询方法")
        void testQuery() {
            YearHalf yh = YearHalf.of(2024, 1);
            java.time.temporal.TemporalQuery<java.time.LocalDate> query = java.time.temporal.TemporalQueries.localDate();
            // query可能返回null，这取决于实现
            yh.query(query); // 只验证不抛异常
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            YearHalf yh1 = YearHalf.of(2024, 1);
            YearHalf yh2 = YearHalf.of(2024, 2);
            YearHalf yh3 = YearHalf.of(2025, 1);

            assertThat(yh1.compareTo(yh2)).isLessThan(0);
            assertThat(yh2.compareTo(yh1)).isGreaterThan(0);
            assertThat(yh1.compareTo(yh3)).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("TemporalAdjuster实现测试")
    class TemporalAdjusterTests {

        @Test
        @DisplayName("adjustInto() 调整日期")
        void testAdjustInto() {
            YearHalf yh = YearHalf.of(2024, 2);
            LocalDate date = LocalDate.of(2023, 3, 15);
            LocalDate adjusted = (LocalDate) yh.adjustInto(date);
            assertThat(adjusted).isEqualTo(LocalDate.of(2024, 7, 1));
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            YearHalf yh1 = YearHalf.of(2024, 1);
            YearHalf yh2 = YearHalf.of(2024, 1);
            YearHalf yh3 = YearHalf.of(2024, 2);

            assertThat(yh1).isEqualTo(yh2);
            assertThat(yh1).isNotEqualTo(yh3);
            assertThat(yh1).isEqualTo(yh1);
            assertThat(yh1).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            YearHalf yh1 = YearHalf.of(2024, 1);
            YearHalf yh2 = YearHalf.of(2024, 1);
            assertThat(yh1.hashCode()).isEqualTo(yh2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(YearHalf.of(2024, 1).toString()).isEqualTo("2024-H1");
            assertThat(YearHalf.of(2024, 2).toString()).isEqualTo("2024-H2");
        }
    }
}
