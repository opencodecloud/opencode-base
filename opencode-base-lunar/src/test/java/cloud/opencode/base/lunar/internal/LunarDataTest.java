package cloud.opencode.base.lunar.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * LunarData 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("LunarData 测试")
class LunarDataTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(LunarData.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = LunarData.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("BASE_YEAR为1900")
        void testBaseYear() {
            assertThat(LunarData.BASE_YEAR).isEqualTo(1900);
        }

        @Test
        @DisplayName("MIN_YEAR为1900")
        void testMinYear() {
            assertThat(LunarData.MIN_YEAR).isEqualTo(1900);
        }

        @Test
        @DisplayName("MAX_YEAR为2100")
        void testMaxYear() {
            assertThat(LunarData.MAX_YEAR).isEqualTo(2100);
        }
    }

    @Nested
    @DisplayName("isSupported方法测试")
    class IsSupportedTests {

        @Test
        @DisplayName("最小年份支持")
        void testMinYearSupported() {
            assertThat(LunarData.isSupported(1900)).isTrue();
        }

        @Test
        @DisplayName("最大年份支持")
        void testMaxYearSupported() {
            assertThat(LunarData.isSupported(2100)).isTrue();
        }

        @Test
        @DisplayName("中间年份支持")
        void testMiddleYearSupported() {
            assertThat(LunarData.isSupported(2000)).isTrue();
            assertThat(LunarData.isSupported(2024)).isTrue();
        }

        @Test
        @DisplayName("小于最小年份不支持")
        void testBelowMinNotSupported() {
            assertThat(LunarData.isSupported(1899)).isFalse();
            assertThat(LunarData.isSupported(1800)).isFalse();
        }

        @Test
        @DisplayName("大于最大年份不支持")
        void testAboveMaxNotSupported() {
            assertThat(LunarData.isSupported(2101)).isFalse();
            assertThat(LunarData.isSupported(2200)).isFalse();
        }
    }

    @Nested
    @DisplayName("getLeapMonth方法测试")
    class GetLeapMonthTests {

        @Test
        @DisplayName("2020年闰四月")
        void testLeapMonth2020() {
            assertThat(LunarData.getLeapMonth(2020)).isEqualTo(4);
        }

        @Test
        @DisplayName("2023年闰二月")
        void testLeapMonth2023() {
            assertThat(LunarData.getLeapMonth(2023)).isEqualTo(2);
        }

        @Test
        @DisplayName("2024年无闰月")
        void testNoLeapMonth2024() {
            assertThat(LunarData.getLeapMonth(2024)).isEqualTo(0);
        }

        @Test
        @DisplayName("2025年闰六月")
        void testLeapMonth2025() {
            assertThat(LunarData.getLeapMonth(2025)).isEqualTo(6);
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 1950, 2000, 2050, 2100})
        @DisplayName("所有年份闰月在0-12之间")
        void testLeapMonthRange(int year) {
            int leapMonth = LunarData.getLeapMonth(year);
            assertThat(leapMonth).isBetween(0, 12);
        }
    }

    @Nested
    @DisplayName("getLeapMonthDays方法测试")
    class GetLeapMonthDaysTests {

        @Test
        @DisplayName("有闰月时返回29或30")
        void testLeapMonthDaysWithLeap() {
            int days = LunarData.getLeapMonthDays(2020);
            assertThat(days).isIn(29, 30);
        }

        @Test
        @DisplayName("无闰月时返回0")
        void testLeapMonthDaysWithoutLeap() {
            assertThat(LunarData.getLeapMonthDays(2024)).isEqualTo(0);
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 1950, 2000, 2050, 2100})
        @DisplayName("所有年份闰月天数为0、29或30")
        void testLeapMonthDaysValues(int year) {
            int days = LunarData.getLeapMonthDays(year);
            assertThat(days).isIn(0, 29, 30);
        }
    }

    @Nested
    @DisplayName("getMonthDays方法测试")
    class GetMonthDaysTests {

        @Test
        @DisplayName("月份天数为29或30")
        void testMonthDaysValue() {
            for (int month = 1; month <= 12; month++) {
                int days = LunarData.getMonthDays(2024, month);
                assertThat(days).isIn(29, 30);
            }
        }

        @Test
        @DisplayName("不同年份同月天数可能不同")
        void testMonthDaysDiffer() {
            int days2020 = LunarData.getMonthDays(2020, 1);
            int days2021 = LunarData.getMonthDays(2021, 1);
            // 天数可能相同也可能不同，但都应该是29或30
            assertThat(days2020).isIn(29, 30);
            assertThat(days2021).isIn(29, 30);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
        @DisplayName("所有月份天数为29或30")
        void testAllMonthDays(int month) {
            int days = LunarData.getMonthDays(2024, month);
            assertThat(days).isIn(29, 30);
        }
    }

    @Nested
    @DisplayName("getYearDays方法测试")
    class GetYearDaysTests {

        @Test
        @DisplayName("年天数在合理范围内")
        void testYearDaysRange() {
            int days2024 = LunarData.getYearDays(2024);
            // 农历年354-385天
            assertThat(days2024).isBetween(354, 385);
        }

        @Test
        @DisplayName("无闰月年约354天")
        void testYearDaysWithoutLeap() {
            // 2024年无闰月
            int days = LunarData.getYearDays(2024);
            assertThat(days).isBetween(354, 355);
        }

        @Test
        @DisplayName("有闰月年约384天")
        void testYearDaysWithLeap() {
            // 2020年有闰月
            int days = LunarData.getYearDays(2020);
            assertThat(days).isBetween(383, 385);
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 1950, 2000, 2050, 2100})
        @DisplayName("所有年份天数在354-385之间")
        void testAllYearDays(int year) {
            int days = LunarData.getYearDays(year);
            assertThat(days).isBetween(354, 385);
        }
    }

    @Nested
    @DisplayName("getDaysToYearStart方法测试")
    class GetDaysToYearStartTests {

        @Test
        @DisplayName("1900年起始偏移为0")
        void testBaseYearOffset() {
            assertThat(LunarData.getDaysToYearStart(1900)).isEqualTo(0);
        }

        @Test
        @DisplayName("偏移随年份递增")
        void testOffsetIncreasing() {
            int offset1900 = LunarData.getDaysToYearStart(1900);
            int offset1901 = LunarData.getDaysToYearStart(1901);
            int offset2000 = LunarData.getDaysToYearStart(2000);

            assertThat(offset1901).isGreaterThan(offset1900);
            assertThat(offset2000).isGreaterThan(offset1901);
        }

        @Test
        @DisplayName("偏移等于之前所有年份天数之和")
        void testOffsetEqualsSum() {
            int offset1901 = LunarData.getDaysToYearStart(1901);
            int days1900 = LunarData.getYearDays(1900);

            assertThat(offset1901).isEqualTo(days1900);
        }
    }
}
