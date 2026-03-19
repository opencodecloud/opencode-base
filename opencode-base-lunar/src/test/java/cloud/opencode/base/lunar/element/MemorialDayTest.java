package cloud.opencode.base.lunar.element;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * MemorialDay (纪念日) 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("MemorialDay (纪念日) 测试")
class MemorialDayTest {

    @Nested
    @DisplayName("Record基本测试")
    class RecordBasicTests {

        @Test
        @DisplayName("创建公历纪念日")
        void testCreateSolar() {
            MemorialDay day = new MemorialDay("生日", LocalDate.of(2000, 6, 15), false, "我的生日");

            assertThat(day.name()).isEqualTo("生日");
            assertThat(day.date()).isEqualTo(LocalDate.of(2000, 6, 15));
            assertThat(day.isLunar()).isFalse();
            assertThat(day.description()).isEqualTo("我的生日");
        }

        @Test
        @DisplayName("创建农历纪念日")
        void testCreateLunar() {
            MemorialDay day = new MemorialDay("农历生日", LocalDate.of(2000, 5, 15), true, "农历生日");

            assertThat(day.name()).isEqualTo("农历生日");
            assertThat(day.isLunar()).isTrue();
        }

        @Test
        @DisplayName("equals比较")
        void testEquals() {
            MemorialDay d1 = new MemorialDay("生日", LocalDate.of(2000, 6, 15), false, "desc");
            MemorialDay d2 = new MemorialDay("生日", LocalDate.of(2000, 6, 15), false, "desc");
            MemorialDay d3 = new MemorialDay("纪念日", LocalDate.of(2000, 6, 16), false, "desc");

            assertThat(d1).isEqualTo(d2);
            assertThat(d1).isNotEqualTo(d3);
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfFactoryTests {

        @Test
        @DisplayName("of创建公历纪念日")
        void testOf() {
            MemorialDay day = MemorialDay.of("生日", LocalDate.of(2000, 6, 15));

            assertThat(day.name()).isEqualTo("生日");
            assertThat(day.date()).isEqualTo(LocalDate.of(2000, 6, 15));
            assertThat(day.isLunar()).isFalse();
        }

        @Test
        @DisplayName("of带描述")
        void testOfWithDescription() {
            MemorialDay day = MemorialDay.of("生日", LocalDate.of(2000, 6, 15), "我的生日");

            assertThat(day.name()).isEqualTo("生日");
            assertThat(day.description()).isEqualTo("我的生日");
        }
    }

    @Nested
    @DisplayName("ofLunar工厂方法测试")
    class OfLunarFactoryTests {

        @Test
        @DisplayName("ofLunar创建农历纪念日")
        void testOfLunar() {
            MemorialDay day = MemorialDay.ofLunar("农历生日", LocalDate.of(2000, 5, 15));

            assertThat(day.name()).isEqualTo("农历生日");
            assertThat(day.isLunar()).isTrue();
        }
    }

    @Nested
    @DisplayName("yearsSince方法测试")
    class YearsSinceTests {

        @Test
        @DisplayName("计算经过年数")
        void testYearsSince() {
            MemorialDay day = MemorialDay.of("生日", LocalDate.of(2000, 6, 15));
            long years = day.yearsSince();

            assertThat(years).isGreaterThanOrEqualTo(24); // 假设测试在2024年或之后运行
        }

        @Test
        @DisplayName("未来日期返回负数")
        void testFutureDate() {
            MemorialDay day = MemorialDay.of("未来", LocalDate.of(2100, 1, 1));
            long years = day.yearsSince();

            assertThat(years).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("daysUntilNextOccurrence方法测试")
    class DaysUntilNextOccurrenceTests {

        @Test
        @DisplayName("计算距下次天数")
        void testDaysUntilNext() {
            MemorialDay day = MemorialDay.of("元旦", LocalDate.of(2000, 1, 1));
            long days = day.daysUntilNextOccurrence();

            assertThat(days).isBetween(0L, 365L);
        }
    }

    @Nested
    @DisplayName("isToday方法测试")
    class IsTodayTests {

        @Test
        @DisplayName("今天返回true")
        void testIsToday() {
            LocalDate today = LocalDate.now();
            MemorialDay day = MemorialDay.of("今天", LocalDate.of(2000, today.getMonthValue(), today.getDayOfMonth()));

            assertThat(day.isToday()).isTrue();
        }

        @Test
        @DisplayName("其他日期返回false")
        void testNotToday() {
            LocalDate notToday = LocalDate.now().plusDays(1);
            MemorialDay day = MemorialDay.of("明天", LocalDate.of(2000, notToday.getMonthValue(), notToday.getDayOfMonth()));

            assertThat(day.isToday()).isFalse();
        }
    }

    @Nested
    @DisplayName("getAnniversaryNumber方法测试")
    class GetAnniversaryNumberTests {

        @Test
        @DisplayName("计算周年数")
        void testGetAnniversaryNumber() {
            MemorialDay day = MemorialDay.of("建国", LocalDate.of(1949, 10, 1));
            int anniversary = day.getAnniversaryNumber();

            assertThat(anniversary).isGreaterThanOrEqualTo(75); // 2024年是75周年
        }
    }

    @Nested
    @DisplayName("getNextOccurrence方法测试")
    class GetNextOccurrenceTests {

        @Test
        @DisplayName("获取下次发生日期")
        void testGetNextOccurrence() {
            MemorialDay day = MemorialDay.of("生日", LocalDate.of(2000, 6, 15));
            LocalDate next = day.getNextOccurrence();

            assertThat(next).isNotNull();
            assertThat(next.getMonthValue()).isEqualTo(6);
            assertThat(next.getDayOfMonth()).isEqualTo(15);
            assertThat(next.isAfter(LocalDate.now()) || next.isEqual(LocalDate.now())).isTrue();
        }
    }
}
