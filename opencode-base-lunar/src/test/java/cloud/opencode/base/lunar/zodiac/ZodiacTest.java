package cloud.opencode.base.lunar.zodiac;

import cloud.opencode.base.lunar.ganzhi.Zhi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Zodiac (生肖) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("Zodiac (生肖) 测试")
class ZodiacTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含12个生肖")
        void testTwelveZodiacs() {
            assertThat(Zodiac.values()).hasSize(12);
        }

        @Test
        @DisplayName("生肖顺序正确")
        void testZodiacOrder() {
            Zodiac[] zodiacs = Zodiac.values();
            assertThat(zodiacs[0]).isEqualTo(Zodiac.RAT);
            assertThat(zodiacs[1]).isEqualTo(Zodiac.OX);
            assertThat(zodiacs[2]).isEqualTo(Zodiac.TIGER);
            assertThat(zodiacs[3]).isEqualTo(Zodiac.RABBIT);
            assertThat(zodiacs[4]).isEqualTo(Zodiac.DRAGON);
            assertThat(zodiacs[5]).isEqualTo(Zodiac.SNAKE);
            assertThat(zodiacs[6]).isEqualTo(Zodiac.HORSE);
            assertThat(zodiacs[7]).isEqualTo(Zodiac.GOAT);
            assertThat(zodiacs[8]).isEqualTo(Zodiac.MONKEY);
            assertThat(zodiacs[9]).isEqualTo(Zodiac.ROOSTER);
            assertThat(zodiacs[10]).isEqualTo(Zodiac.DOG);
            assertThat(zodiacs[11]).isEqualTo(Zodiac.PIG);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("鼠")
        void testRat() {
            assertThat(Zodiac.RAT.getName()).isEqualTo("鼠");
        }

        @Test
        @DisplayName("牛")
        void testOx() {
            assertThat(Zodiac.OX.getName()).isEqualTo("牛");
        }

        @Test
        @DisplayName("虎")
        void testTiger() {
            assertThat(Zodiac.TIGER.getName()).isEqualTo("虎");
        }

        @Test
        @DisplayName("兔")
        void testRabbit() {
            assertThat(Zodiac.RABBIT.getName()).isEqualTo("兔");
        }

        @Test
        @DisplayName("龙")
        void testDragon() {
            assertThat(Zodiac.DRAGON.getName()).isEqualTo("龙");
        }

        @Test
        @DisplayName("蛇")
        void testSnake() {
            assertThat(Zodiac.SNAKE.getName()).isEqualTo("蛇");
        }

        @Test
        @DisplayName("马")
        void testHorse() {
            assertThat(Zodiac.HORSE.getName()).isEqualTo("马");
        }

        @Test
        @DisplayName("羊")
        void testGoat() {
            assertThat(Zodiac.GOAT.getName()).isEqualTo("羊");
        }

        @Test
        @DisplayName("猴")
        void testMonkey() {
            assertThat(Zodiac.MONKEY.getName()).isEqualTo("猴");
        }

        @Test
        @DisplayName("鸡")
        void testRooster() {
            assertThat(Zodiac.ROOSTER.getName()).isEqualTo("鸡");
        }

        @Test
        @DisplayName("狗")
        void testDog() {
            assertThat(Zodiac.DOG.getName()).isEqualTo("狗");
        }

        @Test
        @DisplayName("猪")
        void testPig() {
            assertThat(Zodiac.PIG.getName()).isEqualTo("猪");
        }
    }

    @Nested
    @DisplayName("of(year)方法测试")
    class OfYearTests {

        @Test
        @DisplayName("2024年是龙年")
        void test2024Dragon() {
            assertThat(Zodiac.of(2024)).isEqualTo(Zodiac.DRAGON);
        }

        @Test
        @DisplayName("2023年是兔年")
        void test2023Rabbit() {
            assertThat(Zodiac.of(2023)).isEqualTo(Zodiac.RABBIT);
        }

        @Test
        @DisplayName("2020年是鼠年")
        void test2020Rat() {
            assertThat(Zodiac.of(2020)).isEqualTo(Zodiac.RAT);
        }

        @Test
        @DisplayName("2021年是牛年")
        void test2021Ox() {
            assertThat(Zodiac.of(2021)).isEqualTo(Zodiac.OX);
        }

        @Test
        @DisplayName("2022年是虎年")
        void test2022Tiger() {
            assertThat(Zodiac.of(2022)).isEqualTo(Zodiac.TIGER);
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 1950, 2000, 2050, 2100})
        @DisplayName("所有年份返回有效生肖")
        void testAllYears(int year) {
            Zodiac zodiac = Zodiac.of(year);
            assertThat(zodiac).isNotNull();
        }
    }

    @Nested
    @DisplayName("ofIndex方法测试")
    class OfIndexTests {

        @Test
        @DisplayName("索引0返回鼠")
        void testIndex0() {
            assertThat(Zodiac.ofIndex(0)).isEqualTo(Zodiac.RAT);
        }

        @Test
        @DisplayName("索引11返回猪")
        void testIndex11() {
            assertThat(Zodiac.ofIndex(11)).isEqualTo(Zodiac.PIG);
        }

        @Test
        @DisplayName("索引循环")
        void testIndexCycle() {
            assertThat(Zodiac.ofIndex(12)).isEqualTo(Zodiac.RAT);
            assertThat(Zodiac.ofIndex(13)).isEqualTo(Zodiac.OX);
        }
    }

    @Nested
    @DisplayName("nextYear方法测试")
    class NextYearTests {

        @Test
        @DisplayName("从2020年查找下一个鼠年是2032年")
        void testNextRatYear() {
            // 2020年是鼠年，下一个鼠年是2032年
            assertThat(Zodiac.RAT.nextYear(2020)).isEqualTo(2032);
        }

        @Test
        @DisplayName("从2024年查找下一个龙年是2036年")
        void testNextDragonYear() {
            // 2024年是龙年，下一个龙年是2036年
            assertThat(Zodiac.DRAGON.nextYear(2024)).isEqualTo(2036);
        }

        @Test
        @DisplayName("从2023年查找下一个龙年是2024年")
        void testNextDragonFromRabbit() {
            // 2023年是兔年，下一个龙年是2024年
            assertThat(Zodiac.DRAGON.nextYear(2023)).isEqualTo(2024);
        }

        @ParameterizedTest
        @EnumSource(Zodiac.class)
        @DisplayName("所有生肖都能找到下一年")
        void testAllZodiacsHaveNext(Zodiac zodiac) {
            int nextYear = zodiac.nextYear(2024);
            assertThat(nextYear).isGreaterThan(2024);
            assertThat(Zodiac.of(nextYear)).isEqualTo(zodiac);
        }
    }

    @Nested
    @DisplayName("previousYear方法测试")
    class PreviousYearTests {

        @Test
        @DisplayName("从2020年查找上一个鼠年是2008年")
        void testPreviousRatYear() {
            // 2020年是鼠年，上一个鼠年是2008年
            assertThat(Zodiac.RAT.previousYear(2020)).isEqualTo(2008);
        }

        @Test
        @DisplayName("从2024年查找上一个龙年是2012年")
        void testPreviousDragonYear() {
            // 2024年是龙年，上一个龙年是2012年
            assertThat(Zodiac.DRAGON.previousYear(2024)).isEqualTo(2012);
        }

        @Test
        @DisplayName("从2025年查找上一个龙年是2024年")
        void testPreviousDragonFromSnake() {
            // 2025年是蛇年，上一个龙年是2024年
            assertThat(Zodiac.DRAGON.previousYear(2025)).isEqualTo(2024);
        }

        @ParameterizedTest
        @EnumSource(Zodiac.class)
        @DisplayName("所有生肖都能找到上一年")
        void testAllZodiacsHavePrevious(Zodiac zodiac) {
            int prevYear = zodiac.previousYear(2024);
            assertThat(prevYear).isLessThan(2024);
            assertThat(Zodiac.of(prevYear)).isEqualTo(zodiac);
        }
    }

    @Nested
    @DisplayName("isYear方法测试")
    class IsYearTests {

        @Test
        @DisplayName("2024是龙年")
        void test2024IsDragon() {
            assertThat(Zodiac.DRAGON.isYear(2024)).isTrue();
            assertThat(Zodiac.RAT.isYear(2024)).isFalse();
        }

        @Test
        @DisplayName("2020是鼠年")
        void test2020IsRat() {
            assertThat(Zodiac.RAT.isYear(2020)).isTrue();
            assertThat(Zodiac.DRAGON.isYear(2020)).isFalse();
        }
    }

    @Nested
    @DisplayName("getZhi方法测试")
    class GetZhiTests {

        @Test
        @DisplayName("鼠对应子")
        void testRatZhi() {
            assertThat(Zodiac.RAT.getZhi()).isEqualTo(Zhi.ZI);
        }

        @Test
        @DisplayName("龙对应辰")
        void testDragonZhi() {
            assertThat(Zodiac.DRAGON.getZhi()).isEqualTo(Zhi.CHEN);
        }

        @ParameterizedTest
        @EnumSource(Zodiac.class)
        @DisplayName("所有生肖都有对应地支")
        void testAllZodiacsHaveZhi(Zodiac zodiac) {
            assertThat(zodiac.getZhi()).isNotNull();
        }
    }

    @Nested
    @DisplayName("循环测试")
    class CycleTests {

        @Test
        @DisplayName("12年循环")
        void testTwelveYearCycle() {
            // 验证12年一个循环：2020年和2032年都是鼠年
            assertThat(Zodiac.of(2020)).isEqualTo(Zodiac.of(2032));
        }

        @Test
        @DisplayName("生肖与年份对应")
        void testZodiacYearCorrespondence() {
            for (int year = 2020; year < 2032; year++) {
                // 验证12年一个循环
                assertThat(Zodiac.of(year)).isEqualTo(Zodiac.of(year + 12));
            }
        }

        @Test
        @DisplayName("nextYear和previousYear互逆")
        void testNextAndPreviousInverse() {
            int baseYear = 2024;
            for (Zodiac zodiac : Zodiac.values()) {
                int nextYear = zodiac.nextYear(baseYear);
                int prevYear = zodiac.previousYear(nextYear);
                // 从nextYear往前找，应该回到baseYear或更早的同生肖年
                assertThat(Zodiac.of(prevYear)).isEqualTo(zodiac);
            }
        }
    }
}
