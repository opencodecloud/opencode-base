package cloud.opencode.base.lunar.ganzhi;

import cloud.opencode.base.lunar.element.WuXing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * NaYin (纳音五行) Test
 * 纳音五行测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.3
 */
@DisplayName("NaYin (纳音五行) 测试")
class NaYinTest {

    @Nested
    @DisplayName("枚举基本测试")
    class EnumBasicTests {

        @Test
        @DisplayName("共有30个纳音")
        void testCount() {
            assertThat(NaYin.values()).hasSize(30);
        }

        @Test
        @DisplayName("所有纳音名称不为空")
        void testAllNamesNotEmpty() {
            for (NaYin naYin : NaYin.values()) {
                assertThat(naYin.getName()).isNotEmpty();
            }
        }

        @Test
        @DisplayName("所有纳音都有五行属性")
        void testAllHaveWuXing() {
            for (NaYin naYin : NaYin.values()) {
                assertThat(naYin.getWuXing()).isNotNull();
            }
        }

        @Test
        @DisplayName("五行分布均匀（金木水火土各6个）")
        void testWuXingDistribution() {
            int metal = 0, wood = 0, water = 0, fire = 0, earth = 0;
            for (NaYin naYin : NaYin.values()) {
                switch (naYin.getWuXing()) {
                    case METAL -> metal++;
                    case WOOD -> wood++;
                    case WATER -> water++;
                    case FIRE -> fire++;
                    case EARTH -> earth++;
                }
            }
            assertThat(metal).isEqualTo(6);
            assertThat(wood).isEqualTo(6);
            assertThat(water).isEqualTo(6);
            assertThat(fire).isEqualTo(6);
            assertThat(earth).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("海中金")
        void testHaiZhongJin() {
            assertThat(NaYin.HAI_ZHONG_JIN.getName()).isEqualTo("海中金");
        }

        @Test
        @DisplayName("炉中火")
        void testLuZhongHuo() {
            assertThat(NaYin.LU_ZHONG_HUO.getName()).isEqualTo("炉中火");
        }

        @Test
        @DisplayName("大海水")
        void testDaHaiShui() {
            assertThat(NaYin.DA_HAI_SHUI.getName()).isEqualTo("大海水");
        }
    }

    @Nested
    @DisplayName("getWuXing方法测试")
    class GetWuXingTests {

        @Test
        @DisplayName("海中金属金")
        void testHaiZhongJinWuXing() {
            assertThat(NaYin.HAI_ZHONG_JIN.getWuXing()).isEqualTo(WuXing.METAL);
        }

        @Test
        @DisplayName("炉中火属火")
        void testLuZhongHuoWuXing() {
            assertThat(NaYin.LU_ZHONG_HUO.getWuXing()).isEqualTo(WuXing.FIRE);
        }

        @Test
        @DisplayName("大林木属木")
        void testDaLinMuWuXing() {
            assertThat(NaYin.DA_LIN_MU.getWuXing()).isEqualTo(WuXing.WOOD);
        }

        @Test
        @DisplayName("路旁土属土")
        void testLuPangTuWuXing() {
            assertThat(NaYin.LU_PANG_TU.getWuXing()).isEqualTo(WuXing.EARTH);
        }

        @Test
        @DisplayName("涧下水属水")
        void testJianXiaShuiWuXing() {
            assertThat(NaYin.JIAN_XIA_SHUI.getWuXing()).isEqualTo(WuXing.WATER);
        }
    }

    @Nested
    @DisplayName("of方法测试")
    class OfTests {

        @Test
        @DisplayName("索引0和1都是海中金")
        void testIndex0And1() {
            assertThat(NaYin.of(0)).isEqualTo(NaYin.HAI_ZHONG_JIN);
            assertThat(NaYin.of(1)).isEqualTo(NaYin.HAI_ZHONG_JIN);
        }

        @Test
        @DisplayName("索引2和3都是炉中火")
        void testIndex2And3() {
            assertThat(NaYin.of(2)).isEqualTo(NaYin.LU_ZHONG_HUO);
            assertThat(NaYin.of(3)).isEqualTo(NaYin.LU_ZHONG_HUO);
        }

        @Test
        @DisplayName("索引58和59都是大海水")
        void testIndex58And59() {
            assertThat(NaYin.of(58)).isEqualTo(NaYin.DA_HAI_SHUI);
            assertThat(NaYin.of(59)).isEqualTo(NaYin.DA_HAI_SHUI);
        }

        @Test
        @DisplayName("每对连续索引返回相同纳音")
        void testPairConsistency() {
            for (int i = 0; i < 60; i += 2) {
                assertThat(NaYin.of(i)).isEqualTo(NaYin.of(i + 1));
            }
        }

        @Test
        @DisplayName("相邻对返回不同纳音")
        void testAdjacentPairsDifferent() {
            for (int i = 0; i < 58; i += 2) {
                assertThat(NaYin.of(i)).isNotEqualTo(NaYin.of(i + 2));
            }
        }

        @Test
        @DisplayName("负索引正确处理")
        void testNegativeIndex() {
            // -1 should map to 59, which is 大海水
            assertThat(NaYin.of(-1)).isEqualTo(NaYin.DA_HAI_SHUI);
            // -2 should map to 58, also 大海水
            assertThat(NaYin.of(-2)).isEqualTo(NaYin.DA_HAI_SHUI);
        }

        @Test
        @DisplayName("超出范围索引正确处理（取模60）")
        void testOverflowIndex() {
            assertThat(NaYin.of(60)).isEqualTo(NaYin.of(0));
            assertThat(NaYin.of(61)).isEqualTo(NaYin.of(1));
            assertThat(NaYin.of(120)).isEqualTo(NaYin.of(0));
        }
    }

    @Nested
    @DisplayName("纳音表完整对照测试")
    class NaYinTableTests {

        @ParameterizedTest
        @CsvSource({
            "0, HAI_ZHONG_JIN",
            "2, LU_ZHONG_HUO",
            "4, DA_LIN_MU",
            "6, LU_PANG_TU",
            "8, JIAN_FENG_JIN",
            "10, SHAN_TOU_HUO",
            "12, JIAN_XIA_SHUI",
            "14, CHENG_TOU_TU",
            "16, BAI_LA_JIN",
            "18, YANG_LIU_MU",
            "20, QUAN_ZHONG_SHUI",
            "22, WU_SHANG_TU",
            "24, PI_LI_HUO",
            "26, SONG_BAI_MU",
            "28, CHANG_LIU_SHUI",
            "30, SHA_ZHONG_JIN",
            "32, SHAN_XIA_HUO",
            "34, PING_DI_MU",
            "36, BI_SHANG_TU",
            "38, JIN_BO_JIN",
            "40, FU_DENG_HUO",
            "42, TIAN_HE_SHUI",
            "44, DA_YI_TU",
            "46, CHAI_CHUAN_JIN",
            "48, SANG_ZHE_MU",
            "50, DA_XI_SHUI",
            "52, SHA_ZHONG_TU",
            "54, TIAN_SHANG_HUO",
            "56, SHI_LIU_MU",
            "58, DA_HAI_SHUI"
        })
        @DisplayName("六十甲子纳音对照")
        void testNaYinMapping(int cycleIndex, String expectedName) {
            NaYin expected = NaYin.valueOf(expectedName);
            assertThat(NaYin.of(cycleIndex)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("与GanZhi集成测试")
    class GanZhiIntegrationTests {

        @Test
        @DisplayName("甲子年纳音为海中金")
        void testJiaZiYear() {
            // 1984年是甲子年
            GanZhi gz = GanZhi.ofYear(1984);
            assertThat(gz.getNaYin()).isEqualTo(NaYin.HAI_ZHONG_JIN);
            assertThat(gz.getNaYin().getName()).isEqualTo("海中金");
        }

        @Test
        @DisplayName("甲辰年纳音为覆灯火")
        void testJiaChenYear() {
            // 2024年是甲辰年, cycle index = 40
            GanZhi gz = GanZhi.ofYear(2024);
            assertThat(gz.getName()).isEqualTo("甲辰");
            assertThat(gz.getNaYin()).isEqualTo(NaYin.FU_DENG_HUO);
            assertThat(gz.getNaYin().getName()).isEqualTo("覆灯火");
            assertThat(gz.getNaYin().getWuXing()).isEqualTo(WuXing.FIRE);
        }

        @Test
        @DisplayName("癸亥纳音为大海水")
        void testGuiHaiNaYin() {
            GanZhi gz = new GanZhi(Gan.GUI, Zhi.HAI);
            assertThat(gz.getNaYin()).isEqualTo(NaYin.DA_HAI_SHUI);
        }

        @Test
        @DisplayName("60个干支的纳音覆盖全部30种")
        void testAllNaYinCovered() {
            java.util.Set<NaYin> seen = new java.util.HashSet<>();
            for (int i = 0; i < 60; i++) {
                GanZhi gz = GanZhi.ofCycleIndex(i);
                seen.add(gz.getNaYin());
            }
            assertThat(seen).hasSize(30);
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString返回中文名称")
        void testToString() {
            assertThat(NaYin.HAI_ZHONG_JIN.toString()).isEqualTo("海中金");
            assertThat(NaYin.DA_HAI_SHUI.toString()).isEqualTo("大海水");
        }
    }

    @Nested
    @DisplayName("valueOf方法测试")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(NaYin.valueOf("HAI_ZHONG_JIN")).isEqualTo(NaYin.HAI_ZHONG_JIN);
            assertThat(NaYin.valueOf("DA_HAI_SHUI")).isEqualTo(NaYin.DA_HAI_SHUI);
        }

        @Test
        @DisplayName("无效名称抛出异常")
        void testValueOfInvalid() {
            assertThatThrownBy(() -> NaYin.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
