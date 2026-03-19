package cloud.opencode.base.lunar.divination;

import cloud.opencode.base.lunar.LunarDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * YiJi (宜忌) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("YiJi (宜忌) 测试")
class YiJiTest {

    @Nested
    @DisplayName("Record基本测试")
    class RecordBasicTests {

        @Test
        @DisplayName("创建宜忌")
        void testCreate() {
            List<String> suitable = List.of("嫁娶", "祭祀");
            List<String> avoid = List.of("破土", "安葬");
            YiJi yiji = new YiJi(suitable, avoid);

            assertThat(yiji.suitable()).isEqualTo(suitable);
            assertThat(yiji.avoid()).isEqualTo(avoid);
        }

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            YiJi y1 = new YiJi(List.of("祭祀"), List.of("破土"));
            YiJi y2 = new YiJi(List.of("祭祀"), List.of("破土"));
            YiJi y3 = new YiJi(List.of("嫁娶"), List.of("安葬"));

            assertThat(y1).isEqualTo(y2);
            assertThat(y1).isNotEqualTo(y3);
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("JI_SI常量存在")
        void testJiSi() {
            assertThat(YiJi.JI_SI).isEqualTo("祭祀");
        }

        @Test
        @DisplayName("QI_FU常量存在")
        void testQiFu() {
            assertThat(YiJi.QI_FU).isEqualTo("祈福");
        }

        @Test
        @DisplayName("JIE_HUN常量存在")
        void testJieHun() {
            assertThat(YiJi.JIE_HUN).isEqualTo("结婚");
        }

        @Test
        @DisplayName("AN_ZANG常量存在")
        void testAnZang() {
            assertThat(YiJi.AN_ZANG).isEqualTo("安葬");
        }

        @Test
        @DisplayName("PO_TU常量存在")
        void testPoTu() {
            assertThat(YiJi.PO_TU).isEqualTo("破土");
        }

        @Test
        @DisplayName("CHU_HUAN常量存在")
        void testChuHuan() {
            assertThat(YiJi.CHU_HUAN).isEqualTo("出行");
        }
    }

    @Nested
    @DisplayName("of方法测试")
    class OfMethodTests {

        @Test
        @DisplayName("获取农历日期宜忌")
        void testOfLunarDate() {
            LunarDate lunar = new LunarDate(2024, 1, 1, false);
            YiJi yiji = YiJi.of(lunar);

            assertThat(yiji).isNotNull();
            assertThat(yiji.suitable()).isNotNull();
            assertThat(yiji.avoid()).isNotNull();
        }

        @Test
        @DisplayName("不同日期可能有不同宜忌")
        void testDifferentDates() {
            YiJi y1 = YiJi.of(new LunarDate(2024, 1, 1, false));
            YiJi y2 = YiJi.of(new LunarDate(2024, 1, 15, false));

            assertThat(y1).isNotNull();
            assertThat(y2).isNotNull();
            // 不同日期宜忌可能不同
        }
    }

    @Nested
    @DisplayName("isSuitable方法测试")
    class IsSuitableTests {

        @Test
        @DisplayName("检查活动是否适宜")
        void testIsSuitable() {
            YiJi yiji = new YiJi(List.of("祭祀", "嫁娶"), List.of("破土"));

            assertThat(yiji.isSuitable("祭祀")).isTrue();
            assertThat(yiji.isSuitable("嫁娶")).isTrue();
            assertThat(yiji.isSuitable("破土")).isFalse();
            assertThat(yiji.isSuitable("出行")).isFalse();
        }
    }

    @Nested
    @DisplayName("shouldAvoid方法测试")
    class ShouldAvoidTests {

        @Test
        @DisplayName("检查活动是否应避免")
        void testShouldAvoid() {
            YiJi yiji = new YiJi(List.of("祭祀"), List.of("破土", "安葬"));

            assertThat(yiji.shouldAvoid("破土")).isTrue();
            assertThat(yiji.shouldAvoid("安葬")).isTrue();
            assertThat(yiji.shouldAvoid("祭祀")).isFalse();
            assertThat(yiji.shouldAvoid("出行")).isFalse();
        }
    }

    @Nested
    @DisplayName("getSuitableString方法测试")
    class GetSuitableStringTests {

        @Test
        @DisplayName("获取宜字符串")
        void testGetSuitableString() {
            YiJi yiji = new YiJi(List.of("祭祀", "嫁娶", "出行"), List.of("破土"));

            String suitable = yiji.getSuitableString();
            assertThat(suitable).contains("祭祀");
            assertThat(suitable).contains("嫁娶");
            assertThat(suitable).contains("出行");
        }

        @Test
        @DisplayName("空宜列表")
        void testEmptySuitable() {
            YiJi yiji = new YiJi(List.of(), List.of("破土"));

            String suitable = yiji.getSuitableString();
            assertThat(suitable).isNotNull();
        }
    }

    @Nested
    @DisplayName("getAvoidString方法测试")
    class GetAvoidStringTests {

        @Test
        @DisplayName("获取忌字符串")
        void testGetAvoidString() {
            YiJi yiji = new YiJi(List.of("祭祀"), List.of("破土", "安葬"));

            String avoid = yiji.getAvoidString();
            assertThat(avoid).contains("破土");
            assertThat(avoid).contains("安葬");
        }

        @Test
        @DisplayName("空忌列表")
        void testEmptyAvoid() {
            YiJi yiji = new YiJi(List.of("祭祀"), List.of());

            String avoid = yiji.getAvoidString();
            assertThat(avoid).isNotNull();
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含宜和忌")
        void testToString() {
            YiJi yiji = new YiJi(List.of("祭祀"), List.of("破土"));

            String str = yiji.toString();
            assertThat(str).contains("宜");
            assertThat(str).contains("忌");
        }
    }
}
