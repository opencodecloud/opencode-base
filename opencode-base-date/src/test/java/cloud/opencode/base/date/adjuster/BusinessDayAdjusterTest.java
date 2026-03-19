package cloud.opencode.base.date.adjuster;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * BusinessDayAdjuster 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("BusinessDayAdjuster 测试")
class BusinessDayAdjusterTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("plusDays() 加工作日")
        void testPlusDays() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = friday.with(BusinessDayAdjuster.plusDays(3));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 19)); // Wednesday
        }

        @Test
        @DisplayName("minusDays() 减工作日")
        void testMinusDays() {
            LocalDate wednesday = LocalDate.of(2024, 6, 19);
            LocalDate result = wednesday.with(BusinessDayAdjuster.minusDays(3));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }

        @Test
        @DisplayName("nextBusinessDay() 下一个工作日")
        void testNextBusinessDay() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = friday.with(BusinessDayAdjuster.nextBusinessDay());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 17)); // Monday
        }

        @Test
        @DisplayName("previousBusinessDay() 上一个工作日")
        void testPreviousBusinessDay() {
            LocalDate monday = LocalDate.of(2024, 6, 17);
            LocalDate result = monday.with(BusinessDayAdjuster.previousBusinessDay());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }
    }

    @Nested
    @DisplayName("调整方法测试")
    class AdjustmentTests {

        @Test
        @DisplayName("adjustInto() 零天不变")
        void testAdjustIntoZeroDays() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().days(0).build();
            LocalDate result = (LocalDate) adjuster.adjustInto(date);
            assertThat(result).isEqualTo(date);
        }

        @Test
        @DisplayName("adjustInto() 保留时间组件")
        void testAdjustIntoPreservesTime() {
            LocalDateTime dt = LocalDateTime.of(2024, 6, 14, 14, 30); // Friday
            LocalDateTime result = (LocalDateTime) BusinessDayAdjuster.nextBusinessDay().adjustInto(dt);
            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 6, 17));
            assertThat(result.toLocalTime()).isEqualTo(dt.toLocalTime());
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("isBusinessDay() 工作日判断")
        void testIsBusinessDay() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 12))).isTrue(); // Wednesday
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 15))).isFalse(); // Saturday
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 16))).isFalse(); // Sunday
        }

        @Test
        @DisplayName("isBusinessDay() null抛出异常")
        void testIsBusinessDayNull() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            assertThatThrownBy(() -> adjuster.isBusinessDay(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isHoliday() 假日判断")
        void testIsHoliday() {
            Set<LocalDate> holidays = Set.of(LocalDate.of(2024, 10, 1));
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .holidays(holidays)
                    .build();
            assertThat(adjuster.isHoliday(LocalDate.of(2024, 10, 1))).isTrue();
            assertThat(adjuster.isHoliday(LocalDate.of(2024, 10, 2))).isFalse();
        }

        @Test
        @DisplayName("isSpecialWorkday() 特殊工作日判断")
        void testIsSpecialWorkday() {
            Set<LocalDate> specialWorkdays = Set.of(LocalDate.of(2024, 9, 29)); // Saturday
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .specialWorkdays(specialWorkdays)
                    .build();
            assertThat(adjuster.isSpecialWorkday(LocalDate.of(2024, 9, 29))).isTrue();
            assertThat(adjuster.isSpecialWorkday(LocalDate.of(2024, 9, 28))).isFalse();
        }

        @Test
        @DisplayName("特殊工作日被视为工作日")
        void testSpecialWorkdayIsBusinessDay() {
            Set<LocalDate> specialWorkdays = Set.of(LocalDate.of(2024, 6, 15)); // Saturday
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .specialWorkdays(specialWorkdays)
                    .build();
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 15))).isTrue();
        }

        @Test
        @DisplayName("特殊工作日如果是假日则不是工作日")
        void testSpecialWorkdayIsHoliday() {
            LocalDate date = LocalDate.of(2024, 9, 29);
            Set<LocalDate> specialWorkdays = Set.of(date);
            Set<LocalDate> holidays = Set.of(date);
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .specialWorkdays(specialWorkdays)
                    .holidays(holidays)
                    .build();
            assertThat(adjuster.isBusinessDay(date)).isFalse();
        }
    }

    @Nested
    @DisplayName("计数方法测试")
    class CountingTests {

        @Test
        @DisplayName("countBusinessDays() 计算工作日数")
        void testCountBusinessDays() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            LocalDate start = LocalDate.of(2024, 6, 10); // Monday
            LocalDate end = LocalDate.of(2024, 6, 17); // Monday
            long count = adjuster.countBusinessDays(start, end);
            assertThat(count).isEqualTo(5); // Mon-Fri
        }

        @Test
        @DisplayName("countBusinessDays() 反向返回负数")
        void testCountBusinessDaysReverse() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            LocalDate start = LocalDate.of(2024, 6, 17);
            LocalDate end = LocalDate.of(2024, 6, 10);
            long count = adjuster.countBusinessDays(start, end);
            assertThat(count).isEqualTo(-5);
        }

        @Test
        @DisplayName("countBusinessDays() null抛出异常")
        void testCountBusinessDaysNull() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            assertThatThrownBy(() -> adjuster.countBusinessDays(null, LocalDate.now()))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> adjuster.countBusinessDays(LocalDate.now(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("查找方法测试")
    class FindMethodTests {

        @Test
        @DisplayName("nextFrom() 从给定日期查找下一个工作日")
        void testNextFrom() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = adjuster.nextFrom(friday);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 17)); // Monday
        }

        @Test
        @DisplayName("nextFrom() null抛出异常")
        void testNextFromNull() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            assertThatThrownBy(() -> adjuster.nextFrom(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("previousFrom() 从给定日期查找上一个工作日")
        void testPreviousFrom() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            LocalDate monday = LocalDate.of(2024, 6, 17);
            LocalDate result = adjuster.previousFrom(monday);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }

        @Test
        @DisplayName("previousFrom() null抛出异常")
        void testPreviousFromNull() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            assertThatThrownBy(() -> adjuster.previousFrom(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("nthBusinessDayOfMonth() 查找月份第N个工作日")
        void testNthBusinessDayOfMonth() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            LocalDate result = adjuster.nthBusinessDayOfMonth(2024, 6, 5);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 7)); // Friday (5th business day)
        }

        @Test
        @DisplayName("nthBusinessDayOfMonth() n小于1抛出异常")
        void testNthBusinessDayOfMonthInvalidN() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            assertThatThrownBy(() -> adjuster.nthBusinessDayOfMonth(2024, 6, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nthBusinessDayOfMonth() 超出月份工作日数抛出异常")
        void testNthBusinessDayOfMonthExceedsMonth() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder().build();
            assertThatThrownBy(() -> adjuster.nthBusinessDayOfMonth(2024, 6, 50))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("days() 设置天数")
        void testBuilderDays() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .days(5)
                    .build();
            LocalDate result = friday.with(adjuster);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 21)); // Friday
        }

        @Test
        @DisplayName("weekendDays() 设置周末")
        void testBuilderWeekendDays() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .weekendDays(EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY))
                    .build();
            // Friday should be a weekend
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 14))).isFalse();
            // Sunday should be a workday
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 16))).isTrue();
        }

        @Test
        @DisplayName("middleEastWeekend() 设置中东周末")
        void testBuilderMiddleEastWeekend() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .middleEastWeekend()
                    .build();
            // Friday is weekend in Middle East
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 14))).isFalse();
            // Saturday is also weekend
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 15))).isFalse();
            // Sunday is workday
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 16))).isTrue();
        }

        @Test
        @DisplayName("holidays(Predicate) 设置假日谓词")
        void testBuilderHolidaysPredicate() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .holidays(date -> date.getDayOfMonth() == 1)
                    .build();
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 1))).isFalse();
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 3))).isTrue();
        }

        @Test
        @DisplayName("holidays(Set) 设置假日集合")
        void testBuilderHolidaysSet() {
            Set<LocalDate> holidays = Set.of(
                    LocalDate.of(2024, 10, 1),
                    LocalDate.of(2024, 10, 2)
            );
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .holidays(holidays)
                    .build();
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 10, 1))).isFalse();
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 10, 3))).isTrue();
        }

        @Test
        @DisplayName("specialWorkdays(Predicate) 设置特殊工作日谓词")
        void testBuilderSpecialWorkdaysPredicate() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .specialWorkdays(date -> date.equals(LocalDate.of(2024, 6, 15)))
                    .build();
            // Saturday is usually weekend, but this one is special workday
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 15))).isTrue();
        }

        @Test
        @DisplayName("specialWorkdays(Set) 设置特殊工作日集合")
        void testBuilderSpecialWorkdaysSet() {
            Set<LocalDate> specialWorkdays = Set.of(LocalDate.of(2024, 9, 29));
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .specialWorkdays(specialWorkdays)
                    .build();
            // This Saturday is a makeup workday
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 9, 29))).isTrue();
        }

        @Test
        @DisplayName("weekendDays(null) 使用默认值")
        void testBuilderWeekendDaysNull() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .weekendDays(null)
                    .build();
            // Should use default weekend (Sat, Sun)
            assertThat(adjuster.isBusinessDay(LocalDate.of(2024, 6, 15))).isFalse();
        }

        @Test
        @DisplayName("holidays(Predicate null) 使用默认值")
        void testBuilderHolidaysPredicateNull() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .holidays((java.util.function.Predicate<LocalDate>) null)
                    .build();
            // No holidays
            assertThat(adjuster.isHoliday(LocalDate.of(2024, 10, 1))).isFalse();
        }

        @Test
        @DisplayName("holidays(Set null) 使用默认值")
        void testBuilderHolidaysSetNull() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .holidays((Set<LocalDate>) null)
                    .build();
            assertThat(adjuster.isHoliday(LocalDate.of(2024, 10, 1))).isFalse();
        }

        @Test
        @DisplayName("specialWorkdays(Predicate null) 使用默认值")
        void testBuilderSpecialWorkdaysPredicateNull() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .specialWorkdays((java.util.function.Predicate<LocalDate>) null)
                    .build();
            assertThat(adjuster.isSpecialWorkday(LocalDate.of(2024, 9, 29))).isFalse();
        }

        @Test
        @DisplayName("specialWorkdays(Set null) 使用默认值")
        void testBuilderSpecialWorkdaysSetNull() {
            BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
                    .specialWorkdays((Set<LocalDate>) null)
                    .build();
            assertThat(adjuster.isSpecialWorkday(LocalDate.of(2024, 9, 29))).isFalse();
        }
    }
}
