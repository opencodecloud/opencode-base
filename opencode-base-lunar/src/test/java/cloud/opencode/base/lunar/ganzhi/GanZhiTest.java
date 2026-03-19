package cloud.opencode.base.lunar.ganzhi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * GanZhi (干支) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("GanZhi (干支) 测试")
class GanZhiTest {

    @Nested
    @DisplayName("Record基本测试")
    class RecordBasicTests {

        @Test
        @DisplayName("创建干支")
        void testCreate() {
            GanZhi ganZhi = new GanZhi(Gan.JIA, Zhi.ZI);

            assertThat(ganZhi.gan()).isEqualTo(Gan.JIA);
            assertThat(ganZhi.zhi()).isEqualTo(Zhi.ZI);
        }

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            GanZhi gz1 = new GanZhi(Gan.JIA, Zhi.ZI);
            GanZhi gz2 = new GanZhi(Gan.JIA, Zhi.ZI);
            GanZhi gz3 = new GanZhi(Gan.YI, Zhi.CHOU);

            assertThat(gz1).isEqualTo(gz2);
            assertThat(gz1).isNotEqualTo(gz3);
        }

        @Test
        @DisplayName("hashCode一致")
        void testHashCode() {
            GanZhi gz1 = new GanZhi(Gan.JIA, Zhi.ZI);
            GanZhi gz2 = new GanZhi(Gan.JIA, Zhi.ZI);

            assertThat(gz1.hashCode()).isEqualTo(gz2.hashCode());
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("甲子")
        void testJiaZi() {
            GanZhi ganZhi = new GanZhi(Gan.JIA, Zhi.ZI);
            assertThat(ganZhi.getName()).isEqualTo("甲子");
        }

        @Test
        @DisplayName("乙丑")
        void testYiChou() {
            GanZhi ganZhi = new GanZhi(Gan.YI, Zhi.CHOU);
            assertThat(ganZhi.getName()).isEqualTo("乙丑");
        }

        @Test
        @DisplayName("甲辰")
        void testJiaChen() {
            GanZhi ganZhi = new GanZhi(Gan.JIA, Zhi.CHEN);
            assertThat(ganZhi.getName()).isEqualTo("甲辰");
        }
    }

    @Nested
    @DisplayName("getCycleIndex方法测试")
    class GetCycleIndexTests {

        @Test
        @DisplayName("甲子是第0个")
        void testJiaZiIndex() {
            GanZhi ganZhi = new GanZhi(Gan.JIA, Zhi.ZI);
            assertThat(ganZhi.getCycleIndex()).isEqualTo(0);
        }

        @Test
        @DisplayName("乙丑是第1个")
        void testYiChouIndex() {
            GanZhi ganZhi = new GanZhi(Gan.YI, Zhi.CHOU);
            assertThat(ganZhi.getCycleIndex()).isEqualTo(1);
        }

        @Test
        @DisplayName("癸亥是第59个")
        void testGuiHaiIndex() {
            GanZhi ganZhi = new GanZhi(Gan.GUI, Zhi.HAI);
            assertThat(ganZhi.getCycleIndex()).isEqualTo(59);
        }
    }

    @Nested
    @DisplayName("ofYear方法测试")
    class OfYearTests {

        @Test
        @DisplayName("2024年是甲辰年")
        void test2024() {
            GanZhi ganZhi = GanZhi.ofYear(2024);

            assertThat(ganZhi.gan()).isEqualTo(Gan.JIA);
            assertThat(ganZhi.zhi()).isEqualTo(Zhi.CHEN);
            assertThat(ganZhi.getName()).isEqualTo("甲辰");
        }

        @Test
        @DisplayName("2023年是癸卯年")
        void test2023() {
            GanZhi ganZhi = GanZhi.ofYear(2023);

            assertThat(ganZhi.gan()).isEqualTo(Gan.GUI);
            assertThat(ganZhi.zhi()).isEqualTo(Zhi.MAO);
            assertThat(ganZhi.getName()).isEqualTo("癸卯");
        }

        @Test
        @DisplayName("1984年是甲子年")
        void test1984() {
            GanZhi ganZhi = GanZhi.ofYear(1984);

            assertThat(ganZhi.gan()).isEqualTo(Gan.JIA);
            assertThat(ganZhi.zhi()).isEqualTo(Zhi.ZI);
            assertThat(ganZhi.getName()).isEqualTo("甲子");
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 1950, 2000, 2050, 2100})
        @DisplayName("所有年份返回有效干支")
        void testAllYears(int year) {
            GanZhi ganZhi = GanZhi.ofYear(year);
            assertThat(ganZhi).isNotNull();
            assertThat(ganZhi.gan()).isNotNull();
            assertThat(ganZhi.zhi()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ofMonth方法测试")
    class OfMonthTests {

        @Test
        @DisplayName("获取月干支")
        void testOfMonth() {
            GanZhi ganZhi = GanZhi.ofMonth(2024, 1);

            assertThat(ganZhi).isNotNull();
            assertThat(ganZhi.gan()).isNotNull();
            assertThat(ganZhi.zhi()).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
        @DisplayName("所有月份返回有效干支")
        void testAllMonths(int month) {
            GanZhi ganZhi = GanZhi.ofMonth(2024, month);
            assertThat(ganZhi).isNotNull();
        }
    }

    @Nested
    @DisplayName("ofDay方法测试")
    class OfDayTests {

        @Test
        @DisplayName("获取日干支")
        void testOfDay() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            GanZhi ganZhi = GanZhi.ofDay(date);

            assertThat(ganZhi).isNotNull();
            assertThat(ganZhi.gan()).isNotNull();
            assertThat(ganZhi.zhi()).isNotNull();
        }

        @Test
        @DisplayName("不同日期返回不同干支")
        void testDifferentDays() {
            GanZhi gz1 = GanZhi.ofDay(LocalDate.of(2024, 1, 1));
            GanZhi gz2 = GanZhi.ofDay(LocalDate.of(2024, 1, 2));

            assertThat(gz1).isNotEqualTo(gz2);
        }
    }

    @Nested
    @DisplayName("ofHour方法测试")
    class OfHourTests {

        @Test
        @DisplayName("获取时干支")
        void testOfHour() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            GanZhi dayGanZhi = GanZhi.ofDay(date);
            GanZhi ganZhi = GanZhi.ofHour(dayGanZhi, 0);

            assertThat(ganZhi).isNotNull();
            assertThat(ganZhi.gan()).isNotNull();
            assertThat(ganZhi.zhi()).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23})
        @DisplayName("不同小时返回有效干支")
        void testDifferentHours(int hour) {
            GanZhi dayGanZhi = GanZhi.ofDay(LocalDate.of(2024, 1, 1));
            GanZhi ganZhi = GanZhi.ofHour(dayGanZhi, hour);
            assertThat(ganZhi).isNotNull();
        }
    }

    @Nested
    @DisplayName("ofCycleIndex方法测试")
    class OfCycleIndexTests {

        @Test
        @DisplayName("索引0返回甲子")
        void testIndex0() {
            GanZhi ganZhi = GanZhi.ofCycleIndex(0);

            assertThat(ganZhi.getName()).isEqualTo("甲子");
        }

        @Test
        @DisplayName("索引59返回癸亥")
        void testIndex59() {
            GanZhi ganZhi = GanZhi.ofCycleIndex(59);

            assertThat(ganZhi.getName()).isEqualTo("癸亥");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 9, 19, 29, 39, 49, 59})
        @DisplayName("所有有效索引返回干支")
        void testValidIndexes(int index) {
            GanZhi ganZhi = GanZhi.ofCycleIndex(index);
            assertThat(ganZhi).isNotNull();
            assertThat(ganZhi.getCycleIndex()).isEqualTo(index);
        }
    }

    @Nested
    @DisplayName("next方法测试")
    class NextTests {

        @Test
        @DisplayName("甲子的下一个是乙丑")
        void testJiaZiNext() {
            GanZhi ganZhi = new GanZhi(Gan.JIA, Zhi.ZI);
            GanZhi next = ganZhi.next();

            assertThat(next.getName()).isEqualTo("乙丑");
        }

        @Test
        @DisplayName("癸亥的下一个是甲子")
        void testGuiHaiNext() {
            GanZhi ganZhi = new GanZhi(Gan.GUI, Zhi.HAI);
            GanZhi next = ganZhi.next();

            assertThat(next.getName()).isEqualTo("甲子");
        }
    }

    @Nested
    @DisplayName("previous方法测试")
    class PreviousTests {

        @Test
        @DisplayName("乙丑的上一个是甲子")
        void testYiChouPrevious() {
            GanZhi ganZhi = new GanZhi(Gan.YI, Zhi.CHOU);
            GanZhi prev = ganZhi.previous();

            assertThat(prev.getName()).isEqualTo("甲子");
        }

        @Test
        @DisplayName("甲子的上一个是癸亥")
        void testJiaZiPrevious() {
            GanZhi ganZhi = new GanZhi(Gan.JIA, Zhi.ZI);
            GanZhi prev = ganZhi.previous();

            assertThat(prev.getName()).isEqualTo("癸亥");
        }
    }

    @Nested
    @DisplayName("60甲子循环测试")
    class SixtyJiaZiCycleTests {

        @Test
        @DisplayName("next循环60次回到起点")
        void testNextCycle() {
            GanZhi current = new GanZhi(Gan.JIA, Zhi.ZI);
            for (int i = 0; i < 60; i++) {
                current = current.next();
            }
            assertThat(current.getName()).isEqualTo("甲子");
        }

        @Test
        @DisplayName("previous循环60次回到起点")
        void testPreviousCycle() {
            GanZhi current = new GanZhi(Gan.JIA, Zhi.ZI);
            for (int i = 0; i < 60; i++) {
                current = current.previous();
            }
            assertThat(current.getName()).isEqualTo("甲子");
        }

        @Test
        @DisplayName("60个干支索引从0到59")
        void testAllSixtyIndexes() {
            GanZhi current = new GanZhi(Gan.JIA, Zhi.ZI);
            for (int i = 0; i < 60; i++) {
                assertThat(current.getCycleIndex()).isEqualTo(i);
                current = current.next();
            }
        }
    }
}
