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
 * WorkdayAdjuster 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("WorkdayAdjuster 测试")
class WorkdayAdjusterTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("plusDays(int) 加工作日")
        void testPlusDays() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = friday.with(WorkdayAdjuster.plusDays(3));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 19)); // Wednesday
        }

        @Test
        @DisplayName("plusDays(int, Set) 加工作日并考虑假日")
        void testPlusDaysWithHolidays() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            Set<LocalDate> holidays = Set.of(LocalDate.of(2024, 6, 17)); // Monday is holiday
            LocalDate result = friday.with(WorkdayAdjuster.plusDays(1, holidays));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 18)); // Tuesday (skips weekend and holiday)
        }

        @Test
        @DisplayName("plusDays(int, Set) null假日集合抛出异常")
        void testPlusDaysWithNullHolidays() {
            assertThatThrownBy(() -> WorkdayAdjuster.plusDays(1, (Set<LocalDate>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("plusDays(int, Predicate) 加工作日并使用假日谓词")
        void testPlusDaysWithPredicate() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = friday.with(WorkdayAdjuster.plusDays(1, date -> date.getDayOfMonth() == 17));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 18)); // Tuesday
        }

        @Test
        @DisplayName("minusDays() 减工作日")
        void testMinusDays() {
            LocalDate wednesday = LocalDate.of(2024, 6, 19);
            LocalDate result = wednesday.with(WorkdayAdjuster.minusDays(3));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }

        @Test
        @DisplayName("nextWorkday() 下一个工作日")
        void testNextWorkday() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = friday.with(WorkdayAdjuster.nextWorkday());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 17)); // Monday
        }

        @Test
        @DisplayName("nextWorkday(Set) 下一个工作日考虑假日")
        void testNextWorkdayWithHolidays() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            Set<LocalDate> holidays = Set.of(LocalDate.of(2024, 6, 17));
            LocalDate result = friday.with(WorkdayAdjuster.nextWorkday(holidays));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 18));
        }

        @Test
        @DisplayName("nextWorkday(Set) null假日集合抛出异常")
        void testNextWorkdayWithNullHolidays() {
            assertThatThrownBy(() -> WorkdayAdjuster.nextWorkday(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("previousWorkday() 上一个工作日")
        void testPreviousWorkday() {
            LocalDate monday = LocalDate.of(2024, 6, 17);
            LocalDate result = monday.with(WorkdayAdjuster.previousWorkday());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }

        @Test
        @DisplayName("nearestWorkday() 最近工作日")
        void testNearestWorkday() {
            // Saturday - nearest is Friday
            LocalDate saturday = LocalDate.of(2024, 6, 15);
            LocalDate result1 = saturday.with(WorkdayAdjuster.nearestWorkday());
            assertThat(result1).isEqualTo(LocalDate.of(2024, 6, 14));

            // Sunday - nearest is Monday
            LocalDate sunday = LocalDate.of(2024, 6, 16);
            LocalDate result2 = sunday.with(WorkdayAdjuster.nearestWorkday());
            assertThat(result2).isEqualTo(LocalDate.of(2024, 6, 17));

            // Workday - returns same
            LocalDate wednesday = LocalDate.of(2024, 6, 12);
            LocalDate result3 = wednesday.with(WorkdayAdjuster.nearestWorkday());
            assertThat(result3).isEqualTo(wednesday);
        }
    }

    @Nested
    @DisplayName("调整方法测试")
    class AdjustmentTests {

        @Test
        @DisplayName("adjustInto() 零天不变")
        void testAdjustIntoZeroDays() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder().days(0).build();
            LocalDate result = (LocalDate) adjuster.adjustInto(date);
            assertThat(result).isEqualTo(date);
        }

        @Test
        @DisplayName("adjustInto() 保留时间组件")
        void testAdjustIntoPreservesTime() {
            LocalDateTime dt = LocalDateTime.of(2024, 6, 14, 14, 30); // Friday
            LocalDateTime result = (LocalDateTime) WorkdayAdjuster.nextWorkday().adjustInto(dt);
            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 6, 17));
            assertThat(result.toLocalTime()).isEqualTo(dt.toLocalTime());
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("isWorkday() 工作日判断")
        void testIsWorkday() {
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder().build();
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 12))).isTrue(); // Wednesday
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 15))).isFalse(); // Saturday
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 16))).isFalse(); // Sunday
        }

        @Test
        @DisplayName("isWorkday() 考虑假日")
        void testIsWorkdayWithHolidays() {
            Set<LocalDate> holidays = Set.of(LocalDate.of(2024, 6, 12));
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder()
                    .holidays(holidays)
                    .build();
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 12))).isFalse();
        }

        @Test
        @DisplayName("countWorkdays() 计算工作日数")
        void testCountWorkdays() {
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder().build();
            LocalDate start = LocalDate.of(2024, 6, 10); // Monday
            LocalDate end = LocalDate.of(2024, 6, 17); // Monday
            long count = adjuster.countWorkdays(start, end);
            assertThat(count).isEqualTo(5); // Mon-Fri
        }

        @Test
        @DisplayName("countWorkdays() 反向返回负数")
        void testCountWorkdaysReverse() {
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder().build();
            LocalDate start = LocalDate.of(2024, 6, 17);
            LocalDate end = LocalDate.of(2024, 6, 10);
            long count = adjuster.countWorkdays(start, end);
            assertThat(count).isEqualTo(-5);
        }

        @Test
        @DisplayName("countWorkdays() 考虑假日")
        void testCountWorkdaysWithHolidays() {
            Set<LocalDate> holidays = Set.of(LocalDate.of(2024, 6, 12));
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder()
                    .holidays(holidays)
                    .build();
            LocalDate start = LocalDate.of(2024, 6, 10);
            LocalDate end = LocalDate.of(2024, 6, 17);
            long count = adjuster.countWorkdays(start, end);
            assertThat(count).isEqualTo(4); // Mon, Tue is holiday, Wed, Thu, Fri
        }
    }

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("days() 设置天数")
        void testBuilderDays() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder()
                    .days(5)
                    .build();
            LocalDate result = friday.with(adjuster);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 21)); // Friday
        }

        @Test
        @DisplayName("weekendDays() 设置周末")
        void testBuilderWeekendDays() {
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder()
                    .weekendDays(EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY))
                    .build();
            // Friday should be a weekend
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 14))).isFalse();
            // Sunday should be a workday
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 16))).isTrue();
        }

        @Test
        @DisplayName("middleEastWeekend() 设置中东周末")
        void testBuilderMiddleEastWeekend() {
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder()
                    .middleEastWeekend()
                    .build();
            // Friday is weekend in Middle East
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 14))).isFalse();
            // Saturday is also weekend
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 15))).isFalse();
            // Sunday is workday
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 16))).isTrue();
        }

        @Test
        @DisplayName("holidays(Predicate) 设置假日谓词")
        void testBuilderHolidaysPredicate() {
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder()
                    .holidays(date -> date.getDayOfMonth() == 1)
                    .build();
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 1))).isFalse();
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 3))).isTrue();
        }

        @Test
        @DisplayName("holidays(Set) 设置假日集合")
        void testBuilderHolidaysSet() {
            Set<LocalDate> holidays = Set.of(
                    LocalDate.of(2024, 10, 1),
                    LocalDate.of(2024, 10, 2)
            );
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder()
                    .holidays(holidays)
                    .build();
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 10, 1))).isFalse();
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 10, 3))).isTrue();
        }

        @Test
        @DisplayName("weekendDays(null) 使用默认值")
        void testBuilderWeekendDaysNull() {
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder()
                    .weekendDays(null)
                    .build();
            // Should use default weekend (Sat, Sun)
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 15))).isFalse();
        }

        @Test
        @DisplayName("holidays(Predicate null) 使用默认值")
        void testBuilderHolidaysPredicateNull() {
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder()
                    .holidays((java.util.function.Predicate<LocalDate>) null)
                    .build();
            // Should not treat any day as holiday
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 12))).isTrue();
        }

        @Test
        @DisplayName("holidays(Set null) 使用默认值")
        void testBuilderHolidaysSetNull() {
            WorkdayAdjuster adjuster = WorkdayAdjuster.builder()
                    .holidays((Set<LocalDate>) null)
                    .build();
            assertThat(adjuster.isWorkday(LocalDate.of(2024, 6, 12))).isTrue();
        }
    }
}
