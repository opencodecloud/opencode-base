package cloud.opencode.base.lunar.spi;

import cloud.opencode.base.lunar.LunarDate;
import cloud.opencode.base.lunar.SolarDate;
import cloud.opencode.base.lunar.calendar.SolarTerm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CalendarProvider SPI 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("CalendarProvider SPI 测试")
class CalendarProviderTest {

    @Nested
    @DisplayName("接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("是接口")
        void testIsInterface() {
            assertThat(CalendarProvider.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("包含必要方法")
        void testHasRequiredMethods() throws NoSuchMethodException {
            assertThat(CalendarProvider.class.getMethod("getName")).isNotNull();
            assertThat(CalendarProvider.class.getMethod("solarToLunar", SolarDate.class)).isNotNull();
            assertThat(CalendarProvider.class.getMethod("lunarToSolar", LunarDate.class)).isNotNull();
            assertThat(CalendarProvider.class.getMethod("getSolarTerm", LocalDate.class)).isNotNull();
            assertThat(CalendarProvider.class.getMethod("getSolarTermDates", int.class)).isNotNull();
            assertThat(CalendarProvider.class.getMethod("isLeapMonth", int.class, int.class)).isNotNull();
            assertThat(CalendarProvider.class.getMethod("getLeapMonth", int.class)).isNotNull();
            assertThat(CalendarProvider.class.getMethod("getDaysInLunarMonth", int.class, int.class, boolean.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("getPriority默认返回100")
        void testGetPriority() {
            CalendarProvider provider = new MockCalendarProvider();
            assertThat(provider.getPriority()).isEqualTo(100);
        }

        @Test
        @DisplayName("getSupportedYearStart默认返回1900")
        void testGetSupportedYearStart() {
            CalendarProvider provider = new MockCalendarProvider();
            assertThat(provider.getSupportedYearStart()).isEqualTo(1900);
        }

        @Test
        @DisplayName("getSupportedYearEnd默认返回2100")
        void testGetSupportedYearEnd() {
            CalendarProvider provider = new MockCalendarProvider();
            assertThat(provider.getSupportedYearEnd()).isEqualTo(2100);
        }
    }

    @Nested
    @DisplayName("Mock实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("Mock实现可以正常工作")
        void testMockImplementation() {
            CalendarProvider provider = new MockCalendarProvider();

            assertThat(provider.getName()).isEqualTo("MOCK");
            assertThat(provider.solarToLunar(new SolarDate(2024, 1, 1))).isNotNull();
            assertThat(provider.lunarToSolar(new LunarDate(2024, 1, 1, false))).isNotNull();
        }
    }

    /**
     * Mock实现用于测试
     */
    private static class MockCalendarProvider implements CalendarProvider {
        @Override
        public String getName() {
            return "MOCK";
        }

        @Override
        public LunarDate solarToLunar(SolarDate solar) {
            return new LunarDate(solar.year(), 1, 1, false);
        }

        @Override
        public SolarDate lunarToSolar(LunarDate lunar) {
            return new SolarDate(lunar.year(), 1, 31);
        }

        @Override
        public SolarTerm getSolarTerm(LocalDate date) {
            return null;
        }

        @Override
        public List<LocalDate> getSolarTermDates(int year) {
            return List.of();
        }

        @Override
        public boolean isLeapMonth(int year, int month) {
            return false;
        }

        @Override
        public int getLeapMonth(int year) {
            return 0;
        }

        @Override
        public int getDaysInLunarMonth(int year, int month, boolean isLeapMonth) {
            return 30;
        }
    }
}
