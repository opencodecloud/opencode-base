package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoField;

import static org.assertj.core.api.Assertions.*;

/**
 * DayOfYear 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("DayOfYear 测试")
class DayOfYearTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of() 创建DayOfYear")
        void testOf() {
            DayOfYear doy = DayOfYear.of(100);
            assertThat(doy.getValue()).isEqualTo(100);
        }

        @Test
        @DisplayName("of() 无效值抛出异常")
        void testOfInvalid() {
            assertThatThrownBy(() -> DayOfYear.of(0))
                    .isInstanceOf(OpenDateException.class);
            assertThatThrownBy(() -> DayOfYear.of(367))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("first() 获取第一天")
        void testFirst() {
            DayOfYear doy = DayOfYear.first();
            assertThat(doy.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("lastOf() 获取年份最后一天")
        void testLastOf() {
            assertThat(DayOfYear.lastOf(2024).getValue()).isEqualTo(366); // leap
            assertThat(DayOfYear.lastOf(2023).getValue()).isEqualTo(365); // non-leap
        }

        @Test
        @DisplayName("from() 从日期创建")
        void testFrom() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            DayOfYear doy = DayOfYear.from(date);
            assertThat(doy.getValue()).isEqualTo(100);
        }

        @Test
        @DisplayName("from() 从DayOfYear返回自身")
        void testFromDayOfYear() {
            DayOfYear original = DayOfYear.of(200);
            assertThat(DayOfYear.from(original)).isSameAs(original);
        }

        @Test
        @DisplayName("now() 获取当前天")
        void testNow() {
            DayOfYear doy = DayOfYear.now();
            assertThat(doy.getValue()).isBetween(1, 366);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("isValidFor(Year) 验证")
        void testIsValidForYear() {
            DayOfYear doy365 = DayOfYear.of(365);
            DayOfYear doy366 = DayOfYear.of(366);

            assertThat(doy365.isValidFor(Year.of(2024))).isTrue();
            assertThat(doy365.isValidFor(Year.of(2023))).isTrue();
            assertThat(doy366.isValidFor(Year.of(2024))).isTrue(); // leap year
            assertThat(doy366.isValidFor(Year.of(2023))).isFalse(); // non-leap
        }

        @Test
        @DisplayName("isValidFor(Year) null抛出异常")
        void testIsValidForYearNull() {
            assertThatThrownBy(() -> DayOfYear.of(100).isValidFor((Year) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isValidFor(int) 验证")
        void testIsValidForInt() {
            DayOfYear doy366 = DayOfYear.of(366);
            assertThat(doy366.isValidFor(2024)).isTrue();
            assertThat(doy366.isValidFor(2023)).isFalse();
        }

        @Test
        @DisplayName("isValidForAllYears() 验证")
        void testIsValidForAllYears() {
            assertThat(DayOfYear.of(365).isValidForAllYears()).isTrue();
            assertThat(DayOfYear.of(366).isValidForAllYears()).isFalse();
        }

        @Test
        @DisplayName("isFirst() 检查是否为第一天")
        void testIsFirst() {
            assertThat(DayOfYear.of(1).isFirst()).isTrue();
            assertThat(DayOfYear.of(2).isFirst()).isFalse();
        }

        @Test
        @DisplayName("isLeapDay() 检查是否为闰年额外天")
        void testIsLeapDay() {
            assertThat(DayOfYear.of(366).isLeapDay()).isTrue();
            assertThat(DayOfYear.of(365).isLeapDay()).isFalse();
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("atYear(Year) 组合日期")
        void testAtYearYear() {
            DayOfYear doy = DayOfYear.of(100);
            LocalDate date = doy.atYear(Year.of(2024));
            assertThat(date).isEqualTo(LocalDate.of(2024, 4, 9));
        }

        @Test
        @DisplayName("atYear(Year) 无效天数抛出异常")
        void testAtYearInvalid() {
            DayOfYear doy = DayOfYear.of(366);
            assertThatThrownBy(() -> doy.atYear(Year.of(2023)))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("atYear(Year) null抛出异常")
        void testAtYearNull() {
            assertThatThrownBy(() -> DayOfYear.of(100).atYear((Year) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("atYear(int) 组合日期")
        void testAtYearInt() {
            DayOfYear doy = DayOfYear.of(100);
            LocalDate date = doy.atYear(2024);
            assertThat(date).isEqualTo(LocalDate.of(2024, 4, 9));
        }
    }

    @Nested
    @DisplayName("TemporalAccessor实现测试")
    class TemporalAccessorTests {

        @Test
        @DisplayName("isSupported() 支持的字段")
        void testIsSupported() {
            DayOfYear doy = DayOfYear.of(100);
            assertThat(doy.isSupported(ChronoField.DAY_OF_YEAR)).isTrue();
            assertThat(doy.isSupported(ChronoField.MONTH_OF_YEAR)).isFalse();
        }

        @Test
        @DisplayName("getLong() 获取字段值")
        void testGetLong() {
            DayOfYear doy = DayOfYear.of(100);
            assertThat(doy.getLong(ChronoField.DAY_OF_YEAR)).isEqualTo(100);
        }

        @Test
        @DisplayName("getLong() 不支持字段抛出异常")
        void testGetLongUnsupported() {
            assertThatThrownBy(() -> DayOfYear.of(100).getLong(ChronoField.YEAR))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("range() 获取字段范围")
        void testRange() {
            DayOfYear doy = DayOfYear.of(100);
            assertThat(doy.range(ChronoField.DAY_OF_YEAR).getMinimum()).isEqualTo(1);
            assertThat(doy.range(ChronoField.DAY_OF_YEAR).getMaximum()).isEqualTo(366);
        }
    }

    @Nested
    @DisplayName("TemporalAdjuster实现测试")
    class TemporalAdjusterTests {

        @Test
        @DisplayName("adjustInto() 调整日期")
        void testAdjustInto() {
            DayOfYear doy = DayOfYear.of(200);
            LocalDate date = LocalDate.of(2024, 1, 1);
            LocalDate adjusted = (LocalDate) doy.adjustInto(date);
            assertThat(adjusted.getDayOfYear()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Query测试")
    class QueryTests {

        @Test
        @DisplayName("query() 获取查询")
        void testQuery() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            DayOfYear doy = date.query(DayOfYear.query());
            assertThat(doy.getValue()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            DayOfYear doy1 = DayOfYear.of(100);
            DayOfYear doy2 = DayOfYear.of(200);

            assertThat(doy1.compareTo(doy2)).isLessThan(0);
            assertThat(doy2.compareTo(doy1)).isGreaterThan(0);
            assertThat(doy1.compareTo(DayOfYear.of(100))).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            DayOfYear doy1 = DayOfYear.of(100);
            DayOfYear doy2 = DayOfYear.of(100);
            DayOfYear doy3 = DayOfYear.of(101);

            assertThat(doy1).isEqualTo(doy2);
            assertThat(doy1).isNotEqualTo(doy3);
            assertThat(doy1).isEqualTo(doy1);
            assertThat(doy1).isNotEqualTo(null);
            assertThat(doy1).isNotEqualTo("100");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            DayOfYear doy1 = DayOfYear.of(100);
            DayOfYear doy2 = DayOfYear.of(100);
            assertThat(doy1.hashCode()).isEqualTo(doy2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(DayOfYear.of(100).toString()).isEqualTo("DayOfYear(100)");
        }
    }
}
