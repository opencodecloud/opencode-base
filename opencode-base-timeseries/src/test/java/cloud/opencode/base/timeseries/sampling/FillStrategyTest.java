package cloud.opencode.base.timeseries.sampling;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * FillStrategyTest Tests
 * FillStrategyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("FillStrategy 测试")
class FillStrategyTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含所有预期的填充策略")
        void testAllValuesPresent() {
            assertThat(FillStrategy.values()).containsExactly(
                    FillStrategy.ZERO,
                    FillStrategy.PREVIOUS,
                    FillStrategy.LINEAR,
                    FillStrategy.NAN,
                    FillStrategy.NEXT,
                    FillStrategy.AVERAGE
            );
        }

        @Test
        @DisplayName("共有6个填充策略")
        void testValuesCount() {
            assertThat(FillStrategy.values()).hasSize(6);
        }
    }

    @Nested
    @DisplayName("valueOf方法测试")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf返回正确的枚举值")
        void testValueOfReturnsCorrectValue() {
            assertThat(FillStrategy.valueOf("ZERO")).isEqualTo(FillStrategy.ZERO);
            assertThat(FillStrategy.valueOf("PREVIOUS")).isEqualTo(FillStrategy.PREVIOUS);
            assertThat(FillStrategy.valueOf("LINEAR")).isEqualTo(FillStrategy.LINEAR);
            assertThat(FillStrategy.valueOf("NAN")).isEqualTo(FillStrategy.NAN);
            assertThat(FillStrategy.valueOf("NEXT")).isEqualTo(FillStrategy.NEXT);
            assertThat(FillStrategy.valueOf("AVERAGE")).isEqualTo(FillStrategy.AVERAGE);
        }

        @Test
        @DisplayName("无效名称抛出异常")
        void testValueOfInvalidName() {
            assertThatThrownBy(() -> FillStrategy.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
