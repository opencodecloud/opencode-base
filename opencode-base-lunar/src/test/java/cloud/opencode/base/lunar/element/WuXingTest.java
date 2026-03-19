package cloud.opencode.base.lunar.element;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * WuXing (五行) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("WuXing (五行) 测试")
class WuXingTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含5种元素")
        void testFiveElements() {
            assertThat(WuXing.values()).hasSize(5);
        }

        @Test
        @DisplayName("元素顺序正确")
        void testElementOrder() {
            WuXing[] elements = WuXing.values();
            assertThat(elements[0]).isEqualTo(WuXing.WOOD);
            assertThat(elements[1]).isEqualTo(WuXing.FIRE);
            assertThat(elements[2]).isEqualTo(WuXing.EARTH);
            assertThat(elements[3]).isEqualTo(WuXing.METAL);
            assertThat(elements[4]).isEqualTo(WuXing.WATER);
        }
    }

    @Nested
    @DisplayName("getChinese方法测试")
    class GetChineseTests {

        @Test
        @DisplayName("木")
        void testWood() {
            assertThat(WuXing.WOOD.getChinese()).isEqualTo("木");
        }

        @Test
        @DisplayName("火")
        void testFire() {
            assertThat(WuXing.FIRE.getChinese()).isEqualTo("火");
        }

        @Test
        @DisplayName("土")
        void testEarth() {
            assertThat(WuXing.EARTH.getChinese()).isEqualTo("土");
        }

        @Test
        @DisplayName("金")
        void testMetal() {
            assertThat(WuXing.METAL.getChinese()).isEqualTo("金");
        }

        @Test
        @DisplayName("水")
        void testWater() {
            assertThat(WuXing.WATER.getChinese()).isEqualTo("水");
        }
    }

    @Nested
    @DisplayName("getColor方法测试")
    class GetColorTests {

        @Test
        @DisplayName("木-青")
        void testWoodColor() {
            assertThat(WuXing.WOOD.getColor()).isEqualTo("青");
        }

        @Test
        @DisplayName("火-赤")
        void testFireColor() {
            assertThat(WuXing.FIRE.getColor()).isEqualTo("赤");
        }

        @Test
        @DisplayName("土-黄")
        void testEarthColor() {
            assertThat(WuXing.EARTH.getColor()).isEqualTo("黄");
        }

        @Test
        @DisplayName("金-白")
        void testMetalColor() {
            assertThat(WuXing.METAL.getColor()).isEqualTo("白");
        }

        @Test
        @DisplayName("水-黑")
        void testWaterColor() {
            assertThat(WuXing.WATER.getColor()).isEqualTo("黑");
        }
    }

    @Nested
    @DisplayName("getDirection方法测试")
    class GetDirectionTests {

        @Test
        @DisplayName("木-东")
        void testWoodDirection() {
            assertThat(WuXing.WOOD.getDirection()).isEqualTo("东");
        }

        @Test
        @DisplayName("火-南")
        void testFireDirection() {
            assertThat(WuXing.FIRE.getDirection()).isEqualTo("南");
        }

        @Test
        @DisplayName("土-中")
        void testEarthDirection() {
            assertThat(WuXing.EARTH.getDirection()).isEqualTo("中");
        }

        @Test
        @DisplayName("金-西")
        void testMetalDirection() {
            assertThat(WuXing.METAL.getDirection()).isEqualTo("西");
        }

        @Test
        @DisplayName("水-北")
        void testWaterDirection() {
            assertThat(WuXing.WATER.getDirection()).isEqualTo("北");
        }
    }

    @Nested
    @DisplayName("generates方法测试 - 相生")
    class GeneratesTests {

        @Test
        @DisplayName("木生火")
        void testWoodGeneratesFire() {
            assertThat(WuXing.WOOD.generates()).isEqualTo(WuXing.FIRE);
        }

        @Test
        @DisplayName("火生土")
        void testFireGeneratesEarth() {
            assertThat(WuXing.FIRE.generates()).isEqualTo(WuXing.EARTH);
        }

        @Test
        @DisplayName("土生金")
        void testEarthGeneratesMetal() {
            assertThat(WuXing.EARTH.generates()).isEqualTo(WuXing.METAL);
        }

        @Test
        @DisplayName("金生水")
        void testMetalGeneratesWater() {
            assertThat(WuXing.METAL.generates()).isEqualTo(WuXing.WATER);
        }

        @Test
        @DisplayName("水生木")
        void testWaterGeneratesWood() {
            assertThat(WuXing.WATER.generates()).isEqualTo(WuXing.WOOD);
        }

        @ParameterizedTest
        @EnumSource(WuXing.class)
        @DisplayName("所有元素都有生成对象")
        void testAllGenerate(WuXing element) {
            assertThat(element.generates()).isNotNull();
            assertThat(element.generates()).isNotEqualTo(element);
        }
    }

    @Nested
    @DisplayName("generatedBy方法测试 - 被生")
    class GeneratedByTests {

        @Test
        @DisplayName("木被水生")
        void testWoodGeneratedByWater() {
            assertThat(WuXing.WOOD.generatedBy()).isEqualTo(WuXing.WATER);
        }

        @Test
        @DisplayName("火被木生")
        void testFireGeneratedByWood() {
            assertThat(WuXing.FIRE.generatedBy()).isEqualTo(WuXing.WOOD);
        }

        @Test
        @DisplayName("土被火生")
        void testEarthGeneratedByFire() {
            assertThat(WuXing.EARTH.generatedBy()).isEqualTo(WuXing.FIRE);
        }

        @Test
        @DisplayName("金被土生")
        void testMetalGeneratedByEarth() {
            assertThat(WuXing.METAL.generatedBy()).isEqualTo(WuXing.EARTH);
        }

        @Test
        @DisplayName("水被金生")
        void testWaterGeneratedByMetal() {
            assertThat(WuXing.WATER.generatedBy()).isEqualTo(WuXing.METAL);
        }
    }

    @Nested
    @DisplayName("overcomes方法测试 - 相克")
    class OvercomesTests {

        @Test
        @DisplayName("木克土")
        void testWoodOvercomesEarth() {
            assertThat(WuXing.WOOD.overcomes()).isEqualTo(WuXing.EARTH);
        }

        @Test
        @DisplayName("火克金")
        void testFireOvercomesMetal() {
            assertThat(WuXing.FIRE.overcomes()).isEqualTo(WuXing.METAL);
        }

        @Test
        @DisplayName("土克水")
        void testEarthOvercomesWater() {
            assertThat(WuXing.EARTH.overcomes()).isEqualTo(WuXing.WATER);
        }

        @Test
        @DisplayName("金克木")
        void testMetalOvercomesWood() {
            assertThat(WuXing.METAL.overcomes()).isEqualTo(WuXing.WOOD);
        }

        @Test
        @DisplayName("水克火")
        void testWaterOvercomesFire() {
            assertThat(WuXing.WATER.overcomes()).isEqualTo(WuXing.FIRE);
        }

        @ParameterizedTest
        @EnumSource(WuXing.class)
        @DisplayName("所有元素都有克制对象")
        void testAllOvercome(WuXing element) {
            assertThat(element.overcomes()).isNotNull();
            assertThat(element.overcomes()).isNotEqualTo(element);
        }
    }

    @Nested
    @DisplayName("overcomeBy方法测试 - 被克")
    class OvercomeByTests {

        @Test
        @DisplayName("木被金克")
        void testWoodOvercomeByMetal() {
            assertThat(WuXing.WOOD.overcomeBy()).isEqualTo(WuXing.METAL);
        }

        @Test
        @DisplayName("火被水克")
        void testFireOvercomeByWater() {
            assertThat(WuXing.FIRE.overcomeBy()).isEqualTo(WuXing.WATER);
        }

        @Test
        @DisplayName("土被木克")
        void testEarthOvercomeByWood() {
            assertThat(WuXing.EARTH.overcomeBy()).isEqualTo(WuXing.WOOD);
        }

        @Test
        @DisplayName("金被火克")
        void testMetalOvercomeByFire() {
            assertThat(WuXing.METAL.overcomeBy()).isEqualTo(WuXing.FIRE);
        }

        @Test
        @DisplayName("水被土克")
        void testWaterOvercomeByEarth() {
            assertThat(WuXing.WATER.overcomeBy()).isEqualTo(WuXing.EARTH);
        }
    }

    @Nested
    @DisplayName("fromGan方法测试")
    class FromGanTests {

        @Test
        @DisplayName("甲(0)属木")
        void testJiaWood() {
            assertThat(WuXing.fromGan(0)).isEqualTo(WuXing.WOOD); // 甲
        }

        @Test
        @DisplayName("乙(1)属木")
        void testYiWood() {
            assertThat(WuXing.fromGan(1)).isEqualTo(WuXing.WOOD); // 乙
        }

        @Test
        @DisplayName("丙(2)属火")
        void testBingFire() {
            assertThat(WuXing.fromGan(2)).isEqualTo(WuXing.FIRE); // 丙
        }

        @Test
        @DisplayName("丁(3)属火")
        void testDingFire() {
            assertThat(WuXing.fromGan(3)).isEqualTo(WuXing.FIRE); // 丁
        }

        @Test
        @DisplayName("戊(4)属土")
        void testWuEarth() {
            assertThat(WuXing.fromGan(4)).isEqualTo(WuXing.EARTH); // 戊
        }

        @Test
        @DisplayName("己(5)属土")
        void testJiEarth() {
            assertThat(WuXing.fromGan(5)).isEqualTo(WuXing.EARTH); // 己
        }

        @Test
        @DisplayName("庚(6)属金")
        void testGengMetal() {
            assertThat(WuXing.fromGan(6)).isEqualTo(WuXing.METAL); // 庚
        }

        @Test
        @DisplayName("辛(7)属金")
        void testXinMetal() {
            assertThat(WuXing.fromGan(7)).isEqualTo(WuXing.METAL); // 辛
        }

        @Test
        @DisplayName("壬(8)属水")
        void testRenWater() {
            assertThat(WuXing.fromGan(8)).isEqualTo(WuXing.WATER); // 壬
        }

        @Test
        @DisplayName("癸(9)属水")
        void testGuiWater() {
            assertThat(WuXing.fromGan(9)).isEqualTo(WuXing.WATER); // 癸
        }
    }

    @Nested
    @DisplayName("fromZhi方法测试")
    class FromZhiTests {

        @Test
        @DisplayName("子(0)属水")
        void testZiWater() {
            assertThat(WuXing.fromZhi(0)).isEqualTo(WuXing.WATER); // 子
        }

        @Test
        @DisplayName("丑(1)属土")
        void testChouEarth() {
            assertThat(WuXing.fromZhi(1)).isEqualTo(WuXing.EARTH); // 丑
        }

        @Test
        @DisplayName("寅(2)属木")
        void testYinWood() {
            assertThat(WuXing.fromZhi(2)).isEqualTo(WuXing.WOOD); // 寅
        }

        @Test
        @DisplayName("卯(3)属木")
        void testMaoWood() {
            assertThat(WuXing.fromZhi(3)).isEqualTo(WuXing.WOOD); // 卯
        }

        @Test
        @DisplayName("巳(5)属火")
        void testSiFire() {
            assertThat(WuXing.fromZhi(5)).isEqualTo(WuXing.FIRE); // 巳
        }

        @Test
        @DisplayName("午(6)属火")
        void testWuFire() {
            assertThat(WuXing.fromZhi(6)).isEqualTo(WuXing.FIRE); // 午
        }

        @Test
        @DisplayName("申(8)属金")
        void testShenMetal() {
            assertThat(WuXing.fromZhi(8)).isEqualTo(WuXing.METAL); // 申
        }

        @Test
        @DisplayName("酉(9)属金")
        void testYouMetal() {
            assertThat(WuXing.fromZhi(9)).isEqualTo(WuXing.METAL); // 酉
        }

        @Test
        @DisplayName("亥(11)属水")
        void testHaiWater() {
            assertThat(WuXing.fromZhi(11)).isEqualTo(WuXing.WATER); // 亥
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString返回中文名")
        void testToString() {
            assertThat(WuXing.WOOD.toString()).isEqualTo("木");
            assertThat(WuXing.FIRE.toString()).isEqualTo("火");
            assertThat(WuXing.EARTH.toString()).isEqualTo("土");
            assertThat(WuXing.METAL.toString()).isEqualTo("金");
            assertThat(WuXing.WATER.toString()).isEqualTo("水");
        }
    }

    @Nested
    @DisplayName("相生相克循环测试")
    class CycleTests {

        @Test
        @DisplayName("相生循环")
        void testGenerationCycle() {
            WuXing current = WuXing.WOOD;
            for (int i = 0; i < 5; i++) {
                current = current.generates();
            }
            assertThat(current).isEqualTo(WuXing.WOOD);
        }

        @Test
        @DisplayName("相克循环")
        void testOvercomeCycle() {
            WuXing current = WuXing.WOOD;
            for (int i = 0; i < 5; i++) {
                current = current.overcomes();
            }
            assertThat(current).isEqualTo(WuXing.WOOD);
        }

        @Test
        @DisplayName("相生和被生互逆")
        void testGeneratesAndGeneratedByInverse() {
            for (WuXing element : WuXing.values()) {
                assertThat(element.generates().generatedBy()).isEqualTo(element);
            }
        }

        @Test
        @DisplayName("相克和被克互逆")
        void testOvercomesAndOvercomeByInverse() {
            for (WuXing element : WuXing.values()) {
                assertThat(element.overcomes().overcomeBy()).isEqualTo(element);
            }
        }
    }
}
