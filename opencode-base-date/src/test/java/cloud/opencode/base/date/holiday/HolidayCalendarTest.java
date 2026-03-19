package cloud.opencode.base.date.holiday;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * HolidayCalendar 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("HolidayCalendar 测试")
class HolidayCalendarTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("empty() 创建空日历")
        void testEmpty() {
            HolidayCalendar calendar = HolidayCalendar.empty();
            assertThat(calendar.getAllHolidays()).isEmpty();
            assertThat(calendar.getSpecialWorkdays()).isEmpty();
        }

        @Test
        @DisplayName("of(Collection) 从集合创建")
        void testOfCollection() {
            List<Holiday> holidays = List.of(
                    Holiday.of(LocalDate.of(2024, 1, 1), "New Year"),
                    Holiday.of(LocalDate.of(2024, 12, 25), "Christmas")
            );
            HolidayCalendar calendar = HolidayCalendar.of(holidays);
            assertThat(calendar.getAllHolidays()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder() 添加假日")
        void testBuilderAddHoliday() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHoliday(Holiday.of(LocalDate.of(2024, 1, 1), "New Year"))
                    .build();
            assertThat(calendar.isHoliday(LocalDate.of(2024, 1, 1))).isTrue();
        }

        @Test
        @DisplayName("builder() 添加多个假日")
        void testBuilderAddHolidays() {
            List<Holiday> holidays = List.of(
                    Holiday.of(LocalDate.of(2024, 1, 1), "New Year"),
                    Holiday.of(LocalDate.of(2024, 5, 1), "Labor Day")
            );
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHolidays(holidays)
                    .build();
            assertThat(calendar.getAllHolidays()).hasSize(2);
        }

        @Test
        @DisplayName("builder() 添加特殊工作日")
        void testBuilderAddSpecialWorkday() {
            LocalDate specialDay = LocalDate.of(2024, 9, 29); // Sunday
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addSpecialWorkday(specialDay)
                    .build();
            assertThat(calendar.isSpecialWorkday(specialDay)).isTrue();
        }

        @Test
        @DisplayName("builder() 添加多个特殊工作日")
        void testBuilderAddSpecialWorkdays() {
            Set<LocalDate> specialDays = Set.of(
                    LocalDate.of(2024, 9, 29),
                    LocalDate.of(2024, 10, 12)
            );
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addSpecialWorkdays(specialDays)
                    .build();
            assertThat(calendar.getSpecialWorkdays()).hasSize(2);
        }

        @Test
        @DisplayName("builder() 设置周末天数")
        void testBuilderWeekendDays() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .weekendDays(EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY))
                    .build();
            assertThat(calendar.getWeekendDays()).containsExactlyInAnyOrder(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
        }

        @Test
        @DisplayName("builder() 默认周末为周六周日")
        void testBuilderDefaultWeekendDays() {
            HolidayCalendar calendar = HolidayCalendar.builder().build();
            assertThat(calendar.getWeekendDays()).containsExactlyInAnyOrder(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        }

        @Test
        @DisplayName("builder() 设置名称")
        void testBuilderName() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .name("China 2024")
                    .build();
            assertThat(calendar.getName()).isEqualTo("China 2024");
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("isHoliday() 检查是否为假日")
        void testIsHoliday() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHoliday(Holiday.of(LocalDate.of(2024, 1, 1), "New Year"))
                    .build();
            assertThat(calendar.isHoliday(LocalDate.of(2024, 1, 1))).isTrue();
            assertThat(calendar.isHoliday(LocalDate.of(2024, 1, 2))).isFalse();
        }

        @Test
        @DisplayName("isPublicHoliday() 检查是否为公共假日")
        void testIsPublicHoliday() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHoliday(Holiday.of(LocalDate.of(2024, 1, 1), "New Year", Holiday.HolidayType.PUBLIC))
                    .addHoliday(Holiday.of(LocalDate.of(2024, 5, 1), "Bank Holiday", Holiday.HolidayType.BANK))
                    .build();
            assertThat(calendar.isPublicHoliday(LocalDate.of(2024, 1, 1))).isTrue();
            assertThat(calendar.isPublicHoliday(LocalDate.of(2024, 5, 1))).isFalse();
            assertThat(calendar.isPublicHoliday(LocalDate.of(2024, 1, 2))).isFalse();
        }

        @Test
        @DisplayName("isSpecialWorkday() 检查特殊工作日")
        void testIsSpecialWorkday() {
            LocalDate specialDay = LocalDate.of(2024, 9, 29);
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addSpecialWorkday(specialDay)
                    .build();
            assertThat(calendar.isSpecialWorkday(specialDay)).isTrue();
            assertThat(calendar.isSpecialWorkday(LocalDate.of(2024, 9, 28))).isFalse();
        }

        @Test
        @DisplayName("isWeekend() 检查周末")
        void testIsWeekend() {
            HolidayCalendar calendar = HolidayCalendar.builder().build();
            assertThat(calendar.isWeekend(LocalDate.of(2024, 6, 15))).isTrue(); // Saturday
            assertThat(calendar.isWeekend(LocalDate.of(2024, 6, 16))).isTrue(); // Sunday
            assertThat(calendar.isWeekend(LocalDate.of(2024, 6, 14))).isFalse(); // Friday
        }

        @Test
        @DisplayName("isWorkday() 工作日判断")
        void testIsWorkday() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHoliday(Holiday.of(LocalDate.of(2024, 1, 1), "New Year"))
                    .build();
            assertThat(calendar.isWorkday(LocalDate.of(2024, 1, 2))).isTrue(); // Tuesday
            assertThat(calendar.isWorkday(LocalDate.of(2024, 1, 1))).isFalse(); // Holiday
            assertThat(calendar.isWorkday(LocalDate.of(2024, 1, 6))).isFalse(); // Saturday
        }

        @Test
        @DisplayName("isWorkday() 特殊工作日覆盖周末")
        void testIsWorkdaySpecialOverridesWeekend() {
            LocalDate sunday = LocalDate.of(2024, 9, 29);
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addSpecialWorkday(sunday)
                    .build();
            assertThat(calendar.isWorkday(sunday)).isTrue();
        }

        @Test
        @DisplayName("isWorkday() 特殊工作日如果是假日则不是工作日")
        void testIsWorkdaySpecialButHoliday() {
            LocalDate date = LocalDate.of(2024, 9, 29);
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addSpecialWorkday(date)
                    .addHoliday(Holiday.of(date, "Holiday"))
                    .build();
            assertThat(calendar.isWorkday(date)).isFalse();
        }

        @Test
        @DisplayName("getHoliday() 获取假日")
        void testGetHoliday() {
            Holiday newYear = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHoliday(newYear)
                    .build();
            assertThat(calendar.getHoliday(LocalDate.of(2024, 1, 1))).isEqualTo(newYear);
            assertThat(calendar.getHoliday(LocalDate.of(2024, 1, 2))).isNull();
        }

        @Test
        @DisplayName("findHoliday() 获取假日Optional")
        void testFindHoliday() {
            Holiday newYear = Holiday.of(LocalDate.of(2024, 1, 1), "New Year");
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHoliday(newYear)
                    .build();
            assertThat(calendar.findHoliday(LocalDate.of(2024, 1, 1))).contains(newYear);
            assertThat(calendar.findHoliday(LocalDate.of(2024, 1, 2))).isEmpty();
        }
    }

    @Nested
    @DisplayName("过滤方法测试")
    class FilterMethodTests {

        private HolidayCalendar createTestCalendar() {
            return HolidayCalendar.builder()
                    .addHoliday(Holiday.of(LocalDate.of(2024, 1, 1), "New Year", Holiday.HolidayType.PUBLIC))
                    .addHoliday(Holiday.of(LocalDate.of(2024, 5, 1), "Labor Day", Holiday.HolidayType.PUBLIC))
                    .addHoliday(Holiday.of(LocalDate.of(2024, 10, 1), "National Day", Holiday.HolidayType.PUBLIC))
                    .addHoliday(Holiday.of(LocalDate.of(2024, 12, 25), "Christmas", Holiday.HolidayType.RELIGIOUS))
                    .addHoliday(Holiday.of(LocalDate.of(2025, 1, 1), "New Year 2025", Holiday.HolidayType.PUBLIC))
                    .build();
        }

        @Test
        @DisplayName("getAllHolidays() 获取所有假日")
        void testGetAllHolidays() {
            HolidayCalendar calendar = createTestCalendar();
            assertThat(calendar.getAllHolidays()).hasSize(5);
        }

        @Test
        @DisplayName("getHolidays(year) 获取指定年份假日")
        void testGetHolidaysByYear() {
            HolidayCalendar calendar = createTestCalendar();
            assertThat(calendar.getHolidays(2024)).hasSize(4);
            assertThat(calendar.getHolidays(2025)).hasSize(1);
            assertThat(calendar.getHolidays(2023)).isEmpty();
        }

        @Test
        @DisplayName("getHolidays(year, month) 获取指定年月假日")
        void testGetHolidaysByYearMonth() {
            HolidayCalendar calendar = createTestCalendar();
            assertThat(calendar.getHolidays(2024, Month.JANUARY)).hasSize(1);
            assertThat(calendar.getHolidays(2024, Month.FEBRUARY)).isEmpty();
        }

        @Test
        @DisplayName("getHolidaysByType() 获取指定类型假日")
        void testGetHolidaysByType() {
            HolidayCalendar calendar = createTestCalendar();
            assertThat(calendar.getHolidaysByType(Holiday.HolidayType.PUBLIC)).hasSize(4);
            assertThat(calendar.getHolidaysByType(Holiday.HolidayType.RELIGIOUS)).hasSize(1);
            assertThat(calendar.getHolidaysByType(Holiday.HolidayType.BANK)).isEmpty();
        }

        @Test
        @DisplayName("getHolidaysInRange() 获取范围内假日")
        void testGetHolidaysInRange() {
            HolidayCalendar calendar = createTestCalendar();
            List<Holiday> holidays = calendar.getHolidaysInRange(
                    LocalDate.of(2024, 4, 1),
                    LocalDate.of(2024, 10, 31)
            );
            assertThat(holidays).hasSize(2); // Labor Day, National Day
        }
    }

    @Nested
    @DisplayName("工作日计算测试")
    class WorkdayCalculationTests {

        @Test
        @DisplayName("nextWorkday() 获取下一个工作日")
        void testNextWorkday() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHoliday(Holiday.of(LocalDate.of(2024, 1, 1), "New Year"))
                    .build();
            // Friday Dec 29, 2023 -> next workday is Tuesday Jan 2, 2024 (skip weekend and holiday)
            LocalDate friday = LocalDate.of(2023, 12, 29);
            LocalDate result = calendar.nextWorkday(friday);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 2));
        }

        @Test
        @DisplayName("previousWorkday() 获取上一个工作日")
        void testPreviousWorkday() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHoliday(Holiday.of(LocalDate.of(2024, 1, 1), "New Year"))
                    .build();
            // Tuesday Jan 2, 2024 -> previous workday is Friday Dec 29, 2023
            LocalDate tuesday = LocalDate.of(2024, 1, 2);
            LocalDate result = calendar.previousWorkday(tuesday);
            assertThat(result).isEqualTo(LocalDate.of(2023, 12, 29));
        }

        @Test
        @DisplayName("addWorkdays() 向前添加工作日")
        void testAddWorkdaysForward() {
            HolidayCalendar calendar = HolidayCalendar.builder().build();
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = calendar.addWorkdays(friday, 3);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 19)); // Wednesday
        }

        @Test
        @DisplayName("addWorkdays() 向后添加工作日（负数）")
        void testAddWorkdaysBackward() {
            HolidayCalendar calendar = HolidayCalendar.builder().build();
            LocalDate wednesday = LocalDate.of(2024, 6, 19);
            LocalDate result = calendar.addWorkdays(wednesday, -3);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }

        @Test
        @DisplayName("addWorkdays() 零天返回原日期")
        void testAddWorkdaysZero() {
            HolidayCalendar calendar = HolidayCalendar.builder().build();
            LocalDate date = LocalDate.of(2024, 6, 15);
            assertThat(calendar.addWorkdays(date, 0)).isEqualTo(date);
        }

        @Test
        @DisplayName("countWorkdays() 计算工作日数")
        void testCountWorkdays() {
            HolidayCalendar calendar = HolidayCalendar.builder().build();
            LocalDate start = LocalDate.of(2024, 6, 10); // Monday
            LocalDate end = LocalDate.of(2024, 6, 17); // Monday
            long count = calendar.countWorkdays(start, end);
            assertThat(count).isEqualTo(5); // Mon-Fri
        }

        @Test
        @DisplayName("countWorkdays() 反向返回负数")
        void testCountWorkdaysReverse() {
            HolidayCalendar calendar = HolidayCalendar.builder().build();
            LocalDate start = LocalDate.of(2024, 6, 17);
            LocalDate end = LocalDate.of(2024, 6, 10);
            long count = calendar.countWorkdays(start, end);
            assertThat(count).isEqualTo(-5);
        }

        @Test
        @DisplayName("countWorkdays() 考虑假日")
        void testCountWorkdaysWithHolidays() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHoliday(Holiday.of(LocalDate.of(2024, 6, 12), "Holiday"))
                    .build();
            LocalDate start = LocalDate.of(2024, 6, 10);
            LocalDate end = LocalDate.of(2024, 6, 17);
            long count = calendar.countWorkdays(start, end);
            assertThat(count).isEqualTo(4); // Mon, Wed, Thu, Fri (Tue is holiday)
        }
    }

    @Nested
    @DisplayName("谓词方法测试")
    class PredicateMethodTests {

        @Test
        @DisplayName("asHolidayPredicate() 创建假日谓词")
        void testAsHolidayPredicate() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .addHoliday(Holiday.of(LocalDate.of(2024, 1, 1), "New Year"))
                    .build();
            Predicate<LocalDate> predicate = calendar.asHolidayPredicate();
            assertThat(predicate.test(LocalDate.of(2024, 1, 1))).isTrue();
            assertThat(predicate.test(LocalDate.of(2024, 1, 2))).isFalse();
        }

        @Test
        @DisplayName("asWorkdayPredicate() 创建工作日谓词")
        void testAsWorkdayPredicate() {
            HolidayCalendar calendar = HolidayCalendar.builder().build();
            Predicate<LocalDate> predicate = calendar.asWorkdayPredicate();
            assertThat(predicate.test(LocalDate.of(2024, 6, 14))).isTrue(); // Friday
            assertThat(predicate.test(LocalDate.of(2024, 6, 15))).isFalse(); // Saturday
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            HolidayCalendar calendar = HolidayCalendar.builder()
                    .name("China 2024")
                    .addHoliday(Holiday.of(LocalDate.of(2024, 1, 1), "New Year"))
                    .addSpecialWorkday(LocalDate.of(2024, 9, 29))
                    .build();
            String str = calendar.toString();
            assertThat(str).contains("China 2024");
            assertThat(str).contains("holidays=1");
            assertThat(str).contains("specialWorkdays=1");
        }
    }
}
