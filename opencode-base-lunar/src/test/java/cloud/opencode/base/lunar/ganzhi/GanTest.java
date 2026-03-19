package cloud.opencode.base.lunar.ganzhi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Gan (天干) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("Gan (天干) 测试")
class GanTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含10个天干")
        void testTenGans() {
            assertThat(Gan.values()).hasSize(10);
        }

        @Test
        @DisplayName("天干顺序正确")
        void testGanOrder() {
            Gan[] gans = Gan.values();
            assertThat(gans[0]).isEqualTo(Gan.JIA);
            assertThat(gans[1]).isEqualTo(Gan.YI);
            assertThat(gans[2]).isEqualTo(Gan.BING);
            assertThat(gans[3]).isEqualTo(Gan.DING);
            assertThat(gans[4]).isEqualTo(Gan.WU);
            assertThat(gans[5]).isEqualTo(Gan.JI);
            assertThat(gans[6]).isEqualTo(Gan.GENG);
            assertThat(gans[7]).isEqualTo(Gan.XIN);
            assertThat(gans[8]).isEqualTo(Gan.REN);
            assertThat(gans[9]).isEqualTo(Gan.GUI);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("甲")
        void testJia() {
            assertThat(Gan.JIA.getName()).isEqualTo("甲");
        }

        @Test
        @DisplayName("乙")
        void testYi() {
            assertThat(Gan.YI.getName()).isEqualTo("乙");
        }

        @Test
        @DisplayName("丙")
        void testBing() {
            assertThat(Gan.BING.getName()).isEqualTo("丙");
        }

        @Test
        @DisplayName("丁")
        void testDing() {
            assertThat(Gan.DING.getName()).isEqualTo("丁");
        }

        @Test
        @DisplayName("戊")
        void testWu() {
            assertThat(Gan.WU.getName()).isEqualTo("戊");
        }

        @Test
        @DisplayName("己")
        void testJi() {
            assertThat(Gan.JI.getName()).isEqualTo("己");
        }

        @Test
        @DisplayName("庚")
        void testGeng() {
            assertThat(Gan.GENG.getName()).isEqualTo("庚");
        }

        @Test
        @DisplayName("辛")
        void testXin() {
            assertThat(Gan.XIN.getName()).isEqualTo("辛");
        }

        @Test
        @DisplayName("壬")
        void testRen() {
            assertThat(Gan.REN.getName()).isEqualTo("壬");
        }

        @Test
        @DisplayName("癸")
        void testGui() {
            assertThat(Gan.GUI.getName()).isEqualTo("癸");
        }
    }

    @Nested
    @DisplayName("getElement方法测试")
    class GetElementTests {

        @Test
        @DisplayName("甲乙属木")
        void testWoodElement() {
            assertThat(Gan.JIA.getElement()).isEqualTo("Wood");
            assertThat(Gan.YI.getElement()).isEqualTo("Wood");
        }

        @Test
        @DisplayName("丙丁属火")
        void testFireElement() {
            assertThat(Gan.BING.getElement()).isEqualTo("Fire");
            assertThat(Gan.DING.getElement()).isEqualTo("Fire");
        }

        @Test
        @DisplayName("戊己属土")
        void testEarthElement() {
            assertThat(Gan.WU.getElement()).isEqualTo("Earth");
            assertThat(Gan.JI.getElement()).isEqualTo("Earth");
        }

        @Test
        @DisplayName("庚辛属金")
        void testMetalElement() {
            assertThat(Gan.GENG.getElement()).isEqualTo("Metal");
            assertThat(Gan.XIN.getElement()).isEqualTo("Metal");
        }

        @Test
        @DisplayName("壬癸属水")
        void testWaterElement() {
            assertThat(Gan.REN.getElement()).isEqualTo("Water");
            assertThat(Gan.GUI.getElement()).isEqualTo("Water");
        }
    }

    @Nested
    @DisplayName("isYang方法测试")
    class IsYangTests {

        @Test
        @DisplayName("奇数位置是阳干")
        void testYangGans() {
            assertThat(Gan.JIA.isYang()).isTrue();
            assertThat(Gan.BING.isYang()).isTrue();
            assertThat(Gan.WU.isYang()).isTrue();
            assertThat(Gan.GENG.isYang()).isTrue();
            assertThat(Gan.REN.isYang()).isTrue();
        }

        @Test
        @DisplayName("偶数位置是阴干")
        void testYinGans() {
            assertThat(Gan.YI.isYang()).isFalse();
            assertThat(Gan.DING.isYang()).isFalse();
            assertThat(Gan.JI.isYang()).isFalse();
            assertThat(Gan.XIN.isYang()).isFalse();
            assertThat(Gan.GUI.isYang()).isFalse();
        }
    }

    @Nested
    @DisplayName("of方法测试")
    class OfMethodTests {

        @Test
        @DisplayName("索引0返回甲")
        void testIndex0() {
            assertThat(Gan.of(0)).isEqualTo(Gan.JIA);
        }

        @Test
        @DisplayName("索引9返回癸")
        void testIndex9() {
            assertThat(Gan.of(9)).isEqualTo(Gan.GUI);
        }

        @Test
        @DisplayName("索引循环")
        void testIndexCycle() {
            assertThat(Gan.of(10)).isEqualTo(Gan.JIA);
            assertThat(Gan.of(11)).isEqualTo(Gan.YI);
        }

        @Test
        @DisplayName("负索引处理")
        void testNegativeIndex() {
            assertThat(Gan.of(-1)).isEqualTo(Gan.GUI);
            assertThat(Gan.of(-10)).isEqualTo(Gan.JIA);
        }
    }

    @Nested
    @DisplayName("ofYear方法测试")
    class OfYearTests {

        @Test
        @DisplayName("2024年是甲")
        void test2024() {
            assertThat(Gan.ofYear(2024)).isEqualTo(Gan.JIA);
        }

        @Test
        @DisplayName("2023年是癸")
        void test2023() {
            assertThat(Gan.ofYear(2023)).isEqualTo(Gan.GUI);
        }

        @Test
        @DisplayName("1984年是甲")
        void test1984() {
            assertThat(Gan.ofYear(1984)).isEqualTo(Gan.JIA);
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 1950, 2000, 2050, 2100})
        @DisplayName("所有年份返回有效天干")
        void testAllYears(int year) {
            Gan gan = Gan.ofYear(year);
            assertThat(gan).isNotNull();
        }
    }

    @Nested
    @DisplayName("next方法测试")
    class NextTests {

        @Test
        @DisplayName("甲的下一个是乙")
        void testJiaNext() {
            assertThat(Gan.JIA.next()).isEqualTo(Gan.YI);
        }

        @Test
        @DisplayName("癸的下一个是甲")
        void testGuiNext() {
            assertThat(Gan.GUI.next()).isEqualTo(Gan.JIA);
        }

        @ParameterizedTest
        @EnumSource(Gan.class)
        @DisplayName("所有天干都有下一个")
        void testAllGansHaveNext(Gan gan) {
            assertThat(gan.next()).isNotNull();
        }
    }

    @Nested
    @DisplayName("previous方法测试")
    class PreviousTests {

        @Test
        @DisplayName("乙的上一个是甲")
        void testYiPrevious() {
            assertThat(Gan.YI.previous()).isEqualTo(Gan.JIA);
        }

        @Test
        @DisplayName("甲的上一个是癸")
        void testJiaPrevious() {
            assertThat(Gan.JIA.previous()).isEqualTo(Gan.GUI);
        }

        @ParameterizedTest
        @EnumSource(Gan.class)
        @DisplayName("所有天干都有上一个")
        void testAllGansHavePrevious(Gan gan) {
            assertThat(gan.previous()).isNotNull();
        }
    }

    @Nested
    @DisplayName("循环测试")
    class CycleTests {

        @Test
        @DisplayName("next循环回到起点")
        void testNextCycle() {
            Gan current = Gan.JIA;
            for (int i = 0; i < 10; i++) {
                current = current.next();
            }
            assertThat(current).isEqualTo(Gan.JIA);
        }

        @Test
        @DisplayName("previous循环回到起点")
        void testPreviousCycle() {
            Gan current = Gan.JIA;
            for (int i = 0; i < 10; i++) {
                current = current.previous();
            }
            assertThat(current).isEqualTo(Gan.JIA);
        }
    }
}
