package cloud.opencode.base.date.lunar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Lunar 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Lunar 测试")
class LunarTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(year, month, day, leapMonth) 创建农历日期")
        void testOfFourParams() {
            Lunar lunar = Lunar.of(2024, 1, 15, false);
            assertThat(lunar.getYear()).isEqualTo(2024);
            assertThat(lunar.getMonth()).isEqualTo(1);
            assertThat(lunar.getDay()).isEqualTo(15);
            assertThat(lunar.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("of(year, month, day, true) 创建闰月日期")
        void testOfLeapMonth() {
            Lunar lunar = Lunar.of(2024, 4, 10, true);
            assertThat(lunar.isLeapMonth()).isTrue();
        }

        @Test
        @DisplayName("of(year, month, day) 创建非闰月日期")
        void testOfThreeParams() {
            Lunar lunar = Lunar.of(2024, 1, 1);
            assertThat(lunar.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("of() 年份超出范围抛出异常")
        void testOfYearOutOfRange() {
            assertThatThrownBy(() -> Lunar.of(1899, 1, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Year must be between 1900 and 2100");
            assertThatThrownBy(() -> Lunar.of(2101, 1, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Year must be between 1900 and 2100");
        }

        @Test
        @DisplayName("of() 月份超出范围抛出异常")
        void testOfMonthOutOfRange() {
            assertThatThrownBy(() -> Lunar.of(2024, 0, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Month must be between 1 and 12");
            assertThatThrownBy(() -> Lunar.of(2024, 13, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Month must be between 1 and 12");
        }

        @Test
        @DisplayName("of() 日期超出范围抛出异常")
        void testOfDayOutOfRange() {
            assertThatThrownBy(() -> Lunar.of(2024, 1, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Day must be between 1 and 30");
            assertThatThrownBy(() -> Lunar.of(2024, 1, 31))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Day must be between 1 and 30");
        }
    }

    @Nested
    @DisplayName("生肖方法测试")
    class ZodiacTests {

        @Test
        @DisplayName("getZodiac() 获取生肖")
        void testGetZodiac() {
            // 2024 is Year of the Dragon (龙)
            assertThat(Lunar.of(2024, 1, 1).getZodiac()).isEqualTo("龙");
            // 2023 is Year of the Rabbit (兔)
            assertThat(Lunar.of(2023, 1, 1).getZodiac()).isEqualTo("兔");
            // 2025 is Year of the Snake (蛇)
            assertThat(Lunar.of(2025, 1, 1).getZodiac()).isEqualTo("蛇");
        }

        @Test
        @DisplayName("getZodiac() 完整生肖周期")
        void testGetZodiacFullCycle() {
            String[] expectedZodiac = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
            // 2020 is Year of the Rat
            for (int i = 0; i < 12; i++) {
                assertThat(Lunar.of(2020 + i, 1, 1).getZodiac()).isEqualTo(expectedZodiac[i]);
            }
        }
    }

    @Nested
    @DisplayName("天干地支测试")
    class StemBranchTests {

        @Test
        @DisplayName("getHeavenlyStem() 获取天干")
        void testGetHeavenlyStem() {
            // 2024 is 甲辰年
            assertThat(Lunar.of(2024, 1, 1).getHeavenlyStem()).isEqualTo("甲");
        }

        @Test
        @DisplayName("getEarthlyBranch() 获取地支")
        void testGetEarthlyBranch() {
            // 2024 is 甲辰年
            assertThat(Lunar.of(2024, 1, 1).getEarthlyBranch()).isEqualTo("辰");
        }

        @Test
        @DisplayName("getStemBranchYear() 获取干支纪年")
        void testGetStemBranchYear() {
            assertThat(Lunar.of(2024, 1, 1).getStemBranchYear()).isEqualTo("甲辰");
            assertThat(Lunar.of(2023, 1, 1).getStemBranchYear()).isEqualTo("癸卯");
        }
    }

    @Nested
    @DisplayName("中文表示测试")
    class ChineseRepresentationTests {

        @Test
        @DisplayName("getChineseYear() 获取中文年份")
        void testGetChineseYear() {
            assertThat(Lunar.of(2024, 1, 1).getChineseYear()).isEqualTo("二〇二四");
            assertThat(Lunar.of(1900, 1, 1).getChineseYear()).isEqualTo("一九〇〇");
        }

        @Test
        @DisplayName("getChineseMonth() 获取中文月份")
        void testGetChineseMonth() {
            assertThat(Lunar.of(2024, 1, 1).getChineseMonth()).isEqualTo("正月");
            assertThat(Lunar.of(2024, 2, 1).getChineseMonth()).isEqualTo("二月");
            assertThat(Lunar.of(2024, 11, 1).getChineseMonth()).isEqualTo("冬月");
            assertThat(Lunar.of(2024, 12, 1).getChineseMonth()).isEqualTo("腊月");
        }

        @Test
        @DisplayName("getChineseMonth() 闰月表示")
        void testGetChineseMonthLeap() {
            assertThat(Lunar.of(2024, 4, 1, true).getChineseMonth()).isEqualTo("闰四月");
        }

        @Test
        @DisplayName("getChineseDay() 获取中文日期")
        void testGetChineseDay() {
            assertThat(Lunar.of(2024, 1, 1).getChineseDay()).isEqualTo("初一");
            assertThat(Lunar.of(2024, 1, 2).getChineseDay()).isEqualTo("初二");
            assertThat(Lunar.of(2024, 1, 10).getChineseDay()).isEqualTo("初十");
            assertThat(Lunar.of(2024, 1, 11).getChineseDay()).isEqualTo("十一");
            assertThat(Lunar.of(2024, 1, 15).getChineseDay()).isEqualTo("十五");
            assertThat(Lunar.of(2024, 1, 20).getChineseDay()).isEqualTo("二十");
            assertThat(Lunar.of(2024, 1, 21).getChineseDay()).isEqualTo("廿一");
            assertThat(Lunar.of(2024, 1, 30).getChineseDay()).isEqualTo("三十");
        }

        @Test
        @DisplayName("toChinese() 完整中文表示")
        void testToChinese() {
            assertThat(Lunar.of(2024, 1, 1).toChinese()).isEqualTo("二〇二四年正月初一");
            assertThat(Lunar.of(2024, 12, 30).toChinese()).isEqualTo("二〇二四年腊月三十");
        }

        @Test
        @DisplayName("toStemBranch() 干支表示")
        void testToStemBranch() {
            String result = Lunar.of(2024, 1, 1).toStemBranch();
            assertThat(result).contains("甲辰年");
            assertThat(result).contains("正月");
            assertThat(result).contains("初一");
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 按年比较")
        void testCompareToByYear() {
            Lunar l1 = Lunar.of(2023, 1, 1);
            Lunar l2 = Lunar.of(2024, 1, 1);
            assertThat(l1.compareTo(l2)).isLessThan(0);
            assertThat(l2.compareTo(l1)).isGreaterThan(0);
        }

        @Test
        @DisplayName("compareTo() 按月比较")
        void testCompareToByMonth() {
            Lunar l1 = Lunar.of(2024, 1, 1);
            Lunar l2 = Lunar.of(2024, 2, 1);
            assertThat(l1.compareTo(l2)).isLessThan(0);
        }

        @Test
        @DisplayName("compareTo() 按日比较")
        void testCompareToByDay() {
            Lunar l1 = Lunar.of(2024, 1, 1);
            Lunar l2 = Lunar.of(2024, 1, 15);
            assertThat(l1.compareTo(l2)).isLessThan(0);
        }

        @Test
        @DisplayName("compareTo() 闰月在正常月之后")
        void testCompareToLeapMonth() {
            Lunar normal = Lunar.of(2024, 4, 15, false);
            Lunar leap = Lunar.of(2024, 4, 15, true);
            assertThat(normal.compareTo(leap)).isLessThan(0);
        }

        @Test
        @DisplayName("compareTo() 相等")
        void testCompareToEqual() {
            Lunar l1 = Lunar.of(2024, 1, 1);
            Lunar l2 = Lunar.of(2024, 1, 1);
            assertThat(l1.compareTo(l2)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等判断")
        void testEquals() {
            Lunar l1 = Lunar.of(2024, 1, 1);
            Lunar l2 = Lunar.of(2024, 1, 1);
            Lunar l3 = Lunar.of(2024, 1, 2);
            Lunar l4 = Lunar.of(2024, 1, 1, true);

            assertThat(l1).isEqualTo(l2);
            assertThat(l1).isNotEqualTo(l3);
            assertThat(l1).isNotEqualTo(l4); // different leap month
            assertThat(l1).isEqualTo(l1);
            assertThat(l1).isNotEqualTo(null);
            assertThat(l1).isNotEqualTo("2024-1-1");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            Lunar l1 = Lunar.of(2024, 1, 1);
            Lunar l2 = Lunar.of(2024, 1, 1);
            assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(Lunar.of(2024, 1, 1).toString()).isEqualTo("Lunar[2024-01-01]");
        }

        @Test
        @DisplayName("toString() 闰月标记")
        void testToStringLeapMonth() {
            assertThat(Lunar.of(2024, 4, 1, true).toString()).isEqualTo("Lunar[2024-04-01 (leap)]");
        }
    }
}
