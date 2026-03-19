package cloud.opencode.base.lunar.spi;

import cloud.opencode.base.lunar.LunarDate;
import cloud.opencode.base.lunar.SolarDate;
import cloud.opencode.base.lunar.calendar.Festival;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * FestivalProvider SPI 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("FestivalProvider SPI 测试")
class FestivalProviderTest {

    @Nested
    @DisplayName("接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("是接口")
        void testIsInterface() {
            assertThat(FestivalProvider.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("包含必要方法")
        void testHasRequiredMethods() throws NoSuchMethodException {
            assertThat(FestivalProvider.class.getMethod("getName")).isNotNull();
            assertThat(FestivalProvider.class.getMethod("getLunarFestivals", int.class, int.class)).isNotNull();
            assertThat(FestivalProvider.class.getMethod("getSolarFestivals", int.class, int.class)).isNotNull();
            assertThat(FestivalProvider.class.getMethod("getFestivals", LunarDate.class)).isNotNull();
            assertThat(FestivalProvider.class.getMethod("getFestivals", SolarDate.class)).isNotNull();
            assertThat(FestivalProvider.class.getMethod("getAllFestivalDates", int.class)).isNotNull();
            assertThat(FestivalProvider.class.getMethod("isFestival", LocalDate.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("getRegion默认返回CN")
        void testGetRegion() {
            FestivalProvider provider = new MockFestivalProvider();
            assertThat(provider.getRegion()).isEqualTo("CN");
        }

        @Test
        @DisplayName("getPriority默认返回100")
        void testGetPriority() {
            FestivalProvider provider = new MockFestivalProvider();
            assertThat(provider.getPriority()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Mock实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("Mock实现可以正常工作")
        void testMockImplementation() {
            FestivalProvider provider = new MockFestivalProvider();

            assertThat(provider.getName()).isEqualTo("MOCK");
            assertThat(provider.getLunarFestivals(1, 1)).isNotNull();
            assertThat(provider.getSolarFestivals(1, 1)).isNotNull();
        }

        @Test
        @DisplayName("getLunarFestivals返回农历节日")
        void testGetLunarFestivals() {
            FestivalProvider provider = new MockFestivalProvider();
            List<Festival> festivals = provider.getLunarFestivals(1, 1);

            assertThat(festivals).isNotNull();
        }

        @Test
        @DisplayName("getSolarFestivals返回公历节日")
        void testGetSolarFestivals() {
            FestivalProvider provider = new MockFestivalProvider();
            List<Festival> festivals = provider.getSolarFestivals(1, 1);

            assertThat(festivals).isNotNull();
            // 元旦
            assertThat(festivals).isNotEmpty();
        }

        @Test
        @DisplayName("isFestival判断是否节日")
        void testIsFestival() {
            FestivalProvider provider = new MockFestivalProvider();

            assertThat(provider.isFestival(LocalDate.of(2024, 1, 1))).isTrue();
            assertThat(provider.isFestival(LocalDate.of(2024, 3, 15))).isFalse();
        }
    }

    /**
     * Mock实现用于测试
     */
    private static class MockFestivalProvider implements FestivalProvider {
        @Override
        public String getName() {
            return "MOCK";
        }

        @Override
        public List<Festival> getLunarFestivals(int month, int day) {
            if (month == 1 && day == 1) {
                return List.of(new Festival("春节", "Spring Festival",
                    Festival.FestivalType.LUNAR, MonthDay.of(1, 1)));
            }
            return List.of();
        }

        @Override
        public List<Festival> getSolarFestivals(int month, int day) {
            if (month == 1 && day == 1) {
                return List.of(new Festival("元旦", "New Year",
                    Festival.FestivalType.SOLAR, MonthDay.of(1, 1)));
            }
            return List.of();
        }

        @Override
        public List<Festival> getFestivals(LunarDate lunar) {
            return getLunarFestivals(lunar.month(), lunar.day());
        }

        @Override
        public List<Festival> getFestivals(SolarDate solar) {
            return getSolarFestivals(solar.month(), solar.day());
        }

        @Override
        public List<LocalDate> getAllFestivalDates(int year) {
            return List.of(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 5, 1),
                LocalDate.of(year, 10, 1)
            );
        }

        @Override
        public boolean isFestival(LocalDate date) {
            return (date.getMonthValue() == 1 && date.getDayOfMonth() == 1) ||
                   (date.getMonthValue() == 5 && date.getDayOfMonth() == 1) ||
                   (date.getMonthValue() == 10 && date.getDayOfMonth() == 1);
        }
    }
}
