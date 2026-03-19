package cloud.opencode.base.date.lunar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SolarTerm 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("SolarTerm 测试")
class SolarTermTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(index) 按索引获取节气")
        void testOfIndex() {
            assertThat(SolarTerm.of(0)).isEqualTo(SolarTerm.LICHUN);
            assertThat(SolarTerm.of(23)).isEqualTo(SolarTerm.DAHAN);
        }

        @Test
        @DisplayName("of(index) 索引超出范围抛出异常")
        void testOfIndexOutOfRange() {
            assertThatThrownBy(() -> SolarTerm.of(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Index must be between 0 and 23");
            assertThatThrownBy(() -> SolarTerm.of(24))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Index must be between 0 and 23");
        }

        @Test
        @DisplayName("fromChineseName() 按中文名称获取节气")
        void testFromChineseName() {
            assertThat(SolarTerm.fromChineseName("立春")).isEqualTo(SolarTerm.LICHUN);
            assertThat(SolarTerm.fromChineseName("大寒")).isEqualTo(SolarTerm.DAHAN);
            assertThat(SolarTerm.fromChineseName("不存在")).isNull();
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("getIndex() 获取索引")
        void testGetIndex() {
            assertThat(SolarTerm.LICHUN.getIndex()).isEqualTo(0);
            assertThat(SolarTerm.CHUNFEN.getIndex()).isEqualTo(3);
            assertThat(SolarTerm.DAHAN.getIndex()).isEqualTo(23);
        }

        @Test
        @DisplayName("getChineseName() 获取中文名称")
        void testGetChineseName() {
            assertThat(SolarTerm.LICHUN.getChineseName()).isEqualTo("立春");
            assertThat(SolarTerm.QINGMING.getChineseName()).isEqualTo("清明");
            assertThat(SolarTerm.DONGZHI.getChineseName()).isEqualTo("冬至");
        }

        @Test
        @DisplayName("getEnglishName() 获取英文名称")
        void testGetEnglishName() {
            assertThat(SolarTerm.LICHUN.getEnglishName()).isEqualTo("Beginning of Spring");
            assertThat(SolarTerm.QINGMING.getEnglishName()).isEqualTo("Clear and Bright");
            assertThat(SolarTerm.DONGZHI.getEnglishName()).isEqualTo("Winter Solstice");
        }

        @Test
        @DisplayName("getSunLongitude() 获取太阳黄经")
        void testGetSunLongitude() {
            assertThat(SolarTerm.LICHUN.getSunLongitude()).isEqualTo(315);
            assertThat(SolarTerm.CHUNFEN.getSunLongitude()).isEqualTo(0);
            assertThat(SolarTerm.XIAZHI.getSunLongitude()).isEqualTo(90);
            assertThat(SolarTerm.QIUFEN.getSunLongitude()).isEqualTo(180);
            assertThat(SolarTerm.DONGZHI.getSunLongitude()).isEqualTo(270);
        }
    }

    @Nested
    @DisplayName("导航方法测试")
    class NavigationTests {

        @Test
        @DisplayName("next() 获取下一个节气")
        void testNext() {
            assertThat(SolarTerm.LICHUN.next()).isEqualTo(SolarTerm.YUSHUI);
            assertThat(SolarTerm.DAHAN.next()).isEqualTo(SolarTerm.LICHUN); // wraps around
        }

        @Test
        @DisplayName("previous() 获取上一个节气")
        void testPrevious() {
            assertThat(SolarTerm.YUSHUI.previous()).isEqualTo(SolarTerm.LICHUN);
            assertThat(SolarTerm.LICHUN.previous()).isEqualTo(SolarTerm.DAHAN); // wraps around
        }
    }

    @Nested
    @DisplayName("类型检查测试")
    class TypeCheckTests {

        @Test
        @DisplayName("isMajorTerm() 检查是否为中气")
        void testIsMajorTerm() {
            // Major terms have odd indices (1, 3, 5, ...)
            assertThat(SolarTerm.LICHUN.isMajorTerm()).isFalse();  // index 0
            assertThat(SolarTerm.YUSHUI.isMajorTerm()).isTrue();   // index 1
            assertThat(SolarTerm.CHUNFEN.isMajorTerm()).isTrue();  // index 3
        }

        @Test
        @DisplayName("isMinorTerm() 检查是否为节气")
        void testIsMinorTerm() {
            // Minor terms have even indices (0, 2, 4, ...)
            assertThat(SolarTerm.LICHUN.isMinorTerm()).isTrue();   // index 0
            assertThat(SolarTerm.YUSHUI.isMinorTerm()).isFalse();  // index 1
            assertThat(SolarTerm.JINGZHE.isMinorTerm()).isTrue();  // index 2
        }

        @Test
        @DisplayName("getSeason() 获取季节")
        void testGetSeason() {
            // Spring: 0-5
            assertThat(SolarTerm.LICHUN.getSeason()).isEqualTo(0);
            assertThat(SolarTerm.GUYU.getSeason()).isEqualTo(0);
            // Summer: 6-11
            assertThat(SolarTerm.LIXIA.getSeason()).isEqualTo(1);
            assertThat(SolarTerm.DASHU.getSeason()).isEqualTo(1);
            // Autumn: 12-17
            assertThat(SolarTerm.LIQIU.getSeason()).isEqualTo(2);
            assertThat(SolarTerm.SHUANGJIANG.getSeason()).isEqualTo(2);
            // Winter: 18-23
            assertThat(SolarTerm.LIDONG.getSeason()).isEqualTo(3);
            assertThat(SolarTerm.DAHAN.getSeason()).isEqualTo(3);
        }

        @Test
        @DisplayName("getSeasonName() 获取季节名称")
        void testGetSeasonName() {
            assertThat(SolarTerm.LICHUN.getSeasonName()).isEqualTo("春");
            assertThat(SolarTerm.LIXIA.getSeasonName()).isEqualTo("夏");
            assertThat(SolarTerm.LIQIU.getSeasonName()).isEqualTo("秋");
            assertThat(SolarTerm.LIDONG.getSeasonName()).isEqualTo("冬");
        }
    }

    @Nested
    @DisplayName("枚举测试")
    class EnumTests {

        @Test
        @DisplayName("values() 所有24个节气")
        void testValues() {
            assertThat(SolarTerm.values()).hasSize(24);
        }

        @Test
        @DisplayName("valueOf() 按名称获取")
        void testValueOf() {
            assertThat(SolarTerm.valueOf("LICHUN")).isEqualTo(SolarTerm.LICHUN);
            assertThat(SolarTerm.valueOf("QINGMING")).isEqualTo(SolarTerm.QINGMING);
        }

        @Test
        @DisplayName("春季节气顺序")
        void testSpringTerms() {
            assertThat(SolarTerm.LICHUN.getIndex()).isEqualTo(0);
            assertThat(SolarTerm.YUSHUI.getIndex()).isEqualTo(1);
            assertThat(SolarTerm.JINGZHE.getIndex()).isEqualTo(2);
            assertThat(SolarTerm.CHUNFEN.getIndex()).isEqualTo(3);
            assertThat(SolarTerm.QINGMING.getIndex()).isEqualTo(4);
            assertThat(SolarTerm.GUYU.getIndex()).isEqualTo(5);
        }

        @Test
        @DisplayName("夏季节气顺序")
        void testSummerTerms() {
            assertThat(SolarTerm.LIXIA.getIndex()).isEqualTo(6);
            assertThat(SolarTerm.XIAOMAN.getIndex()).isEqualTo(7);
            assertThat(SolarTerm.MANGZHONG.getIndex()).isEqualTo(8);
            assertThat(SolarTerm.XIAZHI.getIndex()).isEqualTo(9);
            assertThat(SolarTerm.XIAOSHU.getIndex()).isEqualTo(10);
            assertThat(SolarTerm.DASHU.getIndex()).isEqualTo(11);
        }

        @Test
        @DisplayName("秋季节气顺序")
        void testAutumnTerms() {
            assertThat(SolarTerm.LIQIU.getIndex()).isEqualTo(12);
            assertThat(SolarTerm.CHUSHU.getIndex()).isEqualTo(13);
            assertThat(SolarTerm.BAILU.getIndex()).isEqualTo(14);
            assertThat(SolarTerm.QIUFEN.getIndex()).isEqualTo(15);
            assertThat(SolarTerm.HANLU.getIndex()).isEqualTo(16);
            assertThat(SolarTerm.SHUANGJIANG.getIndex()).isEqualTo(17);
        }

        @Test
        @DisplayName("冬季节气顺序")
        void testWinterTerms() {
            assertThat(SolarTerm.LIDONG.getIndex()).isEqualTo(18);
            assertThat(SolarTerm.XIAOXUE.getIndex()).isEqualTo(19);
            assertThat(SolarTerm.DAXUE.getIndex()).isEqualTo(20);
            assertThat(SolarTerm.DONGZHI.getIndex()).isEqualTo(21);
            assertThat(SolarTerm.XIAOHAN.getIndex()).isEqualTo(22);
            assertThat(SolarTerm.DAHAN.getIndex()).isEqualTo(23);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(SolarTerm.LICHUN.toString()).isEqualTo("立春 (Beginning of Spring)");
            assertThat(SolarTerm.QINGMING.toString()).isEqualTo("清明 (Clear and Bright)");
        }
    }
}
