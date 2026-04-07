package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.zodiac.Constellation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * SolarDate 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("SolarDate 测试")
class SolarDateTest {

    @Nested
    @DisplayName("Record基本测试")
    class RecordBasicTests {

        @Test
        @DisplayName("创建日期")
        void testCreateDate() {
            SolarDate date = new SolarDate(2024, 6, 15);

            assertThat(date.year()).isEqualTo(2024);
            assertThat(date.month()).isEqualTo(6);
            assertThat(date.day()).isEqualTo(15);
        }

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            SolarDate date1 = new SolarDate(2024, 6, 15);
            SolarDate date2 = new SolarDate(2024, 6, 15);
            SolarDate date3 = new SolarDate(2024, 6, 16);

            assertThat(date1).isEqualTo(date2);
            assertThat(date1).isNotEqualTo(date3);
        }

        @Test
        @DisplayName("hashCode一致")
        void testHashCode() {
            SolarDate date1 = new SolarDate(2024, 6, 15);
            SolarDate date2 = new SolarDate(2024, 6, 15);

            assertThat(date1.hashCode()).isEqualTo(date2.hashCode());
        }

        @Test
        @DisplayName("toString包含信息")
        void testToString() {
            SolarDate date = new SolarDate(2024, 6, 15);
            String str = date.toString();

            assertThat(str).contains("2024");
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfFactoryTests {

        @Test
        @DisplayName("of(year, month, day)创建日期")
        void testOfYearMonthDay() {
            SolarDate date = SolarDate.of(2024, 6, 15);

            assertThat(date.year()).isEqualTo(2024);
            assertThat(date.month()).isEqualTo(6);
            assertThat(date.day()).isEqualTo(15);
        }

        @Test
        @DisplayName("of(year, month, day)等价于构造函数")
        void testOfEqualsConstructor() {
            SolarDate d1 = SolarDate.of(2024, 3, 20);
            SolarDate d2 = new SolarDate(2024, 3, 20);

            assertThat(d1).isEqualTo(d2);
        }

        @Test
        @DisplayName("of从LocalDate创建")
        void testOfLocalDate() {
            LocalDate localDate = LocalDate.of(2024, 6, 15);
            SolarDate date = SolarDate.of(localDate);

            assertThat(date.year()).isEqualTo(2024);
            assertThat(date.month()).isEqualTo(6);
            assertThat(date.day()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("today方法测试")
    class TodayTests {

        @Test
        @DisplayName("today返回今天日期")
        void testToday() {
            SolarDate today = SolarDate.today();
            LocalDate now = LocalDate.now();

            assertThat(today.year()).isEqualTo(now.getYear());
            assertThat(today.month()).isEqualTo(now.getMonthValue());
            assertThat(today.day()).isEqualTo(now.getDayOfMonth());
        }
    }

    @Nested
    @DisplayName("toLocalDate方法测试")
    class ToLocalDateTests {

        @Test
        @DisplayName("转换为LocalDate")
        void testToLocalDate() {
            SolarDate date = new SolarDate(2024, 6, 15);
            LocalDate localDate = date.toLocalDate();

            assertThat(localDate.getYear()).isEqualTo(2024);
            assertThat(localDate.getMonthValue()).isEqualTo(6);
            assertThat(localDate.getDayOfMonth()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("toLunar方法测试")
    class ToLunarTests {

        @Test
        @DisplayName("春节转农历")
        void testSpringFestival() {
            SolarDate solar = new SolarDate(2024, 2, 10);
            LunarDate lunar = solar.toLunar();

            assertThat(lunar.year()).isEqualTo(2024);
            assertThat(lunar.month()).isEqualTo(1);
            assertThat(lunar.day()).isEqualTo(1);
        }

        @Test
        @DisplayName("中秋节转农历")
        void testMidAutumnFestival() {
            SolarDate solar = new SolarDate(2024, 9, 17);
            LunarDate lunar = solar.toLunar();

            assertThat(lunar.year()).isEqualTo(2024);
            assertThat(lunar.month()).isEqualTo(8);
            assertThat(lunar.day()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("getConstellation方法测试")
    class GetConstellationTests {

        @Test
        @DisplayName("白羊座")
        void testAries() {
            SolarDate date = new SolarDate(2024, 4, 1);
            Constellation constellation = date.getConstellation();

            assertThat(constellation).isEqualTo(Constellation.ARIES);
        }

        @Test
        @DisplayName("金牛座")
        void testTaurus() {
            SolarDate date = new SolarDate(2024, 5, 1);
            Constellation constellation = date.getConstellation();

            assertThat(constellation).isEqualTo(Constellation.TAURUS);
        }

        @Test
        @DisplayName("巨蟹座")
        void testCancer() {
            SolarDate date = new SolarDate(2024, 7, 1);
            Constellation constellation = date.getConstellation();

            assertThat(constellation).isEqualTo(Constellation.CANCER);
        }

        @Test
        @DisplayName("狮子座")
        void testLeo() {
            SolarDate date = new SolarDate(2024, 8, 1);
            Constellation constellation = date.getConstellation();

            assertThat(constellation).isEqualTo(Constellation.LEO);
        }
    }

    @Nested
    @DisplayName("isLeapYear方法测试")
    class IsLeapYearTests {

        @Test
        @DisplayName("2024是闰年")
        void test2024LeapYear() {
            SolarDate date = new SolarDate(2024, 1, 1);
            assertThat(date.isLeapYear()).isTrue();
        }

        @Test
        @DisplayName("2023不是闰年")
        void test2023NotLeapYear() {
            SolarDate date = new SolarDate(2023, 1, 1);
            assertThat(date.isLeapYear()).isFalse();
        }

        @Test
        @DisplayName("2000是闰年")
        void test2000LeapYear() {
            SolarDate date = new SolarDate(2000, 1, 1);
            assertThat(date.isLeapYear()).isTrue();
        }

        @Test
        @DisplayName("1900不是闰年")
        void test1900NotLeapYear() {
            SolarDate date = new SolarDate(1900, 1, 1);
            assertThat(date.isLeapYear()).isFalse();
        }
    }

    @Nested
    @DisplayName("getDayOfWeek方法测试")
    class GetDayOfWeekTests {

        @Test
        @DisplayName("2024年1月1日是星期一(1)")
        void testNewYear2024() {
            SolarDate date = new SolarDate(2024, 1, 1);
            assertThat(date.getDayOfWeek()).isEqualTo(1); // 1=Monday
        }

        @Test
        @DisplayName("2024年6月15日是星期六(6)")
        void testJune15_2024() {
            SolarDate date = new SolarDate(2024, 6, 15);
            assertThat(date.getDayOfWeek()).isEqualTo(6); // 6=Saturday
        }

        @Test
        @DisplayName("getDayOfWeek返回1-7")
        void testDayOfWeekRange() {
            for (int i = 0; i < 7; i++) {
                SolarDate date = new SolarDate(2024, 1, 1).plusDays(i);
                assertThat(date.getDayOfWeek()).isBetween(1, 7);
            }
        }
    }

    @Nested
    @DisplayName("plusDays方法测试")
    class PlusDaysTests {

        @Test
        @DisplayName("加1天")
        void testPlusOneDay() {
            SolarDate date = new SolarDate(2024, 6, 15);
            SolarDate next = date.plusDays(1);

            assertThat(next.day()).isEqualTo(16);
        }

        @Test
        @DisplayName("跨月")
        void testPlusDaysCrossMonth() {
            SolarDate date = new SolarDate(2024, 6, 30);
            SolarDate next = date.plusDays(1);

            assertThat(next.month()).isEqualTo(7);
            assertThat(next.day()).isEqualTo(1);
        }

        @Test
        @DisplayName("跨年")
        void testPlusDaysCrossYear() {
            SolarDate date = new SolarDate(2024, 12, 31);
            SolarDate next = date.plusDays(1);

            assertThat(next.year()).isEqualTo(2025);
            assertThat(next.month()).isEqualTo(1);
            assertThat(next.day()).isEqualTo(1);
        }

        @Test
        @DisplayName("加多天")
        void testPlusMultipleDays() {
            SolarDate date = new SolarDate(2024, 1, 1);
            SolarDate next = date.plusDays(100);

            assertThat(next).isNotNull();
        }
    }

    @Nested
    @DisplayName("minusDays方法测试")
    class MinusDaysTests {

        @Test
        @DisplayName("减1天")
        void testMinusOneDay() {
            SolarDate date = new SolarDate(2024, 6, 15);
            SolarDate prev = date.minusDays(1);

            assertThat(prev.day()).isEqualTo(14);
        }

        @Test
        @DisplayName("跨月")
        void testMinusDaysCrossMonth() {
            SolarDate date = new SolarDate(2024, 7, 1);
            SolarDate prev = date.minusDays(1);

            assertThat(prev.month()).isEqualTo(6);
            assertThat(prev.day()).isEqualTo(30);
        }

        @Test
        @DisplayName("跨年")
        void testMinusDaysCrossYear() {
            SolarDate date = new SolarDate(2024, 1, 1);
            SolarDate prev = date.minusDays(1);

            assertThat(prev.year()).isEqualTo(2023);
            assertThat(prev.month()).isEqualTo(12);
            assertThat(prev.day()).isEqualTo(31);
        }
    }

    @Nested
    @DisplayName("getDayOfYear方法测试")
    class GetDayOfYearTests {

        @Test
        @DisplayName("1月1日是第1天")
        void testFirstDay() {
            SolarDate date = new SolarDate(2024, 1, 1);
            assertThat(date.getDayOfYear()).isEqualTo(1);
        }

        @Test
        @DisplayName("12月31日是闰年第366天")
        void testLastDayLeapYear() {
            SolarDate date = new SolarDate(2024, 12, 31);
            assertThat(date.getDayOfYear()).isEqualTo(366);
        }

        @Test
        @DisplayName("12月31日是非闰年第365天")
        void testLastDayNonLeapYear() {
            SolarDate date = new SolarDate(2023, 12, 31);
            assertThat(date.getDayOfYear()).isEqualTo(365);
        }
    }

    @Nested
    @DisplayName("格式化方法测试")
    class FormatTests {

        @Test
        @DisplayName("format返回ISO格式")
        void testFormat() {
            SolarDate date = new SolarDate(2024, 6, 15);
            assertThat(date.format()).isEqualTo("2024-06-15");
        }

        @Test
        @DisplayName("formatChinese返回中文格式")
        void testFormatChinese() {
            SolarDate date = new SolarDate(2024, 6, 15);
            assertThat(date.formatChinese()).isEqualTo("2024年6月15日");
        }
    }

    @Nested
    @DisplayName("getDayOfWeekName方法测试")
    class GetDayOfWeekNameTests {

        @Test
        @DisplayName("返回中文星期名")
        void testDayOfWeekName() {
            SolarDate date = new SolarDate(2024, 1, 1); // Monday
            assertThat(date.getDayOfWeekName()).isEqualTo("周一");
        }
    }
}
