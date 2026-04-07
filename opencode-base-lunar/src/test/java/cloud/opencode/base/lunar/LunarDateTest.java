package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.calendar.Festival;
import cloud.opencode.base.lunar.ganzhi.GanZhi;
import cloud.opencode.base.lunar.zodiac.Zodiac;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * LunarDate 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("LunarDate 测试")
class LunarDateTest {

    @Nested
    @DisplayName("Record基本测试")
    class RecordBasicTests {

        @Test
        @DisplayName("创建非闰月日期")
        void testCreateNonLeapDate() {
            LunarDate date = new LunarDate(2024, 1, 15, false);

            assertThat(date.year()).isEqualTo(2024);
            assertThat(date.month()).isEqualTo(1);
            assertThat(date.day()).isEqualTo(15);
            assertThat(date.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("创建闰月日期")
        void testCreateLeapDate() {
            LunarDate date = new LunarDate(2020, 4, 15, true);

            assertThat(date.year()).isEqualTo(2020);
            assertThat(date.month()).isEqualTo(4);
            assertThat(date.day()).isEqualTo(15);
            assertThat(date.isLeapMonth()).isTrue();
        }

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            LunarDate date1 = new LunarDate(2024, 1, 15, false);
            LunarDate date2 = new LunarDate(2024, 1, 15, false);
            // 2020 has leap month 4
            LunarDate date3 = new LunarDate(2020, 4, 15, false);
            LunarDate date4 = new LunarDate(2020, 4, 15, true);

            assertThat(date1).isEqualTo(date2);
            assertThat(date3).isNotEqualTo(date4);
        }

        @Test
        @DisplayName("hashCode一致")
        void testHashCode() {
            LunarDate date1 = new LunarDate(2024, 1, 15, false);
            LunarDate date2 = new LunarDate(2024, 1, 15, false);

            assertThat(date1.hashCode()).isEqualTo(date2.hashCode());
        }

        @Test
        @DisplayName("toString包含信息")
        void testToString() {
            LunarDate date = new LunarDate(2024, 1, 15, false);
            String str = date.toString();

            // toString returns Chinese format like "甲辰年 正月十五"
            assertThat(str).contains("正月");
            assertThat(str).contains("十五");
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfFactoryTests {

        @Test
        @DisplayName("of(year, month, day)创建非闰月")
        void testOfThreeArgs() {
            LunarDate date = LunarDate.of(2024, 1, 15);

            assertThat(date.year()).isEqualTo(2024);
            assertThat(date.month()).isEqualTo(1);
            assertThat(date.day()).isEqualTo(15);
            assertThat(date.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("of(year, month, day, isLeap)创建闰月")
        void testOfFourArgs() {
            LunarDate date = LunarDate.of(2020, 4, 15, true);

            assertThat(date.year()).isEqualTo(2020);
            assertThat(date.month()).isEqualTo(4);
            assertThat(date.day()).isEqualTo(15);
            assertThat(date.isLeapMonth()).isTrue();
        }

        @Test
        @DisplayName("of(year, month, day, false)等价于of(year, month, day)")
        void testOfFourArgsFalseEqualsThreeArgs() {
            LunarDate d1 = LunarDate.of(2024, 6, 10);
            LunarDate d2 = LunarDate.of(2024, 6, 10, false);

            assertThat(d1).isEqualTo(d2);
        }
    }

    @Nested
    @DisplayName("from工厂方法测试")
    class FromFactoryTests {

        @Test
        @DisplayName("from春节公历日期")
        void testFromSpringFestival() {
            LunarDate date = LunarDate.from(LocalDate.of(2024, 2, 10));

            assertThat(date.year()).isEqualTo(2024);
            assertThat(date.month()).isEqualTo(1);
            assertThat(date.day()).isEqualTo(1);
            assertThat(date.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("from中秋节公历日期")
        void testFromMidAutumn() {
            LunarDate date = LunarDate.from(LocalDate.of(2024, 9, 17));

            assertThat(date.year()).isEqualTo(2024);
            assertThat(date.month()).isEqualTo(8);
            assertThat(date.day()).isEqualTo(15);
        }

        @Test
        @DisplayName("from与toSolar互逆")
        void testFromRoundtrip() {
            LunarDate original = LunarDate.of(2024, 5, 20);
            SolarDate solar = original.toSolar();
            LunarDate roundtrip = LunarDate.from(solar.toLocalDate());

            assertThat(roundtrip).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("月份小于1抛异常")
        void testMonthTooSmall() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new LunarDate(2024, 0, 1, false))
                    .withMessageContaining("month");
        }

        @Test
        @DisplayName("月份大于12抛异常")
        void testMonthTooLarge() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new LunarDate(2024, 13, 1, false))
                    .withMessageContaining("month");
        }

        @Test
        @DisplayName("日期小于1抛异常")
        void testDayTooSmall() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new LunarDate(2024, 1, 0, false))
                    .withMessageContaining("day");
        }

        @Test
        @DisplayName("日期大于30抛异常")
        void testDayTooLarge() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new LunarDate(2024, 1, 31, false))
                    .withMessageContaining("day");
        }

        @Test
        @DisplayName("日期超过实际月天数抛异常")
        void testDayExceedsActualMonthDays() {
            // 2024年正月是29天的小月
            // Find a month with 29 days and try day 30
            // We know LunarData can tell us; just try a known small month
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LunarDate.of(2024, 1, 30));
        }

        @Test
        @DisplayName("非闰月标记为闰月抛异常")
        void testInvalidLeapMonth() {
            // 2024 has no leap month 1
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LunarDate.of(2024, 1, 1, true))
                    .withMessageContaining("no leap month");
        }
    }

    @Nested
    @DisplayName("getZodiac方法测试")
    class GetZodiacTests {

        @Test
        @DisplayName("2024年是龙年")
        void testDragonYear() {
            LunarDate date = new LunarDate(2024, 1, 1, false);
            Zodiac zodiac = date.getZodiac();

            assertThat(zodiac).isEqualTo(Zodiac.DRAGON);
        }

        @Test
        @DisplayName("2023年是兔年")
        void testRabbitYear() {
            LunarDate date = new LunarDate(2023, 1, 1, false);
            Zodiac zodiac = date.getZodiac();

            assertThat(zodiac).isEqualTo(Zodiac.RABBIT);
        }

        @Test
        @DisplayName("2020年是鼠年")
        void testRatYear() {
            LunarDate date = new LunarDate(2020, 1, 1, false);
            Zodiac zodiac = date.getZodiac();

            assertThat(zodiac).isEqualTo(Zodiac.RAT);
        }
    }

    @Nested
    @DisplayName("getYearGanZhi方法测试")
    class GetYearGanZhiTests {

        @Test
        @DisplayName("2024年是甲辰年")
        void test2024GanZhi() {
            LunarDate date = new LunarDate(2024, 1, 1, false);
            GanZhi ganZhi = date.getYearGanZhi();

            assertThat(ganZhi.getName()).isEqualTo("甲辰");
        }

        @Test
        @DisplayName("2023年是癸卯年")
        void test2023GanZhi() {
            LunarDate date = new LunarDate(2023, 1, 1, false);
            GanZhi ganZhi = date.getYearGanZhi();

            assertThat(ganZhi.getName()).isEqualTo("癸卯");
        }
    }

    @Nested
    @DisplayName("getMonthGanZhi方法测试")
    class GetMonthGanZhiTests {

        @Test
        @DisplayName("月干支不为空")
        void testMonthGanZhiNotNull() {
            LunarDate date = LunarDate.of(2024, 1, 1);
            GanZhi monthGZ = date.getMonthGanZhi();

            assertThat(monthGZ).isNotNull();
            assertThat(monthGZ.getName()).isNotEmpty();
        }

        @Test
        @DisplayName("不同月份干支不同")
        void testDifferentMonthsHaveDifferentGanZhi() {
            GanZhi gz1 = LunarDate.of(2024, 1, 1).getMonthGanZhi();
            GanZhi gz3 = LunarDate.of(2024, 3, 1).getMonthGanZhi();

            assertThat(gz1).isNotEqualTo(gz3);
        }
    }

    @Nested
    @DisplayName("getDayGanZhi方法测试")
    class GetDayGanZhiTests {

        @Test
        @DisplayName("日干支不为空")
        void testDayGanZhiNotNull() {
            LunarDate date = LunarDate.of(2024, 1, 1);
            GanZhi dayGZ = date.getDayGanZhi();

            assertThat(dayGZ).isNotNull();
            assertThat(dayGZ.getName()).isNotEmpty();
        }

        @Test
        @DisplayName("相邻日的干支连续")
        void testConsecutiveDaysGanZhi() {
            LunarDate d1 = LunarDate.of(2024, 1, 1);
            LunarDate d2 = LunarDate.of(2024, 1, 2);
            GanZhi gz1 = d1.getDayGanZhi();
            GanZhi gz2 = d2.getDayGanZhi();

            assertThat(gz1.next()).isEqualTo(gz2);
        }
    }

    @Nested
    @DisplayName("getMonthName方法测试")
    class GetMonthNameTests {

        @Test
        @DisplayName("正月")
        void testFirstMonth() {
            LunarDate date = new LunarDate(2024, 1, 1, false);
            assertThat(date.getMonthName()).isEqualTo("正月");
        }

        @Test
        @DisplayName("腊月")
        void testLastMonth() {
            LunarDate date = new LunarDate(2024, 12, 1, false);
            assertThat(date.getMonthName()).isEqualTo("腊月");
        }

        @Test
        @DisplayName("闰四月")
        void testLeapMonth() {
            LunarDate date = new LunarDate(2020, 4, 1, true);
            assertThat(date.getMonthName()).isEqualTo("闰四月");
        }

        @Test
        @DisplayName("二月")
        void testSecondMonth() {
            LunarDate date = new LunarDate(2024, 2, 1, false);
            assertThat(date.getMonthName()).isEqualTo("二月");
        }
    }

    @Nested
    @DisplayName("getDayName方法测试")
    class GetDayNameTests {

        @Test
        @DisplayName("初一")
        void testFirstDay() {
            LunarDate date = new LunarDate(2024, 1, 1, false);
            assertThat(date.getDayName()).isEqualTo("初一");
        }

        @Test
        @DisplayName("初十")
        void testTenthDay() {
            LunarDate date = new LunarDate(2024, 1, 10, false);
            assertThat(date.getDayName()).isEqualTo("初十");
        }

        @Test
        @DisplayName("十五")
        void testFifteenthDay() {
            LunarDate date = new LunarDate(2024, 1, 15, false);
            assertThat(date.getDayName()).isEqualTo("十五");
        }

        @Test
        @DisplayName("二十")
        void testTwentiethDay() {
            LunarDate date = new LunarDate(2024, 1, 20, false);
            assertThat(date.getDayName()).isEqualTo("二十");
        }

        @Test
        @DisplayName("三十 — 大月")
        void testThirtiethDay() {
            // Pick a month that actually has 30 days
            LunarDate date = new LunarDate(2024, 2, 30, false);
            assertThat(date.getDayName()).isEqualTo("三十");
        }
    }

    @Nested
    @DisplayName("toSolar方法测试")
    class ToSolarTests {

        @Test
        @DisplayName("春节转公历")
        void testSpringFestival() {
            LunarDate lunar = new LunarDate(2024, 1, 1, false);
            SolarDate solar = lunar.toSolar();

            assertThat(solar.year()).isEqualTo(2024);
            assertThat(solar.month()).isEqualTo(2);
            assertThat(solar.day()).isEqualTo(10);
        }

        @Test
        @DisplayName("中秋节转公历")
        void testMidAutumnFestival() {
            LunarDate lunar = new LunarDate(2024, 8, 15, false);
            SolarDate solar = lunar.toSolar();

            assertThat(solar.year()).isEqualTo(2024);
            assertThat(solar.month()).isEqualTo(9);
            assertThat(solar.day()).isEqualTo(17);
        }
    }

    @Nested
    @DisplayName("plusDays方法测试")
    class PlusDaysTests {

        @Test
        @DisplayName("加1天")
        void testPlusOneDay() {
            LunarDate date = LunarDate.of(2024, 1, 1);
            LunarDate next = date.plusDays(1);

            assertThat(next.day()).isEqualTo(2);
            assertThat(next.month()).isEqualTo(1);
        }

        @Test
        @DisplayName("加0天不变")
        void testPlusZeroDays() {
            LunarDate date = LunarDate.of(2024, 3, 15);
            LunarDate same = date.plusDays(0);

            assertThat(same).isEqualTo(date);
        }

        @Test
        @DisplayName("加天跨月")
        void testPlusDaysCrossMonth() {
            LunarDate date = LunarDate.of(2024, 1, 29);
            // 2024 month 1 has 29 days, so +1 should be month 2 day 1
            LunarDate next = date.plusDays(1);

            assertThat(next.month()).isEqualTo(2);
            assertThat(next.day()).isEqualTo(1);
        }

        @Test
        @DisplayName("加天跨年")
        void testPlusDaysCrossYear() {
            LunarDate date = LunarDate.of(2024, 12, 29);
            LunarDate next = date.plusDays(2);

            assertThat(next.year()).isEqualTo(2025);
        }

        @Test
        @DisplayName("加负天数等于减天")
        void testPlusNegativeDays() {
            LunarDate date = LunarDate.of(2024, 3, 15);
            LunarDate minus = date.plusDays(-5);
            LunarDate expected = date.minusDays(5);

            assertThat(minus).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("minusDays方法测试")
    class MinusDaysTests {

        @Test
        @DisplayName("减1天")
        void testMinusOneDay() {
            LunarDate date = LunarDate.of(2024, 1, 15);
            LunarDate prev = date.minusDays(1);

            assertThat(prev.day()).isEqualTo(14);
        }

        @Test
        @DisplayName("减天跨月")
        void testMinusDaysCrossMonth() {
            LunarDate date = LunarDate.of(2024, 2, 1);
            LunarDate prev = date.minusDays(1);

            assertThat(prev.month()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("daysUntil方法测试")
    class DaysUntilTests {

        @Test
        @DisplayName("同一天间隔为0")
        void testSameDay() {
            LunarDate date = LunarDate.of(2024, 5, 15);
            assertThat(date.daysUntil(date)).isZero();
        }

        @Test
        @DisplayName("正向间隔为正")
        void testPositiveInterval() {
            LunarDate d1 = LunarDate.of(2024, 1, 1);
            LunarDate d2 = LunarDate.of(2024, 1, 10);

            assertThat(d1.daysUntil(d2)).isEqualTo(9);
        }

        @Test
        @DisplayName("反向间隔为负")
        void testNegativeInterval() {
            LunarDate d1 = LunarDate.of(2024, 1, 10);
            LunarDate d2 = LunarDate.of(2024, 1, 1);

            assertThat(d1.daysUntil(d2)).isEqualTo(-9);
        }

        @Test
        @DisplayName("plusDays与daysUntil一致")
        void testConsistencyWithPlusDays() {
            LunarDate d1 = LunarDate.of(2024, 3, 1);
            LunarDate d2 = d1.plusDays(45);

            assertThat(d1.daysUntil(d2)).isEqualTo(45);
        }
    }

    @Nested
    @DisplayName("compareTo方法测试")
    class CompareToTests {

        @Test
        @DisplayName("相同日期compareTo为0")
        void testEqualDates() {
            LunarDate d1 = LunarDate.of(2024, 5, 15);
            LunarDate d2 = LunarDate.of(2024, 5, 15);

            assertThat(d1.compareTo(d2)).isZero();
        }

        @Test
        @DisplayName("较早日期compareTo小于0")
        void testEarlierDate() {
            LunarDate d1 = LunarDate.of(2024, 1, 1);
            LunarDate d2 = LunarDate.of(2024, 6, 1);

            assertThat(d1.compareTo(d2)).isNegative();
        }

        @Test
        @DisplayName("较晚日期compareTo大于0")
        void testLaterDate() {
            LunarDate d1 = LunarDate.of(2024, 6, 1);
            LunarDate d2 = LunarDate.of(2024, 1, 1);

            assertThat(d1.compareTo(d2)).isPositive();
        }

        @Test
        @DisplayName("跨年比较")
        void testCrossYearComparison() {
            LunarDate d1 = LunarDate.of(2023, 12, 29);
            LunarDate d2 = LunarDate.of(2024, 1, 1);

            assertThat(d1).isLessThan(d2);
        }

        @Test
        @DisplayName("闰月在常规月之后")
        void testLeapMonthAfterRegular() {
            // 2020 has leap month 4
            LunarDate regular = LunarDate.of(2020, 4, 15, false);
            LunarDate leap = LunarDate.of(2020, 4, 15, true);

            assertThat(regular).isLessThan(leap);
        }
    }

    @Nested
    @DisplayName("getFestivals方法测试")
    class GetFestivalsTests {

        @Test
        @DisplayName("春节")
        void testSpringFestival() {
            LunarDate date = new LunarDate(2024, 1, 1, false);
            List<Festival> festivals = date.getFestivals();

            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().anyMatch(f -> f.name().contains("春节"))).isTrue();
        }

        @Test
        @DisplayName("中秋节")
        void testMidAutumnFestival() {
            LunarDate date = new LunarDate(2024, 8, 15, false);
            List<Festival> festivals = date.getFestivals();

            assertThat(festivals).isNotEmpty();
            assertThat(festivals.stream().anyMatch(f -> f.name().contains("中秋"))).isTrue();
        }

        @Test
        @DisplayName("非节日日期")
        void testNonFestivalDate() {
            LunarDate date = new LunarDate(2024, 5, 10, false);
            List<Festival> festivals = date.getFestivals();

            assertThat(festivals).isEmpty();
        }
    }

    @Nested
    @DisplayName("format方法测试")
    class FormatTests {

        @Test
        @DisplayName("格式化非闰月日期")
        void testFormatNonLeap() {
            LunarDate date = new LunarDate(2024, 1, 15, false);
            String formatted = date.format();

            assertThat(formatted).contains("正月");
            assertThat(formatted).contains("十五");
        }

        @Test
        @DisplayName("格式化闰月日期")
        void testFormatLeap() {
            LunarDate date = new LunarDate(2020, 4, 15, true);
            String formatted = date.format();

            assertThat(formatted).contains("闰");
        }
    }

    @Nested
    @DisplayName("三参数构造函数测试")
    class ThreeArgConstructorTests {

        @Test
        @DisplayName("三参数构造函数创建非闰月日期")
        void testThreeArgConstructor() {
            LunarDate date = new LunarDate(2024, 1, 15);

            assertThat(date.year()).isEqualTo(2024);
            assertThat(date.month()).isEqualTo(1);
            assertThat(date.day()).isEqualTo(15);
            assertThat(date.isLeapMonth()).isFalse();
        }
    }

    @Nested
    @DisplayName("isFestival方法测试")
    class IsFestivalTests {

        @Test
        @DisplayName("春节是节日")
        void testSpringFestivalIsFestival() {
            LunarDate date = new LunarDate(2024, 1, 1, false);
            assertThat(date.isFestival()).isTrue();
        }

        @Test
        @DisplayName("非节日日期返回false")
        void testNonFestivalDate() {
            LunarDate date = new LunarDate(2024, 5, 10, false);
            assertThat(date.isFestival()).isFalse();
        }
    }

    @Nested
    @DisplayName("formatSimple方法测试")
    class FormatSimpleTests {

        @Test
        @DisplayName("格式化为简单字符串")
        void testFormatSimple() {
            LunarDate date = new LunarDate(2024, 1, 15, false);
            String formatted = date.formatSimple();

            assertThat(formatted).contains("2024");
            assertThat(formatted).contains("正月");
            assertThat(formatted).contains("十五");
        }
    }
}
