package cloud.opencode.base.lunar.internal;

import cloud.opencode.base.lunar.LunarDate;
import cloud.opencode.base.lunar.SolarDate;
import cloud.opencode.base.lunar.exception.DateOutOfRangeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * LunarCalculator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("LunarCalculator 测试")
class LunarCalculatorTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(LunarCalculator.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = LunarCalculator.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("solarToLunar方法测试")
    class SolarToLunarTests {

        @Test
        @DisplayName("基准日期转换")
        void testBaseDate() {
            // 1900-01-31 是农历 1900-01-01
            LocalDate solar = LocalDate.of(1900, 1, 31);
            LunarDate lunar = LunarCalculator.solarToLunar(solar);

            assertThat(lunar.year()).isEqualTo(1900);
            assertThat(lunar.month()).isEqualTo(1);
            assertThat(lunar.day()).isEqualTo(1);
            assertThat(lunar.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("春节转换")
        void testSpringFestival2024() {
            // 2024年春节是2月10日
            LocalDate solar = LocalDate.of(2024, 2, 10);
            LunarDate lunar = LunarCalculator.solarToLunar(solar);

            assertThat(lunar.year()).isEqualTo(2024);
            assertThat(lunar.month()).isEqualTo(1);
            assertThat(lunar.day()).isEqualTo(1);
        }

        @Test
        @DisplayName("中秋节转换")
        void testMidAutumnFestival2024() {
            // 2024年中秋节是9月17日
            LocalDate solar = LocalDate.of(2024, 9, 17);
            LunarDate lunar = LunarCalculator.solarToLunar(solar);

            assertThat(lunar.year()).isEqualTo(2024);
            assertThat(lunar.month()).isEqualTo(8);
            assertThat(lunar.day()).isEqualTo(15);
        }

        @Test
        @DisplayName("闰月日期转换")
        void testLeapMonthDate() {
            // 2020年有闰四月，选一个闰四月的日期
            LocalDate solar = LocalDate.of(2020, 5, 23);
            LunarDate lunar = LunarCalculator.solarToLunar(solar);

            // 根据实际情况可能是闰四月初一
            assertThat(lunar.year()).isEqualTo(2020);
        }

        @Test
        @DisplayName("年份过小抛出异常")
        void testYearTooSmall() {
            LocalDate solar = LocalDate.of(1899, 12, 31);
            assertThatThrownBy(() -> LunarCalculator.solarToLunar(solar))
                .isInstanceOf(DateOutOfRangeException.class);
        }

        @Test
        @DisplayName("年份过大抛出异常")
        void testYearTooLarge() {
            LocalDate solar = LocalDate.of(2101, 1, 1);
            assertThatThrownBy(() -> LunarCalculator.solarToLunar(solar))
                .isInstanceOf(DateOutOfRangeException.class);
        }

        @ParameterizedTest
        @CsvSource({
            "2020, 1, 25, 2020, 1, 1",
            "2021, 2, 12, 2021, 1, 1",
            "2022, 2, 1, 2022, 1, 1",
            "2023, 1, 22, 2023, 1, 1"
        })
        @DisplayName("多年春节转换")
        void testMultipleSpringFestivals(int solarYear, int solarMonth, int solarDay,
                                          int lunarYear, int lunarMonth, int lunarDay) {
            LocalDate solar = LocalDate.of(solarYear, solarMonth, solarDay);
            LunarDate lunar = LunarCalculator.solarToLunar(solar);

            assertThat(lunar.year()).isEqualTo(lunarYear);
            assertThat(lunar.month()).isEqualTo(lunarMonth);
            assertThat(lunar.day()).isEqualTo(lunarDay);
        }
    }

    @Nested
    @DisplayName("lunarToSolar方法测试")
    class LunarToSolarTests {

        @Test
        @DisplayName("基准日期转换")
        void testBaseDate() {
            LunarDate lunar = new LunarDate(1900, 1, 1, false);
            SolarDate solar = LunarCalculator.lunarToSolar(lunar);

            assertThat(solar.year()).isEqualTo(1900);
            assertThat(solar.month()).isEqualTo(1);
            assertThat(solar.day()).isEqualTo(31);
        }

        @Test
        @DisplayName("春节转换")
        void testSpringFestival2024() {
            LunarDate lunar = new LunarDate(2024, 1, 1, false);
            SolarDate solar = LunarCalculator.lunarToSolar(lunar);

            assertThat(solar.year()).isEqualTo(2024);
            assertThat(solar.month()).isEqualTo(2);
            assertThat(solar.day()).isEqualTo(10);
        }

        @Test
        @DisplayName("中秋节转换")
        void testMidAutumnFestival2024() {
            LunarDate lunar = new LunarDate(2024, 8, 15, false);
            SolarDate solar = LunarCalculator.lunarToSolar(lunar);

            assertThat(solar.year()).isEqualTo(2024);
            assertThat(solar.month()).isEqualTo(9);
            assertThat(solar.day()).isEqualTo(17);
        }

        @Test
        @DisplayName("闰月日期转换")
        void testLeapMonthDate() {
            // 2020年闰四月初一
            LunarDate lunar = new LunarDate(2020, 4, 1, true);
            SolarDate solar = LunarCalculator.lunarToSolar(lunar);

            assertThat(solar.year()).isEqualTo(2020);
            assertThat(solar.month()).isEqualTo(5);
        }

        @Test
        @DisplayName("年份过小抛出异常")
        void testYearTooSmall() {
            LunarDate lunar = new LunarDate(1899, 1, 1, false);
            assertThatThrownBy(() -> LunarCalculator.lunarToSolar(lunar))
                .isInstanceOf(DateOutOfRangeException.class);
        }

        @Test
        @DisplayName("年份过大抛出异常")
        void testYearTooLarge() {
            LunarDate lunar = new LunarDate(2101, 1, 1, false);
            assertThatThrownBy(() -> LunarCalculator.lunarToSolar(lunar))
                .isInstanceOf(DateOutOfRangeException.class);
        }
    }

    @Nested
    @DisplayName("往返转换测试")
    class RoundTripTests {

        @Test
        @DisplayName("公历转农历再转公历")
        void testSolarToLunarAndBack() {
            LocalDate original = LocalDate.of(2024, 6, 15);
            LunarDate lunar = LunarCalculator.solarToLunar(original);
            SolarDate solar = LunarCalculator.lunarToSolar(lunar);

            assertThat(solar.year()).isEqualTo(original.getYear());
            assertThat(solar.month()).isEqualTo(original.getMonthValue());
            assertThat(solar.day()).isEqualTo(original.getDayOfMonth());
        }

        @Test
        @DisplayName("农历转公历再转农历")
        void testLunarToSolarAndBack() {
            LunarDate original = new LunarDate(2024, 5, 10, false);
            SolarDate solar = LunarCalculator.lunarToSolar(original);
            LocalDate localDate = LocalDate.of(solar.year(), solar.month(), solar.day());
            LunarDate lunar = LunarCalculator.solarToLunar(localDate);

            assertThat(lunar.year()).isEqualTo(original.year());
            assertThat(lunar.month()).isEqualTo(original.month());
            assertThat(lunar.day()).isEqualTo(original.day());
            assertThat(lunar.isLeapMonth()).isEqualTo(original.isLeapMonth());
        }

        @ParameterizedTest
        @CsvSource({
            "2020, 1, 1",
            "2020, 6, 15",
            "2020, 12, 31",
            "2024, 3, 15",
            "2024, 8, 20"
        })
        @DisplayName("多日期往返转换")
        void testMultipleDatesRoundTrip(int year, int month, int day) {
            LocalDate original = LocalDate.of(year, month, day);
            LunarDate lunar = LunarCalculator.solarToLunar(original);
            SolarDate solar = LunarCalculator.lunarToSolar(lunar);

            assertThat(solar.year()).isEqualTo(year);
            assertThat(solar.month()).isEqualTo(month);
            assertThat(solar.day()).isEqualTo(day);
        }
    }

    @Nested
    @DisplayName("getMonthDays方法测试")
    class GetMonthDaysTests {

        @Test
        @DisplayName("非闰月天数")
        void testNonLeapMonthDays() {
            int days = LunarCalculator.getMonthDays(2024, 1, false);
            assertThat(days).isIn(29, 30);
        }

        @Test
        @DisplayName("闰月天数")
        void testLeapMonthDays() {
            // 2020年闰四月
            int days = LunarCalculator.getMonthDays(2020, 4, true);
            assertThat(days).isIn(29, 30);
        }

        @Test
        @DisplayName("非闰月年请求闰月返回0")
        void testNoLeapMonthReturns0() {
            // 2024年无闰月
            int days = LunarCalculator.getMonthDays(2024, 4, true);
            assertThat(days).isEqualTo(0);
        }

        @Test
        @DisplayName("请求错误的闰月返回0")
        void testWrongLeapMonthReturns0() {
            // 2020年闰四月，请求闰六月
            int days = LunarCalculator.getMonthDays(2020, 6, true);
            assertThat(days).isEqualTo(0);
        }
    }
}
