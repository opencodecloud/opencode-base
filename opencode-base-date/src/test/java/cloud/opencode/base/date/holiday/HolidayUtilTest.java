package cloud.opencode.base.date.holiday;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * HolidayUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("HolidayUtil 测试")
class HolidayUtilTest {

    /**
     * 测试用的假日提供者
     */
    static class TestHolidayProvider implements HolidayProvider {
        private final Set<LocalDate> holidays = Set.of(
                LocalDate.of(2024, 1, 1),   // New Year
                LocalDate.of(2024, 10, 1),  // National Day
                LocalDate.of(2024, 10, 2),
                LocalDate.of(2024, 10, 3)
        );
        private final Set<LocalDate> adjustedWorkdays = Set.of(
                LocalDate.of(2024, 9, 29),  // Sunday makeup
                LocalDate.of(2024, 10, 12)  // Saturday makeup
        );

        @Override
        public String getName() {
            return "Test Provider";
        }

        @Override
        public String getCountryCode() {
            return "TEST";
        }

        @Override
        public List<Holiday> getHolidays(int year) {
            return holidays.stream()
                    .filter(d -> d.getYear() == year)
                    .map(d -> Holiday.of(d, "Holiday"))
                    .toList();
        }

        @Override
        public List<Holiday> getHolidays(LocalDate start, LocalDate end) {
            return holidays.stream()
                    .filter(d -> !d.isBefore(start) && !d.isAfter(end))
                    .map(d -> Holiday.of(d, "Holiday"))
                    .toList();
        }

        @Override
        public boolean isHoliday(LocalDate date) {
            return holidays.contains(date);
        }

        @Override
        public Optional<Holiday> getHoliday(LocalDate date) {
            return isHoliday(date) ? Optional.of(Holiday.of(date, "Holiday")) : Optional.empty();
        }

        @Override
        public boolean isWorkday(LocalDate date) {
            if (isAdjustedWorkday(date)) {
                return !isHoliday(date);
            }
            DayOfWeek dow = date.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                return false;
            }
            return !isHoliday(date);
        }

        @Override
        public Set<LocalDate> getAdjustedWorkdays(int year) {
            return adjustedWorkdays.stream()
                    .filter(d -> d.getYear() == year)
                    .collect(java.util.stream.Collectors.toSet());
        }

        @Override
        public boolean isAdjustedWorkday(LocalDate date) {
            return adjustedWorkdays.contains(date);
        }

        @Override
        public int[] getSupportedYearRange() {
            return new int[]{2020, 2030};
        }
    }

    private HolidayProvider originalProvider;

    @BeforeEach
    void setUp() {
        // 保存原始提供者
        originalProvider = HolidayUtil.getDefaultProvider();
    }

    @AfterEach
    void tearDown() {
        // 恢复原始提供者
        if (originalProvider != null) {
            HolidayUtil.setDefaultProvider(originalProvider);
        }
    }

    @Nested
    @DisplayName("提供者管理测试")
    class ProviderManagementTests {

        @Test
        @DisplayName("setDefaultProvider() 设置默认提供者")
        void testSetDefaultProvider() {
            TestHolidayProvider provider = new TestHolidayProvider();
            HolidayUtil.setDefaultProvider(provider);
            assertThat(HolidayUtil.getDefaultProvider()).isSameAs(provider);
        }

        @Test
        @DisplayName("setDefaultProvider() null抛出异常")
        void testSetDefaultProviderNull() {
            assertThatThrownBy(() -> HolidayUtil.setDefaultProvider(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getDefaultProvider() 默认使用DefaultHolidayProvider")
        void testGetDefaultProvider() {
            // 已在@BeforeEach中获取，验证不为null
            assertThat(originalProvider).isNotNull();
            assertThat(originalProvider.getName()).isEqualTo("Default");
        }

        @Test
        @DisplayName("registerProvider() 注册提供者")
        void testRegisterProvider() {
            TestHolidayProvider provider = new TestHolidayProvider();
            HolidayUtil.registerProvider(provider);
            assertThat(HolidayUtil.getProvider("TEST")).contains(provider);
        }

        @Test
        @DisplayName("registerProvider() null抛出异常")
        void testRegisterProviderNull() {
            assertThatThrownBy(() -> HolidayUtil.registerProvider(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getProvider() 获取不存在的提供者返回空")
        void testGetProviderNotFound() {
            assertThat(HolidayUtil.getProvider("NONEXISTENT")).isEmpty();
        }
    }

    @Nested
    @DisplayName("假日检查测试")
    class HolidayCheckTests {

        @Test
        @DisplayName("isHoliday() 使用默认提供者")
        void testIsHolidayDefault() {
            // 默认提供者不包含任何假日
            assertThat(HolidayUtil.isHoliday(LocalDate.of(2024, 1, 1))).isFalse();
        }

        @Test
        @DisplayName("isHoliday() 使用自定义提供者")
        void testIsHolidayCustomProvider() {
            TestHolidayProvider provider = new TestHolidayProvider();
            HolidayUtil.setDefaultProvider(provider);

            assertThat(HolidayUtil.isHoliday(LocalDate.of(2024, 1, 1))).isTrue();
            assertThat(HolidayUtil.isHoliday(LocalDate.of(2024, 1, 2))).isFalse();
        }

        @Test
        @DisplayName("isHoliday(date, provider) 使用指定提供者")
        void testIsHolidayWithProvider() {
            TestHolidayProvider provider = new TestHolidayProvider();
            assertThat(HolidayUtil.isHoliday(LocalDate.of(2024, 10, 1), provider)).isTrue();
        }

        @Test
        @DisplayName("getHoliday() 获取假日")
        void testGetHoliday() {
            TestHolidayProvider provider = new TestHolidayProvider();
            HolidayUtil.setDefaultProvider(provider);

            assertThat(HolidayUtil.getHoliday(LocalDate.of(2024, 1, 1))).isPresent();
            assertThat(HolidayUtil.getHoliday(LocalDate.of(2024, 1, 2))).isEmpty();
        }
    }

    @Nested
    @DisplayName("工作日检查测试")
    class WorkdayCheckTests {

        @Test
        @DisplayName("isWorkday() 使用默认提供者")
        void testIsWorkdayDefault() {
            // 默认提供者只考虑周末
            assertThat(HolidayUtil.isWorkday(LocalDate.of(2024, 6, 14))).isTrue(); // Friday
            assertThat(HolidayUtil.isWorkday(LocalDate.of(2024, 6, 15))).isFalse(); // Saturday
        }

        @Test
        @DisplayName("isWorkday() 使用自定义提供者")
        void testIsWorkdayCustomProvider() {
            TestHolidayProvider provider = new TestHolidayProvider();
            HolidayUtil.setDefaultProvider(provider);

            assertThat(HolidayUtil.isWorkday(LocalDate.of(2024, 10, 1))).isFalse(); // Holiday
            assertThat(HolidayUtil.isWorkday(LocalDate.of(2024, 9, 29))).isTrue(); // Adjusted workday (Sunday)
        }

        @Test
        @DisplayName("isWorkday(date, provider) 使用指定提供者")
        void testIsWorkdayWithProvider() {
            TestHolidayProvider provider = new TestHolidayProvider();
            assertThat(HolidayUtil.isWorkday(LocalDate.of(2024, 9, 29), provider)).isTrue();
        }

        @Test
        @DisplayName("isWeekend() 检查周末")
        void testIsWeekend() {
            assertThat(HolidayUtil.isWeekend(LocalDate.of(2024, 6, 15))).isTrue(); // Saturday
            assertThat(HolidayUtil.isWeekend(LocalDate.of(2024, 6, 16))).isTrue(); // Sunday
            assertThat(HolidayUtil.isWeekend(LocalDate.of(2024, 6, 14))).isFalse(); // Friday
        }

        @Test
        @DisplayName("isAdjustedWorkday() 检查调休工作日")
        void testIsAdjustedWorkday() {
            TestHolidayProvider provider = new TestHolidayProvider();
            HolidayUtil.setDefaultProvider(provider);

            assertThat(HolidayUtil.isAdjustedWorkday(LocalDate.of(2024, 9, 29))).isTrue();
            assertThat(HolidayUtil.isAdjustedWorkday(LocalDate.of(2024, 9, 28))).isFalse();
        }
    }

    @Nested
    @DisplayName("工作日计算测试")
    class WorkdayCalculationTests {

        @Test
        @DisplayName("plusWorkdays() 添加工作日")
        void testPlusWorkdays() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = HolidayUtil.plusWorkdays(friday, 3);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 19)); // Wednesday
        }

        @Test
        @DisplayName("plusWorkdays() 零天返回原日期")
        void testPlusWorkdaysZero() {
            LocalDate date = LocalDate.of(2024, 6, 14);
            assertThat(HolidayUtil.plusWorkdays(date, 0)).isEqualTo(date);
        }

        @Test
        @DisplayName("plusWorkdays() null抛出异常")
        void testPlusWorkdaysNull() {
            assertThatThrownBy(() -> HolidayUtil.plusWorkdays(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("minusWorkdays() 减去工作日")
        void testMinusWorkdays() {
            LocalDate wednesday = LocalDate.of(2024, 6, 19);
            LocalDate result = HolidayUtil.minusWorkdays(wednesday, 3);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }

        @Test
        @DisplayName("workdaysBetween() 计算工作日数")
        void testWorkdaysBetween() {
            LocalDate start = LocalDate.of(2024, 6, 10); // Monday
            LocalDate end = LocalDate.of(2024, 6, 17); // Monday
            long count = HolidayUtil.workdaysBetween(start, end);
            assertThat(count).isEqualTo(5); // Mon-Fri
        }

        @Test
        @DisplayName("workdaysBetween() 反向返回负数")
        void testWorkdaysBetweenReverse() {
            LocalDate start = LocalDate.of(2024, 6, 17);
            LocalDate end = LocalDate.of(2024, 6, 10);
            long count = HolidayUtil.workdaysBetween(start, end);
            assertThat(count).isEqualTo(-5);
        }

        @Test
        @DisplayName("workdaysBetween() null抛出异常")
        void testWorkdaysBetweenNull() {
            assertThatThrownBy(() -> HolidayUtil.workdaysBetween(null, LocalDate.now()))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> HolidayUtil.workdaysBetween(LocalDate.now(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("nextWorkday() 获取下一个工作日")
        void testNextWorkday() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = HolidayUtil.nextWorkday(friday);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 17)); // Monday
        }

        @Test
        @DisplayName("nextWorkday() null抛出异常")
        void testNextWorkdayNull() {
            assertThatThrownBy(() -> HolidayUtil.nextWorkday(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("previousWorkday() 获取上一个工作日")
        void testPreviousWorkday() {
            LocalDate monday = LocalDate.of(2024, 6, 17);
            LocalDate result = HolidayUtil.previousWorkday(monday);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }

        @Test
        @DisplayName("previousWorkday() null抛出异常")
        void testPreviousWorkdayNull() {
            assertThatThrownBy(() -> HolidayUtil.previousWorkday(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("nextOrSameWorkday() 当天是工作日返回当天")
        void testNextOrSameWorkdayIsWorkday() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            assertThat(HolidayUtil.nextOrSameWorkday(friday)).isEqualTo(friday);
        }

        @Test
        @DisplayName("nextOrSameWorkday() 当天不是工作日返回下一个工作日")
        void testNextOrSameWorkdayNotWorkday() {
            LocalDate saturday = LocalDate.of(2024, 6, 15);
            assertThat(HolidayUtil.nextOrSameWorkday(saturday)).isEqualTo(LocalDate.of(2024, 6, 17));
        }

        @Test
        @DisplayName("nextOrSameWorkday() null抛出异常")
        void testNextOrSameWorkdayNull() {
            assertThatThrownBy(() -> HolidayUtil.nextOrSameWorkday(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("假日列表测试")
    class HolidayListTests {

        @Test
        @DisplayName("getHolidays(year) 获取指定年份假日")
        void testGetHolidaysByYear() {
            TestHolidayProvider provider = new TestHolidayProvider();
            HolidayUtil.setDefaultProvider(provider);

            List<Holiday> holidays = HolidayUtil.getHolidays(2024);
            assertThat(holidays).hasSize(4);
        }

        @Test
        @DisplayName("getHolidays(start, end) 获取范围内假日")
        void testGetHolidaysInRange() {
            TestHolidayProvider provider = new TestHolidayProvider();
            HolidayUtil.setDefaultProvider(provider);

            List<Holiday> holidays = HolidayUtil.getHolidays(
                    LocalDate.of(2024, 10, 1),
                    LocalDate.of(2024, 10, 3)
            );
            assertThat(holidays).hasSize(3);
        }
    }

    @Nested
    @DisplayName("DefaultHolidayProvider测试")
    class DefaultHolidayProviderTests {

        @Test
        @DisplayName("默认提供者属性")
        void testDefaultProviderProperties() {
            HolidayProvider provider = HolidayUtil.getDefaultProvider();
            assertThat(provider.getName()).isEqualTo("Default");
            assertThat(provider.getCountryCode()).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("默认提供者getHolidays()返回空")
        void testDefaultProviderGetHolidays() {
            HolidayProvider provider = HolidayUtil.getDefaultProvider();
            assertThat(provider.getHolidays(2024)).isEmpty();
            assertThat(provider.getHolidays(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))).isEmpty();
        }

        @Test
        @DisplayName("默认提供者isHoliday()返回false")
        void testDefaultProviderIsHoliday() {
            HolidayProvider provider = HolidayUtil.getDefaultProvider();
            assertThat(provider.isHoliday(LocalDate.of(2024, 1, 1))).isFalse();
        }

        @Test
        @DisplayName("默认提供者getHoliday()返回空")
        void testDefaultProviderGetHoliday() {
            HolidayProvider provider = HolidayUtil.getDefaultProvider();
            assertThat(provider.getHoliday(LocalDate.of(2024, 1, 1))).isEmpty();
        }

        @Test
        @DisplayName("默认提供者isWorkday()只考虑周末")
        void testDefaultProviderIsWorkday() {
            HolidayProvider provider = HolidayUtil.getDefaultProvider();
            assertThat(provider.isWorkday(LocalDate.of(2024, 6, 14))).isTrue(); // Friday
            assertThat(provider.isWorkday(LocalDate.of(2024, 6, 15))).isFalse(); // Saturday
            assertThat(provider.isWorkday(LocalDate.of(2024, 6, 16))).isFalse(); // Sunday
        }

        @Test
        @DisplayName("默认提供者getAdjustedWorkdays()返回空")
        void testDefaultProviderGetAdjustedWorkdays() {
            HolidayProvider provider = HolidayUtil.getDefaultProvider();
            assertThat(provider.getAdjustedWorkdays(2024)).isEmpty();
        }

        @Test
        @DisplayName("默认提供者isAdjustedWorkday()返回false")
        void testDefaultProviderIsAdjustedWorkday() {
            HolidayProvider provider = HolidayUtil.getDefaultProvider();
            assertThat(provider.isAdjustedWorkday(LocalDate.of(2024, 9, 29))).isFalse();
        }

        @Test
        @DisplayName("默认提供者getSupportedYearRange()")
        void testDefaultProviderGetSupportedYearRange() {
            HolidayProvider provider = HolidayUtil.getDefaultProvider();
            int[] range = provider.getSupportedYearRange();
            assertThat(range).hasSize(2);
            assertThat(range[0]).isEqualTo(1970);
            assertThat(range[1]).isEqualTo(2100);
        }

        @Test
        @DisplayName("默认提供者isYearSupported()")
        void testDefaultProviderIsYearSupported() {
            HolidayProvider provider = HolidayUtil.getDefaultProvider();
            assertThat(provider.isYearSupported(2024)).isTrue();
            assertThat(provider.isYearSupported(1969)).isFalse();
            assertThat(provider.isYearSupported(2101)).isFalse();
        }

        @Test
        @DisplayName("默认提供者refresh()无操作")
        void testDefaultProviderRefresh() {
            HolidayProvider provider = HolidayUtil.getDefaultProvider();
            // Should not throw
            assertThatCode(() -> provider.refresh()).doesNotThrowAnyException();
        }
    }
}
