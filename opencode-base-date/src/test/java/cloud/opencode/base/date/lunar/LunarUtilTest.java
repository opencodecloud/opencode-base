package cloud.opencode.base.date.lunar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * LunarUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("LunarUtil 测试")
class LunarUtilTest {

    @Nested
    @DisplayName("阳历转农历测试")
    class ToLunarTests {

        @Test
        @DisplayName("toLunar() 基本转换")
        void testToLunarBasic() {
            // 2024年2月10日是农历正月初一（Spring Festival）
            Lunar lunar = LunarUtil.toLunar(LocalDate.of(2024, 2, 10));
            assertThat(lunar.getYear()).isEqualTo(2024);
            assertThat(lunar.getMonth()).isEqualTo(1);
            assertThat(lunar.getDay()).isEqualTo(1);
            assertThat(lunar.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("toLunar() 跨年转换")
        void testToLunarCrossYear() {
            // 2024年1月1日是农历2023年腊月
            Lunar lunar = LunarUtil.toLunar(LocalDate.of(2024, 1, 1));
            assertThat(lunar.getYear()).isEqualTo(2023);
            assertThat(lunar.getMonth()).isEqualTo(11);
        }

        @Test
        @DisplayName("toLunar() null抛出异常")
        void testToLunarNull() {
            assertThatThrownBy(() -> LunarUtil.toLunar(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("toLunar() 早于1900年抛出异常")
        void testToLunarBeforeBase() {
            assertThatThrownBy(() -> LunarUtil.toLunar(LocalDate.of(1899, 1, 1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Date must be after 1900-01-31");
        }
    }

    @Nested
    @DisplayName("农历转阳历测试")
    class ToSolarTests {

        @Test
        @DisplayName("toSolar() 基本转换")
        void testToSolarBasic() {
            // 农历2024年正月初一是2024年2月10日
            LocalDate solar = LunarUtil.toSolar(Lunar.of(2024, 1, 1));
            assertThat(solar).isEqualTo(LocalDate.of(2024, 2, 10));
        }

        @Test
        @DisplayName("toSolar() 双向转换一致性")
        void testToSolarRoundTrip() {
            LocalDate original = LocalDate.of(2024, 6, 15);
            Lunar lunar = LunarUtil.toLunar(original);
            LocalDate converted = LunarUtil.toSolar(lunar);
            assertThat(converted).isEqualTo(original);
        }

        @Test
        @DisplayName("toSolar() null抛出异常")
        void testToSolarNull() {
            assertThatThrownBy(() -> LunarUtil.toSolar(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("toSolar() 闰月转换")
        void testToSolarLeapMonth() {
            // 测试闰月转换（如果某年有闰四月）
            // 使用一个已知有闰月的年份进行验证
            int year = 2020; // 2020年有闰四月
            int leapMonth = LunarUtil.getLeapMonth(year);
            if (leapMonth > 0) {
                Lunar leapLunar = Lunar.of(year, leapMonth, 15, true);
                LocalDate solar = LunarUtil.toSolar(leapLunar);
                assertThat(solar).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("农历信息方法测试")
    class LunarInfoTests {

        @Test
        @DisplayName("getLeapMonth() 获取闰月")
        void testGetLeapMonth() {
            // 2020年有闰四月
            assertThat(LunarUtil.getLeapMonth(2020)).isEqualTo(4);
            // 2024年无闰月（或有特定闰月）
            int leapMonth2024 = LunarUtil.getLeapMonth(2024);
            assertThat(leapMonth2024).isBetween(0, 12);
        }

        @Test
        @DisplayName("hasLeapMonth() 检查是否有闰月")
        void testHasLeapMonth() {
            // 2020年有闰月
            assertThat(LunarUtil.hasLeapMonth(2020)).isTrue();
        }

        @Test
        @DisplayName("getLunarYearDays() 获取农历年总天数")
        void testGetLunarYearDays() {
            int days = LunarUtil.getLunarYearDays(2024);
            // 农历年一般在353-385天之间
            assertThat(days).isBetween(353, 385);
        }

        @Test
        @DisplayName("getLunarMonthDays() 获取农历月天数")
        void testGetLunarMonthDays() {
            int days = LunarUtil.getLunarMonthDays(2024, 1);
            // 农历月为29或30天
            assertThat(days).isBetween(29, 30);
        }

        @Test
        @DisplayName("getLeapMonthDays() 获取闰月天数")
        void testGetLeapMonthDays() {
            // 2020年有闰四月
            int days = LunarUtil.getLeapMonthDays(2020);
            assertThat(days).isBetween(29, 30);

            // 无闰月的年份返回0
            if (LunarUtil.getLeapMonth(2019) == 0) {
                assertThat(LunarUtil.getLeapMonthDays(2019)).isEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("生肖方法测试")
    class ZodiacTests {

        @Test
        @DisplayName("getZodiac(year) 按年份获取生肖")
        void testGetZodiacByYear() {
            assertThat(LunarUtil.getZodiac(2024)).isEqualTo("龙");
            assertThat(LunarUtil.getZodiac(2023)).isEqualTo("兔");
            assertThat(LunarUtil.getZodiac(2020)).isEqualTo("鼠");
        }

        @Test
        @DisplayName("getZodiac(date) 按日期获取生肖")
        void testGetZodiacByDate() {
            // 2024年2月10日是龙年
            assertThat(LunarUtil.getZodiac(LocalDate.of(2024, 2, 10))).isEqualTo("龙");
            // 2024年2月9日还是兔年（农历2023年）
            assertThat(LunarUtil.getZodiac(LocalDate.of(2024, 2, 9))).isEqualTo("兔");
        }

        @Test
        @DisplayName("getStemBranchYear() 获取干支纪年")
        void testGetStemBranchYear() {
            assertThat(LunarUtil.getStemBranchYear(2024)).isEqualTo("甲辰");
            assertThat(LunarUtil.getStemBranchYear(2023)).isEqualTo("癸卯");
        }
    }

    @Nested
    @DisplayName("today方法测试")
    class TodayTests {

        @Test
        @DisplayName("today() 获取当前农历日期")
        void testToday() {
            Lunar today = LunarUtil.today();
            assertThat(today).isNotNull();
            assertThat(today.getYear()).isGreaterThanOrEqualTo(1900);
            assertThat(today.getMonth()).isBetween(1, 12);
            assertThat(today.getDay()).isBetween(1, 30);
        }

        @Test
        @DisplayName("today() 与toLunar(LocalDate.now())一致")
        void testTodayConsistency() {
            Lunar today1 = LunarUtil.today();
            Lunar today2 = LunarUtil.toLunar(LocalDate.now());
            assertThat(today1).isEqualTo(today2);
        }
    }

    @Nested
    @DisplayName("边界值测试")
    class BoundaryTests {

        @Test
        @DisplayName("1900年转换")
        void testYear1900() {
            // 1900年1月31日是农历1900年正月初一
            Lunar lunar = LunarUtil.toLunar(LocalDate.of(1900, 1, 31));
            assertThat(lunar.getYear()).isEqualTo(1900);
            assertThat(lunar.getMonth()).isEqualTo(1);
            assertThat(lunar.getDay()).isEqualTo(1);
        }

        @Test
        @DisplayName("各月份转换测试")
        void testMonthlyConversion() {
            // 测试2024年每个月中旬的转换
            for (int month = 1; month <= 12; month++) {
                LocalDate date = LocalDate.of(2024, month, 15);
                Lunar lunar = LunarUtil.toLunar(date);
                LocalDate converted = LunarUtil.toSolar(lunar);
                assertThat(converted).isEqualTo(date);
            }
        }

        @Test
        @DisplayName("闰月年份转换测试")
        void testLeapYearConversion() {
            // 2020年有闰四月，测试闰月附近的转换
            for (int day = 1; day <= 30; day++) {
                LocalDate date = LocalDate.of(2020, 6, day);
                Lunar lunar = LunarUtil.toLunar(date);
                LocalDate converted = LunarUtil.toSolar(lunar);
                assertThat(converted).isEqualTo(date);
            }
        }
    }
}
