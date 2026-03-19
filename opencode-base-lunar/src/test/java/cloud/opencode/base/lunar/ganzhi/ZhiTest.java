package cloud.opencode.base.lunar.ganzhi;

import cloud.opencode.base.lunar.zodiac.Zodiac;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Zhi (地支) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("Zhi (地支) 测试")
class ZhiTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含12个地支")
        void testTwelveZhis() {
            assertThat(Zhi.values()).hasSize(12);
        }

        @Test
        @DisplayName("地支顺序正确")
        void testZhiOrder() {
            Zhi[] zhis = Zhi.values();
            assertThat(zhis[0]).isEqualTo(Zhi.ZI);
            assertThat(zhis[1]).isEqualTo(Zhi.CHOU);
            assertThat(zhis[2]).isEqualTo(Zhi.YIN);
            assertThat(zhis[3]).isEqualTo(Zhi.MAO);
            assertThat(zhis[4]).isEqualTo(Zhi.CHEN);
            assertThat(zhis[5]).isEqualTo(Zhi.SI);
            assertThat(zhis[6]).isEqualTo(Zhi.WU);
            assertThat(zhis[7]).isEqualTo(Zhi.WEI);
            assertThat(zhis[8]).isEqualTo(Zhi.SHEN);
            assertThat(zhis[9]).isEqualTo(Zhi.YOU);
            assertThat(zhis[10]).isEqualTo(Zhi.XU);
            assertThat(zhis[11]).isEqualTo(Zhi.HAI);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("子")
        void testZi() {
            assertThat(Zhi.ZI.getName()).isEqualTo("子");
        }

        @Test
        @DisplayName("丑")
        void testChou() {
            assertThat(Zhi.CHOU.getName()).isEqualTo("丑");
        }

        @Test
        @DisplayName("寅")
        void testYin() {
            assertThat(Zhi.YIN.getName()).isEqualTo("寅");
        }

        @Test
        @DisplayName("卯")
        void testMao() {
            assertThat(Zhi.MAO.getName()).isEqualTo("卯");
        }

        @Test
        @DisplayName("辰")
        void testChen() {
            assertThat(Zhi.CHEN.getName()).isEqualTo("辰");
        }

        @Test
        @DisplayName("巳")
        void testSi() {
            assertThat(Zhi.SI.getName()).isEqualTo("巳");
        }

        @Test
        @DisplayName("午")
        void testWu() {
            assertThat(Zhi.WU.getName()).isEqualTo("午");
        }

        @Test
        @DisplayName("未")
        void testWei() {
            assertThat(Zhi.WEI.getName()).isEqualTo("未");
        }

        @Test
        @DisplayName("申")
        void testShen() {
            assertThat(Zhi.SHEN.getName()).isEqualTo("申");
        }

        @Test
        @DisplayName("酉")
        void testYou() {
            assertThat(Zhi.YOU.getName()).isEqualTo("酉");
        }

        @Test
        @DisplayName("戌")
        void testXu() {
            assertThat(Zhi.XU.getName()).isEqualTo("戌");
        }

        @Test
        @DisplayName("亥")
        void testHai() {
            assertThat(Zhi.HAI.getName()).isEqualTo("亥");
        }
    }

    @Nested
    @DisplayName("getElement方法测试")
    class GetElementTests {

        @Test
        @DisplayName("子亥属水")
        void testWaterElement() {
            assertThat(Zhi.ZI.getElement()).isEqualTo("Water");
            assertThat(Zhi.HAI.getElement()).isEqualTo("Water");
        }

        @Test
        @DisplayName("寅卯属木")
        void testWoodElement() {
            assertThat(Zhi.YIN.getElement()).isEqualTo("Wood");
            assertThat(Zhi.MAO.getElement()).isEqualTo("Wood");
        }

        @Test
        @DisplayName("巳午属火")
        void testFireElement() {
            assertThat(Zhi.SI.getElement()).isEqualTo("Fire");
            assertThat(Zhi.WU.getElement()).isEqualTo("Fire");
        }

        @Test
        @DisplayName("申酉属金")
        void testMetalElement() {
            assertThat(Zhi.SHEN.getElement()).isEqualTo("Metal");
            assertThat(Zhi.YOU.getElement()).isEqualTo("Metal");
        }

        @Test
        @DisplayName("丑辰未戌属土")
        void testEarthElement() {
            assertThat(Zhi.CHOU.getElement()).isEqualTo("Earth");
            assertThat(Zhi.CHEN.getElement()).isEqualTo("Earth");
            assertThat(Zhi.WEI.getElement()).isEqualTo("Earth");
            assertThat(Zhi.XU.getElement()).isEqualTo("Earth");
        }
    }

    @Nested
    @DisplayName("getHourStart和getHourEnd方法测试")
    class HourRangeTests {

        @Test
        @DisplayName("子时23-1点")
        void testZiHour() {
            assertThat(Zhi.ZI.getHourStart()).isEqualTo(23);
            assertThat(Zhi.ZI.getHourEnd()).isEqualTo(1);
        }

        @Test
        @DisplayName("丑时1-3点")
        void testChouHour() {
            assertThat(Zhi.CHOU.getHourStart()).isEqualTo(1);
            assertThat(Zhi.CHOU.getHourEnd()).isEqualTo(3);
        }

        @Test
        @DisplayName("午时11-13点")
        void testWuHour() {
            assertThat(Zhi.WU.getHourStart()).isEqualTo(11);
            assertThat(Zhi.WU.getHourEnd()).isEqualTo(13);
        }

        @ParameterizedTest
        @EnumSource(Zhi.class)
        @DisplayName("所有地支时辰范围有效")
        void testAllHourRanges(Zhi zhi) {
            assertThat(zhi.getHourStart()).isBetween(0, 23);
            assertThat(zhi.getHourEnd()).isBetween(1, 24);
        }
    }

    @Nested
    @DisplayName("of方法测试")
    class OfMethodTests {

        @Test
        @DisplayName("索引0返回子")
        void testIndex0() {
            assertThat(Zhi.of(0)).isEqualTo(Zhi.ZI);
        }

        @Test
        @DisplayName("索引11返回亥")
        void testIndex11() {
            assertThat(Zhi.of(11)).isEqualTo(Zhi.HAI);
        }

        @Test
        @DisplayName("索引循环")
        void testIndexCycle() {
            assertThat(Zhi.of(12)).isEqualTo(Zhi.ZI);
            assertThat(Zhi.of(13)).isEqualTo(Zhi.CHOU);
        }

        @Test
        @DisplayName("负索引处理")
        void testNegativeIndex() {
            assertThat(Zhi.of(-1)).isEqualTo(Zhi.HAI);
            assertThat(Zhi.of(-12)).isEqualTo(Zhi.ZI);
        }
    }

    @Nested
    @DisplayName("ofYear方法测试")
    class OfYearTests {

        @Test
        @DisplayName("2024年是辰")
        void test2024() {
            assertThat(Zhi.ofYear(2024)).isEqualTo(Zhi.CHEN);
        }

        @Test
        @DisplayName("2023年是卯")
        void test2023() {
            assertThat(Zhi.ofYear(2023)).isEqualTo(Zhi.MAO);
        }

        @Test
        @DisplayName("2020年是子")
        void test2020() {
            assertThat(Zhi.ofYear(2020)).isEqualTo(Zhi.ZI);
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 1950, 2000, 2050, 2100})
        @DisplayName("所有年份返回有效地支")
        void testAllYears(int year) {
            Zhi zhi = Zhi.ofYear(year);
            assertThat(zhi).isNotNull();
        }
    }

    @Nested
    @DisplayName("ofHour方法测试")
    class OfHourTests {

        @Test
        @DisplayName("0点是子时")
        void testMidnight() {
            assertThat(Zhi.ofHour(0)).isEqualTo(Zhi.ZI);
        }

        @Test
        @DisplayName("12点是午时")
        void testNoon() {
            assertThat(Zhi.ofHour(12)).isEqualTo(Zhi.WU);
        }

        @Test
        @DisplayName("23点是子时")
        void test23Hour() {
            assertThat(Zhi.ofHour(23)).isEqualTo(Zhi.ZI);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23})
        @DisplayName("所有小时返回有效地支")
        void testAllHours(int hour) {
            Zhi zhi = Zhi.ofHour(hour);
            assertThat(zhi).isNotNull();
        }
    }

    @Nested
    @DisplayName("next方法测试")
    class NextTests {

        @Test
        @DisplayName("子的下一个是丑")
        void testZiNext() {
            assertThat(Zhi.ZI.next()).isEqualTo(Zhi.CHOU);
        }

        @Test
        @DisplayName("亥的下一个是子")
        void testHaiNext() {
            assertThat(Zhi.HAI.next()).isEqualTo(Zhi.ZI);
        }

        @ParameterizedTest
        @EnumSource(Zhi.class)
        @DisplayName("所有地支都有下一个")
        void testAllZhisHaveNext(Zhi zhi) {
            assertThat(zhi.next()).isNotNull();
        }
    }

    @Nested
    @DisplayName("previous方法测试")
    class PreviousTests {

        @Test
        @DisplayName("丑的上一个是子")
        void testChouPrevious() {
            assertThat(Zhi.CHOU.previous()).isEqualTo(Zhi.ZI);
        }

        @Test
        @DisplayName("子的上一个是亥")
        void testZiPrevious() {
            assertThat(Zhi.ZI.previous()).isEqualTo(Zhi.HAI);
        }

        @ParameterizedTest
        @EnumSource(Zhi.class)
        @DisplayName("所有地支都有上一个")
        void testAllZhisHavePrevious(Zhi zhi) {
            assertThat(zhi.previous()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getZodiac方法测试")
    class GetZodiacTests {

        @Test
        @DisplayName("子对应鼠")
        void testZiZodiac() {
            assertThat(Zhi.ZI.getZodiac()).isEqualTo(Zodiac.RAT);
        }

        @Test
        @DisplayName("丑对应牛")
        void testChouZodiac() {
            assertThat(Zhi.CHOU.getZodiac()).isEqualTo(Zodiac.OX);
        }

        @Test
        @DisplayName("寅对应虎")
        void testYinZodiac() {
            assertThat(Zhi.YIN.getZodiac()).isEqualTo(Zodiac.TIGER);
        }

        @Test
        @DisplayName("辰对应龙")
        void testChenZodiac() {
            assertThat(Zhi.CHEN.getZodiac()).isEqualTo(Zodiac.DRAGON);
        }

        @ParameterizedTest
        @EnumSource(Zhi.class)
        @DisplayName("所有地支都有对应生肖")
        void testAllZhisHaveZodiac(Zhi zhi) {
            assertThat(zhi.getZodiac()).isNotNull();
        }
    }

    @Nested
    @DisplayName("循环测试")
    class CycleTests {

        @Test
        @DisplayName("next循环回到起点")
        void testNextCycle() {
            Zhi current = Zhi.ZI;
            for (int i = 0; i < 12; i++) {
                current = current.next();
            }
            assertThat(current).isEqualTo(Zhi.ZI);
        }

        @Test
        @DisplayName("previous循环回到起点")
        void testPreviousCycle() {
            Zhi current = Zhi.ZI;
            for (int i = 0; i < 12; i++) {
                current = current.previous();
            }
            assertThat(current).isEqualTo(Zhi.ZI);
        }
    }
}
