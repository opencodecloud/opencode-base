package cloud.opencode.base.lunar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LunarMonth 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.3
 */
@DisplayName("LunarMonth 测试")
class LunarMonthTest {

    @Nested
    @DisplayName("构造和工厂方法测试")
    class ConstructionTests {

        @Test
        @DisplayName("of(year, month)创建非闰月")
        void testOfTwoArgs() {
            LunarMonth m = LunarMonth.of(2024, 1);

            assertThat(m.year()).isEqualTo(2024);
            assertThat(m.month()).isEqualTo(1);
            assertThat(m.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("of(year, month, true)创建闰月")
        void testOfThreeArgs() {
            LunarMonth m = LunarMonth.of(2020, 4, true);

            assertThat(m.year()).isEqualTo(2020);
            assertThat(m.month()).isEqualTo(4);
            assertThat(m.isLeapMonth()).isTrue();
        }

        @Test
        @DisplayName("年份越界抛异常")
        void testYearOutOfRange() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LunarMonth.of(1899, 1));
        }

        @Test
        @DisplayName("月份越界抛异常")
        void testMonthOutOfRange() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LunarMonth.of(2024, 0));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LunarMonth.of(2024, 13));
        }

        @Test
        @DisplayName("不存在的闰月抛异常")
        void testInvalidLeapMonth() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LunarMonth.of(2024, 1, true))
                    .withMessageContaining("no leap month");
        }
    }

    @Nested
    @DisplayName("getDays测试")
    class GetDaysTests {

        @Test
        @DisplayName("月天数为29或30")
        void testDaysIs29Or30() {
            LunarMonth m = LunarMonth.of(2024, 1);
            assertThat(m.getDays()).isIn(29, 30);
        }

        @Test
        @DisplayName("闰月天数为29或30")
        void testLeapMonthDays() {
            LunarMonth m = LunarMonth.of(2020, 4, true);
            assertThat(m.getDays()).isIn(29, 30);
        }
    }

    @Nested
    @DisplayName("大月小月测试")
    class BigSmallTests {

        @Test
        @DisplayName("大月30天")
        void testBigMonth() {
            // Find a 30-day month
            LunarMonth m = LunarMonth.of(2024, 2); // 2024 month 2 is 30 days
            assertThat(m.isBig()).isTrue();
            assertThat(m.isSmall()).isFalse();
            assertThat(m.getDays()).isEqualTo(30);
        }

        @Test
        @DisplayName("小月29天")
        void testSmallMonth() {
            // 2024 month 1 is 29 days
            LunarMonth m = LunarMonth.of(2024, 1);
            assertThat(m.isSmall()).isTrue();
            assertThat(m.isBig()).isFalse();
            assertThat(m.getDays()).isEqualTo(29);
        }

        @Test
        @DisplayName("isBig和isSmall互斥")
        void testBigSmallExclusive() {
            for (int month = 1; month <= 12; month++) {
                LunarMonth m = LunarMonth.of(2024, month);
                assertThat(m.isBig() ^ m.isSmall()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("getName测试")
    class GetNameTests {

        @Test
        @DisplayName("正月")
        void testFirstMonth() {
            assertThat(LunarMonth.of(2024, 1).getName()).isEqualTo("正月");
        }

        @Test
        @DisplayName("腊月")
        void testLastMonth() {
            assertThat(LunarMonth.of(2024, 12).getName()).isEqualTo("腊月");
        }

        @Test
        @DisplayName("闰四月")
        void testLeapMonth() {
            assertThat(LunarMonth.of(2020, 4, true).getName()).isEqualTo("闰四月");
        }

        @Test
        @DisplayName("二月")
        void testSecondMonth() {
            assertThat(LunarMonth.of(2024, 2).getName()).isEqualTo("二月");
        }

        @Test
        @DisplayName("冬月")
        void testEleventhMonth() {
            assertThat(LunarMonth.of(2024, 11).getName()).isEqualTo("冬月");
        }

        @Test
        @DisplayName("toString等于getName")
        void testToString() {
            LunarMonth m = LunarMonth.of(2024, 1);
            assertThat(m.toString()).isEqualTo(m.getName());
        }
    }

    @Nested
    @DisplayName("getFirstDay和getLastDay测试")
    class FirstLastDayTests {

        @Test
        @DisplayName("首日为初一")
        void testFirstDay() {
            LunarMonth m = LunarMonth.of(2024, 1);
            LunarDate first = m.getFirstDay();

            assertThat(first.year()).isEqualTo(2024);
            assertThat(first.month()).isEqualTo(1);
            assertThat(first.day()).isEqualTo(1);
            assertThat(first.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("末日等于天数")
        void testLastDay() {
            LunarMonth m = LunarMonth.of(2024, 1);
            LunarDate last = m.getLastDay();

            assertThat(last.day()).isEqualTo(m.getDays());
        }

        @Test
        @DisplayName("闰月首日和末日")
        void testLeapMonthFirstLastDay() {
            LunarMonth m = LunarMonth.of(2020, 4, true);
            LunarDate first = m.getFirstDay();
            LunarDate last = m.getLastDay();

            assertThat(first.isLeapMonth()).isTrue();
            assertThat(last.isLeapMonth()).isTrue();
            assertThat(first.day()).isEqualTo(1);
            assertThat(last.day()).isEqualTo(m.getDays());
        }

        @Test
        @DisplayName("首日到末日的天数差等于getDays-1")
        void testFirstToLastDays() {
            LunarMonth m = LunarMonth.of(2024, 3);
            LunarDate first = m.getFirstDay();
            LunarDate last = m.getLastDay();

            assertThat(first.daysUntil(last)).isEqualTo(m.getDays() - 1);
        }
    }

    @Nested
    @DisplayName("next方法测试")
    class NextTests {

        @Test
        @DisplayName("正月下一月为二月")
        void testNextRegularMonth() {
            LunarMonth m = LunarMonth.of(2024, 1);
            LunarMonth next = m.next();

            assertThat(next.year()).isEqualTo(2024);
            assertThat(next.month()).isEqualTo(2);
            assertThat(next.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("腊月下一月为次年正月")
        void testNextFromDecember() {
            LunarMonth m = LunarMonth.of(2024, 12);
            LunarMonth next = m.next();

            assertThat(next.year()).isEqualTo(2025);
            assertThat(next.month()).isEqualTo(1);
            assertThat(next.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("有闰月时常规月下一月为闰月")
        void testNextToLeapMonth() {
            // 2020 has leap month 4
            LunarMonth m = LunarMonth.of(2020, 4);
            LunarMonth next = m.next();

            assertThat(next.month()).isEqualTo(4);
            assertThat(next.isLeapMonth()).isTrue();
        }

        @Test
        @DisplayName("闰月下一月为下一常规月")
        void testNextFromLeapMonth() {
            LunarMonth m = LunarMonth.of(2020, 4, true);
            LunarMonth next = m.next();

            assertThat(next.month()).isEqualTo(5);
            assertThat(next.isLeapMonth()).isFalse();
        }
    }

    @Nested
    @DisplayName("previous方法测试")
    class PreviousTests {

        @Test
        @DisplayName("二月上一月为正月")
        void testPreviousRegularMonth() {
            LunarMonth m = LunarMonth.of(2024, 2);
            LunarMonth prev = m.previous();

            assertThat(prev.year()).isEqualTo(2024);
            assertThat(prev.month()).isEqualTo(1);
            assertThat(prev.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("正月上一月为上年腊月")
        void testPreviousFromJanuary() {
            LunarMonth m = LunarMonth.of(2024, 1);
            LunarMonth prev = m.previous();

            assertThat(prev.year()).isEqualTo(2023);
            assertThat(prev.month()).isEqualTo(12);
        }

        @Test
        @DisplayName("闰月上一月为同月常规版")
        void testPreviousFromLeapMonth() {
            LunarMonth m = LunarMonth.of(2020, 4, true);
            LunarMonth prev = m.previous();

            assertThat(prev.month()).isEqualTo(4);
            assertThat(prev.isLeapMonth()).isFalse();
        }

        @Test
        @DisplayName("闰月之后的常规月上一月为闰月")
        void testPreviousToLeapMonth() {
            // 2020 has leap month 4, so month 5's previous should be leap 4
            LunarMonth m = LunarMonth.of(2020, 5);
            LunarMonth prev = m.previous();

            assertThat(prev.month()).isEqualTo(4);
            assertThat(prev.isLeapMonth()).isTrue();
        }
    }

    @Nested
    @DisplayName("next和previous互逆测试")
    class NavigationRoundtripTests {

        @Test
        @DisplayName("next再previous回到原月")
        void testNextThenPrevious() {
            LunarMonth m = LunarMonth.of(2024, 6);
            LunarMonth roundtrip = m.next().previous();

            assertThat(roundtrip).isEqualTo(m);
        }

        @Test
        @DisplayName("previous再next回到原月")
        void testPreviousThenNext() {
            LunarMonth m = LunarMonth.of(2024, 6);
            LunarMonth roundtrip = m.previous().next();

            assertThat(roundtrip).isEqualTo(m);
        }

        @Test
        @DisplayName("闰月导航往返")
        void testLeapMonthRoundtrip() {
            LunarMonth m = LunarMonth.of(2020, 4, true);
            assertThat(m.next().previous()).isEqualTo(m);
            assertThat(m.previous().next()).isEqualTo(m);
        }
    }

    @Nested
    @DisplayName("Record基本测试")
    class RecordTests {

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            LunarMonth m1 = LunarMonth.of(2024, 1);
            LunarMonth m2 = LunarMonth.of(2024, 1);
            LunarMonth m3 = LunarMonth.of(2024, 2);

            assertThat(m1).isEqualTo(m2);
            assertThat(m1).isNotEqualTo(m3);
        }

        @Test
        @DisplayName("闰月和常规月不相等")
        void testLeapNotEqualRegular() {
            LunarMonth regular = LunarMonth.of(2020, 4);
            LunarMonth leap = LunarMonth.of(2020, 4, true);

            assertThat(regular).isNotEqualTo(leap);
        }

        @Test
        @DisplayName("hashCode一致")
        void testHashCode() {
            LunarMonth m1 = LunarMonth.of(2024, 1);
            LunarMonth m2 = LunarMonth.of(2024, 1);

            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }
    }
}
