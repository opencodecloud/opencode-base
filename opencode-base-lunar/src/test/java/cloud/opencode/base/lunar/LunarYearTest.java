package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.ganzhi.GanZhi;
import cloud.opencode.base.lunar.internal.LunarData;
import cloud.opencode.base.lunar.zodiac.Zodiac;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * LunarYear 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.3
 */
@DisplayName("LunarYear 测试")
class LunarYearTest {

    @Nested
    @DisplayName("构造和工厂方法测试")
    class ConstructionTests {

        @Test
        @DisplayName("of创建LunarYear")
        void testOf() {
            LunarYear year = LunarYear.of(2024);
            assertThat(year.year()).isEqualTo(2024);
        }

        @Test
        @DisplayName("构造函数创建LunarYear")
        void testConstructor() {
            LunarYear year = new LunarYear(2024);
            assertThat(year.year()).isEqualTo(2024);
        }

        @Test
        @DisplayName("边界年份1900有效")
        void testMinYear() {
            LunarYear year = LunarYear.of(1900);
            assertThat(year.year()).isEqualTo(1900);
        }

        @Test
        @DisplayName("边界年份2100有效")
        void testMaxYear() {
            LunarYear year = LunarYear.of(2100);
            assertThat(year.year()).isEqualTo(2100);
        }

        @Test
        @DisplayName("年份小于1900抛异常")
        void testYearTooSmall() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LunarYear.of(1899))
                    .withMessageContaining("1900");
        }

        @Test
        @DisplayName("年份大于2100抛异常")
        void testYearTooLarge() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LunarYear.of(2101))
                    .withMessageContaining("2100");
        }
    }

    @Nested
    @DisplayName("getTotalDays测试")
    class TotalDaysTests {

        @Test
        @DisplayName("2024年总天数合理")
        void testTotalDays2024() {
            LunarYear year = LunarYear.of(2024);
            int totalDays = year.getTotalDays();

            // Lunar year typically 353-385 days
            assertThat(totalDays).isBetween(353, 385);
        }

        @Test
        @DisplayName("有闰月的年总天数大于无闰月的年")
        void testLeapYearHasMoreDays() {
            // 2020 has leap month 4
            LunarYear leapYear = LunarYear.of(2020);
            // 2019 has no leap month
            LunarYear normalYear = LunarYear.of(2019);

            assertThat(leapYear.getTotalDays()).isGreaterThan(normalYear.getTotalDays());
        }
    }

    @Nested
    @DisplayName("闰月测试")
    class LeapMonthTests {

        @Test
        @DisplayName("2020年闰四月")
        void testLeapMonth2020() {
            LunarYear year = LunarYear.of(2020);

            assertThat(year.getLeapMonth()).isEqualTo(4);
            assertThat(year.hasLeapMonth()).isTrue();
        }

        @Test
        @DisplayName("2024年无闰月")
        void testNoLeapMonth2024() {
            LunarYear year = LunarYear.of(2024);

            assertThat(year.getLeapMonth()).isZero();
            assertThat(year.hasLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("闰月天数为29或30")
        void testLeapMonthDays() {
            LunarYear year = LunarYear.of(2020);
            int leapDays = year.getLeapMonthDays();

            assertThat(leapDays).isIn(29, 30);
        }

        @Test
        @DisplayName("无闰月时闰月天数为0")
        void testNoLeapMonthDays() {
            LunarYear year = LunarYear.of(2024);

            assertThat(year.getLeapMonthDays()).isZero();
        }
    }

    @Nested
    @DisplayName("getMonthCount测试")
    class MonthCountTests {

        @Test
        @DisplayName("有闰月的年有13个月")
        void testLeapYearMonthCount() {
            LunarYear year = LunarYear.of(2020);
            assertThat(year.getMonthCount()).isEqualTo(13);
        }

        @Test
        @DisplayName("无闰月的年有12个月")
        void testNormalYearMonthCount() {
            LunarYear year = LunarYear.of(2024);
            assertThat(year.getMonthCount()).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("getMonths测试")
    class GetMonthsTests {

        @Test
        @DisplayName("无闰月的年返回12个月")
        void testNormalYearMonths() {
            LunarYear year = LunarYear.of(2024);
            List<LunarMonth> months = year.getMonths();

            assertThat(months).hasSize(12);
        }

        @Test
        @DisplayName("有闰月的年返回13个月")
        void testLeapYearMonths() {
            LunarYear year = LunarYear.of(2020);
            List<LunarMonth> months = year.getMonths();

            assertThat(months).hasSize(13);
        }

        @Test
        @DisplayName("闰月紧跟在常规月之后")
        void testLeapMonthOrder() {
            LunarYear year = LunarYear.of(2020); // leap month 4
            List<LunarMonth> months = year.getMonths();

            // Index 3 is month 4 (regular), index 4 is leap month 4
            assertThat(months.get(3).month()).isEqualTo(4);
            assertThat(months.get(3).isLeapMonth()).isFalse();
            assertThat(months.get(4).month()).isEqualTo(4);
            assertThat(months.get(4).isLeapMonth()).isTrue();
        }

        @Test
        @DisplayName("月份列表不可修改")
        void testMonthsUnmodifiable() {
            LunarYear year = LunarYear.of(2024);
            List<LunarMonth> months = year.getMonths();

            assertThatThrownBy(() -> months.add(LunarMonth.of(2024, 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("月份总天数等于年总天数")
        void testMonthDaysSumEqualsYearDays() {
            LunarYear year = LunarYear.of(2020);
            List<LunarMonth> months = year.getMonths();
            int sum = months.stream().mapToInt(LunarMonth::getDays).sum();

            assertThat(sum).isEqualTo(year.getTotalDays());
        }
    }

    @Nested
    @DisplayName("干支生肖测试")
    class GanZhiZodiacTests {

        @Test
        @DisplayName("2024年干支为甲辰")
        void testGanZhi2024() {
            LunarYear year = LunarYear.of(2024);
            GanZhi gz = year.getGanZhi();

            assertThat(gz.getName()).isEqualTo("甲辰");
        }

        @Test
        @DisplayName("2024年生肖为龙")
        void testZodiac2024() {
            LunarYear year = LunarYear.of(2024);
            Zodiac zodiac = year.getZodiac();

            assertThat(zodiac).isEqualTo(Zodiac.DRAGON);
        }

        @Test
        @DisplayName("2023年生肖为兔")
        void testZodiac2023() {
            LunarYear year = LunarYear.of(2023);
            assertThat(year.getZodiac()).isEqualTo(Zodiac.RABBIT);
        }
    }

    @Nested
    @DisplayName("getName测试")
    class GetNameTests {

        @Test
        @DisplayName("2024年名称为甲辰年")
        void testName2024() {
            LunarYear year = LunarYear.of(2024);
            assertThat(year.getName()).isEqualTo("甲辰年");
        }

        @Test
        @DisplayName("toString等于getName")
        void testToString() {
            LunarYear year = LunarYear.of(2024);
            assertThat(year.toString()).isEqualTo(year.getName());
        }
    }

    @Nested
    @DisplayName("getMonth测试")
    class GetMonthTests {

        @Test
        @DisplayName("获取正月")
        void testGetFirstMonth() {
            LunarYear year = LunarYear.of(2024);
            LunarMonth m = year.getMonth(1);

            assertThat(m.year()).isEqualTo(2024);
            assertThat(m.month()).isEqualTo(1);
            assertThat(m.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("获取闰月")
        void testGetLeapMonth() {
            LunarYear year = LunarYear.of(2020);
            LunarMonth m = year.getMonth(4, true);

            assertThat(m.year()).isEqualTo(2020);
            assertThat(m.month()).isEqualTo(4);
            assertThat(m.isLeapMonth()).isTrue();
        }

        @Test
        @DisplayName("获取非存在的闰月抛异常")
        void testGetNonExistentLeapMonth() {
            LunarYear year = LunarYear.of(2024);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> year.getMonth(1, true));
        }
    }

    @Nested
    @DisplayName("Record基本测试")
    class RecordTests {

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            LunarYear y1 = LunarYear.of(2024);
            LunarYear y2 = LunarYear.of(2024);
            LunarYear y3 = LunarYear.of(2023);

            assertThat(y1).isEqualTo(y2);
            assertThat(y1).isNotEqualTo(y3);
        }

        @Test
        @DisplayName("hashCode一致")
        void testHashCode() {
            LunarYear y1 = LunarYear.of(2024);
            LunarYear y2 = LunarYear.of(2024);

            assertThat(y1.hashCode()).isEqualTo(y2.hashCode());
        }
    }
}
