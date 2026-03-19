package cloud.opencode.base.lunar.zodiac;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.time.MonthDay;

import static org.assertj.core.api.Assertions.*;

/**
 * Constellation (星座) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("Constellation (星座) 测试")
class ConstellationTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含12个星座")
        void testTwelveConstellations() {
            assertThat(Constellation.values()).hasSize(12);
        }

        @Test
        @DisplayName("星座顺序正确")
        void testConstellationOrder() {
            Constellation[] constellations = Constellation.values();
            assertThat(constellations[0]).isEqualTo(Constellation.CAPRICORN);
            assertThat(constellations[1]).isEqualTo(Constellation.AQUARIUS);
            assertThat(constellations[2]).isEqualTo(Constellation.PISCES);
            assertThat(constellations[3]).isEqualTo(Constellation.ARIES);
            assertThat(constellations[4]).isEqualTo(Constellation.TAURUS);
            assertThat(constellations[5]).isEqualTo(Constellation.GEMINI);
            assertThat(constellations[6]).isEqualTo(Constellation.CANCER);
            assertThat(constellations[7]).isEqualTo(Constellation.LEO);
            assertThat(constellations[8]).isEqualTo(Constellation.VIRGO);
            assertThat(constellations[9]).isEqualTo(Constellation.LIBRA);
            assertThat(constellations[10]).isEqualTo(Constellation.SCORPIO);
            assertThat(constellations[11]).isEqualTo(Constellation.SAGITTARIUS);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("摩羯座")
        void testCapricorn() {
            assertThat(Constellation.CAPRICORN.getName()).isEqualTo("摩羯座");
        }

        @Test
        @DisplayName("水瓶座")
        void testAquarius() {
            assertThat(Constellation.AQUARIUS.getName()).isEqualTo("水瓶座");
        }

        @Test
        @DisplayName("白羊座")
        void testAries() {
            assertThat(Constellation.ARIES.getName()).isEqualTo("白羊座");
        }

        @Test
        @DisplayName("狮子座")
        void testLeo() {
            assertThat(Constellation.LEO.getName()).isEqualTo("狮子座");
        }

        @ParameterizedTest
        @EnumSource(Constellation.class)
        @DisplayName("所有星座名称不为空")
        void testAllNamesNotEmpty(Constellation constellation) {
            assertThat(constellation.getName()).isNotEmpty();
            assertThat(constellation.getName()).endsWith("座");
        }
    }

    @Nested
    @DisplayName("of(LocalDate)方法测试")
    class OfLocalDateTests {

        @Test
        @DisplayName("1月1日是摩羯座")
        void testJan1() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            assertThat(Constellation.of(date)).isEqualTo(Constellation.CAPRICORN);
        }

        @Test
        @DisplayName("3月21日是白羊座")
        void testMar21() {
            LocalDate date = LocalDate.of(2024, 3, 21);
            assertThat(Constellation.of(date)).isEqualTo(Constellation.ARIES);
        }

        @Test
        @DisplayName("7月1日是巨蟹座")
        void testJul1() {
            LocalDate date = LocalDate.of(2024, 7, 1);
            assertThat(Constellation.of(date)).isEqualTo(Constellation.CANCER);
        }

        @Test
        @DisplayName("12月31日是摩羯座")
        void testDec31() {
            LocalDate date = LocalDate.of(2024, 12, 31);
            assertThat(Constellation.of(date)).isEqualTo(Constellation.CAPRICORN);
        }
    }

    @Nested
    @DisplayName("of(month, day)方法测试")
    class OfMonthDayTests {

        @ParameterizedTest
        @CsvSource({
            "1, 1, CAPRICORN",
            "1, 20, AQUARIUS",
            "2, 19, PISCES",
            "3, 21, ARIES",
            "4, 20, TAURUS",
            "5, 21, GEMINI",
            "6, 22, CANCER",
            "7, 23, LEO",
            "8, 23, VIRGO",
            "9, 23, LIBRA",
            "10, 24, SCORPIO",
            "11, 23, SAGITTARIUS",
            "12, 22, CAPRICORN"
        })
        @DisplayName("月日对应正确星座")
        void testMonthDayMapping(int month, int day, Constellation expected) {
            assertThat(Constellation.of(month, day)).isEqualTo(expected);
        }

        @Test
        @DisplayName("边界日期测试")
        void testBoundaryDates() {
            // 摩羯座: 12/22 - 1/19
            assertThat(Constellation.of(12, 22)).isEqualTo(Constellation.CAPRICORN);
            assertThat(Constellation.of(1, 19)).isEqualTo(Constellation.CAPRICORN);

            // 水瓶座: 1/20 - 2/18
            assertThat(Constellation.of(1, 20)).isEqualTo(Constellation.AQUARIUS);
            assertThat(Constellation.of(2, 18)).isEqualTo(Constellation.AQUARIUS);
        }
    }

    @Nested
    @DisplayName("contains方法测试")
    class ContainsTests {

        @Test
        @DisplayName("摩羯座包含1月1日")
        void testCapricornContainsJan1() {
            assertThat(Constellation.CAPRICORN.contains(MonthDay.of(1, 1))).isTrue();
        }

        @Test
        @DisplayName("摩羯座不包含2月1日")
        void testCapricornNotContainsFeb1() {
            assertThat(Constellation.CAPRICORN.contains(MonthDay.of(2, 1))).isFalse();
        }

        @Test
        @DisplayName("白羊座包含4月1日")
        void testAriesContainsApr1() {
            assertThat(Constellation.ARIES.contains(MonthDay.of(4, 1))).isTrue();
        }
    }

    @Nested
    @DisplayName("isDate方法测试")
    class IsDateTests {

        @Test
        @DisplayName("检查日期是否属于星座")
        void testIsDate() {
            LocalDate date = LocalDate.of(2024, 4, 15);
            assertThat(Constellation.ARIES.isDate(date)).isTrue();
            assertThat(Constellation.TAURUS.isDate(date)).isFalse();
        }
    }

    @Nested
    @DisplayName("getStartDate和getEndDate方法测试")
    class DateRangeTests {

        @Test
        @DisplayName("白羊座日期范围")
        void testAriesRange() {
            assertThat(Constellation.ARIES.getStartDate()).isEqualTo(MonthDay.of(3, 21));
            assertThat(Constellation.ARIES.getEndDate()).isEqualTo(MonthDay.of(4, 19));
        }

        @Test
        @DisplayName("摩羯座跨年日期范围")
        void testCapricornRange() {
            assertThat(Constellation.CAPRICORN.getStartDate()).isEqualTo(MonthDay.of(12, 22));
            assertThat(Constellation.CAPRICORN.getEndDate()).isEqualTo(MonthDay.of(1, 19));
        }

        @ParameterizedTest
        @EnumSource(Constellation.class)
        @DisplayName("所有星座都有日期范围")
        void testAllHaveDateRange(Constellation constellation) {
            assertThat(constellation.getStartDate()).isNotNull();
            assertThat(constellation.getEndDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("循环测试")
    class CycleTests {

        @Test
        @DisplayName("一年中每天都有对应星座")
        void testEveryDayHasConstellation() {
            for (int month = 1; month <= 12; month++) {
                int daysInMonth = LocalDate.of(2024, month, 1).lengthOfMonth();
                for (int day = 1; day <= daysInMonth; day++) {
                    Constellation constellation = Constellation.of(month, day);
                    assertThat(constellation).isNotNull();
                }
            }
        }
    }
}
