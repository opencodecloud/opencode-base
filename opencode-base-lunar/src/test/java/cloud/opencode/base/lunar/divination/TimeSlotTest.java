package cloud.opencode.base.lunar.divination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

/**
 * TimeSlot (时辰) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("TimeSlot (时辰) 测试")
class TimeSlotTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含12个时辰")
        void testTwelveSlots() {
            assertThat(TimeSlot.values()).hasSize(12);
        }

        @Test
        @DisplayName("时辰顺序正确")
        void testSlotOrder() {
            TimeSlot[] slots = TimeSlot.values();
            assertThat(slots[0]).isEqualTo(TimeSlot.ZI);
            assertThat(slots[1]).isEqualTo(TimeSlot.CHOU);
            assertThat(slots[2]).isEqualTo(TimeSlot.YIN);
            assertThat(slots[3]).isEqualTo(TimeSlot.MAO);
            assertThat(slots[4]).isEqualTo(TimeSlot.CHEN);
            assertThat(slots[5]).isEqualTo(TimeSlot.SI);
            assertThat(slots[6]).isEqualTo(TimeSlot.WU);
            assertThat(slots[7]).isEqualTo(TimeSlot.WEI);
            assertThat(slots[8]).isEqualTo(TimeSlot.SHEN);
            assertThat(slots[9]).isEqualTo(TimeSlot.YOU);
            assertThat(slots[10]).isEqualTo(TimeSlot.XU);
            assertThat(slots[11]).isEqualTo(TimeSlot.HAI);
        }
    }

    @Nested
    @DisplayName("getChinese方法测试")
    class GetChineseTests {

        @Test
        @DisplayName("子时")
        void testZi() {
            assertThat(TimeSlot.ZI.getChinese()).isEqualTo("子");
        }

        @Test
        @DisplayName("午时")
        void testWu() {
            assertThat(TimeSlot.WU.getChinese()).isEqualTo("午");
        }

        @ParameterizedTest
        @EnumSource(TimeSlot.class)
        @DisplayName("所有时辰名称不为空")
        void testAllNamesNotEmpty(TimeSlot slot) {
            assertThat(slot.getChinese()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("fromHour方法测试")
    class FromHourTests {

        @Test
        @DisplayName("0点是子时")
        void testMidnight() {
            assertThat(TimeSlot.fromHour(0)).isEqualTo(TimeSlot.ZI);
        }

        @Test
        @DisplayName("23点是子时")
        void test23() {
            assertThat(TimeSlot.fromHour(23)).isEqualTo(TimeSlot.ZI);
        }

        @Test
        @DisplayName("12点是午时")
        void testNoon() {
            assertThat(TimeSlot.fromHour(12)).isEqualTo(TimeSlot.WU);
        }

        @Test
        @DisplayName("1点是丑时")
        void test1() {
            assertThat(TimeSlot.fromHour(1)).isEqualTo(TimeSlot.CHOU);
        }

        @Test
        @DisplayName("3点是寅时")
        void test3() {
            assertThat(TimeSlot.fromHour(3)).isEqualTo(TimeSlot.YIN);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23})
        @DisplayName("所有小时返回有效时辰")
        void testAllHours(int hour) {
            TimeSlot slot = TimeSlot.fromHour(hour);
            assertThat(slot).isNotNull();
        }
    }

    @Nested
    @DisplayName("fromTime方法测试")
    class FromTimeTests {

        @Test
        @DisplayName("LocalTime 00:00 是子时")
        void testMidnight() {
            assertThat(TimeSlot.fromTime(LocalTime.of(0, 0))).isEqualTo(TimeSlot.ZI);
        }

        @Test
        @DisplayName("LocalTime 12:30 是午时")
        void testNoon() {
            assertThat(TimeSlot.fromTime(LocalTime.of(12, 30))).isEqualTo(TimeSlot.WU);
        }

        @Test
        @DisplayName("LocalTime 23:59 是子时")
        void testBeforeMidnight() {
            assertThat(TimeSlot.fromTime(LocalTime.of(23, 59))).isEqualTo(TimeSlot.ZI);
        }
    }

    @Nested
    @DisplayName("now方法测试")
    class NowTests {

        @Test
        @DisplayName("now返回当前时辰")
        void testNow() {
            TimeSlot now = TimeSlot.now();
            assertThat(now).isNotNull();
        }
    }

    @Nested
    @DisplayName("getFullName方法测试")
    class GetFullNameTests {

        @Test
        @DisplayName("子时全名")
        void testZiFullName() {
            String fullName = TimeSlot.ZI.getFullName();
            assertThat(fullName).contains("子");
            assertThat(fullName).contains("时");
        }

        @ParameterizedTest
        @EnumSource(TimeSlot.class)
        @DisplayName("所有时辰全名包含'时'")
        void testAllFullNamesContainsTime(TimeSlot slot) {
            assertThat(slot.getFullName()).contains("时");
        }
    }

    @Nested
    @DisplayName("getTimeRange方法测试")
    class GetTimeRangeTests {

        @Test
        @DisplayName("子时时间范围")
        void testZiTimeRange() {
            String range = TimeSlot.ZI.getTimeRange();
            assertThat(range).contains("23");
            assertThat(range).contains("1");
        }

        @Test
        @DisplayName("午时时间范围")
        void testWuTimeRange() {
            String range = TimeSlot.WU.getTimeRange();
            assertThat(range).contains("11");
            assertThat(range).contains("13");
        }

        @ParameterizedTest
        @EnumSource(TimeSlot.class)
        @DisplayName("所有时辰都有时间范围")
        void testAllHaveTimeRange(TimeSlot slot) {
            assertThat(slot.getTimeRange()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getStartHour和getEndHour方法测试")
    class HourRangeTests {

        @Test
        @DisplayName("子时 23-1")
        void testZiHours() {
            assertThat(TimeSlot.ZI.getStartHour()).isEqualTo(23);
            assertThat(TimeSlot.ZI.getEndHour()).isEqualTo(1);
        }

        @Test
        @DisplayName("丑时 1-3")
        void testChouHours() {
            assertThat(TimeSlot.CHOU.getStartHour()).isEqualTo(1);
            assertThat(TimeSlot.CHOU.getEndHour()).isEqualTo(3);
        }

        @Test
        @DisplayName("午时 11-13")
        void testWuHours() {
            assertThat(TimeSlot.WU.getStartHour()).isEqualTo(11);
            assertThat(TimeSlot.WU.getEndHour()).isEqualTo(13);
        }
    }
}
