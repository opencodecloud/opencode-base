package cloud.opencode.base.lunar.spi;

import cloud.opencode.base.lunar.LunarDate;
import cloud.opencode.base.lunar.divination.TimeSlot;
import cloud.opencode.base.lunar.divination.YiJi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * DivinationProvider SPI 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
@DisplayName("DivinationProvider SPI 测试")
class DivinationProviderTest {

    @Nested
    @DisplayName("接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("是接口")
        void testIsInterface() {
            assertThat(DivinationProvider.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("包含必要方法")
        void testHasRequiredMethods() throws NoSuchMethodException {
            assertThat(DivinationProvider.class.getMethod("getName")).isNotNull();
            assertThat(DivinationProvider.class.getMethod("getYiJi", LunarDate.class)).isNotNull();
            assertThat(DivinationProvider.class.getMethod("getYiJi", LocalDate.class)).isNotNull();
            assertThat(DivinationProvider.class.getMethod("getSuitable", LocalDate.class)).isNotNull();
            assertThat(DivinationProvider.class.getMethod("getAvoid", LocalDate.class)).isNotNull();
            assertThat(DivinationProvider.class.getMethod("isAuspicious", String.class, LocalDate.class)).isNotNull();
            assertThat(DivinationProvider.class.getMethod("findNextAuspicious", String.class, LocalDate.class, int.class)).isNotNull();
            assertThat(DivinationProvider.class.getMethod("getAuspiciousTimeSlots", LocalDate.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("getDailyFortune默认实现")
        void testGetDailyFortune() {
            DivinationProvider provider = new MockDivinationProvider();
            LocalDate date = LocalDate.of(2024, 1, 1);
            String fortune = provider.getDailyFortune(date);

            assertThat(fortune).isNotNull();
        }

        @Test
        @DisplayName("getPriority默认返回100")
        void testGetPriority() {
            DivinationProvider provider = new MockDivinationProvider();
            assertThat(provider.getPriority()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Mock实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("Mock实现可以正常工作")
        void testMockImplementation() {
            DivinationProvider provider = new MockDivinationProvider();

            assertThat(provider.getName()).isEqualTo("MOCK");
            assertThat(provider.getYiJi(LocalDate.of(2024, 1, 1))).isNotNull();
            assertThat(provider.getSuitable(LocalDate.of(2024, 1, 1))).isNotNull();
            assertThat(provider.getAvoid(LocalDate.of(2024, 1, 1))).isNotNull();
        }

        @Test
        @DisplayName("isAuspicious返回布尔值")
        void testIsAuspicious() {
            DivinationProvider provider = new MockDivinationProvider();
            boolean result = provider.isAuspicious("嫁娶", LocalDate.of(2024, 1, 1));

            assertThat(result).isIn(true, false);
        }
    }

    /**
     * Mock实现用于测试
     */
    private static class MockDivinationProvider implements DivinationProvider {
        @Override
        public String getName() {
            return "MOCK";
        }

        @Override
        public YiJi getYiJi(LunarDate lunar) {
            return new YiJi(List.of("祭祀"), List.of("破土"));
        }

        @Override
        public YiJi getYiJi(LocalDate date) {
            return new YiJi(List.of("祭祀"), List.of("破土"));
        }

        @Override
        public List<String> getSuitable(LocalDate date) {
            return List.of("祭祀", "祈福");
        }

        @Override
        public List<String> getAvoid(LocalDate date) {
            return List.of("破土", "安葬");
        }

        @Override
        public boolean isAuspicious(String activity, LocalDate date) {
            return getSuitable(date).contains(activity);
        }

        @Override
        public LocalDate findNextAuspicious(String activity, LocalDate from, int maxDays) {
            return from.plusDays(1);
        }

        @Override
        public List<TimeSlot> getAuspiciousTimeSlots(LocalDate date) {
            return List.of(TimeSlot.ZI, TimeSlot.WU);
        }
    }
}
