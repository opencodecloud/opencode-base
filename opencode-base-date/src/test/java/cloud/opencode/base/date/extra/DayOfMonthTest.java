package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoField;

import static org.assertj.core.api.Assertions.*;

/**
 * DayOfMonth 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("DayOfMonth 测试")
class DayOfMonthTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of() 创建DayOfMonth")
        void testOf() {
            DayOfMonth dom = DayOfMonth.of(15);
            assertThat(dom.getValue()).isEqualTo(15);
        }

        @Test
        @DisplayName("of() 缓存实例")
        void testOfCached() {
            DayOfMonth dom1 = DayOfMonth.of(10);
            DayOfMonth dom2 = DayOfMonth.of(10);
            assertThat(dom1).isSameAs(dom2);
        }

        @Test
        @DisplayName("of() 无效值抛出异常")
        void testOfInvalid() {
            assertThatThrownBy(() -> DayOfMonth.of(0))
                    .isInstanceOf(OpenDateException.class);
            assertThatThrownBy(() -> DayOfMonth.of(32))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("first() 获取第一天")
        void testFirst() {
            DayOfMonth dom = DayOfMonth.first();
            assertThat(dom.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("from() 从日期创建")
        void testFrom() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            DayOfMonth dom = DayOfMonth.from(date);
            assertThat(dom.getValue()).isEqualTo(15);
        }

        @Test
        @DisplayName("from() 从DayOfMonth返回自身")
        void testFromDayOfMonth() {
            DayOfMonth original = DayOfMonth.of(20);
            assertThat(DayOfMonth.from(original)).isSameAs(original);
        }

        @Test
        @DisplayName("now() 获取当前天")
        void testNow() {
            DayOfMonth dom = DayOfMonth.now();
            assertThat(dom.getValue()).isBetween(1, 31);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("isValidFor(YearMonth) 验证")
        void testIsValidForYearMonth() {
            DayOfMonth dom15 = DayOfMonth.of(15);
            DayOfMonth dom31 = DayOfMonth.of(31);
            DayOfMonth dom29 = DayOfMonth.of(29);

            assertThat(dom15.isValidFor(YearMonth.of(2024, 2))).isTrue();
            assertThat(dom31.isValidFor(YearMonth.of(2024, 2))).isFalse();
            assertThat(dom29.isValidFor(YearMonth.of(2024, 2))).isTrue(); // leap year
            assertThat(dom29.isValidFor(YearMonth.of(2023, 2))).isFalse(); // non-leap
        }

        @Test
        @DisplayName("isValidFor(YearMonth) null抛出异常")
        void testIsValidForYearMonthNull() {
            assertThatThrownBy(() -> DayOfMonth.of(15).isValidFor((YearMonth) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isValidFor(Month, boolean) 验证")
        void testIsValidForMonthBoolean() {
            DayOfMonth dom29 = DayOfMonth.of(29);
            DayOfMonth dom31 = DayOfMonth.of(31);

            assertThat(dom29.isValidFor(Month.FEBRUARY, true)).isTrue();
            assertThat(dom29.isValidFor(Month.FEBRUARY, false)).isFalse();
            assertThat(dom31.isValidFor(Month.JANUARY, false)).isTrue();
            assertThat(dom31.isValidFor(Month.APRIL, false)).isFalse();
        }

        @Test
        @DisplayName("isValidFor(Month, boolean) null抛出异常")
        void testIsValidForMonthNull() {
            assertThatThrownBy(() -> DayOfMonth.of(15).isValidFor((Month) null, true))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isFirst() 检查是否为第一天")
        void testIsFirst() {
            assertThat(DayOfMonth.of(1).isFirst()).isTrue();
            assertThat(DayOfMonth.of(2).isFirst()).isFalse();
        }

        @Test
        @DisplayName("isPossibleLastDay() 检查是否可能为最后一天")
        void testIsPossibleLastDay() {
            assertThat(DayOfMonth.of(27).isPossibleLastDay()).isFalse();
            assertThat(DayOfMonth.of(28).isPossibleLastDay()).isTrue();
            assertThat(DayOfMonth.of(29).isPossibleLastDay()).isTrue();
            assertThat(DayOfMonth.of(30).isPossibleLastDay()).isTrue();
            assertThat(DayOfMonth.of(31).isPossibleLastDay()).isTrue();
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("atYearMonth(YearMonth) 组合日期")
        void testAtYearMonthYearMonth() {
            DayOfMonth dom = DayOfMonth.of(15);
            LocalDate date = dom.atYearMonth(YearMonth.of(2024, 3));
            assertThat(date).isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("atYearMonth(YearMonth) 无效天数抛出异常")
        void testAtYearMonthInvalid() {
            DayOfMonth dom = DayOfMonth.of(31);
            assertThatThrownBy(() -> dom.atYearMonth(YearMonth.of(2024, 2)))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("atYearMonth(YearMonth) null抛出异常")
        void testAtYearMonthNull() {
            assertThatThrownBy(() -> DayOfMonth.of(15).atYearMonth((YearMonth) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("atYearMonth(int, int) 组合日期")
        void testAtYearMonthIntInt() {
            DayOfMonth dom = DayOfMonth.of(15);
            LocalDate date = dom.atYearMonth(2024, 3);
            assertThat(date).isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("atYearMonth(int, Month) 组合日期")
        void testAtYearMonthIntMonth() {
            DayOfMonth dom = DayOfMonth.of(15);
            LocalDate date = dom.atYearMonth(2024, Month.MARCH);
            assertThat(date).isEqualTo(LocalDate.of(2024, 3, 15));
        }
    }

    @Nested
    @DisplayName("TemporalAccessor实现测试")
    class TemporalAccessorTests {

        @Test
        @DisplayName("isSupported() 支持的字段")
        void testIsSupported() {
            DayOfMonth dom = DayOfMonth.of(15);
            assertThat(dom.isSupported(ChronoField.DAY_OF_MONTH)).isTrue();
            assertThat(dom.isSupported(ChronoField.MONTH_OF_YEAR)).isFalse();
        }

        @Test
        @DisplayName("getLong() 获取字段值")
        void testGetLong() {
            DayOfMonth dom = DayOfMonth.of(15);
            assertThat(dom.getLong(ChronoField.DAY_OF_MONTH)).isEqualTo(15);
        }

        @Test
        @DisplayName("getLong() 不支持字段抛出异常")
        void testGetLongUnsupported() {
            assertThatThrownBy(() -> DayOfMonth.of(15).getLong(ChronoField.YEAR))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("range() 获取字段范围")
        void testRange() {
            DayOfMonth dom = DayOfMonth.of(15);
            assertThat(dom.range(ChronoField.DAY_OF_MONTH).getMinimum()).isEqualTo(1);
            assertThat(dom.range(ChronoField.DAY_OF_MONTH).getMaximum()).isEqualTo(31);
        }
    }

    @Nested
    @DisplayName("TemporalAdjuster实现测试")
    class TemporalAdjusterTests {

        @Test
        @DisplayName("adjustInto() 调整日期")
        void testAdjustInto() {
            DayOfMonth dom = DayOfMonth.of(20);
            LocalDate date = LocalDate.of(2024, 3, 5);
            LocalDate adjusted = (LocalDate) dom.adjustInto(date);
            assertThat(adjusted).isEqualTo(LocalDate.of(2024, 3, 20));
        }
    }

    @Nested
    @DisplayName("Query测试")
    class QueryTests {

        @Test
        @DisplayName("query() 获取查询")
        void testQuery() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            DayOfMonth dom = date.query(DayOfMonth.query());
            assertThat(dom.getValue()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            DayOfMonth dom1 = DayOfMonth.of(10);
            DayOfMonth dom2 = DayOfMonth.of(20);

            assertThat(dom1.compareTo(dom2)).isLessThan(0);
            assertThat(dom2.compareTo(dom1)).isGreaterThan(0);
            assertThat(dom1.compareTo(DayOfMonth.of(10))).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            DayOfMonth dom1 = DayOfMonth.of(15);
            DayOfMonth dom2 = DayOfMonth.of(15);
            DayOfMonth dom3 = DayOfMonth.of(16);

            assertThat(dom1).isEqualTo(dom2);
            assertThat(dom1).isNotEqualTo(dom3);
            assertThat(dom1).isEqualTo(dom1);
            assertThat(dom1).isNotEqualTo(null);
            assertThat(dom1).isNotEqualTo("15");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            DayOfMonth dom1 = DayOfMonth.of(15);
            DayOfMonth dom2 = DayOfMonth.of(15);
            assertThat(dom1.hashCode()).isEqualTo(dom2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(DayOfMonth.of(15).toString()).isEqualTo("DayOfMonth(15)");
        }
    }
}
