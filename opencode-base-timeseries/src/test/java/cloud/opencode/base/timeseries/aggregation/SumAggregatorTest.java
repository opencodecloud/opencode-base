package cloud.opencode.base.timeseries.aggregation;

import cloud.opencode.base.timeseries.DataPoint;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SumAggregatorTest Tests
 * SumAggregatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("SumAggregator 测试")
class SumAggregatorTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("getInstance返回相同实例")
        void testGetInstanceReturnsSameInstance() {
            assertThat(SumAggregator.getInstance()).isSameAs(SumAggregator.getInstance());
        }
    }

    @Nested
    @DisplayName("aggregate方法测试")
    class AggregateTests {

        @Test
        @DisplayName("计算总和")
        void testSumCalculation() {
            List<DataPoint> points = List.of(
                    DataPoint.of(Instant.now(), 10.0),
                    DataPoint.of(Instant.now(), 20.0),
                    DataPoint.of(Instant.now(), 30.0)
            );

            double result = SumAggregator.getInstance().aggregate(points);

            assertThat(result).isEqualTo(60.0);
        }

        @Test
        @DisplayName("空列表返回0")
        void testEmptyList() {
            assertThat(SumAggregator.getInstance().aggregate(List.of())).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("name方法测试")
    class NameTests {

        @Test
        @DisplayName("name返回SUM")
        void testName() {
            assertThat(SumAggregator.getInstance().name()).isEqualTo("SUM");
        }
    }
}
