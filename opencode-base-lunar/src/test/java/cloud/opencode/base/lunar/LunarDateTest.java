package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.calendar.Festival;
import cloud.opencode.base.lunar.ganzhi.GanZhi;
import cloud.opencode.base.lunar.zodiac.Zodiac;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
            LunarDate date3 = new LunarDate(2024, 1, 15, true);

            assertThat(date1).isEqualTo(date2);
            assertThat(date1).isNotEqualTo(date3);
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
        @DisplayName("三十")
        void testThirtiethDay() {
            LunarDate date = new LunarDate(2024, 1, 30, false);
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
