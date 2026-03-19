package cloud.opencode.base.timeseries.sampling;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * AggregationTypeTest Tests
 * AggregationTypeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("AggregationType 测试")
class AggregationTypeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含所有预期的聚合类型")
        void testAllValuesPresent() {
            assertThat(AggregationType.values()).containsExactly(
                    AggregationType.SUM,
                    AggregationType.AVG,
                    AggregationType.MIN,
                    AggregationType.MAX,
                    AggregationType.FIRST,
                    AggregationType.LAST,
                    AggregationType.COUNT
            );
        }

        @Test
        @DisplayName("共有7个聚合类型")
        void testValuesCount() {
            assertThat(AggregationType.values()).hasSize(7);
        }
    }

    @Nested
    @DisplayName("valueOf方法测试")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf返回正确的枚举值")
        void testValueOfReturnsCorrectValue() {
            assertThat(AggregationType.valueOf("SUM")).isEqualTo(AggregationType.SUM);
            assertThat(AggregationType.valueOf("AVG")).isEqualTo(AggregationType.AVG);
            assertThat(AggregationType.valueOf("MIN")).isEqualTo(AggregationType.MIN);
            assertThat(AggregationType.valueOf("MAX")).isEqualTo(AggregationType.MAX);
            assertThat(AggregationType.valueOf("FIRST")).isEqualTo(AggregationType.FIRST);
            assertThat(AggregationType.valueOf("LAST")).isEqualTo(AggregationType.LAST);
            assertThat(AggregationType.valueOf("COUNT")).isEqualTo(AggregationType.COUNT);
        }

        @Test
        @DisplayName("无效名称抛出异常")
        void testValueOfInvalidName() {
            assertThatThrownBy(() -> AggregationType.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("name方法测试")
    class NameTests {

        @Test
        @DisplayName("name返回正确的字符串")
        void testNameReturnsCorrectString() {
            assertThat(AggregationType.SUM.name()).isEqualTo("SUM");
            assertThat(AggregationType.AVG.name()).isEqualTo("AVG");
            assertThat(AggregationType.COUNT.name()).isEqualTo("COUNT");
        }
    }
}
