package cloud.opencode.base.lunar.calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Holiday (法定假日) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("Holiday (法定假日) 测试")
class HolidayTest {

    @Nested
    @DisplayName("Record基本测试")
    class RecordBasicTests {

        @Test
        @DisplayName("创建假日")
        void testCreate() {
            Holiday holiday = new Holiday("测试假日", "Test Holiday", 3, false);

            assertThat(holiday.name()).isEqualTo("测试假日");
            assertThat(holiday.englishName()).isEqualTo("Test Holiday");
            assertThat(holiday.vacationDays()).isEqualTo(3);
            assertThat(holiday.isLunar()).isFalse();
        }

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            Holiday h1 = new Holiday("假日1", "H1", 3, false);
            Holiday h2 = new Holiday("假日1", "H1", 3, false);
            Holiday h3 = new Holiday("假日2", "H2", 5, true);

            assertThat(h1).isEqualTo(h2);
            assertThat(h1).isNotEqualTo(h3);
        }
    }

    @Nested
    @DisplayName("预定义假日测试")
    class PredefinedHolidaysTests {

        @Test
        @DisplayName("元旦假日")
        void testNewYear() {
            assertThat(Holiday.NEW_YEAR).isNotNull();
            assertThat(Holiday.NEW_YEAR.name()).isEqualTo("元旦");
            assertThat(Holiday.NEW_YEAR.vacationDays()).isEqualTo(1);
            assertThat(Holiday.NEW_YEAR.isLunar()).isFalse();
        }

        @Test
        @DisplayName("春节假日")
        void testSpringFestival() {
            assertThat(Holiday.SPRING_FESTIVAL).isNotNull();
            assertThat(Holiday.SPRING_FESTIVAL.name()).isEqualTo("春节");
            assertThat(Holiday.SPRING_FESTIVAL.vacationDays()).isEqualTo(3);
            assertThat(Holiday.SPRING_FESTIVAL.isLunar()).isTrue();
        }

        @Test
        @DisplayName("清明假日")
        void testQingming() {
            assertThat(Holiday.QINGMING).isNotNull();
            assertThat(Holiday.QINGMING.name()).isEqualTo("清明节");
            assertThat(Holiday.QINGMING.vacationDays()).isEqualTo(1);
        }

        @Test
        @DisplayName("劳动节假日")
        void testLaborDay() {
            assertThat(Holiday.LABOR_DAY).isNotNull();
            assertThat(Holiday.LABOR_DAY.name()).isEqualTo("劳动节");
            assertThat(Holiday.LABOR_DAY.vacationDays()).isEqualTo(1);
            assertThat(Holiday.LABOR_DAY.isLunar()).isFalse();
        }

        @Test
        @DisplayName("端午假日")
        void testDragonBoat() {
            assertThat(Holiday.DRAGON_BOAT).isNotNull();
            assertThat(Holiday.DRAGON_BOAT.name()).isEqualTo("端午节");
            assertThat(Holiday.DRAGON_BOAT.vacationDays()).isEqualTo(1);
            assertThat(Holiday.DRAGON_BOAT.isLunar()).isTrue();
        }

        @Test
        @DisplayName("中秋假日")
        void testMidAutumn() {
            assertThat(Holiday.MID_AUTUMN).isNotNull();
            assertThat(Holiday.MID_AUTUMN.name()).isEqualTo("中秋节");
            assertThat(Holiday.MID_AUTUMN.vacationDays()).isEqualTo(1);
            assertThat(Holiday.MID_AUTUMN.isLunar()).isTrue();
        }

        @Test
        @DisplayName("国庆假日")
        void testNationalDay() {
            assertThat(Holiday.NATIONAL_DAY).isNotNull();
            assertThat(Holiday.NATIONAL_DAY.name()).isEqualTo("国庆节");
            assertThat(Holiday.NATIONAL_DAY.vacationDays()).isEqualTo(3);
            assertThat(Holiday.NATIONAL_DAY.isLunar()).isFalse();
        }
    }

    @Nested
    @DisplayName("getAll方法测试")
    class GetAllTests {

        @Test
        @DisplayName("返回所有法定假日")
        void testGetAll() {
            List<Holiday> holidays = Holiday.getAll();

            assertThat(holidays).isNotEmpty();
            assertThat(holidays).contains(Holiday.NEW_YEAR);
            assertThat(holidays).contains(Holiday.SPRING_FESTIVAL);
            assertThat(holidays).contains(Holiday.NATIONAL_DAY);
        }

        @Test
        @DisplayName("包含7个法定假日")
        void testContainsSevenHolidays() {
            List<Holiday> holidays = Holiday.getAll();
            assertThat(holidays).hasSize(7);
        }
    }

    @Nested
    @DisplayName("getTotalVacationDays方法测试")
    class GetTotalVacationDaysTests {

        @Test
        @DisplayName("计算总假日天数")
        void testTotalDays() {
            int total = Holiday.getTotalVacationDays();

            // 1 + 3 + 1 + 1 + 1 + 1 + 3 = 11
            assertThat(total).isEqualTo(11);
        }
    }

    @Nested
    @DisplayName("isLunarBased和isSolarBased方法测试")
    class TypeCheckTests {

        @Test
        @DisplayName("春节是农历假日")
        void testSpringFestivalLunar() {
            assertThat(Holiday.SPRING_FESTIVAL.isLunarBased()).isTrue();
            assertThat(Holiday.SPRING_FESTIVAL.isSolarBased()).isFalse();
        }

        @Test
        @DisplayName("国庆是公历假日")
        void testNationalDaySolar() {
            assertThat(Holiday.NATIONAL_DAY.isLunarBased()).isFalse();
            assertThat(Holiday.NATIONAL_DAY.isSolarBased()).isTrue();
        }

        @Test
        @DisplayName("元旦是公历假日")
        void testNewYearSolar() {
            assertThat(Holiday.NEW_YEAR.isSolarBased()).isTrue();
        }

        @Test
        @DisplayName("中秋是农历假日")
        void testMidAutumnLunar() {
            assertThat(Holiday.MID_AUTUMN.isLunarBased()).isTrue();
        }
    }

    @Nested
    @DisplayName("假日天数统计测试")
    class VacationDaysStatsTests {

        @Test
        @DisplayName("农历假日总天数")
        void testLunarHolidayDays() {
            List<Holiday> holidays = Holiday.getAll();
            int lunarDays = holidays.stream()
                .filter(Holiday::isLunarBased)
                .mapToInt(Holiday::vacationDays)
                .sum();

            // 春节3 + 端午1 + 中秋1 = 5
            assertThat(lunarDays).isEqualTo(5);
        }

        @Test
        @DisplayName("公历假日总天数")
        void testSolarHolidayDays() {
            List<Holiday> holidays = Holiday.getAll();
            int solarDays = holidays.stream()
                .filter(Holiday::isSolarBased)
                .mapToInt(Holiday::vacationDays)
                .sum();

            // 元旦1 + 清明1 + 劳动节1 + 国庆3 = 6
            assertThat(solarDays).isEqualTo(6);
        }
    }
}
