package cloud.opencode.base.date.holiday;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * HolidayProvider 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("HolidayProvider 测试")
class HolidayProviderTest {

    /**
     * 测试用的HolidayProvider实现
     */
    private static class TestHolidayProvider implements HolidayProvider {
        private final int minYear;
        private final int maxYear;
        private final List<Holiday> holidays;

        TestHolidayProvider(int minYear, int maxYear) {
            this.minYear = minYear;
            this.maxYear = maxYear;
            this.holidays = new ArrayList<>();
        }

        TestHolidayProvider(int minYear, int maxYear, List<Holiday> holidays) {
            this.minYear = minYear;
            this.maxYear = maxYear;
            this.holidays = holidays;
        }

        @Override
        public String getName() {
            return "TestProvider";
        }

        @Override
        public String getCountryCode() {
            return "TEST";
        }

        @Override
        public List<Holiday> getHolidays(int year) {
            return holidays.stream()
                    .filter(h -> h.getDate().getYear() == year)
                    .toList();
        }

        @Override
        public List<Holiday> getHolidays(LocalDate start, LocalDate end) {
            return holidays.stream()
                    .filter(h -> !h.getDate().isBefore(start) && !h.getDate().isAfter(end))
                    .toList();
        }

        @Override
        public boolean isHoliday(LocalDate date) {
            return holidays.stream().anyMatch(h -> h.getDate().equals(date));
        }

        @Override
        public Optional<Holiday> getHoliday(LocalDate date) {
            return holidays.stream()
                    .filter(h -> h.getDate().equals(date))
                    .findFirst();
        }

        @Override
        public boolean isWorkday(LocalDate date) {
            return !isHoliday(date) && date.getDayOfWeek().getValue() <= 5;
        }

        @Override
        public Set<LocalDate> getAdjustedWorkdays(int year) {
            return Set.of();
        }

        @Override
        public boolean isAdjustedWorkday(LocalDate date) {
            return false;
        }

        @Override
        public int[] getSupportedYearRange() {
            return new int[]{minYear, maxYear};
        }
    }

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("getName() 获取提供者名称")
        void testGetName() {
            HolidayProvider provider = new TestHolidayProvider(2020, 2030);
            assertThat(provider.getName()).isEqualTo("TestProvider");
        }

        @Test
        @DisplayName("getCountryCode() 获取国家代码")
        void testGetCountryCode() {
            HolidayProvider provider = new TestHolidayProvider(2020, 2030);
            assertThat(provider.getCountryCode()).isEqualTo("TEST");
        }

        @Test
        @DisplayName("getHolidays(int year) 获取年度节假日")
        void testGetHolidaysByYear() {
            List<Holiday> holidays = List.of(
                    Holiday.of(LocalDate.of(2024, 1, 1), "New Year"),
                    Holiday.of(LocalDate.of(2024, 10, 1), "National Day"),
                    Holiday.of(LocalDate.of(2025, 1, 1), "New Year 2025")
            );
            HolidayProvider provider = new TestHolidayProvider(2020, 2030, holidays);

            List<Holiday> result = provider.getHolidays(2024);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("getHolidays(start, end) 获取日期范围内节假日")
        void testGetHolidaysByRange() {
            List<Holiday> holidays = List.of(
                    Holiday.of(LocalDate.of(2024, 1, 1), "New Year"),
                    Holiday.of(LocalDate.of(2024, 5, 1), "Labor Day"),
                    Holiday.of(LocalDate.of(2024, 10, 1), "National Day")
            );
            HolidayProvider provider = new TestHolidayProvider(2020, 2030, holidays);

            List<Holiday> result = provider.getHolidays(
                    LocalDate.of(2024, 4, 1),
                    LocalDate.of(2024, 6, 30)
            );
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Labor Day");
        }

        @Test
        @DisplayName("isHoliday() 检查是否为节假日")
        void testIsHoliday() {
            List<Holiday> holidays = List.of(
                    Holiday.of(LocalDate.of(2024, 1, 1), "New Year")
            );
            HolidayProvider provider = new TestHolidayProvider(2020, 2030, holidays);

            assertThat(provider.isHoliday(LocalDate.of(2024, 1, 1))).isTrue();
            assertThat(provider.isHoliday(LocalDate.of(2024, 1, 2))).isFalse();
        }

        @Test
        @DisplayName("getHoliday() 获取节假日详情")
        void testGetHoliday() {
            List<Holiday> holidays = List.of(
                    Holiday.of(LocalDate.of(2024, 1, 1), "New Year")
            );
            HolidayProvider provider = new TestHolidayProvider(2020, 2030, holidays);

            Optional<Holiday> result = provider.getHoliday(LocalDate.of(2024, 1, 1));
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("New Year");

            Optional<Holiday> empty = provider.getHoliday(LocalDate.of(2024, 1, 2));
            assertThat(empty).isEmpty();
        }

        @Test
        @DisplayName("isWorkday() 检查是否为工作日")
        void testIsWorkday() {
            List<Holiday> holidays = List.of(
                    Holiday.of(LocalDate.of(2024, 1, 1), "New Year") // Monday
            );
            HolidayProvider provider = new TestHolidayProvider(2020, 2030, holidays);

            // Holiday is not workday
            assertThat(provider.isWorkday(LocalDate.of(2024, 1, 1))).isFalse();
            // Regular weekday is workday
            assertThat(provider.isWorkday(LocalDate.of(2024, 1, 2))).isTrue();
            // Weekend is not workday
            assertThat(provider.isWorkday(LocalDate.of(2024, 1, 6))).isFalse(); // Saturday
        }

        @Test
        @DisplayName("getAdjustedWorkdays() 获取调休工作日")
        void testGetAdjustedWorkdays() {
            HolidayProvider provider = new TestHolidayProvider(2020, 2030);
            Set<LocalDate> result = provider.getAdjustedWorkdays(2024);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("isAdjustedWorkday() 检查是否为调休工作日")
        void testIsAdjustedWorkday() {
            HolidayProvider provider = new TestHolidayProvider(2020, 2030);
            assertThat(provider.isAdjustedWorkday(LocalDate.of(2024, 1, 1))).isFalse();
        }

        @Test
        @DisplayName("getSupportedYearRange() 获取支持的年份范围")
        void testGetSupportedYearRange() {
            HolidayProvider provider = new TestHolidayProvider(2020, 2030);
            int[] range = provider.getSupportedYearRange();
            assertThat(range).containsExactly(2020, 2030);
        }
    }

    @Nested
    @DisplayName("Default方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("isYearSupported() 在范围内返回true")
        void testIsYearSupportedInRange() {
            HolidayProvider provider = new TestHolidayProvider(2020, 2030);
            assertThat(provider.isYearSupported(2020)).isTrue();
            assertThat(provider.isYearSupported(2025)).isTrue();
            assertThat(provider.isYearSupported(2030)).isTrue();
        }

        @Test
        @DisplayName("isYearSupported() 在范围外返回false")
        void testIsYearSupportedOutOfRange() {
            HolidayProvider provider = new TestHolidayProvider(2020, 2030);
            assertThat(provider.isYearSupported(2019)).isFalse();
            assertThat(provider.isYearSupported(2031)).isFalse();
        }

        @Test
        @DisplayName("refresh() 默认实现不抛异常")
        void testRefreshDefault() {
            HolidayProvider provider = new TestHolidayProvider(2020, 2030);
            assertThatCode(provider::refresh).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空节假日列表")
        void testEmptyHolidays() {
            HolidayProvider provider = new TestHolidayProvider(2020, 2030, List.of());
            assertThat(provider.getHolidays(2024)).isEmpty();
            assertThat(provider.isHoliday(LocalDate.of(2024, 1, 1))).isFalse();
        }

        @Test
        @DisplayName("单一年份范围")
        void testSingleYearRange() {
            HolidayProvider provider = new TestHolidayProvider(2024, 2024);
            assertThat(provider.isYearSupported(2024)).isTrue();
            assertThat(provider.isYearSupported(2023)).isFalse();
            assertThat(provider.isYearSupported(2025)).isFalse();
        }

        @Test
        @DisplayName("日期范围边界")
        void testDateRangeBoundary() {
            List<Holiday> holidays = List.of(
                    Holiday.of(LocalDate.of(2024, 1, 1), "Start"),
                    Holiday.of(LocalDate.of(2024, 1, 15), "Middle"),
                    Holiday.of(LocalDate.of(2024, 1, 31), "End")
            );
            HolidayProvider provider = new TestHolidayProvider(2020, 2030, holidays);

            // 包含边界日期
            List<Holiday> result = provider.getHolidays(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );
            assertThat(result).hasSize(3);

            // 排除边界日期
            List<Holiday> partial = provider.getHolidays(
                    LocalDate.of(2024, 1, 2),
                    LocalDate.of(2024, 1, 30)
            );
            assertThat(partial).hasSize(1);
        }
    }
}
